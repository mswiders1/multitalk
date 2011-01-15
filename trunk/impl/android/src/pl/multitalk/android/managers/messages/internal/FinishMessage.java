package pl.multitalk.android.managers.messages.internal;

import pl.multitalk.android.managers.messages.BaseMessage;
import pl.multitalk.android.managers.messages.Message;

/**
 * Specjalny komunikat do zakończenia wątku ClientTCPSender
 * @author Michał Kołodziejski
 */
public final class FinishMessage extends BaseMessage{

    @Override
    public String serialize() {
        return null;
    }

    @Override
    public void deserialize(String jsonString) {
    }

    @Override
    public Message getClone() {
        return this;
    }

}
