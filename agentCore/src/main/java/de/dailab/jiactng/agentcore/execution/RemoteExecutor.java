package de.dailab.jiactng.agentcore.execution;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.Session;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.knowledge.IMemory;

/**
 * Handles remote agent action invocation.
 * 
 * @author axle
 */
public class RemoteExecutor implements SpaceObserver<IFact>, ResultReceiver {
	private static final long serialVersionUID = -6391933561226831472L;

	/** The "invoke" protocol id.*/
	private static final String INVOKE = "invoke";
	
	/** Communication with agent beans needs the memory.*/
	private IMemory memory;
	
	/** sendAction of CommunicationBean */
	private Action sendAction;

	/** Remote invocations not yet replied on. Key is SessionId.*/
	private HashMap<String, DoAction> remoteInvocations = new HashMap<String, DoAction>();
	
	/** Local invocations, no results yet. Key is SessionId.*/
	private HashMap<String, JiacMessage> localInvocations = new HashMap<String, JiacMessage>();

	/**
	 * Default constructor. Starts listening to special JiacMessages.
	 * 
	 * @param memory the space to listen to
	 * @see IMemory
	 */
	public RemoteExecutor(IMemory memory) {
		this.memory = memory;
		
		JiacMessage template = new JiacMessage();
		template.setProtocol(INVOKE);
		memory.attach(this, template);
		
		sendAction = memory.read(new Action(ICommunicationBean.ACTION_SEND,null,new Class[]{IJiacMessage.class, ICommunicationAddress.class},null));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void notify(SpaceEvent<? extends IFact> event) {
		if(event instanceof WriteCallEvent) {
			WriteCallEvent writeCallEvent = (WriteCallEvent) event;
			if (writeCallEvent.getObject() instanceof JiacMessage){
				JiacMessage message = (JiacMessage)writeCallEvent.getObject();
				if (message.getProtocol().equals(INVOKE)) {
					message = memory.remove(message);
					
					if (message.getPayload() instanceof DoAction) {
						DoAction doAction = (DoAction) message.getPayload();
						Action localAction = (Action) memory.read(doAction.getAction());
						
						if (localAction != null) {
							DoAction localDoAction = localAction.createDoAction(doAction.getSession(), doAction.getParams(), this);
							localInvocations.put(localDoAction.getSessionId(), message);	
							memory.write(localDoAction);
						} else {
							RemoteExecutionException ree = new RemoteExecutionException("No action found at provider!" + doAction.getAction().getProviderDescription().getAid());
							ActionResult failure = new ActionResult(doAction, ree);
							JiacMessage failureMessage = new JiacMessage(failure);
							failureMessage.setProtocol(INVOKE);
							
							Serializable[] params = new Serializable[]{failureMessage, message.getSender()};
							DoAction sendDoAction = sendAction.createDoAction(params, this);
							memory.write(sendDoAction);
						}
					}
					
					else if (message.getPayload() instanceof ActionResult) {
						ActionResult result = (ActionResult) message.getPayload();
						result.setSource(remoteInvocations.get(result.getSessionId()));
						memory.write(result);
					}
					
					else {
						System.err.println("Got strange message: " + message.toString());
					}
				}
			}
		}
	}

	/**
	 * Send DoActions to their providers.
	 * 
	 * @param doAction the action to be invoked remotely
	 */
	public void executeRemote(DoAction doAction) {
		ICommunicationAddress destination = doAction.getAction().getProviderDescription().getMessageBoxAddress();
		if (destination == null) {
			memory.write(new ActionResult(doAction, new RemoteExecutionException("Provider has no address!")));
			return;
		}
		
		JiacMessage message = new JiacMessage(doAction);
		message.setProtocol(INVOKE);
		
		Serializable[] params = new Serializable[] {message, destination};
		DoAction sendDoAction = sendAction.createDoAction(params, this);
		memory.write(sendDoAction);
		remoteInvocations.put(doAction.getSessionId(), doAction);
		
		Action act = (Action)doAction.getAction();
        try {
			if ((act.getResultTypes() != null)
					&& (act.getResultTypes().size() > 0)) {
				Session session = doAction.getSession();
				if (session.getCurrentCallDepth() == null) {
					session.setCurrentCallDepth(1);
				} else {
					session.setCurrentCallDepth(session.getCurrentCallDepth() + 1);
				}
				memory.write(doAction.getSession());
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/** Use this method to cleanup when RemoteExecutor is no longer used.*/
	public void cleanup() {
		memory.detach(this);
	}

	@Override
	public void receiveResult(ActionResult result) {
		if (result.getAction().equals(sendAction)) {
			return;
		}

		JiacMessage message = localInvocations.get(result.getSessionId());
		DoAction doAction = (DoAction)message.getPayload();
		ActionResult resultToSend;
		if (result.getResults() != null) {
			Action action = (Action)doAction.getAction();
			resultToSend = action.createActionResult(doAction, result.getResults());
		} else {
			resultToSend = new ActionResult(doAction, result.getFailure());
		}
		JiacMessage resultMessage = new JiacMessage(resultToSend);
		resultMessage.setProtocol(INVOKE);

		Serializable[] params = new Serializable[]{resultMessage, message.getSender()};
		DoAction sendDoAction = sendAction.createDoAction(params, this);
		memory.write(sendDoAction);
	}
}
