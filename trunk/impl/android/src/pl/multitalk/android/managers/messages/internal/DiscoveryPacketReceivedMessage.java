package pl.multitalk.android.managers.messages.internal;

import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.managers.messages.BaseMessage;
import pl.multitalk.android.managers.messages.Message;

/**
 * @author Michał Kołodziejski
 */
public class DiscoveryPacketReceivedMessage extends BaseMessage {

    @Override
    public void deserialize(String jsonString) {
    }

    @Override
    public String serialize() {
        return null;
    }

    
    @Override
    public Message getClone() {
        DiscoveryPacketReceivedMessage cloneMessage = new DiscoveryPacketReceivedMessage();
        
        if(sender != null)
            cloneMessage.setSenderInfo(new UserInfo(sender));
        if(recipient != null)
            cloneMessage.setRecipientInfo(new UserInfo(recipient));
        
        return cloneMessage;
    }

}
