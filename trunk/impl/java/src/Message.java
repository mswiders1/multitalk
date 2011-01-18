import java.util.*;

public class Message {
	
	protected String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public Message()
	{ }
	
	public Message(String type)
	{
		this.type = type;
	}
}

class BroadcastMsg
{
	private String content;
	private String header;
	private long length;
	
	public String getContent() {
		return content;
	}
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
	
	public void setContent(String content) {
		this.content = content;
	}

	public BroadcastMsg()
	{
		this.setContent("MULTITALK_5387132");
	}
	
}

class P2pMsg extends Message
{
	public P2pMsg()
	{	super();
		this.setType("P2P");
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

class HiiMsg extends Message
{
	private String uid;
	private String username;
	private Vector<UserVector> Vector;
	
	
	public String getType() {
		return type;
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
		super();
		this.type = "HII";
		this.uid = uid;
		this.username = username;
		Vector = vector;
		
	}
	
	public HiiMsg()
	{	
		super();
		this.type = "HII";
		Vector = new Vector<UserVector>();
	}	
}

class LogMsg extends Message
{
	
	private String uid;
	private String ip_address;
	private String usename;
	
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
	public String getIp_address() {
		return ip_address;
	}
	public void setIp_address(String ipAddress) {
		ip_address = ipAddress;
	}
	
	public LogMsg(String uid, String usename, String ip) {
		super();
		this.type = "LOG";
		this.uid = uid;
		this.usename = usename;
		this.ip_address = ip;
	}
	public LogMsg()
	{	super();
		this.type = "LOG";
	}
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

class MtxMsg extends Message
{
	private Vector<Vector<Knowlage>> mac;
	private Vector<UserId> vector;
	
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
		super();
		this.type = "MTX";
		this.mac = mac;
		this.vector = vector;
	}
	
	
	
	public MtxMsg()
	{	
		super();
		this.type = "MTX";
		this.mac = new Vector<Vector<Knowlage>>();
		this.vector = new Vector<UserId>();
	}
	
}

class MsgMsg extends Message
{
	
	private String sender;
	private String receiver;
	private long msg_id;
	private Vector<Knowlage> time_vec;
	private Vector<UserId> vec;
	private String content;
	
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
	public Vector<Knowlage> getTime_vec() {
		return time_vec;
	}
	public void setTime_vec(Vector<Knowlage> time_vec) {
		this.time_vec = time_vec;
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
			Vector<Knowlage> time_vec, Vector<UserId> vec, String content) {
		super();
		this.type = "MSG";
		this.sender = sender;
		this.receiver = receiver;
		this.msg_id = msgId;
		this.time_vec = time_vec;
		this.vec = vec;
		this.content = content;
	}
	
	public MsgMsg()
	{
		super();
		this.type = "MSG";
		this.vec = new Vector<UserId>();
		this.time_vec = new Vector<Knowlage>();
	}
	
	
}

class LivMsg extends Message
{
	private String uid;
	private String ip_address;
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getIp_address() {
		return ip_address;
	}
	public void setIp_address(String ipAddress) {
		ip_address = ipAddress;
	}
	
	public LivMsg(String uid, String ipAddress) {
		super();
		this.type ="LIV";
		this.uid = uid;
		ip_address = ipAddress;
	}
	
	public LivMsg()
	{
		super();
		this.type ="LIV";		
	}
	
	
}

class GetMsg extends Message
{
	private String uid;
	private long msg_id;
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public long getMsg_id() {
		return msg_id;
	}
	public void setMsg_id(long msgId) {
		msg_id = msgId;
	}
	public GetMsg(String uid, long msgId) {
		super();
		this.type = "LIV";
		this.uid = uid;
		msg_id = msgId;
	}
	public GetMsg()
	{
		super();
		this.type= "OUT";
	}
	
}
class OutMsg extends Message
{
	private String uid;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public OutMsg(String uid) {
		super();
		this.type = "OUT";
		this.uid = uid;
	}
	
	public OutMsg()
	{
		super();
		this.type = "OUT";
	}
	
}


