import java.net.Socket;


public class Connection {

	private Contact contact;
	private Unicast unicast;
	
	public Connection (Socket socket, NetManagement net_management)
	{
		contact = new Contact();
		unicast = new Unicast(socket, net_management, this);
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public Unicast getUnicast() {
		return unicast;
	}

	public void setUnicast(Unicast unicast) {
		this.unicast = unicast;
	}
	
	
}
