package pl.multitalk.android.model;

import pl.multitalk.android.managers.MultitalkNetworkManager;
import android.app.Application;

/**
 * Klasa aplikacji
 * @author Michał Kołodziejski
 */
public class MultitalkApplication extends Application {

    /**
     * Manager połączeń sieci Multitalk
     */
    private MultitalkNetworkManager multitalkNetworkManager;
    
    
    /**
     * Tworzy instancję aplikacji
     */
    public MultitalkApplication() {
        multitalkNetworkManager = new MultitalkNetworkManager(this);
    }
    
    
    /**
     * Zwraca managera połączeń sieci Multitalk
     * @return manager połączeń sieci Multitalk
     */
    public MultitalkNetworkManager getMultitalkNetworkManager(){
        return multitalkNetworkManager;
    }
}
