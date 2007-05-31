/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.knowledge;

import java.util.Iterator;
import java.util.Set;

import javax.security.auth.DestroyFailedException;

import org.sercho.masp.space.ObjectMatcher;
import org.sercho.masp.space.ObjectUpdater;
import org.sercho.masp.space.SimpleObjectSpace;
import org.sercho.masp.space.event.EventedSpaceWrapper;
import org.sercho.masp.space.event.EventedTupleSpace;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.EventedSpaceWrapper.SpaceDestroyer;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

/**
 * @author Thomas Konnerth
 * @author axle
 * 
 * @see de.dailab.jiactng.agentcore.knowledge.IMemory
 */
public class Memory extends AbstractLifecycle implements IMemory {

	private SpaceDestroyer<IFact> destroyer;
	private EventedTupleSpace<IFact> space;

    /**
     * During initialization the TupleSpace is created.
     * 
     * @see org.sercho.masp.space.TupleSpace
     */
	@Override
	public void doInit() throws LifecycleException {
		destroyer = EventedSpaceWrapper.getSpaceWithDestroyer(new SimpleObjectSpace<IFact>("FactBase"));
		space = destroyer.space;
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void doStart() throws LifecycleException {
		// nothing to do yet
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doStop() throws LifecycleException {
		// nothing to do yet
		// persistency may go here
	}

    /**
     * {@inheritDoc}
     */
	@Override
	public void doCleanup() throws LifecycleException {
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
	public <E extends IFact> E read(E template) {
		return space.read(template);
	}

    /**
     * {@inheritDoc}
     */
	public <E extends IFact> E read(E template, long timeout) {
		return space.read(template, timeout);
	}

    /**
     * {@inheritDoc}
     */
	public <E extends IFact> Set<E> readAll(E template) {
		return space.readAll(template);
	}

    /**
     * {@inheritDoc}
     */
	public <E extends IFact> Set<E> readAllOfType(Class<E> c) {
		return space.readAllOfType(c);
	}

    /**
     * {@inheritDoc}
     */
	public <E extends IFact> E remove(E template) {
		return space.remove(template);
	}

    /**
     * {@inheritDoc}
     */
	public <E extends IFact> E remove(E template, long timeout) {
		return space.remove(template, timeout);
	}

    /**
     * {@inheritDoc}
     */
	public <E extends IFact> Set<E> removeAll(E template) {
		return space.removeAll(template);
	}

    /**
     * {@inheritDoc}
     */
	public <E extends IFact> boolean update(E template, E pattern) {
		return space.update(template, pattern);
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

    /**
     * {@inheritDoc}
     */
	public void attach(SpaceObserver<? super IFact> observer) {
		space.attach(observer);
	}

    /**
     * {@inheritDoc}
     */
	public void attach(SpaceObserver<? super IFact> observer, IFact template) {
		space.attach(observer, template);
	}

    /**
     * {@inheritDoc}
     */
	public void detach(SpaceObserver<? super IFact> observer) {
		space.detach(observer);
	}
}
