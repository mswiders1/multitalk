package pl.multitalk.android.managers.messages;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;
import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.util.Constants;

/**
 * Komunikat GET
 * @author Michał Kołodziejski
 */
public class GetMessage extends BaseMessage {

    private UserInfo userInfo;
    private int msgId;
    
    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
    
    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    @Override
    public void deserialize(String jsonString) {
        try {
            JSONTokener jsonTokener = new JSONTokener(jsonString);
            JSONObject object = (JSONObject) jsonTokener.nextValue();

            userInfo = new UserInfo();
            userInfo.setUid(object.getString("UID"));
            msgId = object.getInt("MSG_ID");
            
        } catch (JSONException e) {
            Log.e(Constants.ERROR_TAG, "JSONException at GetMessage#deserialize()");
            return;
        }
    
    }

    @Override
    public String serialize() {
        JSONObject object = new JSONObject();
        
        try {
            object.put("TYPE", "GET");
            object.put("UID", userInfo.getUid());
            object.put("MSG_ID", msgId);
            
        } catch (JSONException e) {
            Log.e(Constants.ERROR_TAG, "JSONException at GetMessage#serialize()");
            return "ERROR";
        }
        
        return object.toString();
    }

    @Override
    public Message getClone() {
        GetMessage cloneMessage = new GetMessage();

        if(sender != null)
            cloneMessage.setSenderInfo(new UserInfo(sender));
        if(recipient != null)
            cloneMessage.setRecipientInfo(new UserInfo(recipient));
        
        if(userInfo != null)
            cloneMessage.setUserInfo(new UserInfo(userInfo));
        
        cloneMessage.setMsgId(msgId);
        
        return cloneMessage;
    }

}
