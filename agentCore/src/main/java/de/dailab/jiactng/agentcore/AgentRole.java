package de.dailab.jiactng.agentcore;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanNameAware;

import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * This class provides basic implementation of the agentrole interface.
 * TODO check transient attributes or just class names instead of interfaces?
 * TODO clarify duplicates
 * 
 * @author axle
 */
public class AgentRole implements IAgentRole, BeanNameAware {
	private static final long serialVersionUID = -4262133709570599762L;
	
	private String name = null;

	private List<IAgentBean> agentBeans = new ArrayList<IAgentBean>();
	
	private List<IAgentRole> includedAgentRoles = new ArrayList<IAgentRole>();
	
	private List<String> scripts = new ArrayList<String>();
	
	private IEffector interpreter = null;
	
	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<IActionDescription> getActions() {
		List<IActionDescription> actions = new ArrayList<IActionDescription>();
		for (IAgentBean agentBean: agentBeans) {
			if (agentBean instanceof IEffector) {
				actions.addAll(((IEffector)agentBean).getActions());
			}
		}
		if (interpreter != null) {
			actions.addAll(interpreter.getActions());
		}
		return actions;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IAgentBean> getAgentBeans() {
		return agentBeans;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IAgentRole> getIncludedAgentRoles() {
		return includedAgentRoles;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getScripts() {
		return scripts;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAgentBeans(List<IAgentBean> agentbeans) {
		this.agentBeans.clear();
		this.agentBeans.addAll(agentbeans);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setIncludedAgentRoles(List<IAgentRole> includedAgentRoles) {
		this.includedAgentRoles.clear();
		this.includedAgentRoles.addAll(includedAgentRoles);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInterpreter(IEffector interpreter) {
		this.interpreter = interpreter;
	}

	@Override
	public void setScripts(List<String> scripts) {
		this.scripts.clear();
		this.scripts.addAll(scripts);
	}

  @Override
  public String toString() {
    return "AgentRole [name=" + this.name + ", agentBeans=" + this.agentBeans + ", includedAgentRoles="
        + this.includedAgentRoles + "]";
  }

  /* (non-Javadoc)
   * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
   */
  @Override
  public void setBeanName(String name) {
    this.name = name;
  }
	
	
}
