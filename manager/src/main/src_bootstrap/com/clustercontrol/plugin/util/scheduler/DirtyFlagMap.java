package com.clustercontrol.plugin.util.scheduler;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DirtyFlagMap<K,V> implements Map<K,V>, Cloneable, java.io.Serializable {

	/*
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * Data members.
	 *
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */
	private static final long serialVersionUID = 1433884852607126222L;

	private boolean dirty = false;
	private Map<K,V> map;

	/*
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * Constructors.

	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */

	/**
	 * @see java.util.HashMap
	 */
	public DirtyFlagMap() {
		map = new HashMap<K,V>();
	}

	/**
	 * @see java.util.HashMap
	 */
	public DirtyFlagMap(final int initialCapacity) {
		map = new HashMap<K,V>(initialCapacity);
	}

	/**
	 * @see java.util.HashMap
	 */
	public DirtyFlagMap(final int initialCapacity, final float loadFactor) {
		map = new HashMap<K,V>(initialCapacity, loadFactor);
	}

	/*
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * Interface.
	 *
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */

	public void clearDirtyFlag() {
		dirty = false;
	}

	public boolean isDirty() {
		return dirty;
	}

	public Map<K,V> getWrappedMap() {
		return map;
	}

	public void clear() {
		if (!map.isEmpty()) {
			dirty = true;
		}
		map.clear();
	}

	public boolean containsKey(final Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(final Object val) {
		return map.containsValue(val);
	}

	public Set<Entry<K,V>> entrySet() {
		return new DirtyFlagMapEntrySet(map.entrySet());
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof DirtyFlagMap)) {
			return false;
		}

		return map.equals(((DirtyFlagMap<?,?>) obj).getWrappedMap());
	}

	@Override
	public int hashCode()
	{
		return map.hashCode();
	}

	public V get(final Object key) {
		return map.get(key);
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public Set<K> keySet() {
		return new DirtyFlagSet<K>(map.keySet());
	}

	public V put(final K key, final V val) {
		dirty = true;

		return map.put(key, val);
	}

	public void putAll(final Map<? extends K, ? extends V> t) {
		if (!t.isEmpty()) {
			dirty = true;
		}

		map.putAll(t);
	}

	public V remove(final Object key) {
		V obj = map.remove(key);

		if (obj != null) {
			dirty = true;
		}

		return obj;
	}

	public int size() {
		return map.size();
	}

	public Collection<V> values() {
		return new DirtyFlagCollection<V>(map.values());
	}

	@Override
	@SuppressWarnings("unchecked") // suppress warnings on generic cast of super.clone() and map.clone() lines.
	public Object clone() {
		DirtyFlagMap<K,V> copy;
		try {
			copy = (DirtyFlagMap<K,V>) super.clone();
			if (map instanceof HashMap) {
				copy.map = (Map<K,V>)((HashMap<K,V>)map).clone();
			}
		} catch (CloneNotSupportedException ex) {
			throw new IncompatibleClassChangeError("Not Cloneable.");
		}

		return copy;
	}

	private class DirtyFlagCollection<T> implements Collection<T> {
		private Collection<T> collection;

		public DirtyFlagCollection(final Collection<T> c) {
			collection = c;
		}

		protected Collection<T> getWrappedCollection() {
			return collection;
		}

		public Iterator<T> iterator() {
			return new DirtyFlagIterator<T>(collection.iterator());
		}

		public boolean remove(final Object o) {
			boolean removed = collection.remove(o);
			if (removed) {
				dirty = true;
			}
			return removed;
		}

		public boolean removeAll(final Collection<?> c) {
			boolean changed = collection.removeAll(c);
			if (changed) {
				dirty = true;
			}
			return changed;
		}

		public boolean retainAll(final Collection<?> c) {
			boolean changed = collection.retainAll(c);
			if (changed) {
				dirty = true;
			}
			return changed;
		}

		public void clear() {
			if (collection.isEmpty() == false) {
				dirty = true;
			}
			collection.clear();
		}

		// Pure wrapper methods
		public int size() { return collection.size(); }
		public boolean isEmpty() { return collection.isEmpty(); }
		public boolean contains(final Object o) { return collection.contains(o); }
		public boolean add(final T o) { return collection.add(o); } // Not supported
		public boolean addAll(final Collection<? extends T> c) { return collection.addAll(c); } // Not supported
		public boolean containsAll(final Collection<?> c) { return collection.containsAll(c); }
		public Object[] toArray() { return collection.toArray(); }
		public <U> U[] toArray(final U[] array) { return collection.toArray(array); }
	}

	private class DirtyFlagSet<T> extends DirtyFlagCollection<T> implements Set<T> {
		public DirtyFlagSet(final Set<T> set) {
			super(set);
		}

		protected Set<T> getWrappedSet() {
			return (Set<T>)getWrappedCollection();
		}
	}

	private class DirtyFlagIterator<T> implements Iterator<T> {
		private Iterator<T> iterator;

		public DirtyFlagIterator(final Iterator<T> iterator) {
			this.iterator = iterator;
		}

		public void remove() {
			dirty = true;
			iterator.remove();
		}

		// Pure wrapper methods
		public boolean hasNext() { return iterator.hasNext(); }
		public T next() { return iterator.next(); }
	}

	private class DirtyFlagMapEntrySet extends DirtyFlagSet<Map.Entry<K,V>> {

		public DirtyFlagMapEntrySet(final Set<Map.Entry<K,V>> set) {
			super(set);
		}

		@Override
		public Iterator<Map.Entry<K,V>> iterator() {
			return new DirtyFlagMapEntryIterator(getWrappedSet().iterator());
		}

		@Override
		public Object[] toArray() {
			return toArray(new Object[super.size()]);
		}

		@SuppressWarnings("unchecked") // suppress warnings on both U[] and U casting.
		@Override
		public <U> U[] toArray(final U[] array) {
			if (array.getClass().getComponentType().isAssignableFrom(Map.Entry.class) == false) {
				throw new IllegalArgumentException("Array must be of type assignable from Map.Entry");
			}

			int size = super.size();

			U[] result =
				array.length < size ?
					(U[])Array.newInstance(array.getClass().getComponentType(), size) : array;

			Iterator<Map.Entry<K,V>> entryIter = iterator(); // Will return DirtyFlagMapEntry objects
			for (int i = 0; i < size; i++) {
				result[i] = ( U ) entryIter.next();
			}

			if (result.length > size) {
				result[size] = null;
			}

			return result;
		}
	}

	private class DirtyFlagMapEntryIterator extends DirtyFlagIterator<Map.Entry<K,V>> {
		public DirtyFlagMapEntryIterator(final Iterator<Map.Entry<K,V>> iterator) {
			super(iterator);
		}

		@Override
		public DirtyFlagMapEntry next() {
			return new DirtyFlagMapEntry(super.next());
		}
	}

	private class DirtyFlagMapEntry implements Map.Entry<K,V> {
		private Map.Entry<K,V> entry;

		public DirtyFlagMapEntry(final Map.Entry<K,V> entry) {
			this.entry = entry;
		}

		public V setValue(final V o) {
			dirty = true;
			return entry.setValue(o);
		}

		// Pure wrapper methods
		public K getKey() { return entry.getKey(); }
		public V getValue() { return entry.getValue(); }
		public boolean equals(Object o) { return entry.equals(o); }
		public int hashCode() {
			assert false : "hashCodeが呼び出されることは想定されていません";
			return 31;
		}
	}
}

