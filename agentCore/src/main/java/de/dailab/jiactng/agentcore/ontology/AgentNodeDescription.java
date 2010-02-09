package de.dailab.jiactng.agentcore.ontology;

import java.util.Date;

import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;

/**
 * The datastructure to store information about an agentnode.
 * 
 * @author axle
 */
public class AgentNodeDescription implements IAgentNodeDescription {
	private static final long serialVersionUID = -7460322120270381260L;

	/** The messageBox of the node.*/
	private ICommunicationAddress address;
	
	/** Last update received. */
	private long alive;

	/**
	 * Constructor for setting address and alive time of the agentnode.
	 * @param address the messageBox address of the agentnode
	 * @param alive the time when the node has reported alive
	 */
	public AgentNodeDescription(ICommunicationAddress address, long alive) {
		this.address = address;
		this.alive = alive;
	}

	/**
	 * Returns the messageBox address of the agentnode.
	 * @return the messageBox address of the agentnode
	 * @see ICommunicationAddress
	 */
	public ICommunicationAddress getAddress() {
		return address;
	}

	/**
	 * Sets the messageBox address of the node.
	 * @param newAddress the address of the node
	 * @see ICommunicationAddress
	 */
	public void setAddress(ICommunicationAddress newAddress) {
		address = newAddress;
	}

	/**
	 * Returns the last time the agentnode has sent a sign of life.
	 * @return the last time the agentnode has sent a sign of life
	 */
	public long getAlive() {
		return alive;
	}

	/**
	 * Sets the time when the node has reported alive.
	 * @param newAlive the time when the node has reported alive
	 */
	public void setAlive(long newAlive) {
		alive = newAlive;
	}

	private String[] getItemNames() {
		return new String[] {
				ITEMNAME_ADDRESS,
				ITEMNAME_ALIVE
	    };
	}

	   /**
	    * Gets the type of JIAC agent node descriptions based on JMX open types.
	    * 
	    * @return A composite type containing agent node address and last time of alive.
	    * @throws OpenDataException
	    *             if an error occurs during the creation of the type.
	    * @see javax.management.openmbean.CompositeType
	    */
	   public OpenType<?> getDescriptionType() throws OpenDataException {
	      final OpenType<?>[] itemTypes = new OpenType<?>[] {
	    		  SimpleType.STRING, 
	    		  SimpleType.STRING, 
	      };

	      // use names of agent node description items as their description
	      final String[] itemDescriptions = getItemNames();

	      // create and return open type of a JIAC agent node description
	      return new CompositeType(this.getClass().getName(), "standard JIAC-TNG agent node description", getItemNames(), itemDescriptions, itemTypes);
	   }

	   /**
	    * Gets the description of this JIAC agent node description based on JMX open types.
	    * 
	    * @return Composite data containing agent node address and last time of alive.
	    * @throws OpenDataException
	    *             if an error occurs during the creation of the data.
	    * @see javax.management.openmbean.CompositeData
	    */
	   public Object getDescription() throws OpenDataException {
	      final Object[] itemValues = new Object[] {
	    		  (address != null)? address.getName():null,
	    		  new Date(alive).toString()
	      };

	      final CompositeType type = (CompositeType) getDescriptionType();
	      return new CompositeDataSupport(type, getItemNames(), itemValues);
	   }
}
