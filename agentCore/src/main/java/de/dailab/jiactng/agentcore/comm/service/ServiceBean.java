/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.service;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.DoRemoteAction;
import de.dailab.jiactng.agentcore.action.RemoteAction;
import de.dailab.jiactng.agentcore.action.RemoteActionResult;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacContent;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ServiceBean extends AbstractMethodExposingBean implements IEffector, ResultReceiver {
    private static final String SERVICE_BROADCAST_ADDRESS= "JiacTNG/service/broadcast";
    private static final String SERVICE_EXECUTION_PROTOCOL= "JiacTNG-service-exec-protocol";
    private static final String SERVICE_MANAGEMENT_PROTOCOL= "JiacTNG-service-mgmt-protocol";
    private static final String SERVICE_OFFER_KEY= "JiacTNGServiceOffer";
    
    private static final JiacMessage EXECUTION_MESSAGE_TEMPLATE;
    private static final JiacMessage MANAGEMENT_MESSAGE_TEMPLATE;
    
    private static final String ADD_OFFER= "add";
    private static final String REMOVE_OFFER= "remove";
    
    static {
        EXECUTION_MESSAGE_TEMPLATE= new JiacMessage();
        EXECUTION_MESSAGE_TEMPLATE.setHeader(IJiacMessage.Header.PROTOCOL, SERVICE_EXECUTION_PROTOCOL);
        
        MANAGEMENT_MESSAGE_TEMPLATE= new JiacMessage();
        MANAGEMENT_MESSAGE_TEMPLATE.setHeader(IJiacMessage.Header.PROTOCOL, SERVICE_MANAGEMENT_PROTOCOL);
    }
    
    @SuppressWarnings("serial")
    private class ServiceExecutionProtocol implements SpaceObserver<IFact> {
        @SuppressWarnings("unchecked")
        public void notify(SpaceEvent<? extends IFact> event) {
            if(event instanceof WriteCallEvent) {
                WriteCallEvent<IJiacMessage> wce= (WriteCallEvent<IJiacMessage>) event;
                IJiacMessage message= memory.remove(wce.getObject());
                IJiacContent content= message.getPayload();
                
                if(content instanceof RemoteActionResult) {
                    processActionResult((RemoteActionResult) content);
                } else if (content instanceof DoRemoteAction) {
                    processAction((DoRemoteAction) content, message.getSender().toUnboundAddress());
                } else {
                    log.warn("unexpected content for this protocol '" + content + "'");
                }
            }
        }
    }
    
    @SuppressWarnings("serial")
    private class ServiceManagementProtocol implements SpaceObserver<IFact> {
        @SuppressWarnings("unchecked")
        public void notify(SpaceEvent<? extends IFact> event) {
            if(event instanceof WriteCallEvent) {
                WriteCallEvent<IJiacMessage> wce= (WriteCallEvent<IJiacMessage>) event;
                IJiacMessage message= memory.remove(wce.getObject());
                IJiacContent content= message.getPayload();
                
                if(content instanceof RemoteAction) {
                    String task= message.getHeader(SERVICE_OFFER_KEY).toString();
                    
                    if(task.equals(ADD_OFFER)) {
                        insertAction((RemoteAction) content, message.getSender().toUnboundAddress());
                    } else if(task.equals(REMOVE_OFFER)) {
                        removeAction((RemoteAction) content);
                    } else {
                        log.warn("unexpected task '" + task + "' for offer");
                    }
                } else {
                    log.warn("unexpected content for this protocol '" + content + "'");
                }
            }
        }
    }
    
    private final Object _workLock= new Object();
    
    private ICommunicationBean _communicationBean;
    
    /**
     * The group to broadcast offering and withdrawing information.
     */
    private IGroupAddress _serviceBroadcastGroup;
    
    /**
     * This protocol is responsible to forward the action request
     * to the specific provider <strong>in this</strong> agent.
     */
    private ServiceExecutionProtocol _executionProtocol;
    
    /**
     * The protocol that is responsible to update the memory when
     * remote actions are offered or withdrawn.
     */
    private ServiceManagementProtocol _managementProtocol;
    
    /**
     * This map provides information of which communication
     * address has to be used for a given action.
     */
    private Map<Action, RemoteActionContext> _actionToContext;
    
    /**
     * This map contains all sessions from external clients that are currently open.
     */
    private Map<String, ICommunicationAddress> _sessionsFromExternalClients;
    
    /**
     * This map contains all sessions to external providers that are currently open.
     */
    private Map<String, DoAction> _sessionsToExternalProviders;
    
    /**
     * This set contains all concrete actions that are currently offered
     * (and thus communicated) to other agents.
     */
    private final Set<Action> _offeredActions= new HashSet<Action>();
    
    /**
     * This set contains all action templates whose matching actions should be provided to other
     * agents.
     */
    private Set<Action> _actionTemplates= new HashSet<Action>();

    public void setActionTemplates(Set<Action> actionTemplates) {
        if(actionTemplates != null) {
            _actionTemplates= actionTemplates;
        }
    }
    
    @Override
    public void doCleanup() throws Exception {
        log.debug("cleanup ServiceBean...");
        _communicationBean.unregister(CommunicationAddressFactory.createMessageBoxAddress(thisAgent.getAgentName()), EXECUTION_MESSAGE_TEMPLATE);
        _communicationBean.unregister(_serviceBroadcastGroup, MANAGEMENT_MESSAGE_TEMPLATE);
        _serviceBroadcastGroup= null;
        memory.detach(_executionProtocol);
        memory.detach(_executionProtocol);
        _managementProtocol= null;
        _executionProtocol= null;
        _communicationBean= null;
        
        for(Action action : _actionToContext.keySet()) {
            memory.remove(action);
        }
        
        _actionToContext= null;
        _sessionsFromExternalClients= null;
        super.doCleanup();
    }

    @Override
    public void doInit() throws Exception {
        super.doInit();
        log.debug("initialise ServiceBean...");
//        for(IAgentBean bean : thisAgent.getAgentBeans()) {
//            if(bean instanceof CommunicationBean) {
//                _communicationBean= (CommunicationBean) bean;
//                break;
//            }
//        }
        
        _communicationBean= thisAgent.getActionInvocationHandler().getInvocatorInstance(ICommunicationBean.class);
//        
//        if(_communicationBean == null) {
//            throw new IllegalStateException("could not find communication bean");
//        }
        _actionToContext= new Hashtable<Action, RemoteActionContext>();
        _sessionsFromExternalClients= new Hashtable<String, ICommunicationAddress>();
        _sessionsToExternalProviders= new Hashtable<String, DoAction>();
        
        _executionProtocol= new ServiceExecutionProtocol();
        _managementProtocol= new ServiceManagementProtocol();
        memory.attach(_executionProtocol, EXECUTION_MESSAGE_TEMPLATE);
        memory.attach(_managementProtocol, MANAGEMENT_MESSAGE_TEMPLATE);
        
        _serviceBroadcastGroup= CommunicationAddressFactory.createGroupAddress(SERVICE_BROADCAST_ADDRESS);
        _communicationBean.register(_serviceBroadcastGroup, MANAGEMENT_MESSAGE_TEMPLATE);
        _communicationBean.register(CommunicationAddressFactory.createMessageBoxAddress(thisAgent.getAgentName()), EXECUTION_MESSAGE_TEMPLATE);
    }

    @Override
    public void doStart() throws Exception {
        super.doStart();
        log.debug("starting ServiceBean...");
    }

    @Override
    public void doStop() throws Exception {
        log.debug("stopping ServiceBean...");
        synchronized (_offeredActions) {
            for(Action action : _offeredActions) {
                updateActionOffer(action, REMOVE_OFFER);
            }
            
            _offeredActions.clear();
        }
        
        super.doStop();
    }

    
    @Override
    public void execute() {
        Set<Action> toProcess= new HashSet<Action>();
        synchronized(_actionTemplates) {
            log.debug("walk through templates...");
            for(Action template : _actionTemplates) {
                toProcess.addAll(memory.readAll(template));
            }
        }
        
        synchronized(_offeredActions) {
            if(isActive()) {
                log.debug("update offers...");
                for(Action toAdd : toProcess) {
                        try {
                            updateActionOffer(toAdd, ADD_OFFER);
                        } catch (Exception e) {
                            log.debug("could not offer action '" + toAdd + "'", e);
                        }
                }
                
                for(Iterator<Action> iter= _offeredActions.iterator(); iter.hasNext();) {
                    Action toRemove= iter.next();
                    if(!toProcess.remove(toRemove)) {
                        // no template matched the currently offered action
                        try {
                            updateActionOffer(toRemove, REMOVE_OFFER);
                            iter.remove();
                        } catch (Exception e) {
                            log.debug("could not withdraw action '" + toRemove + "'", e);
                        }
                    }
                }
                
                // save newly offered actions for next run
                _offeredActions.addAll(toProcess);
            }
        }
    }
    
    /**
     * Offers an action to other agents.
     * 
     * @param action    the action template to expose to other agents
     */
    @Expose
    public boolean offerAction(Action template) {
        synchronized(_actionTemplates) {
            return _actionTemplates.add(template);
        }
    }
    
    /**
     * Withdraws a previously offered action.
     * 
     * @param action    the action template to withdraw
     */
    @Expose
    public boolean withdrawAction(Action template) {
        synchronized(_actionTemplates) {
            return _actionTemplates.remove(template);
        }
    }

    protected void overrideDoAction(DoAction doAction) {
        Action action= doAction.getAction();
        RemoteActionContext context= _actionToContext.get(action);
        
        if(context != null) {
            IJiacMessage request= new JiacMessage(new DoRemoteAction(doAction));
            ResultReceiver receiver= (ResultReceiver) doAction.getSource();
            
            if(receiver != null) {
                _sessionsToExternalProviders.put(doAction.getSessionId(), doAction);
            }
            
            request.setHeader(IJiacMessage.Header.PROTOCOL, SERVICE_EXECUTION_PROTOCOL);
            try {
                log.debug("send request for remote action to '" + context.providerAddress + "'");
                _communicationBean.send(request, context.providerAddress);
            } catch (CommunicationException ce) {
                log.error("could not send DoRemoteAction to '" + context.providerAddress + "'", ce);
            }
        } else {
            log.debug("could not find action: '" + action.getName() + "'");
        }
    }

    public void receiveResult(ActionResult result) {
        DoAction doAction= (DoAction) result.getSource();
        String sessionId= doAction.getSessionId();
        ICommunicationAddress recipient= _sessionsFromExternalClients.remove(sessionId);
        
        if(recipient != null) {
            IJiacMessage response= new JiacMessage(new RemoteActionResult(result));
            response.setHeader(IJiacMessage.Header.PROTOCOL, SERVICE_EXECUTION_PROTOCOL);
            try {
                log.debug("send result for session '" + sessionId + "' to '" + recipient + "'");
                _communicationBean.send(response, recipient);
            } catch (CommunicationException ce) {
                log.error("could not send RemoteActionResult to '" + recipient + "'", ce);
            }
        } else {
            log.debug("could not find session '" + sessionId + "'");
        }
    }
    
    void insertAction(RemoteAction remoteAction, ICommunicationAddress provider) {
        synchronized(_workLock) {
            Action action= remoteAction.getAction();
            
            if(memory.read(new Action(action.getName(), null, action.getParameters(), action.getResults())) == null) {
                _actionToContext.put(action, new RemoteActionContext(remoteAction, provider));
                action.setProviderBean(this);
                memory.write(action);
                log.debug("new remote action available: '" + action + "'");
            } else {
                log.debug("action '" + action + "' already exists");
            }
        }
    }
    
    void removeAction(RemoteAction remoteAction) {
        synchronized (_workLock) {
            Action action= remoteAction.getAction();
            _actionToContext.remove(action);
            Action current= memory.read(new Action(action.getName(), null, action.getParameters(), action.getResults()));
            if(current == null) {
                log.debug("action '" + action + "' is already removed");
            } else if(current.getProviderBean() == this) {
                log.debug("removed action '" + action + "' from memory");
                memory.remove(current);
            } else {
                log.debug("this bean is not the provider of action '" + action + "'");
            }
        }
    }
    
    void processAction(DoRemoteAction doRemoteAction, ICommunicationAddress requestSource) {
        log.debug("got some request from '" + requestSource + "'");
        DoAction doAction= doRemoteAction.getAction();
        // set this bean as result receiver
        doAction.setSource(this);
        doAction.getSession().setSource(this);
        
        Action action= doAction.getAction();
        Action current= memory.read(new Action(action.getName(), null, action.getParameters(), action.getResults()));
        
        if(current != null) {
            _sessionsFromExternalClients.put(doAction.getSessionId(), requestSource);
            action.setProviderBean(current.getProviderBean());
            memory.write(doAction);
            log.debug("delegated doAction request");
        } else {
            log.debug("action '" + action + "' is not provided here");
            // TODO return error
        }
    }
    
    void processActionResult(RemoteActionResult remoteActionResult) {
        ActionResult result= remoteActionResult.getResult();
        log.debug("got action result for session '" + result.getSessionId() + "'");
        DoAction doAction= _sessionsToExternalProviders.get(result.getSessionId());
        result.setSource(doAction);
        memory.write(result);
    }
    
    private boolean isActive() {
        switch(getState()) {
            case INITIALIZED: case STARTING: case STARTED: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    private void updateActionOffer(Action action, String task) throws CommunicationException {
        // TODO where to get the agentDescription from???
        IJiacMessage message= new JiacMessage(new RemoteAction(action, null));
        message.setHeader(IJiacMessage.Header.PROTOCOL, SERVICE_MANAGEMENT_PROTOCOL);
        message.setHeader(SERVICE_OFFER_KEY, task);
        _communicationBean.send(message, _serviceBroadcastGroup);
    }
}
