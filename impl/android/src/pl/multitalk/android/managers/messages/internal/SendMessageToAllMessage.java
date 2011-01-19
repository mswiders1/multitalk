package pl.multitalk.android.managers.messages.internal;

import pl.multitalk.android.managers.messages.Message;

/**
 * Wewnętrzny komunikat do wysłania wiadomości do wszystkich 
 * @author Michał Kołodziejski
 */
public class SendMessageToAllMessage extends InternalBaseMessage {

    private Message messageToSend;
    
    
    public Message getMessageToSend() {
        return messageToSend;
    }

    public void setMessageToSend(Message messageToSend) {
        this.messageToSend = messageToSend;
    }

}
