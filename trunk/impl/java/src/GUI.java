import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class GUI {

}


class ContactsFrame extends JFrame
{
	public ContactsFrame()
	{
		setTitle("Mutitalk-Kontakty");
		setSize(WIDTH,HEIGHT);
		
		
		JMenu  menuSettings = new JMenu("Settings");
		JMenu menuMessages = new JMenu("Messages");
		
		JMenuItem userSettMenu = menuSettings.add(new MenuAction ("User"));
		JMenuItem comunicatorSettMenu = menuSettings.add(new MenuAction ("Comunicator"));
		
		JMenuItem talkMessagesMenu = menuMessages.add(new MenuAction ("Talk"));
		
		
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		menuBar.add(menuSettings);
		menuBar.add(menuMessages);
		
		
		Vector v = new Vector();
		v.add("user1");
		v.add("user2");
		v.add("user3");
		contactsList = new JList(v);
		
		ContactsPanel panel = new ContactsPanel();
		Container contentPane = getContentPane();
		contentPane.add(contactsList);
		//contentPane.add(panel);
		
		
		
		
	}
	
	public static final int WIDTH = 300;
	public static final int HEIGHT = 600;
	public JList contactsList;
}

class ContactsPanel extends JPanel
{
	ContactsPanel ()
	{
		JButton b = new JButton("Guzik");
		add(b);
	}
}

class MenuAction extends AbstractAction
{
	public MenuAction(String name)
	{
		super(name);
	}
	public void actionPerformed(ActionEvent action)
	{
		;	
	}
}