package pl.multitalk.android.managers;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.util.Constants;
import pl.multitalk.android.util.DigestUtil;
import pl.multitalk.android.util.NetworkUtil;
import pl.multitalk.android.util.NetworkUtil.WifiNotEnabledException;

/**
 * Manager sieci Multitalk. Zarządza połączeniami z innymi użytkownikami.
 * @author Michał Kołodziejski
 */
public class MultitalkNetworkManager {

    private Context context;
    private BroadcastNetworkManager broadcastNetworkManager;
    private TCPIPNetworkManager tcpipNetworkManager;
    
    /*
     * Dane niezbędne do zalogowania do sieci Multitalk
     */
    private UserInfo userInfo;
    private boolean isLoggedIn;
    
    /*
     * Informacje o innych uzytkownikach
     */
    private List<UserInfo> users;
    
    /**
     * Tworzy zarządcę sieci
     */
    public MultitalkNetworkManager(Context context){
        this.context = context;
        this.broadcastNetworkManager = new BroadcastNetworkManager(context);
        this.tcpipNetworkManager = new TCPIPNetworkManager(context, this);
        
        isLoggedIn = false;
        userInfo = null;
        users = new ArrayList<UserInfo>();
    }
    
    
    /**
     * Przeprowadza akcję logowania do sieci Multitalk
     * @param login login użytkownika
     */
    public void logIn(String login){
        if(isLoggedIn){
            logout();
        }
        
        UserInfo newUserInfo = new UserInfo();
        
        if(!NetworkUtil.isWifiEnabled(context)){
            // wifi wyłączone
            return;
        }

        int ipInt = 0;
        try {
            ipInt = NetworkUtil.getIPaddressAsInt(context);
            if(ipInt == 0){
                // brak połączenia z siecią
                return;
            }
            
            newUserInfo.setMacAddress(NetworkUtil.getWifiMAC(context));
            newUserInfo.setIpAddress(NetworkUtil.getIPaddressAsString(context));
            
        } catch (WifiNotEnabledException e) {
            return;
        }
        newUserInfo.setUsername(login);

        StringBuffer sb = new StringBuffer();
        sb.append(newUserInfo.getMacAddress());
        sb.append(newUserInfo.getIpAddress());
        sb.append(newUserInfo.getUsername());
        
        newUserInfo.setUid(DigestUtil.getBase64(DigestUtil.getSHA1(sb.toString())));
        
        userInfo = newUserInfo;
        
        Log.d(Constants.DEBUG_TAG, "logIn| mac: "+userInfo.getMacAddress()
                +", ip: "+userInfo.getIpAddress()
                +", username: "+userInfo.getUsername()
                +" | UID (before encoding): "+sb.toString()
                +" | UID: "+userInfo.getUid());
        
        // wysłanie UDP discovery
        broadcastNetworkManager.sendUDPHostsDiscoveryPacket();
        
        // oczekiwanie na klientów
        tcpipNetworkManager.startListeningForConnections();
    }
    
    
    /**
     * Wylogowuje z sieci Multitalk
     */
    public void logout(){
        tcpipNetworkManager.disconnectAllClients();
        tcpipNetworkManager.stopListeningForConnections();
        isLoggedIn = false;
        userInfo = null;
        users = new ArrayList<UserInfo>();
    }
    
    
    /**
     * Dodaje informację o nowym użytkowniku
     * @param newUserInfo informacje o nowym użytkowniku
     */
    private void addUserInfo(UserInfo newUserInfo){
        if(!users.contains(newUserInfo)){
            users.add(newUserInfo);
        }
    }
    
    
    /**
     * Usuwa informację o użytkowniku
     * @param userInfoToRemove informacje o użytkowniku do usunięcia
     */
    private void removeUserInfo(UserInfo userInfoToRemove){
        if(users.contains(userInfoToRemove)){
            users.remove(userInfoToRemove);
        }
    }
}
