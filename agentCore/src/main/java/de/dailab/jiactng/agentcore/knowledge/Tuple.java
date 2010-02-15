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
 * 
 * 
 * TODO: check whether we need this or a more generic tuple class
 */
@SuppressWarnings("serial")
public class Tuple implements IFact {

  /**
   * First argument of the tuple.
   */
  private String arg1;

  /**
   * Second argument of the tuple.
   */
  private String arg2;

  /**
   * Creates a new Tuple from two Strings. Both may be null.
   * 
   * @param str1
   *          the first argument.
   * @param str2
   *          the second argument.
   */
  public Tuple(String str1, String str2) {
    arg1 = str1;
    arg2 = str2;
  }

  /**
   * Getter for the first argument.
   * 
   * @return the value (may be null).
   */
  public String getArg1() {
    return arg1;
  }

  /**
   * Setter for the first argument.
   * 
   * @param str1
   *          the new value (may be null).
   */
  public void setArg1(String str1) {
    arg1 = str1;
  }

  /**
   * Getter for the second argument.
   * 
   * @return the value (may be null).
   */
  public String getArg2() {
    return arg2;
  }

  /**
   * Setter for the second argument.
   * 
   * @param str2
   *          the new value (may be null).
   */
  public void setArg2(String str2) {
    arg2 = str2;
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
