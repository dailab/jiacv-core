package de.dailab.jiactng.agentcore.servicediscovery;

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

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * @author Janko Dimitroff
 * Eigener Memory - der nicht den Lifecycle implementiert. Plain Java class.
 * Benutzt den TupleSpace.
 */
public class ServiceDirectoryMemory {

	private SpaceDestroyer<IFact> destroyer;
	private EventedTupleSpace<IFact> space;

	public ServiceDirectoryMemory() {
		doInit();
	}

	/**
	 * During initialization the TupleSpace is created.
	 * 
	 * @see org.sercho.masp.space.TupleSpace
	 */
	public void doInit() {
		destroyer = EventedSpaceWrapper.getSpaceWithDestroyer(new SimpleObjectSpace<IFact>("FactBase"));
		space = destroyer.space;
	}

	/**
	 * {@inheritDoc}
	 */
	public void doStart() {
	// nothing to do yet
	}

	/**
	 * {@inheritDoc}
	 */
	public void doStop() {
	// nothing to do yet
	// persistency may go here
	}

	/**
	 * {@inheritDoc}
	 */
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
