/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;


/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class CommunicationAddressFactory {
    private static CommunicationAddressFactory INSTANCE= null;
    
    public static CommunicationAddressFactory getDefault() {
        if(INSTANCE == null) {
            synchronized(CommunicationAddressFactory.class) {
                if(INSTANCE == null) {
                    /*
                     * TODO remove this stuff and introduce a static class which represents the agentNode
                     *      This class might then be used to acquire information about the nodes configuration.
                     */
//                    INSTANCE= new JMSCommunicationAddressFactory();
                }
            }
        }
        
        return INSTANCE;
    }
    
    public abstract IGroupAddress createGroupAddress(String groupName);
    public abstract IMessageBoxAddress createMessageBoxAddress(String boxName);
}
