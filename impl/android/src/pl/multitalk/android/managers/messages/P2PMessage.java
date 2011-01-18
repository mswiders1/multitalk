package pl.multitalk.android.managers.messages;

import org.json.JSONException;
import org.json.JSONObject;

import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.util.Constants;
import android.util.Log;

/**
 * Komunikat P2P
 * @author Michał Kołodziejski
 */
public class P2PMessage extends BaseMessage {

    @Override
    public void deserialize(String jsonString) {
    }

    @Override
    public String serialize() {
        JSONObject object = new JSONObject();
        
        try {
            object.put("TYPE", "P2P");
            
        } catch (JSONException e) {
            Log.e(Constants.ERROR_TAG, "JSONException at P2PMessage#serialize()");
            return "ERROR";
        }
        
        return object.toString();
    }

    @Override
    public Message getClone() {
        P2PMessage cloneMessage = new P2PMessage();
        
        if(sender != null)
            cloneMessage.setSenderInfo(new UserInfo(sender));
        if(recipient != null)
            cloneMessage.setRecipientInfo(new UserInfo(recipient));
        
        return cloneMessage;
    }

}
