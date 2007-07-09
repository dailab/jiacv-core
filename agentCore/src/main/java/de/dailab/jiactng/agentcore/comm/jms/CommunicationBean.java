/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.comm.AbstractCommunicationBean;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class CommunicationBean extends AbstractCommunicationBean {
    private final Log _log;
    
    public CommunicationBean() {
        _log= LogFactory.getLog(getClass());
    }
    
    @Override
    protected Log getLog() {
        return _log;
    }

    /* (non-Javadoc)
     * @see de.dailab.jiactng.agentcore.comm.AbstractCommunicationBean#createMessageBox(de.dailab.jiactng.agentcore.comm.IMessageBoxAddress)
     */
    @Override
    public void createMessageBox(IMessageBoxAddress messageBox) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see de.dailab.jiactng.agentcore.comm.AbstractCommunicationBean#destroyMessageBox(de.dailab.jiactng.agentcore.comm.IMessageBoxAddress)
     */
    @Override
    public void destroyMessageBox(IMessageBoxAddress messageBox) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see de.dailab.jiactng.agentcore.comm.AbstractCommunicationBean#informGroup(de.dailab.jiactng.agentcore.comm.message.IJiacMessage, de.dailab.jiactng.agentcore.comm.IGroupAddress)
     */
    @Override
    public void informGroup(IJiacMessage message, IGroupAddress group) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see de.dailab.jiactng.agentcore.comm.AbstractCommunicationBean#informOne(de.dailab.jiactng.agentcore.comm.message.IJiacMessage, de.dailab.jiactng.agentcore.comm.IMessageBoxAddress)
     */
    @Override
    public void informOne(IJiacMessage message, IMessageBoxAddress messageBox) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see de.dailab.jiactng.agentcore.comm.AbstractCommunicationBean#joinGroup(de.dailab.jiactng.agentcore.comm.IGroupAddress)
     */
    @Override
    public void joinGroup(IGroupAddress group) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see de.dailab.jiactng.agentcore.comm.AbstractCommunicationBean#leaveGroup(de.dailab.jiactng.agentcore.comm.IGroupAddress)
     */
    @Override
    public void leaveGroup(IGroupAddress group) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see de.dailab.jiactng.agentcore.comm.AbstractCommunicationBean#requestRemoteAction(de.dailab.jiactng.agentcore.action.DoAction, de.dailab.jiactng.agentcore.ontology.AgentDescription)
     */
    @Override
    public void requestRemoteAction(DoAction doAction, AgentDescription agentDescription) {
        // TODO Auto-generated method stub

    }

}
