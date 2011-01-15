package pl.multitalk.android.managers.messages;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.util.Constants;

/**
 * Wiadomość typu "HI"
 * @author Michał Kołodziejski
 */
public class HiMessage extends BaseMessage {

    protected List<UserInfo> loggedUsers;
    
    
    public List<UserInfo> getLoggedUsers() {
        return loggedUsers;
    }
    

    public void setLoggedUsers(List<UserInfo> loggedUsers) {
        this.loggedUsers = loggedUsers;
    }

    
    @Override
    public String serialize() {
        JSONObject object = new JSONObject();
        
        try {
            object.put("TYPE", "HII");
            object.put("UID", sender.getUid());
            object.put("USERNAME", sender.getUsername());
            
            JSONArray vec = new JSONArray();
            if(loggedUsers != null){
                for(UserInfo user : loggedUsers){
                    JSONObject userObject = new JSONObject();
                    userObject.put("IP_ADDRESS", user.getIpAddress());
                    userObject.put("UID", user.getUid());
                    userObject.put("USERNAME", user.getUsername());
                    
                    vec.put(userObject);
                }
            }
            
            object.put("VECTOR", vec);
            
        } catch (JSONException e) {
            Log.e(Constants.ERROR_TAG, "JSONException at HiMessage#serialize()");
            return "ERROR";
        }
        
        return object.toString();
    }
    

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
            
            
            JSONArray vec = object.getJSONArray("VECTOR");
            loggedUsers = new ArrayList<UserInfo>();
            for(int i=0; i< vec.length(); ++i){
                JSONObject userObject = vec.getJSONObject(i);
                
                UserInfo user = new UserInfo();
                user.setIpAddress(userObject.getString("IP_ADDRESS"));
                user.setUid(userObject.getString("UID"));
                user.setUsername(userObject.getString("USERNAME"));
                
                loggedUsers.add(user);
            }
            
        } catch (JSONException e) {
            Log.e(Constants.ERROR_TAG, "JSONException at HiMessage#deserialize()");
            return;
        }
        
    }


    @Override
    public Message getClone() {
        HiMessage cloneMessage = new HiMessage();
        if(sender != null)
            cloneMessage.setSenderInfo(new UserInfo(sender));
        if(recipient != null)
            cloneMessage.setRecipientInfo(new UserInfo(recipient));
        
        if(loggedUsers != null){
            List<UserInfo> newLoggedUsers = new ArrayList<UserInfo>();
            for(UserInfo userInfo : loggedUsers){
                newLoggedUsers.add(new UserInfo(userInfo));
            }
            cloneMessage.setLoggedUsers(newLoggedUsers);
        }
        return cloneMessage;
    }

}
