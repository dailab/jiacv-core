package de.dailab.jiactng.agentcore.comm.wp;

import java.util.HashSet;
import java.util.Set;

import javax.security.auth.DestroyFailedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	public final static String PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp#DirectoryAgentNodeBean";
	
	private SpaceDestroyer<IAgentDescription> destroyer = null;
	private EventedTupleSpace<IAgentDescription> space = null;
	private MessageTransport messageTransport = null;
	private ICommunicationAddress myAddress = null; 

	private SearchRequestHandler _searchRequestHandler = null;
	
	public DirectoryAgentNodeBean() {
		destroyer = EventedSpaceWrapper.getSpaceWithDestroyer(new SimpleObjectSpace<IAgentDescription>("WhitePages"));
		space = destroyer.destroybleSpace;
	}

	/**
	 * This method is meant to give the AgentNode the option to directly
	 * add an AgentDescription object when an agent is added to the node.
	 */
	public void addAgentDescription(IAgentDescription agentDescription){
		if (agentDescription != null) {
			space.write(agentDescription);
		}
	}
	
	/**
	 * This method is meant to give the AgentNode the option to directly
	 * remove an AgentDescription object from the directory when an agent
	 * is removed from the node.
	 */
	public void removeAgentDescription(IAgentDescription agentDescription){
		if (agentDescription != null) {
			space.remove(agentDescription);
		}
	}
	
	
	public void doInit(){
		_searchRequestHandler = new SearchRequestHandler();
		messageTransport.setDefaultDelegate(_searchRequestHandler);
		try {
			messageTransport.doInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void doStart(){
		myAddress = CommunicationAddressFactory.createMessageBoxAddress(agentNode.getName() + SEARCHREQUESTSUFFIX);
		try {
			messageTransport.listen(myAddress, null);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
	}
	
	public void doStop(){
		try {
			messageTransport.stopListen(myAddress, null);
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
		messageTransport = mt;
	}
	
	
	
	private class SearchRequestHandler implements IMessageTransportDelegate {
		
		public Log getLog(String extension){
			//TODO Creating a method within the AgentNode to get a log for AgentNodeBeans and use it here
			return LogFactory.getLog(getClass().getName() + "." + extension);
		}
		
		@Override
		public void onAsynchronousException(MessageTransport source, Exception e) {
			e.printStackTrace();
			
		}
		@Override
		public void onMessage(MessageTransport source, IJiacMessage message,
				ICommunicationAddress at) {

			log.debug("got message " + message);
			if (message.getPayload() instanceof SearchRequest){
				SearchRequest request = (SearchRequest) message.getPayload();

				log.debug("Message is holding SearchRequest");
				if (request.getSearchTemplate() instanceof IAgentDescription){
					log.debug("SearchRequest hold SearchTemplate of type IAgentDescription");
					IAgentDescription template = (IAgentDescription) request.getSearchTemplate();

					log.debug("SearchRequest holds template " + template);
					Set<IAgentDescription> descriptions = space.readAll(template);
					Set<IFact> result = new HashSet<IFact>();
					result.addAll(descriptions);
					log.debug("Result to send reads " + result);
					
					SearchResponse response = new SearchResponse(request, result);

					JiacMessage resultMessage = new JiacMessage(response);
					resultMessage.setProtocol(DirectoryAgentNodeBean.PROTOCOL_ID);
					try {
						log.debug("AgentNode: sending Message " + resultMessage);
						log.debug("sending it to " + message.getSender());
						messageTransport.send(resultMessage, message.getSender());
					} catch (CommunicationException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
