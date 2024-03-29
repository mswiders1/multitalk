package pl.multitalk.android.managers;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.content.Context;
import android.util.Log;
import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.managers.messages.GetMessage;
import pl.multitalk.android.managers.messages.HiMessage;
import pl.multitalk.android.managers.messages.LivMessage;
import pl.multitalk.android.managers.messages.LogMessage;
import pl.multitalk.android.managers.messages.Message;
import pl.multitalk.android.managers.messages.MsgMessage;
import pl.multitalk.android.managers.messages.MtxMessage;
import pl.multitalk.android.managers.messages.OutMessage;
import pl.multitalk.android.managers.messages.P2PMessage;
import pl.multitalk.android.managers.messages.internal.DiscoveryPacketReceivedMessage;
import pl.multitalk.android.managers.messages.internal.FinishMessage;
import pl.multitalk.android.managers.messages.internal.SendMessageDelayed;
import pl.multitalk.android.managers.messages.internal.SendMessageToAllMessage;
import pl.multitalk.android.managers.messages.internal.SendMessageToClient;
import pl.multitalk.android.model.RBMtxPair;
import pl.multitalk.android.util.Constants;
import pl.multitalk.android.util.DigestUtil;
import pl.multitalk.android.util.NetworkUtil;
import pl.multitalk.android.util.NetworkUtil.WifiNotEnabledException;

/**
 * Manager sieci Multitalk. Zarządza połączeniami z innymi użytkownikami.
 * @author Michał Kołodziejski
 */
public class MultitalkNetworkManager {

    private final long LOG_SEND_DELAY = 5000;
    private final long LIV_SEND_PERIOD = 10000;
    
    private Context context;
    private BroadcastNetworkManager broadcastNetworkManager;
    private TCPIPNetworkManager tcpipNetworkManager;
    private Timer sendLogTimer;
    private Timer sendLivTimer;
    private Timer removeDeadClientsTimer;
    private BlockingQueue<Message> messageQueue = null;
    private MessageDispatcher messageDispatcher;
    
    /*
     * Dane niezbędne do zalogowania do sieci Multitalk
     */
    private UserInfo userInfo;
    private boolean isLoggedIn;
    
    /*
     * Informacje o innych uzytkownikach
     */
    private List<UserInfo> users;
    private List<UserInfo> notLoggedUsers;
    
