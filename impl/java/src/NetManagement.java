import java.util.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class NetManagement {

	
	private Vector<Connection> connections;
	private Constant constant;
	
	private Vector<MessageWithContact> received;
	private Vector<MessageWithContact> send;
	
	public NetManagement()
	{
		Broadcast broadcast = new Broadcast(this);
		broadcast.send();
		broadcast.receive();
	
		this.connections = new Vector<Connection>();
		this.constant = new Constant();
		received = new Vector<MessageWithContact> ();
		send = new Vector<MessageWithContact> ();
		
		this.startListiningForConnections();
	}
	
	/**
	 * Executed by Unicast Msg Listener. each time new message comes 
	 * 
	 */
	public void add_received(Contact c, Message m)
	{
		received.add( new MessageWithContact(c,m));
		// przekazanie do kontrolera dopisac;
	}
	/**
	*Executed by controller. Each time user sends new message
	*
	*/
	public void add_send(Contact c_, Message m_)
	{
		Iterator<Connection> it = connections.iterator();
		Contact c;
		Connection connection;
		while(it.hasNext())
		{
			connection = it.next();
			c = connection.getContact();
			if (c.getIp()== c_.getIp() || c.getId() == c_.getId())
			{
				connection.getUnicast().putMessage(m_);
				this.send.add(new MessageWithContact(c,m_));
				break;
			}
		}
	}
	
	public void checkIfNewUser(String ip)
	{
		Contact contact = new Contact();
		contact.setIp(ip);
		connectToClient(contact);
	}
	
	
	
	
	/**
	 * Zamienia ip w postaci AAA.BBB.CCC.DDD na tablice 4 el. bajtow
	 * @param string_ip
	 * @return
	 */
	public byte[] convertStringIp2bytes(String string_ip)
	{
		InetAddress ia;
		byte []b = new byte[4];		
		try{
			b = InetAddress.getByName(string_ip).getAddress();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return b;
		
	}
	
	/**
	 * Uruchamia watek oczekinania na polaczenia przychodzace
	 */
	
	public void startListiningForConnections()
	{			
		try{
			ConnectionListener listener= new ConnectionListener();
			Thread listener_thread = new Thread(listener);
			listener_thread.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Sprawdza czy na liscie polaczen jest dany klient. 
	 * Jezeli nie to dodaje do niej polaczenie do tego klienta
	 * @param contact
	 */
	public void connectToClient(Contact contact)
	{
		Iterator<Connection> it = connections.iterator();
		Connection conn;
		String address = contact.getIp();
		String id = contact.getId();
		boolean exists = false;
		while (it.hasNext())
		{
			conn = it.next();
			if (address.equals(conn.getContact().getIp()))
			{
				exists = true;
				break;
			}
		}
		if (exists)
		{
			;// do nothing, use existing
		}
		else
		{
			
			Socket s = new Socket();
			try
			{
				InetAddress ia = InetAddress.getByAddress(convertStringIp2bytes(contact.getIp()));				
				s.connect(new InetSocketAddress(ia,Constant.getPort()));
				conn = new Connection(s,this);				
				//System.out.print("tworzy nowy connection"+ conn.getContact().getIp());
				connections.add(conn);
				
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			// dopisac wsylanie Hii msg do tego polaczenia
		}
	}
	
	/**
	 * Jeżeli wykryto polaczenie przychodzace sprawdza czy
	 * juz istnieje takie polaczenie na liscie.Jeżeli nie to
	 * dodaje nowe 
	 * @param s
	 */
	
	public void newConnection(Socket s)
	{
		String address = s.getInetAddress().getHostAddress();
		Iterator<Connection> it = connections.iterator();
		Connection conn;
		boolean exists = false;
		while (it.hasNext())
		{
			conn = it.next();
			if (address.equals(conn.getContact().getIp()))
			{
				exists = true;
				break;
			}
		}
		if (exists)
		{
			;// do nothing, use existing
		}
		else
		{
			conn = new Connection(s,this);
			connections.add(conn);
			// dopisac wsylanie Hii msg do tego polaczenia
		}
			
		
	}
	/*
	 * Nasluchuje polaczen przychodzacych;
	 */
	class ConnectionListener implements Runnable
	{
		ServerSocket ss;
		
		public ConnectionListener ()
		{
				;
		}

		public void run()
		{
			try
			{
				ss = new ServerSocket(constant.getPort());
				while(true)
				{
					Socket socket = ss.accept();
					NetManagement.this.newConnection(socket);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return;
			}
		}
	}
	public Vector<Connection> getConnections() {
		return connections;
	}

	public void setConnections(Vector<Connection> connections) {
		this.connections = connections;
	}

	public Constant getConstant() {
		return constant;
	}

	public void setConstant(Constant constant) {
		this.constant = constant;
	}

	public Vector<MessageWithContact> getReceived() {
		return received;
	}

	public void setReceived(Vector<MessageWithContact> received) {
		this.received = received;
	}

	public Vector<MessageWithContact> getSend() {
		return send;
	}

	public void setSend(Vector<MessageWithContact> send) {
		this.send = send;
	}
	

}
