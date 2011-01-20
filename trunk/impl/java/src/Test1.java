import java.util.*;
import javax.swing.JFrame;


public class Test1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Controller c = new Controller();
		c.LogIn();
		//System.out.println("connectiony: "+ c.getNet_management().getConnections());
		//System.out.println("podlaczeni:" + c.getNet_management().getConnections());
		
		//Broadcast b = new Broadcast(new NetManagement());
		//b.send();
		//b.receive();
		//NetManagement nm = new NetManagement();
		//Contact c= new Contact();
		//c.setIp("192.168.1.102");
		//System.out.println("Contact"+ c.getIp());
		
		
		//System.out.println(nm.getConnections());
		//b.closeSocket();
		
		/*
		Knowlage k = new Knowlage(1);
		UserId u = new UserId(2);
		Vector vk = new Vector();
		Vector vk2 = new Vector();

		vk.add(k);
		vk.add(new Knowlage (3));
		vk.add(new Knowlage (4));
		vk.add(new Knowlage (5));
		vk2 = (Vector)vk.clone();
		Vector vu = new Vector();
		vu.add(u);
		MsgMsg msg = new MsgMsg("krzysiek", "olka", 1, vk, vu, "zawartosc");
		Serializer serial =	new Serializer();		
		serial.pack(msg);
		
		MtxMsg mtx = new MtxMsg();
		Vector<Vector<Knowlage>> mac = new Vector();
		mac.add(vk);
		mac.add(vk2);
		mtx.setMac(mac);
		mtx.setVector(vu);
		serial = new Serializer();
		serial.pack(mtx);
		System.out.println(serial.getObj().toString());
		serial.unpack(serial.getObj());
		*/
		//
		// TODO Auto-generated method stub
		/*Vector <Contact> v = new Vector<Contact>();
		v.add(new Contact(1,"krzysiek",true));
		v.add(new Contact(2,"krzysiek2",true));
		Contact sasiad = new Contact(1,"k",true);
		sasiad.setIp("192.168.1.100");
		Contact c =new Contact(3,"ja",true);
		MyId myid = new MyId();
		GUI Gui = new GUI(v,c,sasiad);
		*/
		//myid.findNIC();
		//System.out.println(myid.findIp(myid.inaddr));
		//System.out.println(myid.findMac(myid.nic));
		//System.out.println(myid.findId());
		//System.out.println(myid.getIp());		

	}

}
