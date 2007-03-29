package de.dailab.jiactng.agentcore.communication;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * DUMMY!!!!
 *
 * @author Joachim Fuchs
 */
public class PingObject implements IFact {
    
    public String string;
    public boolean receivedFromSpace = false;
    public boolean receivedFromNetwork = false;
    
    /**
     * Creates a new instance of PingObject
     *
     */
    public PingObject(String string) {
        
        this.string = string;
        
    }
    
    public PingObject(String string, boolean received) {
        
        this.string = string;
        this.receivedFromSpace = received;
        
    }
    
    public void setReceivedFromSpace() {
     
        receivedFromSpace = true;
        
    }
    
    public void setReceivedFromNetwork() {
        
        receivedFromNetwork = true;
        
    }
    
    public String toString() {
        
        return string + " " + receivedFromSpace + " " + receivedFromNetwork;
        
    }
    
}
