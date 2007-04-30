/*
 * Created on 27.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.environment;

import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * @author moekon
 *
 */
public interface ISensor extends IAgentBean {

	/**
	 * @return
	 */
	public IAdaptor getEnvironment();

	/**
	 * @return
	 */
	public IFact readSensor();

	/**
	 * @return
	 */
	public boolean isActive();

}
