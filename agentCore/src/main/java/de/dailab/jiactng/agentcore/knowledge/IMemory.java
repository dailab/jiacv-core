/*
 * Created on 20.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiangtng.agentcore.knowledge;

import java.util.Set;

public interface IMemory {

  /**
   * Writes a tuple to the Memory.
   * 
   * @see org.sercho.masp.space.SimpleObjectSpace
   * 
   * @param tp
   *          the tuple to store.
   */
  public abstract void out(Tuple tp);

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
  public abstract Tuple in(Tuple tp);

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
  public abstract Tuple read(Tuple tp);

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
  public abstract Tuple test(Tuple tp);

  public abstract Set<Tuple> readAll(Tuple tp);

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

}