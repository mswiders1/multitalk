package pl.multitalk.android.managers.messages.internal;

import pl.multitalk.android.managers.messages.BaseMessage;
import pl.multitalk.android.managers.messages.Message;

/**
 * Klasa bazowa dla komunikatów wewnętrznych
 * @author Michał Kołodziejski
 */
public abstract class InternalBaseMessage extends BaseMessage {

    @Override
    public void deserialize(String jsonString) {
    }

    @Override
    public Message getClone() {
        return this;
    }

    @Override
    public String serialize() {
        return null;
    }

}
