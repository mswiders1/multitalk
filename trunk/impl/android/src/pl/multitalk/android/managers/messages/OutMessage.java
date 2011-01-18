package pl.multitalk.android.managers.messages;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;
import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.util.Constants;

/**
 * Komunikat wylogowania
 * @author Michał Kołodziejski
 */
public class OutMessage extends BaseMessage {

    private UserInfo userInfo;
    
    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
    
    @Override
    public void deserialize(String jsonString) {
        try {
            JSONTokener jsonTokener = new JSONTokener(jsonString);
            JSONObject object = (JSONObject) jsonTokener.nextValue();

            userInfo = new UserInfo();
            userInfo.setUid(object.getString("UID"));
            
        } catch (JSONException e) {
            Log.e(Constants.ERROR_TAG, "JSONException at OutMessage#deserialize()");
            return;
        }
    
    }

    @Override
    public String serialize() {
        JSONObject object = new JSONObject();
        
        try {
            object.put("TYPE", "OUT");
            object.put("UID", userInfo.getUid());
            
        } catch (JSONException e) {
            Log.e(Constants.ERROR_TAG, "JSONException at OutMessage#serialize()");
            return "ERROR";
        }
        
        return object.toString();
    }

    @Override
    public Message getClone() {
        OutMessage cloneMessage = new OutMessage();

        if(sender != null)
            cloneMessage.setSenderInfo(new UserInfo(sender));
        if(recipient != null)
            cloneMessage.setRecipientInfo(new UserInfo(recipient));
        
        if(userInfo != null)
            cloneMessage.setUserInfo(new UserInfo(userInfo));
        
        return cloneMessage;
    }

}
