package de.dailab.jiactng.agentcore.comm.description;

import de.dailab.jiactng.agentcore.comm.IEndPoint;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * Klasse zum Beschreiben eines Agenten. Sie enth�lt also META-Infos �ber den Agenten.
 * Es ist nicht der Agent selbst.
 * @author janko
 *
 */
public class AgentDescription implements IFact {

	String _name;
	IEndPoint _endpoint;

	// damit spring auch instanziieren kann
	public AgentDescription() {}
	
	public AgentDescription(String name, IEndPoint endpoint) {
		setName(name);
		setEndpoint(endpoint);
	}
	
	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public IEndPoint getEndpoint() {
		return _endpoint;
	}

	public void setEndpoint(IEndPoint endpoint) {
		_endpoint = endpoint;
	}	
	
}
