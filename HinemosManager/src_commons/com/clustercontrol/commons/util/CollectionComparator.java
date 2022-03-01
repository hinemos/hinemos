/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.clustercontrol.fault.HinemosUnknown;

/**
 * Collection同士を比較し、結果に応じた処理を実行するクラス
 * @see com.clustercontrol.xcloud.util.CollectionComparator<O1, O2>
 */
public class CollectionComparator<O1, O2> {
	
	public CollectionComparator(Comparator<O1,O2> comparator) {
		this.comparator = comparator;
	}

	public static interface Comparator<O1, O2> {
		public boolean match(O1 o1, O2 o2) throws HinemosUnknown;
		public void matched(O1 o1, O2 o2) throws HinemosUnknown;
		public void afterO1(O1 o1) throws HinemosUnknown;
		public void afterO2(O2 o2) throws HinemosUnknown;
	}

	private Comparator<O1, O2> comparator;
		
	public void compare(Collection<O1> o1s, Collection<O2> o2s) throws HinemosUnknown {
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
	}

	public static <O1, O2> void compare(Collection<O1> o1s, Collection<O2> o2s, Comparator<O1, O2> comparator) throws HinemosUnknown {
		new CollectionComparator<O1, O2>(comparator).compare(o1s, o2s);
	}

}
