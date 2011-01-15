package pl.multitalk.android.managers.messages;

import pl.multitalk.android.datatypes.UserInfo;

/**
 * Wiadomość wysyłana/przesyłana do/od klienta
 * @author Michał Kołodziejski
 */
public interface Message{

    /**
     * Ustawia dane nadawcy wiadomości
     * @param senderInfo dane nadawcy wiadomości
     */
    void setSenderInfo(UserInfo senderInfo);

    /**
     * Zwraca dane nadawcy wiadomości
     * @return dane nadawcy wiadomości
     */
    UserInfo getSenderInfo();
    
    /**
     * Ustawia dane odbiorcy wiadomości
     * @param recipientInfo dane odbiorcy wiadomości
     */
    void setRecipientInfo(UserInfo recipientInfo);
    
    /**
     * Zwraca dane odbiorcy wiadomości
     * @return dane odbiorcy wiadomości
     */
    UserInfo getRecipientInfo();
    
    /**
     * Serializuje do postaci JSON
     * @return zserializowany komunikat
     */
    String serialize();
    
    /**
     * Deserializuje z postaci JSON
     * @param jsonString komunikat w postaci JSON
     */
    void deserialize(String jsonString);
    
    
    /**
     * Zwraca głęboką kopię wiadomości
     * @return głęboka kopia wiadomości
     */
    Message getClone();
}
