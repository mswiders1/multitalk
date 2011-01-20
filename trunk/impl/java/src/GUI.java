import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class GUI {
	
	public GUI(Controller controller)
	{
		this.controller = controller;
		ContactsFrame contactsFrame = new ContactsFrame(controller.getContacts_for_gui() ,controller.getMe() , controller.getNeighbour(),controller);
		contactsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contactsFrame.show();		
	}
	Controller controller;
	
	
}

class ContactsFrame extends JFrame implements ActionListener
{
	public ContactsFrame(Collection<Contact> contacts_list, Contact me, Contact connect_to, Controller controller)
	{
		setTitle("Mutitalk-Kontakty");
		setSize(WIDTH,HEIGHT);
		this.me = me;
		this.connect_to = connect_to;
		this.contacts_list = contacts_list;
		
		JMenu  menuSettings = new JMenu("Settings");
		JMenu menuMessages = new JMenu("Contact");
		JMenu menuFind = new JMenu("Find");
		
		userSettMenu = menuSettings.add(new MenuAction ("User"));		
		talkMessagesMenu = menuMessages.add(new MenuAction ("Talk"));
		deleteMessagesMenu = menuMessages.add(new MenuAction ("Delete"));
		findContactMenu = menuFind.add(new MenuAction ("Find Contact"));
		
		userSettMenu.addActionListener(this);
		talkMessagesMenu.addActionListener(this);
		deleteMessagesMenu.addActionListener(this);
		findContactMenu.addActionListener(this);
		

			
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		menuBar.add(menuSettings);
		menuBar.add(menuMessages);
		menuBar.add(menuFind);
			
		contacts_panel = new ContactsPanel(contacts_list, controller);
		Container contentPane = getContentPane();		
		contentPane.add(contacts_panel);	
		this.controller = controller;
		
	}
	
	public void actionPerformed(ActionEvent e)
	{
		Vector<Contact> marked;
		Object [] obj_marked;
		String names = "";
		
		if (e.getSource() == userSettMenu)
		{
			System.out.println("Wcisnieto menu user settings");
			//System.out.println("Na liscie:" + this.contacts_list);
			//this.refresh();
			new SettingsFrame (me, connect_to, controller);
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
			 new TalkFrame(marked,controller);
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
			 String id = "0";
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
			 this.refresh();			 			  
		}
		else if (e.getSource() == findContactMenu )
		{
			System.out.println("Wcisnieto menu find");
			new FindParamFrame(contacts_list,this, controller);
		}
			
	}
	
	public void refresh()
	{
		contacts_panel.refresh();
		this.validate();
		this.repaint();
	}
	
	private  JMenuItem userSettMenu, talkMessagesMenu ,deleteMessagesMenu, findContactMenu;
	private ContactsPanel contacts_panel;
	private Contact me, connect_to;
	private Collection<Contact> contacts_list;
	private static final int WIDTH = 300;
	private static final int HEIGHT = 600;	
	private Controller controller;
}

class ContactsPanel extends JPanel
{
	ContactsPanel (Collection<Contact> contacts_list,Controller controller)
	{
		this.controller = controller;
		model = new DefaultListModel();
		this.contacts_list = contacts_list;
		Iterator<Contact> it = this.contacts_list.iterator();
		int i = 0;
		while (it.hasNext())
		{
			model.add(i,it.next() );
			i++;
		}
			
		j_contacts_list = new JList(model);
		add(j_contacts_list);
		
	}
	
	public void refresh()
	{
		this.j_contacts_list.removeAll();		
		model =(DefaultListModel) this.j_contacts_list.getModel();
		model.removeAllElements();
		Iterator<Contact> it = this.contacts_list.iterator();
		int i = 0;
		while (it.hasNext())
		{
			model.add(i,it.next() );
			i++;
		}
		
		this.validate();
		this.repaint();

	}
	
	private JList j_contacts_list;
	private Collection<Contact> contacts_list;
	DefaultListModel model;
	private Controller controller;
	
	
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

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
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
	public TalkFrame(Collection<Contact> interlocutors_list, Controller controller)
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
		
