package de.dailab.jiactng.agentcore.comm;

import java.io.Serializable;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class ObjectContent implements IJiacContent {
	Serializable _object;

	public Serializable getObject() {
		return _object;
	}

	public void setObject(Serializable object) {
		_object = object;
	}
	
	public String toString() {
		return _object.toString();
	}
	
}
