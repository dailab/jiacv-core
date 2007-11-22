/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.util;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This set implementation ensures that references are inserted only once.
 * {@link Object#hashCode()} or {@link Object#equals(Object)} are not used.
 * <p>
 * Manipulations of this set are not synchronized!
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ReferenceEqualityCheckSet<E> extends AbstractSet<E>  {
    private List<E> _content= new ArrayList<E>();
    
    @Override
    public boolean add(E object) {
        for(E current : _content) {
            if(current == object) {
                return false;
            }
        }
        
        return _content.add(object);
    }
    
    @Override
    public void clear() {
        _content.clear();
    }

    @Override
    public boolean remove(Object o) {
        for(int i= 0; i < _content.size(); ++i) {
            if(_content.get(i) == o) {
                _content.remove(i);
                return true;
            }
        }
        
        return false;
    }
    
    public int size() {
        return _content.size();
    }
    
    public Iterator<E> iterator() {
        return _content.iterator();
    }

    @Override
    public boolean contains(Object o) {
        for(int i= 0; i < _content.size(); ++i) {
            if(_content.get(i) == o) {
                return true;
            }
        }
        
        return false;
    }
}
