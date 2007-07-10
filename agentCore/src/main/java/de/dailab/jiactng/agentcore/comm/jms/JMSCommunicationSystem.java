/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.jms;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
class JMSCommunicationSystem {
    enum AddressProperty {
        CLOSED,
        EXISTENT,
        LOCAL
    }
    
    static boolean checkAddressProperty(JMSCommunicationAddress address, AddressProperty property) {
        return false;
    }
}
