package pl.multitalk.android.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Klasa użytkowa do operacji sieciowych
 * @author Michał Kołodziejski
 */
public class NetworkUtil {

    /**
     * Zwraca adres MAC karty sieciowej
     * @param context kontekst wywołania
     * @return adres MAC karty sieciowej
     * @throws WifiNotEnabledException wifi nie zostało włączone
     */
    public static String getWifiMAC(Context context) throws WifiNotEnabledException{
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        
        // sprawdzenie stanu wifi
        if(!wifiManager.isWifiEnabled()){
            throw new WifiNotEnabledException();
        }
        
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getMacAddress();
    }
    
    
    /**
     * Zwraca adres IP połączenia po Wifi jako int
     * @param context kontekst wywołania
     * @return adres IP jako int
     * @throws WifiNotEnabledException wifi nie zostało włączone
     */
    public static int getIPaddressAsInt(Context context) throws WifiNotEnabledException{
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        
        // sprawdzenie stanu wifi
        if(!wifiManager.isWifiEnabled()){
            throw new WifiNotEnabledException();
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getIpAddress();
    }
    
    
    /**
     * Zwraca adres IP połączenia po Wifi w postaci xxx.xxx.xxx.xxx
     * @param context kontekst wywołania
     * @return adres IP w postaci xxx.xxx.xxx.xxx
     * @throws WifiNotEnabledException wifi nie zostało włączone
     */
    public static String getIPaddressAsString(Context context) throws WifiNotEnabledException{
        int ipAddress = getIPaddressAsInt(context);
        
        int[] ipAddressParts = new int[4];
        ipAddressParts[0] = (ipAddress & 0x000000ff);
        ipAddressParts[1] = (ipAddress >> 8) & 0x000000ff;
        ipAddressParts[2] = (ipAddress >> 16) & 0x000000ff;
        ipAddressParts[3] = (ipAddress >> 24) & 0x000000ff;
        
        StringBuffer sb = new StringBuffer();
        sb.append(ipAddressParts[0]);
        sb.append(".");
        sb.append(ipAddressParts[1]);
        sb.append(".");
        sb.append(ipAddressParts[2]);
        sb.append(".");
        sb.append(ipAddressParts[3]);
        
        return sb.toString();
    }
    
    
    /**
     * Sprawdza stan Wifi
     * @param context kontekst wywołania
     * @return true jeżeli Wifi jest włączone, false w przeciwnym przypadku
     */
    public static boolean isWifiEnabled(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }
    
    
    
    /**
     * Wyjątek oznaczający, że Wifi nie zostało włączone
     */
    public static class WifiNotEnabledException extends Exception {
        /**
         * serial
         */
        private static final long serialVersionUID = 6761874627585793970L;
        
    }
}
