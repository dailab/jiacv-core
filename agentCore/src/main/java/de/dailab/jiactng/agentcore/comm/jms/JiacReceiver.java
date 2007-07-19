package de.dailab.jiactng.agentcore.comm.jms;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.IJiacMessageListener;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.protocol.IProtocolHandler;

/**
 * 
 * JiacReceiver is a ServiceClass for CommBeanV2, offering functionality for managing messagelisteners
 * on JMS destinations. 
 * 
 * @author Loeffelholz
 *
 */


public class JiacReceiver implements MessageListener{
	Log log = LogFactory.getLog(getClass());
	ConnectionFactory _connectionFactory;
	Connection _connection;
	Session _session;
	Destination _queue;
	CommBeanV2 _parent;
	
	MessageConsumer _consumer = null;
	
	IProtocolHandler _protocol;
	
	List<ConsumerData> _consumerList = new ArrayList<ConsumerData>();
	List<ConsumerData> _hearAllList = new ArrayList<ConsumerData>();
	String _debugId;

	ICommunicationAddress _defaultCommAddress;
	Destination _defaultDestination = null; 
	boolean _defaultDestinationIsTopic = false;
	
	public JiacReceiver(ConnectionFactory connectionFactory, CommBeanV2 parent) {
		log.debug("Creating JiacReceiver");
		_connectionFactory = (ConnectionFactory) connectionFactory;
		_parent = parent;
		
		doInit();
	}

	/**
	 * Initializes the JiacReceiver creating a MessageConsumer for receiving messages to the CommBean
	 */
	public void doInit() {
		log.debug("JiacReceiver initializing");
		try {
			_connection = _connectionFactory.createConnection();
			_session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			_queue = _session.createQueue(_parent.getAddress().toString());
			_consumer = _session.createConsumer(_queue);
			_consumer.setMessageListener(this);
			_connection.start();
		} catch (Exception e) {
			log.error(e.getCause());
		}
		
		log.debug("JiacReceiver initilized");
	}
	
	/**
	 * commence Cleanup procedures, closing all consumers and connections.
	 */
	public void doCleanup(){
		log.debug("JiacReceiver.doCleanup");
		try {
			this.stopReceiveAll();
			_consumer.close();
			_session.close();
			_connection.close();
		} catch (JMSException e) {
			log.error(e.getCause());
		}
	}
	
	private Queue createQueue(String queueName) {
		log.debug("creating Queue: " + queueName);
		
		Queue queue = null;
		try {
			queue = _session.createQueue(queueName);
		} catch (JMSException e) {
			log.error(e.getCause());
		}
		return queue;
	}
	
	private Topic createTopic(String topicName) {
		log.debug("creating Topic: " + topicName);
		
		Topic topic = null;
		try {
			topic = _session.createTopic(topicName);
		} catch (JMSException e) {
			log.error(e.getCause());
		}
		return topic;
	}
	
	/**
	 * Initializing a new Consumer using the given destination and setting the given Listener to it.
	 * 
	 * @param listener the MessageListener used to get onto the messages
	 * @param destinationName the Name of the destination from which the Messages will be sent
	 * @param topic    is the destination to listen on a topic? (true/false)
	 * @param selector a selector to get only special messages
	 */
	public Destination receive(IJiacMessageListener listener, String destinationName, boolean topic, String selector) {
		log.debug("Creating destination for JiacReceiver.receive");
		Destination destination = null;
		if (topic)
			destination = createTopic(destinationName);
		else
			destination = createQueue(destinationName);
		
		receive(listener, destination, selector);
		return destination;
		
	}
	
	/**
	 * Initializing a new Consumer using the given destination and setting the given Listener to it.
	 * 
	 * @param listener the MessageListener used to get onto the messages
	 * @param destination the destination from which the Messages will be sent
	 * @param selector a selector to get only special messages
	 */
	public void receive(IJiacMessageListener listener, Destination destination, String selector) {
		log.debug("JiacReceiver.receive");
		
		try {
			MessageConsumer consumer = _session.createConsumer(destination, selector);
			consumer.setMessageListener(this);
			ConsumerData consumerData = new ConsumerData(consumer, destination.toString(), listener, selector);
			_consumerList.add(consumerData);
		} catch (Exception e) {
			log.error(e.getCause());
		}	
	}
	
	/**
	 * Initializing a new Consumer using a given ICommunicationAddress
	 * 
	 * @param listener
	 * @param commAdd
	 * @param selector
	 */
	public void receive(IJiacMessageListener listener, ICommunicationAddress commAdd, String selector){
		Destination destination;
		if (commAdd instanceof IGroupAddress){
			destination = createTopic(commAdd.getAddress());
		} else {
			destination = createQueue(commAdd.getAddress());
		}
		receive(listener, destination, selector);
	}
	
