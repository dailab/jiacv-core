package de.dailab.jiactng.agentcore.comm.helpclasses;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageProducer;

// testsubclass needed for testing with the sender class
public class MessageProducerShunt implements MessageProducer {

	Message justSent = null;
	long _timeToLive = 0;
	
	public void close(){}
	public Destination getDestination(){return null;}
	public boolean getDisableMessageID(){return true;}
	public boolean getDisableMessageTimestamp(){return true; }
	public int getPriority(){return 0;}
	public int getDeliveryMode(){return 0;}
	public long getTimeToLive(){return _timeToLive;}
	public void setDeliveryMode(int deliveryMode){}
	public void setDisableMessageID(boolean value){}
	public void setDisableMessageTimestamp(boolean value){}
	public void setPriority(int defaultPriority){}
	public void setTimeToLive(long timeToLive){
		_timeToLive = timeToLive;
	}
	
	public void send(Destination destination, Message message){}
	public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive){}
	public void send(Message message, int deliveryMode, int priority, long timeToLive){}
	
	// stores last "sent" message
	public void send(Message message){
		justSent = message;
	}
	
	public Message getMessage(){
		return justSent;
	}
	
}