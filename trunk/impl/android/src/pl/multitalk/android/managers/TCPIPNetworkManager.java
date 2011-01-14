package pl.multitalk.android.managers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import pl.multitalk.android.datatypes.UserInfo;
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
        
        ClientConnection clientConnection = new ClientConnection(userInfo, socket, this);
        clientConnections.put(userInfo, clientConnection);
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
