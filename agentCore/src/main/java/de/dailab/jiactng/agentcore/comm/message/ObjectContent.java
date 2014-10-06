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
	private Serializable object;
    private transient boolean recursionDetected= false;

    /**
     * Creates an object content with a serialisable object.
     * @param object the serialisable object
     */
	public ObjectContent(Serializable object) {
		setObject(object);
	}

	/**
	 * Get the serialisable object of this object content.
	 * @return the object
	 */
	public Serializable getObject() {
		return object;
	}

	/**
	 * Set the serialisable object of this object content.
	 * @param newObject the object
	 */
	public void setObject(Serializable newObject) {
		object = newObject;
	}

	/**
	 * Checks the equality of two object contents. The contents are equal
	 * if their objects are equal or if a recursion was detected within
	 * this content.
	 * @param obj the other object content
	 * @return the result of the equality check
	 * @see EqualityChecker#equals(Object, Object)
	 */
    @Override
    public boolean equals(Object obj) {
        if(recursionDetected) {
            return true;
        }
        
        try {
            recursionDetected= true;
            if(obj == this) {
                return true;
            }
            
            if (! (obj instanceof ObjectContent)) {
                return false;
            }
            
            final ObjectContent other= (ObjectContent) obj;
            return EqualityChecker.equals(object, other.object);
        } finally {
            recursionDetected= false;
        }
    }

    /**
	 * Returns the hash code by calculation from the hash code of this class and the object.
	 * Thus it is the same hash code for all messages with the same object. It returns 0
	 * if a recursion was detected within this content. It returns 1 if the object is null.
	 * @return the calculated hash code
     */
    @Override
    public int hashCode() {
        if(recursionDetected) {
            return 0;
        }

        try {
            recursionDetected= true;
            return ObjectContent.class.hashCode() ^ (object != null ? object.hashCode() : 0);
        } finally {
            recursionDetected= false;
        }
    }

    /**
     * Returns the string representation of the object. It returns "&lt;empty&gt;"
     * if the object is <code>null</code>. It returns "&lt;recursion&gt;" if a recursion 
     * was detected within this content.
     * @return a string representation of the object content
     */
    @Override
	public synchronized String toString() {
        if(recursionDetected) {
            return "<recursion>";
        }
        
        try {
            recursionDetected= true;
            return object != null ? object.toString() : "<empty>";
        } finally {
            recursionDetected= false;
        }
	}
}
