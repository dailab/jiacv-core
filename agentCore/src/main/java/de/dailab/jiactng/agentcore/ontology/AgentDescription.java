package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;
import de.dailab.jiactng.agentcore.util.EqualityChecker;

/**
 * Klasse zum Beschreiben eines Agenten. Sie enthuelt also META-Infos ueber den Agenten.
 * Es ist nicht der Agent selbst.
 * @author janko
 * @author axle
 */
public class AgentDescription implements IAgentDescription {
    private static final long serialVersionUID = -5364612545330522105L;

    /** Agent IDentifier.*/
	private String aid;
	
	/** Agent name. */
	private String name;
	
	/** Agent's state. */
	private transient String state;
	
	/** Communication Identifier. */
	private IMessageBoxAddress messageBoxAddress;
	
	/** UUID of the AgentNode that holds this agent */
	private String agentNodeUUID;
	
//	public AgentDescription(String aid, String name, String state) {
//        this(aid, name, state, name != null ? CommunicationAddressFactory.createMessageBoxAddress(aid) : null);
//	}
    
	public AgentDescription() {
	    this(null, null, null, null, null);
	}
	
    public AgentDescription(String aid, String name, String state, IMessageBoxAddress messageBoxAddress, String agentNodeUUID) {
        this.aid=aid;
        this.name=name;
        this.state=state;
        this.messageBoxAddress= messageBoxAddress;
        this.agentNodeUUID = agentNodeUUID;
    }
    
    

	/**
	 * @return the aid
	 */
	public String getAid() {
		return aid;
	}

	/**
	 * @param aid the aid to set
	 */
	public void setAid(String aid) {
		this.aid = aid;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}
	
	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}
    
	/**
	 * @return the endpoint
     * 
	 */
	public IMessageBoxAddress getMessageBoxAddress() {
		return messageBoxAddress;
	}

	/**
	 * @param messageBoxAddress the endpoint to set
	 */
	public void setMessageBoxAddress(IMessageBoxAddress messageBoxAddress) {
		this.messageBoxAddress = messageBoxAddress;
	}
    
    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        
        if(obj == null || !(obj instanceof AgentDescription)) {
            return false;
        }
        
        AgentDescription other= (AgentDescription) obj;
        return EqualityChecker.equals(aid, other.aid);
    }
    
    @Override
    public int hashCode() {
        return AgentDescription.class.hashCode() ^ (aid != null ? aid.hashCode() : 0);
    }

    @Override
    public String toString() {
        StringBuilder builder= new StringBuilder();

        // name
        builder.append("Agent:\n name=");
        if (name != null) {
        	builder.append("'").append(name).append("'");
        } else {
        	builder.append("null");
        }

        // aid
        builder.append("\n aid=");
        if (aid != null) {
        	builder.append("'").append(aid).append("'");
        } else {
        	builder.append("null");
        }

        // state
        builder.append("\n state=");
        if (state != null) {
        	builder.append("'").append(state).append("'");
        } else {
        	builder.append("null");
        }

        // endpoint
        if (messageBoxAddress != null) {
        	// local
        	builder.append("\n messageBoxAddress=").append(messageBoxAddress.toString());
        } else {
        	builder.append("\n messageBoxAddress=null");
        }

        builder.append('\n');
        return builder.toString();
    }

    /**
     * Returns the UUID of the AgentNode that is holding this agent
     * @return
     */
	public String getAgentNodeUUID() {
		return agentNodeUUID;
	}
}
