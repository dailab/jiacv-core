package de.dailab.jiactng.agentcore.comm.wp;

import org.apache.commons.logging.Log;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport.IMessageTransportDelegate;
import de.dailab.jiactng.agentcore.knowledge.IFact;

public class DirectoryAccessBean extends AbstractAgentBean implements
IAgentBean {

	private MessageTransport messageTransport = null;
	private ICommunicationAddress directoryAddress = null;
//	private ICommunicationAddress myAddress = null; 
	private SearchRequestHandler _searchRequestHandler = null;

	public DirectoryAccessBean() {
		setBeanName("DirectoryAccessBean");
		String name = thisAgent.getAgentName() + getBeanName();

//      Die Addresse des Agenten steht ab der Initialisierungsphase im Memory!
//      siehe CommunicationBean
//		myAddress = CommunicationAddressFactory.createMessageBoxAddress(name);
		String boxName = thisAgent.getAgentNode().getName() + DirectoryAgentNodeBean.SEARCHREQUESTSUFFIX;
		
		// TODO: wir muessen einen Weg finden, die Addresse des Verzeichnisses zu dieser
		//       Bean zu kommunizieren. MessageBoxAddressen sollten allerdings nicht auf
		//       Agentenebene erzeugt werden!
		directoryAddress = CommunicationAddressFactory.createMessageBoxAddress(boxName);
	}

	public DirectoryAccessBean(boolean strict) {
		super(strict);
		// TODO Auto-generated constructor stub
	}

	public <E extends IFact> void requestSearch(E template){
		_searchRequestHandler.requestSearch(template);
	}

	public void onInit(){
		_searchRequestHandler = new SearchRequestHandler();
		messageTransport.setDefaultDelegate(_searchRequestHandler);
	}

//	public void onStart(){
//		try {
//			messageBus.listen(myAddress, null);
//		} catch (CommunicationException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void onStop(){
//		try {
//			messageBus.stopListen(myAddress, null);
//		} catch (CommunicationException e) {
//			e.printStackTrace();
//		}
//	}

	public void onCleanup(){
		
	}

	public void setMessageTransport(MessageTransport mt){
		messageTransport = mt;
	}

	private class SearchRequestHandler implements IMessageTransportDelegate {

		public Log getLog(String extension){
			return null;
		}

		@Override
		public void onAsynchronousException(MessageTransport source, Exception e) {
			e.printStackTrace();

		}

		public <E extends IFact> void requestSearch(E template){
//			IJiacMessage message = new JiacMessage(template, myAddress);
//          Absender wird von der CommunicationBean selber gesetzt
		    IJiacMessage message = new JiacMessage(template);
			try {
				messageTransport.send(message, directoryAddress);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onMessage(MessageTransport source, IJiacMessage message,
				ICommunicationAddress at) {

			if (message.getPayload() instanceof SearchRequest){
				SearchRequest request = (SearchRequest) message.getPayload();

				System.out.println(request.getResult());

			}

		}


	}

}
