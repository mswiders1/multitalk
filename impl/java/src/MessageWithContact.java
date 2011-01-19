/**
 * For storing messages to and from others
 * 
 *
 */
public class MessageWithContact {
	
	private Contact contact;
	private Message message;
	
	public Contact getContact() {
		return contact;
	}
	public void setContact(Contact contact) {
		this.contact = contact;
	}
	public Message getMessage() {
		return message;
	}
	public void setMessage(Message message) {
		this.message = message;
	}
	
	public MessageWithContact()
	{
		contact = null;
		message = null;
	}
	public MessageWithContact(Contact c, Message m)
	{
		contact = c;
		message = m;
	}
	
}
