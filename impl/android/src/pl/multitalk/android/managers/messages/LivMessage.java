package pl.multitalk.android.managers.messages;

import java.util.GregorianCalendar;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;
import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.util.Constants;

/**
 * Komunikat LIV
 * @author Michał Kołodziejski
 */
public class LivMessage extends BaseMessage {

    private UserInfo userInfo;
    private int seq;
    private GregorianCalendar receiveDate;
    
    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }
    

    public GregorianCalendar getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(GregorianCalendar receiveDate) {
        this.receiveDate = receiveDate;
    }
    
    public void setReceiveDateToNow(){
        this.receiveDate = new GregorianCalendar();
        this.receiveDate.setTimeInMillis(System.currentTimeMillis());
    }

    @Override
    public void deserialize(String jsonString) {
        try {
            JSONTokener jsonTokener = new JSONTokener(jsonString);
            JSONObject object = (JSONObject) jsonTokener.nextValue();

            userInfo = new UserInfo();
            userInfo.setUid(object.getString("UID"));
            userInfo.setIpAddress(object.getString("IP_ADDRESS"));
            seq = object.getInt("SEQUENCE");
            
        } catch (JSONException e) {
            Log.e(Constants.ERROR_TAG, "JSONException at OutMessage#deserialize()");
            return;
        }
    }

    @Override
    public String serialize() {
        JSONObject object = new JSONObject();
        
        try {
            object.put("TYPE", "LIV");
            object.put("UID", userInfo.getUid());
            object.put("IP_ADDRESS", userInfo.getIpAddress());
            object.put("SEQUENCE", seq);
            
        } catch (JSONException e) {
            Log.e(Constants.ERROR_TAG, "JSONException at OutMessage#serialize()");
            return "ERROR";
        }
        
        return object.toString();
    }

    @Override
    public Message getClone() {
        LivMessage cloneMessage = new LivMessage();

        if(sender != null)
            cloneMessage.setSenderInfo(new UserInfo(sender));
        if(recipient != null)
            cloneMessage.setRecipientInfo(new UserInfo(recipient));
        
        if(userInfo != null)
            cloneMessage.setUserInfo(new UserInfo(userInfo));
        cloneMessage.setSeq(getSeq());
        if(receiveDate != null){
            GregorianCalendar cloneReceiveDate = new GregorianCalendar();
            cloneReceiveDate.setTimeInMillis(receiveDate.getTimeInMillis());
            cloneMessage.setReceiveDate(cloneReceiveDate);
        }
        
        return cloneMessage;
    }

}
