/*
 * Created on 20.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.knowledge;

import java.util.Set;

import org.sercho.masp.space.ObjectMatcher;
import org.sercho.masp.space.ObjectUpdater;
import org.sercho.masp.space.TupleSpace;

import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;

/**
 * This interface declares methods which must be implemented
 * by the memory implementation. This interface is geared to
 * the <code>TupleSpace</code> interface of Grzegorz Lehmann.
 * Many thanks to Grzegorz.
 * 
 * @author Thomas Konnerth
 * @author axle
 * @see org.sercho.masp.space.TupleSpace
 */
public interface IMemory extends ILifecycle, Iterable<IFact> {

    /**
     * <code>getID</code> returns the ID of the space implementation of memory
     * 
     * @return String - ID of this space
     */
    public abstract String getID();

	/**
	 * <code>getMatcher</code> returns the matcher used by memory.
	 * 
	 * @return ObjectMatcher - matcher used by memory
	 * @see org.sercho.masp.space.ObjectMatcher
	 */
	public abstract ObjectMatcher getMatcher();
	
	/**
	 * <code>getUpdater</code> returns the updater used memory for
	 * {@link #update(IFact, IFact)} calls.
	 * 
	 * @return ObjectUpdater - updater used by memory
	 * @see org.sercho.masp.space.ObjectUpdater
	 */
	public abstract ObjectUpdater getUpdater();

	/**
	 * Getter for the timeout property. This timeout is used by all read- and
	 * out-operations. Default is 10000, negative values will always be changed to
	 * Integer.MAX_VALUE.
	 * 
	 * @return the current timeout value.
	 */
	public abstract int getTimeOut();

	/**
	 * Setter for the timeout property. This timeout is used by all read- and
	 * out-operations. Default is 10000, negative values will set the timeout to
	 * Integer.MAX_VALUE.
	 * 
	 * @param timeOut
	 *          the new timeOut value.
	 */
	public abstract void setTimeOut(int timeOut);

	/**
	 * Return the internal TupleSpace implementation
	 * @return the actual TupleSpace
	 */
	public abstract TupleSpace<IFact> getTupleSpace();

	/**
	 * Set the internal TupleSpace implementation
	 * @param space
	 */
	public abstract void setTupleSpace(TupleSpace<IFact> space);
	
	/**
	 * <code>read</code> returns fact from Memory, which matches
	 * <code>template</code> or <code>null</code> if none could be found. The
	 * <code>template</code> will be matched to the returned fact according to
	 * general rules of tuple spaces. All fields of <code>template</code> will
	 * be compared with those of the fact. <code>null</code> is the wildcard.
	 * A tuple space must support subclassing, in other words objects
	 * subclassing <code>template</code> will also be compared.
	 * <p>
	 * If multiple facts match <code>template</code>, no rules are defined as
	 * which of them will be returned. The behaviour may differ between space
	 * implementations. 
	 * <p>
	 * However, implementing classes may use custom template-matching algorithms
	 * and thus their behaviour may differ. This should be documented in the
	 * specific implementation.
	 * 
	 * @param template specifies the fact to be returned
	 * @return a fact or <code>null</code> if none found
     * @throws IllegalArgumentException if <code>template</code> is <code>null</code>
	 * @see org.sercho.masp.space.ObjectMatcher
	 */
	public abstract IFact read(IFact template);

	/**
	 * <code>read</code> returns one fact from the space, which matches
	 * <code>template</code>. If no matching fact is found, this method waits
	 * until one arrives. The maximum amount of time to block is specified in
	 * the parameters. If no matching fact is found during the timeout,
	 * <code>null</code> is returned. The <code>template</code> will be matched
	 * to the returned fact according to general rules of tuple spaces. All
	 * fields of <code>template</code> will be compared with those of the fact.
	 * <code>null</code> is the wildcard. A tuple space must support
	 * subclassing, in other words objects subclassing <code>template</code>
	 * will also be compared.
	 * <p>
	 * If multiple facts match <code>template</code>, no rules are defined as
	 * which of them will be returned. The behaviour may differ between space
	 * implementations. 
	 * <p>
	 * However, implementing classes may use custom template-matching algorithms
	 * and thus their behaviour may differ. This should be documented in the
	 * specific implementation.
	 * 
	 * @param template specifies the fact to be returned
	 * @param timeOut amount of time to wait in milliseconds
	 * @return a fact or <code>null</code> if none found or the timeout passed
     * @throws IllegalArgumentException if <code>template</code> is <code>null</code>
	 * @see org.sercho.masp.space.ObjectMatcher
	 */
	public abstract IFact read(IFact template, long timeOut);

	/**
	 * <code>readAll</code> returns a set of all facts from memory, which
	 * match <code>template</code>. The <code>template</code> will be matched to
	 * the facts according to general rules of tuple spaces. All fields of
	 * <code>template</code> will be compared with those of the facts.
	 * <code>null</code> is the wildcard. A tuple space must support
	 * subclassing, in other words objects subclassing <code>template</code>
	 * will also be compared.
	 * <p>
	 * However, implementing classes may use custom template-matching algorithms
	 * and thus their behaviour may differ. This should be documented in the
	 * specific implementation.
	 * 
	 * @param template specifies the facts to be returned 
	 * @return Set<IFact> - matched facts
     * @throws IllegalArgumentException if <code>template</code> is <code>null</code>
	 * @see org.sercho.masp.space.ObjectMatcher
	 */
	public abstract Set<IFact> readAll(IFact template);

