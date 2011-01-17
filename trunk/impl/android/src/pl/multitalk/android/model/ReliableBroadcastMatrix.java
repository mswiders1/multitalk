package pl.multitalk.android.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pl.multitalk.android.datatypes.UserInfo;

/**
 * Macierz zegarów logicznych na potrzeby algorytmu reliable-broadcast
 * @author Michał Kołodziejski
 */
public class ReliableBroadcastMatrix {

    /**
     * Zalogowany user
     */
    private UserInfo user;
    
    private List<UserInfo> mtxUsersOrder;
    private List<List<Integer>> mtx;
    
    
    /**
     * Tworzy nową macierz dla użytkownika
     * @param user użytkownik
     */
    public ReliableBroadcastMatrix(UserInfo user) {
        this.user = user;
        
        mtxUsersOrder = new LinkedList<UserInfo>();
        mtx = new ArrayList<List<Integer>>();
        
        mtxUsersOrder.add(this.user);
        mtx.add(new ArrayList<Integer>());
        mtx.get(0).add(0);
    }
    
    
    /**
     * Zwraca wartość zegara logicznego użytkownika aplikacji
     * @return wartość zegara logicznego użytkownika aplikacji
     */
    public synchronized int getMyValue(){
        return mtx.get(0).get(0).intValue();
    }
    
    
    /**
     * Zwiększa wartość zegara logicznego użytkownika aplikacji
     * @return nowa wartość zegara logicznego
     */
    public synchronized int incrementMyValue(){
        int newValue = mtx.get(0).get(0).intValue() + 1;
        mtx.get(0).set(0, newValue);
        return newValue;
    }
    
    
    /**
     * Dodaje uzytkownika z zerowym wektorem wiedzy orz zerowym zegarem logicznym.
     * Do wykorzystania w przypadku nowozalogowanego użytkownika
     * @param newUser nowy użytkownik
     */
    public synchronized void addUserWithZeroVector(UserInfo newUser){
        int usersNumber = mtxUsersOrder.size();
        
        // dodanie wektora wiedzy nowego usera o długości takiej jak wszystkich innych
        List<Integer> newUserKnowledgeVector = new ArrayList<Integer>();
        for(int i=0; i<usersNumber; ++i){
            newUserKnowledgeVector.add(0);
        }
        mtx.add(newUserKnowledgeVector);
        
        // dodanie usera
        mtxUsersOrder.add(newUser);
        ++usersNumber;
        
        // dodanie info o zegarze usera
        for(List<Integer> userVec : mtx){
            userVec.add(0);
        }
    }
    
    
    /**
     * Zwraca macierz wiedzy o wiedzy uzytkowników
     * @return para: macierz + kolejność użytkowników w macierzy
     */
    public synchronized RBMtxPair getMatrix(){
        List<UserInfo> copyMtxUsersOrder = new LinkedList<UserInfo>();
        List<List<Integer>> copyMtx = new ArrayList<List<Integer>>();
        
        // kopia listy uzytkowników
        for(UserInfo user : mtxUsersOrder){
            copyMtxUsersOrder.add(user);
        }
        
        // kopia macierzy
        for(List<Integer> userKnowledge : mtx){
            List<Integer> copyUserKnowledge = new ArrayList<Integer>();
            for(Integer i : userKnowledge){
                copyUserKnowledge.add(new Integer(i));
            }
            copyMtx.add(copyUserKnowledge);
        }
        
        return new RBMtxPair(copyMtxUsersOrder, copyMtx);
    }
}
