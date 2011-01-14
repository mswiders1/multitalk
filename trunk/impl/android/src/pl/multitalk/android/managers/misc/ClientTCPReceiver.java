package pl.multitalk.android.managers.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import android.util.Log;

import pl.multitalk.android.managers.MultitalkNetworkManager;
import pl.multitalk.android.util.Constants;

/**
 * Odbiorca komunikatów od klienta
 * @author Michał Kołodziejski
 */
public class ClientTCPReceiver implements Runnable {

    private Socket socket;
    private MultitalkNetworkManager networkManager;
    private InputStreamReader socketReader;
    
    
    /**
     * Tworzy odbiorcę komunikatów od klienta
     * @param clientSocket
     * @param networkManager
     */
    public ClientTCPReceiver(Socket clientSocket, MultitalkNetworkManager networkManager) {
        this.socket = clientSocket;
        this.networkManager = networkManager;
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
        int messageLength = -1;
        int messageReadBytes = 0;
        
        try {
            while(true){
            
                while((readChars = socketReader.read(buf)) != -1){
                    String packet = new String(buf, 0, readChars);
                    Log.d(Constants.DEBUG_TAG, "Read first packet: "+packet);
                    sb.append(packet);
                    
                    if(!atNewMessage){
                        // pierwszy pakiet nowego komunikatu
                        atNewMessage = true;
                        messageLength = -1;
                        messageReadBytes = 0;
                    }

                    
                    if(messageLength == -1){
                        // nie mamy jeszcze długości wiadomości
                        String msgBuf = sb.toString();

                        int newLineIdx = msgBuf.indexOf("\n");
                        if(newLineIdx == -1){
                            // za mało odczytał...
                            // dodaj do bufora i jazda dalej
                            sb.append(packet);
                            continue;
                            
                            
                        }
                        
                        String header = packet.substring(0, newLineIdx);
                        Log.d(Constants.DEBUG_TAG, "   - header: "+header);
                        
                        // 14 == header.indexOf(":")
                        messageLength = Integer.valueOf(header.substring(14)).intValue();
                        messageReadBytes = msgBuf.length() - newLineIdx;
                    }
                    
                    if(messageReadBytes < messageLength){
                        continue;
                    }
                    
                    // odczytaliśmy całą wiadomość - przetwarzamy
                    
                    // wycięcie nagłówka
                    // TODO
                    
                    // wycięcie i utworzenie wiadomości
                    // TODO
                    
                    // zainicjowanie sb
                    // trzeba sprawdzić czy nie zaczęła się już nowa wiadomość
                    // TODO
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

}
