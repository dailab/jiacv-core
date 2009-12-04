/*
 * Created on 27.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.environment;

/**
 * This interface is a simple aggregation of the Sensor and Effector interface.
 * It's simply there for conveniance, if you want to implement both interfaces.
 * 
 * @see de.dailab.jiactng.agentcore.environment.ISensor
 * @see de.dailab.jiactng.agentcore.environment.IEffector
 * @author moekon
 * 
 */
public interface IAdaptor extends ISensor, IEffector {

}
