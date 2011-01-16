package pl.multitalk.android.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.content.Context;
import android.util.Log;
import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.managers.messages.HiMessage;
import pl.multitalk.android.managers.messages.LogMessage;
import pl.multitalk.android.managers.messages.Message;
import pl.multitalk.android.managers.messages.internal.DiscoveryPacketReceivedMessage;
import pl.multitalk.android.managers.messages.internal.FinishMessage;
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
    
    private Context context;
    private BroadcastNetworkManager broadcastNetworkManager;
    private TCPIPNetworkManager tcpipNetworkManager;
    private Timer sendLogTimer;
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
     */
    public void logIn(String login){
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
        
        // wysłanie UDP discovery
        broadcastNetworkManager.sendUDPHostsDiscoveryPacket();
        
        // timer
        sendLogTimer = new Timer();
        sendLogTimer.schedule(new SendLogMessageTimerTask(), LOG_SEND_DELAY);
    }
    
    
    /**
     * Wylogowuje z sieci Multitalk
     */
    public void logout(){
        tcpipNetworkManager.disconnectAllClients();
        tcpipNetworkManager.stopListeningForConnections();
        broadcastNetworkManager.stopBroadcastListening();
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
     * Dodaje informację o nowym niezalogowanym użytkowniku
     * @param newUserInfo informacje o nowym niezalogowanym użytkowniku
     */
    private synchronized void addNotLoggedUserInfo(UserInfo newUserInfo){
        if(!notLoggedUsers.contains(newUserInfo)){
            notLoggedUsers.add(newUserInfo);
        }
    }/**
     * Usuwa informację o niezalogowanym użytkowniku
     * @param userInfoToRemove informacje o niezalogowanym użytkowniku do usunięcia
     */
    private synchronized void removeNotLoggedUserInfo(UserInfo userInfoToRemove){
        if(notLoggedUsers.contains(userInfoToRemove)){
            notLoggedUsers.remove(userInfoToRemove);
        }
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
    
    
    /* *********************************************** */
    /*   handlery dla komunikatów
    /* *********************************************** */
    
    public void handleHiMessage(HiMessage message){
        MultitalkNetworkManager.this.addUserInfo(message.getSenderInfo());
        tcpipNetworkManager.connectToClient(message.getSenderInfo());
        
        for(UserInfo user : message.getLoggedUsers()){
            if(userInfo.equals(user) || userInfo.getIpAddress().equals(user.getIpAddress())){
                // pomijamy siebie
                continue;
            }
            
            MultitalkNetworkManager.this.addUserInfo(user);
            tcpipNetworkManager.connectToClient(user);
        }
    }
    
    
    public void handleDiscoveryPacketReceived(DiscoveryPacketReceivedMessage message){
        // dodajemy usera
        MultitalkNetworkManager.this.addNotLoggedUserInfo(message.getSenderInfo());
        tcpipNetworkManager.connectToClient(message.getSenderInfo());
        
        // w odpowiedzi wysyłamy HiMessage
        HiMessage hiMessage = new HiMessage();
        hiMessage.setSenderInfo(userInfo);
        hiMessage.setRecipientInfo(message.getSenderInfo());
        // kopia
        List<UserInfo> loggedUsers = new ArrayList<UserInfo>();
        for(UserInfo user : users){
            loggedUsers.add(new UserInfo(user));
        }
        hiMessage.setLoggedUsers(loggedUsers);
        
        tcpipNetworkManager.sendMessage(hiMessage);
    }
    
    
    public void handleLogMessage(LogMessage message){
        UserInfo user = message.getSenderInfo();
        if(userInfo.equals(user) || userInfo.getIpAddress().equals(user.getIpAddress())){
            // wiadomość o zalogowaniu siebie
            // ignorujemy i usuwamy ewentualny wpis 
            removeNotLoggedUserInfo(user);
            tcpipNetworkManager.disconnectClient(user);
            return;
        }
        removeNotLoggedUserInfo(user);
        addUserInfo(user);
        
        // TODO wysłać MTX message
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
            tcpipNetworkManager.sendMessageToAll(logMessage);
            
            // ustawienie znacznika
            MultitalkNetworkManager.this.setLoggedIn(true);
            
            // rozpoczęcie nasłuchiwania po broadcast-cie
            broadcastNetworkManager.startBroadcastListening();
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
                    
                    if(message instanceof DiscoveryPacketReceivedMessage){
                        Log.d(Constants.DEBUG_TAG, "received discovery packet");
                        MultitalkNetworkManager.this.handleDiscoveryPacketReceived(
                                (DiscoveryPacketReceivedMessage) message);
                        continue;
                    
                    }else if(message instanceof HiMessage){
                        Log.d(Constants.DEBUG_TAG, "received HII message");
                        MultitalkNetworkManager.this.handleHiMessage((HiMessage) message);                        
                        continue;
                        
                    } else if(message instanceof LogMessage){
                        Log.d(Constants.DEBUG_TAG, "received LOG message");
                        MultitalkNetworkManager.this.handleLogMessage((LogMessage) message);
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
