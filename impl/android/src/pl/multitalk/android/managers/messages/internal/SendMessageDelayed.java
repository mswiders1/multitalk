package pl.multitalk.android.managers.messages.internal;

/**
 * Wysyła opóźnioną wiadomość - do celów testowych
 * @author Michał Kołodziejski
 */
public class SendMessageDelayed extends InternalBaseMessage {

    private int delay;
    private SendMessageToClient msgToSend;

    public void setMsgToSend(SendMessageToClient msgToSend) {
        this.msgToSend = msgToSend;
    }

    public SendMessageToClient getMsgToSend() {
        return msgToSend;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }
    
}