    /**
     * Manager wiadomości
     */
    private MessageManager messageManager;
    private Map<UserInfo, LivMessage> lastReceivedLivMessages;
    
    
    /**
     * Tworzy zarządcę sieci
     */
    public MultitalkNetworkManager(Context context){
        this.context = context;
        this.messageQueue = new ArrayBlockingQueue<Message>(30);
        this.broadcastNetworkManager = new BroadcastNetworkManager(context, this);
        this.tcpipNetworkManager = new TCPIPNetworkManager(context, this);
        this.messageDispatcher = new MessageDispatcher(messageQueue);
        
        this.messageDispatcher.start();
        
        isLoggedIn = false;
        userInfo = null;
        users = new ArrayList<UserInfo>();
        notLoggedUsers = new ArrayList<UserInfo>();
    }
    
    
    /**
     * Przeprowadza akcję logowania do sieci Multitalk
     * @param login login użytkownika
     * @param peerIpAddress opcjonalny adres IP
     */
    public void logIn(String login, String peerIpAddress){
        if(isLoggedIn){
            logout();
        }
        
        UserInfo newUserInfo = new UserInfo();
        
        if(!NetworkUtil.isWifiEnabled(context)){
            // wifi wyłączone
            return;
        }

        int ipInt = 0;
        try {
            ipInt = NetworkUtil.getIPaddressAsInt(context);
            if(ipInt == 0){
                // brak połączenia z siecią
                return;
            }
            
            newUserInfo.setMacAddress(NetworkUtil.getWifiMAC(context));
            newUserInfo.setIpAddress(NetworkUtil.getIPaddressAsString(context));
            
        } catch (WifiNotEnabledException e) {
            return;
        }

        // oczekiwanie na klientów
        tcpipNetworkManager.startListeningForConnections();
        
        newUserInfo.setUsername(login);

        StringBuffer sb = new StringBuffer();
        sb.append(newUserInfo.getMacAddress());
        sb.append(newUserInfo.getIpAddress());
        sb.append(newUserInfo.getUsername());
        
        newUserInfo.setUid(DigestUtil.getBase64(DigestUtil.getSHA1(sb.toString())));
        
        userInfo = newUserInfo;
        
        Log.d(Constants.DEBUG_TAG, "logIn| mac: "+userInfo.getMacAddress()
                +", ip: "+userInfo.getIpAddress()
                +", username: "+userInfo.getUsername()
                +" | UID (before encoding): "+sb.toString()
                +" | UID: "+userInfo.getUid());
        
        // nowy manager wiadomości
        messageManager = new MessageManager(userInfo);
        lastReceivedLivMessages = new HashMap<UserInfo, LivMessage>();
        
        if(peerIpAddress == null){
            // wysłanie UDP discovery
            broadcastNetworkManager.sendDiscoveryPackets();
            
        } else {
            // bezpośrednie połączenie
            UserInfo peerUser = new UserInfo();
            peerUser.setIpAddress(peerIpAddress);
            peerUser.setUid(String.valueOf(System.currentTimeMillis()+"."+Math.random()));
            addNotLoggedUserInfo(peerUser);
            tcpipNetworkManager.connectToClient(peerUser);
            
            // wysłanie P2P message
            P2PMessage p2pMessage = new P2PMessage();
            p2pMessage.setSenderInfo(userInfo);
            p2pMessage.setRecipientInfo(peerUser);
            tcpipNetworkManager.sendMessage(p2pMessage);
        }
        
        // timer
        sendLogTimer = new Timer();
        sendLogTimer.schedule(new SendLogMessageTimerTask(), LOG_SEND_DELAY);
    }
    
    
    /**
     * Wylogowuje z sieci Multitalk
     */
    public void logout(){
        stopSendingLivMessage();
        stopRemovingDeadClients();
        
        if(isLoggedIn){
            // wysłanie OUT message
            OutMessage outMessage = new OutMessage();
            outMessage.setSenderInfo(userInfo);
            outMessage.setUserInfo(userInfo);
            tcpipNetworkManager.sendMessageToAll(outMessage);
        }
        
        broadcastNetworkManager.stopBroadcastListening();
        tcpipNetworkManager.stopListeningForConnections();
        tcpipNetworkManager.disconnectAllClients();
        isLoggedIn = false;
        userInfo = null;
        users = new ArrayList<UserInfo>();
        notLoggedUsers = new ArrayList<UserInfo>();
    }
    
    
    /**
     * Kończy działanie
     */
    public void destroy(){
        logout();
        try {
            messageQueue.put(new FinishMessage());
        } catch (InterruptedException e) {
            Log.e(Constants.ERROR_TAG, "InterruptedException at destroy()");
        }
        if(sendLogTimer != null){
            sendLogTimer.cancel();
        }
    }
    
    
    /**
     * Dodaje informację o nowym użytkowniku
     * @param newUserInfo informacje o nowym użytkowniku
     */
    private synchronized void addUserInfo(UserInfo newUserInfo){
        if(!users.contains(newUserInfo)){
            users.add(newUserInfo);
        }
    }
    
    
    /**
     * Usuwa informację o użytkowniku
     * @param userInfoToRemove informacje o użytkowniku do usunięcia
     */
    private synchronized void removeUserInfo(UserInfo userInfoToRemove){
        if(users.contains(userInfoToRemove)){
            users.remove(userInfoToRemove);
        }
    }
    
    
    /**
     * Sprawdza czy użytkownik został już dodany
     * @param user informacje o użytkowniku
     */
    private synchronized boolean containsUserInfo(UserInfo user){
        if(users.contains(user)){
            return true;
        }
        return false;
    }
    
    
    /**
     * Zwraca pełne informacje o zalogowanym użytkowniku
     * @param user użytkownik
     * @return pełne dane o zalogowanym użytkowniku
     */
    private synchronized UserInfo getUserInfo(UserInfo user){
        if(containsUserInfo(user)){
            return users.get(users.indexOf(user));
        }
        return null;
    }
    
    
    /**
     * Sprawdza czy użytkownik został już dodany
     * @param user informacje o użytkowniku
     */
    private synchronized boolean containsNotLoggedUserInfo(UserInfo user){
        if(notLoggedUsers.contains(user)){
            return true;
        }
        return false;
    }
    
    
    /**
     * Sprawdza czy mamy już info o użytkowniku z podanym IP
     * @param ipAddress adres IP użytkownika
     * @return true jeżeli mamy info o takim użytkowniku
     */
    private synchronized boolean containsUserWithIP(String ipAddress, boolean onlyLoggedUsers){
        // zalogowani
        for(UserInfo user : users){
            if(ipAddress.equals(user.getIpAddress())){
                return true;
            }
        }
        
        if (onlyLoggedUsers){
            // nie ma
            return false;
        }
        
        // niezalogowani
        for(UserInfo user : notLoggedUsers){
            if(user.getIpAddress() != null && ipAddress.equals(user.getIpAddress())){
                return true;
            }
        }
        
        return false;
    }
    
    
    /**
     * Dodaje informację o nowym niezalogowanym użytkowniku
     * @param newUserInfo informacje o nowym niezalogowanym użytkowniku
     */
    private synchronized void addNotLoggedUserInfo(UserInfo newUserInfo){
        if(!notLoggedUsers.contains(newUserInfo)){
            notLoggedUsers.add(newUserInfo);
        }
    }
    
    
    /**
     * Usuwa informację o niezalogowanym użytkowniku
     * @param userInfoToRemove informacje o niezalogowanym użytkowniku do usunięcia
     */
    private synchronized void removeNotLoggedUserInfo(UserInfo userInfoToRemove){
        if(notLoggedUsers.contains(userInfoToRemove)){
            notLoggedUsers.remove(userInfoToRemove);
        }
    }
    
    
    /**
     * Zwraca informację o niezalogowanym użytkowniku
     * @param user informacje o niezalogowanym użytkowniku do usunięcia
     * @return zwraca informacje o niezalogowanym użytkowniku
     */
    private synchronized UserInfo getNotLoggedUserInfo(UserInfo user){
        if(notLoggedUsers.contains(user)){
            return notLoggedUsers.get(notLoggedUsers.indexOf(user));
        }
        return null;
    }
    
    
    /**
     * Zwraca informację, czy zalogowano do sieci
     * @return true jeżeli zalogowano do sieci, false w przeciwnym przypadku
     */
    public synchronized boolean isLoggedIn(){
        return isLoggedIn;
    }
    
    
    /**
     * Ustawia informację, czy zalogowano do sieci
     * @param loggedIn informacja czy zalogowano do sieci
     */
    public synchronized void setLoggedIn(boolean loggedIn){
        isLoggedIn = loggedIn;

        // rozpoczęcie nasłuchiwania po broadcast-cie
        broadcastNetworkManager.startBroadcastListening();
        startSendingLivMessage();
//        startRemoveingDeadClients();
    }
    
    
    /**
     * Zwraca listę użytkowników
     * @return lista użytkowników
     */
    public List<UserInfo> getUsers(){
        return users;
    }
    
    
    /**
     * Dodaje nowy komunikat do przetworzenia
     * @param message komunikat
     */
    public void putMessage(Message message){
        try {
            messageQueue.put(message);
        } catch (InterruptedException e) {
            Log.e(Constants.ERROR_TAG, "InterruptedException at MultitalkNetworkManager#putMessage");
            return;
        }
    }
    
    
    /**
     * Rozpoczyna wysyłanie komunikatów LIV
     */
    private void startSendingLivMessage(){
        sendLivTimer = new Timer();
        sendLivTimer.schedule(new SendLivMessageTimerTask(), LIV_SEND_PERIOD/2, LIV_SEND_PERIOD);
    }
    
    
    /**
     * Kończy wysyłanie komunikatów LIV
     */
    private void stopSendingLivMessage(){
        if(sendLivTimer != null){
            sendLivTimer.cancel();
        }
    }
    
    
    /**
     * Rozpoczyna usuwanie martwych klientów
     */
    @SuppressWarnings("unused")
    private void startRemoveingDeadClients(){
        removeDeadClientsTimer = new Timer();
        removeDeadClientsTimer.schedule(new RemoveDeadClientsTimerTask(), 10000, 10000);
    }
    
    
    /**
     * Kończy usuwanie martwych klientów
     */
    private void stopRemovingDeadClients(){
        if(removeDeadClientsTimer != null){
            removeDeadClientsTimer.cancel();
            removeDeadClientsTimer = null;
        }
    }
    
