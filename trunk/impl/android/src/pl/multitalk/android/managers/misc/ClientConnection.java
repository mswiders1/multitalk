package pl.multitalk.android.managers.misc;

import java.net.Socket;

/**
 * Reprezentacja połączenia z klientem.
 * @author Michał Kołodziejski
 */
public class ClientConnection {

    private Socket clientSocket;
    private ClientTCPSender tcpSender;
    private ClientTCPReceiver tcpReceiver;
    
}
