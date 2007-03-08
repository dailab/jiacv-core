/*
 * Created on 20.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.knowledge;

import java.util.Set;

import org.sercho.masp.space.TupleSpace;

import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;

public interface IMemory extends ILifecycle {

  /**
   * Writes a tuple to the Memory.
   * 
   * @see org.sercho.masp.space.SimpleObjectSpace
   * 
   * @param tp
   *          the tuple to store.
   */
  public abstract void out(IFact tp);

  /**
   * Tries to read a matching tuple from the memory. This is a blocking
   * operation, which returns either when a matching Tuple is found, or after a
   * generic timeout, defined by the timeOut property. The Tuple in the Memory
   * is consumed when read.
   * 
   * @param tp
   *          The template-tuple for the matcher.
   * @return a Tuple that matches tp, or null if no such tuple could be found
   *         after the timeout expired.
   */
  public abstract IFact in(IFact tp);

  /**
   * Tries to read a matching tuple from the memory. This is a blocking
   * operation, which returns either when a matching Tuple is found, or after a
   * generic timeout, defined by the timeOut property. The Tuple in the Memory
   * is not consumed when read.
   * 
   * @param tp
   *          The template-tuple for the matcher.
   * @return a Tuple that matches tp, or null if no such tuple could be found
   *         after the timeout expired.
   */
  public abstract IFact read(IFact tp);

  /**
   * Tries to read a matching tuple from the memory. This is a non-blocking
   * operation, which imediately returns. Either a matching Tuple is returned,
   * or null if no matching tuple is found. The Tuple in the Memory is not
   * consumed when read.
   * 
   * @param tp
   *          The template-tuple for the matcher.
   * @return a Tuple that matches tp, or null if no such tuple could be found.
   */
  public abstract IFact test(IFact tp);

  public abstract Set<IFact> readAll(IFact tp);

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
  
  public abstract void setTupleSpace(TupleSpace<IFact> space);
}