	public void receive(IJiacMessageListener listener, String selector){
		ConsumerData consumerData = new ConsumerData(null, null, listener, selector);
		_hearAllList.add(consumerData);
	}
	
	
	
	
	/**
	 * Stops receivment of the Messages from a given destination by removing the consumer
	 * aligned to it from the consumerlist
	 * 
	 * @param destinationName	the name of the destination we don't want to listen anymore to
	 * @param topic				is this destionation a topic? (true/false)
	 * @param selector			a selector to recieve only special messages
	 */
	public void stopReceive(String destinationName, boolean topic, IJiacMessageListener listener, String selector){
		log.debug("creating Destination for JiacReceiver.stopReceive");
		
		Destination destination = null;
		if (topic)
			destination = createTopic(destinationName);
		else
			destination = createQueue(destinationName);
		
		stopReceive(destination, listener, selector);
		
		}
	
	/**
	 * Stops receivment of the Messages from a given destination by removing the consumer
	 * aligned to it from the consumerlist (especially useful for temporaryDestinations)
	 * 
	 * @param destinationName	the name of the destination we don't want to listen anymore to
	 * @param selector			a selector to recieve only special messages
	 */
	public void stopReceive(Destination destination, IJiacMessageListener listener, String selector){
		log.debug("JiacReceiver.stopReceive");
		
		ListIterator<ConsumerData> list = _consumerList.listIterator();
		
		while (list.hasNext()){ // lets go and find it
			ConsumerData consumerData = list.next();
			
			if (consumerData.getDestination().equalsIgnoreCase(destination.toString())){ // is the destination right?
				
				if (consumerData.getListener().equals(listener)){ // does the same listener listens to it?
					
					// now that we are allmost sure... let's check one last time if it has the same selector on it.
					if ((selector == null) || (selector.equalsIgnoreCase(consumerData.getSelector()))){
						
						try {
							// after finding it let's close it.
							if (consumerData.getConsumer() != null) {
								consumerData.getConsumer().close();
							}
						} catch (JMSException e) {
							log.error(e.getCause());
						} // end try
						
						// one last thing to do... let's remove it from the list.
						list.remove();
						
					} // end selectorcheck
				} // end listenercheck
			} // end destinationcheck
		}// end remove
		
	}
	
	/**
	 * Stops receivment of the Messages from a given destination by removing the consumer
	 * aligned to it from the consumerlist (especially useful for temporaryDestinations)
	 * 
	 * @param destinationName	the name of the destination we don't want to listen anymore to
	 * @param selector			a selector to recieve only special messages
	 */
	public void stopReceive(ICommunicationAddress commAdd, IJiacMessageListener listener, String selector){
		log.debug("JiacReceiver.stopReceive");
		Destination destination;
		
		if(commAdd instanceof IGroupAddress){
			destination = createTopic(commAdd.getAddress());
		} else {
			destination = createQueue(commAdd.getAddress());
		}
		
		ListIterator<ConsumerData> list = _consumerList.listIterator();
		
		while (list.hasNext()){ // lets go and find it
			ConsumerData consumerData = list.next();
			
			if (consumerData.getDestination().equalsIgnoreCase(destination.toString())){ // is the destination right?
				
				if (consumerData.getListener().equals(listener)){ // does the same listener listens to it?
					
					// now that we are allmost sure... let's check one last time if it has the same selector on it.
					if ((selector == null) || (selector.equalsIgnoreCase(consumerData.getSelector()))){
						
						try {
							// after finding it let's close it.
							if (consumerData.getConsumer() != null) {
								consumerData.getConsumer().close();
							}
						} catch (JMSException e) {
							log.error(e.getCause());
						} // end try
						
						// one last thing to do... let's remove it from the list.
						list.remove();
						
					} // end selectorcheck
				} // end listenercheck
			} // end destinationcheck
		}// end remove
		
	}
	
	/**
	 * Stops receivment of the Messages from a given destination by removing the consumer
	 * aligned to it from the consumerlist (especially useful for temporaryDestinations)
	 * 
	 * @param destinationName	the name of the destination we don't want to listen anymore to
	 * @param selector			a selector to recieve only special messages
	 */
	public void stopReceive(IJiacMessageListener listener, String selector){
		log.debug("JiacReceiver.stopReceive");
		Destination destination;
		
		
		ListIterator<ConsumerData> list = _hearAllList.listIterator();

		while (list.hasNext()){ // lets go and find it
			ConsumerData consumerData = list.next();

			if (consumerData.getListener().equals(listener)){ // does the same listener listens to it?

				// now that we are allmost sure... let's check one last time if it has the same selector on it.
				if ((selector == null) || (selector.equalsIgnoreCase(consumerData.getSelector()))){

					try {
						// after finding it let's close it.
						if (consumerData.getConsumer() != null) {
							consumerData.getConsumer().close();
						}
					} catch (JMSException e) {
						log.error(e.getCause());
					} // end try

					// one last thing to do... let's remove it from the list.
					list.remove();

				} // end selectorcheck
			} // end listenercheck
		}// end remove

	}
	
	
	/**
	 * stops receiving Messages from all enlisted Destinations except the Commbean itself.
	 */
	public void stopReceiveAll(){
		log.debug("JiacReceiver.stopReceiveAll");
		ListIterator<ConsumerData> list = _consumerList.listIterator();
		
		while(list.hasNext()){
			try {
				ConsumerData consumerData = list.next();
				if (consumerData.getConsumer() != null){
					consumerData.getConsumer().close();
				}
			} catch (JMSException e) {
				log.error(e.getCause());
			}
		}
		_consumerList.clear();	
		_hearAllList.clear();
	}	
	
	
	
