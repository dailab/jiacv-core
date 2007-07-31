package de.dailab.jiactng.agentcore.comm.message;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
	private ICommunicationAddress _sender;
    private Map<String, String> _headers;
	
    public JiacMessage(IJiacContent payload) {
        this(payload, null);
    }
    
	public JiacMessage(IJiacContent payload, ICommunicationAddress address) {
		_payload = payload;
		_sender = address;
        _headers= new Hashtable<String, String>();
	}

	public IJiacContent getPayload() {
		return _payload;
	}
    
	public String getHeader(String key) {
        return _headers.get(key);
    }

    public void setHeader(String key, String value) {
        if(value == null) {
            _headers.remove(key);
        } else {
            _headers.put(key, value);
        }
    }
    
    public Set<String> getHeaderKeys() {
        Set<String> result= new HashSet<String>();
        result.addAll(_headers.keySet());
        return result;
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
	
	public ICommunicationAddress getSender(){
		return _sender;
	}
    
    public void setSender(ICommunicationAddress sender) {
        _sender= sender;
    }
}
