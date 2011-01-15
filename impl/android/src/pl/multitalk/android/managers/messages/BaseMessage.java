package pl.multitalk.android.managers.messages;

import pl.multitalk.android.datatypes.UserInfo;

/**
 * Klasa bazowa dla wiadomości
 * @author Michał Kołodziejski
 */
public abstract class BaseMessage implements Message {

    protected UserInfo sender;
    protected UserInfo recipient;
    
    @Override
    public UserInfo getRecipientInfo() {
        return recipient;
    }

    @Override
    public UserInfo getSenderInfo() {
        return sender;
    }

    @Override
    public void setRecipientInfo(UserInfo recipientInfo) {
        recipient = recipientInfo;
    }

    @Override
    public void setSenderInfo(UserInfo senderInfo) {
        sender = senderInfo;
    }

}
