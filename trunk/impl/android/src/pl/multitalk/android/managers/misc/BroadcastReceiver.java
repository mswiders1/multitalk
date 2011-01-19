package pl.multitalk.android.managers.misc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.managers.BroadcastNetworkManager;
import pl.multitalk.android.managers.messages.internal.DiscoveryPacketReceivedMessage;
import pl.multitalk.android.util.Constants;
import pl.multitalk.android.util.NetworkUtil;
import pl.multitalk.android.util.NetworkUtil.WifiNotEnabledException;
import android.content.Context;
import android.util.Log;

/**
 * Odbiorca pakietów UDP
 * @author Michał Kołodziejski
 */
public class BroadcastReceiver extends Thread {
    
    private Context context;
    private DatagramSocket socket;
    private BroadcastNetworkManager networkManager;
    
    /**
     * Tworzy odbiorcę pakietów UDP
     * @param socket
     */
    public BroadcastReceiver(Context context, DatagramSocket socket, BroadcastNetworkManager networkManager) {
        this.context = context;
        this.socket = socket;
        this.networkManager = networkManager;
    }
    
    
    @Override
    public void run() {
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        String selfAddress;
        try {
            selfAddress = NetworkUtil.getIPaddressAsString(context);
        } catch (WifiNotEnabledException e) {
            Log.e(Constants.ERROR_TAG, "WifiNotEnabledException at BroadcastReceiver");
            return;
        }

        Log.d(Constants.DEBUG_TAG, "Started listening for broadcast packets...");
        
        try {
            while(true){
                socket.receive(packet);
                
                if(packet.getAddress().getHostAddress().equals(selfAddress)){
                    // pakiet od samego siebie - pomijamy
                    continue;
                }

                String data = new String(packet.getData(), 0, packet.getLength());
                Log.d(Constants.DEBUG_TAG, "Received broadcast packet:"
                        +" from: "+packet.getAddress().getHostAddress()
                        +" data: "+data);
                
                if(!Constants.DISCOVERY_PACKET_DATA.equals(data)){
                    // nieznany pakiet - dropujemy
                    continue;
                }
                
                UserInfo newUser = new UserInfo();
                newUser.setIpAddress(packet.getAddress().getHostAddress());
                // fake UID - nie wiemy jak się nazywa, ale musimy go rozpoznawać...
                newUser.setUid(String.valueOf(System.currentTimeMillis()+"."+Math.random()));
                newUser.setUsername(String.valueOf(System.currentTimeMillis()+"."+Math.random()));
                
                // podajemy dalej wiadomość
                DiscoveryPacketReceivedMessage message = new DiscoveryPacketReceivedMessage();
                message.setSenderInfo(newUser);
                networkManager.getMultitalkNetworkManager().putMessage(message);
                
            }
            
        } catch (IOException e) {
            Log.e(Constants.ERROR_TAG, "IOException at BroadcastReceiver#run()"
                    +"\nCause msg: "+e.getMessage());
            return;
        }
        
    }
}