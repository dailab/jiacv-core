package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.ontology.IActionDescription;


/**
 * This Interface is meant to be an extension to the IActionDescription Interface.
 * It's purpose is to make the usage of the WebserviceGateway for JIAC more flexible
 * and user-friendly.
 *
 *  On the other side, it also merges with the management of action descriptions within the
 *  WebserviceProviderBean.
 *
 * If there is already a service with <code>serviceName</code> this action will be added to
 * the service. 
 * 
 * If there is already an operation with <code>operationName</code> this action will
 * be published as operation with operationName#, where # is an increment
 * 
 * @author loeffelholz
 *
 */
public interface IWebserviceAction extends IActionDescription{

	
	/**
	 * Gets the Name of the Service this Action should be published in
	 * 
	 * @return the servicename
	 */
	public String getServiceName();
	
	/**
	 * Sets the Name of the Service this Action should be published in
	 * 
	 * @param the servicename
	 */
	public void setServiceName(String servicename);
	
	/**
	 * Gets the OperationName this Action should be associated with,
	 * when published within a webservice
	 * 
	 * @return the OperationName this Action is mapped to
	 */
	public String getOperationName();
	
	/**
	 * Sets the OperationName this Action should be associated with,
	 * when published within a webservice.
	 *  
	 * @param operationname this action should be mapped to (if possible)
	 */
	public void setOperationName(String operationname);
	
}
