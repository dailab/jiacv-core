/*
 * Created on 20.02.2007
 */
package de.dailab.jiactng.examples.helloWorld;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

/**
 * The HelloWorldBean. Simply prints Hello World and quits the agent afterwards.
 * 
 * @author Thomas Konnerth
 */
public class HelloBean extends AbstractAgentBean {

  /**
   * Exection of the HelloWorld Example. Pretty simple.
   * 
   * @see de.dailab.jiactng.agentcore.AbstractAgentBean#execute()
   */
  public void execute() {
    // print Hello world
    System.out.println("Hello World from "+getBeanName());
    
//    try {
//      // stop Agent
//      thisAgent.getAgentNode().stop();
//      thisAgent.getAgentNode().cleanup();
//    } catch (LifecycleException e) {
//      e.printStackTrace();
//    }

  }

}
