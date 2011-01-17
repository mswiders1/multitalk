import java.util.*;

public class Message {
	private String header;
	private long length;
	private String MessageJson;
	
	
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	public long getLength() {
		return length;
	}
	public void setLength(long length) {
		this.length = length;
	}
	public String getMessageJson() {
		return MessageJson;
	}
	public void setMessageJson(String messageJson) {
		MessageJson = messageJson;
	}
	
	public Message()
	{
		this.setHeader("BEGIN_MESSAGE:");
	}
	public Message(long l ,String m)
	{
		this.setHeader("BEGIN_MESSAGE:");
		this.setLength(l);
		this.setMessageJson(m);
	}
}

class BroadcastMsg
{
	private String content;	
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}

	public BroadcastMsg()
	{
		this.setContent("MULTITALK_5387132");
	}
	
}

class P2pMsg
{
	private String content;
	private String type;

	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
		
	public P2pMsg()
	{
		this.setType("P2P");
		this.setContent("MULTITALK_5387132");
	}
}

class UserVector
{
	private String ip_address;
	private String uid;
	private String username;
	
	public String getIp_address() {
		return ip_address;
	}
	public void setIp_address(String ipAddress) {
		ip_address = ipAddress;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public UserVector(String ipAddress, String uid, String username) {
		super();
		ip_address = ipAddress;
		this.uid = uid;
		this.username = username;
	}	
	
	public UserVector()
	{
		;
	}
}

class HiiMsg
{
	private String type;
	private String uid;
	private String username;
	private Vector<UserVector> Vector;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Vector<UserVector> getVector() {
		return Vector;
	}
	public void setVector(Vector<UserVector> vector) {
		Vector = vector;
	}
	
	public HiiMsg(String uid, String username,
			Vector<UserVector> vector) {
		
		this.type = "HII";
		this.uid = uid;
		this.username = username;
		Vector = vector;
	}
	
	public HiiMsg()
	{
		this.type = "HII";
		Vector = new Vector<UserVector>();
	}	
}

class LogMsg
{
	private String type;
	private String uid;
	private String usename;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getUsename() {
		return usename;
	}
	public void setUsename(String usename) {
		this.usename = usename;
	}
	
	public LogMsg(String uid, String usename) {	
		this.type = "LOG";
		this.uid = uid;
		this.usename = usename;
	}
	public LogMsg(){this.type = "LOG";};
}

class UserId
{
	private long user_id;

	public long getUser_id() {
		return user_id;
	}

	public void setUser_id(long userId) {
		user_id = userId;
	}

	public UserId(long userId) {
		user_id = userId;
	}
	
	public UserId(){}
	
}

class Knowlage
{
	private long msg_no;

	public long getMsg_no() {
		return msg_no;
	}

	public void setMsg_no(long msgNo) {
		msg_no = msgNo;
	}

	public Knowlage(long msgNo) {		
		msg_no = msgNo;
	}
	
	public Knowlage	()
	{
		;
	}
	
}

class MtxMsg
{
	private String type;
	private Vector<Vector<Knowlage>> mac;
	private Vector<UserId> vector;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Vector<Vector<Knowlage>> getMac() {
		return mac;
	}
	public void setMac(Vector<Vector<Knowlage>> mac) {
		this.mac = mac;
	}
	public Vector<UserId> getVector() {
		return vector;
	}
	public void setVector(Vector<UserId> vector) {
		this.vector = vector;
	}
	
	public MtxMsg(Vector<Vector<Knowlage>> mac,
			Vector<UserId> vector) {
		
		this.type = "MTX";
		this.mac = mac;
		this.vector = vector;
	}
	
	public MtxMsg()
	{
		this.type = "MTX";
		this.mac = new Vector<Vector<Knowlage>>();
		this.vector = new Vector<UserId>();
	}
	
}

class MsgMsg
{
	private String type;
	private String sender;
	private String receiver;
	private long msg_id;
	private Vector<Vector<Knowlage>> mac;
	private Vector<UserId> vec;
	private String content;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	public long getMsg_id() {
		return msg_id;
	}
	public void setMsg_id(long msgId) {
		msg_id = msgId;
	}
	public Vector<Vector<Knowlage>> getMac() {
		return mac;
	}
	public void setMac(Vector<Vector<Knowlage>> mac) {
		this.mac = mac;
	}
	public Vector<UserId> getVec() {
		return vec;
	}
	public void setVec(Vector<UserId> vec) {
		this.vec = vec;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public MsgMsg(String sender, String receiver, long msgId,
			Vector<Vector<Knowlage>> mac, Vector<UserId> vec, String content) {
	
		this.type = "MSG";
		this.sender = sender;
		this.receiver = receiver;
		this.msg_id = msgId;
		this.mac = mac;
		this.vec = vec;
		this.content = content;
	}
	
	public MsgMsg()
	{
		this.type = "MSG";
		this.vec = new Vector<UserId>();
		this.mac = new Vector<Vector<Knowlage>>();
	}
	
	
}





