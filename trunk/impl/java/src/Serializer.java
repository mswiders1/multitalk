import net.sf.json.*;
import java.util.*;


public class Serializer {
	private JSONObject obj;
	
	String pack(Message msg)
	{
		String type = msg.getType();
		if (type.toUpperCase()=="P2P")
		{
			obj.put("TYPE", "P2P" );
			obj.put("CONTENT","MULTITALK_5387132");
		}
		else if (type.toUpperCase()=="HII")
		{
			JSONArray jarray = new JSONArray();
			obj.put("TYPE", "HII");
			obj.put("UID",((HiiMsg)msg).getUid());
			obj.put("USERNAME", ((HiiMsg)msg).getUid());
			
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
	
	Message unpack(JSONObject obj)
	{
		Message msg = new Message();
		return msg;
	}
	
	public Serializer()
	{
		obj = new JSONObject();
	}

	public JSONObject getObj() {
		return obj;
	}

	public void setObj(JSONObject obj) {
		this.obj = obj;
	}
	
}

