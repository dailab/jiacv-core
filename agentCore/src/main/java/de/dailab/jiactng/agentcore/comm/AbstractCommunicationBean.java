/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.comm.message.IEndPoint;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;

/**
 * This bean specifies the way an agent communicates. It implements a messaged-based approach for information
 * exchange and group administration.
 * 
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public abstract class AbstractCommunicationBean extends AbstractAgentBean implements IEffector {
    /**
     * Action name for joining a group. An invocation will associate this agent with a further logical destination.
     */
    public final static String DO_JOIN_GROUP= AbstractCommunicationBean.class.getName() + "#joinGroup";
    
    /**
     * Action name for leaving a group.
     */
    public final static String DO_LEAVE_GROUP= AbstractCommunicationBean.class.getName() + "#leaveGroup";
    
    /**
     * Action name for creating a message box for this agent exclusivly.
     */
    public final static String DO_CREATE_MESSAGE_BOX= AbstractCommunicationBean.class.getName() + "#createMessageBox";
    
    /**
     * Action name for destroying an existing message box.
     */
    public final static String DO_DESTROY_MESSAGE_BOX= AbstractCommunicationBean.class.getName() + "#destroyMessageBox";
    
    /**
     * Action name for sending a message to a closed group of agents.
     */
    public final static String DO_INFORM_CLOSED_GROUP= AbstractCommunicationBean.class.getName() + "#informClosedGroup";
    
    /**
     * Action name for sending a message to an open group of agents and thus to a logical endpoint.
     */
    public final static String DO_INFORM_OPEN_GROUP= AbstractCommunicationBean.class.getName() + "#informOpenGroup";
    
    /**
     * Action name for sending a message directly to a message box.
     */
    public final static String DO_INFORM_ONE= AbstractCommunicationBean.class.getName() + "#informOne";
    
    /**
     * Action name for a remote action invocation.
     */
    public final static String DO_REQUEST_REMOTE_ACTION= AbstractCommunicationBean.class.getName() + "#requestRemoteAction";
    
    protected final Log log;
    
    protected AbstractCommunicationBean() {
        log= LogFactory.getLog(getClass());
    }

    /* (non-Javadoc)
     * @see de.dailab.jiactng.agentcore.environment.IEffector#doAction(de.dailab.jiactng.agentcore.action.DoAction)
     */
    public final void doAction(DoAction doAction) {
        String name= doAction.getAction().getName();
        int hashmark= name.indexOf('#');
        
        if(hashmark < 0 || hashmark == name.length() - 1) {
            // TODO error case
        }
        
        String methodName= name.substring(hashmark + 1);
        for(Method method : getClass().getDeclaredMethods()) {
            if(method.getName().equals(methodName)) {
                try {
                    method.invoke(this, doAction.getParams());
                } catch (Exception e) {
                    // TODO error case
                }
                break;
            }
        }
    }

    /* (non-Javadoc)
     * @see de.dailab.jiactng.agentcore.environment.IEffector#getActions()
     */
    public final ArrayList<? extends Action> getActions() {
        ArrayList<Action> actions= new ArrayList<Action>();
        
        Action doJoinGroupAction= new Action(
            DO_JOIN_GROUP,
            this,
            new Class[]{ILogicalEndPoint.class},
            new Class[0]
        );
        actions.add(doJoinGroupAction);
        
        Action doLeaveGroupAction= new Action(
            DO_LEAVE_GROUP,
            this,
            new Class[]{ILogicalEndPoint.class},
            new Class[0]
        );
        actions.add(doLeaveGroupAction);
        
        Action doCreateMessageBoxAction= new Action(
            DO_CREATE_MESSAGE_BOX,
            this,
            new Class[]{IEndPoint.class},
            new Class[0]
        );
        actions.add(doCreateMessageBoxAction);
        
        Action doDestroyMessageBoxAction= new Action(
            DO_DESTROY_MESSAGE_BOX,
            this,
            new Class[]{IEndPoint.class},
            new Class[0]
        );
        actions.add(doDestroyMessageBoxAction);
        
        // FIXME it will be better to use ILogicalEndPoint here
        Action doInformClosedGroupAction= new Action(
            DO_INFORM_CLOSED_GROUP,
            this,
            new Class[]{IJiacMessage.class, IEndPoint[].class},
            new Class[0]
        );
        actions.add(doInformClosedGroupAction);
        
        Action doInformOpenGroupAction= new Action(
            DO_INFORM_OPEN_GROUP,
            this,
            new Class[]{IJiacMessage.class, ILogicalEndPoint.class},
            new Class[0]
        );
        actions.add(doInformOpenGroupAction);
        
        Action doInformOneAction= new Action(
            DO_INFORM_ONE,
            this,
            new Class[]{IJiacMessage.class, IEndPoint.class},
            new Class[0]
        );
        actions.add(doInformOneAction);
        
        Action doRequestRemoteActionAction= new Action(
            DO_REQUEST_REMOTE_ACTION,
            this,
            new Class[]{DoAction.class, AgentDescription.class},
            new Class[0]
        );
        actions.add(doRequestRemoteActionAction);
        return actions;
    }
    
    protected abstract void joinGroup(ILogicalEndPoint group);
    protected abstract void leaveGroup(ILogicalEndPoint group);
    protected abstract void createMessageBox(IEndPoint messageBox);
    protected abstract void destroyMessageBox(IEndPoint messageBox);
    
    /**
     * FIXME: a better way might be to not distinguish groups explicitly
     */
    protected abstract void informClosedGroup(IJiacMessage message, IEndPoint[] endPoints);
    
    /**
     * FIXME: a better way might be to not distinguish groups explicitly
     */
    protected abstract void informOpenGroup(IJiacMessage message, ILogicalEndPoint group);
    
    protected abstract void informOne(IJiacMessage message, IEndPoint messageBox);
    protected abstract void requestRemoteAction(DoAction doAction, AgentDescription agentDescription);
}
