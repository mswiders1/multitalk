package pl.multitalk.android;

import java.util.Timer;
import java.util.TimerTask;

import pl.multitalk.android.managers.MultitalkNetworkManager;
import pl.multitalk.android.model.MultitalkApplication;
import pl.multitalk.android.util.Constants;
import pl.multitalk.android.util.NetworkUtil;
import pl.multitalk.android.util.NetworkUtil.WifiNotEnabledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
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
    private Timer logCheckTimer;
    
    public static final String LOGGED_IN_FLAG = "LOGGED_IN";
    
    private Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            if(msg.getData().getBoolean(LOGGED_IN_FLAG) == true){
                // zalogowano
                logCheckTimer.cancel();
                loggingInProgressDialog.dismiss();
                Intent intent = new Intent(Constants.ACTION_CONTACT_LIST_ACTIVITY);
                startActivity(intent);
            }
        };
    };
    
    
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
                String login = loginEditText.getText().toString();
                
                if(!checkWifiConnection()){
                    return;
                }
                
                if(login == null || "".equals(login)){
                    return;
                }
                
                loggingInProgressDialog = ProgressDialog.show(StartActivity.this, "", 
                        getString(R.string.start_loginDialog), true, true);
                
                MultitalkNetworkManager multitalkNetworkManager = app.getMultitalkNetworkManager();
                multitalkNetworkManager.logIn(login);
                
                // start timera
                logCheckTimer = new Timer();
                logCheckTimer.schedule(new LogCheckTimerTask(), 2000, 1000);
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
    
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.getMultitalkNetworkManager().destroy();
        if(logCheckTimer != null){
            logCheckTimer.cancel();
        }
        Process.killProcess(Process.myPid());
    }
    
    
    @Override
    protected void onNewIntent(Intent intent) {
        // logout
        app.getMultitalkNetworkManager().destroy();
        finish();
        Process.killProcess(Process.myPid());
    }
    
    
    
    /**
     * Wpisuje do logów informacje na potrzeby debugu
     */
    private void printDebugInfo(){
        boolean wifiOn = NetworkUtil.isWifiEnabled(getBaseContext());
        Log.d(Constants.DEBUG_TAG, "Wifi enabled: " + wifiOn);
        
        try {
            String mac = NetworkUtil.getWifiMAC(getBaseContext());
            Log.d(Constants.DEBUG_TAG, "MAC address: " + mac);
            
            String ipAddress = NetworkUtil.getIPaddressAsString(getBaseContext());
            Log.d(Constants.DEBUG_TAG, "IP address: " + ipAddress);
        
        } catch (WifiNotEnabledException e) {
            Log.d(Constants.DEBUG_TAG, "WifiNotEnabledException occured");
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
        builder.setMessage(getString(R.string.start_wifiNotEnabledDialog))
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true).setTitle(getString(R.string.common_errorOccured)).create().show();
    }
    
    
    /**
     * Wyświetla okno dialogowe z błędem połączenia
     */
    private void showErrorDialogNoConnectionToNetwork(){
        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
        builder.setMessage(getString(R.string.start_noConnectionDialog))
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true).setTitle(getString(R.string.common_errorOccured)).create().show();
    }
    
    
    
    /**
     * Zadanie sprawdzenia czy zalogowano do sieci
     */
    class LogCheckTimerTask extends TimerTask {

        @Override
        public void run() {
            if(app.getMultitalkNetworkManager().isLoggedIn()){
                // zalogowano
                Message msg = handler.obtainMessage();
                Bundle b = new Bundle();
                b.putBoolean(LOGGED_IN_FLAG, true);
                msg.setData(b);
                handler.sendMessage(msg);
            }
        }
        
    }
}