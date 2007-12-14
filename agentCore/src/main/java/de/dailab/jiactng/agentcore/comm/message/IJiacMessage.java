package de.dailab.jiactng.agentcore.comm.message;

import java.util.Set;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.knowledge.IFact;

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
public interface IJiacMessage extends IFact {
    enum Header {
        SENDER("JiacTNGSenderAddress"),
        PROTOCOL("JiacTNGProtocolID");
        
        private final String _value;
        
        private Header(String value) {
            _value= value;
        }
        
        public String toString() {
            return _value;
        }
    }
    
	/**
	 * Returns the payload of this message. There are several different payload
     * types available.
	 * 
	 * @return
	 */
	IJiacContent getPayload();

	/**
	 * Who did sent me this Message?
	 * 
	 * @return ICommunicationAddress	the address from where the Message is sent.
	 */
	ICommunicationAddress getSender();
    
    String getProtocol();
    
    /**
     * Set a header for this message
     * @param key       the key for the header
     * @param value     the value for the header
     */
    void setHeader(Object key, String value);
    
    /**
     * Returns the header of this message
     * @param key       the key of the header
     * @return          the value if the header is set or <code>null</code> otherwise
     */
    String getHeader(Object key);
    
    /**
     * Returns all header keys for this message.
     */
    Set<String> getHeaderKeys();
}
