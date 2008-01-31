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

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.CommunicationBean;
import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;
import de.dailab.jiactng.agentcore.comm.message.BinaryContent;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.knowledge.IFact;


/**
 * This Bean is just building and providing a GUI for useage with the JIAC-TNG framework including the communicationBean,
 * demonstrating simple chat capabilities with a simple gui.
 * For this Bean to work properly there have to be a communicationBean within the agent that wishes to use it.
 * 
 * @author Martin Loeffelholz
 *
 */

@SuppressWarnings("serial")
public class ChatGuiBean extends AbstractAgentBean implements SpaceObserver<IFact> {

	private static final JiacMessage MESSAGE_TEMPLATE;
	private static final String MESSAGE_HEADER = "SimpleChatExample";
	
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
	
	static {
		MESSAGE_TEMPLATE = new JiacMessage();
		MESSAGE_TEMPLATE.setHeader(IJiacMessage.Header.PROTOCOL, MESSAGE_HEADER);
	}
	
	/**
	 * A class that encapsulates a textline for the list. e.g. a messagetext of a
	 * message received during runtime. It may contain informations about an address
	 * of such a message.
	 *
	 */
	private class ListElement {
		public byte[] message;
		public ICommunicationAddress from;
		public ICommunicationAddress at;
		public String fontName = _messagesReceived.getFont().getName();
		public int fontSize = _messagesReceived.getFont().getSize();
		public int fontStyle = Font.PLAIN;
		public Color fontColor = _messagesReceived.getForeground();
		
		public ListElement(){
			message = "message".getBytes();
		}
		
		public ListElement(String messageText){
			message = messageText.getBytes();
		}
		
		public ListElement(byte[] messageTextInBytes){
			this.message = messageTextInBytes;
		}
		
		public String toString(){
			StringBuilder outMessage = new StringBuilder();
			for (byte digit : message){
				outMessage.append((char) (digit & 0xFF));
			}
			
			return outMessage.toString();
		}
		
	}
	
	
	/**
	 * This CellRenderer is responsible for formating and printing listelements within the used JList
	 *
	 */
	class MessageCellRenderer implements ListCellRenderer {
		  protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

		  public Component getListCellRendererComponent(JList list, Object value, int index,
		      boolean isSelected, boolean cellHasFocus) {

			  JLabel listEntry = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
					  isSelected, cellHasFocus);
			  if (value instanceof ListElement){
				 ListElement listThat = (ListElement) value;
				 
				 listEntry.setText(listThat.toString());
				 listEntry.setFont(new Font (listThat.fontName, listThat.fontStyle, listThat.fontSize));
				 listEntry.setForeground(listThat.fontColor);
				 
			  }
			  
