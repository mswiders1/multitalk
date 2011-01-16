package pl.multitalk.android.util;

/**
 * Klasa stałych
 * @author Michał Kołodziejski
 */
public class Constants {

    /*
     * Log
     */
    public static final String DEBUG_TAG = "Multitalk-DEBUG";
    public static final String ERROR_TAG = "Multitalk-ERROR";
    
    /*
     * Akcje
     */
    public static final String ACTION_CONTACT_LIST_ACTIVITY = "pl.multitalk.android.ContactList";
    public static final String ACTION_CONVERSATION_ACTIVITY = "pl.multitalk.android.Conversation";

    /*
     * Networking
     */
    public static final int UDP_PORT = 3554;
    public static final int TCP_PORT = 3554;
    public static final String DISCOVERY_PACKET_DATA = "MULTITALK_5387132";
    public static final String BEGIN_MESSAGE_HEADER = "BEGIN_MESSAGE:";
    
}
