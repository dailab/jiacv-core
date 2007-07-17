package de.dailab.jiactng.agentcore.comm.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.comm.CommBeanV2;
import de.dailab.jiactng.agentcore.comm.helpclasses.TestContent;
import de.dailab.jiactng.agentcore.comm.message.IJiacContent;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.message.EndPoint;
import de.dailab.jiactng.agentcore.comm.message.EndPointFactory;


/**
 * TestCommBeans is a simple example for communicating over queues and topics using a CommBeanV2
 * 
 * @author Loeffelholz
 *
 */

public class TestCommBean implements MessageListener {

	private static String username = ActiveMQConnection.DEFAULT_USER;
	private static String password = ActiveMQConnection.DEFAULT_PASSWORD;
	private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
	private static boolean transacted = false;
	private static ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(username, password, url);
	
	private static Connection connection;
	private static Session session; 
	
	Log log = LogFactory.getLog(getClass());
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestCommBean tcb = new TestCommBean();
		
		tcb.run();
	}
	
	public void run(){
		log.debug("begin of demonstration");
		
		IJiacContent payload = new TestContent();
		
		CommBeanV2 cBean = new CommBeanV2();
		Destination chatChannel = null;
		Destination listenChannel = null;
		Destination topicChannel = null;
		
		String cBeanName = "";
		String topicName = "";
		EndPoint startpoint = (EndPoint) EndPointFactory.createEndPoint("cBean");
		EndPoint receiver = (EndPoint) EndPointFactory.createEndPoint("cBean");
		
		
		BufferedReader keyboard = new BufferedReader (new InputStreamReader (System.in));
		String input = "";
		String text = "";
		String targetName = "";
		boolean topic = false;
		boolean isTalking = true;
		
		
		System.out.println("Bitte einen Namen fuer die CommBean eingeben");
		try {
			input = keyboard.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		cBeanName = input.toLowerCase();
		
		// Topic
		System.out.println("Bitte den Namen des Topics eingeben");
		try {
			input = keyboard.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		topicName = input.toLowerCase();
		
		
		
		doInit();
		topicChannel = setupChannel(topicName, true);
		log.debug("Setting up topicChannel to " + topicChannel);
		listenChannel = setupChannel(cBeanName, false);
		log.debug("Setting up listenChannel to " + listenChannel);
		cBean = setupCommBean(cBean, cBeanName);
		
		cBean.receive(this, listenChannel, null);
		cBean.receive(this, topicChannel, null);
		
		System.out.println("type \"quit\" to terminate the chat");
		while(isTalking){
			log.debug("Let's begin with the Chat");
			System.out.println("Bitte etwas eingeben ({[t.Name]/[q.Name]}.Nachricht)");
			try {
				input = keyboard.readLine();
			} catch (IOException e) {
				log.error(e.getStackTrace());
			}
			
			if (0 == input.toString().compareToIgnoreCase("quit")){
				isTalking = false;
				System.out.println("Kommunikation wird beendet");
				
				payload = new TestContent(cBeanName + " meldet sich ab.");
				JiacMessage jMessage = new JiacMessage("Chatting", payload, receiver, startpoint, listenChannel);
				cBean.send(jMessage, topicChannel);
			
			} else if ( ((input.startsWith("t.") || (input.startsWith("q.")))) && (input != "") ){
				boolean isValidInput = false;
				
				topic = targetIsTopic(input);
				text = input.substring(input.indexOf(".") + 1);
				if (text.contains(".")){
					targetName = text.substring(0, text.indexOf("."));
					text = text.substring(text.indexOf(".") + 1);
					payload = new TestContent(text);
					JiacMessage jMessage = new JiacMessage("Chatting", payload, receiver, startpoint, listenChannel);
					chatChannel = setupChannel(targetName, topic);
					cBean.send(jMessage, chatChannel);
				}
			}
			
			
		}
		
		cBean.doCleanup();
		doCleanup();
		System.out.println("Bye bye");
	}
	
	private boolean targetIsTopic(String input){
		return (input.substring(0, input.indexOf(".")).compareToIgnoreCase("t") == 0);
	}
	
	public void doInit(){
		// Connection and Session Setup
		log.debug("initializing connection");
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			log.error(e.getStackTrace());
		}
	}

	private Destination setupChannel(String channelName, boolean topic){
		log.debug("Setting up Channel");
		Destination chatChannel = null;
		try {
			if (topic)
				chatChannel = session.createTopic(channelName);
			else
				chatChannel = session.createQueue(channelName);
		} catch (JMSException e) {
			log.error(e.getStackTrace());
		}
		return chatChannel;
	}
	
	
	private CommBeanV2 setupCommBean(CommBeanV2 cBean, String cBeanName){
		log.debug("Setting up CommBean");
		// CommBean Setup
		cBean.setConnectionFactory(connectionFactory);
		cBean.setAgentNodeName(cBeanName);
		try {
			cBean.doInit();
		} catch (Exception e) {
			log.error(e.getStackTrace());
		}
		return cBean;
	}
	
	
	public void onMessage(Message message){
//		System.err.println("Message received");
		boolean topic = true;
		String origin = "";
		ObjectMessage objMessage = null;
		JiacMessage jMessage = null;
		IJiacContent content;
		TestContent tContent = new TestContent();
		
		if (message instanceof ObjectMessage){
			objMessage = (ObjectMessage) message;
			try {
				jMessage = (JiacMessage) objMessage.getObject();
				topic = (message.getJMSDestination().toString().startsWith("topic"));
				origin = message.getJMSDestination().toString().substring(8);
			} catch (JMSException e) {
				log.error(e.getStackTrace());
			}
			content = jMessage.getPayload();
			if (content instanceof TestContent){
				tContent = (TestContent) content;
				System.err.println("Incoming over " + (topic ? "topic: " : "queue: ") + origin);
				System.err.println("Message from " + jMessage.getSender().toString().substring(8) + " reads:");
				System.err.println(tContent.getContent());
			}
		}
	}
	
	
	public void doCleanup(){
		log.debug("Commencing Cleanup");
		try {
			session.close();
			connection.close();
		} catch (JMSException e) {
			log.error(e.getStackTrace());
		}
		log.debug("Cleanup completed");
	}
}
