/*
 * Created on 27.02.2007
 */
package de.dailab.jiactng.agentcore.environment;

import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * Class is left from design, but has no meaning any longer.
 * 
 * @author moekon
 */
public interface ISensor extends IAgentBean {

	/**
	 * Getter for the environment of this Sensor
	 * 
	 * @return the environment with which this Sensor is associated.
	 */
	IAdaptor getEnvironment();

	/**
	 * Reads the current data from this sensor
	 * 
	 * @return a fact describing the sensors current data.
	 */
	IFact readSensor();

	/**
	 * Getter for the type of the Sensor.
	 * 
	 * @return true, if this sensor actively creates data, false if the sensor
	 *         has to be polled.
	 */
	boolean isActive();

}
