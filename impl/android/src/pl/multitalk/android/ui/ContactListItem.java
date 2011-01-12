package pl.multitalk.android.ui;

/**
 * Element listy kontaktów
 * @author Michał Kołodziejski
 */
public class ContactListItem {

    /**
     * Nazwa użytkownika
     */
    private String username;
    
    /**
     * Tworzy element
     */
    public ContactListItem() {
        // nic
    }

    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    
}
