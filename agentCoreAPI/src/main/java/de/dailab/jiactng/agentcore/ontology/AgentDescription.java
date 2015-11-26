package de.dailab.jiactng.agentcore.ontology;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;
import de.dailab.jiactng.agentcore.util.EqualityChecker;

/**
 * This ontology is used to describe agents. Thus, it contains META information about agents
 * and is not the agent itself.
 * @author janko
 * @author axle
 */
public class AgentDescription implements IAgentDescription {
    private static final long serialVersionUID = -5364612545330522105L;

    /** Agent IDentifier.*/
	private String aid;
	
	/** Agent name. */
	private String name;
	
	/** Agent owner. */
	private String owner;
	
	/** Agent's state. */
	private String state;
	
	/** Communication Identifier. */
	private IMessageBoxAddress messageBoxAddress;
	
	/** UUID of the AgentNode that holds this agent */
	private String agentNodeUUID;
	
	/** Flag, if it is set <code>true</code>, it is a mobile agent */
	private Boolean isMobile = Boolean.FALSE;
	
//	public AgentDescription(String aid, String name, String state) {
//        this(aid, name, state, name != null ? CommunicationAddressFactory.createMessageBoxAddress(aid) : null);
//	}

	/**
	 * Creates an empty agent description.
	 */
	public AgentDescription() {
	    this(null, null, null, null, null, null, null);
	}

	/**
	 * Creates a description of a stationary agent.
	 * @param aid the unique identifier of the agent
	 * @param name the name of the agent
	 * @param owner the owner of the agent
	 * @param state the life-cycle state of the agent
	 * @param messageBoxAddress the communication address of the agent
	 * @param agentNodeUUID the unique identifier of the agents node
	 */
    public AgentDescription(String aid, String name, String owner, String state, IMessageBoxAddress messageBoxAddress, String agentNodeUUID) {
//        this.aid=aid;
//        this.name=name;
//        this.state=state;
//        this.messageBoxAddress= messageBoxAddress;
//        this.agentNodeUUID = agentNodeUUID;
    	this( aid,  name,  owner, state,  messageBoxAddress,  agentNodeUUID, Boolean.FALSE);
    }

    /**
     * Creates an agent description with mobility flag.
	 * @param aid the unique identifier of the agent
	 * @param name the name of the agent
	 * @param owner the owner of the agent
	 * @param state the life-cycle state of the agent
	 * @param messageBoxAddress the communication address of the agent
	 * @param agentNodeUUID the unique identifier of the agents node
     * @param mobile the mobility of the agent
     */
    public AgentDescription(String aid, String name, String owner, String state, IMessageBoxAddress messageBoxAddress, String agentNodeUUID, Boolean mobile) {
        this.aid=aid;
        this.name=name;
        this.owner=owner;
        this.state=state;
        this.messageBoxAddress= messageBoxAddress;
        this.agentNodeUUID = agentNodeUUID;
        this.isMobile = mobile;
    }

    /**
     * Creates an agent description from JMX composite data.
     * @param descr the agent description based on JMX open types.
     */
    public AgentDescription(CompositeData descr) {
		aid = (String) descr.get(IAgentDescription.ITEMNAME_ID);
		name = (String) descr.get(IAgentDescription.ITEMNAME_NAME);
		owner = (String) descr.get(IAgentDescription.ITEMNAME_OWNER);
		agentNodeUUID = (String) descr.get(IAgentDescription.ITEMNAME_NODE);
		state = (String) descr.get(IAgentDescription.ITEMNAME_STATE);
		isMobile = (Boolean) descr.get(IAgentDescription.ITEMNAME_MOBILE);
		String address = (String) descr.get(IAgentDescription.ITEMNAME_MESSAGEBOX);
		if (address != null) {
			messageBoxAddress = CommunicationAddressFactory.createMessageBoxAddress(address);
		}
    }

	/**
	 * Get the unique identifier of the agent.
	 * @return the unique agent identifier
	 */
	public String getAid() {
		return aid;
	}

	/**
	 * Set the unique identifier of the agent.
	 * @param newAid the unique agent identifier to set
	 */
	public void setAid(String newAid) {
		aid = newAid;
	}

	/**
	 * Get the name of the agent.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the agent.
	 * @param newName the name to set
	 */
	public void setName(String newName) {
		name = newName;
	}

	/**
	 * Get the owner of the agent.
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * Set the owner of the agent.
	 * @param newOwner the owner to set
	 */
	public void setOwner(String newOwner) {
		owner = newOwner;
	}

	/**
	 * Get the life-cycle state of the agent.
	 * @return the life-cycle state
	 * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates
	 */
	public String getState() {
		return state;
	}
	
	/**
	 * Set the life-cycle state of the agent
	 * @param newState the life-cycle state to set
	 * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates
	 */
	public void setState(String newState) {
		state = newState;
	}
    
	/**
	 * Get the communication address of the agent.
	 * @return the communication end-point
	 */
	public IMessageBoxAddress getMessageBoxAddress() {
		return messageBoxAddress;
	}

	/**
	 * Set the communication address of the agent.
	 * @param newMessageBoxAddress the communication end-point to set
	 */
	public void setMessageBoxAddress(IMessageBoxAddress newMessageBoxAddress) {
		messageBoxAddress = newMessageBoxAddress;
	}

