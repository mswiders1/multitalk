package pl.multitalk.android.managers.messages;

/**
 * Wiadomość wysyłana/przesyłana do/od klienta
 * @author Michał Kołodziejski
 */
public interface Message {

    /**
     * Serializuje do postaci JSON
     * @return zserializowany komunikat
     */
    String serialize();
    
}
