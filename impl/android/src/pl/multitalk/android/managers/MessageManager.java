package pl.multitalk.android.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.util.Log;

import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.managers.messages.MsgMessage;
import pl.multitalk.android.model.RBMtxPair;
import pl.multitalk.android.model.ReliableBroadcastMatrix;
import pl.multitalk.android.util.Constants;

/**
 * Manager wiadomości
 * @author Michał Kołodziejski
 */
public class MessageManager {

    /**
     * Zalogowany użytkownik
     */
    private UserInfo userInfo;
    
    /**
     * Macierz algorytmu Reliable Broadcast
     */
    private ReliableBroadcastMatrix mtx;
    
    /**
     * Lista wszystkich zaakceptowanych wiadomości w porządku chronologicznym
     */
    private LinkedList<MsgMessage> acceptedMessages;
    
    /**
     * Lista wszystkich niezaakceptowanych wiadomości
     */
    private LinkedList<MsgMessage> waitingMessages;
    
    /**
     * Mapa list wiadomości wysyłanych przez użytkowników, które są gotowe do wyświetlenia
     */
    private Map<UserInfo, LinkedList<MsgMessage>> acceptedUserMessages;

    /**
     * Mapa list wiadomości wysyłanych przez użytkowników, które oczekują na brakujące wiadomości
     */
    private Map<UserInfo, LinkedList<MsgMessage>> waitingUserMessages;
    
    
    
