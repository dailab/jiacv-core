/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.knowledge;

public class Tuple {

  private String arg1;

  private String arg2;

  public Tuple(String arg1, String arg2) {
    this.arg1 = arg1;
    this.arg2 = arg2;
  }

  public String getArg1() {
    return arg1;
  }

  public void setArg1(String arg1) {
    this.arg1 = arg1;
  }

  public String getArg2() {
    return arg2;
  }

  public void setArg2(String arg2) {
    this.arg2 = arg2;
  }

  public String toString() {
    return new StringBuffer("(").append(arg1).append(":").append(arg2).append(
        ")").toString();
  }
}