	/**
	 * <code>remove</code> removes and returns one fact from memory, which
	 * matches <code>template</code> or <code>null</code> if none could be
	 * found. The <code>template</code> will be matched to the facts according
	 * to general rules of tuple spaces. All fields of <code>template</code>
	 * will be compared with those of the fact. <code>null</code> is the
	 * wildcard. A tuple space must support subclassing, in other words objects
	 * subclassing <code>template</code> will also be compared.
	 * <p>
	 * If multiple facts match <code>template</code>, no rules are defined as
	 * which of them will be removed. The behaviour may differ between space
	 * implementations.
	 * <p>
	 * However, implementing classes may use custom template-matching algorithms
	 * and thus their behaviour may differ. This should be documented in the
	 * specific implementation.
	 * 
	 * @param template specifies the fact to be removed
	 * @return removed fact or <code>null</code> if none found
     * @throws IllegalArgumentException if <code>template</code> is <code>null</code>
	 * @see org.sercho.masp.space.ObjectMatcher
	 */
	public abstract IFact remove(IFact template);

    /**
     * <code>remove</code> removes and returns one fact from memory, which
     * matches <code>template</code>. If no matching fact is found, this method
     * waits until one arrives. The maximum amount of time to block is specified
     * in the parameters. If no matching fact is found during the timeout,
     * <code>null</code> is returned. The <code>template</code> will be matched
     * to the returned fact according to general rules of tuple spaces. All
     * fields of <code>template</code> will be compared with those of the fact.
     * <code>null</code> is the wildcard. A tuple space must support
     * subclassing, in other words objects subclassing <code>template</code>
     * will also be compared.
     * <p>
     * If multiple facts match <code>template</code>, no rules are defined as
     * which of them will be removed. The behaviour may differ between space
     * implementations. 
     * <p>
     * However, implementing classes may use custom template-matching algorithms
     * and thus their behaviour may differ. This should be documented in the
     * specific implementation.
     * 
     * @param template specifies the fact to be removed
     * @param timeout amount of time to wait in milliseconds
     * @return a fact or <code>null</code> if none found or the timeout passed
     * @throws IllegalArgumentException if <code>template</code> is <code>null</code>
     * @see org.sercho.masp.space.ObjectMatcher
     */
	public abstract IFact remove(IFact template, long timeOut);

	/**
	 * <code>removeAll</code> removes a set of all facts from the space, which
	 * match <code>template</code>. The <code>template</code> will be matched to
	 * the facts according to general rules of tuple spaces. All fields of
	 * <code>template</code> will be compared with those of the facts.
	 * <code>null</code> is the wildcard. A tuple space must support
	 * subclassing, in other words objects subclassing <code>template</code>
	 * will also be compared.
	 * <p>
	 * However, implementing classes may use custom template-matching algorithms
	 * and thus their behaviour may differ. This should be documented in the
	 * specific implementation.
	 * 
	 * @param template specifies the facts to be removed 
	 * @return Set<E> - removed facts
     * @throws IllegalArgumentException if <code>template</code> is <code>null</code>
	 * @see org.sercho.masp.space.ObjectMatcher
	 */
	public abstract Set<IFact> removeAll(IFact template);
	
	/**
	 * <code>write</code> adds a fact to memory
	 * 
	 * @param fact new fact
     * @throws IllegalArgumentException if <code>fact</code> is <code>null</code>
	 */
	public abstract void write(IFact fact);

	/**
	 * <code>update</code> updates all facts from memory, which match
	 * <code>template</code> with the new value <code>substPattern</code>. The
	 * <code>template</code> will be matched to the facts according to general
	 * rules of tuple spaces. All fields of <code>template</code> will be
	 * compared with those of the facts. <code>null</code> is the wildcard. A
	 * tuple space must support subclassing, in other words objects subclassing
	 * <code>template</code> will also be updated. Fields of all matched facts
	 * will be set to values of corresponding non-null fields of <code>substPattern</code>.
	 * <p>
	 * However, implementing classes may use custom template-matching algorithms
	 * and thus their behaviour may differ. This should be documented in the
	 * specific implementation.
	 * <p>
	 * Note that, since <code>null</code> is used as a wildcard value, all
	 * <code>null</code> fields of <code>substPattern</code> will be ignored in the update.
	 * Thus it is not possible to set a fact's field to <code>null</code> with
	 * this method. In order to accomplish this a fact needs to be removed,
	 * changed and rewritten into memory. 
	 * 
	 * @param template specifies the facts to be updated
	 * @param substPattern a fact which is the pattern for the values to be updated
	 * @return boolean <code>true</code> if at least on fact has matched the
	 * 					 template, <code>false</code> otherwise
     * @throws IllegalArgumentException if <code>template</code> is <code>null</code>
	 * @see org.sercho.masp.space.ObjectMatcher
	 */
	public abstract boolean update(IFact template, IFact substPattern);
	
    /**
     * <code>readAllOfType</code> returns all facts of a given type from memory
     * (including facts of subtypes). This method might be useful if
     * reading facts specified by an interface (which obviously cannot be
     * instantiated to a template).
     *
     * @param type type of facts to read
     * @return Set<IFact> all facts of <code>type</code>, never 
     *                  <code>null</code>
     * @throws IllegalArgumentException if <code>type</code> is <code>null</code>
     */
    public abstract Set<IFact> readAllOfType(Class<IFact> type);

}