package pl.multitalk.android.managers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import pl.multitalk.android.util.Constants;
import pl.multitalk.android.util.NetworkUtil;
import pl.multitalk.android.util.NetworkUtil.NotConnectedToNetworkException;
import pl.multitalk.android.util.NetworkUtil.WifiNotEnabledException;

import android.content.Context;
import android.util.Log;

/**
 * Zarządca połączeń po broadcast-cie
 * @author Michał Kołodziejski
 */
public class BroadcastNetworkManager {

    private static final int BROADCAST_PORT = 3554;
    
    private Context context;
    
    /**
     * Tworzy managera połączeń broadcast-owych
     * @param context kontekst wykonania
     */
    public BroadcastNetworkManager(Context context) {
        this.context = context;
        
    }
    
    
    public void sendUDPHostsDiscoveryPacket(){
        InetAddress broadcastAddress;
        try {
            broadcastAddress = NetworkUtil.getBroadcastInetAddress(context);
            
        } catch (UnknownHostException e) {
            Log.e(Constants.ERROR_TAG, "UnknownHostException at BroadcastNetworkManager#discoverOtherHosts()");
            return;
            
        } catch (NotConnectedToNetworkException e) {
            Log.e(Constants.ERROR_TAG, "NotConnectedToNetworkException at BroadcastNetworkManager#discoverOtherHosts()");
            return;
            
        } catch (WifiNotEnabledException e) {
            Log.e(Constants.ERROR_TAG, "WifiNotEnabledException at BroadcastNetworkManager#discoverOtherHosts()");
            return;
            
        }
        
        try {
            DatagramSocket socket = new DatagramSocket(BROADCAST_PORT);
            socket.setBroadcast(true);
            
            // FIXME tymczasowo
//            BroadcastReceiver receiver = new BroadcastReceiver(socket);
//            Thread receiverTh = new Thread(receiver);
//            receiverTh.start();
            
            String data = "MULTITALK_5387132";
            DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),
                broadcastAddress, BROADCAST_PORT);
            socket.send(packet);
            
            socket.close();
            
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
     * Nasłuchiwacz
     */
    class BroadcastReceiver implements Runnable {
        
        private DatagramSocket socket;
        
        public BroadcastReceiver(DatagramSocket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            
            try {
                Log.d(Constants.DEBUG_TAG, "Waiting for broadcast packet...");
                socket.receive(packet);
                
            } catch (IOException e) {
                Log.e(Constants.ERROR_TAG, "IOException at BroadcastReceiver#run()"
                        +"\nCause msg: "+e.getMessage());
                return;
            }
            
            String data = new String(packet.getData(), 0, packet.getLength());
            Log.d(Constants.DEBUG_TAG, "Received broadcast packet:"
                    +" from: "+packet.getAddress().getHostAddress()
                    +" data: "+data);
        }
    }
}