    /**
     * Wysyła HII message do użytkownika
     * @param recipientInfo odbiorca
     */
    private void sendHiiMessage(UserInfo recipientInfo){
        HiMessage hiMessage = new HiMessage();
        hiMessage.setSenderInfo(userInfo);
        hiMessage.setUserInfo(userInfo);
        hiMessage.setRecipientInfo(recipientInfo);
        // kopia
        List<UserInfo> loggedUsers = new ArrayList<UserInfo>();
        for(UserInfo user : users){
            loggedUsers.add(new UserInfo(user));
        }
        hiMessage.setLoggedUsers(loggedUsers);
        
        tcpipNetworkManager.sendMessage(hiMessage);
    }
    
    
    /**
     * Zwraca konwersację z uzytkownikiem
     * @param user użytkownik
     * @return lista wiadomości w porządku chronologicznym
     */
    public List<MsgMessage> getConversation(UserInfo user){
        return messageManager.getConversation(user);
    }
    
    
    /* *********************************************** */
    /*   handlery dla komunikatów
    /* *********************************************** */
    
    /**
     * Obsługuje komunikat HII
     * @param message komunikat
     */
    public void handleHiMessage(HiMessage message){
        // uaktualniamy info o kliencie
        UserInfo senderUserInfo = message.getSenderInfo();
        UserInfo clientUserInfo = message.getUserInfo();
        clientUserInfo.setIpAddress(senderUserInfo.getIpAddress());
        
        // dodajemy i aktualizujemy w network managerze
        boolean userExisted = containsUserInfo(clientUserInfo);
        
        MultitalkNetworkManager.this.removeNotLoggedUserInfo(senderUserInfo);
        MultitalkNetworkManager.this.addUserInfo(clientUserInfo);
        messageManager.addUser(clientUserInfo);
        
        if(!userExisted){
            tcpipNetworkManager.updateUserInfo(senderUserInfo, clientUserInfo);
            if(isLoggedIn()){
                // HII przyszedł po rozesłaniu LOG - wysyłam tylko jemu
                LogMessage logMessage = new LogMessage();
                logMessage.setRecipientInfo(clientUserInfo);
                logMessage.setUserInfo(userInfo);
                logMessage.setSenderInfo(userInfo);
                tcpipNetworkManager.sendMessage(logMessage);
            }
            
        } else if (!senderUserInfo.equals(clientUserInfo) 
                && tcpipNetworkManager.hasConnection(senderUserInfo) 
                && tcpipNetworkManager.hasConnection(clientUserInfo)){
            // podwójne połączenie, dropujemy nie-klienta
            tcpipNetworkManager.disconnectClient(senderUserInfo);
            
        }
        
        for(UserInfo user : message.getLoggedUsers()){
            if(userInfo.equals(user) || userInfo.getIpAddress().equals(user.getIpAddress())){
                // pomijamy siebie
                continue;
            }
            
            if(containsUserInfo(user) || containsUserWithIP(user.getIpAddress(), false)){
                // jeżeli mamy juz takiego uzytkownika na liscie zalogowanych
                // lub mamy już info o użytkowniku pod takim adresem IP
                continue;
            }
            
            MultitalkNetworkManager.this.addUserInfo(user);
            messageManager.addUser(user);

            tcpipNetworkManager.connectToClient(user);
            P2PMessage p2pMessage = new P2PMessage();
            p2pMessage.setSenderInfo(userInfo);
            p2pMessage.setRecipientInfo(user);
            tcpipNetworkManager.sendMessage(p2pMessage);
            
        }
    }
    

