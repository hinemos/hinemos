/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * マネージャ名と任意の型のデータをペアで保持する単純なクラスです。
 * @param <T> データの型。
 */
public class ManagerTag<T> {
	/** マネージャ名 */
	public final String managerName;
	/** データ */
	public final T data;

	public ManagerTag(String managerName, T data) {
		this.managerName = managerName;
		this.data = data;
	}

	/**
	 * 複数のデータから、複数のインスタンスの生成します。
	 * 
	 * @param managerName 全データに共通のマネージャ名。
	 * @param dataList 複数のデータ。
	 */
	public static <T> List<ManagerTag<T>> listFrom(String managerName, Iterable<T> dataList) {
		List<ManagerTag<T>> rtn = new ArrayList<>();
		for (T data : dataList) {
			rtn.add(new ManagerTag<>(managerName, data));
		}
		return rtn;
	}

	/**
	 * 複数のオブジェクトをマップへ変換します。
	 *
	 * @param objects 複数のオブジェクト。
	 * @return K:マネージャ名、V:そのマネージャとペアになっていたデータのリスト。
	 */
	public static <T> SortedMap<String, List<T>> listToMap(Iterable<ManagerTag<T>> objects) {
		SortedMap<String, List<T>> rtn = new TreeMap<>();
		for (ManagerTag<T> o : objects) {
			List<T> list = rtn.get(o.managerName);
			if (list == null) {
				list = new ArrayList<>();
				rtn.put(o.managerName, list);
			}
			list.add(o.data);
		}
		return rtn;
	}
}
