package pl.multitalk.android.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

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
     * Zwraca adres IP połączenia po Wifi jako InetAddress
     * @param context kontekst wywołania
     * @return adres IP jako InetAddress
     * @throws WifiNotEnabledException wifi nie zostało włączone
     * @throws UnknownHostException 
     */
    public static InetAddress getIPaddressAsInetAddress(Context context) throws WifiNotEnabledException, UnknownHostException{
        int ipAddress = getIPaddressAsInt(context);

        byte[] ipBytes = new byte[4];
        ipBytes[0] = (byte) (ipAddress & 0x000000ff);
        ipBytes[1] = (byte) ((ipAddress >> 8) & 0x000000ff);
        ipBytes[2] = (byte) ((ipAddress >> 16) & 0x000000ff);
        ipBytes[3] = (byte) ((ipAddress >> 24) & 0x000000ff);
        
        InetAddress inetAddress = InetAddress.getByAddress(ipBytes);
        
        return inetAddress;
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
     * Zwraca adres broadcastu
     * @param context kontekst wywołania
     * @return adres broadcastu
     * @throws WifiNotEnabledException 
     * @throws NotConnectedToNetworkException 
     * @throws UnknownHostException
     */
    public static InetAddress getBroadcastInetAddress(Context context) throws UnknownHostException, NotConnectedToNetworkException, WifiNotEnabledException{
        if(!isWifiEnabled(context)){
            throw new WifiNotEnabledException();
        }
        
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifiManager.getDhcpInfo();
        
        if(dhcp == null || dhcp.ipAddress == 0){
            throw new NotConnectedToNetworkException();
        }
        
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] ipBytes = new byte[4];
        ipBytes[0] = (byte) (broadcast & 0x000000ff);
        ipBytes[1] = (byte) ((broadcast >> 8) & 0x000000ff);
        ipBytes[2] = (byte) ((broadcast >> 16) & 0x000000ff);
        ipBytes[3] = (byte) ((broadcast >> 24) & 0x000000ff);
        
        Log.d(Constants.DEBUG_TAG,"Broadcast IP: "+(int)(broadcast & 0x000000ff)
                                            +"."+(int)((broadcast >> 8) & 0x000000ff)
                                            +"."+(int)((broadcast >> 16) & 0x000000ff)
                                            +"."+(int)((broadcast >> 24) & 0x000000ff));
        
        return InetAddress.getByAddress(ipBytes);

    }
    
    
    /**
     * Zwraca adres IP w postaci InetAddress na podstawie Stringa
     * @param addr String
     * @return adres IP w postaci InetAddress
     * @throws UnknownHostException 
     */
    public static InetAddress getInetAddressFromString(String addr) throws UnknownHostException{
        byte[] ipBytes = new byte[4];
        ipBytes[0] = (byte) Integer.valueOf(addr.substring(0, addr.indexOf("."))).intValue();
        addr = addr.substring(addr.indexOf(".") + 1);
        ipBytes[1] = (byte) Integer.valueOf(addr.substring(0, addr.indexOf("."))).intValue();
        addr = addr.substring(addr.indexOf(".") + 1);
        ipBytes[2] = (byte) Integer.valueOf(addr.substring(0, addr.indexOf("."))).intValue();
        addr = addr.substring(addr.indexOf(".") + 1);
        ipBytes[3] = (byte) Integer.valueOf(addr).intValue();
        
        return InetAddress.getByAddress(ipBytes);
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
    
    
    /**
     * Wyjątek oznaczający, że Wifi nie zostało podłączone do żadnej z sieci
     */
    public static class NotConnectedToNetworkException extends Exception {
        /**
         * serial
         */
        private static final long serialVersionUID = 3364172649613944758L;
        
    }
}
