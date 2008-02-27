package de.dailab.jiactng.agentcore.comm.message;

import java.io.Serializable;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.util.EqualityChecker;


/**
 * Der Content ist ein serialisierbares Object.
 * @author janko
 *
 */
@SuppressWarnings("serial")
public class ObjectContent implements IFact {
	private Serializable _object;
    private transient boolean _recursionDetected= false;

	public ObjectContent(Serializable object) {
		setObject(object);
	}
	
	public Serializable getObject() {
		return _object;
	}

	public void setObject(Serializable object) {
		_object = object;
	}
	
    @Override
    public synchronized boolean equals(Object obj) {
        if(_recursionDetected) {
            return true;
        }
        
        try {
            _recursionDetected= true;
            if(obj == this) {
                return true;
            }
            
            if(obj == null || !(obj instanceof ObjectContent)) {
                return false;
            }
            
            ObjectContent other= (ObjectContent) obj;
            return EqualityChecker.equals(_object, other._object);
        } finally {
            _recursionDetected= false;
        }
    }

    @Override
    public synchronized int hashCode() {
        if(_recursionDetected) {
            return 0;
        }

        try {
            _recursionDetected= true;
            return ObjectContent.class.hashCode() ^ (_object != null ? _object.hashCode() : 0);
        } finally {
            _recursionDetected= false;
        }
    }

    @Override
	public synchronized String toString() {
        if(_recursionDetected) {
            return "<recursion>";
        }
        
        try {
            _recursionDetected= true;
            return _object != null ? _object.toString() : "<empty>";
        } finally {
            _recursionDetected= false;
        }
	}
}
