package de.dailab.jiactng.agentcore.comm.wp;

import java.util.HashSet;
import java.util.Set;

import javax.security.auth.DestroyFailedException;

import org.apache.commons.logging.Log;
import org.sercho.masp.space.SimpleObjectSpace;
import org.sercho.masp.space.event.EventedSpaceWrapper;
import org.sercho.masp.space.event.EventedTupleSpace;
import org.sercho.masp.space.event.EventedSpaceWrapper.SpaceDestroyer;

import de.dailab.jiactng.agentcore.AbstractAgentNodeBean;
import de.dailab.jiactng.agentcore.IAgentNodeBean;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport.IMessageTransportDelegate;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;

public class DirectoryAgentNodeBean extends AbstractAgentNodeBean implements
		IAgentNodeBean {
	
	public final static String SEARCHREQUESTSUFFIX = "DirectoryAgentNodeBean";
	
	private SpaceDestroyer<IFact> destroyer = null;
	private EventedTupleSpace<IFact> space = null;
	private MessageTransport messageBus = null;
	private ICommunicationAddress myAddress = null; 

	private SearchRequestHandler _searchRequestHandler = null;
	
	public DirectoryAgentNodeBean() {
		setBeanName(agentNode.getName() + SEARCHREQUESTSUFFIX);
	}

	/**
	 * This method is meant to give the AgentNode the option to directly
	 * add an AgentDescription object when an agent is added to the node.
	 */
	public void addAgentDescription(IFact agentDescription){
		space.write(agentDescription);
	}
	
	/**
	 * This method is meant to give the AgentNode the option to directly
	 * remove an AgentDescription object from the directory when an agent
	 * is removed from the node.
	 */
	public void removeAgentDescription(IFact agentDescription){
		space.remove(agentDescription);
	}
	
	public <E extends IFact> Set<E> processSearchRequest(E template){
		return space.readAll(template);
	}
	
	
	public void onInit(){
		_searchRequestHandler = new SearchRequestHandler();
		destroyer = EventedSpaceWrapper.getSpaceWithDestroyer(new SimpleObjectSpace<IFact>("FactBase"));
		space = destroyer.destroybleSpace;
		messageBus.setDefaultDelegate(_searchRequestHandler);
		
	}
	
	public void doStart(){
		myAddress = CommunicationAddressFactory.createMessageBoxAddress(getBeanName());
		try {
			messageBus.listen(myAddress, null);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
	}
	
	public void doStop(){
		try {
			messageBus.stopListen(myAddress, null);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
	}
	
	public void doCleanup(){
		try {
			destroyer.destroy();
		} catch (DestroyFailedException e) {
			e.printStackTrace();
		}
		space = null;
		destroyer = null;
	}

	public void setMessageTransport(MessageTransport mt){
		messageBus = mt;
	}
	
	
	
	private class SearchRequestHandler implements IMessageTransportDelegate {
		
		public Log getLog(String extension){
			return null;
		}
		
		@Override
		public void onAsynchronousException(MessageTransport source, Exception e) {
			e.printStackTrace();
			
		}
		@Override
		public void onMessage(MessageTransport source, IJiacMessage message,
				ICommunicationAddress at) {
			
			if (message.getPayload() instanceof SearchRequest){
				SearchRequest request = (SearchRequest) message.getPayload();
				
				Set<IFact> agents = space.readAll(request.getSearchTemplate());
				Set<AgentDescription> result = new HashSet<AgentDescription>();
				
				for (IFact agent : agents){
					if (agent instanceof AgentDescription) {
						result.add((AgentDescription) agent);
					}
				}
				
				request.setResult(result);
				
				IJiacMessage resultMessage = new JiacMessage(request);
				try {
					messageBus.send(resultMessage, message.getReplyToAddress());
				} catch (CommunicationException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		
	}
	
}
