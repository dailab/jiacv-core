package de.dailab.jiactng.agentcore.comm.message;

import java.io.Serializable;


/**
 * Der Content ist ein serialisierbares Object.
 * @author janko
 *
 */
public class ObjectContent implements IJiacContent {
	Serializable _object;

	public ObjectContent(Serializable object) {
		setObject(object);
	}
	
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
