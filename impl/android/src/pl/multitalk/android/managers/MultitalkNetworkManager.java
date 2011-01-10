package pl.multitalk.android.managers;

import android.content.Context;
import android.util.Log;
import pl.multitalk.android.util.DigestUtil;
import pl.multitalk.android.util.NetworkUtil;
import pl.multitalk.android.util.NetworkUtil.WifiNotEnabledException;

/**
 * Manager sieci Multitalk. Zarządza połączeniami z innymi użytkownikami.
 * @author Michał Kołodziejski
 */
public class MultitalkNetworkManager {

    private Context context;
    
    /*
     * Dane niezbędne do zalogowania do sieci Multitalk
     */
    private String multitalkLogin;
    private String multitalkMACaddress;
    private String multitalkIPaddress;
    private boolean isLoggedIn;
    
    /**
     * Tworzy zarządcę sieci
     */
    public MultitalkNetworkManager(Context context){
        this.context = context;
        
        isLoggedIn = false;
        multitalkLogin = null;
        multitalkMACaddress = null;
        multitalkIPaddress = null;
    }
    
    
    /**
     * Przeprowadza akcję logowania do sieci Multitalk
     * @param login login użytkownika
     * @return true jeżeli zalogowano, false w przeciwnym przypadku
     */
    public boolean logIn(String login){
        if(isLoggedIn){
            logout();
        }
        
        if(!NetworkUtil.isWifiEnabled(context)){
            // wifi wyłączone
            return false;
        }

        int ipInt = 0;
        try {
            ipInt = NetworkUtil.getIPaddressAsInt(context);
            if(ipInt == 0){
                // brak połączenia z siecią
                return false;
            }
            
            multitalkMACaddress = NetworkUtil.getWifiMAC(context);
            multitalkIPaddress = NetworkUtil.getIPaddressAsString(context);
            
        } catch (WifiNotEnabledException e) {
            return false;
        }
        multitalkLogin = login;

        StringBuffer sb = new StringBuffer();
        sb.append(multitalkMACaddress);
        sb.append(multitalkIPaddress);
        sb.append(multitalkLogin);
        
        String uid = DigestUtil.getBase64(DigestUtil.getSHA1(sb.toString()));
        Log.d("Multitalk-DEBUG", "logIn| mac: "+multitalkMACaddress+", ip: "+multitalkIPaddress
                +", login: "+multitalkLogin+" | UID (before encoding): "+sb.toString()
                +" | UID: "+uid);
        
        
        return true;
    }
    
    
    /**
     * Wylogowuje z sieci Multitalk
     */
    public void logout(){
        isLoggedIn = false;
        multitalkLogin = null;
        multitalkMACaddress = null;
        multitalkIPaddress = null;
    }
}
