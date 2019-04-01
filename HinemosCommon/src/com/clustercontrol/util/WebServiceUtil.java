/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * ウェブサービスに関連するユーティリティ メソッドを提供します。
 *
 * @since 6.2.0
 */
public class WebServiceUtil {

	/**
	 * JAX-WS で Map が利用できないことを回避するため、Map を List へ変換します。
	 * <p>
	 * Map のキーと値は、クローンされることなく、List へ追加されます。
	 *
	 * @param map 変換元の Map。
	 * @return 変換後の List。引数が null であった場合は null。
	 */
	// TODO: 回避方法を調査する
	public static <T> List<T> convertToList(Map<T, T> map) {
		if (map == null) {
			return null;
		}
		
		List<T> list = new ArrayList<T>();
		for (Map.Entry<T, T> entry : map.entrySet()) {
			list.add(entry.getKey());
			list.add(entry.getValue());
		}
		return list;
	}

	/**
	 * convertToList で変換した List を Map へ復元します。
	 * <p>
	 * List の各要素は、クローンされることなく、Map へ追加されます。<br>
	 * List の要素が奇数の場合(本来はあってはならないことですが)、変換後の Map には、
	 * 「List の最後の要素をキー、nullを値」とするエントリーが追加されます。
	 *
	 * @param list 変換元の List。
	 * @return 変換後の Map。引数が null であった場合は null。
	 */
	// TODO: 回避方法を調査する
	public static <T> Map<T, T> convertToMap(List<T> list) {
		if (list == null) {
			return null;
		}
		
		Map<T, T> map = new HashMap<T, T>();
		Iterator<T> itr = list.iterator();
		while (itr.hasNext()) {
			T key = itr.next();
			T value = null;
			if (itr.hasNext()) {
				value = itr.next();
			}
			map.put(key, value);
		}
		return map;
	}
}
