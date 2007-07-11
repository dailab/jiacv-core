package de.dailab.jiactng.agentcore.comm;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import org.apache.activemq.pool.ConnectionPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * Klasse, die in eine Topic schreibt - properties müssen gesetzt, dann doInit() aufgerufen werden.
 * 
 * @author janko
 */
public class TopicSender implements IJiacSender {
	Log log = LogFactory.getLog(getClass());
	ConnectionPool _connectionPool;
	ConnectionFactory _connectionFactory;
	Topic _topic;
	String _topicName;
	Session _session = null;
	Connection _connection = null;

	String _debugId;

	/**
	 * Die default Absender-Destination.. sollte eine Queue mit namen der platform sein. Frage: wer setzt sie?
	 */
	Destination _defaultReplyDestination;
	int _defaultTimeout = 1000;

	public TopicSender(ConnectionFactory connectionFactory, String topicName) {
		_connectionFactory = (TopicConnectionFactory) connectionFactory;
		_topicName = topicName;
		doInit();
	}

	public void doInit() {
		try {
			log.debug("TopicSender.init");
			_connection = _connectionFactory.createConnection();
			_session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			_topic = createTopic(_topicName);
		} catch (Exception e) {
			log.error(e.getStackTrace());
		}
	}

	private Topic createTopic(String topicName) {
		Topic topic = null;
		try {
			_topicName = topicName;
			topic = _session.createTopic(_topicName);
			_topic = topic;
		} catch (JMSException e) {
			e.printStackTrace(System.err);
		}
		return topic;
	}

	public Destination getReplyToDestination() {
		return _topic;
	}
	
	public void send(IJiacMessage message) {
		sendToTopic(message, null);
	}

	/**
	 * Sendet in angegeben Topic und setzt die instanzvariable auf dieses Topic
	 */
	public void send(IJiacMessage message, String topicName) {
		createTopic(topicName);
		sendToTopic(message, null);
	}

	/**
	 * Sendet in angegeben Topic und setzt die instanzvariable auf dieses Topic
	 */
	public void send(IJiacMessage message, Destination destination) {
		_topic = (Topic) destination;
		sendToTopic(message, null);
	}

	/**
	 * Verschickt eine Nachricht an ein Topic (im ggs zu einer Queue) _destination muss vorher gesetzt worden sein
	 * 
	 * @param message
	 * @param platformId
	 */
	public void sendToTopic(IJiacMessage message, Properties props) {
		TopicPublisher publisher = null;
		try {
			dbgLog("sending msg to Topic:'" + message.getPayload().toString() + "'");

			_session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Hier wird entschieden, dass es sich um nen TopicPublisher handelt
			publisher = (TopicPublisher) createProducer(_topic);
//			publisher = ((TopicSession) _session).createPublisher(_topic);
			// publisher.setTimeToLive(_topicTimeToLive);
			publisher.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			ObjectMessage msg = _session.createObjectMessage(message);
			Util.setProperties(msg, props);
			publisher.publish(msg);
		} catch (JMSException e) {
			log.error(">>>> Exception occurred: " + e);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			Util.closeAll(null, null, null, publisher);
		}
	}

	protected MessageProducer createProducer(Destination destination){
		TopicPublisher publisher = null;
		try {
			publisher = ((TopicSession) _session).createPublisher(_topic);
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return publisher;
	}
	
	private void dbgLog(String text) {
		log.debug("[TopicSender:" + getDebugId() + "]>>> " + text);
	}

	public Connection getConnection() {
		return _connection;
	}

	public void setConnection(Connection connection) {
		_connection = connection;
	}

	public ConnectionFactory getConnectionFactory() {
		return _connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		_connectionFactory = connectionFactory;
	}

	public Destination getDefaultReplyDestination() {
		return _defaultReplyDestination;
	}

	public void setDefaultReplyDestination(Destination defaultReplyDestination) {
		_defaultReplyDestination = defaultReplyDestination;
	}

	public int getDefaultTimeout() {
		return _defaultTimeout;
	}

	public void setDefaultTimeout(int defaultTimeout) {
		_defaultTimeout = defaultTimeout;
	}

	public Topic getTopic() {
		return _topic;
	}

	public void setTopic(Topic topic) {
		_topic = topic;
	}

	public String getTopicName() {
		return _topicName;
	}

	public void setTopicName(String topicName) {
		_topicName = topicName;
	}

	public String getDebugId() {
		return _debugId;
	}

	public void setDebugId(String debugId) {
		_debugId = debugId;
	}
}
