/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.util;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;

/**
 * This set implementation ensures that references are inserted only once. {@link Object#hashCode()} or
 * {@link Object#equals(Object)} are not used.
 * <p>
 * Manipulations of this set are not synchronized!
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class IdentityHashSet<E> extends AbstractSet<E> implements Cloneable, Serializable {
    private static final long serialVersionUID= -8782662619051723940L;

    private transient IdentityHashMap<E, Object> _map;

    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT= new Object();

    public IdentityHashSet() {
        _map= new IdentityHashMap<E, Object>();
    }

    public IdentityHashSet(Collection<? extends E> c) {
        _map= new IdentityHashMap<E, Object>(Math.max((int) (c.size() / .75f) + 1, 32));
        addAll(c);
    }

    @Override
    public boolean add(E e) {
        return _map.put(e, PRESENT) == null;
    }

    @Override
    public void clear() {
        _map.clear();
    }

    @Override
    public boolean contains(Object o) {
        return _map.containsKey(o);
    }

    @Override
    public boolean isEmpty() {
        return _map.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return _map.keySet().iterator();
    }

    @Override
    public boolean remove(Object o) {
        return _map.remove(o) == PRESENT;
    }

    @Override
    public int size() {
        return _map.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        try {
            IdentityHashSet<E> newSet= (IdentityHashSet<E>) super.clone();
            newSet._map= (IdentityHashMap<E, Object>) _map.clone();
            return newSet;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        // Write out any hidden serialization magic
        s.defaultWriteObject();

        // Write out size
        s.writeInt(_map.size());

        // Write out all elements in the proper order.
        for (Iterator<E> i= _map.keySet().iterator(); i.hasNext();)
            s.writeObject(i.next());
    }

    /**
     * Reconstitute the <tt>HashSet</tt> instance from a stream (that is, deserialize it).
     */
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();

        // Read in size
        int size= s.readInt();
        _map= new IdentityHashMap<E, Object>(size);

        // Read in all elements in the proper order.
        for (int i= 0; i < size; i++) {
            E e= (E) s.readObject();
            _map.put(e, PRESENT);
        }
    }
}
