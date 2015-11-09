/**
 * 
 */
package de.dailab.jiactng.agentcore.roletest;

import java.util.ArrayList;
import java.util.List;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * @author moekon
 * 
 */
public class NoRoleBean extends AbstractAgentBean implements IEffector {

  public final static String ACTION_DUMMY_A = "ACTION_NAME_DUMMY_A";

  private Action             ACTION_A;

  @Override
  public void execute() {
    setExecutionInterval(0);
    StringBuffer sb = new StringBuffer();
//    sb.append("Agent: ").append(thisAgent.getAgentName());
//    sb.append("\nRoles: ").append(thisAgent.getRoles());
//    sb.append("\nBeans: ").append(thisAgent.getAgentBeans());
//    sb.append("\nActions: ").append(thisAgent.getActionList());
//    sb.append("\n");
    sb.append("Agent: ").append(thisAgent.getAgentName());
    sb.append("; Roles: ").append(thisAgent.getRoles().size());
    sb.append("; Beans: ").append(thisAgent.getAgentBeans().size());
    sb.append("; Actions: ").append(thisAgent.getActionList().size());
    log.info(sb.toString());
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.environment.IEffector#getActions()
   */
  @Override
  public List<? extends IActionDescription> getActions() {
    ArrayList<IActionDescription> ret = new ArrayList<IActionDescription>();
    ACTION_A = new Action(ACTION_DUMMY_A, this, new Class[] {}, new Class[] {});
    ret.add(ACTION_A);
    return ret;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.environment.IEffector#doAction(de.dailab.jiactng.agentcore.action.DoAction)
   */
  @Override
  public void doAction(DoAction doAction) throws Exception {
    if (ACTION_DUMMY_A.equals(doAction.getAction().getName())) {
      log.warn(ACTION_DUMMY_A + " called.");
    } else {
      log.error("Unknown action called: " + doAction);
    }
  }

}
