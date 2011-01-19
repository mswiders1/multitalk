package pl.multitalk.android.managers.messages.internal;


/**
 * Komunikat wysłania wiadomości do klienta
 * @author Michał Kołodziejski
 */
public class SendMessageToClient extends InternalBaseMessage {
    
    private String content;

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
    
}
