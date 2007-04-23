package de.dailab.jiactng.agentcore.comm.description;

import de.dailab.jiactng.agentcore.comm.IEndPoint;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * Klasse zum Beschreiben eines Agenten. Sie enthält also META-Infos über den Agenten.
 * Es ist nicht der Agent selbst.
 * @author janko
 *
 */
public class AgentDescription implements IFact {

	String _name;
	IEndPoint _endpoint;

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
