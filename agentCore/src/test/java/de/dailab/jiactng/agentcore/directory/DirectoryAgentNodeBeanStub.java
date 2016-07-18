package de.dailab.jiactng.agentcore.directory;

import java.util.ArrayList;
import java.util.List;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;

public class DirectoryAgentNodeBeanStub extends DirectoryAgentNodeBean{
	
	private List<String> receivedMessagesFromGroup;
	
	private List<String> sendMessagesToGroup;
	
	
	public DirectoryAgentNodeBeanStub(){
		receivedMessagesFromGroup = new ArrayList<String>();
		sendMessagesToGroup = new ArrayList<String>();
	}
	
	@Override
	public void onMessage(MessageTransport source, IJiacMessage message,
			ICommunicationAddress at) {
		
		receivedMessagesFromGroup.add(message.getGroup());
		super.onMessage(source, message, at);
	}
	
	@Override
	protected void sendMessage(JiacMessage message,
			ICommunicationAddress address) {

		if (address instanceof IGroupAddress) {
			//deletes df@
			String cleanName = address.toUnboundAddress().getName().split("@")[1];
			sendMessagesToGroup.add(cleanName);
		}
		
		super.sendMessage(message, address);
	}
	
	public List<String> getReceivedMessagesFromGroup(){
		return receivedMessagesFromGroup;
	}
	
	public List<String> getSendMessagesToGroup(){
		return sendMessagesToGroup;
	}
	
	public String getAddress(){
		return myAddress.getName();
	}
}
