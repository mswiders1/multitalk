import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class GUI {
	public GUI(Collection<Contact> contacts_list)
	{
		ContactsFrame contactsFrame = new ContactsFrame(contacts_list);
		contactsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contactsFrame.show();
	}	
	
}

class ContactsFrame extends JFrame implements ActionListener
{
	public ContactsFrame(Collection<Contact> contacts_list)
	{
		setTitle("Mutitalk-Kontakty");
		setSize(WIDTH,HEIGHT);		
		
		JMenu  menuSettings = new JMenu("Settings");
		JMenu menuMessages = new JMenu("Contact");
		
		userSettMenu = menuSettings.add(new MenuAction ("User"));		
		talkMessagesMenu = menuMessages.add(new MenuAction ("Talk"));
		deleteMessagesMenu = menuMessages.add(new MenuAction ("Delete"));
		userSettMenu.addActionListener(this);
		talkMessagesMenu.addActionListener(this);
		deleteMessagesMenu.addActionListener(this);

			
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		menuBar.add(menuSettings);
		menuBar.add(menuMessages);
			
		contacts_panel = new ContactsPanel(contacts_list);
		Container contentPane = getContentPane();		
		contentPane.add(contacts_panel);		
	}
	
	public void actionPerformed(ActionEvent e)
	{
		Vector<Contact> marked;
		Object [] obj_marked;
		String names = "";
		
		if (e.getSource() == userSettMenu)
		{
			System.out.println("Wcisnieto menu user settings");			
		}
		else if (e.getSource() == talkMessagesMenu )
		{
			System.out.println("Wcisnieto menu talk");	
			 obj_marked = this.contacts_panel.getJ_contacts_list().getSelectedValues();
			 marked = new Vector<Contact>();
			 for(int i =0;i<obj_marked.length;i++)
			 {
				 marked.addElement((Contact)(obj_marked[i]));
				 names +=((Contact)(obj_marked[i]));
			 }
			 System.out.println("Wybrano " + marked);
			 new TalkFrame(marked);
		}
		else if (e.getSource() == deleteMessagesMenu )
		{
			System.out.println("Wcisnieto menu delete");
		}
			
	}
	private  JMenuItem userSettMenu, talkMessagesMenu ,deleteMessagesMenu;
	private ContactsPanel contacts_panel;
	private static final int WIDTH = 300;
	private static final int HEIGHT = 600;	
	
}

class ContactsPanel extends JPanel
{
	ContactsPanel (Collection<Contact> contacts_list)
	{
		this.contacts_list = contacts_list;
		j_contacts_list = new JList((Vector<Contact>)contacts_list);
		add(j_contacts_list);

	}
	
	private JList j_contacts_list;
	private Collection<Contact> contacts_list;
	
	
	public JList getJ_contacts_list() {
		return j_contacts_list;
	}
	public void setJ_contacts_list(JList jContactsList) {
		j_contacts_list = jContactsList;
	}
	public Collection<Contact> getContacts_list() {
		return contacts_list;
	}
	public void setContacts_list(Collection<Contact> contactsList) {
		contacts_list = contactsList;
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

class TalkFrame extends JFrame
{
	public TalkFrame(Collection<Contact> interlocutors_list)
	{
		Iterator<Contact> it = 	interlocutors_list.iterator();
		String names="";
		while(it.hasNext())
		{
			names+=it.next().getName()+"~";
		}
		setTitle(names);
		setSize(WIDTH,HEIGHT);	
		show();
		
		talk_panel = new TalkPanel(interlocutors_list);
		Container contentPane = getContentPane();		
		contentPane.add(talk_panel);	
	}
	
	private static final int WIDTH = 500;
	private static final int HEIGHT = 500;
	private TalkPanel talk_panel;	
	private Vector<Contact> interlocutors_list;
}

class TalkPanel extends JPanel implements ActionListener
{
	TalkPanel(Collection<Contact> interlocutors_list)
	{
		this.setBackground(Color.LIGHT_GRAY);		
		
		this.interlocutors_list = interlocutors_list;
		j_text_area = new JTextArea(20,30);
		j_text_area.setLineWrap(true);
		j_text_area.setEditable(false);		
		add(j_text_area);
		j_write_area = new JTextArea(3,30);
		add(j_write_area);
		j_btn_send = new JButton("Send");
		j_btn_send.addActionListener(this);
		add(j_btn_send);
		
	}
	
	public void actionPerformed(ActionEvent e)
	{
		String message;
		if(e.getSource() == j_btn_send)
		{
			message = j_write_area.getText();
			j_text_area.append("me:\n");			
			j_text_area.append(message+"\n");
			j_write_area.setText("");
		
		}
	}
		
	private Collection<Contact> interlocutors_list;
	private JButton j_btn_send; 
	private JTextArea j_write_area;
	private JTextArea j_text_area;
}