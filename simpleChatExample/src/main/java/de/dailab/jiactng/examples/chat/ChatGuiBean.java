package de.dailab.jiactng.examples.chat;


import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

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
	private JTextField _inputField;
	private int _newMessageIndex = 0;
	
	private final DefaultListModel _messageListModel = new DefaultListModel(); 
	
	private CommunicationBean _cBean;
	private ArrayList<ICommunicationAddress> addressList = new ArrayList<ICommunicationAddress>();
	
	/**
	 * A class that encapsulates a textline for the list. e.g. a messagetext of a
	 * message received during runtime. It may contain informations about an address
	 * of such a message.
	 * 
	 * @author Martin Loeffelholz
	 *
	 */
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
	
	/**
	 * A class mostly similar to a normal listelement, but used to contain especially the addressinformations
	 * of messages received, so they can be used for replymethods more comfortable than in the beginning
	 * which will get implemented in a later version
	 * 
	 * @author Martin Loeffelholz
	 *
	 */
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
	
	
	/**
	 * This CellRenderer is responsible for formating and printing listelements within the used JList
	 * 
	 * @author Martin Loeffelholz
	 *
	 */
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

	/**
	 * Constructor for this class
	 * 
	 * @author Martin Loeffelholz
	 */
	public ChatGuiBean() {
		setBeanName("CommunicationGuiBean");
	}
	
	/**
	 * Entrypoint for messages coming from the CommunicationBean.
	 * Here all messages will get processed.
	 * 
	 *  @param message an IJiacMessage received from the communicationAddress
	 *  @param at the ICommunicationAddress the messages was sent to
	 *  
	 *  @author Martin Loeffelholz
	 */
	public void receive(IJiacMessage message, ICommunicationAddress at){
		ICommunicationAddress atUnbound = at.toUnboundAddress();
		log.debug("message received from: " + message.getSender() + " at " + atUnbound);
		
		TestContent payload = null;
		if (message.getPayload() instanceof TestContent) {
			payload = ( TestContent) message.getPayload();
			log.debug("Payload is instanceof TestContent");
		} else {
			log.debug("Payload is NOT instanceof TestContent");
		}
		
		if (payload != null){
			
			ListElement le = new ListElement(payload.getContent());
			le.from = message.getSender();
			le.at = at;
			ListElementHead leh = new ListElementHead(le);
			
			
			_messageListModel.add(_newMessageIndex++, leh);
			_messageListModel.add(_newMessageIndex++, le);
			
		} else {
			printLine("Content could not be decyphered");
		}
	}
	
	/**
	 * prints the given line into the JList
	 * @param line the line to print into the JList
	 * 
	 * @author Martin Loeffelholz
	 */
	private void printLine(String line){
		ListElement le = new ListElement(line);
		_messageListModel.add(_newMessageIndex++, le);
	}
	
	/**
	 * prints a list of all commands to the chatwindow
	 * 
	 * @author Martin Loeffelholz
	 */
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
	
	/**
	 * prints the list of all addresses the agent is listening to.
	 * 
	 * @author Martin Loeffelholz
	 */
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
	 * 
	 * @author Martin Loeffelholz
	 */
	private void listenTo(String targetAddress){
		if (_cBean != null){
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
		} else {
			System.err.println("Tried to listen to: " + targetAddress + " but no CommunicationBean was installed");
		}
		
	}
	
	/**
	 * stops to listen to the given targetAddress. 
	 * @param targetAddress No messages sent to this address will get received from this address anymore
	 * 
	 * @author Martin Loeffelholz
	 */
	private void stopListenTo(String targetAddress){
		if (_cBean != null){
		
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
	}
	
	/**
	 * Stops to listen to all addresses except your main-mailbox
	 * 
	 * @author Martin Loeffelholz
	 */
	private void stopListenToAll(){
		if (_cBean != null){
			for (ICommunicationAddress address : addressList){
				try {
					_cBean.unregister(address);
				} catch (CommunicationException e) {
					e.printStackTrace();
				}
			}
		} 
		addressList.clear();
	}
	
	/**
	 * helping method sending a message with "line" to the group all, where all agents
	 * using this bean will receive them
	 * @param line the messageText for all other agents listening
	 * 
	 * @author Martin Loeffelholz
	 */
	private void sendStatusMessage(String line){
		if (_cBean != null){
			TestContent payload = new TestContent(line);
			JiacMessage jMessage = new JiacMessage(payload, _messageBoxAddress);
			try {
				_cBean.send(
						jMessage,
						CommunicationAddressFactory.createGroupAddress("all")
				); 
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Tried to send Message " + line + " but no CommunicationBean installed");
		}
	}
	
	/**
	 * method to change "my own address" meaning the mailbox of this agentbean
	 * @param address the mailbox of this agentbean
	 * 
	 * @author Martin Loeffelholz
	 */
	private void changeAddress(String address){
		try {
			if (_cBean != null){
				ICommunicationAddress oldAddress = _messageBoxAddress.toUnboundAddress();
				_cBean.destroyMessageBox(_messageBoxAddress);
				_messageBoxAddress = CommunicationAddressFactory.createMessageBoxAddress(address);
				_cBean.register(this, _messageBoxAddress, null);
				_addressLine.setText("Your Address: m." + _messageBoxAddress.toString().substring(7));
				_addressLine.validate();
				sendStatusMessage(oldAddress + " is now known as " + _messageBoxAddress.toUnboundAddress());
				_f.repaint();
			} else {
				System.err.println("Tried to change address, but no CommunicationBean was installed");
			}
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * setter for the CommunicationBean to use for sending messages
	 * @param cBean the CommunicationBean to use for sending messages
	 * 
	 * @author Martin Loeffelholz
	 */
	public void setCommunicationBean(CommunicationBean cBean){
		_cBean = cBean;
	}
	
	@Override
	/**
	 * Initializationmethod used by the LifeCycle class
	 * 
	 * @author Martin Loeffelholz
	 */
	public void doInit() throws Exception {
		super.doInit();
		
		if (this.thisAgent != null){
			_beanName = this.thisAgent.getAgentName();
		}
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		
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

		GridBagLayout layout = new GridBagLayout();
		Container contentPane = _f.getContentPane();
		contentPane.setLayout(layout);
		
		addComponent(contentPane, layout, _topline, 0, 0, 1, 1, 0.0, 0.0);
		addComponent(contentPane, layout, _addressLine, 0, 1, 1, 1, 0.0, 0.0);
		addComponent(contentPane, layout, scrollPane, 0, 2, 10, 30, 1.0, 1.0);
		addComponent(contentPane, layout, _inputDescription, 0, 33, 1, 1, 0.0, 0.0);
		addComponent(contentPane, layout, _inputField, 0, 34, 1, 1, 1.0, 0.0);
		
		_f.setBackground(Color.LIGHT_GRAY);
		
	}
	
	
	/**
	 * helping method to ease the use of the GridBagLayout
	 * 
	 * @param cont		the container to store the component in
	 * @param gbl		the GridBagLayout to use
	 * @param c			the Component to store in the Container
	 * @param x			the x coordinate within the grid to place it too
	 * @param y			the y coordinate within the grid to place it too
	 * @param width		the number of x grids for the Component to allocate to it
	 * @param height 	the number of y grids for the Component to allocate to it
	 * @param weightx	a weightfactor for resizeingoperations on the component
	 * @param weighty	a weightfactor for resizeingoperations on the component
	 */
	private void addComponent( Container cont, 
			GridBagLayout gbl, 
			Component c, 
			int x, int y, 
			int width, int height, 
			double weightx, double weighty ) 
	{ 
		GridBagConstraints gbc = new GridBagConstraints(); 
		gbc.fill = GridBagConstraints.BOTH; 
		gbc.gridx = x; gbc.gridy = y; 
		gbc.gridwidth = width; gbc.gridheight = height; 
		gbc.weightx = weightx; gbc.weighty = weighty; 
		gbl.setConstraints( c, gbc ); 
		cont.add( c ); 
	} 
	
	
	@Override
	/**
	 * Starting method used by the LifeCycle class
	 * 
	 * @author Martin Loeffelholz
	 */
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
	/**
	 * Stopping method used by the LifeCycle class
	 * 
	 * @author Martin Loeffelholz
	 */
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
	/**
	 * Cleanup method used by the LifeCycle class
	 * 
	 * @author Martin Loeffelholz
	 */
	public void doCleanup() throws Exception {
		super.doCleanup();
		_f = null;
		_cBean = null;
		log.debug("Cleanup was finished");
	}
	

	
}