	/**
	 * receives messages from registered Destinations of the CommBean
	 */
	public void onMessage(Message message){
		log.debug("JiacReceiver receiving Message");
		
		Destination dest= null;
		Destination destination = null;
		try {
			dest = message.getJMSReplyTo();
			destination = message.getJMSDestination();
		} catch (JMSException e1) {
			log.error(e1.getCause());
		}
		log.debug("Message received from " + dest);
		if ((message != null) && (message instanceof ObjectMessage)) {
			ObjectMessage objectMessage = (ObjectMessage) message;
			try {
				Object content = objectMessage.getObject();
				if (content instanceof IJiacMessage) {
					IJiacMessage jiacMessage = (IJiacMessage) content;
					Object payload = jiacMessage.getPayload();
					log.debug("<<<received Payload:" + payload.toString());
//					_protocol.processMessage(message);
					
					informAllhearing(jiacMessage);
		
					// inform Single Listeners
					ListIterator<ConsumerData> list = _consumerList.listIterator();
					ICommunicationAddress from = jiacMessage.getSender();
					
					while(list.hasNext()){
						ConsumerData consumerData = list.next();
						if (consumerData.isReceiver(consumerData, destination)){
							if (consumerData.getSelector() != null){
							    String selector = consumerData.getSelector();
								int index = selector.indexOf("=");
								String property = selector.substring(0, index);
								String value = selector.substring(index+1);
								
								if ((objectMessage.propertyExists("property")) && (objectMessage.getStringProperty(property).equalsIgnoreCase(value)) ){
									consumerData.getListener().receive(jiacMessage, from);
								}
								
							} else {
								consumerData.getListener().receive(jiacMessage, from);
							}
							
						}
					}
					
					
					
				}
				message.acknowledge();
			} catch (Exception e) {
				log.error(e.getCause());
			}
		}
	}

	private void informAllhearing(IJiacMessage message){
		ListIterator<ConsumerData> list = _hearAllList.listIterator();

		while (list.hasNext()){ // lets go and find it
			ConsumerData consumerData = list.next();
			ICommunicationAddress from = message.getSender();
			consumerData.getListener().receive(message, from);
		}
	}

	private void informSingleListeners(IJiacMessage message, Destination destination){
		ListIterator<ConsumerData> list = _consumerList.listIterator();
		ICommunicationAddress from = message.getSender();
		
		while(list.hasNext()){
			ConsumerData consumerData = list.next();
			if (consumerData.isReceiver(consumerData, destination)){
				consumerData.getListener().receive(message, from);
			}
		}
		
	}
	
	
	
	// and now... all regarding services for "outsiders"
	
	/**
	 * Initializes a new Consumer for a temporary Queue and setting the given Listener, returning the created
	 * temporary Queue
	 * 
	 * @param selector	To select only special Messages from the destination
	 * @param listener	the Listener whom should get all the messages from this Destination
	 * @param selector	a selector to recieve only special messages
	 * @return TemporaryQueue to send to and receive from
	 */
	public TemporaryQueue receiveFromTemporaryQueue(IJiacMessageListener listener, String selector) {
		log.debug("creating TemporaryQueue");
		try {
			TemporaryQueue temporaryQueue = _session.createTemporaryQueue();
			MessageConsumer consumer = _session.createConsumer(temporaryQueue, selector);
			
			consumer.setMessageListener(this);
			ConsumerData consumerData = new ConsumerData(consumer, temporaryQueue.toString(), listener, selector);
			_consumerList.add(consumerData);

			return temporaryQueue;
		} catch (Exception e) {
			log.error(e.getCause());
		}
		return null;
	}
	
	/**
	 * Initializes a new Consumer for a temporary Topic and setting the given Listener, returning the created
	 * temporary Queue
	 * 
	 * @param selector	To select only special Messages from the destination
	 * @param listener	the Listener whom should get all the messages from this Destination
	 * @param selector	a selector to recieve only special messages
	 * @return TemporaryQueue to send to and receive from
	 */
	public TemporaryTopic receiveFromTemporaryTopic(IJiacMessageListener listener, String selector) {
		log.debug("creating TemporaryTopic");
		try {
			TemporaryTopic temporaryTopic = _session.createTemporaryTopic();
			MessageConsumer consumer = _session.createConsumer(temporaryTopic, selector);
			
			consumer.setMessageListener(this);
			ConsumerData consumerData = new ConsumerData(consumer, temporaryTopic.toString(), listener, selector);
			_consumerList.add(consumerData);

			return temporaryTopic;
		} catch (Exception e) {
			log.error(e.getCause());
		}
		return null;
	}


	
	public Destination getReplyToAdress(){
		return _queue;
	}
	
	public IProtocolHandler getProtocol() {
		return _protocol;
	}

	public void setProtocol(IProtocolHandler protocol) {
		_protocol = protocol;
	}
	
}