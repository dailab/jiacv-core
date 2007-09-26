package de.dailab.jiactng.examples.chat;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.CommunicationBean;
import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.IJiacMessageListener;
import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;

public class ChatGuiBean extends AbstractMethodExposingBean implements IJiacMessageListener{

	private String _beanName = "CommunicationBean";
	private IMessageBoxAddress _messageBoxAddress = null;
	private JFrame _f;
	private JLabel _topline;
	private JLabel _addressLine;
	private JLabel _inputDescription;
	private JList _messagesReceived;
	private ListCellRenderer _renderer = new MessageCellRenderer();
	private volatile String[] _messages;
	private JTextField _inputField;
	private int _newMessageIndex = 0;
	private final int _maxMessages = 50;
	private JPanel _panel;
	
	private final DefaultListModel _messageListModel = new DefaultListModel(); 
	
	private CommunicationBean _cBean;
	private ArrayList<ICommunicationAddress> addressList = new ArrayList<ICommunicationAddress>();
	
	
	private class ListElement {
		public String message = "";
		public ICommunicationAddress from;
		public ICommunicationAddress at;
		
		public ListElement(){
			message = "message";
		}
		
		public ListElement(String messageText){
			message = messageText;
		}
		
		public String toString(){
			return message;
		}
		
	}
	
	private class ListElementHead extends ListElement {
		public ListElementHead(String messageText){
			super(messageText);
		}
		
		public ListElementHead( ListElement le){
			this.message = le.message;
			this.from = le.from;
			this.at = le.at;
		}
		
		public String toString(){
			return "message received from: " + from + " at " + at;
		}
		
	}
	
	class MessageCellRenderer implements ListCellRenderer {
		  protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

		  public Component getListCellRendererComponent(JList list, Object value, int index,
		      boolean isSelected, boolean cellHasFocus) {
			  String listFont = list.getFont().getName();
			  int listFontSize = list.getFont().getSize();

			  JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
					  isSelected, cellHasFocus);

			  if (value instanceof ListElementHead) {
				  ListElementHead listThis = (ListElementHead) value;

				  renderer.setText(listThis.toString());
				  renderer.setFont(new Font(listFont, Font.ITALIC, listFontSize) );
				  
				  if (!isSelected) {
					  renderer.setForeground(Color.RED);
				  }
				  
			  } else {
				 ListElement listThat = (ListElement) value;
				 
				 renderer.setText(listThat.toString());
				 renderer.setFont(list.getFont());
				 
				 if (!isSelected) {
					  renderer.setForeground(list.getForeground());
				  }
				 
			  }
			  
