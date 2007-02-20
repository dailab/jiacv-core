/*
 * Created on 20.02.2007
 */
package de.dailab.jiactng.test.helloworld;

import de.dailab.jiactng.agentcore.AAgentBean;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

/**
 * The HelloWorldBean. Simply prints Hello World and quits the agent afterwards.
 * 
 * @author Thomas Konnerth
 */
public class HelloBean extends AAgentBean {

  /**
   * Exection of the HelloWorld Example. Pretty simple.
   * 
   * @see de.dailab.jiactng.agentcore.AAgentBean#execute()
   */
  public void execute() {
    // print Hello world
    System.out.println("Hello World");

    try {
      // stop Agent
      thisAgent.stop();
    } catch (LifecycleException e) {
      e.printStackTrace();
    }

  }

}
