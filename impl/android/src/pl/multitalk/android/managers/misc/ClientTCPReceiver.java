package pl.multitalk.android.managers.misc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.json.JSONException;

import android.util.Log;

import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.managers.TCPIPNetworkManager;
import pl.multitalk.android.managers.messages.Message;
import pl.multitalk.android.managers.messages.MessageFactory;
import pl.multitalk.android.util.Constants;

/**
 * Odbiorca komunikatów od klienta
 * @author Michał Kołodziejski
 */
public class ClientTCPReceiver extends Thread {

    private static final String BEGIN_MESSAGE_FLAG = "BEGIN_MESSAGE";
    
    
    private UserInfo clientInfo;
    private Socket socket;
    private TCPIPNetworkManager networkManager;
    private InputStreamReader socketReader;
    
    
    /**
     * Tworzy odbiorcę komunikatów od klienta
     * @param clientSocket
     * @param networkManager
     */
    public ClientTCPReceiver(Socket clientSocket, UserInfo clientInfo, TCPIPNetworkManager networkManager) {
        this.socket = clientSocket;
        this.clientInfo = clientInfo;
        this.networkManager = networkManager;
    }

    
    /**
     * Aktualizuje informacje o uzytkowniku
     * @param newUserInfo nowe informacje
     */
    public void updateUserInfo(UserInfo newUserInfo){
        clientInfo = newUserInfo;
    }
    
    
    @Override
    public void run() {
        try {
            socketReader = new InputStreamReader(socket.getInputStream());
        } catch (IOException e) {
            Log.e(Constants.ERROR_TAG, "IOException at ClientTCPReceiver - cannot get " +
                    "socket InputStream" +
                    "\n Cause: " + e.getMessage());
            return;
        }
        
        char[] buf = new char[1024];
        int readChars = 0;
        
        boolean atNewMessage = false;
        StringBuffer sb = new StringBuffer();
        String packet;
        int messageLength = -1;
        int messageReadBytes = 0;
        
        boolean continueRead = false;
        try {
            while(true){
            
                while(continueRead == true || ((readChars = socketReader.read(buf)) != -1)){
                    if(continueRead){
                        Log.d(Constants.DEBUG_TAG, "Kontynuuję odczytywanie poprzedniego pakietu");
                        continueRead = false;
                        packet = "";
                    }
                    else { // (readChars != -1)
                        packet = new String(buf, 0, readChars);
//                        Log.d(Constants.DEBUG_TAG, "Read packet: "+packet);
                        sb.append(packet);
                    }
                    
                    if(!atNewMessage){
                        // pierwszy pakiet nowego komunikatu
                        atNewMessage = true;
                        messageLength = -1;
                        messageReadBytes = 0;
                    }

                    
                    if(messageLength == -1){
                        // nie mamy jeszcze długości wiadomości
                        String msgBuf = sb.toString();

                        int beginMessageFlagIdx = msgBuf.indexOf(BEGIN_MESSAGE_FLAG);
                        if(beginMessageFlagIdx == -1){
                            // za mało odczytał... jazda dalej
                            Log.d(Constants.DEBUG_TAG, "przed 'BEGIN_MESSAGE' w nagłówku");
                            continue;
                            
                        }
                        sb.delete(0, beginMessageFlagIdx);
                        msgBuf = sb.toString();
                        
                        int newLineIdx = msgBuf.indexOf("\n");
                        if(newLineIdx == -1){
                            // za mało odczytał... jazda dalej
                            Log.d(Constants.DEBUG_TAG, "przed znakiem nowej linii w nagłówku");
                            continue;
                            
                        }
                        
                        String header = msgBuf.substring(0, newLineIdx);
//                        Log.d(Constants.DEBUG_TAG, "Nagłówek "+header);
                        
                        // 14 == header.indexOf(":")
                        messageLength = Integer.valueOf(header.substring(14)).intValue();
                        messageReadBytes = msgBuf.length() - newLineIdx;
                        
                    } else {
                        messageReadBytes += packet.length();
                        
                    }
                    
                    if(messageReadBytes < messageLength){
                        continue;
                    }
                    
                    // odczytaliśmy całą wiadomość - przetwarzamy
                    String bufContent = sb.toString();

                    // wycięcie nagłówka
                    int newLineIdx = bufContent.indexOf("\n");
                    bufContent = bufContent.substring(newLineIdx + 1);
                    
                    // wycięcie i utworzenie wiadomości
                    String messageString = bufContent.substring(0, messageLength);
                    Log.d(Constants.DEBUG_TAG, "Odczytano wiadomość:\n" + messageString);
                    
                    Message message = null;
                    try {
                        message = MessageFactory.fromJSON(messageString);
                        if(message != null){
                            // znana wiadomość - przekazujemy
                            passMessage(message);
                        }
                        
                    } catch (JSONException e) {
                        Log.d(Constants.ERROR_TAG, "Błąd parse-owania JSON-a: \n" 
                                + e.getMessage());
                        // trudno, jedziemy dalej...
                    }
                    
                    // wyczyszczenie sb
                    sb.delete(0, newLineIdx + messageLength + 1);
                    atNewMessage = false;
                    if(sb.toString().length() > 0){
                        // odczytaliśmy całą wiadomość, ale coś zostało i trzeba przetworzyć
                        // resztę
                        Log.d(Constants.DEBUG_TAG, "W buforze pozostało:\n" + sb.toString());
                        continueRead = true;
                    }
                }
                
                // brak danych od klienta - poczekaj...
                Thread.sleep(500);
            }
        } catch (IOException e) {
            Log.e(Constants.ERROR_TAG, "IOException at ClientTCPReceiver" +
            		"\n Cause: "+e.getMessage());
            return;
        } catch (InterruptedException e) {
            Log.e(Constants.ERROR_TAG, "InterruptedException at ClientTCPReceiver");
            return;
        }
    }

    
    
    /**
     * Podaje dalej odczytaną wiadomość
     * @param message odczytana wiadomość
     */
    private void passMessage(Message message){
        message.setSenderInfo(clientInfo);
        
        // podaj dalej
        networkManager.getMultitalkNetworkManager().putMessage(message);
    }
}