    /**
     * Obsługuje komunikat discovery
     * @param message komunikat
     */
    public void handleDiscoveryPacketReceived(DiscoveryPacketReceivedMessage message){
        // sprawdzenie czy mamy już info o takim użytkowniku
        boolean userExists = containsUserWithIP(message.getSenderInfo().getIpAddress(), false);
        boolean loggedUserExists = containsUserWithIP(message.getSenderInfo().getIpAddress(), true); 
        
        if(userExists && loggedUserExists){
            // rozłączamy
            Log.d(Constants.DEBUG_TAG, "Użytkownik istniał, rozłączam stare połączenie");
            tcpipNetworkManager.disconnectClientWithIP(message.getSenderInfo());
            // nie usuwam macierzy - może to być ten sam user co stary
            
        } else if(userExists && !loggedUserExists){
            // mamy połączenie z niezalogowanym - ignorujemy (wielokrotny broadcast)
            Log.d(Constants.DEBUG_TAG, "mamy połączenie z niezalogowanym - ignorujemy (wielokrotny broadcast)");
            return;
            
        } else{
            Log.d(Constants.DEBUG_TAG, "Pierwsza informacja o użytkowniku: "
                    +message.getSenderInfo().getIpAddress()+", nawiązuję połączenie...");
        }
        
        // dodajemy usera
        MultitalkNetworkManager.this.addNotLoggedUserInfo(message.getSenderInfo());
        tcpipNetworkManager.connectToClient(message.getSenderInfo());
            
        // w odpowiedzi wysyłamy HiMessage
        sendHiiMessage(message.getSenderInfo());

    }
    

