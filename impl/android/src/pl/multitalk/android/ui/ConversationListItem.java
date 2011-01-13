package pl.multitalk.android.ui;

/**
 * Element listy wiadomości
 * @author Michał Kołodziejski
 */
public class ConversationListItem {

    /**
     * Nazwa użytkownika
     */
    private String username;
    /**
     * Wiadomość
     */
    private String message;
    /**
     * Data wysłania
     */
    private String date;
    
    
    /**
     * Tworzy element
     */
    public ConversationListItem() {
        // nic
    }

    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }


    public String getDate() {
        return date;
    }


    public void setDate(String date) {
        this.date = date;
    }
    
    
}