		talk_panel = new TalkPanel(interlocutors_list,controller);
		Container contentPane = getContentPane();		
		contentPane.add(talk_panel);
		this.controller = controller;
		
	}
	
	private static final int WIDTH = 500;
	private static final int HEIGHT = 500;
	private TalkPanel talk_panel;	
	private Vector<Contact> interlocutors_list;
	private Controller controller;
	public TalkPanel getTalk_panel() {
		return talk_panel;
	}
	public void setTalk_panel(TalkPanel talkPanel) {
		talk_panel = talkPanel;
	}
	public Vector<Contact> getInterlocutors_list() {
		return interlocutors_list;
	}
	public void setInterlocutors_list(Vector<Contact> interlocutorsList) {
		interlocutors_list = interlocutorsList;
	}
	public Controller getController() {
		return controller;
	}
	public void setController(Controller controller) {
		this.controller = controller;
	}
	
	
}

class TalkPanel extends JPanel implements ActionListener
{
	TalkPanel(Collection<Contact> interlocutors_list, Controller controller)
	{
		this.setBackground(Color.LIGHT_GRAY);
		this.controller = controller;
		
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
			
			this.controller.messageFromGUI(this.getInterlocutors_list(), message);
			//this.controller.getNet_management();
		}
	}
		
	private Collection<Contact> interlocutors_list;
	private JButton j_btn_send; 
	private JTextArea j_write_area;
	private JTextArea j_text_area;
	private Controller controller;
	
	public Collection<Contact> getInterlocutors_list() {
		return interlocutors_list;
	}

	public void setInterlocutors_list(Collection<Contact> interlocutorsList) {
		interlocutors_list = interlocutorsList;
	}

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}
	
}

class SettingsFrame extends JFrame
{
	public SettingsFrame(Contact c, Contact connect_to, Controller controller)
	{
		this.controller = controller;
		settings_panel = new SettingsPanel(c,this, connect_to, controller);
		setTitle("Settings");
		setSize(WIDTH,HEIGHT);
		
		
		Container settings_pane = getContentPane();		
		settings_pane.add(settings_panel);		
		show();
	}
	
	private static final int WIDTH = 400;
	private static final int HEIGHT = 400;
	private SettingsPanel settings_panel;
	private Controller controller;
	
	public SettingsPanel getSettings_panel() {
		return settings_panel;
	}
	public void setSettings_panel(SettingsPanel settingsPanel) {
		settings_panel = settingsPanel;
	}
	public Controller getController() {
		return controller;
	}
	public void setController(Controller controller) {
		this.controller = controller;
	}
		
}

class SettingsPanel extends JPanel implements ActionListener
{
	public SettingsPanel(Contact c, SettingsFrame f, Contact connect_to, Controller controller)
	{
		this.controller = controller;
		me_contact = c;
		this.connect_to = connect_to; 
		this.f = f;
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
		
		JLabel ip_label = new JLabel("Ip of host to which connect (Optional): ");

		ip_txt_field = new JTextField(15);
		ip_txt_field.setText(this.connect_to.getIp());
		ip_txt_field.setMaximumSize(ip_txt_field.getPreferredSize());
		
		Box layout_lev3 = Box.createHorizontalBox();
		layout_lev3.add(ip_label);
		//layout_lev4.add(Box.createHorizontalStrut(10));
		
		Box layout_lev4 = Box.createHorizontalBox();
		layout_lev4.add(ip_txt_field);
		
		JButton btn_change = new JButton("Accept");
		btn_change.addActionListener(this);
		
		Box layout_lev5 = Box.createHorizontalBox();
		layout_lev5.add(btn_change);
		
		Box layout_vertical = Box.createVerticalBox();
		layout_vertical.add(layout_lev1);
		layout_vertical.add(layout_lev2);
		layout_vertical.add(Box.createGlue());
		layout_vertical.add(layout_lev3);
		layout_vertical.add(layout_lev4);
		layout_vertical.add(layout_lev5);
		
		add(layout_vertical,BorderLayout.CENTER);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		me_contact.setName(name_txt_field.getText());
		connect_to.setIp(ip_txt_field.getText());
		f.setVisible(false);
	}
	
	private Contact me_contact, connect_to;
	private JTextField name_txt_field, ip_txt_field;
	private SettingsFrame f;
	private Controller controller;
	public Contact getMe_contact() {
		return me_contact;
	}

	public void setMe_contact(Contact meContact) {
		me_contact = meContact;
	}

	public Contact getConnect_to() {
		return connect_to;
	}

	public void setConnect_to(Contact connectTo) {
		connect_to = connectTo;
	}

	public JTextField getName_txt_field() {
		return name_txt_field;
	}

