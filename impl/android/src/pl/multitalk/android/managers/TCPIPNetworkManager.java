package pl.multitalk.android.managers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.managers.messages.Message;
import pl.multitalk.android.managers.misc.ClientConnection;
import pl.multitalk.android.util.Constants;
import pl.multitalk.android.util.NetworkUtil;
import pl.multitalk.android.util.NetworkUtil.WifiNotEnabledException;

import android.content.Context;
import android.util.Log;

/**
 * Manager połączeń TCP/IP
 * @author Michał Kołodziejski
 */
public class TCPIPNetworkManager {
    
    private Context context;
    private MultitalkNetworkManager multitalkNetworkManager;
    private ClientConnectionsListener connectionsListener;
    
    /**
     * Mapa połączeń z klientami
     */
    private Map<UserInfo, ClientConnection> clientConnections;
    
    
    /**
     * Tworzy managera połączeń TCP/IP
     * @param context kontekst wykonania
     */
    public TCPIPNetworkManager(Context context, MultitalkNetworkManager multitalkNetworkManager){
        this.context = context;
        this.multitalkNetworkManager = multitalkNetworkManager;
        this.connectionsListener = null;
        
        clientConnections = new HashMap<UserInfo, ClientConnection>();
    }
    
    
    /**
     * Zwraca MultitalkNetworkManager
     * @return MultitalkNetworkManager
     */
    public MultitalkNetworkManager getMultitalkNetworkManager(){
        return this.multitalkNetworkManager;
    }
    
    
    /**
     * Rozpoczyna nasłuchiwanie połączeń od innych uzytkowników
     */
    public void startListeningForConnections(){
        try {
            connectionsListener = new ClientConnectionsListener(NetworkUtil.getIPaddressAsInetAddress(context),
                    Constants.TCP_PORT, this);
            Thread concectionsListenerTh = new Thread(connectionsListener);
            concectionsListenerTh.start();
            
        } catch (UnknownHostException e) {
            Log.e(Constants.ERROR_TAG, "UnknownHostException occured at TCPIPNetworkManager#startListeningForConnections()"
                    +"\nCause msg: "+e.getMessage());
            // FIXME poinformowanie "kogoś" wyżej...
            return;
            
        } catch (WifiNotEnabledException e) {
            Log.e(Constants.ERROR_TAG, "WifiNotEnabledException occured at TCPIPNetworkManager#startListeningForConnections()"
                    +"\nCause msg: "+e.getMessage());
            // FIXME poinformowanie "kogoś" wyżej...
            return;
        }
    }
    
    
    /**
     * Kończy nasłuchiwanie połączeń
     */
    public void stopListeningForConnections(){
        if(connectionsListener != null){
            connectionsListener.stopListening();
        }
    }
    
    
    /**
     * Rozłącza się ze wszystkimi klientami
     */
    public void disconnectAllClients(){
        for(ClientConnection clientConnection : clientConnections.values()){
            clientConnection.disconnect();
        }
        
        clientConnections.clear();
    }
    
    
    /**
     * Tworzy połączenie z nowym klientem
     * @param socket socket
     */
    public void newClientConnected(Socket socket){
        UserInfo userInfo = new UserInfo();
        userInfo.setIpAddress(socket.getInetAddress().getHostAddress());
        // fake UID - nie wiemy jak się nazywa, ale musimy go rozpoznawać...
        userInfo.setUid(String.valueOf(System.currentTimeMillis()+"."+Math.random()));
        userInfo.setUsername(String.valueOf(System.currentTimeMillis()+"."+Math.random()));
        
        ClientConnection clientConnection = new ClientConnection(userInfo, socket, this);
        clientConnections.put(userInfo, clientConnection);
    }
    
    
    /**
     * Tworzy połączenie z klientem
     * @param userInfo klient
     */
    public void connectToClient(UserInfo userInfo){
        if(clientConnections.containsKey(userInfo)){
            // już mamy to połączenie
            return;
        }
        
        InetAddress clientAddress = null;
        Log.d(Constants.DEBUG_TAG, "TCP/IP connecting to client at: " + userInfo.getIpAddress());
        
        try {
            clientAddress = NetworkUtil.getInetAddressFromString(userInfo.getIpAddress());
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(clientAddress, Constants.TCP_PORT),
                    NetworkUtil.CONNECTION_TIMEOUT);
            
            ClientConnection clientConnection = new ClientConnection(userInfo, socket, this);
            clientConnections.put(userInfo, clientConnection);
            
        } catch (UnknownHostException e) {
            // TODO wysłać do multitalknetworkmanagera info o niepowodzeniu
            Log.e(Constants.ERROR_TAG, "UnknownHostException occured at connectToClient"
                    +"\nCause msg: "+e.getMessage());
        } catch (IOException e) {
            // TODO wysłać do multitalknetworkmanagera info o niepowodzeniu
            Log.e(Constants.ERROR_TAG, "IOException occured at connectToClient"
                    +"\nCause msg: "+e.getMessage());
        }
    }
    
    
    /**
     * Rozłącza się z użytkownikiem
     * @param userInfo użytkownik
     */
    public void disconnectClient(UserInfo userInfo){
        if(!clientConnections.containsKey(userInfo)){
            // nie ma takiego połączenia
            return;
        }

        Log.d(Constants.DEBUG_TAG, "Rozlaczam z klientem: "+userInfo.getIpAddress()+" ("+userInfo.getUid()+")");
        ClientConnection clientConnection = clientConnections.get(userInfo);
        clientConnection.disconnect();
        clientConnections.remove(userInfo);
    }
    
    
    /**
     * Wysyła wiadomość do wszystkich klientów
     * @param message wiadomość do wysłania
     */
    public void sendMessageToAll(Message message){
//        Log.d(Constants.DEBUG_TAG, "sending message to all: \n"
//                + message.serialize());
        
        for(Entry<UserInfo, ClientConnection> entry : clientConnections.entrySet()){
            Message cloneMessage = message.getClone();
            cloneMessage.setRecipientInfo(entry.getKey());
            
            Log.d(Constants.DEBUG_TAG, "sending message: \n"
                    + message.serialize()
                    +"\n to client at: "+cloneMessage.getRecipientInfo().getIpAddress()+" ("+cloneMessage.getRecipientInfo().getUid()+")");
            
            entry.getValue().sendMessage(cloneMessage);
        }
    }
    
    
    /**
     * Wysyła wiadomość do klienta
     * @param message wiadomość
     */
    public void sendMessage(Message message){
        Log.d(Constants.DEBUG_TAG, "sending message: \n"
                + message.serialize()
                +"\n to client at: "+message.getRecipientInfo().getIpAddress()+" ("+message.getRecipientInfo().getUid()+")");
        
        ClientConnection clientConnection = clientConnections.get(message.getRecipientInfo());
        if(clientConnection != null){
            clientConnection.sendMessage(message);
        }
    }
    
    
    /**
     * Aktualizuje informacje o uzytkowniku
     * @param oldUserInfo stare informacje
     * @param newUserInfo nowe informacje
     */
    public void updateUserInfo(UserInfo oldUserInfo, UserInfo newUserInfo){
        Log.d(Constants.DEBUG_TAG, "TCPIPNetworkManager: update user info ("+oldUserInfo.getIpAddress()+") from: "+oldUserInfo.getUid()
                +", to: "+newUserInfo.getUid());
        ClientConnection clientConnection = clientConnections.get(oldUserInfo);
        clientConnections.remove(oldUserInfo);
        clientConnections.put(newUserInfo, clientConnection);
        clientConnection.updateUserInfo(newUserInfo);
    }
    
    
    /**
     * Nasłuchiwacz połączeń od innych klientów
     */
    class ClientConnectionsListener implements Runnable {
        
        private TCPIPNetworkManager networkManager;
        private InetAddress inetAddress;
        private int port;
        private ServerSocket serverSocket;
        private boolean stopListeningFlag;
        
        
        public ClientConnectionsListener(InetAddress inetAddress, int port, 
                TCPIPNetworkManager networkManager){
            
            this.inetAddress = inetAddress;
            this.port = port;
            this.networkManager = networkManager;
        }
        
        
        @Override
        public void run() {
            try {
                stopListeningFlag = false;
                serverSocket = new ServerSocket(port, 1, inetAddress);
                Log.d(Constants.DEBUG_TAG, "starting to listen for connections from clients"
                        +" at IP: "+inetAddress.getHostAddress()
                        +" and port: "+port);
                
                while(true){
                    Socket clientSocket = serverSocket.accept();
                    Log.d(Constants.DEBUG_TAG, "Accepted connection from client with "
                            +"IP: "+clientSocket.getInetAddress().getHostAddress());
                    
                    // poinformowanie o nowym kliencie
                    networkManager.newClientConnected(clientSocket);
                }
                
            } catch (IOException e) {
                if(stopListeningFlag == true){
                    Log.d(Constants.DEBUG_TAG, "client connections listener stopped (by exception)");
                    return;
                }
                
                Log.e(Constants.ERROR_TAG, "IOException occured at ClientConnectionsListener"
                        +"\nCause msg: "+e.getMessage());
                // FIXME poinformowanie "kogoś" wyżej...
                return;
            }
        }
        
        
        /**
         * Zatrzymuje nasłuchiwacz
         */
        public void stopListening(){
            stopListeningFlag = true;
            if(serverSocket != null && serverSocket.isBound()){
                try {
                    serverSocket.close();
                    
                } catch (IOException e) {
                    Log.e(Constants.ERROR_TAG, "IOException occured at ClientConnectionsListener#stopListening()"
                            +"\nCause msg: "+e.getMessage());
                    return;
                }
            }
        }
    }
}
