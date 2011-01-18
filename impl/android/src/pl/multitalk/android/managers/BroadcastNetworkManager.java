package pl.multitalk.android.managers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import pl.multitalk.android.managers.misc.BroadcastReceiver;
import pl.multitalk.android.util.Constants;
import pl.multitalk.android.util.NetworkUtil;

import android.content.Context;
import android.util.Log;

/**
 * Zarządca połączeń po broadcast-cie
 * @author Michał Kołodziejski
 */
public class BroadcastNetworkManager {
    
    private Context context;
    private MultitalkNetworkManager multitalkNetworkManager;
    private BroadcastReceiver broadcastReceiver;
    private DatagramSocket socket;
    private Timer discoveryPacketSendTimer;
    
    
    /**
     * Tworzy managera połączeń broadcast-owych
     * @param context kontekst wykonania
     */
    public BroadcastNetworkManager(Context context, MultitalkNetworkManager multitalkNetworkManager) {
        this.context = context;
        this.multitalkNetworkManager = multitalkNetworkManager;
    }
    
    
    /**
     * Tworzy socket UDP
     * @throws SocketException
     */
    private void createUDPsocket() throws SocketException{
        if(socket == null || socket.isClosed()){
            socket = new DatagramSocket(Constants.UDP_PORT);
            socket.setBroadcast(true);
        }
    }
    

    /**
     * Wysyła serię pakietów UDP w celu wykrycia inych podłączonych hostów
     */
    public void sendDiscoveryPackets(){
        discoveryPacketSendTimer = new Timer();
        discoveryPacketSendTimer.schedule(new DiscoveryPacketSendTask(), 0, 1000);
    }
    
    
    /**
     * Wysyła pakiet UDP w celu wykrycia inych podłączonych hostów
     */
    private void sendUDPHostsDiscoveryPacket(){
        InetAddress broadcastAddress;
        try {
            broadcastAddress = NetworkUtil.getInetAddressFromString("255.255.255.255");
            
        } catch (UnknownHostException e) {
            Log.e(Constants.ERROR_TAG, "UnknownHostException at BroadcastNetworkManager#discoverOtherHosts()");
            return;
            
        }
        
        try {
            createUDPsocket();
            
            DatagramPacket packet = new DatagramPacket(Constants.DISCOVERY_PACKET_DATA.getBytes(),
                    Constants.DISCOVERY_PACKET_DATA.length(), broadcastAddress, Constants.UDP_PORT);
            socket.send(packet);
            
        } catch (SocketException e){
            Log.e(Constants.ERROR_TAG, "SocketException at BroadcastNetworkManager#discoverOtherHosts()"
                    +"\nCause msg: "+e.getMessage());
            return;
            
        } catch (IOException e){
            Log.e(Constants.ERROR_TAG, "IOException at BroadcastNetworkManager#discoverOtherHosts()"
                    +"\nCause msg: "+e.getMessage());
            return;
            
        }
        
        Log.d(Constants.DEBUG_TAG, "Broadcast packet sent!");
    }
    
    
    /**
     * Rozpoczyna nasłuchiwać na broadcast-cie
     */
    public void startBroadcastListening(){
        try {
            createUDPsocket();
        } catch (SocketException e) {
            Log.e(Constants.ERROR_TAG, "SocketException at BroadcastNetworkManager#startBroadcastListening()"
                +"\nCause msg: "+e.getMessage());
            return;
        }
        
        broadcastReceiver = new BroadcastReceiver(context, socket, this);
        broadcastReceiver.start();
        
        Log.d(Constants.DEBUG_TAG, "Started thread for broadcast listening");
    }
    
    
    /**
     * Kończy nasłuchiwać po broadcast-cie
     */
    public void stopBroadcastListening(){
        Log.d(Constants.DEBUG_TAG, "stopping broadcast listening...");
        if(socket != null && socket.isBound()){
            socket.close();
        }
    }
    
    
    /**
     * Zwraca MultitalkNetworkManager
     * @return MultitalkNetworkManager
     */
    public MultitalkNetworkManager getMultitalkNetworkManager(){
        return multitalkNetworkManager;
    }
    
    
    
    /**
     * Zadanie wysyłania pakietów broadcast
     */
    class DiscoveryPacketSendTask extends TimerTask {

        int count;
        
        public DiscoveryPacketSendTask() {
            count = 0;
        }
        
        @Override
        public void run() {
            ++count;
            if(count <= 3){
                BroadcastNetworkManager.this.sendUDPHostsDiscoveryPacket();
                
            } else {
                BroadcastNetworkManager.this.discoveryPacketSendTimer.cancel();
                
            }
        }
        
    }
}
