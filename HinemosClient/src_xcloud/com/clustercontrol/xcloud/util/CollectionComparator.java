/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CollectionComparator<O1, O2> {
	public static class Matched<O1, O2> {
		private O1 o1;
		private O2 o2;
		
		public O1 getO1() {
			return o1;
		}
		
		public O2 getO2() {
			return o2;
		}
	}
	
	public static class Result<O1, O2> {
		private List<Matched<O1, O2>> matched = new ArrayList<>();
		private List<O1> o1s = new ArrayList<>();
		private List<O2> o2s = new ArrayList<>();
		
		public List<Matched<O1, O2>> getMatched() {
			return matched;
		}

		public List<O1> getO1s() {
			return o1s;
		}
		
		public List<O2> getO2s() {
			return o2s;
		}
	}
	
	
	public static class Comparator<O1, O2> {
		public boolean match(O1 o1, O2 o2) {return false;}
		public void matched(O1 o1, O2 o2) {}
		public void afterO1(O1 o1) {}
		public void afterO2(O2 o2) {}
	}
	
	private Comparator<O1, O2> comparator;
	
	private CollectionComparator(Comparator<O1, O2> comparator) {
		this.comparator = comparator;
	}
	
	public Result<O1, O2> compare(Collection<O1> o1s, Collection<O2> o2s) {
		Result<O1, O2> result = new Result<>();
		
		List<O1> tempO1s = new ArrayList<>(o1s);
		List<O2> tempO2s = new ArrayList<>(o2s);
		
		Iterator<O1> o1Iter = tempO1s.iterator();
		while(o1Iter.hasNext()) {
			O1 o1 = o1Iter.next();
			Iterator<O2> o2Iter = tempO2s.iterator();
			while(o2Iter.hasNext()) {
				O2 o2 = o2Iter.next();
				if (comparator.match(o1, o2)) {
					comparator.matched(o1, o2);
					o1Iter.remove();
					o2Iter.remove();
					
					Matched<O1, O2> m = new Matched<>();
					m.o1 = o1;
					m.o2 = o2;
					result.matched.add(m);
					break;
				}
			}
		}
		
		for (O1 o1: tempO1s) {
			comparator.afterO1(o1);
		}
		for (O2 o2: tempO2s) {
			comparator.afterO2(o2);
		}
		
		result.o1s = tempO1s;
		result.o2s = tempO2s;
		
		return result;
	}
	
	public static <O1, O2> Result<O1, O2> compareCollection(Collection<O1> o1s, Collection<O2> o2s, Comparator<O1, O2> comparator) {
		return new CollectionComparator<O1, O2>(comparator).compare(o1s, o2s);
	}
}
