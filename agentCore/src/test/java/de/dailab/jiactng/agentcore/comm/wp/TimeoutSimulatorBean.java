package de.dailab.jiactng.agentcore.comm.wp;

import java.io.Serializable;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.FactSet;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.MessageOfChange;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.ResultDump;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;

public class TimeoutSimulatorBean extends AbstractAgentBean {

	/**
	 * the address of the directory at the local agentnode
	 */
	private ICommunicationAddress directoryAddress = null;
	
	/**
	 * sometimes you realy don't want to get a result
	 * besides that a ResultDump keeps the memory clean
	 */
	private final ResultDump _resultDump = new ResultDump();
	
	/**
	 * Action needed to send messages through the communicationBean
	 */
	private Action _sendAction = null;
	
	private static final IJiacMessage WHITEPAGES_REFRESH_MESSAGETEMPLATE;
	
	static{
		JiacMessage refreshMessage = new JiacMessage();
		refreshMessage.setProtocol(DirectoryAgentNodeBean.REFRESH_PROTOCOL_ID);
		WHITEPAGES_REFRESH_MESSAGETEMPLATE = refreshMessage;
	}
	
	private RefreshAgent _refreshAgent = new RefreshAgent();
	
	private IMessageBoxAddress _myAddress = CommunicationAddressFactory.createMessageBoxAddress("TimeoutSimulatorBean");
	
	private String _myUUID = "YeeeeeecccccchhhhhhaaaaaaaIHaveAnUUID!";
	private AgentDescription _myAgent = new AgentDescription(null, "TimeoutAgent", null, _myAddress, _myUUID);
	
	
	@Override
	public void doStart() throws Exception {
		super.doStart();
		String messageboxName = thisAgent.getAgentNode().getUUID() + DirectoryAgentNodeBean.SEARCHREQUESTSUFFIX;
		directoryAddress = CommunicationAddressFactory.createMessageBoxAddress(messageboxName);
		_sendAction = memory.read(new Action(ICommunicationBean.ACTION_SEND,null,new Class[]{IJiacMessage.class, ICommunicationAddress.class},null));
		memory.attach(_refreshAgent, WHITEPAGES_REFRESH_MESSAGETEMPLATE);
	}
	
	@Override
	public void doStop() throws Exception {
		super.doStop();
		memory.detach(_refreshAgent);
		_sendAction = null;
	}
	
	
	public void initTimeout(){
		FactSet additions = new FactSet();
		FactSet removals = null;
		
		
		additions.add(_myAgent);
		MessageOfChange moc = new MessageOfChange(additions, removals);
		
		JiacMessage message = new JiacMessage(moc);
		message.setProtocol(DirectoryAgentNodeBean.CHANGE_PROPAGATION_PROTOCOL_ID);
		message.setHeader("UUID", _myUUID);
		message.setSender(_myAddress);
		message.setHeader("TIMEOUTAGENT", "TESTMESSAGE");
		
		DoAction send = _sendAction.createDoAction(new Serializable[] {message, directoryAddress}, _resultDump);
		memory.write(send);
	}
	
	@SuppressWarnings("serial")
	private class RefreshAgent implements SpaceObserver<IFact>{
		@Override
		@SuppressWarnings("unchecked")
		public void notify(SpaceEvent<? extends IFact> event) {
			if(event instanceof WriteCallEvent) {
				WriteCallEvent wceTemp = (WriteCallEvent) event;
				if (wceTemp.getObject() instanceof IJiacMessage){
					IJiacMessage message = (IJiacMessage) wceTemp.getObject();
					if (message.getProtocol().equalsIgnoreCase(DirectoryAgentNodeBean.REFRESH_PROTOCOL_ID)){
							
							JiacMessage pingMessage = new JiacMessage(_myAgent);
							pingMessage.setProtocol(DirectoryAgentNodeBean.REFRESH_PROTOCOL_ID);
							DoAction send = _sendAction.createDoAction(new Serializable[] {pingMessage, message.getSender()}, _resultDump);
							memory.write(send);
						}
					} 
				}
			}
		}
	}

