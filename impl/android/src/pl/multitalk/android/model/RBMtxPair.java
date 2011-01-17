package pl.multitalk.android.model;

import java.util.List;

import pl.multitalk.android.datatypes.UserInfo;

/**
 * Para: macierz reliable-broadcase + kolejność użytkowników
 * @author Michał Kołodziejski
 */
public class RBMtxPair {

    private List<UserInfo> mtxUsersOrder;
    private List<List<Integer>> mtx;
    
    public RBMtxPair(List<UserInfo> mtxUsersOrder, List<List<Integer>> mtx) {
        this.mtxUsersOrder = mtxUsersOrder;
        this.mtx = mtx;
    }

    public List<UserInfo> getMtxUsersOrder() {
        return mtxUsersOrder;
    }

    public List<List<Integer>> getMtx() {
        return mtx;
    }
    
}
