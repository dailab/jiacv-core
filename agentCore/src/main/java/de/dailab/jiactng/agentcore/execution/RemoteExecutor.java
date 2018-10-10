package de.dailab.jiactng.agentcore.execution;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.log4j.Logger;
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

	/** The logger of this remote executor. */
	private Logger log;

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
	public RemoteExecutor(IMemory memory, Logger log) {
		this.memory = memory;
		this.log = log;
		
		final JiacMessage template = new JiacMessage();
		template.setProtocol(INVOKE);
		memory.attach(this, template);
		
		sendAction = memory.read(new Action(ICommunicationBean.ACTION_SEND,null,new Class[]{IJiacMessage.class, ICommunicationAddress.class},null));
	}
	
	@Override
	public void notify(SpaceEvent<? extends IFact> event) {
		if(event instanceof WriteCallEvent) {
			@SuppressWarnings("rawtypes")
			final WriteCallEvent writeCallEvent = (WriteCallEvent) event;
			if (writeCallEvent.getObject() instanceof JiacMessage){
				JiacMessage message = (JiacMessage)writeCallEvent.getObject();
				if ((message.getProtocol()) != null && (message.getProtocol().equals(INVOKE))) {
					if (memory.remove(message) == null) {
						log.warn("Received JIAC message already removed from memory by another agent component.");
					}

					if (message.getPayload() instanceof DoAction) {
						final DoAction doAction = (DoAction) message.getPayload();
						Action a = new Action((Action)doAction.getAction());
						a.setProviderDescription(((Action)doAction.getAction()).getProviderDescription());
						final Action localAction = (Action) memory.read(a);

						if (localAction != null) {
							final DoAction localDoAction = localAction.createDoAction(doAction.getSession(), doAction.getParams(), this);
							localInvocations.put(localDoAction.getSessionId(), message);	
							memory.write(localDoAction);
						} else {
							final RemoteExecutionException ree = new RemoteExecutionException("No action found at provider!" + doAction.getAction().getProviderDescription().getAid());
							final ActionResult failure = new ActionResult(doAction, ree);
							final JiacMessage failureMessage = new JiacMessage(failure);
							failureMessage.setProtocol(INVOKE);
							
							final Serializable[] params = new Serializable[]{failureMessage, message.getSender()};
							final DoAction sendDoAction = sendAction.createDoAction(params, this);
							memory.write(sendDoAction);
						}
					}
					
					else if (message.getPayload() instanceof ActionResult) {
						final ActionResult result = (ActionResult) message.getPayload();
						if(remoteInvocations.get(result.getSessionId())!=null) {
						  result.setSource(remoteInvocations.remove(result.getSessionId()));
						  memory.write(result);
						} else {
						  log.error("Received ActionResult with unknown session - result is dropped. "+result);
						}
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
		final ICommunicationAddress destination = doAction.getAction().getProviderDescription().getMessageBoxAddress();
		if (destination == null) {
			memory.write(new ActionResult(doAction, new RemoteExecutionException("Provider has no address!")));
			return;
		}
		
		final JiacMessage message = new JiacMessage(doAction);
		message.setProtocol(INVOKE);
		
		final Serializable[] params = new Serializable[] {message, destination};
		final DoAction sendDoAction = sendAction.createDoAction(doAction.getSession(), params, this);
		memory.write(sendDoAction);
		remoteInvocations.put(doAction.getSessionId(), doAction);
		
        final Action act = (Action) doAction.getAction();
        try {
            if ((act.getResultTypeNames() != null) && (act.getResultTypeNames().size() > 0)) {
                final Session session = doAction.getSession();
                if (session.getCurrentCallDepth() == null) {
                    session.setCurrentCallDepth(1);
                } else {
                    session.setCurrentCallDepth(session.getCurrentCallDepth().intValue() + 1);
                }
                if (log.isInfoEnabled()) {
                    log.info("Writing session for " + act.getName() + " to memory: " + doAction.getSessionId() + " ("
                            + session.getCurrentCallDepth() + ")");
                }
                memory.write(doAction.getSession());
            }
        } catch (Exception e) {
            log.error("Exception when forwarding remote action: "+act,e);
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

		final JiacMessage message = localInvocations.get(result.getSessionId());
		if(message == null) {
		  log.error("Received ActionResult with unknown session - result is dropped. "+result);
		  return;
		}
 		
		final DoAction doAction = (DoAction)message.getPayload();
		ActionResult resultToSend;
		if (result.getResults() != null) {
			final Action action = (Action)doAction.getAction();
			resultToSend = action.createActionResult(doAction, result.getResults());
		} else {
			resultToSend = new ActionResult(doAction, result.getFailure());
		}
		final JiacMessage resultMessage = new JiacMessage(resultToSend);
		resultMessage.setProtocol(INVOKE);

		final Serializable[] params = new Serializable[]{resultMessage, message.getSender()};
		final DoAction sendDoAction = sendAction.createDoAction(params, this);
		memory.write(sendDoAction);
		localInvocations.remove(result.getSessionId());
	}
}