			  return listEntry;
		  }
	}

	/**
	 * Constructor for this class
	 * 
	 */
	public ChatGuiBean() {
		setBeanName("ChatGuiBean");
	}
	
	/**
	 * Entrypoint for messages coming from the CommunicationBean through the tuplespace.
	 * Here all messages will get processed.
	 * 
	 *  @param message an IJiacMessage received from the communicationAddress
	 *  @param at the ICommunicationAddress the messages was sent to
	 *  
	 */
	@SuppressWarnings("unchecked")
	public void notify(SpaceEvent<? extends IFact> event) {
		IJiacMessage message = null;
		ICommunicationAddress at = null;
		ICommunicationAddress from = null;
		
		if(event instanceof WriteCallEvent) {
			WriteCallEvent<IJiacMessage> wce= (WriteCallEvent<IJiacMessage>) event;
			message= memory.remove(wce.getObject());
			String sender = message.getHeader("SimpleChatExampleSender");
			String receiver = message.getHeader("SimpleChatExampleReceiver");
			from = CommunicationAddressFactory.createFromURI(sender);
			at = CommunicationAddressFactory.createFromURI(receiver);

			at = at.toUnboundAddress();
			from = from.toUnboundAddress();
			log.debug("message received from: " + from + " at " + at);

			BinaryContent binaryPayload = null;
			// Check if the payload within the message is of a supported type
			if (message.getPayload() instanceof BinaryContent){
				binaryPayload = (BinaryContent) message.getPayload();
				log.debug("Payload is instanceof BinaryContent");
			} 

			if (binaryPayload != null){
				// payload was binary
				// create ListElement to print some text into the _messagesReceived(output)field
				ListElement le = new ListElement("Received Mesage from " + from + " at " + at);
				le.from = message.getSender();
				le.at = at;
				le.fontColor = Color.red;
				le.fontStyle = Font.ITALIC;
				printLine(le);

				le = new ListElement (binaryPayload.getData());
				le.at = at;
				le.from = message.getSender();
				printLine(le);

			} else {
				// if payload within Message was of unsupported type..
				ListElement le = new ListElement("Received Message from " + from + " at " + at);
				le.fontColor = Color.red;
				le.fontStyle = Font.ITALIC;
				printLine(le);
				printLine("Content could not be decyphered!");
			}
		}
	}
	
	/**
	 * prints the given line into the JList
	 * @param line the line to print into the JList
	 * 
	 */
	private void printLine(String line){
		ListElement le = new ListElement(line);
		_messageListModel.add(_newMessageIndex++, le);
	}
	
	/**
	 * prints the given line into the JList
	 * 
	 * @param line 		the Line to put on the List
	 * @param fontStyle	the Style for the font as defined in constants of the class Font
	 * @param fontColor	a color as defined in the class Color, if set to null the default color is used
	 * 
	 */
	private void printLine(String line, int fontStyle, Color fontColor){
		ListElement le = new ListElement(line);
		
		if (fontColor != null){
			le.fontColor = fontColor;
		}
		le.fontStyle = fontStyle;
		_messageListModel.add(_newMessageIndex++, le);	
	}
	
	/**
	 * prints the given Listelement into the JList.
	 * mostly used for messagelogging purposes 
	 * 
	 * @param le the Listelement to display in the list
	 * 
	 */
	private void printLine(ListElement le){
		_messageListModel.add(_newMessageIndex++, le);
	}
	
	/**
	 * prints a list of all commands to the chatwindow
	 * 
	 */
	private void printHelp(){
		final Color helpColor = Color.YELLOW;
		printLine("type  \"pack\" to resize the window; ", Font.ITALIC, helpColor);
		printLine("type \"clear\" to clear the window", Font.ITALIC, helpColor);
		printLine("To send a Message, type \"g.<Name>.\" for a group or \"m.<Name>.\" ", Font.ITALIC, helpColor);
		printLine("for a messagebox address followed by your message.", Font.ITALIC, helpColor);
		printLine("Also see formatdescription over the inputfield.", Font.ITALIC, helpColor);
		printLine("To change your Address, type \"c.<newMessageBoxName>\"", Font.ITALIC, helpColor);
		printLine("To Listen to a new Address, type \"listen.[g|m].<AddressName>\"", Font.ITALIC, helpColor);
		printLine("To stop listen to a Address, type \"stopListen.[g|m].<AddressName>\"", Font.ITALIC, helpColor);
		printLine("To stop listen to all but your very own address, type \"stopListenAll\"", Font.ITALIC, helpColor);
		printLine("To get a List of all used Addresses, type \"ListAddresses\" ", Font.ITALIC, helpColor);
		printLine("To change the Color Theme of this chatclient, type \"ChangeColor\" ");
	}
	
	/**
	 * prints the list of all addresses the agent is listening to.
	 * 
	 */
	private void printAddressList(){
		printLine("List of Addresses Listening To:");
		ICommunicationAddress unbound = _messageBoxAddress.toUnboundAddress();
		printLine(unbound.toString());
		for (ICommunicationAddress address : addressList){
			unbound = address.toUnboundAddress();
			printLine(unbound.toString());
		}
	}
	
	/**
	 * targetaddress has to start with "g." or "m." this is checked before running this method.
	 * @param targetAddress
	 * 
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
			log.error("Tried to listen to: " + targetAddress + " but no CommunicationBean was installed");
		}
	}
	
	/**
	 * stops to listen to the given targetAddress. 
	 * @param targetAddress No messages sent to this address will get received from this address anymore
	 * 
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
				_cBean.unregister(address, MESSAGE_TEMPLATE);
				addressList.remove(address);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Stops to listen to all addresses except your main-mailbox
	 * 
	 */
	private void stopListenToAll(){
		if ((_cBean != null) && (!addressList.isEmpty())){
			for (ICommunicationAddress address : addressList){
				try {
					_cBean.unregister(address, MESSAGE_TEMPLATE);
					log.debug("ChatGuiBean of " + _beanName + "stops listen to " + address);
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
	 */
	private void sendStatusMessage(String line){
		if (_cBean != null){
			ICommunicationAddress to = CommunicationAddressFactory.createGroupAddress("all");
			BinaryContent payload = new BinaryContent(line.getBytes());
			JiacMessage jMessage = createMessage(payload, to);
			try {
				_cBean.send(
						jMessage,
						to
				); 
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
		} else {
			log.error("Tried to send Message " + line + " but no CommunicationBean installed");
		}
	}
	
	/**
	 * Sends a message out to another agent
	 * @param line	The actual message
	 * @param to	The address to send it to
	 * @param isGroup	, true if <code> to </code> is a group address
	 */
	private void sendMessage(String line, String to, boolean isGroup){
		if (_cBean != null){
			
			ICommunicationAddress toAddress = null;
			if (isGroup)
				toAddress = CommunicationAddressFactory.createGroupAddress(to);
			else
				toAddress = CommunicationAddressFactory.createMessageBoxAddress(to);
			
			BinaryContent payload = new BinaryContent(line.getBytes());
			JiacMessage jMessage = createMessage(payload, toAddress);
			try {
				_cBean.send(
						jMessage,
						toAddress
				); 
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
			log.debug("Message sent to: " + toAddress + " reads: " + line);
		} else {
			log.error("Tried to send Message " + line + " but no CommunicationBean installed");
		}
	}
	
	/**
	 * Creates a JiacMessage and sets the headers to make it possible to identify
	 * the sender of this message and the address it was originally sent to.
	 * @param payload	The actual message we want someone other to read
	 * @param to		The address the message is going to be send to
	 * @return			the message with all neccessary headers set
	 */
	private JiacMessage createMessage(IFact payload, ICommunicationAddress to){
		JiacMessage jMessage = new JiacMessage(payload);
		// This header part is neccessary to filter the message from the memory
		jMessage.setHeader(IJiacMessage.Header.PROTOCOL, MESSAGE_HEADER);
		// The headers following are for actual chatintern informations 
		jMessage.setHeader("SimpleChatExampleSender", _messageBoxAddress.toString());
		jMessage.setHeader("SimpleChatExampleReceiver", to.toString());
		return jMessage;
	}
	
	/**
	 * method to change "my own address" meaning the mailbox of this agentbean
	 * @param address the mailbox of this agentbean
	 * 
	 */
	private void changeAddress(String address){
		try {
			if (_cBean != null){
				ICommunicationAddress oldAddress = _messageBoxAddress.toUnboundAddress();
				_cBean.unregister(_messageBoxAddress, MESSAGE_TEMPLATE);
				_messageBoxAddress = CommunicationAddressFactory.createMessageBoxAddress(address);
				_cBean.register(_messageBoxAddress, MESSAGE_TEMPLATE);
				_addressLine.setText("Your Address: m." + _messageBoxAddress.toString().substring(7));
				_addressLine.validate();
				ICommunicationAddress unbound = _messageBoxAddress.toUnboundAddress();
				sendStatusMessage(oldAddress + " is now known as " + unbound.toString());
				_f.repaint();
				log.debug("Address of Chatting Agent changed " + oldAddress + " is now known as " + unbound.toString());
			} else {
				log.error("Tried to change address, but no CommunicationBean was installed");
			}
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * setter for the CommunicationBean to use for sending messages
	 * @param cBean the CommunicationBean to use for sending messages
	 * 
	 */
	public void setCommunicationBean(CommunicationBean cBean){
		_cBean = cBean;
	}
	
	/**
	 * Changes the ColorTheme between two possible versions
	 */
	public void switchColorTheme(){
		
		if (_messagesReceived.getBackground() == Color.BLUE){
			_messagesReceived.setBackground(Color.BLACK);
			_messagesReceived.setForeground(Color.GREEN);
			printLine("Color Theme changed to Smith");
		} else {
			_messagesReceived.setBackground(Color.BLUE);
			_messagesReceived.setForeground(Color.LIGHT_GRAY);
			printLine("Color Theme changed to Old Man");
		}
	}
	
	@Override
	/**
	 * Initializationmethod used by the LifeCycle class
	 * 
	 */
	public void doInit() throws Exception {
		super.doInit();
		log.debug("Initializing...");
		if (this.thisAgent != null){
			_beanName = this.thisAgent.getAgentName();
		}
		
		memory.attach(this, MESSAGE_TEMPLATE);
		
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
		_messagesReceived.setBackground(Color.BLACK);
		_messagesReceived.setForeground(Color.GREEN);
		_messagesReceived.setFixedCellHeight(14);
		_messagesReceived.setCellRenderer(_renderer);
		_messagesReceived.validate();
		
		
	    JScrollPane scrollPane = new JScrollPane(_messagesReceived);
	    scrollPane.setMinimumSize(new Dimension(100, 200));
	    scrollPane.setPreferredSize(new Dimension(100,300));
		
		_inputDescription = new JLabel("Format: [g|m].[Name of group or messagebox].[Messagetext]");
		
		_inputField = new JTextField(20);
		_inputField.setFocusCycleRoot(true);
		
		/**
		 * An ActionListener is created. Here all commands from the inputfield will be processed.
		 */
		_inputField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				String command = evt.getActionCommand();

				if (command.equalsIgnoreCase("pack")){
					_f.pack();
					
				} else if (command.equalsIgnoreCase("help")){
					printHelp();
					
				} else if (command.equalsIgnoreCase("ListAddresses")){
					printAddressList();
				} else if (command.equalsIgnoreCase("changeColor")){
					switchColorTheme();
					
				}else if (command.equalsIgnoreCase("clear")){
					_messagesReceived.clearSelection();
					_messageListModel.clear();
					_newMessageIndex = 0;
					printLine("type \"help\" for help");
					
				} else if (command.startsWith("c.")){
					changeAddress(command.substring(command.indexOf(".")+1).toLowerCase());
					
				} else if (command.startsWith("listen.g.") || command.startsWith("listen.m.")){
					listenTo(command.substring(command.indexOf(".")+1));
				
				} else if (command.startsWith("stopListen.g.") || command.startsWith("stopListen.m.")){	
					stopListenTo(command.substring(command.indexOf(".")+1));
					
				}else if (command.equalsIgnoreCase("stopListenAll")){
					stopListenToAll();
					
				} else if ( (command.startsWith("g.")) || (command.startsWith("m.")) ){
					// The user wants to sent a message either to a g(roup) or m(essagebox) address
					// Syntax used: [g|m].[name of group or messagebox].[messagetext]
					log.debug("got something to send");
					boolean isGroup;
					String text;
					String targetName;
					BinaryContent payload;

					// check if it is a group. If it doesn't it's a messagebox.
					isGroup = command.startsWith("g.");
					// cut out of the string what we allready know.
					text = command.substring(command.indexOf(".") + 1);
					if (text.contains(".")){
						// the substring uses the correct syntax
						log.debug("seems to be a valid something");
						// cut the name of the targetAddress out of the string
						targetName = text.substring(0, text.indexOf("."));
						// the rest of the string has to be the actual messagetext
						text = text.substring(text.indexOf(".") + 1);
						
						log.debug("Sending message to: " + targetName + " reads: " + text);
						
						// Let the user know what he/she just did and print the message that's going to be sent
						printLine("-- Message sent to " + targetName, Font.ITALIC, Color.RED);
						printLine("-- Message sent: " + text, Font.ITALIC, _messagesReceived.getForeground());
						// now really send the message.
						sendMessage(text, targetName, isGroup);
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
		log.debug("Initilisation finished");
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
	 */
	public void doStart() throws Exception {
		super.doStart();
		log.debug("ChatGuiBean is starting...");
		Iterator<IAgentBean> beanList = this.thisAgent.getAgentBeans().iterator();
		
		while (beanList.hasNext()){
			IAgentBean bean = beanList.next();
			if (bean instanceof CommunicationBean){
				_cBean = (CommunicationBean) bean;
				break;
			}
		}
				
		if (_cBean != null){
			log.debug("ChatGuiBean has found CommunicationBean");
			_messageBoxAddress = CommunicationAddressFactory.createMessageBoxAddress(_beanName);
			_cBean.register(_messageBoxAddress, MESSAGE_TEMPLATE);
			
			this.listenTo("g.all");
			
			_addressLine.setText("Your Address: m." + _messageBoxAddress.toString().substring(7));
			_addressLine.validate();
			
			String messageText = "m." + _messageBoxAddress.toString().substring(7) + " is ready to chat";
			sendStatusMessage(messageText);
			
			printLine("CommunicationBean installed, ready to chat");
		} else {
		 	printLine("CommunicationBean not installed!");
		}
		
		printLine("type \"help\" to get some help");
		
		_f.pack();
		_f.setVisible( true ); 
		log.debug("ChatGuiBean has started");
	}
		
	@Override
	/**
	 * Stopping method used by the LifeCycle class
	 * 
	 */
	public void doStop() throws Exception {
		super.doStop();
		log.debug("ChatGuiBean is stopping");
		
		String messageText = "m." + _messageBoxAddress.toString().substring(7) + " has left the chat";
		sendStatusMessage(messageText);
		
		log.debug("ChatGuiBean has stopped");
		_f.setVisible(false);
		_f.dispose();
	}
	
	@Override
	/**
	 * Cleanup method used by the LifeCycle class
	 * 
	 */
	public void doCleanup() throws Exception {
		super.doCleanup();
		log.debug("ChatGuiBean is cleaning up");
		log.debug("... disposing beanaddress");
		if (_messageBoxAddress != null){
			_cBean.unregister(_messageBoxAddress);
		}
		log.debug("... disposing other addresses");
		stopListenToAll();
		
		_f = null;
		_cBean = null;
		log.debug("Cleanup was finished");
	}

	
}
