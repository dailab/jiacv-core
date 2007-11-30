/*
 * Created on 20.02.2007
 */
package de.dailab.jiactng.agentcore;


/**
 * The HelloWorldBean. Simply prints Hello World and quits the agent afterwards.
 * 
 * @author Thomas Konnerth
 */
public class DummyBean extends AbstractAgentBean implements IActiveAgentBean {

  public enum Modes {
    Hello, Failure, Endless, Nothing
  }

  private String test = "";

  private Modes  mode = Modes.Hello;

  /**
   * Exection of the HelloWorld Example. Pretty simple.
   * 
   * @see de.dailab.jiactng.agentcore.AbstractAgentBean#execute()
   */
  public void execute() {
    synchronized (this) {
      switch (mode) {
        case Hello:
          this.test = "Hello World";
          break;
        case Failure:
          this.test = "fail";
          throw new RuntimeException("DummyBean Exception");
        case Endless:
          while (true) {
            this.test = "endless";
          }
        case Nothing:
          break;
        default:
          this.test = "Hello World";
      }
    }

  }

  public String getTest() {
    return test;
  }

  public void setTest(String test) {
    this.test = test;
  }

  public void setMode(Modes newMode) {
    synchronized (this) {
      this.mode = newMode;
    }
  }

  public Modes getMode() {
    return this.mode;
  }

}
