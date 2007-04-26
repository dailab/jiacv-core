package de.dailab.jiactng.agentcore.comm;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;

import org.apache.activemq.pool.ConnectionPool;

import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * Ein JMS Sender für Jiac Tng.. Soll aber Springkonform sein, d.h. öffentliche Properties, die als gesetzt angesehen
 * werden können. Es können aber nur primitive Typen gesetzt werden, so dass zusätzlich eine Init-Methode aufgerufen
 * werden muss, die den Rest initialisiert. Tendenziell mehr globale variablen, weniger flexible Methoden (weniger
 * Params)
 * 
 * @author janko
 */
public class QueueSender implements IJiacSender {
	ConnectionPool _connectionPool;
	ConnectionFactory _connectionFactory;
	Destination _destination;
	String _destinationName;

	Session _session = null;
	Connection _connection = null;
	String _debugId;
	/**
	 * Die default Absender-Destination.. sollte eine Queue mit namen der platform sein. Frage: wer setzt sie?
	 */
	Destination _defaultReplyDestination;

	int _defaultTimeout = 1000;

	public QueueSender(ConnectionFactory connectionFactory, String queueName) {
		_connectionFactory = (QueueConnectionFactory)connectionFactory;
		_destinationName = queueName;
		doInit();
	}
	
	public void doInit() {
		try {
			System.out.println("Sender.init");
			_connection = _connectionFactory.createConnection();
			_session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			_destination = createQueue(_destinationName);
			_defaultReplyDestination = _destination;
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	private Queue createQueue(String queueName) throws JMSException {
		_destinationName = queueName;
		Queue queue = _session.createQueue(queueName);
		_destination = queue;
		return queue;
	}
	
	/**
	 * Verschickt per JMS eine JIAC-Nachricht.
	 * 
	 * @param message die Nachricht
	 * @param destinationName eine im JNDI hinterlegte Destination
	 * @param timeToLive in ms
	 */
	public void sendMessage(IJiacMessage message, Destination replyToDestination, long timeToLive) {
		try {
			sendMessage(message, _destination, replyToDestination, timeToLive);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Verschickt per JMS eine JIAC-Nachricht.
	 * 
	 * @param message die Nachricht
	 * @param destinationName eine im JNDI hinterlegte Destination
	 * @param timeToLive in ms
	 */
	public void send(IJiacMessage message) {
		try {
			sendMessage(message, _destination, _defaultReplyDestination, _defaultTimeout);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Verschickt per JMS eine JIAC-Nachricht.
	 * 
	 * @param message die Nachricht
	 * @param destinationName eine im JNDI hinterlegte Destination
	 * @param timeToLive in ms
	 */
	public void send(IJiacMessage message, String destinationName) {
		try {
			sendMessage(message, createQueue(destinationName), _defaultReplyDestination, _defaultTimeout);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * Verschickt per JMS eine JIAC-Nachricht.
	 * 
	 * @param message die Nachricht
	 * @param destinationName eine im JNDI hinterlegte Destination
	 * @param timeToLive in ms
	 */
	public void send(IJiacMessage message, Destination destination) {
		try {
			sendMessage(message, destination, _defaultReplyDestination, _defaultTimeout);
			_destination = destination;
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * Verschickt per JMS eine JIAC-Nachricht.
	 * 
	 * @param message die Nachricht
	 * @param destinationName eine im JNDI hinterlegte Destination
	 * @param replyToDestination
	 * @param timeToLive in ms
	 */
	public void sendMessage(IJiacMessage message, Destination destination, Destination replyToDestination, long timeToLive) {
		dbgLog("sending... to " + destination.toString() + " / '" + message.getEndPoint().toString() + "'");
		MessageProducer producer = null;
		try {
			dbgLog(" schicke JiacMsg ab:\n" + message.toString());
			producer = _session.createProducer(destination);
			producer.setTimeToLive(timeToLive);
			ObjectMessage objectMessage = _session.createObjectMessage(message);
			objectMessage.setStringProperty(Constants.ADDRESS_PROPERTY, message.getJiacDestination());
			System.out.println(Constants.ADDRESS_PROPERTY + "===" + message.getJiacDestination());
			if (replyToDestination == null) {
				replyToDestination = _defaultReplyDestination;
			}
			objectMessage.setJMSReplyTo(replyToDestination);
			dbgLog(" verpackt in dieser ObjectMsg:\n" + objectMessage.toString());
			producer.send(objectMessage);
		} catch (JMSException e) {
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			Util.closeAll(null, null, producer, null);
		}
		dbgLog("Sending done");
	}

	private void dbgLog(String text) {
		System.out.println("[QueueSender:"+_destination.toString()+"]>>> "+text);
	}
	
	public Destination getDefaultReplyDestination() {
		return _defaultReplyDestination;
	}

	public void setDefaultReplyDestination(Destination defaultReplyDestination) {
		_defaultReplyDestination = defaultReplyDestination;
	}

	public Connection getConnection() {
		return _connection;
	}

	public void setConnection(Connection _connection) {
		this._connection = _connection;
	}

	public ConnectionFactory getConnectionFactory() {
		return _connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory factory) {
		_connectionFactory = factory;
	}

	public ConnectionPool getConnectionPool() {
		return _connectionPool;
	}

	public void setConnectionPool(ConnectionPool pool) {
		_connectionPool = pool;
	}

	public Session getSession() {
		return _session;
	}

	public void setSession(Session _session) {
		this._session = _session;
	}

	public String getDestinationName() {
		return _destinationName;
	}

	public void setDestinationName(String destinationName) {
		_destinationName = destinationName;
	}

	public String getDebugId() {
		return _debugId;
	}

	public void setDebugId(String debugId) {
		_debugId = debugId;
	}

}
