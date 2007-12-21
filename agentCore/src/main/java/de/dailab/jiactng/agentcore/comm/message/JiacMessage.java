package de.dailab.jiactng.agentcore.comm.message;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * The implementation of {@link IJiacMessage}.
 * 
 * @author Marcel Patzlaff
 */
public class JiacMessage implements IJiacMessage {
    private IFact _payload;

    private Map<String, String> _headers;

    private transient boolean _recursionDetected= false;
    
    /**
     * Constructor for a template message with no content.
     */
    public JiacMessage() {
        this(null, null);
    }
    
    public JiacMessage(IFact payload) {
        this(payload, null);
    }

    public JiacMessage(IFact payload, ICommunicationAddress replyToAddress) {
        _payload = payload;
        _headers = new Hashtable<String, String>();
        if(replyToAddress != null) {
            setHeader(Header.REPLY_TO, replyToAddress.toString());
        }
    }

    public IFact getPayload() {
        return _payload;
    }

    public String getHeader(String key) {
        return _headers.get(key);
    }

    public void setHeader(String key, String value) {
        if (value == null) {
            _headers.remove(key);
        } else {
            _headers.put(key, value);
        }
    }

    public Set<String> getHeaderKeys() {
        Set<String> result = new HashSet<String>();
        result.addAll(_headers.keySet());
        return result;
    }

    @Override
    public synchronized final boolean equals(Object obj) {
        if(_recursionDetected) {
            return true;
        }
        
        try {
            _recursionDetected= true;
            if (obj == this) {
                return true;
            }
        
            if (obj == null || !(obj instanceof JiacMessage)) {
                return false;
            }
        
            JiacMessage other = (JiacMessage) obj;
            Set<String> otherKeys = other.getHeaderKeys();
        
            if (otherKeys.size() != _headers.size()) {
                return false;
            }
        
            for (String key : otherKeys) {
                String myValue= _headers.get(key);
                if (myValue == null || !other.getHeader(key).equals(myValue)) {
                    return false;
                }
            }
        
            IFact otherPayload = other.getPayload();
            return otherPayload != null && _payload != null ? otherPayload.equals(_payload) : otherPayload == _payload;
        } finally {
            _recursionDetected= false;
        }
    }

    @Override
    public synchronized final int hashCode() {
        if(_recursionDetected) {
            return 0;
        }
        
        try {
            _recursionDetected= true;
            int hashCode = JiacMessage.class.hashCode();
        
            for (String key : getHeaderKeys()) {
                hashCode ^= key.hashCode() << 7;
                hashCode ^= getHeader(key).hashCode();
            }
        
            if (_payload != null) {
                hashCode ^= _payload.hashCode();
            }
        
            return hashCode;
        } finally {
            _recursionDetected= false;
        }
    }

    public synchronized String toString() {
        if(_recursionDetected) {
            return "<recursion>";
        }
        
        try {
            _recursionDetected= true;
            StringBuilder builder = new StringBuilder();
            builder.append("[Headers: {");
            int counter = _headers.size() - 1;
    
            for (Iterator<String> keys = _headers.keySet().iterator(); keys.hasNext(); --counter) {
                String key = keys.next();
                builder.append(key).append("=>").append(_headers.get(key));
    
                if (counter > 0) {
                    builder.append(";");
                }
            }
    
            builder.append("}");
            builder.append(", Payload: ").append(getPayload()).append(", Sender: ").append(getSender());
            builder.append("]");
            return builder.toString();
        } finally {
            _recursionDetected= false;
        }
    }

    public ICommunicationAddress getSender() {
        String uri = getHeader(Header.SENDER);

        if (uri != null) {
            return CommunicationAddressFactory.createFromURI(uri);
        }

        return null;
    }
    
    public ICommunicationAddress getReplyToAddress() {
        String uri = getHeader(Header.REPLY_TO);

        if (uri != null) {
            return CommunicationAddressFactory.createFromURI(uri);
        }

        return null;
    }

    public void setProtocol(String protocol) {
        setHeader(Header.PROTOCOL, protocol);
    }

    public String getProtocol() {
        return getHeader(Header.PROTOCOL);
    }

    public void setSender(ICommunicationAddress sender) {
        setHeader(Header.SENDER, sender != null ? sender.toString() : null);
    }
}
