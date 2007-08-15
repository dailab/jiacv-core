package de.dailab.jiactng.agentcore.knowledge;

import javax.management.openmbean.CompositeData;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean;

/**
 * JMX-compliant management interface of a memory.
 * @author Jan Keiser
 */
public interface MemoryMBean extends AbstractLifecycleMBean {

	/**
	 * Information about the facts stored in the memory.
	 * @return information about facts stored in memory
	 */
	public CompositeData getSpace();	
}