    /**
     * Obsługuje komunikat LOG
     * @param message komunikat
     */
    public void handleLogMessage(LogMessage message){
        // uaktualniamy info o kliencie
        UserInfo senderUserInfo = message.getSenderInfo();
        UserInfo clientUserInfo = message.getUserInfo();
        
        
        if(userInfo.equals(clientUserInfo)){
            // wiadomość o zalogowaniu siebie - ignorujemy
            Log.d(Constants.DEBUG_TAG, "wiadomość o zalogowaniu siebie - ignorujemy");
            return;
        }
        
        if(userInfo.getIpAddress().equals(senderUserInfo.getIpAddress())){
            // wiadomość od siebie o zalogowaniu siebie
            // ignorujemy i usuwamy połączenie ze sobą
            Log.d(Constants.DEBUG_TAG, "wiadomość od siebie o zalogowaniu siebie - ignorujemy");
            removeNotLoggedUserInfo(senderUserInfo);
            tcpipNetworkManager.disconnectClient(senderUserInfo);
            return;
        }
        
        
        if(containsUserInfo(clientUserInfo)){
            // już wiemy, że ten user jest zalogowany
            Log.d(Constants.DEBUG_TAG, "wiadomość o użytkowniku, który jest już zalogowany");
            
            if(clientUserInfo.getIpAddress().equals(senderUserInfo.getIpAddress())){
                Log.d(Constants.DEBUG_TAG, "wiadomość od tego samego użytkownika - ponowny login");

                removeNotLoggedUserInfo(senderUserInfo);
                tcpipNetworkManager.updateUserInfo(senderUserInfo, clientUserInfo);
                
                // wysłamy MTX message
                MtxMessage mtxMessage = new MtxMessage();
                mtxMessage.setSenderInfo(userInfo);
                mtxMessage.setRecipientInfo(clientUserInfo);
                mtxMessage.setMtxPair(messageManager.getRBMatrix());
                tcpipNetworkManager.sendMessage(mtxMessage);
            }
            return;
        }
        
        if(containsUserInfo(senderUserInfo)){
            // informacja LOG od użytkownika zalogowanego - forward
            Log.d(Constants.DEBUG_TAG, "informacja LOG od użytkownika zalogowanego - forward");
            
            // albo nie mamy bezpośredniego połączenia z logującym się użytkownikiem
            // albo mamy bezpośrednie połączenie a forward dostaliśmy wcześniej niż od logującego
            if(!containsUserWithIP(clientUserInfo.getIpAddress(), false)){
                // nie mamy bezpośredniego połączenia z użytkownikiem logującym
                // dodajemy usera
                Log.d(Constants.DEBUG_TAG, "nie mamy bezpośredniego połączenia z użytkownikiem logującym - dodajemy usera");
                
                addUserInfo(clientUserInfo);
                messageManager.addUser(clientUserInfo);
//                tcpipNetworkManager.connectToClient(clientUserInfo);
                
            } else {
                // mamy bezpośrednie połączenie - dostaliśmy forward wcześniej niż bezpośrednią informację
                // olewamy, czekamy na info z bezpośredniego połączenia
                Log.d(Constants.DEBUG_TAG, "mamy bezpośrednie połączenie - dostaliśmy forward wcześniej niż bezpośrednią informację");
                return;
                
            }
            
        } else {
            // informacja LOG od użytkownika niezalogowanego
            Log.d(Constants.DEBUG_TAG, "informacja LOG od użytkownika niezalogowanego");
            
            removeNotLoggedUserInfo(senderUserInfo);
            addUserInfo(clientUserInfo);
            tcpipNetworkManager.updateUserInfo(senderUserInfo, clientUserInfo);
            messageManager.addUser(clientUserInfo);
            
            // wysłamy MTX message
            MtxMessage mtxMessage = new MtxMessage();
            mtxMessage.setSenderInfo(userInfo);
            mtxMessage.setRecipientInfo(clientUserInfo);
            mtxMessage.setMtxPair(messageManager.getRBMatrix());
            tcpipNetworkManager.sendMessage(mtxMessage);
            
        }
        
        // forwardujemy LOG
        LogMessage forwardLogMessage = (LogMessage) message.getClone();
        forwardLogMessage.setSenderInfo(userInfo);
        tcpipNetworkManager.sendMessageToAll(forwardLogMessage);
    }
    

