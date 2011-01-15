package pl.multitalk.android.managers.misc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import pl.multitalk.android.datatypes.UserInfo;
import pl.multitalk.android.managers.BroadcastNetworkManager;
import pl.multitalk.android.managers.messages.internal.DiscoveryPacketReceivedMessage;
import pl.multitalk.android.util.Constants;
import android.util.Log;

/**
 * Odbiorca pakietów UDP
 * @author Michał Kołodziejski
 */
public class BroadcastReceiver extends Thread {
    
    private DatagramSocket socket;
    private BroadcastNetworkManager networkManager;
    
    /**
     * Tworzy odbiorcę pakietów UDP
     * @param socket
     */
    public BroadcastReceiver(DatagramSocket socket, BroadcastNetworkManager networkManager) {
        this.socket = socket;
        this.networkManager = networkManager;
    }
    
    
    @Override
    public void run() {
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        Log.d(Constants.DEBUG_TAG, "Started listening for broadcast packets...");
        
        try {
            while(true){
                socket.receive(packet);

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