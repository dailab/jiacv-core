/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.knowledge;

import java.util.Iterator;
import java.util.Set;

import org.sercho.masp.space.ObjectMatcher;
import org.sercho.masp.space.ObjectUpdater;
import org.sercho.masp.space.SimpleObjectSpace;
import org.sercho.masp.space.TupleSpace;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

/**
 * @author Thomas Konnerth
 * @author axle
 * 
 * @see de.dailab.jiactng.agentcore.knowledge.IMemory
 * @see de.dailab.jiactng.agentcore.knowledge.Tuple
 */
public class Memory extends AbstractLifecycle implements IMemory {

	private TupleSpace<IFact> space;

	private int timeOut = 10000;

    /**
     * {@inheritDoc}
     */
	@Override
	public void doCleanup() throws LifecycleException {
		space = null;
	}

    /**
     * During initialization the TupleSpace is created.
     * 
     * @see org.sercho.masp.space.TupleSpace
     */
	@Override
	public void doInit() throws LifecycleException {
		space = new SimpleObjectSpace<IFact>("FactBase");
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void doStart() throws LifecycleException {
		// TODO Auto-generated method stub
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void doStop() throws LifecycleException {
		// TODO Auto-generated method stub
	}

    /**
     * {@inheritDoc}
     */
	public int getTimeOut() {
		return timeOut;
	}

    /**
     * {@inheritDoc}
     */
	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

    /**
     * {@inheritDoc}
     */
	public TupleSpace<IFact> getTupleSpace() {
		return space;
	}

    /**
     * {@inheritDoc}
     */
	public void setTupleSpace(TupleSpace<IFact> space) {
		this.space = space;
	}

    /**
     * {@inheritDoc}
     */
	public String getID() {
		return space.getID();
	}

    /**
     * {@inheritDoc}
     */
	public ObjectMatcher getMatcher() {
		return space.getMatcher();
	}

    /**
     * {@inheritDoc}
     */
	public ObjectUpdater getUpdater() {
		return space.getUpdater();
	}

    /**
     * {@inheritDoc}
     */
	public IFact read(IFact template) {
		return space.read(template);
	}

    /**
     * {@inheritDoc}
     */
	public IFact read(IFact template, long timeOut) {
		return space.read(template, timeOut);
	}

    /**
     * {@inheritDoc}
     */
	public Set<IFact> readAll(IFact template) {
		return space.readAll(template);
	}

    /**
     * {@inheritDoc}
     */
	@SuppressWarnings("unchecked")
	public Set<IFact> readAllOfType(Class classname) {
		return space.readAllOfType(classname);
	}

    /**
     * {@inheritDoc}
     */
	public IFact remove(IFact template) {
		return space.remove(template);
	}

    /**
     * {@inheritDoc}
     */
	public IFact remove(IFact template, long timeOut) {
		return space.remove(template, timeOut);
	}

    /**
     * {@inheritDoc}
     */
	public Set<IFact> removeAll(IFact template) {
		return space.removeAll(template);
	}

    /**
     * {@inheritDoc}
     */
	public boolean update(IFact template, IFact substPattern) {
		return space.update(template, substPattern);
	}

    /**
     * {@inheritDoc}
     */
	public void write(IFact fact) {
		space.write(fact);
	}

    /**
     * {@inheritDoc}
     */
	public Iterator<IFact> iterator() {
		return space.iterator();
	}

}
