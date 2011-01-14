package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.ontology.IActionDescription;

public class WebserviceAction extends Action implements IWebserviceAction {

	private String serviceName = null;
	private String operationName = null;
	
	
	public WebserviceAction(Action action){
		super(action);
		this.setProviderDescription(action.getProviderDescription());

		if (action instanceof IWebserviceAction){
			IWebserviceAction wsAction = (IWebserviceAction) action;
			
			if (wsAction.getOperationName() != null)
				operationName = wsAction.getOperationName();
			if (wsAction.getServiceName() != null)
				serviceName = wsAction.getServiceName();
		}
	}
	
	public WebserviceAction(IActionDescription ad){
		this.setProviderDescription(ad.getProviderDescription());
		this.setActionType(ad.getActionType());
		this.setInputTypeNames(ad.getInputTypeNames());
		this.setResultTypeNames(ad.getResultTypeNames());		
		this.setName(ad.getName());
		this.setScope(ad.getScope());
		
		try {
			this.setInputTypes(ad.getInputTypes());
			this.setResultTypes(ad.getResultTypes());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
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
	
	private boolean isValid(String key){
		return !(key==null || key.isEmpty());
	}


	
}
