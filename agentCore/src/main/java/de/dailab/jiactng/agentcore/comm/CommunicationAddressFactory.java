/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

import java.net.URI;
import java.net.URISyntaxException;


/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public final class CommunicationAddressFactory {
    static final String SCHEME= "jiactransport";
    public static IGroupAddress createGroupAddress(String groupName) {
        try {
            return new GroupAddress(groupName);
        } catch (URISyntaxException use) {
            throw new IllegalArgumentException("groupName is not valid");
        }
    }
    
    public static IMessageBoxAddress createMessageBoxAddress(String boxName) {
        try {
            return new MessageBoxAddress(boxName);
        } catch (URISyntaxException use) {
            throw new IllegalArgumentException("boxName is not valid");
        }
    }
    
    public static ICommunicationAddress createFromURI(String uri) {
        int colon= uri.indexOf(':');
        
        if(colon <= 0) {
            throw new IllegalArgumentException("'" + uri + "' is not a valid communication address");
        }
        
        String prefix= uri.substring(0, colon);
        String name= uri.substring(colon + 1);
        
        if(prefix.equalsIgnoreCase(GroupAddress.PREFIX)) {
            return createGroupAddress(name);
        } else if(prefix.equalsIgnoreCase(MessageBoxAddress.PREFIX)) {
            return createMessageBoxAddress(name);
        } else {
            // maybe we have a communication address that is bound to a transport
            return createFromURI(name);
        }
    }
    
    public static ICommunicationAddress createFromURI(URI uri) {
        return createFromURI(uri.toString());
    }
}
