package de.dailab.jiactng.agentcore.comm.helpclasses;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Message;

import de.dailab.jiactng.agentcore.comm.CommMessageListener;

public class HelpListener implements de.dailab.jiactng.agentcore.comm.CommMessageListener {

	private List<Message> queueMessages = new ArrayList<Message>();
	private List<Message> topicMessages = new ArrayList<Message>();
	
	public HelpListener(){

	}

	public List<Message> getQueueMessages(){
		return queueMessages;
	}
	
	public List<Message> getTopicMessages(){
		return topicMessages;
	}

	public Message getLastQueueMessage(){
		return queueMessages.remove(queueMessages.size());
	}
	
	public Message getLastTopicMessage(){
		return topicMessages.remove(topicMessages.size());
	}

	public int getQueueSize(){
		return queueMessages.size();
	}
	
	public int getTopicSize(){
		return topicMessages.size();
	}

	public void clear(){
		queueMessages.clear();
		topicMessages.clear();
	}

	public void messageReceivedFromQueue(Message message){
//		System.err.println("Hurra! Eine Nachricht! Packen wir sie ein! " + messages.size());
		queueMessages.add(message);
//		System.err.println(messages.size());
	}
	public void messageReceivedFromTopic(Message message){
		topicMessages.add(message);
	}

}
