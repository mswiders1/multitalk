import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.net.Socket;


public class Broadcast {
	
	private Constant c;
	private DatagramSocket socket;
	private DatagramPacket broadcast_packet;
	private int broadcast_count = 0;
	private Timer t;
	private BroadcastListener broadcast_listener;
	private NetManagement net_management;
	
	public Broadcast(NetManagement net_management)
	{
		c = new Constant();
		this.net_management = net_management;
		broadcast_listener = new BroadcastListener();
		try
		{
			if(socket == null || socket.isClosed())
			{
				socket = new DatagramSocket(c.getPort());
			}
			
		socket.setBroadcast(true);
		
		// dodawanie siebie do listy Connectionow:
		/*
		MyId my_id = new MyId();
		my_id.findId();
		my_id.findIp(my_id.getInaddr());
		Contact contact = new Contact(my_id.findId(),my_id.findUserName(),true,my_id.findIp(my_id.getInaddr()));
		Connection connection = new Connection(new Socket(),this.net_management);
		connection.setContact(contact);
		this.net_management.getConnections().add(connection);*/
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void send()
	{
		InetAddress broadcast_addr;
		try
		{
			byte [] b = new byte[4];
			Integer i = new Integer(255);
			b[0] = i.byteValue();
			b[1] = i.byteValue();
			b[2] = i.byteValue();
			b[3] = i.byteValue();
			
			broadcast_addr = InetAddress.getByAddress(b);
			broadcast_packet = new DatagramPacket(c.getBroadcast_content().getBytes(),c.getBroadcast_content().length(),broadcast_addr,c.getPort());
			t = new Timer();
			
			if (broadcast_count < 3)
			{
				t.schedule(new SendTTask(), 0,1000);
			}
			else 
			{
				t.cancel();
			}
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void receive()
	{
		broadcast_listener.start();
	}
	
	public void closeSocket()
	{		
		socket.close();
	}
	
	class SendTTask extends TimerTask
	{
		public void run()
		{
			try{
				if (Broadcast.this.broadcast_count == 3)
				{
					t.cancel();
					System.out.println("Zatrzymuje timer");
				}
				else
				{
					Broadcast.this.socket.send(broadcast_packet);
					Broadcast.this.broadcast_count++;
					System.out.println("Posłano broadcast");
				}
				
			
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	class BroadcastListener extends Thread
	{
		
		String MyIp;
		byte []received;
		MyId my_id;
		
		DatagramPacket received_packet;
		
		public BroadcastListener()
		{
			try
			{
				received = new byte[1024];
				received_packet = new DatagramPacket(received, received.length);
				my_id = new MyId();
				my_id.findId();
				//System.out.println("MyId:"+my_id.findIp(my_id.getInaddr()));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		public void run()
		{
			while(true)
			{
				try{
					
					socket.receive(received_packet);
					if(received_packet.getAddress().equals(my_id.getInaddr()))
	                {   System.out.println("Dostalem pakiet od samego siebie");
						continue; }
					
					System.out.println("Watek BroadcastListenera");
					
					Broadcast.this.net_management.checkIfNewUser(received_packet.getAddress().getHostAddress());
					System.out.print("broadcast listener dostal pakiet");
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	    
		
	}
	

	
}
