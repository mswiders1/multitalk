package pl.multitalk.android.managers.misc;

import java.io.IOException;
import java.net.Socket;

import android.util.Log;

import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.managers.TCPIPNetworkManager;
import pl.multitalk.android.managers.messages.Message;
import pl.multitalk.android.managers.messages.internal.FinishMessage;
import pl.multitalk.android.util.Constants;

/**
 * Reprezentacja połączenia z klientem.
 * @author Michał Kołodziejski
 */
public class ClientConnection {

    private UserInfo clientInfo;
    private TCPIPNetworkManager networkManager;
    
    private Socket clientSocket;
    private ClientTCPSender tcpSender;
    private ClientTCPReceiver tcpReceiver;
    
    
    /**
     * Tworzy połączenie z klientem
     * @param clientInfo informacje o kliencie
     * @param multitalkNetworkManager manager połączeń
     */
    public ClientConnection(UserInfo clientInfo, Socket clientSocket,
            TCPIPNetworkManager networkManager) {
        
        this.clientInfo = clientInfo;
        this.clientSocket = clientSocket;
        this.networkManager = networkManager;
        
        init();
    }
    
    
    /**
     * Inicjalizuje połączenie
     */
    private void init(){
        tcpSender = new ClientTCPSender(clientSocket);
        tcpSender.start();
        
        tcpReceiver = new ClientTCPReceiver(clientSocket, clientInfo, networkManager);
        tcpReceiver.start();
    }
    
    
    /**
     * Wysyła wiadomość do klienta
     * @param message wiadomość
     */
    public void sendMessage(Message message){
       tcpSender.putMessage(message); 
    }
    
    
    /**
     * Rozłącza się z klientem
     */
    public void disconnect(){
        tcpSender.putMessage(new FinishMessage());
        
        try {
            clientSocket.close(); // zakończy receiver-a
        } catch (IOException e) {
            Log.e(Constants.ERROR_TAG, "IOException at ClientConnection#disconnect()"
                    +"\nCause msg: "+e.getMessage());
        }
    }

    
    /**
     * Aktualizuje informacje o uzytkowniku
     * @param newUserInfo nowe informacje
     */
    public void updateUserInfo(UserInfo newUserInfo){
        clientInfo = newUserInfo;
        tcpReceiver.updateUserInfo(newUserInfo);
    }
}
