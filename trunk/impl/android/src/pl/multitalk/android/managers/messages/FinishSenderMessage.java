package pl.multitalk.android.managers.messages;

import pl.multitalk.android.datatypes.UserInfo;

/**
 * Specjalny komunikat do zakończenia wątku ClientTCPSender
 * @author Michał Kołodziejski
 */
public class FinishSenderMessage implements Message {

    @Override
    public UserInfo getRecipientInfo() {
        return null;
    }

    @Override
    public UserInfo getSenderInfo() {
        return null;
    }

    @Override
    public String serialize() {
        return null;
    }

    @Override
    public void setRecipientInfo(UserInfo recipientInfo) {
    }

    @Override
    public void setSenderInfo(UserInfo senderInfo) {
    }

}
