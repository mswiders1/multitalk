import java.util.*;
import javax.swing.JFrame;


public class Test1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//
		// TODO Auto-generated method stub
		Vector <Contact> v = new Vector<Contact>();
		v.add(new Contact(1,"krzysiek",true));
		v.add(new Contact(2,"krzysiek2",true));
		Contact sasiad = new Contact(1,"k",true);
		sasiad.setIp("192.168.1.100");
		Contact c =new Contact(3,"ja",true);
		MyId myid = new MyId();
		GUI Gui = new GUI(v,c,sasiad);
		
		//myid.findNIC();
		//System.out.println(myid.findIp(myid.inaddr));
		//System.out.println(myid.findMac(myid.nic));
		System.out.println(myid.findId());
		//System.out.println(myid.getIp());		

	}

}