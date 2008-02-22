/**
 * 
 */
package de.dailab.jiactng.agentcore.comm.util;

import java.io.Serializable;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * @author schenk
 *
 * @deprecated This class should be removed as specific webservice handling is not part of agentCore.
 */
@Deprecated
public interface IServiceInvocationArgument extends IFact {

	/**
	 * Returns the wsdl description of the service to invoke.
	 * @return a wsdl service description of the service to invoke.
	 */
	
	public String getWsdl();
	
	
	/**
	 * Setter for the wsdl attribute. 
	 * @param wsdl the wsdl description of the service.
	 */
	
	public void setWsdl(String wsdl);
	
	/**
	 * Returns the servicename of the service to invoke.
	 * Note: may be used only in the direction jiac->webservice
	 * @return a service name
	 */
	
	public String getServiceName();
	
	/**
	 * Sets the service name of the service to invoke.
	 * @param serviceName the name of the service.
	 */
	
	public void setServiceName(String serviceName);
	
	
	/**
	 * Returns the arguments of the service.
	 * @return the arguments of the service to invoke.
	 */
	
	public Serializable[] getArguments();
	
	/**
	 * Setter for the service arguments.
	 * @param arguments
	 */
	
	public void setArguments(Serializable[] arguments);
	
	/**
	 * Returns the operation name of the service that is invoked.
	 * @return an operation name.
	 */
	
	public String getOperation();
	
	/**
	 * Sets the operation name of the service.
	 * @param operation the operation name.
	 */
	
	public void setOperation(String operation);
	

}
