package pl.multitalk.android;

import java.io.UnsupportedEncodingException;

import pl.multitalk.android.util.DigestUtil;
import pl.multitalk.android.util.NetworkUtil;
import pl.multitalk.android.util.NetworkUtil.WifiNotEnabledException;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Ekran startowy aplikacji
 * @author Michał Kołodziejski
 */
public class StartActivity extends Activity {

    String username = "kouodziey";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
        
        // debug
        printDebugInfo();
    }
    
    
    
    /**
     * Wpisuje do logów informacje na potrzeby debugu
     */
    private void printDebugInfo(){
        boolean wifiOn = NetworkUtil.isWifiEnabled(getBaseContext());
        Log.d("Multitalk-DEBUG", "Wifi enabled: " + wifiOn);
        
        try {
            String mac = NetworkUtil.getWifiMAC(getBaseContext());
            Log.d("Multitalk-DEBUG", "MAC address: " + mac);
            
            String ipAddress = NetworkUtil.getIPaddressAsString(getBaseContext());
            Log.d("Multitalk-DEBUG", "IP address: " + ipAddress);
        
        } catch (WifiNotEnabledException e) {
            Log.d("Multitalk-DEBUG", "WifiNotEnabledException occured");
        }
    }
}