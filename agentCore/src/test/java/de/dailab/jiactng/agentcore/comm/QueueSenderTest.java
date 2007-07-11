package de.dailab.jiactng.agentcore.comm;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import de.dailab.jiactng.agentcore.comm.broker.BrokerValues;
import de.dailab.jiactng.agentcore.comm.broker.JmsBrokerAMQ;
import de.dailab.jiactng.agentcore.comm.helpclasses.MessageProducerShunt;
import de.dailab.jiactng.agentcore.comm.helpclasses.TestContent;
import de.dailab.jiactng.agentcore.comm.message.IJiacContent;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.message.EndPoint;
import de.dailab.jiactng.agentcore.comm.message.EndPointFactory;
import de.dailab.jiactng.agentcore.comm.message.IEndPoint;


import junit.framework.TestCase;

public class QueueSenderTest extends TestCase {

	private static String _username = ActiveMQConnection.DEFAULT_USER;
	private static String _password = ActiveMQConnection.DEFAULT_PASSWORD;
	private static String _url = ActiveMQConnection.DEFAULT_BROKER_URL;
	private static boolean transacted = false;
	private static ActiveMQConnectionFactory _connectionFactory = new ActiveMQConnectionFactory(_username, _password, _url);
	private static Connection _connection;
	private static Session _session;
	
	private static String _defaultQueueName = "DefaultQueueName";
	private static String _testNodeName = "testNode";
	private static long _defaultTimeOut = 250;
	
	private static TestingQueueSender _queueSender;
	private static MessageProducerShunt _producer;
	
	private static String _operation = "Operation";
	private static IJiacContent _payload = new TestContent();
	private static IEndPoint _startPoint;
	private static IEndPoint _recipient;
	
	private static JmsBrokerAMQ broker = new JmsBrokerAMQ();
    private static BrokerValues brokerValues = BrokerValues.getDefaultInstance();
	
	private static boolean _setupDone = false;
	private static boolean _allTestsDone = false;
	
	private static String ParamDestination = "ParamDestination";
	private static String ParamReplyTo = "ParamReplyTo";
	
	private static Destination paramRecipient = null;
	private static Destination messageRecipient = null;
	private static Destination receivedRecipient = null;
	
	private static Destination paramStartPoint = null;
	private static Destination messageReplyTo = null;
	private static Destination receivedReplyTo = null;

	// As JiacMessages now have guaranteed endpoints defaultQueue should never be used.
//	Destination defaultQueue = null;
	
	private static JiacMessage testMessage = null;
	private static JiacMessage receivedTestMessage = null;
	private static ObjectMessage receivedMessage;
	
	
	
	private class TestingQueueSender extends QueueSender{
		
		private Destination sendToDestination;
//		private long timeToLive;
		
		public TestingQueueSender(ConnectionFactory connectionFactory, String defaultQueueName){
			super(connectionFactory, defaultQueueName);
		}
		
		protected MessageProducer createProducer(Destination destination){
			sendToDestination = destination;
			return _producer;
		}
		
