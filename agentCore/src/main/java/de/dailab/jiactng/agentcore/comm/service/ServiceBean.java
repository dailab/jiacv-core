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
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.OtherAgentDescription;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ServiceBean extends AbstractMethodExposingBean implements IEffector, IServiceBean, ResultReceiver {
    private static final String SERVICE_BROADCAST_ADDRESS= "JiacTNG/service/broadcast";
    private static final String SERVICE_EXECUTION_PROTOCOL= "JiacTNG-service-exec-protocol";
    private static final String SERVICE_MANAGEMENT_PROTOCOL= "JiacTNG-service-mgmt-protocol";
    private static final String SERVICE_SEARCH_PROTOCOL= "JiacTNG-service-search-protocol";
    private static final String SERVICE_OFFER_KEY= "JiacTNGServiceOffer";
    
    private static final JiacMessage EXECUTION_MESSAGE_TEMPLATE;
    private static final JiacMessage MANAGEMENT_MESSAGE_TEMPLATE;
    private static final JiacMessage SEARCH_MESSAGE_TEMPLATE;
    
    private static final String ADD_OFFER= "add";
    private static final String REMOVE_OFFER= "remove";
    
    static {
        EXECUTION_MESSAGE_TEMPLATE= new JiacMessage();
        EXECUTION_MESSAGE_TEMPLATE.setHeader(IJiacMessage.Header.PROTOCOL, SERVICE_EXECUTION_PROTOCOL);
        
        MANAGEMENT_MESSAGE_TEMPLATE= new JiacMessage();
        MANAGEMENT_MESSAGE_TEMPLATE.setHeader(IJiacMessage.Header.PROTOCOL, SERVICE_MANAGEMENT_PROTOCOL);
        
        SEARCH_MESSAGE_TEMPLATE= new JiacMessage();
        SEARCH_MESSAGE_TEMPLATE.setHeader(IJiacMessage.Header.PROTOCOL, SERVICE_SEARCH_PROTOCOL);
    }
    
    @SuppressWarnings("serial")
    private class ServiceExecutionProtocol implements SpaceObserver<IFact> {
        @SuppressWarnings("unchecked")
        public void notify(SpaceEvent<? extends IFact> event) {
            if(event instanceof WriteCallEvent) {
                WriteCallEvent<IJiacMessage> wce= (WriteCallEvent<IJiacMessage>) event;
                IJiacMessage message= memory.remove(wce.getObject());
                IFact content= message.getPayload();
                
                if(content instanceof RemoteActionResult) {
                    processActionResult((RemoteActionResult) content);
                } else if (content instanceof DoRemoteAction) {
                    processAction((DoRemoteAction) content, message.getSender().toUnboundAddress());
                } else {
                    log.warn("unexpected content for '" + SERVICE_EXECUTION_PROTOCOL + "': '" + content + "'");
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
                IFact content= message.getPayload();
                
                if(content instanceof RemoteAction) {
                    String task= message.getHeader(SERVICE_OFFER_KEY).toString();
                    
                    if(task.equals(ADD_OFFER)) {
                        insertAction((RemoteAction) content);
                    } else if(task.equals(REMOVE_OFFER)) {
                        removeAction((RemoteAction) content);
                    } else {
                        log.warn("unexpected task '" + task + "' for offer");
                    }
                } else {
                    log.warn("unexpected content for '" + SERVICE_MANAGEMENT_PROTOCOL + "': '" + content + "'");
                }
            }
        }
    }
    
    @SuppressWarnings("serial")
    private class ServiceSearchProtocol implements SpaceObserver<IFact> {
        @SuppressWarnings("unchecked")
        public void notify(SpaceEvent<? extends IFact> event) {
            if(event instanceof WriteCallEvent) {
                WriteCallEvent<IJiacMessage> wce= (WriteCallEvent<IJiacMessage>) event;
                IJiacMessage message= memory.remove(wce.getObject());
                IFact content= message.getPayload();
                
                if(content instanceof Action) {
                    ICommunicationAddress address= message.getSender();
                    if(!thisAgent.getAgentDescription().getMessageBoxAddress().equals(address)) {
                        processActionSearch((Action) content, address);
                    }
                } else if (content instanceof RemoteAction) {
                    insertAction((RemoteAction) content);
                } else {
                    log.warn("unexpected content for '" + SERVICE_SEARCH_PROTOCOL + "': '" + content + "'");
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
     * This protocol is responsible to answer requests for
     * remote actions.
     */
    private ServiceSearchProtocol _searchProtocol;
    
    /**
     * This map provides information of which communication
     * address has to be used for a given action.
     */
    private Map<Action, Set<RemoteAction>> _actionToRemoteAction;
    
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
        _communicationBean.unregister(thisAgent.getAgentDescription().getMessageBoxAddress(), EXECUTION_MESSAGE_TEMPLATE);
        _communicationBean.unregister(thisAgent.getAgentDescription().getMessageBoxAddress(), SEARCH_MESSAGE_TEMPLATE);
        _communicationBean.unregister(_serviceBroadcastGroup, SEARCH_MESSAGE_TEMPLATE);
        _communicationBean.unregister(_serviceBroadcastGroup, MANAGEMENT_MESSAGE_TEMPLATE);
        _serviceBroadcastGroup= null;
        memory.detach(_executionProtocol);
        memory.detach(_executionProtocol);
        _managementProtocol= null;
        _executionProtocol= null;
        _communicationBean= null;
        
        for(Action action : _actionToRemoteAction.keySet()) {
            memory.remove(action);
        }
        
        _actionToRemoteAction= null;
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
        _actionToRemoteAction= new Hashtable<Action, Set<RemoteAction>>();
        _sessionsFromExternalClients= new Hashtable<String, ICommunicationAddress>();
        _sessionsToExternalProviders= new Hashtable<String, DoAction>();
        
        _executionProtocol= new ServiceExecutionProtocol();
        _managementProtocol= new ServiceManagementProtocol();
        _searchProtocol= new ServiceSearchProtocol();
        memory.attach(_executionProtocol, EXECUTION_MESSAGE_TEMPLATE);
        memory.attach(_managementProtocol, MANAGEMENT_MESSAGE_TEMPLATE);
        memory.attach(_searchProtocol, SEARCH_MESSAGE_TEMPLATE);
        
        _serviceBroadcastGroup= CommunicationAddressFactory.createGroupAddress(SERVICE_BROADCAST_ADDRESS);
        _communicationBean.register(_serviceBroadcastGroup, MANAGEMENT_MESSAGE_TEMPLATE);
        _communicationBean.register(_serviceBroadcastGroup, SEARCH_MESSAGE_TEMPLATE);
        _communicationBean.register(thisAgent.getAgentDescription().getMessageBoxAddress(), SEARCH_MESSAGE_TEMPLATE);
        _communicationBean.register(thisAgent.getAgentDescription().getMessageBoxAddress(), EXECUTION_MESSAGE_TEMPLATE);
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
    
    public boolean offerAction(Action template) {
        synchronized(_actionTemplates) {
            return _actionTemplates.add(template);
        }
    }
    
    public boolean withdrawAction(Action template) {
        synchronized(_actionTemplates) {
            return _actionTemplates.remove(template);
        }
    }
    
    public void searchRemoteAction(Action template) throws CommunicationException {
        IJiacMessage request= new JiacMessage(template);
        request.setHeader(IJiacMessage.Header.PROTOCOL, SERVICE_SEARCH_PROTOCOL);
        _communicationBean.send(request, _serviceBroadcastGroup);
    }

    protected void overrideDoAction(DoAction doAction) {
        Action action= doAction.getAction();
        synchronized(_workLock) {
            Set<RemoteAction> remoteActions= _actionToRemoteAction.get(action);
            
            if(remoteActions != null && remoteActions.size() > 0) {
                RemoteAction remoteAction= remoteActions.iterator().next();
                IJiacMessage request= new JiacMessage(new DoRemoteAction(doAction));
                ResultReceiver receiver= (ResultReceiver) doAction.getSource();
                
                if(receiver != null) {
                    _sessionsToExternalProviders.put(doAction.getSessionId(), doAction);
                }
                
                request.setHeader(IJiacMessage.Header.PROTOCOL, SERVICE_EXECUTION_PROTOCOL);
                try {
                    log.debug("send request for remote action to '" + remoteAction.getAgentDescription().getMessageBoxAddress() + "'");
                    _communicationBean.send(request, remoteAction.getAgentDescription().getMessageBoxAddress());
                } catch (CommunicationException ce) {
                    log.error("could not send DoRemoteAction to '" + remoteAction.getAgentDescription().getMessageBoxAddress() + "'", ce);
                }
            } else {
                log.debug("could not find action: '" + action.getName() + "'");
            }
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
    
    void insertAction(RemoteAction remoteAction) {
        if(remoteAction.getAgentDescription().equals(thisAgent.getAgentDescription())) {
            return;
        }
        
        synchronized(_workLock) {
            Action action= remoteAction.getAction();
            
            Set<RemoteAction> remoteActions= _actionToRemoteAction.get(action);
            
            if(remoteActions == null) {
                remoteActions= new HashSet<RemoteAction>();
                _actionToRemoteAction.put(action, remoteActions);
                action.setProviderBean(this);
                memory.write(action);
                log.debug("new remote action for '" + action + "' available ");
            }
            
            if(remoteActions.add(remoteAction)) {
                log.debug("new provider for '" + remoteAction + "' available");
            }
        }
    }
    
    void removeAction(RemoteAction remoteAction) {
        if(remoteAction.getAgentDescription().equals(thisAgent.getAgentDescription())) {
            return;
        }

        synchronized (_workLock) {
            Action action= remoteAction.getAction();
            Set<RemoteAction> remoteActions= _actionToRemoteAction.remove(action);
            remoteActions.remove(remoteAction);
            
            if(remoteActions.isEmpty()) {
                action.setProviderBean(this);
                memory.remove(action);
                log.debug("remote action for '" + action + "' is no longer available");
            } else {
                _actionToRemoteAction.put(action, remoteActions);
                log.debug("removed provider of '" + remoteAction + "'");
            }
        }
    }
    
    void processActionSearch(Action action, ICommunicationAddress address) {
        log.debug("received action search request from '" + address + "'");
        synchronized (_workLock) {
            Action concreteAction= memory.read(action);
            
            if(concreteAction != null) {
                // check whether we are allowed to offer this action
                Set<Action> allowedAction= new HashSet<Action>();
                for(Action template : _actionTemplates) {
                    allowedAction.addAll(memory.readAll(template));
                }
                
                if(!allowedAction.contains(concreteAction)) {
                    log.debug("rejected it");
                } else {
                    log.debug("answer it");
                    _offeredActions.add(concreteAction);
                    try {
                        IJiacMessage message= new JiacMessage(new RemoteAction(concreteAction, new OtherAgentDescription(thisAgent.getAgentDescription())));
                        message.setHeader(IJiacMessage.Header.PROTOCOL, SERVICE_SEARCH_PROTOCOL);
                        _communicationBean.send(message, address);
                    } catch (CommunicationException ce) {
                        log.error("could not answer action search request", ce);
                    }
                }
            } else {
                log.debug("do not have the requested action");
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
        IJiacMessage message= new JiacMessage(new RemoteAction(action, new OtherAgentDescription(thisAgent.getAgentDescription())));
        message.setHeader(IJiacMessage.Header.PROTOCOL, SERVICE_MANAGEMENT_PROTOCOL);
        message.setHeader(SERVICE_OFFER_KEY, task);
        _communicationBean.send(message, _serviceBroadcastGroup);
    }
}
