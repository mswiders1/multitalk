package pl.multitalk.android.managers.messages;

/**
 * Specjalny komunikat do zakończenia wątku ClientTCPSender
 * @author Michał Kołodziejski
 */
public final class FinishSenderMessage extends BaseMessage{

    @Override
    public String serialize() {
        return null;
    }

    @Override
    public void deserialize(String jsonString) {
    }

}
