/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.service;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.DoRemoteAction;
import de.dailab.jiactng.agentcore.action.RemoteAction;
import de.dailab.jiactng.agentcore.action.RemoteActionResult;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.CommunicationBean;
import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.IJiacMessageListener;
import de.dailab.jiactng.agentcore.comm.message.IJiacContent;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ServiceBean extends AbstractMethodExposingBean implements IEffector, ResultReceiver {
    private static final String SERVICE_BROADCAST_ADDRESS= "JiacTNG/service/broadcast";
    private static final String PROTOCOL_KEY="JiacTNG-protocol-ID";
    private static final String SERVICE_PROTOCOL= "JiacTNG-service-protocol";
    private static final String SERVICE_OFFER_KEY= "Jiac-TNG-service-offer";
    
    private static final String ADD_OFFER= "add";
    private static final String REMOVE_OFFER= "remove";
    
    /**
     * This listener is used for processing incoming service requests and results.
     */
    private class ServiceExecutionListener implements IJiacMessageListener {
        public void receive(IJiacMessage message, ICommunicationAddress at) {
            log.debug("execution listener received something");
            IJiacContent content= message.getPayload();
            if(content instanceof RemoteActionResult) {
                processActionResult((RemoteActionResult) content);
            } else if (content instanceof DoRemoteAction) {
                processAction((DoRemoteAction) content, at.toUnboundAddress());
            } else {
                log.warn("unexpected content for this protocol '" + content + "'");
            }
        }
    }
    
    /**
     * This listener is used for collecting remote actions other agents offer.
     */
    private class ServiceManagementListener implements IJiacMessageListener {
        public void receive(IJiacMessage message, ICommunicationAddress at) {
            log.debug("management listener received something");
            IJiacContent content= message.getPayload();
            
            if(content instanceof RemoteAction) {
                String task= message.getHeader(SERVICE_OFFER_KEY);
                
                if(task.equals(ADD_OFFER)) {
                    insertAction((RemoteAction) content);
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
    
    private final Object _workLock= new Object();
    
    private CommunicationBean _communicationBean;
    
    private IGroupAddress _serviceBroadcastGroup;
    
    private IJiacMessageListener _executionListener;
    private IJiacMessageListener _managementListener;
    
    private Map<Action, RemoteActionContext> _actionToContext;
    private Map<String, ICommunicationAddress> _currentSessions;
    
    
    private final Set<Action> _offeredActions= new HashSet<Action>();
    
    @Override
    public void doCleanup() throws Exception {
        _communicationBean.unregister(_executionListener, PROTOCOL_KEY + "=" + SERVICE_PROTOCOL);
        _communicationBean.unregister(_managementListener, _serviceBroadcastGroup, null);
        _serviceBroadcastGroup= null;
        _managementListener= null;
        _executionListener= null;
        _communicationBean= null;
        
        for(Action action : _actionToContext.keySet()) {
            memory.remove(action);
        }
        
        _actionToContext= null;
        _currentSessions= null;
        super.doCleanup();
    }

    @Override
    public void doInit() throws Exception {
        super.doInit();
        for(IAgentBean bean : thisAgent.getAgentBeans()) {
            if(bean instanceof CommunicationBean) {
                _communicationBean= (CommunicationBean) bean;
                break;
            }
        }
        
        if(_communicationBean == null) {
            throw new IllegalStateException("could not find communication bean");
        }
        _actionToContext= new Hashtable<Action, RemoteActionContext>();
        _currentSessions= new Hashtable<String, ICommunicationAddress>();
        _executionListener= new ServiceExecutionListener();
        _managementListener= new ServiceManagementListener();
        _serviceBroadcastGroup= CommunicationAddressFactory.createGroupAddress(SERVICE_BROADCAST_ADDRESS);
        _communicationBean.register(_managementListener, _serviceBroadcastGroup, null);
        _communicationBean.register(_executionListener, PROTOCOL_KEY + "=" + SERVICE_PROTOCOL);
    }

    @Override
    public void doStart() throws Exception {
        super.doStart();
        
        synchronized (_offeredActions) {
            for(Action action : _offeredActions) {
                updateActionOffer(action, ADD_OFFER);
            }
        }
    }

    @Override
    public void doStop() throws Exception {
        synchronized (_offeredActions) {
            for(Action action : _offeredActions) {
                updateActionOffer(action, REMOVE_OFFER);
            }
        }
        
        super.doStop();
    }

    /**
     * Offers an action to other agents.
     * 
     * @param action    the action to expose to other agents
     * @throws CommunicationException
     */
    @Expose
    public void offerAction(Action action) throws CommunicationException {
        synchronized(_offeredActions) {
            if(_offeredActions.add(action)) {
                if(isActive()) {
                    updateActionOffer(action, ADD_OFFER);
                }
            }
        }
    }
    
    /**
     * Withdraws a previously offered action.
     * 
     * @param action    the action to withdraw
     * @throws CommunicationException
     */
    @Expose
    public void withdrawAction(Action action) throws CommunicationException {
        synchronized (_offeredActions) {
            if(_offeredActions.remove(action)) {
                if(isActive()) {
                    updateActionOffer(action, REMOVE_OFFER);
                }
            }
        }
    }
    
    protected void overrideDoAction(DoAction doAction) {
        Action action= doAction.getAction();
        RemoteActionContext context= _actionToContext.get(action);
        
        if(context != null) {
            IJiacMessage request= new JiacMessage(new DoRemoteAction(doAction));
            request.setHeader(PROTOCOL_KEY, SERVICE_PROTOCOL);
            try {
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
        ICommunicationAddress recipient= _currentSessions.remove(sessionId);
        
        if(recipient != null) {
            IJiacMessage response= new JiacMessage(new RemoteActionResult(result));
            response.setHeader(PROTOCOL_KEY, SERVICE_PROTOCOL);
            try {
                _communicationBean.send(response, recipient);
            } catch (CommunicationException ce) {
                log.error("could not send RemoteActionResult to '" + recipient + "'", ce);
            }
        } else {
            log.debug("could not find session '" + sessionId + "'");
        }
    }
    
    void insertAction(RemoteAction remoteAction) {
        synchronized(_workLock) {
            Action action= remoteAction.getAction();
            
            if(memory.read(new Action(action.getName(), null, action.getParameters(), action.getResults())) == null) {
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
            if(current.getProviderBean() == this) {
                memory.read(current);
            } else {
                log.debug("this bean is not the provider of action '" + action + "'");
            }
        }
    }
    
    void processAction(DoRemoteAction doRemoteAction, ICommunicationAddress requestSource) {
        DoAction doAction= doRemoteAction.getAction();
        // set this bean as result receiver
        doAction.setSource(this);
        doAction.getSession().setSource(this);
        
        Action action= doAction.getAction();
        Action current= memory.read(new Action(action.getName(), null, action.getParameters(), action.getResults()));
        
        if(current != null) {
            _currentSessions.put(doAction.getSessionId(), requestSource);
            memory.write(doAction);
            log.debug("delegated doAction request");
        } else {
            log.debug("action '" + action + "' is not provided here");
            // TODO return error
        }
    }
    
    void processActionResult(RemoteActionResult remoteActionResult) {
        memory.write(remoteActionResult.getResult());
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
        message.setHeader(SERVICE_OFFER_KEY, task);
        _communicationBean.send(message, _serviceBroadcastGroup);
    }
}
