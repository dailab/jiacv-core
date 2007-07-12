package de.dailab.jiactng.agentcore.comm;

import javax.jms.MessageListener;
import javax.jms.MessageConsumer;

public class ConsumerData {

	private MessageConsumer _consumer = null;
	private String _destination = "";
	private MessageListener _listener = null;
	private String _selector = null;
	
	public ConsumerData(MessageConsumer consumer, String destination, MessageListener listener, String selector){
		_consumer = consumer;
		_destination = destination;
		_listener = listener;
		_selector = selector;
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
	
	public void setListener(MessageListener listener){
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
	
	public MessageListener getListener(){
		return _listener;
	}
	
	public String getSelector(){
		return _selector;
	}
	
}
