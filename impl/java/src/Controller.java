import java.util.*;

public class Controller {

	private GUI gui;
	private NetManagement net_management;
	private Contact me;
	private Contact neighbour;
	private Collection<Contact> contacts_for_gui;
	private Timer LogInTimer;
	
	public Controller()
	{
		MyId my_id = new MyId();
		my_id.findId();
		contacts_for_gui = new Vector<Contact>();
		contacts_for_gui.add(new Contact("12345","kontakt",true,"192.168.1.1"));
		me = new Contact(my_id.findId(),"krzysiek",true, my_id.findIp(my_id.getInaddr()));
		neighbour = new Contact(my_id.getId(),"neighbour",true, my_id.findIp(my_id.getInaddr()));
		
		net_management = new NetManagement(this);
		gui = new GUI(this);
		
	}
	
	synchronized public void addToConnections(Vector<UserVector> uvec )
	{		
		Iterator<Connection> it = net_management.getConnections().iterator();
		Iterator<UserVector> ituv;
		Connection connection;
		Contact contact;
		UserVector uv;		
		boolean add;
		while (it.hasNext())
		{
			add = true;
			connection = it.next();
			contact = connection.getContact();
			ituv = uvec.iterator();
			while (ituv.hasNext())
			{
				uv = ituv.next();
				if(uv.getIp_address() == contact.getId() || uv.getUid() == contact.getId())
				{
					add = false;
				}
				if (add)
				{
					net_management.connectToClient(new Contact(uv.getUid(),uv.getUsername(),true,uv.getIp_address()));						
				}
			}
			
		}
	}
	
	synchronized public void messageReceived(Contact contact, Message message)
	{
		System.out.println("przyszla wiadomosc do kontorlera:"+ message.getType());
		
		String type = message.getType().toUpperCase();
		if(type == "P2P")
		{
			//net_management.connectToClient(contact);
			messageSendHii(contact);
		}
		else if(type == "HII")
		{
			System.out.println("dostal Hiii msg ");
			
			Vector<UserVector> uv = ((HiiMsg)message).getVector();
			addToConnections(uv);
			messageSendLog();
		}
		return;
	}
	
	public void messageSendP2P()
	{
		Message p2pmsg = new P2pMsg();	
		net_management.add_all(p2pmsg);
	}
	
	public void messageSendHii(Contact c_)
	{
		System.out.print("wysyla Hii");
		HiiMsg hiimsg = new HiiMsg();
		hiimsg.setUid(me.getId());
		hiimsg.setUsername(me.getName());
		Vector<UserVector> uvec = new Vector<UserVector>();
		Iterator<Connection> it = net_management.getConnections().iterator();
		Connection connection;
		Contact contact;		
		while (it.hasNext())
		{
			connection = it.next();
			contact = connection.getContact();
			uvec.add(new UserVector(contact.getIp(),contact.getId(), contact.getName()));
		}
		
		hiimsg.setVector(uvec);
		uvec.add(new UserVector(me.getIp(),me.getId(), me.getName()));
		net_management.add_send_not_yet_logged_in(c_, hiimsg);
		System.out.println("Przeslal Hii");
	}
	
	public void messageSendLog()
	{
		System.out.println("Wysyla wiadomosc log z kontrollera");
		LogMsg logmsg = new LogMsg();
		logmsg.setUid(me.getId());
		logmsg.setUsename(me.getName());
		logmsg.setIp_address(me.getIp());
		System.out.println("Wysyla hi do:" + net_management.getConnections().size() );
		net_management.add_all(logmsg);
	}
	
	public void LogIn()
	{
		// first send broadcast
		Broadcast broadcast = new Broadcast(net_management);
		broadcast.send();
		broadcast.receive();
		//wait for anwsers and send p2p msg to all who answered
		//LogInTimer = new Timer();
		//System.out.println("Bedzie wysylal HII");
		//LogInTimer.schedule(new LogInTTask() , 0, 5000);
		
	}
	
	class LogInTTask extends TimerTask
	{
		public void run()
		{
			//Iterator<Connection> it = Controller.this.getNet_management().getConnections().iterator();
			//Connection connection;
			//while (it.hasNext())
			//Controller.this.messageSendHii();
			System.out.println("Wysyla HII");
			Controller.this.LogInTimer.cancel();
		}
	}
	
	
	////////////////////////////////////////////////////////////////
	/////// Commucication with GUI ////////////////////////////////
	///////////////////////////////////////////////////////////////
	
	public void messageFromGUI(Collection<Contact> from_list, String text)
	{
		Vector<Contact> from = new Vector<Contact>(from_list);
		System.out.println("wiadomosc z GUI: "+text);
		return;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public GUI getGui() {
		return gui;
	}

	public void setGui(GUI gui) {
		this.gui = gui;
	}

	public NetManagement getNet_management() {
		return net_management;
	}

	public void setNet_management(NetManagement netManagement) {
		net_management = netManagement;
	}

	public Contact getMe() {
		return me;
	}

	public void setMe(Contact me) {
		this.me = me;
	}

	public Contact getNeighbour() {
		return neighbour;
	}

	public void setNeighbour(Contact neighbour) {
		this.neighbour = neighbour;
	}

	public Collection<Contact> getContacts_for_gui() {
		return contacts_for_gui;
	}

	public void setContacts_for_gui(Collection<Contact> contactsForGui) {
		contacts_for_gui = contactsForGui;
	}


	
	
}
