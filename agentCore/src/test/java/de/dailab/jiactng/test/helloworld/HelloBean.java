/*
 * Created on 20.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.test.helloworld;

import de.dailab.jiactng.agentcore.AAgentBean;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

public class HelloBean extends AAgentBean {

  @Override
  public void execute() {
    System.err.println("Hello World");
    try {
      thisAgent.stop();
    } catch (LifecycleException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
