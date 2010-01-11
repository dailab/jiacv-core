package de.dailab.jiactng.agentcore.comm.message;

import java.util.Set;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.management.jmx.JmxDescriptionSupport;

/**
 * This interface defines the message type used for inter-agent-communication.
 * 
 * It provides meta-information about the message content, a payload section
 * and a sender field.
 * 
 * This interface is <strong>not</strong> intended to be subclassed by clients!
 * 
 * @author Janko Dimitroff
 * @author Martin Loeffelholz
 * @author Marcel Patzlaff
 */
public interface IJiacMessage extends IFact, JmxDescriptionSupport {

   public static final String ITEMNAME_HEADERS = "headers";
   public static final String ITEMNAME_PAYLOAD = "payload";


    public interface Header {
        String SENDER= "JiacTNGSenderAddress";
        String PROTOCOL= "JiacTNGProtocolID";
        String REPLY_TO= "JiacTNGReplyToAddress";
        String SEND_TO = "JiacTNGSendToAddress";
    }


	/**
	 * Returns the payload of this message. There are several different payload
     * types available.
	 * 
	 * @return the payload
	 */
	IFact getPayload();

	/**
	 * Who did sent me this Message?
	 * 
	 * @return ICommunicationAddress	the address from where the Message is sent.
	 */
	ICommunicationAddress getSender();
    
    ICommunicationAddress getReplyToAddress();
    
    String getProtocol();
    
    /**
     * Set a header for this message
     * @param key       the key for the header
     * @param value     the value for the header
     */
    void setHeader(String key, String value);
    
    /**
     * Returns the header of this message
     * @param key       the key of the header
     * @return          the value if the header is set or <code>null</code> otherwise
     */
    String getHeader(String key);
    
    /**
     * Returns all header keys for this message.
     */
    Set<String> getHeaderKeys();
}
