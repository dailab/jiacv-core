/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public final class Selector implements IFact {
    private final String _key;
    private final String _value;
    
    private String _selectorString;
    
    public Selector(String key, String value) {
        _key= key;
        _value= value;
    }
    
    public String getKey() {
        return _key;
    }

    public String getValue() {
        return _value;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof Selector)) {
            return false;
        }
        
        return toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        if(_selectorString == null) {
            _selectorString= _key + '=' + _value;
        }
        
        return _selectorString;
    }
}
