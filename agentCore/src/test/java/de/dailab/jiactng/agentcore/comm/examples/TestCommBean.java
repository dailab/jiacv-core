package de.dailab.jiactng.agentcore.comm.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.comm.broker.BrokerValues;
import de.dailab.jiactng.agentcore.comm.broker.JmsBrokerAMQ;
import de.dailab.jiactng.agentcore.comm.helpclasses.TestContent;
import de.dailab.jiactng.agentcore.comm.message.EndPoint;
import de.dailab.jiactng.agentcore.comm.message.EndPointFactory;
import de.dailab.jiactng.agentcore.comm.message.IJiacContent;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.jms.JMSMessageTransport;


/**
 * TestCommBeans is a simple example for communicating over queues and topics using a CommBeanV2
 * 
 * Example out of order until further notice!
 * 
 * @author Loeffelholz
 *
 */

public class TestCommBean implements MessageListener {

	private static String username = ActiveMQConnection.DEFAULT_USER;
	private static String password = ActiveMQConnection.DEFAULT_PASSWORD;
	private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
	private static ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(username, password, url);
	
	private static Connection connection;
	private static Session session; 
	
	BrokerValues values = BrokerValues.getDefaultInstance();
	JmsBrokerAMQ broker = new JmsBrokerAMQ();
	
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
		
		JMSMessageTransport cBean = new JMSMessageTransport();
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
		log.debug("Setting up topicChannel to " + topicName);
		log.debug("Setting up listenChannel to " + cBeanName);
		cBean = setupCommBean(cBean, cBeanName);
		
//		listenChannel = cBean.receive(this, cBeanName, false, null);
//		topicChannel = cBean.receive(this, topicName, true, null);
		
		System.out.println("type \"quit\" to terminate the chat");
		while(isTalking){
			log.debug("Let's begin with the Chat");
			System.out.println("Bitte etwas eingeben ({[t.Name]/[q.Name]}.Nachricht)");
			try {
				input = keyboard.readLine();
			} catch (IOException e) {
				log.error(e.getCause());
			}
			
			if (0 == input.toString().compareToIgnoreCase("quit")){
				isTalking = false;
				System.out.println("Kommunikation wird beendet");
				
				payload = new TestContent(cBeanName + " meldet sich ab.");
//				JiacMessage jMessage = new JiacMessage(payload, listenChannel);
//				cBean.send(jMessage, topicChannel);
			
			} else if ( ((input.startsWith("t.") || (input.startsWith("q.")))) && (input != "") ){
				boolean isValidInput = false;
				
				topic = targetIsTopic(input);
				text = input.substring(input.indexOf(".") + 1);
				if (text.contains(".")){
					targetName = text.substring(0, text.indexOf("."));
					text = text.substring(text.indexOf(".") + 1);
					payload = new TestContent(text);
//					JiacMessage jMessage = new JiacMessage(payload, listenChannel);
//					cBean.send(jMessage, targetName, topic);
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

	
	private JMSMessageTransport setupCommBean(JMSMessageTransport cBean, String cBeanName){
		log.debug("Setting up CommBean");
		// CommBean Setup
		cBean.setConnectionFactory(connectionFactory);
//		cBean.setAgentNodeName(cBeanName);
		try {
			cBean.doInit();
		} catch (Exception e) {
			log.error(e.getCause());
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
				log.error(e.getCause());
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
			broker.doStop();
			broker.doCleanup();
		} catch (Exception e) {
			log.error(e.getCause());
		}
		
		log.debug("Cleanup completed");
	}
}
