package de.dailab.jiactng.agentcore.comm.message;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;


/**
 * Ein Objekt, dass Nachrichten, die innerhalb Jiacs verschickt werden, kapselt. Der Recipient bestimmt, wer diese
 * Message bekommen soll. EIne Message ist typisiert, durch das operation-attribut
 * 
 * @author janko, loeffelholz
 */
public class JiacMessage implements IJiacMessage {
	public static final String PLATFORM_ENDPOINT_EXTENSION = "TNG";

	private IJiacContent _payload;
    private Map<String, String> _headers;
    
    /**
     * Field to cache the URI conversion
     */
    private transient ICommunicationAddress _sender;
    
    /**
     * Constructor for a template message with no content.
     */
    public JiacMessage() {
        this(null);
    }

    public JiacMessage(IJiacContent payload) {
        _payload= payload;
        _headers= new Hashtable<String, String>();
    }

	public IJiacContent getPayload() {
		return _payload;
	}
    
	public String getHeader(Object key) {
        return _headers.get(key.toString());
    }

    public void setHeader(Object key, String value) {
        String keyStr= key.toString();
        
        if(value == null) {
            _headers.remove(keyStr);
        } else {
            _headers.put(keyStr, value);
        }
    }
    
    public Set<String> getHeaderKeys() {
        Set<String> result= new HashSet<String>();
        result.addAll(_headers.keySet());
        return result;
    }
    
    @Override
    public final boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        
        if(obj == null || !(obj instanceof JiacMessage)) {
            return false;
        }
        
        JiacMessage other= (JiacMessage) obj;
        Set<String> otherKeys= other.getHeaderKeys();
        
        if(otherKeys.size() != _headers.size()) {
            return false;
        }
        
        for(String key : otherKeys) {
            if(other.getHeader(key).equals(_headers.get(key))) {
                return false;
            }
        }
        
        IJiacContent otherPayload= other.getPayload();
        return otherPayload != null ? otherPayload.equals(_payload) : _payload == null;
    }

    @Override
    public final int hashCode() {
        int hashCode= 0;
        
        for(String key : getHeaderKeys()) {
            hashCode ^= key.hashCode() << 7;
            hashCode ^= getHeader(key).hashCode();
        }
        
        if(_payload != null) {
            hashCode ^= _payload.hashCode();
        }
        
        return hashCode;
    }

    public String toString() {
        StringBuilder builder= new StringBuilder();
        builder.append("[Headers: {");
        int counter= _headers.size() - 1;
        
        for(Iterator<String> keys= _headers.keySet().iterator(); keys.hasNext(); --counter) {
            String key= keys.next();
            builder.append(key).append("=>").append(_headers.get(key));
            
            if(counter > 0) {
                builder.append(";");
            }
        }
      
        builder.append("}");
        builder.append(", Payload: ").append(getPayload()).append(", Sender: ").append(getSender());
        builder.append("]");
		return builder.toString();
	}
	
	public ICommunicationAddress getSender() {
        if(_sender == null) {
            String uri= getHeader(Header.SENDER);
            
            if(uri != null) {
                _sender= CommunicationAddressFactory.createFromURI(uri);
            }
        }
        
		return _sender;
	}
    
    public String getProtocol() {
        return getHeader(Header.PROTOCOL);
    }

    public void setSender(ICommunicationAddress sender) {
        _sender= sender;
        setHeader(Header.SENDER, sender.toString());
    }
}
