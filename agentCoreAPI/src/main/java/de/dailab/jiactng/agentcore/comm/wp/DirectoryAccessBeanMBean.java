package de.dailab.jiactng.agentcore.comm.wp;

import java.util.Set;

import de.dailab.jiactng.agentcore.AbstractAgentBeanMBean;

/**
 * JMX-based management interface of the directory access bean.
 * @author Jan Keiser
 */
public interface DirectoryAccessBeanMBean extends AbstractAgentBeanMBean {

	/**
	 * Gets the name of actions offered by the agent.
	 * @return The set of action names.
	 */
	Set<String> getOfferedActionNames();

}
