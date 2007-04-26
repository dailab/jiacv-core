package de.dailab.jiactng.agentcore.comm;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * Ein Receiver, der mehrere consumer zulassen soll. Design-Problem: Nur eine Session.. Sind überhaupt mehrere nötig?
 * 
 * @author janko
 */
public class QueueReceiver {
	Log log = LogFactory.getLog(getClass());
	QueueConnectionFactory _connectionFactory;
	QueueConnection _connection;
	QueueSession _session;
	Queue _queue;
	String _queueName;

	List<MessageConsumer> _consumerList = new ArrayList<MessageConsumer>();
	String _debugId;

	public QueueReceiver(ConnectionFactory connectionFactory, String queueName) {
		_connectionFactory = (QueueConnectionFactory) connectionFactory;

		_queueName = queueName;
		doInit();
	}

	public void doInit() {
		try {
			log.debug("QueueReceiver.init");
			_connection = _connectionFactory.createQueueConnection();
			_session = _connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			_queue = _session.createQueue(_queueName);
			_connection.start();
		} catch (Exception e) {
			log.error(e.getStackTrace());
		}
	}

	/**
	 * Initialisiert einen neuen Consumer für die gegebenene destination und hängt den gegebenen Listener dran.
	 * 
	 * @param destination
	 * @param selektor
	 * @param listener
	 */
	public void receive(String selector, QueueMessageListener listener) {
		try {
			MessageConsumer consumer = createConsumer(selector, _queue);
			log.debug("Ich kriege Msgs, wenn " + selector);
			consumer.setMessageListener(listener);
			_consumerList.add(consumer);
		} catch (Exception n) {
			n.printStackTrace();
		}
	}

	/**
	 * Initialisiert einen neuen Consumer für eine temporäre Queue und hängt den gegebenen Listener dran. und gibt die
	 * erzeugte temporary Queue zurück.
	 * 
	 * @param selektor
	 * @param listener
	 * @return die Queue auf die gesendet werden kann
	 */
	public TemporaryQueue receiveFromTemporaryQueue(String selector, MessageListener listener) {
		try {
			TemporaryQueue tempoQueue = _session.createTemporaryQueue();
			MessageConsumer consumer = createConsumer(selector, tempoQueue);
			log.debug("Ich kriege Msgs, wenn " + selector + " auf Queue:" + tempoQueue.getQueueName());
			consumer.setMessageListener(listener);
			_consumerList.add(consumer);
			return tempoQueue;
		} catch (Exception n) {
			n.printStackTrace();
		}
		return null;
	}

	private MessageConsumer createConsumer(String selector, Queue queue) throws JMSException {
		if (selector == null || "".equals(selector)) {
			return _session.createConsumer(queue);
		} else {
			return _session.createConsumer(queue, selector);
		}
	}

	/**
	 * Handlet eingehende Messages. 
	 */
	public void onMessage(Message msg) {
		dbgLog("MSG received");
		if (msg != null) {
			if (msg instanceof ObjectMessage) {
				ObjectMessage messageObject = (ObjectMessage) msg;
				try {
					Object content = messageObject.getObject();
					if (content instanceof IJiacMessage) {
						IJiacMessage jiacMsg = (IJiacMessage) content;
						Object payload = jiacMsg.getPayload();
						String outString = convertPayLoadToString(payload);
						dbgLog(" received Payload:" + payload);
					}
					msg.acknowledge();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String convertPayLoadToString(Object payload) {
		StringBuffer sb = new StringBuffer();
		if (payload.getClass().equals(Array.class)) {
			char[] content = (char[]) payload;
			sb.append(content);
		}
		return sb.toString();
	}

	private void dbgLog(String text) {
		log.debug("[QueueReceiver:" + getQueueName() + "]<<< " + text);
	}

	public QueueConnection getConnection() {
		return _connection;
	}

	public void setConnection(QueueConnection connection) {
		_connection = connection;
	}

	public QueueConnectionFactory getConnectionFactory() {
		return _connectionFactory;
	}

	public void setConnectionFactory(QueueConnectionFactory connectionFactory) {
		_connectionFactory = connectionFactory;
	}

	public String getDebugId() {
		return _debugId;
	}

	public void setDebugId(String debugid) {
		_debugId = debugid;
	}

	public Queue getQueue() {
		return _queue;
	}

	public void setQueue(Queue queue) {
		_queue = queue;
	}

	public QueueSession getSession() {
		return _session;
	}

	public void setSession(QueueSession session) {
		_session = session;
	}

	public String getQueueName() {
		return _queueName;
	}

	public void setQueueName(String queueName) {
		_queueName = queueName;
	}
}
