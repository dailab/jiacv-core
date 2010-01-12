package de.dailab.jiactng.agentcore.ontology;

import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

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
	private String state;
	
	/** Communication Identifier. */
	private IMessageBoxAddress messageBoxAddress;
	
	/** UUID of the AgentNode that holds this agent */
	private String agentNodeUUID;
	
	/** Flag, if it is set <code>true</code>, it is a mobile agent */
	private Boolean isMobile = Boolean.valueOf(false);
	
//	public AgentDescription(String aid, String name, String state) {
//        this(aid, name, state, name != null ? CommunicationAddressFactory.createMessageBoxAddress(aid) : null);
//	}
    
	public AgentDescription() {
	    this(null, null, null, null, null, null);
	}
	
    public AgentDescription(String aid, String name, String state, IMessageBoxAddress messageBoxAddress, String agentNodeUUID) {
//        this.aid=aid;
//        this.name=name;
//        this.state=state;
//        this.messageBoxAddress= messageBoxAddress;
//        this.agentNodeUUID = agentNodeUUID;
    	this( aid,  name,  state,  messageBoxAddress,  agentNodeUUID, Boolean.valueOf(false));
    }
    
    public AgentDescription(String aid, String name, String state, IMessageBoxAddress messageBoxAddress, String agentNodeUUID, Boolean mobile) {
        this.aid=aid;
        this.name=name;
        this.state=state;
        this.messageBoxAddress= messageBoxAddress;
        this.agentNodeUUID = agentNodeUUID;
        this.isMobile = mobile;
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
        return EqualityChecker.equalsOrNull(aid, other.aid);
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
     * Returns the UUID of the AgentNode that is holding this agent
     * @return UUID of the agent node
     */
	public String getAgentNodeUUID() {
		return agentNodeUUID;
	}
	
	/**
	 * sets the UUID of the AgentNode that is holding this agent
	 */
	public void setAgentNodeUUID(String UUID){
		agentNodeUUID = UUID;
	}
	
	
	public void setMobile(Boolean mobile) {
		
		if (mobile == null) {
			return;
		}
		
		isMobile = mobile;
	}
	
	public Boolean isMobile() {
		return isMobile;
	}

	private String[] getItemNames() {
		return new String[] {
	    		ITEMNAME_ID,
	    		ITEMNAME_NAME,
	    		ITEMNAME_NODE,
	    	    ITEMNAME_STATE,
	    		ITEMNAME_MOBILE,
	    		ITEMNAME_MESSAGEBOX
	    };
	}

	   /**
	    * Gets the type of JIAC agent descriptions based on JMX open types.
	    * 
	    * @return A composite type containing agent id, name, node's UUID, state, mobility, and message box address.
	    * @throws OpenDataException
	    *             if an error occurs during the creation of the type.
	    * @see javax.management.openmbean.CompositeType
	    */
	   public OpenType<?> getDescriptionType() throws OpenDataException {
	      OpenType<?>[] itemTypes = new OpenType<?>[] {
	    		  SimpleType.STRING, 
	    		  SimpleType.STRING, 
	    		  SimpleType.STRING, 
	    		  SimpleType.STRING, 
	    		  SimpleType.BOOLEAN,
	    		  SimpleType.STRING
	      };

	      // use names of agent description items as their description
	      String[] itemDescriptions = getItemNames();

	      // create and return open type of a JIAC agent description
	      return new CompositeType(this.getClass().getName(), "standard JIAC-TNG agent description", getItemNames(), itemDescriptions, itemTypes);
	   }

	   /**
	    * Gets the description of this JIAC agent description based on JMX open types.
	    * 
	    * @return Composite data containing agent id, name, node's UUID, state, mobility, and message box address.
	    * @throws OpenDataException
	    *             if an error occurs during the creation of the data.
	    * @see javax.management.openmbean.CompositeData
	    */
	   public Object getDescription() throws OpenDataException {
	      Object[] itemValues = new Object[] {
	    		  aid,
	    		  name,
	    		  agentNodeUUID,
	    		  state,
	    		  isMobile,
	    		  (messageBoxAddress != null)? messageBoxAddress.getName():null,
	      };

	      CompositeType type = (CompositeType) getDescriptionType();
	      return new CompositeDataSupport(type, getItemNames(), itemValues);
	   }
}
