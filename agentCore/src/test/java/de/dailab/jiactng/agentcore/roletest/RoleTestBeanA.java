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
public class RoleTestBeanA extends AbstractAgentBean implements IEffector {

  public final static String ACTION_DUMMY_NOROLE = "ACTION_NAME_DUMMY_NOROLE";

  private Action             ACTION_NOROLE;

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.environment.IEffector#getActions()
   */
  @Override
  public List<? extends IActionDescription> getActions() {
    ArrayList<IActionDescription> ret = new ArrayList<IActionDescription>();
    ACTION_NOROLE = new Action(ACTION_DUMMY_NOROLE, this, new Class[] {}, new Class[] {});
    ret.add(ACTION_NOROLE);
    return ret;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.environment.IEffector#doAction(de.dailab.jiactng.agentcore.action.DoAction)
   */
  @Override
  public void doAction(DoAction doAction) throws Exception {
    if (ACTION_DUMMY_NOROLE.equals(doAction.getAction().getName())) {
      log.warn(ACTION_DUMMY_NOROLE + " called.");
    } else {
      log.error("Unknown action called: " + doAction);
    }
  }
}