	public void setName_txt_field(JTextField nameTxtField) {
		name_txt_field = nameTxtField;
	}

	public JTextField getIp_txt_field() {
		return ip_txt_field;
	}

	public void setIp_txt_field(JTextField ipTxtField) {
		ip_txt_field = ipTxtField;
	}

	public SettingsFrame getF() {
		return f;
	}

	public void setF(SettingsFrame f) {
		this.f = f;
	}

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}
	
	
}

class FindParamFrame extends JFrame
{
	public FindParamFrame(Collection<Contact> contacts_list,ContactsFrame f, Controller controller)
	{
		find_contact_param_panel = new ContactParamPanel(contacts_list,f,this, controller);
		setTitle("Find Contact");
		setSize(WIDTH,HEIGHT);
		this.controller = controller;
		
		
		Container find_contact_param_pane = getContentPane();		
		find_contact_param_pane.add(find_contact_param_panel);
		show();// do odzielnej metody
	}
	
	private static final int WIDTH = 400;
	private static final int HEIGHT = 400;
	private ContactParamPanel find_contact_param_panel;
	private Controller controller;
	public ContactParamPanel getFind_contact_param_panel() {
		return find_contact_param_panel;
	}
	public void setFind_contact_param_panel(ContactParamPanel findContactParamPanel) {
		find_contact_param_panel = findContactParamPanel;
	}
	public Controller getController() {
		return controller;
	}
	public void setController(Controller controller) {
		this.controller = controller;
	}
	
	
	
}

class ContactParamPanel extends JPanel implements ActionListener
{
	public ContactParamPanel(Collection<Contact> contacts_list, ContactsFrame f, FindParamFrame f2, Controller controller)
	{
	
	this.controller = controller;	
	this.setBackground(Color.LIGHT_GRAY);
	this.contact = new Contact();
	this.contacts_list = contacts_list;
	this.f = f;
	this.f2 = f2;
	
	JLabel id_label = new JLabel("       id: ");
	id_txt_field = new JTextField(15);	
	id_txt_field.setMaximumSize(id_txt_field.getPreferredSize());
	
	Box layout_lev1 = Box.createHorizontalBox();
	layout_lev1.add(id_label);
	layout_lev1.add(Box.createHorizontalStrut(10));
	layout_lev1.add(id_txt_field);
	
	JLabel name_label = new JLabel("name: ");

	name_txt_field = new JTextField(15);	
	name_txt_field.setMaximumSize(name_txt_field.getPreferredSize());
	
	Box layout_lev2 = Box.createHorizontalBox();
	layout_lev2.add(name_label);
	layout_lev2.add(Box.createHorizontalStrut(10));
	layout_lev2.add(name_txt_field);
	
	JButton btn_search = new JButton("Search");
	btn_search.addActionListener(this);
	
	Box layout_lev3 = Box.createHorizontalBox();
	layout_lev3.add(btn_search);
	
	Box layout_vertical = Box.createVerticalBox();
	layout_vertical.add(layout_lev1);
	layout_vertical.add(layout_lev2);
	layout_vertical.add(Box.createGlue());
	layout_vertical.add(layout_lev3);
	
	add(layout_vertical,BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e)
	{
		
		
	     if(((name_txt_field.getText().toString()).length()) > 0)
	     {
	    	 contact.setName(name_txt_field.getText().toString());
	     }
	     if(((id_txt_field.getText().toString()).length()) > 0)
	     {
	    	 contact.setId(id_txt_field.getText());
	     }
	     
	     new FindListFrame(contact, contacts_list,f,f2,controller);
	     f2.setVisible(false);
	     
    	 System.out.println(contact);
	}
	
	private Contact contact;
	private Collection<Contact> contacts_list;
	private JTextField name_txt_field, id_txt_field;
	private ContactsFrame f;
	private FindParamFrame f2;
	private Controller controller;
	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public Collection<Contact> getContacts_list() {
		return contacts_list;
	}

	public void setContacts_list(Collection<Contact> contactsList) {
		contacts_list = contactsList;
	}

	public JTextField getName_txt_field() {
		return name_txt_field;
	}

	public void setName_txt_field(JTextField nameTxtField) {
		name_txt_field = nameTxtField;
	}

	public JTextField getId_txt_field() {
		return id_txt_field;
	}

	public void setId_txt_field(JTextField idTxtField) {
		id_txt_field = idTxtField;
	}

	public ContactsFrame getF() {
		return f;
	}

	public void setF(ContactsFrame f) {
		this.f = f;
	}

	public FindParamFrame getF2() {
		return f2;
	}

	public void setF2(FindParamFrame f2) {
		this.f2 = f2;
	}

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}
	
	
}

