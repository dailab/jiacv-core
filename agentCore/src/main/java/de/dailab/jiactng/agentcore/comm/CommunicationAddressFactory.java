/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;


/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public final class CommunicationAddressFactory {
    public static IGroupAddress createGroupAddress(String groupName) {
        // TODO name check
        return new GroupAddress(groupName);
    }
    
    public static IMessageBoxAddress createMessageBoxAddress(String boxName) {
        // TODO name check
        return new MessageBoxAddress(boxName);
    }
    
    public static ICommunicationAddress createFromScheme(String scheme) {
        String prefix= scheme.substring(0, scheme.indexOf('/'));
        String address= scheme.substring(scheme.indexOf('/') + 1);
        
        if(prefix.equalsIgnoreCase(GroupAddress.PREFIX)) {
            return createGroupAddress(address);
        } else if (prefix.equalsIgnoreCase(MessageBoxAddress.PREFIX)) {
            return createMessageBoxAddress(address);
        } else {
            throw new IllegalArgumentException("'" + scheme + "' is not a valid communication address");
        }
    }
}
