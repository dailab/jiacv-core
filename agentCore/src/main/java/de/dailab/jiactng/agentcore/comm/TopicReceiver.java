package de.dailab.jiactng.agentcore.comm;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

/**
 * Liest von der Topic
 * 
 * @author janko
 */
public class TopicReceiver implements MessageListener {
	// für topic Subscription
	TopicConnection _topicConnection = null;
	TopicConnectionFactory _topicConnectionFactory;
	Topic _topic;
	String _topicName;
	TopicSession _topicSession;
	String _debugId;
	String _defaultSelector = null;
	CommBean _commBean;
	
	public TopicReceiver(CommBean commBean, ConnectionFactory connectionFactory, String topicName) {
		_commBean = commBean;
		_topicConnectionFactory = (TopicConnectionFactory)connectionFactory;
		_topicName = topicName;
		doInit();
	}	
	/**
	 * initialisiert den Topic, Session dieses Receivers
	 * 
	 * @param topicName
	 * @param selector
	 */
	public void doInit() {
		try {
			System.out.println("TopicReceiver.init");
			_topicConnection = _topicConnectionFactory.createTopicConnection();
			_topicSession = _topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			_topic = _topicSession.createTopic(_topicName);
			_topicConnection.start();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	/**
	 *  erzeugt einen subscriber
	 *  @param selector der selektor, der die Nachrichten filtert
	 *  @param listener der Listener der am Subscriber gesetzt werden soll
	 */
	public void receive(String selector, MessageListener listener) {
		try {
			TopicSubscriber topicSubscriber = createSubscriber(selector, _topic, _topicSession);
			if (listener != null) {
				topicSubscriber.setMessageListener(listener);
			} else {
				topicSubscriber.setMessageListener(this);
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private TopicSubscriber createSubscriber(String selector, Topic topic, TopicSession topicSession) throws JMSException {
		if (selector != null) {
			// wofür war das nochmal ?? warum 'false'
			return topicSession.createSubscriber(topic, selector, true);
		} else {
			return topicSession.createSubscriber(topic, _defaultSelector, true);
		}
	}

	/**
	 * Handlet eingehende Messages. Achtung es gibt bisher nur ein Kommando - d.h. es wird immer davon ausgegangen, dass
	 * eine empfangene Nachricht die Id einer neu erkannten platform beinhaltet
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
						System.out.println("<<<Topic... received Payload:" + payload);
						handleMessage(jiacMsg);
					}
					msg.acknowledge();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	private void handleMessage(IJiacMessage jiacMsg) {
		IEndPoint senderAddress = jiacMsg.getStartPoint();
		if (senderAddress.equals(_commBean.getAddress())) {
			System.out.println("Kam ja von mir.. Ignoriert");
		} else {
			System.out.println("Saustark, ne neue Message");
		}
	}
	

	private void dbgLog(String text) {
		System.out.println("[TopicReceiver:"+getDebugId()+"]<<< "+text);
	}	
	
	public String getTopicName() {
		return _topicName;
	}

	public void setTopicName(String topicName) {
		_topicName = topicName;
	}

	public TopicConnectionFactory getTopicConnectionFactory() {
		return _topicConnectionFactory;
	}

	public void setTopicConnectionFactory(TopicConnectionFactory topicConnectionFactory) {
		_topicConnectionFactory = topicConnectionFactory;
	}

	public String getDebugId() {
		return _debugId;
	}

	public void setDebugId(String debugId) {
		_debugId = debugId;
	}
}
