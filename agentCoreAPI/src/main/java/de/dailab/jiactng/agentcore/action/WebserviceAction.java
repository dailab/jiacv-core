package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.ontology.IActionDescription;

public class WebserviceAction extends Action implements IWebserviceAction {

	private static final long serialVersionUID = 9027891722413235875L;

	private String serviceName = null;
	private String operationName = null;
	
	
	public WebserviceAction(IActionDescription action){
		super(action);
		if (action instanceof IWebserviceAction){
			IWebserviceAction wsAction = (IWebserviceAction) action;
			operationName = wsAction.getOperationName();
			serviceName = wsAction.getServiceName();
		}
	}
	
	@Override
	public String getOperationName() {
		return operationName;
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}

	@Override
	public void setOperationName(String operationname) {
		operationName = operationname;
	}

	@Override
	public void setServiceName(String servicename) {
		serviceName = servicename;
	}
	
}