    /**
     * Obsługuje komunikat MTX
     * @param message komunikat
     */
    public void handleMtxMessage(MtxMessage message){
        messageManager.handleUserRBMatrix(message.getMtxPair());
    }
    

    /**
     * Obsługuje komunikat OUT
     * @param message komunikat
     */
    public void handleOutMessage(OutMessage message){
        boolean userExisted = containsUserInfo(message.getUserInfo());
        
        messageManager.removeUser(message.getUserInfo());
        tcpipNetworkManager.disconnectClient(message.getUserInfo());
        removeUserInfo(message.getUserInfo());
        
        if(userExisted){
            SendMessageToAllMessage smtaMessage = new SendMessageToAllMessage();
            smtaMessage.setMessageToSend(message);
            smtaMessage.setSenderInfo(userInfo);
            putMessage(smtaMessage);
        }
    }
    
    
    /**
     * Obsługuje komunikat P2P
     * @param message komunikat
     */
    public void handleP2PMessage(P2PMessage message){
        // dodajemy usera
        MultitalkNetworkManager.this.addNotLoggedUserInfo(message.getSenderInfo());
        
        // w odpowiedzi wysyłamy HiMessage
        sendHiiMessage(message.getSenderInfo());
    }
    
    
    /**
     * Obsługuje komunikat MSG
     * @param message komunikat
     */
    public void handleMsgMessage(MsgMessage message){
        if(messageManager.getMessage(message.getMsgSender(), message.getMsgId()) != null){
            // już ją mamy
            return;
        }
        // aktualizacja danych uzytkownika wysylajacego
        if(containsUserInfo(message.getMsgSender())){
            message.setMsgSender(users.get(users.indexOf(message.getMsgSender())));
        }
        messageManager.addMessage(message);
        
        // przesyłamy dalej
        SendMessageToAllMessage smtaMsg = new SendMessageToAllMessage();
        smtaMsg.setMessageToSend(message);
        smtaMsg.setSenderInfo(userInfo);
        putMessage(smtaMsg);
    }
    
    
    /**
     * Obsługuje komunikat GET
     * @param message komunikat
     */
    public void handleGetMessage(GetMessage message){
        MsgMessage msgToSend = messageManager.getMessage(message.getUserInfo(), message.getMsgId());
        if(msgToSend == null){
            return;
        }
        msgToSend.setSenderInfo(userInfo);
        msgToSend.setRecipientInfo(message.getSenderInfo());
        tcpipNetworkManager.sendMessage(msgToSend);
    }
    
    
    /**
     * Obsługuje komunikat SendMessageToClient
     * @param message komunikat
     */
    public void handleSendMessageToClientMessage(SendMessageToClient message){
        RBMtxPair mtxPair = messageManager.getRBMatrix();
        
        MsgMessage msgMessage = new MsgMessage();
        msgMessage.setSenderInfo(userInfo);
        msgMessage.setMsgSender(userInfo);
        msgMessage.setMsgReceiver(message.getRecipientInfo());
        msgMessage.setContent(message.getContent());
        msgMessage.setMsgId(mtxPair.getMtx().get(0).get(0));
        msgMessage.setTimeVec(mtxPair.getMtx().get(0));
        msgMessage.setUsersOrder(mtxPair.getMtxUsersOrder());
        messageManager.addMessage(msgMessage);
        tcpipNetworkManager.sendMessageToAll(msgMessage);
    }
    
    
    public void handleSendDelayedMessage(SendMessageDelayed messageDelayed){
        SendMessageToClient message = messageDelayed.getMsgToSend();
        RBMtxPair mtxPair = messageManager.getRBMatrix();
        
        MsgMessage msgMessage = new MsgMessage();
        msgMessage.setSenderInfo(userInfo);
        msgMessage.setMsgSender(userInfo);
        msgMessage.setMsgReceiver(message.getRecipientInfo());
        msgMessage.setContent(message.getContent());
        msgMessage.setMsgId(mtxPair.getMtx().get(0).get(0));
        msgMessage.setTimeVec(mtxPair.getMtx().get(0));
        msgMessage.setUsersOrder(mtxPair.getMtxUsersOrder());
        messageManager.addMessage(msgMessage);
        
        Thread sendDelayedTh = new SendMessageDelayedThread(msgMessage, messageDelayed.getDelay());
        sendDelayedTh.start();
        
    }
    
