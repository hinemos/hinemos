/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ent.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CSV出力用データ({@code List<String[]>})に、ファシリティIDと表示名のキー({@link DataKey})を付けて、保持します。
 * <p>
 * 本クラスはスレッドセーフではありません。
 */
public class KeyedDataStore {
	private static final Log log = LogFactory.getLog(KeyedDataStore.class);

	// facilityId -> displayName -> data
	// getKeys での順序保証のため、内側の Map には SortedMap を使う
	private Map<String, SortedMap<String, List<String[]>>> map;

	public KeyedDataStore() {
		map = new HashMap<>();
	}

	/**
	 * 指定されたキーに対応する、データを返します。
	 * <p>
	 * 対応するデータが存在しない場合は、空のリストを返します。
	 */
	public List<String[]> getData(DataKey key) {
		Map<String, List<String[]>> map2 = map.get(key.getFacilityId());
		if (map2 == null) {
			return new ArrayList<>();
		}

		List<String[]> data = map2.get(key.getDisplayName());
		if (data == null) {
			return new ArrayList<>();
		}
		return data;
	}

	/**
	 * 保持している(対応するデータが存在する)キーのうち、
	 * ファシリティIDが指定されたものと一致するものを返します。<br/>
	 * 返却されるリストの要素は、表示名の辞書順に整列しています。
	 * <p>
	 * 該当するキーが存在しない場合は、空のリストを返します。
	 */
	public List<DataKey> getDataKeys(String facilityId) {
		List<DataKey> rtn = new ArrayList<>();

		SortedMap<String, List<String[]>> map2 = map.get(facilityId);
		if (map2 == null) {
			return rtn; 
		}

		for (String displayName : map2.keySet()) {
			rtn.add(new DataKey(facilityId, displayName));
		}
		return rtn;
	}

	/**
	 * データを追加します。
	 * <p>
	 * 表示名はデータ内から取得します。<br/>
	 * すでに同じ表示名のデータを保持している場合は、そちらをより優先度の高いデータとみなして残し(先勝ち)、
	 * 後から来たデータは追加しません。
	 * 
	 * @param facilityId ファシリティID。
	 * @param indexOfDisplayName データの何列目を表示名とするかを示すインデックス番号。
	 * @param data 追加するデータ。
	 */
	public void addData(String facilityId, int indexOfDisplayName, List<String[]> data) {
		if (data == null || data.isEmpty()) {
			return;
		}

		// データに含まれる displayName をキーにして、Mapへ分割する
		SortedMap<String, List<String[]>> dataMap = new TreeMap<>();
		String displayNamePrev = null;
		List<String[]> list = null;
		for (String[] rec : data) {
			// キーの変わり目でレコードの追加先リストを切り替える(必要なら生成する)
			String displayName = rec[indexOfDisplayName];
			if (displayNamePrev == null || !displayNamePrev.equals(displayName)) {
				list = dataMap.get(displayName);
				if (list == null) {
					list = new ArrayList<>();
					dataMap.put(displayName, list);
				}
				displayNamePrev = displayName;
			}
			// キー別のリストへ1レコードを追加
			list.add(rec);
		}
		
		// 表示名が未設定の場合のみ、分割したデータを保存する
		SortedMap<String, List<String[]>> savedDataMap = map.get(facilityId);
		if (savedDataMap == null) {
			map.put(facilityId, dataMap);
		} else {
			for (Map.Entry<String,List<String[]>> rec : dataMap.entrySet()) {
				String displayName = rec.getKey();
				if (savedDataMap.containsKey(displayName)) {
					log.debug("addDataLines: Discarded duplicates, facilityId=" + facilityId
							+ ", displayName=" + displayName);
				} else {
					savedDataMap.put(displayName, rec.getValue());
				}
			}
		}
	}
	
}
