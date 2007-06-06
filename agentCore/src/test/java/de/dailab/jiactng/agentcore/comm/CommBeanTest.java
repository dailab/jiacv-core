package de.dailab.jiactng.agentcore.comm;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Destination;

import junit.framework.TestCase;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.message.IEndPoint;
import de.dailab.jiactng.agentcore.comm.message.IJiacContent;
import de.dailab.jiactng.agentcore.comm.helpclasses.HelpListener;
import de.dailab.jiactng.agentcore.comm.helpclasses.TestContent;

public class CommBeanTest extends TestCase {
//	private static String username = ActiveMQConnection.DEFAULT_USER;
//	private static String password = ActiveMQConnection.DEFAULT_PASSWORD;
//	private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
//	private static boolean transacted = false;
//	private static ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(username, password, url);
//	
//	private static CommBean cBean = new CommBean();
//	private static CommBean cBean2 = new CommBean();
//	private static CommBean cBean3 = new CommBean();
//	
//	private static HelpListener listener = new HelpListener();
//	
//	private static Connection connection;
//	private static Session session; 
//	
//	private static JiacMessage testMessage; 
//	private static String operation = "Operation";
//	private static IJiacContent payload = new TestContent();
//	private static IEndPoint recipient;
//	private static IEndPoint startPoint;
//	private static Destination sender;
//	//= new JiacMessage(operation, payload, recipient, cBean.getAddress() , Destination sender);
//    boolean setupDone = false;
//    boolean allTestsDone = false;
//    
//    
//	protected void setUp() throws Exception {		
//		super.setUp();
//		if (!setupDone) {
//			connection = connectionFactory.createConnection();
//			connection.start();
//			session = connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
//			
//			cBean.setDefaultQueueName("DefaultQueue"); // hat keine Auswirkungen, tatsächliche "DefaultQueue" ist cBean.getAdress().toString();
//			cBean.setDefaultTopicName("DefaultTopic");
//			cBean.setConnectionFactory(connectionFactory);
//			cBean.setAgentNodeName("cBean");
//			cBean.doInit();
//			
//			operation = "TestMessage";
//			startPoint = cBean.getAddress();
//			recipient = startPoint;
//			sender = session.createQueue(cBean.getAddress().toString());
//
//		}
//	}
//
//	protected void tearDown() throws Exception {
//		super.tearDown();
//		if (allTestsDone){
//			session.close();
//			connection.close();
//		}
//	}
	
	public void testSimple() {
//		cBean.addCommMessageListener(listener);
//		testMessage = new JiacMessage(operation, payload, recipient, startPoint, sender);
//		cBean.send(testMessage);
//		try {
//			Thread.sleep(200);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		System.err.println("Nachrichten: " + listener.getQueueMessages());
//		assertEquals("MessageCountTest", 1, listener.getQueueSize());
////		List<Message> messages = listener.getMessages();
////		Message msg = listener.getLastMessage();
//		allTestsDone = true;
	}

	public void testMessageReceivedFromQueue() {
//		fail("Not yet implemented");
	}

	public void testMessageReceivedFromTopic() {
//		fail("Not yet implemented");
	}

	public void testSendIJiacMessage() {
//		fail("Not yet implemented");
	}

	public void testSendIJiacMessageString() {
//		fail("Not yet implemented");
	}

	public void testSendStringIJiacContentIEndPoint() {
//		fail("Not yet implemented");
	}

	public void testPublishIJiacMessage() {
//		fail("Not yet implemented");
	}

	public void testPublishStringIJiacContentIEndPoint() {
//		fail("Not yet implemented");
	}

}
