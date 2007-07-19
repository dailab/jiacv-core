package de.dailab.jiactng.agentcore.comm.jms;

import javax.jms.Destination;
import javax.jms.MessageConsumer;

import de.dailab.jiactng.agentcore.comm.IJiacMessageListener;

public class ConsumerData {

	private MessageConsumer _consumer = null;
	private String _destination = "";
	private IJiacMessageListener _listener = null;
	private String _selector = null;
	
	public ConsumerData(MessageConsumer consumer, String destination, IJiacMessageListener listener, String selector){
		_consumer = consumer;
		_destination = destination;
		_listener = listener;
		_selector = selector;
	}
	
	public boolean isReceiver(ConsumerData consumerData, Destination destination){
		
		if (consumerData.getDestination().equalsIgnoreCase(destination.toString())){ // is the destination right?	
					return true;
		} // end destinationcheck
	
		return false;
	}
	
	
	public String toString(){
		return "ConsumerDestination: " + _destination.toString();
	}
	
	public void setConsumer(MessageConsumer consumer){
		_consumer = consumer;
	}
	
	public void setDestination(String destination){
		_destination = destination;
	}
	
	public void setListener(IJiacMessageListener listener){
		_listener = listener;
	}
	
	public void setSelector(String selector){
		_selector = selector;
	}
	
	public MessageConsumer getConsumer(){
		return _consumer;
	}
	
	public String getDestination(){
		return _destination;
	}
	
	public IJiacMessageListener getListener(){
		return _listener;
	}
	
	public String getSelector(){
		return _selector;
	}
	
}
