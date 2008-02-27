package de.dailab.jiactng.agentcore;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;

/**
 * Interface to get a description of the object based on JMX open types.
 * @author Jan Keiser
 */
public interface JmxDescriptionSupport {

	/**
	 * Gets the type of the description of this object.
	 * @return the type definition of the description
	 * @throws OpenDataException if an error occurs during construction of the type definition
	 */
	public OpenType<?> getDescriptionType() throws OpenDataException;

	/**
	 * Gets a description of this object that is compliant to the type 
	 * specified by <code>getDescriptionType()</code>.
	 * @return the description of this object
	 * @throws OpenDataException if an error occurs during construction of the description
	 */
	public Object getDescription() throws OpenDataException;
	
}
