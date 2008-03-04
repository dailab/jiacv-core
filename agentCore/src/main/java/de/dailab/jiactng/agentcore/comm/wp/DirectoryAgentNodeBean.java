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
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class DirectoryAgentNodeBean extends AbstractAgentNodeBean implements
		IAgentNodeBean {
	
	public final static String SEARCHREQUESTSUFFIX = "DirectoryAgentNodeBean";
	
	private SpaceDestroyer<IAgentDescription> destroyer = null;
	private EventedTupleSpace<IAgentDescription> space = null;
	private MessageTransport messageBus = null;
	private ICommunicationAddress myAddress = null; 

	private SearchRequestHandler _searchRequestHandler = null;
	
	public DirectoryAgentNodeBean() {
	}

	/**
	 * This method is meant to give the AgentNode the option to directly
	 * add an AgentDescription object when an agent is added to the node.
	 */
	public void addAgentDescription(IAgentDescription agentDescription){
		space.write(agentDescription);
	}
	
	/**
	 * This method is meant to give the AgentNode the option to directly
	 * remove an AgentDescription object from the directory when an agent
	 * is removed from the node.
	 */
	public void removeAgentDescription(IAgentDescription agentDescription){
		space.remove(agentDescription);
	}
	
	
	public void onInit(){
		_searchRequestHandler = new SearchRequestHandler();
		destroyer = EventedSpaceWrapper.getSpaceWithDestroyer(new SimpleObjectSpace<IAgentDescription>("WhitePages"));
		space = destroyer.destroybleSpace;
		messageBus.setDefaultDelegate(_searchRequestHandler);
		
	}
	
	public void doStart(){
	    // TODO: the address of this bean must contain the agentNode ID (for uniqueness)
		myAddress = CommunicationAddressFactory.createMessageBoxAddress(agentNode.getName() + SEARCHREQUESTSUFFIX);
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

				if (request.getSearchTemplate() instanceof IAgentDescription){

					IAgentDescription template = (IAgentDescription) request.getSearchTemplate();

					Set<IAgentDescription> descriptions = space.readAll(template);
					
					Set<IFact> result = new HashSet<IFact>();
					result.addAll(descriptions);
					
					SearchResponse response = new SearchResponse(request, result);

					IJiacMessage resultMessage = new JiacMessage(response);
					try {
						messageBus.send(resultMessage, message.getSender());
					} catch (CommunicationException e) {
						e.printStackTrace();
					}
				}
			}
			
		}
		
		
	}
	
}
