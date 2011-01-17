package pl.multitalk.android.managers.messages;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.model.RBMtxPair;
import pl.multitalk.android.util.Constants;

/**
 * Komunikat typu MTX
 * @author Michał Kołodziejski
 */
public class MtxMessage extends BaseMessage {
    
    /**
     * Para: macierz + kolejność użytkowników
     */
    private RBMtxPair mtxPair;


    public void setMtxPair(RBMtxPair mtxPair) {
        this.mtxPair = mtxPair;
    }

    public RBMtxPair getMtxPair() {
        return mtxPair;
    }

    @Override
    public void deserialize(String jsonString) {
        try {
            JSONTokener jsonTokener = new JSONTokener(jsonString);
            JSONObject object = (JSONObject) jsonTokener.nextValue();
            
            // wektor kolejności
            JSONArray vec = object.getJSONArray("VEC");
            List<UserInfo> usersList = new LinkedList<UserInfo>();
            for(int i=0; i<vec.length(); ++i){
                UserInfo user = new UserInfo();
                user.setUid(vec.getString(i));
                usersList.add(user);
            }
            
            // macierz
            JSONArray mtxArray = object.getJSONArray("MAC");
            List<List<Integer>> mtx = new ArrayList<List<Integer>>();
            for(int i=0; i<mtxArray.length(); ++i){
                List<Integer> mtxRow = new ArrayList<Integer>();
                JSONArray mtxRowArray = mtxArray.getJSONArray(i);
                
                for(int j=0; j<mtxRowArray.length(); ++j){
                    mtxRow.add(mtxRowArray.getInt(j));
                }
                mtx.add(mtxRow);
            }
            mtxPair = new RBMtxPair(usersList, mtx);
            
        } catch (JSONException e) {
            Log.e(Constants.ERROR_TAG, "JSONException at HiMessage#deserialize()");
            return;
        }

    }

    @Override
    public String serialize() {
        JSONObject object = new JSONObject();
        
        try {
            object.put("TYPE", "MTX");
            
            // wektor kolejności
            JSONArray vec = new JSONArray();
            for(UserInfo user : mtxPair.getMtxUsersOrder()){
                vec.put(user.getUid());
            }
            object.put("VEC", vec);
            
            // macierz
            JSONArray mtx = new JSONArray();
            for(List<Integer> vecValues : mtxPair.getMtx()){
                JSONArray mtxRow = new JSONArray();
                for(Integer i : vecValues){
                    mtxRow.put(i.intValue());
                }
                mtx.put(mtxRow);
            }
            object.put("MAC", mtx);
            
            
        } catch (JSONException e) {
            Log.e(Constants.ERROR_TAG, "JSONException at MtxMessage#serialize()");
            return "ERROR";
        }
        
        return object.toString();
    }
    

    @Override
    public Message getClone() {
        MtxMessage cloneMessage = new MtxMessage();
        if(sender != null)
            cloneMessage.setSenderInfo(new UserInfo(sender));
        if(recipient != null)
            cloneMessage.setRecipientInfo(new UserInfo(recipient));
        
        if(mtxPair != null){
            // pozostawiamy bez klonowania - i tak jest read-only
            cloneMessage.setMtxPair(mtxPair);
        }
        
        return cloneMessage;
    }

}
