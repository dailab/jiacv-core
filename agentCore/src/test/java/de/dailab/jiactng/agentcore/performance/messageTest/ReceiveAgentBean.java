package de.dailab.jiactng.agentcore.performance.messageTest;

import java.util.Set;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.performance.Message;

/**
 * 
 * An agent class which can receive messages from a send agent
 * 
 * @author Hilmi Yildirim
 *
 */
public class ReceiveAgentBean extends AbstractAgentBean{

	/**
	 * action for sending messages
	 */
	private IActionDescription sendAction;
	
	/**
	 * the number of messages which are expected
	 */
	private int messagesToReceive;
	
	/**
	 * the number of messages which are received
	 */
	private int numberOfReceivedMessages;
	
	/**
	 * the receiving times of the messages
	 */
	private long[] receiveTimes;
	
	
	/**
	 * The send action will be initialized and an observer will be attached
	 * to the memory
	 */
	public void doStart(){
		try {
			super.doStart();
		} catch (Exception e) {
			System.err.println("Receive agent: " + e.getMessage());
		}
		IActionDescription template = new Action(ICommunicationBean.ACTION_SEND);
		sendAction = memory.read(template);
		
		if(sendAction == null){
			sendAction = thisAgent.searchAction(template);
		}
		
		if(sendAction == null){
			throw new RuntimeException("Action not found");
		}
		
		//memory.attach(new MessageObserver(), new JiacMessage());
	}
	
	/**
	 * initialize the agent
	 * @param messagesToReceive	number of messages which are expected
	 */
	public void initialize(int messagesToReceive){
		this.messagesToReceive = messagesToReceive;
		receiveTimes = new long[messagesToReceive];
		numberOfReceivedMessages = 0;
	}
	
	/**
	 * @return receiving times
	 */
	public long[] getReceiveTimes(){
		return receiveTimes;
	}
	
	/**
	 * @return number of messages which are expected
	 */
	public int getMessagesToReceive() {
		return messagesToReceive;
	}

	/**
	 * @param messagesToReceive sets the number of messages which are expected
	 */
	public void setMessagesToReceive(int messagesToReceive) {
		this.messagesToReceive = messagesToReceive;
	}
	
	/**
	 * @return true if all expected messages are received
	 */
	public boolean allMessagesReceived(){
		return numberOfReceivedMessages == messagesToReceive;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void execute(){
		long time = System.currentTimeMillis();
		Set<JiacMessage> messages = (Set<JiacMessage>)memory.removeAll(new JiacMessage());
		
		synchronized (this) {
			for(JiacMessage message : messages){
				if(message.getPayload() instanceof Message){
					long receiveTime = ((Message)message.getPayload()).getTimeStamp();
					long totalTime = time - receiveTime;
					receiveTimes[numberOfReceivedMessages] = totalTime;
					numberOfReceivedMessages++;
					if(allMessagesReceived()){
						this.notify();
					}
				}
			}
		}
	}
}