			  return renderer;
		  }
	}

	
	public ChatGuiBean() {
		setBeanName("CommunicationGuiBean");
	}
	
	public void receive(IJiacMessage message, ICommunicationAddress at){
		log.debug("message received from: " + message.getSender() + " at " + at.toUnboundAddress().toString());
		
		TestContent payload = null;
		if (message.getPayload() instanceof TestContent) {
			payload = ( TestContent) message.getPayload();
			log.debug("Payload is instanceof TestContent");
		}
		
		if (payload != null){
			
			ListElement le = new ListElement(payload.getContent());
			le.from = message.getSender();
			le.at = at;
			ListElementHead leh = new ListElementHead(le);
			
			
			_messageListModel.add(_newMessageIndex++ % _maxMessages, leh);
			_messageListModel.add(_newMessageIndex++ % _maxMessages, le);
			
		} else {
			printLine("Content could not be decyphered");
		}
	}
	
	private void printLine(String line){
		ListElement le = new ListElement(line);
		_messageListModel.add(_newMessageIndex++ % _maxMessages, le);
	}
	
	
	private void printHelp(){
		printLine("type  \"pack\" to resize the window; ");
		printLine("To send a Message, type \"g.<Name>.\" for a group or \"m.<Name>.\" ");
		printLine("for a messagebox address followed by your message.");
		printLine("To change your Address, type \"c.<newMessageBoxName>\"");
		printLine("To Listen to a new Address, type \"listen.[g|m].<AddressName>\"");
		printLine("To stop listen to a Address, type \"stopListen.[g|m].<AddressName>\"");
		printLine("To stop listen to all but your very own address, type \"stopListenAll\"");
		printLine("To get a List of all used Addresses, type \"ListAddresses\" ");
	}
	
	private void printAddressList(){
		printLine("List of Addresses Listening To:");
		printLine(_messageBoxAddress.toUnboundAddress().toString());
		for (ICommunicationAddress address : addressList){
			printLine(address.toUnboundAddress().toString());
		}
	}
	
	/**
	 * targetaddress has to start with "g." or "m." this is checked before running this method.
	 * @param targetAddress
	 */
	private void listenTo(String targetAddress){
		ICommunicationAddress address;
		String addressName = targetAddress.substring(targetAddress.indexOf(".")+1);
		
		if (targetAddress.startsWith("g.")){
			address = CommunicationAddressFactory.createGroupAddress(addressName);
		} else {
			address = CommunicationAddressFactory.createMessageBoxAddress(addressName);
		}
		
		try {
			_cBean.register(address);
			addressList.add(address);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		
	}
	
	private void stopListenTo(String targetAddress){
		ICommunicationAddress address;
		String addressName = targetAddress.substring(targetAddress.indexOf(".")+1);
		
		if (targetAddress.startsWith("g.")){
			address = CommunicationAddressFactory.createGroupAddress(addressName);
		} else {
			address = CommunicationAddressFactory.createMessageBoxAddress(addressName);
		}
		
		try {
			_cBean.unregister(address);
			addressList.remove(address);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
	}
	
	private void stopListenToAll(){
		for (ICommunicationAddress address : addressList){
			try {
				_cBean.unregister(address);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
		}
		
		addressList.clear();
	}
	
	
	private void changeAddress(String address){
		try {
			_cBean.destroyMessageBox(_messageBoxAddress);
			_messageBoxAddress = CommunicationAddressFactory.createMessageBoxAddress(address);
			_cBean.register(this, _messageBoxAddress, null);
			_addressLine.setText("Your Address: m." + _messageBoxAddress.toString().substring(7));
			_addressLine.validate();
			_f.repaint();
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
	}
	
	public void setCommunicationBean(CommunicationBean cBean){
		_cBean = cBean;
	}
	
	@Override
	public void doInit() throws Exception {
		super.doInit();
		
		if (this.thisAgent != null){
			_beanName = this.thisAgent.getAgentName();
		}
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		
		FormLayout layout = new FormLayout (
				"left:50dlu:grow, 50dlu:grow, left:50dlu:grow", // Columspecification
				"center:10dlu:none, " + // LabelRow
				"center:10dlu:none, " + // AddressRow
				"center:pref:grow, " + // ListRow
				"center:20dlu:none, " + //InputDescription 
				"center:10dlu:none, center:3dlu:none"	// InputRow
				);
		
		layout.addGroupedColumn(1);
		_panel = new JPanel(layout);
		
		CellConstraints cc = new CellConstraints();
		
		// setup Frame
		_f = new JFrame( "Communicator of " + _beanName); 
		_f.setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE ); 
		_f.setLocation(d.width / 16, d.height / 32);
		
		// model gui on Frame
		// JLabel topline = new JLabel ("<html> Messages <p/> received:</html>");
		_topline = new JLabel ("Messages received:");
		_topline.setFont(new Font("serif", Font.BOLD, 16));
		_topline.setForeground(Color.BLACK);
		_topline.setAlignmentX(0f);
		_topline.setAlignmentY(0f);
		
		_addressLine = new JLabel("No Address registered");
		_addressLine.setFont(new Font("serif", Font.ITALIC, 14));
		_addressLine.setForeground(Color.BLACK);
		
		_messages = new String[_maxMessages+1];
		_messages[0] = "no messages yet";
			   
		_messagesReceived = new JList( _messageListModel);
		_messagesReceived.setBackground(Color.BLUE);
		_messagesReceived.setForeground(Color.LIGHT_GRAY);
		_messagesReceived.setFixedCellHeight(14);
		_messagesReceived.setCellRenderer(_renderer);
		_messagesReceived.validate();
		
		
	    JScrollPane scrollPane = new JScrollPane(_messagesReceived);
	    scrollPane.setMinimumSize(new Dimension(100, 200));
	    scrollPane.setPreferredSize(new Dimension(100,300));
		
		_inputDescription = new JLabel("Format: [g|m].[Name of group or messagebox].[Messagetext]");
		
		_inputField = new JTextField(20);
		_inputField.setFocusCycleRoot(true);
		
		
		_inputField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				String command = evt.getActionCommand();

				if (command.equalsIgnoreCase("pack")){
					_f.pack();
					
				} else if (command.equalsIgnoreCase("help")){
					printHelp();
					
				} else if (command.equalsIgnoreCase("ListAddresses")){
					printAddressList();
					
				} else if (command.startsWith("c.")){
					changeAddress(command.substring(command.indexOf(".")+1).toLowerCase());
					
				} else if (command.startsWith("listen.g.") || command.startsWith("listen.m.")){
					listenTo(command.substring(command.indexOf(".")+1));
				
				} else if (command.startsWith("stopListen.g.") || command.startsWith("stopListen.m.")){	
					stopListenTo(command.substring(command.indexOf(".")+1));
					
				}else if (command.equalsIgnoreCase("stopListenAll")){
					stopListenToAll();
					
				} else if ( (command.startsWith("g.")) || (command.startsWith("m.")) ){
					log.debug("got something to send");
					boolean isGroup;
					String text;
					String targetName;
					TestContent payload;

					isGroup = command.startsWith("g.");
					text = command.substring(command.indexOf(".") + 1);
					if (text.contains(".")){
						log.debug("seems to be a valid something");
						targetName = text.substring(0, text.indexOf("."));
						text = text.substring(text.indexOf(".") + 1);
						payload = new TestContent(text);
						JiacMessage jMessage = new JiacMessage(payload, _messageBoxAddress);
						try {
							_cBean.send(
									jMessage,
									isGroup ? CommunicationAddressFactory.createGroupAddress(targetName) 
											: CommunicationAddressFactory.createMessageBoxAddress(targetName));
						} catch (CommunicationException e) {
							e.printStackTrace();
						}
						log.debug("Message sent to: " + targetName + " reads: " + text);
					}
				}
				_inputField.setText("");
			}
		});
		
		_panel.add(_topline, cc.xywh(1, 1, 3, 1));
		_panel.add(_addressLine, cc.xywh(1, 2, 3, 1));
		_panel.add(scrollPane, cc.xywh(1, 3, 3, 1));
		_panel.add(_inputDescription, cc.xywh(1, 4, 3, 1));
		_panel.add(_inputField, cc.xywh(1, 5, 3, 1));
		_panel.setBackground(Color.LIGHT_GRAY);
		
		// add components to the Frame and make it visible
		_f.add(_panel);

	}
	
	@Override
	public void doStart() throws Exception {
		super.doStart();
		Iterator<IAgentBean> beanList = this.thisAgent.getAgentBeans().iterator();
		
		while (beanList.hasNext()){
			IAgentBean bean = beanList.next();
			if (bean instanceof CommunicationBean){
				_cBean = (CommunicationBean) bean;
				break;
			}
		}
				
		if (_cBean != null){
			
			_messageBoxAddress = CommunicationAddressFactory.createMessageBoxAddress(_beanName);
			_cBean.register(this, _messageBoxAddress, null);
			
			_cBean.register(this, CommunicationAddressFactory.createGroupAddress("all"), null);
			addressList.add(CommunicationAddressFactory.createGroupAddress("all"));
			
			_addressLine.setText("Your Address: m." + _messageBoxAddress.toString().substring(7));
			_addressLine.validate();
			
			TestContent payload = new TestContent("m." + _messageBoxAddress.toString().substring(7) + " is ready to chat");
			JiacMessage jMessage = new JiacMessage(payload, _messageBoxAddress);
			try {
				_cBean.send(
						jMessage,
						CommunicationAddressFactory.createGroupAddress("all")
						); 
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
			
			printLine("CommunicationBean installed, ready to chat");
		} else {
		 	printLine("CommunicationBean not installed!");
		}
		
		printLine("type \"help\" to get some help");
		
		_f.pack();
		_f.setVisible( true ); 
		
	}
		
	@Override
	public void doStop() throws Exception {
		super.doStop();
		if (_messageBoxAddress != null){
			_cBean.destroyMessageBox(_messageBoxAddress);
		}
		_f.setVisible(false);
		TestContent payload = new TestContent("m." + _messageBoxAddress.toString().substring(7) + " has left the chat");
		JiacMessage jMessage = new JiacMessage(payload, _messageBoxAddress);
		try {
			_cBean.send(
					jMessage,
					CommunicationAddressFactory.createGroupAddress("all")
					); 
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		log.debug("ChatGuiBean has stopped");
	}
	
	@Override
	public void doCleanup() throws Exception {
		super.doCleanup();
		_f = null;
		_cBean = null;
		log.debug("Cleanup was finished");
	}
	

	
}
