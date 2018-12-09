/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.clustercontrol.utility.difference.anno.ArrayId;

/**
 * 配列の各要素を分類するクラス。
 * 
 * @version 6.0.0
 * @since 2.0.0
 * 
 *
 */
public class ArrayClassifier {
	/**
	 * 比較対象の両配列に存在する要素を保持するクラス。
	 *
	 */
	public static class BothItem {
		public PropValues id;
		public Object item1;
		public Object item2;
	}

	/**
	 * 比較対象の片方の配列にのみ存在する要素を保持するクラス。
	 *
	 */
	public static class ArrayItem {
		public PropValues id;
		public Object item;
	}

	private List<ArrayClassifier.ArrayItem> list1;
	private List<ArrayClassifier.ArrayItem> list2;
	
	private List<ArrayClassifier.ArrayItem> only1s;
	private List<ArrayClassifier.ArrayItem> only2s;

	private List<ArrayClassifier.BothItem> boths;

	private Object[] array1;
	private Object[] array2;
	private ArrayId arrayId;
	
	private PropertyAccessor pa;

	public ArrayClassifier(Object[] array1, Object[] array2, ArrayId arrayId, PropertyAccessor pa) {
		this.array1 = array1;
		this.array2 = array2;
		this.arrayId = arrayId;
		this.pa = pa;
		proc();
	}

	private void proc() {
		list1 = arrayItems(array1);
		list2 = arrayItems(array2);
		
		boths = new ArrayList<ArrayClassifier.BothItem>();
		only1s = new ArrayList<ArrayClassifier.ArrayItem>(list1);
		only2s = new ArrayList<ArrayClassifier.ArrayItem>(list2);

		Iterator<ArrayClassifier.ArrayItem> iter1 = only1s.iterator();
		while (iter1.hasNext()) {
			ArrayClassifier.ArrayItem aitem1 = iter1.next();

			Iterator<ArrayClassifier.ArrayItem> iter2 = only2s.iterator();
			while (iter2.hasNext()) {
				ArrayClassifier.ArrayItem aitem2 = iter2.next();
				
				if (aitem1.id.equals(aitem2.id)) {
					ArrayClassifier.BothItem item = new BothItem();
					item.id = aitem1.id;
					item.item1 = aitem1.item;
					item.item2 = aitem2.item;
					boths.add(item);

					iter1.remove();
					iter2.remove();
					break;
				}
			}
		}
	}

	public List<ArrayClassifier.ArrayItem> getOnly1s() {
		return only1s;
	}

	public List<ArrayClassifier.ArrayItem> getOnly2s() {
		return only2s;
	}

	public List<ArrayClassifier.BothItem> getBoths() {
		return boths;
	}

	public boolean isEqual() {
		return only1s.size() > 0 || only2s.size() > 0;
	}
	
	private List<ArrayClassifier.ArrayItem> arrayItems(Object[] array) {
		List<ArrayClassifier.ArrayItem> aitems = new ArrayList<ArrayClassifier.ArrayItem>();
		
		for (Object item: array) {
			PropValues id = new PropValues();
			switch (arrayId.idType) {
			case pk:
				{
					Object prop = DiffUtil.getPK(item, pa.getType());
					if (prop == null) {
						throw new IllegalStateException(pa.getType().getSimpleName() + " don't have pk.");
					}
					id.props.add(new PropValueImpl(prop, null));
				}
				break;
			case prop:
				{
					PropValue prop = pa.getProperty(item, arrayId.propName);
					if (prop == null) {
						throw new IllegalStateException(pa.getType().getSimpleName() + " don't have " + arrayId.propName +".");
					}
					id.props.add(prop);
				}
				break;
			case props:
				for (String propName: arrayId.props) {
					PropValue prop = pa.getProperty(item, propName);
					if (prop == null) {
						throw new IllegalStateException(
								pa.getType().getSimpleName()
								+ " don't have "
								+ arrayId.propName +" "
								+ propName + "."
								);
					}
					id.props.add(prop);
				}
			}
			
			ArrayClassifier.ArrayItem aitem = new ArrayItem();
			aitem.id = id;
			aitem.item = item;

			aitems.add(aitem);
		}
		
		return aitems;
	}

	public List<ArrayClassifier.ArrayItem> getList1() {
		return list1;
	}

	public List<ArrayClassifier.ArrayItem> getList2() {
		return list2;
	}
}