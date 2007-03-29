package de.dailab.jiactng.agentcore.communication;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Joachim Fuchs
 */
public class TNGMessage implements IFact {
    
    protected Map<String, String> properties = new HashMap<String, String>();
    
    protected Serializable content = null;
        
    public void setDestination(String destination) {
     
        properties.put("tng.destination", destination);
        
    }
    
    public String getDestination() {
        
        return properties.get("tng.destination");
        
    }
    
    public Map<String, String> getMessageProperties() {
        
        return Collections.unmodifiableMap(this.properties);
        
    }
    
    public void setMessageProperty(String name, String value) {
        
        this.properties.put(name, value);
        
    }
    
    public Object getMessageProperty(String name) {
        
        return this.properties.get(name);
        
    }
    
    public Object getContent() {
        
        return (Object)this.content;
        
    }
    
    public void setContent(Object content) {
        
        if (content instanceof Serializable) {
            
            this.content = (Serializable)content;
            
        } else {
         
            throw new IllegalArgumentException("message content must be serializable");
            
        }
        
    }
    
    /**
     *
     */
    public String toString() {
        
        StringBuffer buf = new StringBuffer("{ TNGMessage[hash=" + hashCode() + "] ");
        
        buf.append("Properties=(" + properties + ") ");        
        buf.append("Content=( " + content + ") }");
        
        return buf.toString();
        
    }
    
}
