/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * The communication address factory is the only way to create
 * the two different types of communication addresses. This ensures
 * that the specific implementation of these addresses remains exchangeable.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public final class CommunicationAddressFactory {
    /**
     * Creates a {@link IGroupAddress} with the specified group name.
     * 
     * @param groupName     the name of the group
     * @return              the address representation of the group
     * @throws IllegalArgumentException     if the provided <code>groupName</code>
     *                                      cannot be used in a {@link URI}
     *                                      
     * @see URI
     */
    public static IGroupAddress createGroupAddress(String groupName) {
        try {
            return new GroupAddress(groupName);
        } catch (URISyntaxException use) {
            throw new IllegalArgumentException("groupName is not valid", use);
        }
    }
    
    /**
     * Creates a {@link IMessageBoxAddress} with specified message box name.
     * 
     * @param boxName       the name of the message box
     * @return              the address representation of the message box
     * @throws IllegalArgumentException     if the provided <code>boxName</code>
     *                                      cannot be used in a {@link URI}
     * 
     * @see URI
     */
    public static IMessageBoxAddress createMessageBoxAddress(String boxName) {
        try {
            return new MessageBoxAddress(boxName);
        } catch (URISyntaxException use) {
            throw new IllegalArgumentException("boxName is not valid", use);
        }
    }
    
    /**
     * Parses the specific uri and tries to create a group or message
     * box address from it.
     * 
     * <p>
     * Bondage to message transports is stripped in the current implementation of
     * this method!
     * </p>
     * 
     * @param uri       the uri to parse and convert
     * @return          the address the uri describes
     * @throws IllegalArgumentException     if the provided <code>uri</code> is not
     *                                      a valid communication address.
     */
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
    
    /**
     * This method is the shortcut for {@link #createFromURI(String) createFromURI(uri.toString())}.
     * 
     * @see #createFromURI(String)
     */
    public static ICommunicationAddress createFromURI(URI uri) {
        return createFromURI(uri.toString());
    }
}
