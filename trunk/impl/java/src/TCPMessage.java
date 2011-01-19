
public class TCPMessage {
	
	Constant c;
	Message msg;
	
	TCPMessage()
	{
		c = new Constant();
	}
	
	TCPMessage(Message m)
	{
		c = new Constant();
		this.msg = m;
	}
	
	public String create()
	{
	Serializer ser = new Serializer();
	String msg_string = ser.pack(msg);
	String content_string = new String();
	content_string+=c.getBegin();
	content_string+=msg_string.length();
	content_string+="\n";
	content_string+=msg_string;
	return content_string;
	}

	public Message retrieve(String content_string)
	{
		Message m= new Message();
		//dokonczyc
		return m;
	}

}
