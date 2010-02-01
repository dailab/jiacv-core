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

	/** The item name which can be used to get the headers of an JMX-based message description. */
   String ITEMNAME_HEADERS = "headers";

	/** The item name which can be used to get the payload of an JMX-based message description. */
   String ITEMNAME_PAYLOAD = "payload";


   /**
    * This interface defines the keys of the message header fields.
    */
    public interface Header {

    	/** Key of the message header field which defines the sender of the message. */
        String SENDER= "JiacTNGSenderAddress";

        /** Key of the message header field which defines the protocol of the message. */
        String PROTOCOL= "JiacTNGProtocolID";

        /** Key of the message header field which defines the reply address of the message. */
        String REPLY_TO= "JiacTNGReplyToAddress";

        /** Key of the message header field which defines the receiver address of the message. */
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
	 * @return the address from where the Message is sent.
	 */
	ICommunicationAddress getSender();

	/**
	 * Gets the address to which replies should be send.
	 * @return the reply address
	 */
    ICommunicationAddress getReplyToAddress();

    /**
     * Gets the protocol of this message.
     * @return the protocol
     */
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
     * @return the header keys
     */
    Set<String> getHeaderKeys();
}
