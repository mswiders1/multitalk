import java.lang.*;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;

import net.sf.json.JSONObject;
import net.sf.json.util.JSONTokener;
import net.sf.*;



import java.util.*;


public class Serializer {
	private JSONObject obj;
	private Message msg;
	
	String pack(Message msg)
	{
		this.msg = msg;
		try{
			String type = msg.getType();
			obj = new JSONObject();
			if (type.toUpperCase()=="P2P")
			{
				obj.put("TYPE", "P2P" );
			}
			else if (type.toUpperCase()=="HII")
			{
				JSONArray jarray = new JSONArray();
				obj.put("TYPE", "HII");
				obj.put("UID",((HiiMsg)msg).getUid());
				obj.put("USERNAME", ((HiiMsg)msg).getUsername());
				
				Iterator<UserVector>  it = ((HiiMsg)msg).getVector().iterator();
				UserVector uv;
				JSONObject uobj;
				while(it.hasNext())
				{
					
					uobj = new JSONObject();
					uv = it.next();
					uobj.put("IP_ADDRESS", uv.getIp_address());
					uobj.put("UID", uv.getUid());
					uobj.put("USERNAME", uv.getUsername());
					
					jarray.add(uobj);
				}
				obj.put("VECTOR", jarray);
				
			}
			else if (type.toUpperCase()=="LOG")
			{
				obj.put("TYPE", "LOG");
				obj.put("UID", ((LogMsg)msg).getUid());
				obj.put("USERNAME", ((LogMsg)msg).getUsename());
				obj.put("IP_ADDRESS", ((LogMsg)msg).getIp_address());
			}
			
			else if (type.toUpperCase()=="MTX")
			{
				obj.put("TYPE", "MTX");
				JSONArray jmatrix, jrow, jvec;
				Vector<Knowlage> vk;
				Iterator<Vector<Knowlage>> it = ((MtxMsg)msg).getMac().iterator();
				Iterator<Knowlage> itk;
				Knowlage k;
				jmatrix = new JSONArray();
				while (it.hasNext())
				{
					jrow = new JSONArray();
					vk = it.next();
					itk = vk.iterator();
					while (itk.hasNext())
					{
						k = itk.next();
						jrow.add(k.getMsg_no());
					}				
					jmatrix.add(jrow);
				}
				obj.put("MAC", jmatrix);
					
				jvec = new JSONArray();
				Iterator<UserId> ituid = ((MtxMsg)msg).getVector().iterator();
				UserId uid;
				while(ituid.hasNext())
				{
					uid = ituid.next();
					jvec.add(uid.getUser_id());
				}
						
				obj.put("VEC", jvec);
			}
			
			else if (type.toUpperCase()=="MSG")
			{
				obj.put("TYPE", "MSG");
				obj.put("SENDER", ((MsgMsg)msg).getSender());
				obj.put("RECEIVER", ((MsgMsg)msg).getReceiver());
				obj.put("MSG_ID", ((MsgMsg)msg).getMsg_id());
				Iterator<Knowlage> itk = ((MsgMsg)msg).getTime_vec().iterator();
				Iterator<UserId> itu = ((MsgMsg)msg).getVec().iterator();
				Knowlage k;
				UserId uid;
				JSONArray jarray_time = new JSONArray();
				JSONArray jarray_user = new JSONArray();
				while(itk.hasNext())
				{
					k = itk.next();
					jarray_time.add(k.getMsg_no());
				}
				while(itu.hasNext())
				{
					uid = itu.next();
					jarray_user.add(uid.getUser_id());
				}
				obj.put("TIME_VEC", jarray_time);
				obj.put("VEC", jarray_user);
				obj.put("CONTENT", ((MsgMsg)msg).getContent());			
			}
			
			else if (type.toUpperCase()=="LIV")
			{
				obj.put("TYPE", "LIV");
				obj.put("UID", ((LivMsg)msg).getUid());
				obj.put("IP_ADDRESS", ((LivMsg)msg).getIp_address());
			}
			else if(type.toUpperCase() == "GET")
			{
				obj.put("TYPE", "GET");
				obj.put("UID", ((GetMsg)msg).getUid());
				obj.put("MSG_ID", ((GetMsg)msg).getMsg_id());
			}
			else if(type.toUpperCase() == "OUT")
			{
				obj.put("TYPE", "OUT");
				obj.put("UID", ((OutMsg)msg).getUid());
			}
			else
			{
				System.out.print("Nieznany typ wiadomosci przy pakowaniu do JSON'a");
			}
					
			return obj.toString();
		}
		
		catch (Exception e) {         
            return "ERROR";
		}
	}
	
	
	Message unpack(String json_string)
	{
		Message message =new Message();
		JSONObject json_obj = new JSONObject();
		JSONTokener tokener = new JSONTokener(json_string);
		json_obj = (JSONObject) tokener.nextValue();
		
		return unpack(json_obj);
	}
	
	
	Message unpack(JSONObject obj)
	{	
		this.msg = new Message();
		this.obj = obj;
		try{
			this.obj = obj;
			String type = (String) obj.get("TYPE");
			System.out.print("type: "+ type);
			if(type.toUpperCase() == "P2P" )
			{
				msg.setType("Ptp");
				return (P2pMsg) msg;
			}
			else if(type == "HII")
			{
				((HiiMsg)msg).setType("HII");
				((HiiMsg)msg).setUid((String)obj.get("UID"));
				((HiiMsg)msg).setUsername((String)obj.get("USERNAME"));
				
				UserVector uv;
				JSONObject o;
				JSONArray ja = (JSONArray)obj.get("VECTOR");
				Vector<UserVector> vuv = new Vector<UserVector>();
				Iterator<JSONObject> it = ja.iterator();
				while (it.hasNext())
				{
					uv = new UserVector();
					o = (JSONObject)it.next();
					uv.setIp_address((String)o.get("IP_ADDRESS"));
					uv.setUid((String)o.get("UID"));
					uv.setUsername((String)o.getString("USERNAME"));
					vuv.add(uv);					
				}
				((HiiMsg)msg).setVector(vuv);
				return (HiiMsg)msg;
			}
			else if(type == "LOG")
			{
				((LogMsg)msg).setType("LOG");
				((LogMsg)msg).setUid((String)obj.get("UID"));
				((LogMsg)msg).setUsename((String)obj.get("USERNAME"));
				((LogMsg)msg).setIp_address((String)obj.get("IP_ADDRESS"));
				return (LogMsg)msg;
			}
			else if(type == "MTX")
			{
				((MtxMsg)msg).setType("MTX");
				JSONArray jmatrix, jrow, jvec;
				Vector<Vector<Knowlage>>  kmac= new Vector ();
				Vector<Knowlage> kvec;
				jmatrix = (JSONArray)obj.get("MAC");
				Knowlage k;
				Iterator itm = jmatrix.iterator();
				Iterator itr;
				while (itm.hasNext())
				{
					jrow = (JSONArray)itm.next();
					itr = jrow.iterator();
					kvec = new Vector<Knowlage>();
					while (itr.hasNext());
					{						
						k = new Knowlage();
						k.setMsg_no(((Knowlage)itr.next()).getMsg_no());
						kvec.add(k);						
					}
					kmac.add(kvec);
				}
				((MtxMsg)msg).setMac(kmac);
				Vector<UserId> uidvec = new Vector<UserId>();
				jvec = (JSONArray) obj.getJSONArray("VEC");
				Iterator itv = jvec.iterator();
				UserId uid;
				Long l;
				while (itv.hasNext())
				{
					uid = new UserId();
					l = (Long)itv.next(); 
					uid.setUser_id(l.longValue());
					uidvec.add(uid);
				}
				((MtxMsg)msg).setVector(uidvec);
				System.out.print(msg);
				return (MtxMsg) msg;
			}
			else if(type == "MTX")
			{
				((MsgMsg)msg).setType("MSG");
				((MsgMsg)msg).setSender((String)obj.get("SENDER"));
				((MsgMsg)msg).setReceiver((String)obj.get("RECEIVER"));
				((MsgMsg)msg).setMsg_id(((Long)obj.get("MSG_ID")).longValue());
				JSONArray tvec =(JSONArray)obj.get("TIME_VEC");
				JSONArray vec =(JSONArray)obj.get("VEC");
				Iterator<Long> itk = tvec.iterator();
				Iterator<Long> itv = vec.iterator();
				Long l;
				Knowlage k;
				UserId uid;
				Vector<Knowlage> kvec = new Vector<Knowlage>();
				Vector<UserId> uidvec = new Vector<UserId>();
				while (itk.hasNext())
				{
					k = new Knowlage();
					l = itk.next().longValue();
					k.setMsg_no(l);
					kvec.add(k);					
				}
				while(itv.hasNext())
				{
					uid = new UserId();
					l = itv.next().longValue();
					uid.setUser_id(l);
					uidvec.add(uid);
				}
				((MsgMsg)msg).setTime_vec(kvec);
				((MsgMsg)msg).setVec(uidvec);
				return (MsgMsg) msg;
				
			}
			else if(type == "LIV")
			{
				((LivMsg)msg).setType("LIV");
				((LivMsg)msg).setUid((String)obj.get("UID"));
				((LivMsg)msg).setIp_address((String)obj.get("IP_ADDRESS"));
				return (LivMsg)msg;
			}
			else if(type == "GET")
			{
				((GetMsg)msg).setType("GET");
				((GetMsg)msg).setUid((String)obj.get("UID"));
				((GetMsg)msg).setMsg_id(((Long)obj.get("MSG_ID")).longValue());
				return (GetMsg)msg;
			}
			else if(type == "OUT")
			{
				((OutMsg)msg).setType("OUT");
				((OutMsg)msg).setUid((String)obj.get("UID"));				
				return (OutMsg)msg;
			}
				
			else
			{
				msg.setType("ERR");
			}
				return msg;
			
		}
		catch(Exception e)
		{
			return msg;
		}
	}
	
	public Serializer()	
	{		
		try{
			obj = new JSONObject();
			msg = new Message();
			
		}
		catch(Exception e)
		{
			System.out.print("Blad tworzenia serializatora");
		}
		
	}

	public JSONObject getObj() {
		return obj;
	}

	public void setObj(JSONObject obj) {
		this.obj = obj;
	}

	public Message getMsg() {
		return msg;
	}

	public void setMsg(Message msg) {
		this.msg = msg;
	}
	
}

