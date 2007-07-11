/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.jms;

import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class JMSCommunicationAddressFactory extends CommunicationAddressFactory {
    @Override
    public IGroupAddress createGroupAddress(String groupName) {
        return new JMSGroupAddress(groupName);
    }

    @Override
    public IMessageBoxAddress createMessageBoxAddress(String boxName) {
        return new JMSMessageBoxAddress(boxName);
    }

}
