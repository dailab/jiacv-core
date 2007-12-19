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
public class BinaryContent implements IFact {
    private static final byte[] NO_DATA= new byte[0];
    
    private byte[] _data;

    public BinaryContent(byte[] data) {
        _data= data != null ? data : NO_DATA;
    }
    
    public byte[] getData() {
        return _data;
    }

    @Override
    public final boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        
        if(obj == null || !(obj instanceof BinaryContent)) {
            return false;
        }
        
        BinaryContent other= (BinaryContent) obj;
        byte[] otherData= other.getData();
        
        if(otherData.length != _data.length) {
            return false;
        }
        
        for(int i= 0; i < _data.length; ++i) {
            if(_data[i] != otherData[i]) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public final int hashCode() {
        return Arrays.hashCode(_data);
    }
}
