/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;


public class Tuple implements Iterable<Object> {
	private Object[] values;

	public Tuple(Object...values) {
		this.values = values;
	}

	@SuppressWarnings("unchecked")
	public <X> X get(int i) {
		return (X)values[i];
	}

	public int count() {
		return values.length;
	}

	@SuppressWarnings("unchecked")
	public <X> X get(int i, Class<X> type) {
		return (X)get(i);
	}

	public Object[] toArray() {
		return values;
	}

	public static Tuple build(Object...values) {
		return new Tuple(values);
	}

	@Override
	public Iterator<Object> iterator() {
		return new Iterator<Object>() {
			private int pos = 0;
			@Override
			public boolean hasNext() {
				return pos < Tuple.this.values.length;
			}
			@Override
			public Object next() {
				if (Tuple.this.values.length <= pos) 
					throw new NoSuchElementException();

				return Tuple.this.values[pos];
			}
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(values);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tuple other = (Tuple) obj;
		if (!Arrays.equals(values, other.values))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Tuple [values=" + Arrays.toString(values) + "]";
	}
}
