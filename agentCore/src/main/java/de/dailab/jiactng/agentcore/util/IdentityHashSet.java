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
 * @param <E> the element type of this set
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class IdentityHashSet<E> extends AbstractSet<E> implements Cloneable, Serializable {
    private static final long serialVersionUID= -8782662619051723940L;

    private transient IdentityHashMap<E, Object> map;

    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT= new Object();

    public IdentityHashSet() {
        map= new IdentityHashMap<E, Object>();
    }

    public IdentityHashSet(Collection<? extends E> c) {
        map= new IdentityHashMap<E, Object>(Math.max((int) (c.size() / .75f) + 1, 32));
        addAll(c);
    }

    @Override
    public boolean add(E e) {
        return map.put(e, PRESENT) == null;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) == PRESENT;
    }

    @Override
    public int size() {
        return map.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        try {
            final IdentityHashSet<E> newSet= (IdentityHashSet<E>) super.clone();
            newSet.map= (IdentityHashMap<E, Object>) map.clone();
            return newSet;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        // Write out any hidden serialization magic
        s.defaultWriteObject();

        // Write out size
        s.writeInt(map.size());

        // Write out all elements in the proper order.
        for (final Iterator<E> i= map.keySet().iterator(); i.hasNext();) {
            s.writeObject(i.next());
        }
    }

    /**
     * Reconstitute the <tt>HashSet</tt> instance from a stream (that is, deserialize it).
     */
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();

        // Read in size
        final int size= s.readInt();
        map= new IdentityHashMap<E, Object>(size);

        // Read in all elements in the proper order.
        for (int i= 0; i < size; i++) {
            final E e= (E) s.readObject();
            map.put(e, PRESENT);
        }
    }
}