    /**
     * Tworzy managera wiadomości
     */
    public MessageManager(UserInfo userInfo) {
        this.userInfo = userInfo;
        mtx = new ReliableBroadcastMatrix(this.userInfo);
        
        acceptedMessages = new LinkedList<MsgMessage>();
        waitingMessages = new LinkedList<MsgMessage>();
        acceptedUserMessages = new HashMap<UserInfo, LinkedList<MsgMessage>>();
        waitingUserMessages = new HashMap<UserInfo, LinkedList<MsgMessage>>();

        acceptedUserMessages.put(this.userInfo, new LinkedList<MsgMessage>());
        waitingUserMessages.put(this.userInfo, new LinkedList<MsgMessage>());
    }
    
    
    /**
     * Dodaje użytkownika (nadawcę)
     * @param user użytkownik
     */
    public synchronized void addUser(UserInfo user){
        if(mtx.containsUser(user)){
            return;
        }
        
        mtx.addUser(user);
        acceptedUserMessages.put(user, new LinkedList<MsgMessage>());
        waitingUserMessages.put(user, new LinkedList<MsgMessage>());
    }
    
    
    /**
     * Usuwa użytkownika (nadawcę)
     * @param user użytkownik
     */
    public synchronized void removeUser(UserInfo user){
        if(!mtx.containsUser(user)){
            return;
        }
        
        mtx.removeUserFromMatrix(user);
        acceptedUserMessages.remove(user);
        waitingUserMessages.remove(user);
    }
    
    
    /**
     * Zwraca macierz wiedzy użytkowników
     * @return macierz wiedzy użytkowników
     */
    public synchronized RBMtxPair getRBMatrix(){
        return mtx.getMatrix();
    }
    
    
    /**
     * Obsługuje macierz wiedzy użytkownika
     * @param userMtx macierz wiedzy użytkownika
     */
    public synchronized void handleUserRBMatrix(RBMtxPair userMtx){
        mtx.handleUserMatrix(userMtx);
    }
    
    
    /**
     * Dodaje wiadomość i zwraca informację, czy wiadomość została dodana do listy aktualnych
     * @param message wiadomość
     * @return informację, czy wiadomość została dodana do listy aktualnych
     */
    public synchronized boolean addMessage(MsgMessage message){
        if(!mtx.containsUser(message.getMsgSender())){
            // nie mamy takiego użytkownika - ignorujemy
            return true;
        }
        
        if(acceptedMessages.contains(message) || waitingMessages.contains(message)){
            // mamy już tę wiadomość
            return true;
        }
        
        
        boolean acceptMsg = mtx.isMyVectorUpToDate(message.getTimeVec(), message.getUsersOrder());

        mtx.updateUserVector(message.getMsgSender(), message.getTimeVec(), message.getUsersOrder());
        
        if(acceptMsg){
            // kolejna wiadomość
            acceptMessage(message);
            
            // ewentualne dodanie oczekujących
            checkAndMoveWaitingMessages();
            
        } else {
            Log.d(Constants.DEBUG_TAG, "[MessageManager] Adding message to waiting"
                    +" (to: \""+message.getMsgReceiver().getUid()+"\", msgId: "+message.getMsgId());
            waitingMessages.addLast(message);
            waitingUserMessages.get(message.getMsgSender()).add(message);
            
        }
        
        return acceptMsg;
    }
    
    
    /**
     * Zwraca wiadomość
     * @param sender nadawca wiadomości
     * @param msgId identyfikator wiadomości
     * @return wiadomość, lub null jeżeli nie ma takiej wiadomości
     */
    public synchronized MsgMessage getMessage(UserInfo sender, int msgId){
        List<MsgMessage> acceptedMessages = acceptedUserMessages.get(sender);
        
        if(acceptedMessages != null){
            // zaakceptowane
            for(MsgMessage msg : acceptedMessages){
                if(msg.getMsgId() == msgId){
                    return msg;
                }
            }
        }
        

        List<MsgMessage> waitingMessages = waitingUserMessages.get(sender);
        if(waitingMessages != null){
            // oczekujące
            for(MsgMessage msg : waitingMessages){
                if(msg.getMsgId() == msgId){
                    return msg;
                }
            }
        }
        // nie ma
        return null; 
    }
    
    
    /**
     * Zwraca uporządkowaną chronologicznie listę wiadomości wymienianą
     * między zalogowanym użytkownikiem a klientem
     * @param client zdalny użytkownik
     * @return uporządkowaną chronologicznie listę wiadomości wymienianą
     */
    public synchronized List<MsgMessage> getConversation(UserInfo client){
        List<MsgMessage> conversation = new ArrayList<MsgMessage>();
        for(MsgMessage msg : acceptedMessages){
            if(msg.getMsgSender().equals(userInfo) && msg.getMsgReceiver().equals(client)
                    || msg.getMsgSender().equals(client) && msg.getMsgReceiver().equals(userInfo)
                    || "".equals(client.getUid()) && "".equals(msg.getMsgReceiver().getUid())){
                conversation.add(msg);
            }
        }
        return conversation;
    }
    
    
    /**
     * Sprawdza czy jest możliwe przeniesienie wiadomości z oczekujących do zaakceptowanych
     */
    private void checkAndMoveWaitingMessages(){
        MsgMessage msgToMove = null;
        boolean moveNext = true;
        while(moveNext){
            msgToMove = null;
            moveNext = false;
            
            // sprawdzamy każdą wiadomość
            for(MsgMessage msg : waitingMessages){
                if(mtx.isMyVectorUpToDate(msg.getTimeVec(), msg.getUsersOrder())){
                    msgToMove = msg;
                    break;
                }
            }
            
            if(msgToMove != null){
                // przenosimy
                waitingMessages.remove(msgToMove);
                waitingUserMessages.get(msgToMove.getMsgSender()).remove(msgToMove);
                
                acceptMessage(msgToMove);
                
                moveNext = true;
            }
            
        }
    }
    
    
    private void acceptMessage(MsgMessage message){
        Log.d(Constants.DEBUG_TAG, "[MessageManager] Adding message to accepted"
                    +" (to: \""+message.getMsgReceiver().getUid()+"\", msgId: "+message.getMsgId());
        acceptedMessages.addLast(message);
        acceptedUserMessages.get(message.getMsgSender()).add(message);
        mtx.incrementUserValue(message.getMsgSender());
    }
}
