package pl.multitalk.android.managers.misc;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.util.Log;

import pl.multitalk.android.managers.messages.Message;
import pl.multitalk.android.util.Constants;

/**
 * Nadawca komunikatów do klienta
 * @author Michał Kołodziejski
 */
public class ClientTCPSender extends Thread {

    private Socket socket;
    private BlockingQueue<Message> messagesQueue;
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
        
        Message message;
                
        try{
            while(true){
                message = messagesQueue.take();
                socketOutput.append(message.serialize());
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
}
