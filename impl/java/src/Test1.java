import javax.swing.JFrame;


public class Test1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//
		// TODO Auto-generated method stub
		MyId myid = new MyId();
		ContactsFrame contactsFrame = new ContactsFrame();
		contactsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contactsFrame.show();
		
		//myid.findNIC();
		//System.out.println(myid.findIp(myid.inaddr));
		//System.out.println(myid.findMac(myid.nic));
		System.out.println(myid.findId());
		//System.out.println(myid.getIp());		

	}

}
