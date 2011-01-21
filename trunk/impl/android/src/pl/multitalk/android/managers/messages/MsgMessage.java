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
import pl.multitalk.android.util.Constants;

/**
 * Komunikat MSG
 * @author Michał Kołodziejski
 */
public class MsgMessage extends BaseMessage {

    private UserInfo msgSender;
    private UserInfo msgReceiver;
    private int msgId;
    private List<Integer> timeVec;
    private List<UserInfo> usersOrder;
    private String content;
    
    
    public UserInfo getMsgSender() {
        return msgSender;
    }

    public void setMsgSender(UserInfo msgSender) {
        this.msgSender = msgSender;
    }

    public UserInfo getMsgReceiver() {
        return msgReceiver;
    }

    public void setMsgReceiver(UserInfo msgReceiver) {
        this.msgReceiver = msgReceiver;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public List<Integer> getTimeVec() {
        return timeVec;
    }

    public void setTimeVec(List<Integer> timeVec) {
        this.timeVec = timeVec;
    }

    public List<UserInfo> getUsersOrder() {
        return usersOrder;
    }

    public void setUsersOrder(List<UserInfo> usersOrder) {
        this.usersOrder = usersOrder;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    
    @Override
    public void deserialize(String jsonString) {
        try {
            JSONTokener jsonTokener = new JSONTokener(jsonString);
            JSONObject object = (JSONObject) jsonTokener.nextValue();

            msgSender = new UserInfo();
            msgSender.setUid(object.getString("SENDER"));
            
            msgReceiver = new UserInfo();
            msgReceiver.setUid(object.getString("RECEIVER"));
            
            msgId = object.getInt("MSG_ID");
            
            timeVec = new ArrayList<Integer>();
            JSONArray timeVecArray = object.getJSONArray("TIME_VEC");
            for(int i=0; i<timeVecArray.length(); ++i){
                timeVec.add(new Integer(timeVecArray.getInt(i)));
            }
            
            usersOrder = new LinkedList<UserInfo>();
            JSONArray usersOrderArray = object.getJSONArray("VEC");
            for(int i=0; i<usersOrderArray.length(); ++i){
                UserInfo user = new UserInfo();
                user.setUid(usersOrderArray.getString(i));
                usersOrder.add(user);
            }
            
            content = object.getString("CONTENT");
            
        } catch (JSONException e) {
            Log.e(Constants.ERROR_TAG, "JSONException at MsgMessage#deserialize()");
            return;
        }
    }


    @Override
    public String serialize() {
        JSONObject object = new JSONObject();
        
        try {
            object.put("TYPE", "MSG");
            object.put("SENDER", msgSender.getUid());
            object.put("RECEIVER", (msgReceiver != null) ? msgReceiver.getUid() : "");
            object.put("MSG_ID", msgId);
            
            JSONArray timeVecArray = new JSONArray();
            for(Integer i : timeVec){
                timeVecArray.put(i.intValue());
            }
            object.put("TIME_VEC", timeVecArray);
            
            JSONArray usersOrderArray = new JSONArray();
            for(UserInfo user : usersOrder){
                usersOrderArray.put(user.getUid());
            }
            object.put("VEC", usersOrderArray);
            
            object.put("CONTENT", content);
            
        } catch (JSONException e) {
            Log.e(Constants.ERROR_TAG, "JSONException at MsgMessage#serialize()");
            return "ERROR";
        }
        
        return object.toString();
    }
    

    @Override
    public Message getClone() {
        MsgMessage cloneMessage = new MsgMessage();
        if(sender != null)
            cloneMessage.setSenderInfo(new UserInfo(sender));
        if(recipient != null)
            cloneMessage.setRecipientInfo(new UserInfo(recipient));
        
        if(msgSender != null)
            cloneMessage.setMsgSender(new UserInfo(msgSender));
        if(msgReceiver != null)
            cloneMessage.setMsgReceiver(new UserInfo(msgReceiver));
        cloneMessage.setMsgId(msgId);
        
        if(timeVec != null){
            List<Integer> cloneTimeVec = new ArrayList<Integer>();
            for(Integer i : timeVec){
                cloneTimeVec.add(new Integer(i));
            }
            cloneMessage.setTimeVec(cloneTimeVec);
        }
        
        if(usersOrder != null){
            List<UserInfo> cloneUsersOrder = new LinkedList<UserInfo>();
            for(UserInfo u : usersOrder){
                cloneUsersOrder.add(new UserInfo(u));
            }
            cloneMessage.setUsersOrder(cloneUsersOrder);
        }
        
        if(content != null)
            cloneMessage.setContent(new String(content));
        
        return cloneMessage;
    }

    
    
    @Override
    public boolean equals(Object o) {
        if(o==null)
            return false;
        if(o==this)
            return true;
        
        if(!(o instanceof MsgMessage))
            return false;
        
        MsgMessage oMsg = (MsgMessage) o;
        if(oMsg.getSenderInfo().equals(this.getSenderInfo())
                && oMsg.getMsgId() == this.getMsgId())
            return true;
        
        return false;
    }
    
    
    @Override
    public int hashCode() {
        return getMsgId();
    }
}
