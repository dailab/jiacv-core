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
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;

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
			_defaultReplyDestination = _destination; // wirklich Sinnvoll?
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	int _defaultTimeout = 1000;

	private Queue createQueue(String queueName) throws JMSException {
		_destinationName = queueName;
		Queue queue = _session.createQueue(queueName);
		_destination = queue;
		return queue;
	}
	
	public Destination getReplyToDestination() {
		return _destination;
	}
	
	/**
	 * Verschickt per JMS eine JIAC-Nachricht.
	 * 
	 * @param message the message to send
	 * @param replyToDestination a destination from within the JNDI to which to reply to. If null the startPoint is used.
	 * @param timeToLive in ms
	 */
	public void sendMessage(IJiacMessage message, Destination replyToDestination, long timeToLive) {
		Destination destination = _destination;
		
		// Check if EndPoint is set and create Destination to it.
		if (message.getEndPoint() != null){
			try {
				destination = _session.createQueue(message.getEndPoint().toString());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		
		if (replyToDestination == null){
			// Check if StartPoint is set and create Homedestination to reply to
			if (message.getStartPoint() != null){
				try {
					replyToDestination = _session.createQueue(message.getStartPoint().toString());
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		try {
			sendMessage(message, destination, replyToDestination, timeToLive);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Verschickt per JMS eine JIAC-Nachricht.
	 * Sollte kein Empfänger oder Absender angegeben sein, werden die Lücken durch Defaults gefüllt.
	 * 
	 * @param message die Nachricht
	 */
	public void send(IJiacMessage message) {
		Destination destination = _destination;
		Destination replyToDest = _defaultReplyDestination;
		
		// Check if EndPoint is set and create Destination to it.
		if (message.getEndPoint() != null){
			try {
				destination = _session.createQueue(message.getEndPoint().toString());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		
		// Check if StartPoint is set and create Homedestination to reply to
		if (message.getStartPoint() != null){
			try {
				replyToDest = _session.createQueue(message.getStartPoint().toString());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		
		// tries to send the message
		try {
			sendMessage(message, destination, replyToDest, _defaultTimeout);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Verschickt per JMS eine JIAC-Nachricht.
	 * 
	 * @param message die Nachricht
	 * @param destinationName eine im JNDI hinterlegte Destination
	 */
	public void send(IJiacMessage message, String destinationName) {
		Destination destination = _destination;
		Destination replyToDest = _defaultReplyDestination;
		
		// Check if destinationName is set
		if ((destinationName == null) || (destinationName == "")){
			// if destinationName isn't set extract address from message
			// Check if EndPoint is set and create Destination to it.
			if (message.getEndPoint() != null){
				try {
					destination = _session.createQueue(message.getEndPoint().toString());
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		} else {
			// if destinationName is set create Destination from it
			try {
				destination = _session.createQueue(destinationName);
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		
		// Check if StartPoint is set and create Homedestination to reply to
		if (message.getStartPoint() != null){
			try {
				replyToDest = _session.createQueue(message.getStartPoint().toString());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * At last send the message.
		 * destination created from destinationName if possible. 
		 * 	Otherwise retrieved from the message if possible
		 * 	Otherwise retrieved from Defaultvalues of this Sender
		 * replyToDest checked out of message if possible
		 * 	Otherwise retrieved from Defaultvalues of this Sender
		 */
		try {
			sendMessage(message, destination, replyToDest, _defaultTimeout);
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
		// if destination == null get it from the message or the defaults
		if (destination == null){
			if (message.getEndPoint() != null){
				try {
					destination = _session.createQueue(message.getEndPoint().toString());
				} catch (JMSException e) {
					e.printStackTrace();
				}
			} else {
				destination = _destination;
			}
		}
		
		Destination replyToDest = _defaultReplyDestination;
				
		// Check if StartPoint is set and create Homedestination to reply to
		if (message.getStartPoint() != null){
			try {
				replyToDest = _session.createQueue(message.getStartPoint().toString());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		
		try {
			sendMessage(message, destination, replyToDest, _defaultTimeout);
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
		// if parameterDestinations == null, try to get destinations from message
		// if messageDestiatnions == null too, use defaultValues
		if (destination == null){
			destination = _destination;
			if (message.getEndPoint() != null){
				try {
					destination = _session.createQueue(message.getEndPoint().toString());
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
		System.err.println("Destination: " + destination);
		if (replyToDestination == null){
			replyToDestination = _defaultReplyDestination;
			if (message.getStartPoint() != null){
				try {
					replyToDestination = _session.createQueue(message.getStartPoint().toString());
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
		
		dbgLog("sending... to " + destination.toString() + " / '" + message.getEndPoint().toString() + "'");
		MessageProducer producer = null;
		try {
			dbgLog(" schicke JiacMsg ab:\n" + message.toString());
			producer = createProducer(destination);
			producer.setTimeToLive(timeToLive);
			ObjectMessage objectMessage = _session.createObjectMessage(message);
			objectMessage.setStringProperty(Constants.ADDRESS_PROPERTY, message.getJiacDestination());
			System.out.println(Constants.ADDRESS_PROPERTY + "===" + message.getJiacDestination());
			// wird oben nun miterledigt.
//			if (replyToDestination == null) {
//				replyToDestination = _defaultReplyDestination;
//			} 
			objectMessage.setJMSReplyTo(replyToDestination);
			objectMessage.setJMSDestination(destination);

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
	
	protected MessageProducer createProducer(Destination destination){
		MessageProducer producer = null;
		try {
			producer = _session.createProducer(destination);
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return producer;
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
