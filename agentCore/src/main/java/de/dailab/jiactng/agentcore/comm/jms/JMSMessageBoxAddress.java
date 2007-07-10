/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;
import de.dailab.jiactng.agentcore.comm.jms.JMSCommunicationSystem.AddressProperty;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
class JMSMessageBoxAddress extends JMSCommunicationAddress implements IMessageBoxAddress {
    public JMSMessageBoxAddress(String address) {
        super(address);
    }

    public final boolean isLocal() {
        return JMSCommunicationSystem.checkAddressProperty(this, AddressProperty.LOCAL);
    }

    @Override
    Destination convertToDestination(Session session) throws JMSException {
        return session.createQueue(getAddress());
    }
}
