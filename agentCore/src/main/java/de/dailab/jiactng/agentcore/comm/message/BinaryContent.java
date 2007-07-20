/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.message;

/**
 * This content type can be used for non-serialisable 
 * payloads.
 * 
 * Currently it is used for the communication between JIAC-TNG and
 * microJIAC agents.
 * 
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class BinaryContent implements IJiacContent {
    private byte[] _data;

    public BinaryContent(byte[] data) {
        _data= data;
    }
    
    public byte[] getData() {
        return _data;
    }
}
