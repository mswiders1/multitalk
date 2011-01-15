package pl.multitalk.android.managers.messages;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Fabryka komunikatów
 * @author Michał Kołodziejski
 */
public class MessageFactory {

    
    /**
     * Tworzy klasę komunikatu odpowiedniego typu na podstawie wiadomości w postaci JSON
     * @param jsonString komunikat w postaci JSON
     * @return klasa komunikatu odpowiedniego typu
     * @throws JSONException błąd parse'owania JSON-a
     */
    public static Message fromJSON(String jsonString) throws JSONException{
        Message message = null;
        
        JSONTokener jsonTokener = new JSONTokener(jsonString);
        JSONObject object = (JSONObject) jsonTokener.nextValue();

        String messageType = object.getString("TYPE");
        
        if(messageType.equals("HII")){
            message = new HiMessage();
        
        } else if(messageType.equals("LOG")){
            message = new LogMessage();
            
        }
        
        
        message.deserialize(jsonString);
        return message;
    }
}
