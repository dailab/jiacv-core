/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.knowledge;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.security.auth.DestroyFailedException;

import org.sercho.masp.space.ObjectMatcher;
import org.sercho.masp.space.ObjectUpdater;
import org.sercho.masp.space.SimpleObjectSpace;
import org.sercho.masp.space.event.EventedSpaceWrapper;
import org.sercho.masp.space.event.EventedTupleSpace;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.EventedSpaceWrapper.SpaceDestroyer;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.management.Manager;

/**
 * Implementation of an object memory based on tuple space technology.
 * @see org.sercho.masp.space.event.EventedTupleSpace
 * @author Thomas Konnerth
 * @author axle
 * 
 * @see de.dailab.jiactng.agentcore.knowledge.IMemory
 */
public class Memory extends AbstractLifecycle implements IMemory, MemoryMBean {

	/** SerialVersionUID for Serialization */
	private static final long serialVersionUID = -5229424084593098741L;

	
	private SpaceDestroyer<IFact> destroyer = null;
	private EventedTupleSpace<IFact> space = null;

	/** The agent which contains this memory */
	private transient IAgent thisAgent = null;

    /**
     * During initialization the TupleSpace is created.
     * 
     * @see org.sercho.masp.space.TupleSpace
     */
	@Override
	public void doInit() {
		destroyer = EventedSpaceWrapper.getSpaceWithDestroyer(new SimpleObjectSpace<IFact>("FactBase"));
		space = destroyer.destroybleSpace;
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void doStart() {
		// nothing to do yet
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doStop() {
		// nothing to do yet
		// persistency may go here
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void doCleanup() {
		try {
			destroyer.destroy();
		} catch (DestroyFailedException e) {
			e.printStackTrace();
		}
		space = null;
		destroyer = null;
	}

    /**
     * {@inheritDoc}
     */
	public String getID() {
		if(space==null) {
			throw new RuntimeException("Memory has not yet been initialized!");
		}
		return space.getID();
	}

    /**
     * {@inheritDoc}
     */
	public ObjectMatcher getMatcher() {
		if(space==null) {
			throw new RuntimeException("Memory has not yet been initialized!");
		}
		return space.getMatcher();
	}

    /**
     * {@inheritDoc}
     */
	public ObjectUpdater getUpdater() {
		if(space==null) {
			throw new RuntimeException("Memory has not yet been initialized!");
		}
		return space.getUpdater();
	}

    /**
     * {@inheritDoc}
     */
	public <E extends IFact> E read(E template) {
		if(space==null) {
			throw new RuntimeException("Memory has not yet been initialized!");
		}
		return space.read(template);
	}

    /**
     * {@inheritDoc}
     */
	public <E extends IFact> E read(E template, long timeout) {
		if(space==null) {
			throw new RuntimeException("Memory has not yet been initialized!");
		}
		return space.read(template, timeout);
	}

    /**
     * {@inheritDoc}
     */
	public <E extends IFact> Set<E> readAll(E template) {
		if(space==null) {
			throw new RuntimeException("Memory has not yet been initialized!");
		}
		return space.readAll(template);
	}

    /**
     * {@inheritDoc}
     */
	public <E extends IFact> Set<E> readAllOfType(Class<E> c) {
		if(space==null) {
			throw new RuntimeException("Memory has not yet been initialized!");
		}
		return space.readAllOfType(c);
	}

    /**
     * {@inheritDoc}
     */
	public <E extends IFact> E remove(E template) {
		if(space==null) {
			throw new RuntimeException("Memory has not yet been initialized!");
		}
		return space.remove(template);
	}

    /**
     * {@inheritDoc}
     */
	public <E extends IFact> E remove(E template, long timeout) {
		if(space==null) {
			throw new RuntimeException("Memory has not yet been initialized!");
		}
		return space.remove(template, timeout);
	}

    /**
     * {@inheritDoc}
     */
	public <E extends IFact> Set<E> removeAll(E template) {
		if(space==null) {
			throw new RuntimeException("Memory has not yet been initialized!");
		}
		return space.removeAll(template);
	}

    /**
     * {@inheritDoc}
     */
	public <E extends IFact> boolean update(E template, E pattern) {
		if(space==null) {
			throw new RuntimeException("Memory has not yet been initialized!");
		}
		return space.update(template, pattern);
	}

	/**
	 * {@inheritDoc}
	 */
	public void write(IFact fact) {
		if(space==null) {
			throw new RuntimeException("Memory has not yet been initialized!");
		}
		space.write(fact);
	}

    /**
     * {@inheritDoc}
     */
	public Iterator<IFact> iterator() {
		if(space==null) {
			throw new RuntimeException("Memory has not yet been initialized!");
		}
		return space.iterator();
	}

    /**
     * {@inheritDoc}
     */
	public void attach(SpaceObserver<? super IFact> observer) {
		if(space==null) {
			throw new RuntimeException("Memory has not yet been initialized!");
		}
		space.attach(observer);
	}

    /**
     * {@inheritDoc}
     */
	public void attach(SpaceObserver<? super IFact> observer, IFact template) {
		if(space==null) {
			throw new RuntimeException("Memory has not yet been initialized!");
		}
		space.attach(observer, template);
	}

    /**
     * {@inheritDoc}
     */
	public void detach(SpaceObserver<? super IFact> observer) {
		if(space==null) {
			throw new RuntimeException("Memory has not yet been initialized!");
		}
		space.detach(observer);
	}

	/**
	 * Sets the reference to the agent which contains this memory.
	 * @param agent the agent which contains this memory
	 */
	public void setThisAgent(IAgent agent) {
		thisAgent = agent;
	}

	/**
	 * Information about the facts stored in the memory.
	 * @return information about facts stored in memory
	 */
	@SuppressWarnings("unchecked")
    public CompositeData getSpace() {
	    Set<IFact> facts = readAllOfType(IFact.class);
	    if (facts.isEmpty()) {
	    	return null;
	    }

	    // create map with current memory state
		Map<String,List<String>> map = new Hashtable<String,List<String>>();
	    for (IFact fact : facts) {
	    	String classname = fact.getClass().getName();
	    	List<String> values = map.get(classname);
	    	if (values == null) {
	    		values = new ArrayList<String>();
	    		map.put(classname, values);
	    	}
    		values.add(fact.toString());
	    }

	    // create composite data
	    CompositeData data = null;
	    int size = map.size();
	    String[] itemNames = new String[size];
	    OpenType<?>[] itemTypes = new OpenType[size];
	    Object[] itemValues = new Object[size];
	    Object[] classes = map.keySet().toArray();
	    try {
	    	for (int i=0; i<size; i++) {
	    		String classname = (String) classes[i];
	    		itemNames[i] = classname;
	    		itemTypes[i] = new ArrayType(1, SimpleType.STRING);
	    		List<String> values = map.get(classname);
	    		String[] value = new String[values.size()];
	    		Iterator<String> it = values.iterator();	    		
	    		int j = 0;
	    		while (it.hasNext()) {
	    			value[j] = it.next();
	    			j++;
	    		}
	    		itemValues[i] = value;
	    	}
	    	CompositeType compositeType = new CompositeType(map.getClass().getName(), "facts stored in the memory", itemNames, itemNames, itemTypes);
	    	data = new CompositeDataSupport(compositeType, itemNames, itemValues);
	    }
	    catch (OpenDataException e) {
	    	e.printStackTrace();
	    }

	    return data;
	}

	/**
     * Registers the memory for management.
     * @param manager the manager to be used for registration
	 */
	public void enableManagement(Manager manager) {
		// do nothing if management already enabled
		if (isManagementEnabled()) {
			return;
		}
		
		// register memory for management
		try {
			manager.registerAgentResource(thisAgent, "Memory", this);
		}
		catch (Exception e) {
			System.err.println("WARNING: Unable to register memory of agent " + thisAgent.getAgentName() + " of agent node " + thisAgent.getAgentNode().getName() + " as JMX resource.");
			System.err.println(e.getMessage());					
		}

		super.enableManagement(manager);
	}
	  
	/**
	 * Deregisters the memory from management.
	 */
	public void disableManagement() {
		// do nothing if management already disabled
		if (!isManagementEnabled()) {
			return;
		}
		
		// deregister memory from management
		try {
			_manager.unregisterAgentResource(thisAgent, "Memory");
		}
		catch (Exception e) {
			System.err.println("WARNING: Unable to deregister memory of agent " + thisAgent.getAgentName() + " of agent node " + thisAgent.getAgentNode().getName() + " as JMX resource.");
			System.err.println(e.getMessage());					
		}
		
		super.disableManagement();
	}

    /**
     * {@inheritDoc}
     */
	public Set<IFact> readAll() {
		if(space==null) {
			throw new RuntimeException("Memory has not yet been initialized!");
		}
		return space.readAll();
	}	  
}