    /**
     * Obsługuje komunikat LIV
     * @param message komunikat
     */
    public void handleLivMessage(LivMessage message){
        UserInfo msgUserInfo = message.getUserInfo();
        
        if(msgUserInfo.equals(userInfo)){
            // pomijam siebie samego
            return;
        }
        
        if(!containsUserInfo(msgUserInfo)){
            // info od niezalogowanego lub niewiadomo kogo
            if(containsNotLoggedUserInfo(msgUserInfo)){
                // mamy go w niezalogowanych
                if(!tcpipNetworkManager.hasConnection(msgUserInfo)){
                    // nie mamy do niego połączenia - był rozłączony po LIV timeout
                    // łączymy i oznaczamy jako zalogowany
                    
                    Log.d(Constants.DEBUG_TAG, "LIV message od martwego klienta - wskrzeszam"
                            +" ("+msgUserInfo.getUid()+")");
                    
                    UserInfo user = getNotLoggedUserInfo(msgUserInfo);
                    removeNotLoggedUserInfo(msgUserInfo);
                    addUserInfo(user);
//                    tcpipNetworkManager.connectToClient(user);
                }
            }
        }
        
        SendMessageToAllMessage smtaMessage = null;
        
        message.setReceiveDateToNow();
        synchronized (lastReceivedLivMessages) {
            LivMessage lastLiv = lastReceivedLivMessages.get(msgUserInfo);
            if(lastLiv != null){
                if(lastLiv.getSeq() < message.getSeq()){
                    // najnowszy LIV - przesyłamy dalej
                    smtaMessage = new SendMessageToAllMessage();
                    smtaMessage.setMessageToSend(message);
                    smtaMessage.setSenderInfo(userInfo);
                }
            }
            
            lastReceivedLivMessages.remove(msgUserInfo);
            lastReceivedLivMessages.put(msgUserInfo, message);
        }
        
        // forward
        if(smtaMessage != null){
            putMessage(smtaMessage);
        }
    }
    
    
    /**
     * Zadanie wysłania komunikatu logowania
     */
    class SendLogMessageTimerTask extends TimerTask{

        @Override
        public void run() {
            // wysyłamy do wszystkich
            LogMessage logMessage = new LogMessage();
            logMessage.setSenderInfo(userInfo);
            logMessage.setUserInfo(userInfo);
            
            SendMessageToAllMessage smtaMessage = new SendMessageToAllMessage();
            smtaMessage.setMessageToSend(logMessage);
            putMessage(smtaMessage);
            
            // ustawienie znacznika
            MultitalkNetworkManager.this.setLoggedIn(true);
        }
        
    }
    
    
    /**
     * Zadanie wysłania komunikatu alive
     */
    class SendLivMessageTimerTask extends TimerTask{

        private int seq;
        
        public SendLivMessageTimerTask() {
            seq = 0;
        }
        
        @Override
        public void run() {
            if(isLoggedIn()){
                ++seq;
                
                // wysyłamy do wszystkich
                LivMessage message = new LivMessage();
                message.setSenderInfo(userInfo);
                message.setUserInfo(userInfo);
                message.setSeq(seq);
                
                SendMessageToAllMessage smtaMessage = new SendMessageToAllMessage();
                smtaMessage.setMessageToSend(message);
                putMessage(smtaMessage);
            }
        }
        
    }
    
    
    /**
     * Zadanie usuwania klientów, od których nie otrzymujemy komunikatów LIV
     */
    class RemoveDeadClientsTimerTask extends TimerTask {
        @Override
        public void run() {
            GregorianCalendar lastValidDate = new GregorianCalendar();
            lastValidDate.setTimeInMillis(System.currentTimeMillis());
            lastValidDate.add(GregorianCalendar.SECOND, -30);
            
            synchronized (lastReceivedLivMessages) {
                Iterator<LivMessage> it = lastReceivedLivMessages.values().iterator();
                while(it.hasNext()){
                    LivMessage msg = it.next();
                    if(msg.getReceiveDate().before(lastValidDate)){
                        // klient umarł - przenosimy do niezalogowanych i rozłączamy

                        Log.d(Constants.DEBUG_TAG, "Klient umarł ("+msg.getUserInfo().getUid()+"), usuwam");
                        
                        tcpipNetworkManager.disconnectClient(msg.getUserInfo());
                        UserInfo user = getUserInfo(msg.getUserInfo());
                        removeUserInfo(msg.getUserInfo());
                        it.remove();
                        addNotLoggedUserInfo(user);
                    }
                }
            }
        }
    }
    
    
    class SendMessageDelayedThread extends Thread{
        private MsgMessage message;
        private int delay;
        
