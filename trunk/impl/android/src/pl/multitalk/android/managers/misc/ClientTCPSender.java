package pl.multitalk.android.managers.misc;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.util.Log;

import pl.multitalk.android.managers.messages.Message;
import pl.multitalk.android.managers.messages.internal.FinishMessage;
import pl.multitalk.android.util.Constants;

/**
 * Nadawca komunikatów do klienta
 * @author Michał Kołodziejski
 */
public class ClientTCPSender extends Thread {

    private Socket socket;
    private BlockingQueue<Message> messagesQueue;
    private boolean finish;
    private PrintWriter socketOutput;
    
    
    /**
     * Tworzy nadawcę komunikatów
     * @param clientSocket socket do klienta
     */
    public ClientTCPSender(Socket clientSocket) {
        this.socket = clientSocket;
        messagesQueue = new ArrayBlockingQueue<Message>(20);
    }
    
    
    @Override
    public void run() {
        try {
            socketOutput = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            Log.e(Constants.ERROR_TAG, "IOException at ClientTCPSender - cannot get " +
            		"socket OutputStream" +
            		"\n Cause: " + e.getMessage());
            return;
        }
        
        finish = false;
        Message message;
                
        try{
            while(!finish){
                message = messagesQueue.take();
                
                // czy koniec?
                if(message instanceof FinishMessage){
                    finish = true;
                    continue;
                }

                String messageContent = getMessageWithHeader(message.serialize());
                
                Log.d(Constants.DEBUG_TAG, "sending message: " + messageContent
                        +"\n to: " + socket.getInetAddress().getHostAddress());
                
                socketOutput.append(messageContent);
                socketOutput.flush();
            }
            
        } catch (InterruptedException e){
            Log.e(Constants.ERROR_TAG, "InterruptedException at ClientTCPSender");
            return;
        }
    }
    
    
    /**
     * Dodaje nowy komunikat do wysłania
     * @param message komunikat
     */
    public void putMessage(Message message){
        try {
            messagesQueue.put(message);
        } catch (InterruptedException e) {
            Log.e(Constants.ERROR_TAG, "InterruptedException at ClientTCPSender");
            return;
        }
    }
    
    
    /**
     * Dodaje nagłówek komunikatu i zwraca komunikat gotowy do wysłania
     * @param messageContent ciało komunikatu
     * @return komunikat gotowy do wysłania
     */
    private String getMessageWithHeader(String messageContent){
        StringBuffer sb = new StringBuffer();
        sb.append(Constants.BEGIN_MESSAGE_HEADER);
        sb.append(messageContent.length());
        sb.append("\n");
        sb.append(messageContent);
        return sb.toString();
    }
}
