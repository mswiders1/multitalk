package pl.multitalk.android;

import pl.multitalk.android.managers.MultitalkNetworkManager;
import pl.multitalk.android.model.MultitalkApplication;
import pl.multitalk.android.util.NetworkUtil;
import pl.multitalk.android.util.NetworkUtil.WifiNotEnabledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Ekran startowy aplikacji
 * @author Michał Kołodziejski
 */
public class StartActivity extends Activity {
    
    private MultitalkApplication app;
    private ProgressDialog loggingInProgressDialog = null;
    private EditText loginEditText;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        app = (MultitalkApplication) getApplication();
        
        setContentView(R.layout.start_activity);
        
        loginEditText = (EditText) findViewById(R.id.start_loginInput);
        
        // debug
        printDebugInfo();
        
        // akcje
        Button loginButton = (Button) findViewById(R.id.start_loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkWifiConnection()){
                    return;
                }
                
                String login = loginEditText.getText().toString();
                
                if(login == null || "".equals(login)){
                    return;
                }
                
                loggingInProgressDialog = ProgressDialog.show(StartActivity.this, "", 
                        "Logging in. Please wait...", true, true);
                
                // TODO logowanie
                MultitalkNetworkManager multitalkNetworkManager = app.getMultitalkNetworkManager();
                multitalkNetworkManager.logIn(login);
            }
        });
    }
    
    
    /**
     * Zamyka dialog oczekiwania na zalogowanie
     */
    public void hideLoginProgressDialog(){
        if(loggingInProgressDialog != null && loggingInProgressDialog.isShowing()){
            loggingInProgressDialog.dismiss();
        }
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
    
    
    /**
     * Sprawdza dostępność wifi i połączenia
     * @return
     */
    private boolean checkWifiConnection(){
        printDebugInfo();
        
        // włączone wifi
        if(!NetworkUtil.isWifiEnabled(StartActivity.this)){
            showErrorDialogWifiNotEnabled();
            return false;
        }
        
        // brak połączenia z siecią
        try {
            if(NetworkUtil.getIPaddressAsInt(StartActivity.this) == 0){
                showErrorDialogNoConnectionToNetwork();
                return false;
            }
        } catch (WifiNotEnabledException e) {
            showErrorDialogWifiNotEnabled();
            return false;
        }
        
        return true;
    }
    

    /**
     * Wyświetla okno dialogowe z błędem dostępności wifi
     */
    private void showErrorDialogWifiNotEnabled(){
        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
        builder.setMessage("Wifi is not enabled. Please enable it and connect to a network.")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true).setTitle("Error occured").create().show();
    }
    
    
    /**
     * Wyświetla okno dialogowe z błędem połączenia
     */
    private void showErrorDialogNoConnectionToNetwork(){
        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
        builder.setMessage("You are not connected to any network. Please connect to a network.")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true).setTitle("Error occured").create().show();
    }
}