/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.jms.JMSCommunicationSystem.AddressProperty;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
class JMSGroupAddress extends JMSCommunicationAddress implements IGroupAddress {
    public JMSGroupAddress(String address) {
        super(address);
    }

    public final boolean isClosed() {
        return JMSCommunicationSystem.checkAddressProperty(this, AddressProperty.CLOSED);
    }

    @Override
    Destination convertToDestination(Session session) throws JMSException {
        return session.createTopic(getAddress());
    }
}
