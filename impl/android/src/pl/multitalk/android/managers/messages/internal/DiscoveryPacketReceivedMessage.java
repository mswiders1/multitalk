package pl.multitalk.android.managers.messages.internal;

import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.managers.messages.Message;

/**
 * @author Michał Kołodziejski
 */
public class DiscoveryPacketReceivedMessage extends InternalBaseMessage {
    
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
