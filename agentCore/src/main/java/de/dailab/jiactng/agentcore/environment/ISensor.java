/*
 * Created on 27.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.environment;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public interface ISensor {

  public IEnvironment getEnvironment();

  public IFact readSensor();

  public boolean isActive();

}
