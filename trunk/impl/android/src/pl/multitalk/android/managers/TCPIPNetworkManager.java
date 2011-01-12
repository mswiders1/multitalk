package pl.multitalk.android.managers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import pl.multitalk.android.util.NetworkUtil;
import pl.multitalk.android.util.NetworkUtil.WifiNotEnabledException;

import android.content.Context;
import android.util.Log;

/**
 * Manager połączeń TCP/IP
 * @author Michał Kołodziejski
 */
public class TCPIPNetworkManager {

    private static final int TCPIP_PORT = 3554;
    
    private Context context;
    private ClientConnectionsListener connectionsListener;
    
    /**
     * Tworzy managera połączeń TCP/IP
     * @param context kontekst wykonania
     */
    public TCPIPNetworkManager(Context context){
        this.context = context;
        this.connectionsListener = null;
    }
    
    
    /**
     * Rozpoczyna nasłuchiwanie połączeń od innych uzytkowników
     */
    public void startListeningForConnections(){
        try {
            connectionsListener = new ClientConnectionsListener(NetworkUtil.getIPaddressAsInetAddress(context),
                    TCPIP_PORT);
            Thread concectionsListenerTh = new Thread(connectionsListener);
            concectionsListenerTh.start();
            
        } catch (UnknownHostException e) {
            Log.e("Multitalk-ERROR", "UnknownHostException occured at TCPIPNetworkManager#startListeningForConnections()"
                    +"\nCause msg: "+e.getMessage());
            // FIXME poinformowanie "kogoś" wyżej...
            return;
            
        } catch (WifiNotEnabledException e) {
            Log.e("Multitalk-ERROR", "WifiNotEnabledException occured at TCPIPNetworkManager#startListeningForConnections()"
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
     * Nasłuchiwacz połączeń od innych klientów
     */
    class ClientConnectionsListener implements Runnable {
        
        private InetAddress inetAddress;
        private int port;
        private ServerSocket serverSocket;
        private boolean stopListeningFlag;
        
        public ClientConnectionsListener(InetAddress inetAddress, int port){
            this.inetAddress = inetAddress;
            this.port = port;
        }
        
        @Override
        public void run() {
            try {
                stopListeningFlag = false;
                serverSocket = new ServerSocket(port, 1, inetAddress);
                Log.d("Multitalk-DEBUG", "starting to listen for connections from clients"
                        +" at IP: "+inetAddress.getHostAddress()
                        +" and port: "+port);
                
                while(true){
                    Socket clientSocket = serverSocket.accept();
                    Log.d("Multitalk-DEBUG", "Accepted connection from client with "
                            +"IP: "+clientSocket.getInetAddress().getHostAddress());
                    // TODO nowy wątek do obsługi klienta
                }
                
            } catch (IOException e) {
                if(stopListeningFlag == true){
                    Log.d("Multitalk-DEBUG", "client connections listener stopped (by exception)");
                    return;
                }
                
                Log.e("Multitalk-ERROR", "IOException occured at ClientConnectionsListener"
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
                    Log.e("Multitalk-ERROR", "IOException occured at ClientConnectionsListener#stopListening()"
                            +"\nCause msg: "+e.getMessage());
                }
            }
        }
    }
}
