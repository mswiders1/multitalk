
public class Contact {
	
	private long id;
	private String name;
	private String ip;
	boolean available;
	
	
	public Contact()
	{
		id = 0;
		name = "";
		available = false;
	}
	
	public Contact(long id, String name, boolean available)
	{
		this.id = id;
		this.name = name;
		this.available = available;
	}
	public Contact(long id, String name, boolean available, String ip)
	{
		this.id = id;
		this.name = name;
		this.available = available;
		this.ip = ip;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isAvailable() {
		return available;
	}
	public void setAvailable(boolean available) {
		this.available = available;
	}
	
		
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String toString() {
		return name+ " ";
	}
}
