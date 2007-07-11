/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.jms.JMSCommunicationSystem.AddressProperty;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
abstract class JMSCommunicationAddress implements ICommunicationAddress {
    private String _address;
    
    public JMSCommunicationAddress(String address) {
        _address= address;
    }
    
    public String getAddress() {
        return _address;
    }

    public final boolean exists() {
        return JMSCommunicationSystem.checkAddressProperty(this, AddressProperty.EXISTENT);
    }
    
    abstract Destination convertToDestination(Session session) throws JMSException;
    
    @Override
    public abstract String toString();
}
