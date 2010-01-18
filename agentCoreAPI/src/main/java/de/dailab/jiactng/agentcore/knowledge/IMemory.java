/*
 * Created on 20.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.knowledge;

import org.sercho.masp.space.event.EventedTupleSpace;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;

/**
 * This interface declares methods which must be implemented
 * by the memory implementation. This interface is geared to
 * the <code>TupleSpace</code> interface of Grzegorz Lehmann.
 * Many thanks to Grzegorz.
 * 
 * @author Thomas Konnerth
 * @author axle
 * 
 * @see org.sercho.masp.space.TupleSpace
 * @see org.sercho.masp.space.event.EventedTupleSpace
 */
public interface IMemory extends ILifecycle, EventedTupleSpace<IFact> {

	/**
	 * Sets the reference to the agent which contains this memory.
	 * @param agent the agent which contains this memory
	 */
	void setThisAgent(IAgent agent);
}