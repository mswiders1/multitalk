package pl.multitalk.android.managers.messages;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.util.Constants;
import android.util.Log;

/**
 * Komunikat logowania
 * @author Michał Kołodziejski
 */
public class LogMessage extends BaseMessage {

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
            userInfo.setUsername(object.getString("USERNAME"));
            
        } catch (JSONException e) {
            Log.e(Constants.ERROR_TAG, "JSONException at LogMessage#deserialize()");
            return;
        }
    
    }

    @Override
    public String serialize() {
        JSONObject object = new JSONObject();
        
        try {
            object.put("TYPE", "LOG");
            object.put("UID", userInfo.getUid());
            object.put("USERNAME", userInfo.getUsername());
            
        } catch (JSONException e) {
            Log.e(Constants.ERROR_TAG, "JSONException at LogMessage#serialize()");
            return "ERROR";
        }
        
        return object.toString();
    }
    

    @Override
    public Message getClone() {
        LogMessage cloneMessage = new LogMessage();

        if(sender != null)
            cloneMessage.setSenderInfo(new UserInfo(sender));
        if(recipient != null)
            cloneMessage.setRecipientInfo(new UserInfo(recipient));
        
        if(userInfo != null)
            cloneMessage.setUserInfo(new UserInfo(userInfo));
        
        return cloneMessage;
    }

}
