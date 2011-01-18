package pl.multitalk.android.managers.messages;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import pl.multitalk.android.util.Constants;

import android.util.Log;

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
            
        } else if(messageType.equals("MTX")){
            message = new MtxMessage();
            
        } else if(messageType.equals("OUT")){
            message = new OutMessage();
            
        } else {
            Log.d(Constants.DEBUG_TAG, "MessageFactory: Unknown message type");
            return null;
        }
        
        
        message.deserialize(jsonString);
        return message;
    }
}
