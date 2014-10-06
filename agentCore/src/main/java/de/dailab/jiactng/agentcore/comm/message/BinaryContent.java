/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.message;

import java.util.Arrays;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * This content type can be used for non-serialisable 
 * payloads.
 * 
 * Currently it is used for the communication between JIAC-TNG and
 * microJIAC agents.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class BinaryContent implements IFact {
    private static final byte[] NO_DATA= new byte[0];
    
    private byte[] data;

    /**
     * Creates a binary content.
     * @param data the binary data
     */
    public BinaryContent(byte[] data) {
        this.data= data != null ? data : NO_DATA;
    }

    /**
     * Get the data of the binary content.
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
	 * Checks the equality of two binary contents. The contents are equal
	 * if their data are equal.
	 * @param obj the other binary content
	 * @return the result of the equality check
     */
    @Override
    public final boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        
        if (! (obj instanceof BinaryContent)) {
            return false;
        }
        
        final BinaryContent other= (BinaryContent) obj;
        return Arrays.equals(data, other.getData());
    }

    /**
	 * Returns the hash code by calculation from the hash code of this class and the data.
	 * Thus it is the same hash code for all messages with the same data.
	 * @return the calculated hash code
     */
    @Override
    public final int hashCode() {
        return BinaryContent.class.hashCode() ^ Arrays.hashCode(data);
    }
}
