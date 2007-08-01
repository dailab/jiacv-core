package de.dailab.jiactng.agentcore.comm.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;
import de.dailab.jiactng.agentcore.comm.broker.BrokerValues;
import de.dailab.jiactng.agentcore.comm.broker.JmsBrokerAMQ;
import de.dailab.jiactng.agentcore.comm.helpclasses.TestContent;
import de.dailab.jiactng.agentcore.comm.message.IJiacContent;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport.IMessageTransportDelegate;
import de.dailab.jiactng.agentcore.comm.transport.jms.JMSMessageTransport;


/**
 * TestCommBeans is a simple example for communicating over queues and topics using a CommBeanV2
 * 
 * Example out of order until further notice!
 * 
 * @author Loeffelholz
 *
 */

public class TestCommBean implements IMessageTransportDelegate {

	private static String username = ActiveMQConnection.DEFAULT_USER;
	private static String password = ActiveMQConnection.DEFAULT_PASSWORD;
	private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
	private static ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(username, password, url);

	BrokerValues values = BrokerValues.getDefaultInstance();
	JmsBrokerAMQ broker = new JmsBrokerAMQ();
	
	Log log = LogFactory.getLog(getClass());
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		TestCommBean tcb = new TestCommBean();
		tcb.run();
	}
	
	public void run() throws Exception {
		log.debug("begin of demonstration");
		
		IJiacContent payload = new TestContent();
		
		JMSMessageTransport cBean = new JMSMessageTransport();
		IMessageBoxAddress messageBoxAddress = null;
		IGroupAddress groupAddress = null;
		
		String cBeanName = "";
		String groupName = "";
		
		BufferedReader keyboard = new BufferedReader (new InputStreamReader (System.in));
		String input = "";
		String text = "";
		String targetName = "";
		boolean isGroup = false;
		boolean isTalking = true;
		
		
		System.out.println("Bitte einen Namen fuer die CommBean eingeben");
		try {
			input = keyboard.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		cBeanName = input.toLowerCase();
		
		// Topic
		System.out.println("Bitte den Namen der Gruppe eingeben");
		try {
			input = keyboard.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		groupName = input.toLowerCase();
		
		doInit();
		log.debug("Setting up groupAddress to " + groupName);
		log.debug("Setting up messageBoxAddress to " + cBeanName);
        groupAddress= CommunicationAddressFactory.createGroupAddress(groupName);
        messageBoxAddress= CommunicationAddressFactory.createMessageBoxAddress(cBeanName);
        
		cBean = setupCommBean(cBean, groupAddress, messageBoxAddress);
		
		System.out.println("type \"quit\" to terminate the chat");
		while(isTalking){
			log.debug("Let's begin with the Chat");
			System.out.println("Bitte etwas eingeben ({[g.Name]/[m.Name]}.Nachricht)");
			try {
				input = keyboard.readLine();
			} catch (IOException e) {
				log.error(e.getCause());
			}
			
			if (0 == input.toString().compareToIgnoreCase("quit")){
				isTalking = false;
				System.out.println("Kommunikation wird beendet");
				
				payload = new TestContent(cBeanName + " meldet sich ab.");
				JiacMessage jMessage = new JiacMessage(payload, messageBoxAddress);
				cBean.send(jMessage, groupAddress);
			
			} else if ( ((input.startsWith("g.") || (input.startsWith("m.")))) && (input != "") ) {
				boolean isValidInput = false;
				
				isGroup = targetIsGroup(input);
				text = input.substring(input.indexOf(".") + 1);
				if (text.contains(".")){
					targetName = text.substring(0, text.indexOf("."));
					text = text.substring(text.indexOf(".") + 1);
					payload = new TestContent(text);
					JiacMessage jMessage = new JiacMessage(payload, messageBoxAddress);
					cBean.send(
                        jMessage,
                        isGroup ? CommunicationAddressFactory.createGroupAddress(targetName) 
                                : CommunicationAddressFactory.createMessageBoxAddress(targetName));
				}
			}
			
			
		}
		
		cBean.doCleanup();
		doCleanup();
		System.out.println("Bye bye");
	}
	
	private boolean targetIsGroup(String input){
		return (input.substring(0, input.indexOf(".")).compareToIgnoreCase("g") == 0);
	}
	
	public void doInit(){
		// Broker Setup

		broker.setValues(values);
		broker.setLog(log);
		try {
			broker.doInit();
			broker.doStart();
		} catch (Exception e1) {
			log.error(e1.getCause());
		}
		
		
		// Connection and Session Setup
		log.debug("initializing connection");
	}

	
	private JMSMessageTransport setupCommBean(JMSMessageTransport cBean, IGroupAddress group, IMessageBoxAddress messageBox) {
		log.debug("Setting up CommBean");
		cBean.setConnectionFactory(connectionFactory);
		try {
            cBean.setDefaultDelegate(this);
			cBean.doInit();
            cBean.listen(group, null);
            cBean.listen(messageBox, null);
		} catch (Exception e) {
			log.error(e.getCause());
		}
		return cBean;
	}
    
	public Log getLog(String extension) {
        return LogFactory.getLog(getClass().getName() + "." + extension);
    }

    public void onAsynchronousException(MessageTransport source, Exception e) {
        log.error("caught asynchronous exception", e);
    }

    public void onMessage(MessageTransport source, IJiacMessage message, ICommunicationAddress at) {
        IJiacContent content = message.getPayload();
        if (content instanceof TestContent){
            TestContent tContent = (TestContent) content;
            System.err.println("Incoming over " + at);
            System.err.println("Message from " + message.getSender() + " reads:");
            System.err.println(tContent.getContent());
        }
        
    }

	public void doCleanup(){
		log.debug("Commencing Cleanup");
		
		try {
			broker.doStop();
			broker.doCleanup();
		} catch (Exception e) {
			log.error(e.getCause());
		}
		
		log.debug("Cleanup completed");
	}
}
