package de.dailab.jiactng.agentcore.comm.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.management.jmx.JmxDescriptionSupport;

/**
 * The implementation of {@link IJiacMessage}.
 * 
 * @author Marcel Patzlaff
 */
@SuppressWarnings("serial")
public class JiacMessage implements IJiacMessage, JmxDescriptionSupport {

	public static final String ITEMNAME_HEADERS = "headers";
	public static final String ITEMNAME_PAYLOAD = "payload";

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

    /**
     * Gets the type of JIAC message descriptions based on JMX open types.
     * @return A composite type containing headers and payload.
     * @throws OpenDataException if an error occurs during the creation of the type.
     * @see javax.management.openmbean.CompositeType
     */
    public OpenType<?> getDescriptionType() throws OpenDataException {
    	ArrayList<String> itemNames = new ArrayList<String>();
    	ArrayList<OpenType<?>> itemTypes = new ArrayList<OpenType<?>>();
    	
    	// create open type of message payload
    	if (_payload != null) {
    		itemNames.add(ITEMNAME_PAYLOAD);
    		if (_payload instanceof JmxDescriptionSupport) {
    			itemTypes.add(((JmxDescriptionSupport)_payload).getDescriptionType());
    		} else {
    			itemTypes.add(SimpleType.STRING);
    		}
    	}
    	
    	// create open type of message headers
    	if (_headers != null) {
    		itemNames.add(ITEMNAME_HEADERS);
    		String[] headerNames = getHeaderKeys().toArray(new String[0]);
    		int headerSize = headerNames.length;
    		OpenType<?>[] headerTypes = new OpenType<?>[headerSize];
    		for (int i=0; i<headerSize; i++) {
    			// each header value is of type String
    			headerTypes[i] = SimpleType.STRING;
    		}
    		itemTypes.add(new CompositeType(
    				_headers.getClass().getName(), 
    				"headers of a JIAC-TNG message",
    				headerNames,
    				headerNames,
    				headerTypes));
    	}
    	
    	// use names of message items as their description
    	ArrayList<String> itemDescriptions = itemNames;
    	
    	// transform list to array
    	int compositeSize = itemTypes.size();
		OpenType<?>[] itemTypesArray = new OpenType<?>[compositeSize];
    	for (int i=0; i<compositeSize; i++) {
    		itemTypesArray[i] = itemTypes.get(i);
    	}
    	
    	// create and return open type of a JIAC message
    	return new CompositeType(
    			this.getClass().getName(), 
    			"standard JIAC-TNG message", 
    			itemNames.toArray(new String[0]),
    			itemDescriptions.toArray(new String[0]),
    			itemTypesArray);
    }

    /**
     * Gets the description of this JIAC message based on JMX open types.
     * @return Composite data containing headers and payload.
     * @throws OpenDataException if an error occurs during the creation of the data.
     * @see javax.management.openmbean.CompositeData
     */
	public Object getDescription() throws OpenDataException {
		Map<String,Object> items = new HashMap<String,Object>();
		CompositeType type = (CompositeType) getDescriptionType();

		// create open data of message payload
		if (_payload != null) {
    		if (_payload instanceof JmxDescriptionSupport) {
    			items.put(ITEMNAME_PAYLOAD, ((JmxDescriptionSupport)_payload).getDescription());
    		} else {
    			items.put(ITEMNAME_PAYLOAD, _payload.toString());
    		}
		}

		// create open data of message headers
    	if (_headers != null) {
    		String[] headerNames = getHeaderKeys().toArray(new String[0]);
    		int headerSize = headerNames.length;
    		Object[] headerValues = new Object[headerSize];
    		for (int i=0; i<headerSize; i++) {
    			headerValues[i] = getHeader(headerNames[i]);
    		}
    		items.put(ITEMNAME_HEADERS, new CompositeDataSupport(
    				(CompositeType) type.getType(ITEMNAME_HEADERS),
    				headerNames, headerValues));
    	}
    	
		return new CompositeDataSupport(type, items);
	}
}
