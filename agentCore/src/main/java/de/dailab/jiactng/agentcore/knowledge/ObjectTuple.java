/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.knowledge;

/**
 * Simple Tuple class that is used for storage of simple information in the
 * memory. This class can only hold tuples with two arguments.
 * 
 * @author Thomas Konnerth
 */
public class ObjectTuple implements IFact {

  /**
   * First argument of the tuple.
   */
  private Object arg1;

  /**
   * Second argument of the tuple.
   */
  private Object arg2;

  /**
   * Creates a new Tuple from two Strings. Both may be null.
   * 
   * @param arg1
   *          the first argument.
   * @param arg2
   *          the second argument.
   */
  public ObjectTuple(Object arg1, Object arg2) {
    this.arg1 = arg1;
    this.arg2 = arg2;
  }

  /**
   * Getter for the first argument.
   * 
   * @return the value (may be null).
   */
  public Object getArg1() {
    return arg1;
  }

  /**
   * Setter for the first argument.
   * 
   * @param arg1
   *          the new value (may be null).
   */
  public void setArg1(Object arg1) {
    this.arg1 = arg1;
  }

  /**
   * Getter for the second argument.
   * 
   * @return the value (may be null).
   */
  public Object getArg2() {
    return arg2;
  }

  /**
   * Setter for the second argument.
   * 
   * @param arg2
   *          the new value (may be null).
   */
  public void setArg2(Object arg2) {
    this.arg2 = arg2;
  }

  /**
   * Creates a string representation of the tuple.
   * 
   * @return a string representation of this tuple of the form (arg1 : arg2).
   */
  public String toString() {
    return new StringBuffer("(").append(arg1).append(" : ").append(arg2)
        .append(")").toString();
  }
}
