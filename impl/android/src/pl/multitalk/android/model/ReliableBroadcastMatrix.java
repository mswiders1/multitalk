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
     * Dodaje uzytkownika z zerowym zerowym zegarem logicznym.
     * Do wykorzystania w przypadku nowozalogowanego użytkownika
     * @param newUser nowy użytkownik
     */
    public synchronized void addUser(UserInfo newUser){
        if(mtxUsersOrder.contains(newUser)){
            // już jest taki user
            return;
        }

        // dodanie info o zegarze usera
        for(List<Integer> userVec : mtx){
            userVec.add(0);
        }
        
        // dodanie usera
        mtxUsersOrder.add(newUser);
        int usersNumber = mtxUsersOrder.size();
        
        // dodanie wektora wiedzy nowego usera o długości takiej jak wszystkich innych
        List<Integer> myVec = mtx.get(0);
        List<Integer> newUserKnowledgeVector = new ArrayList<Integer>();
        for(int i=0; i<usersNumber; ++i){
            newUserKnowledgeVector.add(myVec.get(i));
        }
        mtx.add(newUserKnowledgeVector);
        
        
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
    
    
    /**
     * Uaktualnia macierz na podstawie macierzy uzytkownika.
     * Wykorzystywane po odczytaniu MTX message.
     * @param userRBMtxPair para: macierz + kolejność użytkowników w macierzy
     */
    public synchronized void handleUserMatrix(RBMtxPair userRBMtxPair){
        List<UserInfo> userMtxOrder = userRBMtxPair.getMtxUsersOrder();
        List<List<Integer>> userMtx = userRBMtxPair.getMtx();
        
        // lista pozycji użytkowników w lokalnej macierzy
        List<Integer> localUsersPos = getLocalUsersPos(userMtxOrder);
        
        for(int i=0; i<localUsersPos.size(); ++i){
            int localUserVecPos = localUsersPos.get(i).intValue();
            if(localUserVecPos == -1){
                // nie powinno się zdarzyć, ale just in case
                continue;
            }
            
            List<Integer> vec = userMtx.get(i);
            List<Integer> localVec = mtx.get(localUserVecPos);
            for(int j=0; j<localUsersPos.size(); ++j){
                int localVecPos = localUsersPos.get(j).intValue();
                if(localVecPos == -1){
                    // nie powinno się zdarzyć, ale just in case
                    continue;
                }
                
                int max = (vec.get(j).intValue() > localVec.get(localVecPos).intValue()) 
                            ? vec.get(j).intValue() : localVec.get(localVecPos).intValue();
                localVec.set(localVecPos, max);
            }
        }
    }
    
    
    /**
     * Usuwa z macierzy informacje o użytkowniku
     * @param user uzytkownik
     */
    public synchronized void removeUserFromMatrix(UserInfo user){
        int userPos = mtxUsersOrder.indexOf(user);
        if(userPos == -1){
            return;
        }
        
        mtxUsersOrder.remove(userPos);
        for(List<Integer> vec : mtx){
            vec.remove(userPos);
        }
        mtx.remove(userPos);
        
    }
    
    
    /**
     * Aktualizuje wektor wiedzy użytkownika
     * @param user użytkownik
     * @param userVector wektor wiedzy użytkownika
     * @param userVectorOrder wektor kolejności użytkowników w wektorze wiedzy
     */
    public synchronized void updateUserVector(UserInfo user, List<Integer> userVector,
            List<UserInfo> userVectorOrder){
        int userPos = mtxUsersOrder.indexOf(user);
        if(userPos == -1){
            return;
        }
        

        // lista pozycji użytkowników w lokalnej macierzy
        List<Integer> localUsersPos = getLocalUsersPos(userVectorOrder);
        
        List<Integer> localUserVector = mtx.get(userPos);
        int currentValue;
        int vecValue;
        int newValue;
        for(int i=0; i<localUsersPos.size(); ++i){
            int localUserPos = localUsersPos.get(i).intValue();
            if(localUserPos == -1){
                // brak użytkownika w lokalnej macierzy
                continue;
            }
            
            currentValue = localUserVector.get(localUserPos).intValue();
            vecValue = userVector.get(i).intValue();
            newValue = (currentValue > vecValue) ? currentValue : vecValue;
            
            localUserVector.set(localUserPos, newValue);
            
        }
        
        // inkrementacja w wektorze zdalnego użytkownika
        int nextUserValue = mtx.get(userPos).get(userPos) + 1;
        mtx.get(userPos).set(userPos, nextUserValue);
    
    }
    
    
    /**
     * Zwiększa wartość zegara logicznego użytkownika
     * @param user użytkownik
     */
    public synchronized void incrementUserValue(UserInfo user){
        int userPos = mtxUsersOrder.indexOf(user);
        if(userPos == -1){
            return;
        }
        
        // inkrementacja w wektorze zalogowanego użytkownika
        if(!user.equals(this.user)){
            int nextUserValue = mtx.get(0).get(userPos).intValue() + 1;
            mtx.get(0).set(userPos, nextUserValue);
        }
        
    }
    
    
    /**
     * Zwraca informację, czy uzytkownik istnieje w macierzy
     * @param user użytkownik
     * @return true jeżeli istnieje
     */
    public synchronized boolean containsUser(UserInfo user){
        return mtxUsersOrder.contains(user);
    }
    
    
    /**
     * Zwraca informację, czy wektor użytkownika zalogowanego jest tak samo (lub bardziej)
     * aktualny jak wektor użytkownika zdalnego
     * @param userVec wektor użytkownika zdalnego
     * @param usersVecOrder wektor kolejności użytkownika zdalnego
     * @return true jeżeli jest aktualny
     */
    public synchronized boolean isMyVectorUpToDate(List<Integer> userVec, List<UserInfo> usersVecOrder){
        List<Integer> vec = mtx.get(0);
        
        // lista pozycji użytkowników w lokalnej macierzy
        List<Integer> localUsersPos = getLocalUsersPos(usersVecOrder);
        
        for(int i=0; i<localUsersPos.size(); ++i){
            int localUserPos = localUsersPos.get(i).intValue();
            if(localUserPos == -1){
                // brak użytkownika w lokalnej macierzy
                continue;
            }
            
            if(userVec.get(i).intValue() > vec.get(localUserPos)){
                return false;
            }
            
        }
        return true;
    }
    
    
    /**
     * Zwraca listę pozycji użytkowników w lokalnej macierzy
     * @param userMtxOrder lista pozycji użytkowników w innej macierzy
     * @return lista pozycji użytkowników w lokalnej macierzy
     */
    private List<Integer> getLocalUsersPos(List<UserInfo> userMtxOrder){
        List<Integer> localUsersPos = new LinkedList<Integer>();
        for(int i=0; i<userMtxOrder.size(); ++i){
            UserInfo user = userMtxOrder.get(i);
            int localUserPos = mtxUsersOrder.indexOf(user);
            localUsersPos.add(localUserPos);
        }
        return localUsersPos;
    }
}
