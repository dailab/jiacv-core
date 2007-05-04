package de.dailab.jiactng.agentcore.comm;

import javax.jms.MessageListener;

import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.protocol.IProtocolHandler;

public class TopicCommunicator {
	IProtocolHandler _protocol;
	TopicReceiver _receiver;
	TopicSender _sender;

	/**
	 * Verschickt nachricht auf topic, es wird die defaulttopic des topicsenders benutzt
	 * @param message
	 */
	public void publish(IJiacMessage message) {
		_sender.sendToTopic(message, null);
	}

	/**
	 * Verschickt nachricht auf topic, es wird die defaulttopic des topicsenders benutzt
	 * @param message die zu verschikende nachricht
	 * @param topicName bisher unbenutzt
	 */
	public void publish(IJiacMessage message, String topicName) {
		_sender.sendToTopic(message, null);
	}
	
	
	/**
	 * @param selector
	 * @param listener
	 */
	public void subscribe(String selector, MessageListener listener) {
		_receiver.receive(selector, listener);
	}
	
	public IProtocolHandler getProtocol() {
		return _protocol;
	}
	public void setProtocol(IProtocolHandler protocol) {
		_protocol = protocol;
	}
	public TopicReceiver getReceiver() {
		return _receiver;
	}
	public void setReceiver(TopicReceiver receiver) {
		_receiver = receiver;
	}
	public TopicSender getSender() {
		return _sender;
	}
	public void setSender(TopicSender sender) {
		_sender = sender;
	}
	
	
}
