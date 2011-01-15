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

    @Override
    public void deserialize(String jsonString) {
        try {
            JSONTokener jsonTokener = new JSONTokener(jsonString);
            JSONObject object = (JSONObject) jsonTokener.nextValue();

            object.getString("TYPE");
            if(sender==null){
                sender = new UserInfo();
            }
            sender.setUid(object.getString("UID"));
            sender.setUsername(object.getString("USERNAME"));
            
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
            object.put("UID", sender.getUid());
            object.put("USERNAME", sender.getUsername());
            
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
        
        return cloneMessage;
    }

}
