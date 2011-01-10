package pl.multitalk.android.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.util.Log;

/**
 * Klasa użytkowa do wyliczania wartości skrótów
 * @author Michał Kołodziejski
 */
public class DigestUtil {

    /**
     * Wylicza i zwraca skrót wiadomości algorytmem SHA-1
     * @param message wiadomość
     * @return skrót wiadomości
     */
    public static byte[] getSHA1(String message){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(message.getBytes());
            byte[] digestBytes = md.digest();
            return digestBytes;
            
        } catch (NoSuchAlgorithmException e) {
            Log.d("Multitalk-DEBUG", "No SHA-1 algorithm found");
            return null;
        }
    }
    
    
    /**
     * Zwraca ciąg znaków w kodzie base64
     * @param bytes bajty do zakodowania
     * @return zakodowany ciąg bajtów
     */
    public static String getBase64(byte[] bytes){
        return Base64.encodeBytes(bytes);
    }
}
