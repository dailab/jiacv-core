/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.jms;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.commons.logging.Log;

import de.dailab.jiactng.agentcore.comm.AbstractCommunicationBean;
import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;
import de.dailab.jiactng.agentcore.comm.message.EndPointFactory;
import de.dailab.jiactng.agentcore.comm.message.IEndPoint;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class JMSCommunicationBean extends AbstractCommunicationBean {
    private class DefaultMessageDelegate implements IJMSMessageDelegate {
        public void onError(JMSCommunicationAddress from, String selector, Exception exception) {
            _log.warn("the message from " + from + " selected with '" + selector + "' could not be parsed", exception);
        }

        public void onMessage(IJiacMessage message, JMSCommunicationAddress from, String selector) {
            memory.write(message);
        }

        public void onException(JMSException exception) {
            _log.warn("the JMS message system threw an exception", exception);
        }
    }
    
    private static <T,P extends T> void assertValidArgument(String message, T argument, Class<P> expected) {
        if(!expected.isInstance(argument)) {
            throw new IllegalArgumentException(message);
        }
    }
    
    private Log _log;
    private ConnectionFactory _connectionFactory;
    private String _agentNodeName;
    private IEndPoint _address;
    
    private JMSSender _sender;
    private JMSReceiver _receiver;
    
    public JMSCommunicationBean() {
        
    }
    
    // setter methods for configuration
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        _connectionFactory= connectionFactory;
    }
    
    public ConnectionFactory getConnectionFactory() {
        return _connectionFactory;
    }
    
    public void setAgentNodeName(String agentNodeName) {
        _agentNodeName= agentNodeName;
    }
    
    public String getAgentNodeName() {
        return _agentNodeName;
    }

    public IEndPoint getAddress() {
        return _address;
    }
    
    @Override
    public void doInit() throws Exception {
        super.doInit();
        
        if(getConnectionFactory() == null) {
            throw new IllegalStateException("no connection factory set");
        }
        
        if(getAgentNodeName() == null) {
            throw new IllegalStateException("no agentNode name set");
        }
        
        _log= thisAgent.getLog(this);
        IJMSMessageDelegate delegate= new DefaultMessageDelegate();
        
        _address= EndPointFactory.createEndPoint(getAgentNodeName());
        _sender= new JMSSender(getConnectionFactory(), delegate, _log);
        _receiver= new JMSReceiver(getConnectionFactory(), delegate, _log);
    }
    
    @Override
    public void doCleanup() throws Exception {
        if(_sender != null) {
            _sender.doCleanup();
            _sender= null;
        }
        
        if(_receiver != null) {
            _receiver.doCleanup();
            _receiver= null;
        }
        
        super.doCleanup();
    }

    @Override
    public void createMessageBox(IMessageBoxAddress messageBox) throws CommunicationException {
        assertValidArgument("messageBox is not valid", messageBox, JMSMessageBoxAddress.class);
        try {
            _receiver.receive((JMSCommunicationAddress) messageBox, null);
        } catch (JMSException je) {
            throw new CommunicationException("could not create message box '" + messageBox + "'", je);
        }
    }

    @Override
    public void destroyMessageBox(IMessageBoxAddress messageBox) throws CommunicationException {
        assertValidArgument("messageBox is not valid", messageBox, JMSMessageBoxAddress.class);
        try {
            _receiver.stopReceive((JMSCommunicationAddress) messageBox, null);
        } catch (JMSException je) {
            throw new CommunicationException("error while discarding message box '" + messageBox + "'", je);
        }
    }

    @Override
    public void joinGroup(IGroupAddress group) throws CommunicationException {
        assertValidArgument("group is not valid", group, JMSGroupAddress.class);
        try {
            _receiver.receive((JMSCommunicationAddress) group, null);
        } catch (JMSException je) {
            throw new CommunicationException("could not join group '" + group + "'", je);
        }
    }

    @Override
    public void leaveGroup(IGroupAddress group) throws CommunicationException {
        assertValidArgument("group is not valid", group, JMSGroupAddress.class);
        try {
            _receiver.stopReceive((JMSCommunicationAddress) group, null);
        } catch (JMSException je) {
            throw new CommunicationException("error while leaving group '" + group + "'", je);
        }
    }

    @Override
    public void send(IJiacMessage message, ICommunicationAddress address) throws CommunicationException {
        assertValidArgument("address is not valid", address, JMSCommunicationAddress.class);
        try {
            _sender.send(message, (JMSCommunicationAddress) address);
        } catch (JMSException je) {
            throw new CommunicationException("could not send message", je);
        }
    }

    @Override
    public void sendInSession(IJiacMessage message, IEndPoint endpoint, String sessionId) throws CommunicationException {
        // TODO Auto-generated method stub
    }
}