	/**
	 * Checks the equality of two agent descriptions. The descriptions are equal
	 * if their unique identifiers are equal.
	 * @param obj the other agent description
	 * @return the result of the equality check
	 * @see EqualityChecker#equalsOrNull(Object, Object)
	 */
    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        
        if (! (obj instanceof AgentDescription)) {
            return false;
        }
        
        final AgentDescription other= (AgentDescription) obj;
        return EqualityChecker.equals(aid, other.getAid()) && 
        		EqualityChecker.equals(name, other.getName()) &&
        		EqualityChecker.equals(owner, other.getOwner()) &&
        		EqualityChecker.equals(messageBoxAddress, other.getMessageBoxAddress()) && 
        		EqualityChecker.equals(agentNodeUUID, other.getAgentNodeUUID());
    }
    
    /**
     * XXX This is symmetric (and already was before I changed it), i.e. Agent(123) matches
     * template Agent(null), but Agent(null) also matches template Agent(123). Is this intended?
     */
    @Override
    public boolean matches(IAgentDescription template) {
    	if(this == template) {
            return true;
        }
        
        final AgentDescription other= (AgentDescription) template;
        return EqualityChecker.equalsOrNull(aid, other.getAid()) && 
        		EqualityChecker.equalsOrNull(name, other.getName()) &&
        		EqualityChecker.equalsOrNull(owner, other.getOwner()) &&
        		EqualityChecker.equalsOrNull(messageBoxAddress, other.getMessageBoxAddress()) && 
        		EqualityChecker.equalsOrNull(agentNodeUUID, other.getAgentNodeUUID());
    }

    /**
	 * Returns the hash code by calculation from this class and the AID, 
	 * thus it is the same hash code for all agent descriptions with the same AID.
	 * @return the calculated hash code
     */
    @Override
    public int hashCode() {
        return AgentDescription.class.hashCode() ^ (aid != null ? aid.hashCode() : 0);
    }

    /**
	 * Returns a multiline text which contains the name, identifier, life-cycle state,
	 * communication address, and mobility of the agent description.
	 * @return a string representation of the agent description
     */
    @Override
    public String toString() {
        final StringBuilder builder= new StringBuilder();

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

        // owner
        builder.append("\n owner=");
        if (owner != null) {
        	builder.append("'").append(owner).append("'");
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
        
        // mobile
        if (isMobile != null) {
        	builder.append("\n mobile=").append(isMobile.toString());
        }
        else {
        	builder.append("\n mobile=null");
        }

        builder.append('\n');
        return builder.toString();
    }

    /**
     * Returns the UUID of the AgentNode that is holding this agent.
     * @return UUID of the agent node
     */
	public String getAgentNodeUUID() {
		return agentNodeUUID;
	}
	
	/**
	 * Sets the UUID of the AgentNode that is holding this agent.
	 * @param uuid the UUID of the agent node to set
	 */
	public void setAgentNodeUUID(String uuid){
		agentNodeUUID = uuid;
	}

	/**
	 * Set the mobility of the agent.
	 * @param mobile the mobility to set
	 */
	public void setMobile(Boolean mobile) {
		
		if (mobile == null) {
			return;
		}
		
		isMobile = mobile;
	}

	/**
	 * Get the mobility of the agent.
	 * @return the mobility
	 */
	public Boolean isMobile() {
		return isMobile;
	}

	private String[] getItemNames() {
		return new String[] {
	    		ITEMNAME_ID,
	    		ITEMNAME_NAME,
	    		ITEMNAME_OWNER,
	    		ITEMNAME_NODE,
	    	    ITEMNAME_STATE,
	    		ITEMNAME_MOBILE,
	    		ITEMNAME_MESSAGEBOX
	    };
	}

	   /**
	    * Gets the type of JIAC agent descriptions based on JMX open types.
	    * 
	    * @return A composite type containing agent id, name, owner, node's UUID, state, mobility, and message box address.
	    * @throws OpenDataException
	    *             if an error occurs during the creation of the type.
	    * @see javax.management.openmbean.CompositeType
	    */
	   public OpenType<?> getDescriptionType() throws OpenDataException {
	      final OpenType<?>[] itemTypes = new OpenType<?>[] {
	    		  SimpleType.STRING, 
	    		  SimpleType.STRING, 
	    		  SimpleType.STRING, 
	    		  SimpleType.STRING, 
	    		  SimpleType.STRING, 
	    		  SimpleType.BOOLEAN,
	    		  SimpleType.STRING
	      };

	      // use names of agent description items as their description
	      final String[] itemDescriptions = getItemNames();

	      // create and return open type of a JIAC agent description
	      return new CompositeType(this.getClass().getName(), "standard JIAC-TNG agent description", getItemNames(), itemDescriptions, itemTypes);
	   }

	   /**
	    * Gets the description of this JIAC agent description based on JMX open types.
	    * 
	    * @return Composite data containing agent id, name, owner, node's UUID, state, mobility, and message box address.
	    * @throws OpenDataException
	    *             if an error occurs during the creation of the data.
	    * @see javax.management.openmbean.CompositeData
	    */
	   public Object getDescription() throws OpenDataException {
	      final Object[] itemValues = new Object[] {
	    		  aid,
	    		  name,
	    		  owner,
	    		  agentNodeUUID,
	    		  state,
	    		  isMobile,
	    		  (messageBoxAddress != null)? messageBoxAddress.getName():null,
	      };

	      final CompositeType type = (CompositeType) getDescriptionType();
	      return new CompositeDataSupport(type, getItemNames(), itemValues);
	   }
}