        public SendMessageDelayedThread(MsgMessage message, int delay) {
            this.message = message;
            this.delay = delay;
        }
        
        @Override
        public void run() {
            try {
                sleep(delay);
            } catch (InterruptedException e) {
                Log.e(Constants.ERROR_TAG, "InterruptedException at SendMessageDelayedThread");
                return;
            }
            MultitalkNetworkManager.this.tcpipNetworkManager.sendMessageToAll(message);
        }
    }
    
    
    /**
     * Wątek przetwarzający komunikaty od klientów
     */
    class MessageDispatcher extends Thread{
        private BlockingQueue<Message> messageQueue;
        
        public MessageDispatcher(BlockingQueue<Message> messageQueue) {
            this.messageQueue = messageQueue;
        }
        
        
        @Override
        public void run() {
            Log.d(Constants.DEBUG_TAG, "started MessageDispatcher");
            Message message;

            try {
                
                while(true){
                    message = this.messageQueue.take();
                    
                    if(message instanceof LivMessage){
                        //Log.d(Constants.DEBUG_TAG, "received LIV message:\n"+message.serialize());
                        MultitalkNetworkManager.this.handleLivMessage((LivMessage) message);
                        continue;
                    
                    } else if(message instanceof MsgMessage){
                        Log.d(Constants.DEBUG_TAG, "received MSG message:\n"+message.serialize());
                        MultitalkNetworkManager.this.handleMsgMessage((MsgMessage) message);
                        continue;
                    
                    } else if(message instanceof GetMessage){
                        Log.d(Constants.DEBUG_TAG, "received GET message:\n"+message.serialize());
                        MultitalkNetworkManager.this.handleGetMessage((GetMessage) message);
                        continue;
                    
                    } else if(message instanceof SendMessageToClient){
                        Log.d(Constants.DEBUG_TAG, "received SendMessageToClient message");
                        MultitalkNetworkManager.this.handleSendMessageToClientMessage((SendMessageToClient) message);
                        continue;
                    
                    } else if(message instanceof DiscoveryPacketReceivedMessage){
                        Log.d(Constants.DEBUG_TAG, "received discovery packet");
                        MultitalkNetworkManager.this.handleDiscoveryPacketReceived(
                                (DiscoveryPacketReceivedMessage) message);
                        continue;
                    
                    } else if(message instanceof HiMessage){
                        Log.d(Constants.DEBUG_TAG, "received HII message\n"+message.serialize());
                        MultitalkNetworkManager.this.handleHiMessage((HiMessage) message);                        
                        continue;
                        
                    } else if(message instanceof LogMessage){
                        Log.d(Constants.DEBUG_TAG, "received LOG message\n"+message.serialize());
                        MultitalkNetworkManager.this.handleLogMessage((LogMessage) message);
                        continue;
                        
                    } else if(message instanceof MtxMessage){
                        Log.d(Constants.DEBUG_TAG, "received MTX message\n"+message.serialize());
                        MultitalkNetworkManager.this.handleMtxMessage((MtxMessage) message);
                        continue;
                        
                    } else if(message instanceof OutMessage){
                        Log.d(Constants.DEBUG_TAG, "received OUT message\n"+message.serialize());
                        MultitalkNetworkManager.this.handleOutMessage((OutMessage) message);
                        continue;
                        
                    } else if(message instanceof P2PMessage){
                        Log.d(Constants.DEBUG_TAG, "received P2P packet");
                        MultitalkNetworkManager.this.handleP2PMessage((P2PMessage) message);
                        continue;
                    
                    } else if(message instanceof SendMessageToAllMessage){
                        Log.d(Constants.DEBUG_TAG, "received SendMessageToAllMessage message");
                        MultitalkNetworkManager.this.tcpipNetworkManager
                            .sendMessageToAll(((SendMessageToAllMessage) message).getMessageToSend());
                        continue;
                    
                    } else if(message instanceof SendMessageDelayed){
                        Log.d(Constants.DEBUG_TAG, "received SendMessageDelayed message");
                        MultitalkNetworkManager.this.handleSendDelayedMessage((SendMessageDelayed) message);
                        continue;
                    
                    } else if(message instanceof FinishMessage){
                        // koniec
                        return;
                    }
                    
                    
                }
                
            } catch (InterruptedException e) {
                Log.e(Constants.ERROR_TAG, "InterruptedException at MessageDispatcher#run");
                return;
            }
            
        }
        
    }
}
