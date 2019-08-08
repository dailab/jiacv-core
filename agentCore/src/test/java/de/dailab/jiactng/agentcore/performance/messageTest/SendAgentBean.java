package de.dailab.jiactng.agentcore.performance.messageTest;

import java.io.Serializable;
import java.util.List;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;
import de.dailab.jiactng.agentcore.performance.Message;

/**
 * 
 * An agent class which can send messages to receive agents
 * 
 * @author Hilmi Yildirim
 *
 */
public class SendAgentBean extends AbstractAgentBean{

	/**
	 * action for sending messages
	 */
	private IActionDescription  sendAction;
	
	/**
	 * number of messages which should send
	 */
	private int messagesToSend;
	
	
	/**
	 * The send action will be initialized
	 */
	public void doStart(){
		try {
			super.doStart();
		} catch (Exception e) {
			System.out.println("SendAgent: " + e.getMessage());
		}
		IActionDescription template = new Action(ICommunicationBean.ACTION_SEND);
		sendAction = memory.read(template);
		
		if(sendAction == null){
			sendAction = thisAgent.searchAction(template);
		}
		
		if(sendAction == null){
			throw new RuntimeException("Action not found");
		}
	}
	
	/**
	 * initialize the agent
	 * @param messagesToSend number of messages to send
	 */
	public void initialize(int messagesToSend){
		this.messagesToSend = messagesToSend;
	}
	
	/**
	 * sends all messages with the current time stamp
	 */
	public void send(){
		for(int i = 0; i < messagesToSend; i++){
			sendTime();
		}
	}
	
	/**
	 * method for sending a message
	 */
	public void sendTime(){
		List<IAgentDescription> agents = 
				thisAgent.searchAllAgents(new AgentDescription());
		
		for(IAgentDescription agent : agents){
			if(agent.getName().equals("ReceiveAgent")){
				JiacMessage message = new JiacMessage(new Message(System.currentTimeMillis()));
				IMessageBoxAddress receiver = agent.getMessageBoxAddress();
				invoke(sendAction, new Serializable[]{message, receiver});
				break;
			}
		}
	}

	/**
	 * @return number of messages to send
	 */
	public int getMessagesToSend() {
		return messagesToSend;
	}
}
