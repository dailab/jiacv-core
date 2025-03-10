package de.dailab.jiactng.agentcore.ontology;

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.remote.JMXServiceURL;

import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;

/**
 * The data structure to store information about an agent node.
 * 
 * @author axle
 */
public class AgentNodeDescription implements IAgentNodeDescription {
	private static final long serialVersionUID = -7460322120270381260L;

	/** The messageBox of the node.*/
	private ICommunicationAddress address;

	/** The JMX connector servers of the node. */
	private Set<JMXServiceURL> jmxURLs;

	/** Last update received. */
	private long alive;

	/** The alive interval of the node's directory for this group */
	private long aliveInterval;

	/**
	 * Constructor for setting address and alive time of the agent node.
	 * @param address the messageBox address of the agent node
	 * @param alive the time when the node has reported alive
	 */
	public AgentNodeDescription(ICommunicationAddress address, long alive) {
		this.address = address;
		this.alive = alive;
	}

	/**
	 * Creates an agent node description from JMX composite data.
	 * @param descr the agent node description based on JMX open types.
	 */
	public AgentNodeDescription(CompositeData descr) {
		// set address
		String commAddress = (String) descr.get(IAgentNodeDescription.ITEMNAME_ADDRESS);
		if (commAddress != null) {
			address = CommunicationAddressFactory.createMessageBoxAddress(commAddress);
		}

		// set JMX URLs
		jmxURLs = new HashSet<JMXServiceURL>();
		for (String url : (String[])descr.get(IAgentNodeDescription.ITEMNAME_JMXURLS)) {
			try {
				jmxURLs.add(new JMXServiceURL(url));
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		// set alive date
		try {
			alive = DateFormat.getInstance().parse((String)descr.get(IAgentNodeDescription.ITEMNAME_ALIVE)).getTime();
		}
		catch (java.text.ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the messageBox address of the agent node.
	 * @return the messageBox address of the agent node
	 * @see ICommunicationAddress
	 */
	public final ICommunicationAddress getAddress() {
		return address;
	}

	/**
	 * Sets the messageBox address of the agent node.
	 * @param newAddress the address of the agent node
	 * @see ICommunicationAddress
	 */
	public final void setAddress(ICommunicationAddress newAddress) {
		address = newAddress;
	}

	/**
	 * Returns the URLs of all JMX connector server of the agent node.
	 * @return the URLs of the JMX connector server of the agent node
	 */
	public final Set<JMXServiceURL> getJmxURLs() {
		return jmxURLs;
	}

	/**
	 * Sets the URLs of all JMX connector server of the agent node.
	 * @param newJmxURLs the URLs of the JMX connector server of the agent node
	 */
	public final void setJmxURLs(Set<JMXServiceURL> newJmxURLs) {
		jmxURLs = Collections.unmodifiableSet(newJmxURLs);
	}

	/**
	 * Returns the last time the agent node has sent a sign of life.
	 * @return the last time the agent node has sent a sign of life
	 */
	public final long getAlive() {
		return alive;
	}

	/**
	 * Sets the time when the node has reported alive.
	 * @param newAlive the time when the node has reported alive
	 */
	public final void setAlive(long newAlive) {
		alive = newAlive;
	}

	/**
	 * Gets the interval in which the node reports alive.
	 * @return the interval in milliseconds
	 */
	public final long getAliveInterval() {
		return aliveInterval;
	}

	/**
	 * Sets the interval in which the node reports alive.
	 * @param aliveInterval the interval in milliseconds
	 */
	public final void setAliveInterval(long aliveInterval) {
		this.aliveInterval = aliveInterval;
	}

	private String[] getItemNames() {
		return new String[] {
				ITEMNAME_ADDRESS,
				ITEMNAME_JMXURLS,
				ITEMNAME_ALIVE,
				ITEMNAME_ALIVE_INTERVAL
	    };
	}

	/**
	 * Gets the type of JIAC agent node descriptions based on JMX open types.
	 * 
	 * @return A composite type containing agent node address, JMX URLs, last time of alive, and alive interval.
	 * @throws OpenDataException
	 *             if an error occurs during the creation of the type.
	 * @see javax.management.openmbean.CompositeType
	 */
	public OpenType<?> getDescriptionType() throws OpenDataException {
		final OpenType<?>[] itemTypes = new OpenType<?>[] {
				SimpleType.STRING,
				ArrayType.getArrayType(SimpleType.STRING),
				SimpleType.STRING,
				SimpleType.LONG
		};

		// use names of agent node description items as their description
		final String[] itemDescriptions = getItemNames();

		// create and return open type of a JIAC agent node description
		return new CompositeType(this.getClass().getName(), "standard JIAC-TNG agent node description", getItemNames(), itemDescriptions, itemTypes);
	}

	/**
	 * Gets the description of this JIAC agent node description based on JMX open types.
	 * 
	 * @return Composite data containing agent node address, JMX URLs, last time of alive, and alive interval.
	 * @throws OpenDataException
	 *             if an error occurs during the creation of the data.
	 * @see javax.management.openmbean.CompositeData
	 */
	public Object getDescription() throws OpenDataException {
		String[] urls = null;
		if (jmxURLs != null) {
			int i=0;
			urls = new String[jmxURLs.size()];
			for (JMXServiceURL url: jmxURLs) {
				urls[i++] = url.toString();
			}
		}
		final Object[] itemValues = new Object[] {
				(address != null)? address.getName():null,
				urls,
				new Date(alive).toString(),
				aliveInterval
		};

		final CompositeType type = (CompositeType) getDescriptionType();
		return new CompositeDataSupport(type, getItemNames(), itemValues);
	}
}