		public Destination getSendToDestination(){
			return sendToDestination;
		}
		
		
	}
	
	
	protected void setUp() throws Exception {
		super.setUp();
		if (!_setupDone){
			broker.values = brokerValues;
			broker.doInit();
			broker.doStart();
			
			_producer = new MessageProducerShunt();
			_queueSender = new TestingQueueSender(_connectionFactory, _defaultQueueName);
			_queueSender.doInit();
			
			_startPoint = (EndPoint) EndPointFactory.createEndPoint(_testNodeName);
			_recipient = (EndPoint) EndPointFactory.createEndPoint(_testNodeName);
			
			// Connection and Session Setup
			_connection = _connectionFactory.createConnection();
			_connection.start();
			_session = _connection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
			
			try {
				paramRecipient = _session.createQueue(ParamDestination);
				paramStartPoint = _session.createQueue(ParamReplyTo);
				
				messageRecipient = _session.createQueue(_recipient.toString());
				messageReplyTo   = _session.createQueue(_startPoint.toString());
				
//				defaultQueue = _session.createQueue(_defaultQueueName);
			} catch (JMSException e1) {
				e1.printStackTrace();
			}
						
			_setupDone = true;
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (_allTestsDone){
			broker.doStop();
			broker.doCleanup();
		}
	}


	
	public void testSendMessageIJiacMessageDestinationDestinationLong() {
		/*
		 * Test with Defaults, Parameters and Messageproperties set.
		 */
		
		testMessage = new JiacMessage(_operation, _payload, _recipient, _startPoint, null);
		
		// send Message and catch it on the fly
		_queueSender.sendMessage(testMessage, paramRecipient, (Destination) paramStartPoint, _defaultTimeOut);
		receivedMessage = (ObjectMessage) _producer.getMessage();
		
		
		// read Properties from caught Message
		receivedRecipient = _queueSender.getSendToDestination();
		
		
		try {
			receivedReplyTo = receivedMessage.getJMSReplyTo();
			receivedTestMessage = (JiacMessage) receivedMessage.getObject();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
		// Now let's see if all is included
		assertEquals("AllSetTest", paramRecipient, receivedRecipient);
		assertEquals("AllSetTest", paramStartPoint, receivedReplyTo);
		assertEquals("AllSetTest", testMessage, receivedTestMessage);
		
		
		/*
		 * Test with Defaults and Messageproperties set but lacking of Parameters.
		 */
		
		testMessage = new JiacMessage(_operation, _payload, _recipient, _startPoint, null);
		
		// send Message and catch it on the fly
		_queueSender.sendMessage(testMessage, null, null, _defaultTimeOut);
		receivedMessage = (ObjectMessage) _producer.getMessage();
		
		
		// read Properties from caught Message
		receivedRecipient = _queueSender.getSendToDestination();
		
		
		try {
			receivedReplyTo = receivedMessage.getJMSReplyTo();
			receivedTestMessage = (JiacMessage) receivedMessage.getObject();
			
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
		
		
		// Now let's see if all is included
		assertEquals("NoParameterTest", messageRecipient, receivedRecipient);
		assertEquals("NoParameterTest", messageReplyTo, receivedReplyTo);
		assertEquals("NoParameterTest", testMessage, receivedTestMessage);
	}
	
	
	
	public void testsendMessageIJiacMessageDestinationLong(){
		testMessage = new JiacMessage(_operation, _payload, _recipient, _startPoint, null);
		
		//test with parameters set
		_queueSender.sendMessage(testMessage, paramStartPoint, _defaultTimeOut);
		receivedMessage = (ObjectMessage) _producer.getMessage();
		
		try {
			receivedRecipient = _queueSender.getSendToDestination();
			receivedReplyTo = receivedMessage.getJMSReplyTo();
			receivedTestMessage = (JiacMessage) receivedMessage.getObject();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
		assertEquals("AllSetTest", messageRecipient, receivedRecipient);
		assertEquals("AllSetTest", paramStartPoint, receivedReplyTo);
		assertEquals("AllSetTest", testMessage, receivedTestMessage);
		
		
		//test without startpointparameter
		_queueSender.sendMessage(testMessage, null, _defaultTimeOut);
		receivedMessage = (ObjectMessage) _producer.getMessage();
		
		try {
			receivedRecipient = _queueSender.getSendToDestination();
			receivedReplyTo = receivedMessage.getJMSReplyTo();
			receivedTestMessage = (JiacMessage) receivedMessage.getObject();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
		assertEquals("NoParameterTest", messageRecipient, receivedRecipient);
		assertEquals("NoParameterTest", messageReplyTo, receivedReplyTo);
		assertEquals("NoParameterTest", testMessage, receivedTestMessage);
		
	}
	
	public void testSendJiacMessage(){
		/*
		 * Test with Defaults, Parameters and Messageproperties set.
		 */
		
		testMessage = new JiacMessage(_operation, _payload, _recipient, _startPoint, null);
		
		// send Message and catch it on the fly
		_queueSender.send(testMessage);
		receivedMessage = (ObjectMessage) _producer.getMessage();
		
		
		// read Properties from caught Message
		receivedRecipient = _queueSender.getSendToDestination();
		
		
		try {
			receivedReplyTo = receivedMessage.getJMSReplyTo();
			receivedTestMessage = (JiacMessage) receivedMessage.getObject();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
		// Now let's see if all is included
		assertEquals("AllSetTest", messageRecipient, receivedRecipient);
		assertEquals("AllSetTest", messageReplyTo, receivedReplyTo);
		assertEquals("AllSetTest", testMessage, receivedTestMessage);
	}
	
	
	public void testSendJiacMessageString(){
		/*
		 * Test with Defaults, Parameters and Messageproperties set.
		 */
		
		testMessage = new JiacMessage(_operation, _payload, _recipient, _startPoint, null);
		
		// send Message and catch it on the fly
		_queueSender.send(testMessage, ParamDestination);
		receivedMessage = (ObjectMessage) _producer.getMessage();
		
		
		// read Properties from caught Message
		receivedRecipient = _queueSender.getSendToDestination();
		
		
		try {
			receivedReplyTo = receivedMessage.getJMSReplyTo();
			receivedTestMessage = (JiacMessage) receivedMessage.getObject();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
		// Now let's see if all is included
		assertEquals("AllSetTest", paramRecipient, receivedRecipient);
		assertEquals("AllSetTest", messageReplyTo, receivedReplyTo);
		assertEquals("AllSetTest", testMessage, receivedTestMessage);
		
		/*
		 * Test without destinationname Parameter
		 */
		// send Message and catch it on the fly
		_queueSender.send(testMessage, "");
		receivedMessage = (ObjectMessage) _producer.getMessage();
		
		
		// read Properties from caught Message
		receivedRecipient = _queueSender.getSendToDestination();
		
		
		try {
			receivedReplyTo = receivedMessage.getJMSReplyTo();
			receivedTestMessage = (JiacMessage) receivedMessage.getObject();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
		// Now let's see if all is included
		assertEquals("No String Set Test", messageRecipient, receivedRecipient);
		assertEquals("No String Set Test", messageReplyTo, receivedReplyTo);
		assertEquals("No String Set Test", testMessage, receivedTestMessage);
		
	}
	
	public void testSendJiacMessageDestination(){
		/*
		 * Test with Defaults, Parameters and Messageproperties set.
		 */
		
		testMessage = new JiacMessage(_operation, _payload, _recipient, _startPoint, null);
		
		// send Message and catch it on the fly
		_queueSender.send(testMessage, paramRecipient);
		receivedMessage = (ObjectMessage) _producer.getMessage();
		
		
		// read Properties from caught Message
		receivedRecipient = _queueSender.getSendToDestination();
		
		
		try {
			receivedReplyTo = receivedMessage.getJMSReplyTo();
			receivedTestMessage = (JiacMessage) receivedMessage.getObject();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
		// Now let's see if all is included
		assertEquals("AllSetTest", paramRecipient, receivedRecipient);
		assertEquals("AllSetTest", messageReplyTo, receivedReplyTo);
		assertEquals("AllSetTest", testMessage, receivedTestMessage);
		
		/*
		 * Test without destinationname Parameter
		 */
		// send Message and catch it on the fly
		Destination nullDestination = null;
		_queueSender.send(testMessage, nullDestination);
		receivedMessage = (ObjectMessage) _producer.getMessage();
		
		
		// read Properties from caught Message
		receivedRecipient = _queueSender.getSendToDestination();
		
		
		try {
			receivedReplyTo = receivedMessage.getJMSReplyTo();
			receivedTestMessage = (JiacMessage) receivedMessage.getObject();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
		// Now let's see if all is included
		assertEquals("No String Set Test", messageRecipient, receivedRecipient);
		assertEquals("No String Set Test", messageReplyTo, receivedReplyTo);
		assertEquals("No String Set Test", testMessage, receivedTestMessage);
		
	}
		
	public void testFinished(){
		_allTestsDone = true;
	}

}