class FindListFrame extends JFrame implements ActionListener
{
	public FindListFrame(Contact c, Collection<Contact> contacts_list, ContactsFrame f, FindParamFrame f2, Controller controller)
	{
		this.controller = controller;
		setTitle("Results of contacts search");
		setSize(WIDTH,HEIGHT);
		this.contacts_list = contacts_list;
		this.f = f;
		this.f2 = f2;
		///////////////
		JMenu  menuAdd = new JMenu("Add");		
		addMenu = menuAdd.add(new MenuAction ("Add Contact"));		
		addMenu.addActionListener(this);
		

			
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);		
		menuBar.add(menuAdd);
			
		find_list_panel = new FindListPanel(this.search(c),controller);
		
		Container contentPane = getContentPane();		
		contentPane.add(find_list_panel);	
		
		///////////
		Container settings_pane = getContentPane();		
		settings_pane.add(find_list_panel);		
		show();
	}
	
	public Collection<Contact> search(Contact c)
	{
		Collection<Contact> found_list = new Vector<Contact>();
		
		///////////////////////////////
		///// tutaj uruchamia funkcje wyszukujaca kontakty
		///// found_list = global_model.findContact(c);
		//////////////////////////////
		////// to nizej do zakomentowania
		found_list.add(new Contact("10","Olka",true));
		found_list.add(new Contact("11","Olka2",true));
		
		return found_list;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		Vector<Contact> marked;
		Object [] obj_marked;
		String names = "";
		
		if (e.getSource() == addMenu )
		{
			System.out.println("Wcisnieto add user button");
			
			 obj_marked = this.find_list_panel.getJ_found_list().getSelectedValues();
			 marked = new Vector<Contact>();
			 for(int i =0;i<obj_marked.length;i++)
			 {
				 marked.addElement((Contact)(obj_marked[i]));
				 names +=((Contact)(obj_marked[i]));
			 }
			 System.out.println("Wybrano " + marked);		
			 this.contacts_list.addAll(marked);
			 f.refresh();
			 System.out.println("Obecna lista kontaktow:"+ contacts_list);
			 this.setVisible(false);
			 this.dispose();
			 f2.dispose();			 
		}
		
	}
	
	private static final int WIDTH = 400;
	private static final int HEIGHT = 400;
	private FindListPanel find_list_panel;	
	private JMenuItem addMenu;
	private Collection<Contact> contacts_list;
	private ContactsFrame f;
	private FindParamFrame f2;
	private Controller controller;
	
	public FindListPanel getFind_list_panel() {
		return find_list_panel;
	}

	public void setFind_list_panel(FindListPanel findListPanel) {
		find_list_panel = findListPanel;
	}

	public JMenuItem getAddMenu() {
		return addMenu;
	}

	public void setAddMenu(JMenuItem addMenu) {
		this.addMenu = addMenu;
	}

	public Collection<Contact> getContacts_list() {
		return contacts_list;
	}

	public void setContacts_list(Collection<Contact> contactsList) {
		contacts_list = contactsList;
	}

	public ContactsFrame getF() {
		return f;
	}

	public void setF(ContactsFrame f) {
		this.f = f;
	}

	public FindParamFrame getF2() {
		return f2;
	}

	public void setF2(FindParamFrame f2) {
		this.f2 = f2;
	}

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}
	
	
}

class FindListPanel extends JPanel
{
	public FindListPanel(Collection<Contact> found_list, Controller controller)
	{
		this.controller = controller;
		this.found_list = found_list;
		j_found_list = new JList((Vector<Contact>) found_list);
		add(j_found_list);
	}
	
		
	private JList j_found_list;
	private Collection<Contact> found_list;
	private Controller controller;
	
	public JList getJ_found_list() {
		return j_found_list;
	}
	public void setJ_found_list(JList jFoundList) {
		j_found_list = jFoundList;
	}
	public Collection<Contact> getFound_list() {
		return found_list;
	}
	public void setFound_list(Collection<Contact> foundList) {
		found_list = foundList;
	}
	public Controller getController() {
		return controller;
	}
	public void setController(Controller controller) {
		this.controller = controller;
	}
	
}