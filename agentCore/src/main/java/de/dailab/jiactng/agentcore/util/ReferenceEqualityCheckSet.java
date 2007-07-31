/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.util;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class ReferenceEqualityCheckSet<T> implements Iterable<T> {
    private ArrayList<T> _content= new ArrayList<T>();
    
    public synchronized boolean add(T object) {
        for(T current : _content) {
            if(current == object) {
                return false;
            }
        }
        
        return _content.add(object);
    }
    
    public synchronized boolean remote(T object) {
        int index= -1;
        for(int i= 0; i < _content.size(); ++i) {
            if(_content.get(i) == object) {
                _content.remove(index);
                return true;
            }
        }
        
        return false;
    }
    
    public synchronized int size() {
        return _content.size();
    }
    
    public Iterator<T> iterator() {
        return _content.iterator();
    }
}
