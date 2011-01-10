import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class GUI {
	public GUI(Collection<Contact> contacts_list, Contact me)
	{
		ContactsFrame contactsFrame = new ContactsFrame(contacts_list,me);
		contactsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contactsFrame.show();
	}	
	
}

class ContactsFrame extends JFrame implements ActionListener
{
	public ContactsFrame(Collection<Contact> contacts_list, Contact me)
	{
		setTitle("Mutitalk-Kontakty");
		setSize(WIDTH,HEIGHT);
		this.me = me;
		
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
			new SettingsFrame (me);
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
			 
			obj_marked = this.contacts_panel.getJ_contacts_list().getSelectedValues();
			 marked = new Vector<Contact>();

			 for(int i =0;i<obj_marked.length;i++)
			 {
				 marked.addElement((Contact)(obj_marked[i]));
				 names +=((Contact)(obj_marked[i]));
			 }
				 
			 Iterator<Contact> it = this.contacts_panel.getContacts_list().iterator();
			 Iterator<Contact> itm;
			 long id = 0;
			 while (it.hasNext())
			 {
				id = it.next().getId();
				itm = marked.iterator();
				while (itm.hasNext())
				{
					if(itm.next().getId() == id)
					{
						it.remove();
						continue;
					}
				}
			 }
			 this.contacts_panel.getJ_contacts_list().clearSelection();
			 this.contacts_panel.getJ_contacts_list().removeAll();
			 this.contacts_panel.setJ_contacts_list(new JList((Vector<Contact>)this.contacts_panel.getContacts_list()));
			 this.contacts_panel.validate();
			 this.contacts_panel.getJ_contacts_list().validate();
			 this.contacts_panel.getJ_contacts_list().repaint();
			 this.contacts_panel.repaint();
			
			 
			 //System.out.print(this.contacts_panel.getContacts_list().size()+"^&*&)(&(");			 
		}
			
	}
	private  JMenuItem userSettMenu, talkMessagesMenu ,deleteMessagesMenu;
	private ContactsPanel contacts_panel;
	private Contact me;
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
			
		j_write_area = new JTextArea(3,30);
		//j_write_area.setLineWrap(true);
		
		j_btn_send = new JButton("Send");
		j_btn_send.addActionListener(this);
		
		JScrollPane scroll_text = new JScrollPane(j_text_area);
		JScrollPane scroll_write_text = new JScrollPane(j_write_area);
	
		add(scroll_text, BorderLayout.PAGE_START);		
		add(scroll_write_text, BorderLayout.CENTER);
		add(j_btn_send,BorderLayout.PAGE_END);
			
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

class SettingsFrame extends JFrame
{
	public SettingsFrame(Contact c)
	{
		settings_panel = new SettingsPanel(c);
		setTitle("Settings");
		setSize(WIDTH,HEIGHT);
		
		
		Container settings_pane = getContentPane();		
		settings_pane.add(settings_panel);		
		show();
	}
	
	private static final int WIDTH = 400;
	private static final int HEIGHT = 400;
	private SettingsPanel settings_panel;
}

class SettingsPanel extends JPanel implements ActionListener
{
	SettingsPanel(Contact c)
	{
		me_contact = c;
		this.setBackground(Color.LIGHT_GRAY);
		
		JLabel id_label = new JLabel("       id: ");
		JTextField id_txt_field = new JTextField(15);
		id_txt_field.setText(String.valueOf(c.getId()));
		id_txt_field.setEditable(false);
		id_txt_field.setMaximumSize(id_txt_field.getPreferredSize());
		
		Box layout_lev1 = Box.createHorizontalBox();
		layout_lev1.add(id_label);
		layout_lev1.add(Box.createHorizontalStrut(10));
		layout_lev1.add(id_txt_field);
		
		JLabel name_label = new JLabel("name: ");

		name_txt_field = new JTextField(15);
		name_txt_field.setText(c.getName());
		name_txt_field.setMaximumSize(name_txt_field.getPreferredSize());
		
		Box layout_lev2 = Box.createHorizontalBox();
		layout_lev2.add(name_label);
		layout_lev2.add(Box.createHorizontalStrut(10));
		layout_lev2.add(name_txt_field);
		
		JButton btn_change = new JButton("Accept");
		btn_change.addActionListener(this);
		
		Box layout_lev3 = Box.createHorizontalBox();
		layout_lev3.add(btn_change);
		
		Box layout_vertical = Box.createVerticalBox();
		layout_vertical.add(layout_lev1);
		layout_vertical.add(layout_lev2);
		layout_vertical.add(Box.createGlue());
		layout_vertical.add(layout_lev3);
		
		add(layout_vertical,BorderLayout.CENTER);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		me_contact.setName(name_txt_field.getText());		
	}
	
	private Contact me_contact;
	private JTextField name_txt_field;


	
}