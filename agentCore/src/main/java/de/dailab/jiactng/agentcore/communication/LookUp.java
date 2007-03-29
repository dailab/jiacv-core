package de.dailab.jiactng.agentcore.communication;

import de.dailab.jiactng.agentcore.communication.jms.*;
import java.io.Serializable;

/**
 * Represents a lookup message that is used for looking up services.
 *
 * @author Joachim Fuchs
 */
public class LookUp implements Serializable {
    
    private String destination = null;

    private long id = 0;
    
    private long startTime = 0;
    
    private String serviceName = null;
    
    private String methodName  = null;
    
    private Class [] inputTypes = new Class [0];
    
    private Class outputType = null;

    public void setId(long id) {
        
        this.id = id;
        
    }
    
    public long getId() {
        
        return id;
        
    }
    
    public long getStartTime() {
        
        return startTime;
        
    }
        
    public void setServiceName(String name) {
        
        this.serviceName = name;
        
    }
    
    public String getServiceName() {
        
        return this.serviceName;
        
    }
    
    public void setMethodName(String name) {
        
        this.methodName = name;
        
    }
    
    public String getMethodName() {
        
        return this.methodName;
    
    }
    
    public LookUp(long id) {
        
        this.id = id;
        this.startTime = System.currentTimeMillis();
        
    }
    
    public void setInputTypes(Class [] types) {
        
        this.inputTypes = types;
        
    }
    
    public Class [] getInputTypes() {
        
        return this.inputTypes;
        
    }
    
    public void setOutputType(Class type) {
        
        this.outputType = type;
        
    }
    
    public Class getOutputType() {
        
        return this.outputType;
        
    }
    
    public void setDestination(String destination) {
        
        this.destination = destination;
        
    }
    
    public String getDestination() {
        
        return destination;
        
    }
    
}
