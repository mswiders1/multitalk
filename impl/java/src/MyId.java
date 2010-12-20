import java.net.*;
import java.io.*;
import java.nio.*;
import java.util.*;

public class MyId {

	private String id = null;
	private InetAddress inaddr=null;
	private NetworkInterface nic= null;
	
	
	
	private void findNIC()
	{
		Enumeration  e,e2;
		NetworkInterface ni;
		InetAddress ia = null;
		try{
			e= NetworkInterface.getNetworkInterfaces(); 
			while(e.hasMoreElements()) {
	             ni = (NetworkInterface) e.nextElement();
	             if ( ni.isUp() && ni.getName().contains("eth") )
	             {
	            	 e2 = ni.getInetAddresses();
	            	 while (e2.hasMoreElements()){
	            		 ia = ((InetAddress)e2.nextElement());
	            		 if(ia.getHostAddress().matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}") ){
	            			 nic = ni;
	            			 inaddr = ia;
	            			 break;
	            		 }
	            	}
	             }
			}
		}
		catch (Exception exc)
		{
			exc.printStackTrace();
		}
		return ;
	}
		
    private static String byteToHex(byte b){
    	  int i = b & 0xFF;
    	  return Integer.toHexString(i);
    	}
    
    private String findIp(InetAddress _ia)
    {
    	String ip = "";    	 
    	try{
    		ip = _ia.getHostAddress();
    	}
    	catch (Exception exc)
		{
			exc.printStackTrace();
		}    	
    	return ip;
    }
    
    private String findMac(NetworkInterface _nic)
    {
    	String mac = "";
    	byte [] b;
    	try{
    		b = _nic.getHardwareAddress();
    		for(int i=0;i<b.length;i++)
    		{
    			mac += byteToHex(b[i]);
    			if (i< b.length -1)
    			{
    				mac+=":";
    			}
    		}
    	}
    	catch (Exception exc)
		{
			exc.printStackTrace();
		}
    	return mac;
    }
    
    private String findUserName()
    {
    	return System.getProperty("user.name");
    }
	
    public int findId()
    {
    	String uid = "";
    	this.findNIC();
    	
    	uid += this.findMac(nic);
    	uid += this.findIp(inaddr);
    	uid += this.findUserName();
    	return  uid.hashCode() > 0 ? uid.hashCode() : uid.hashCode() * -1 ;
    }
}
