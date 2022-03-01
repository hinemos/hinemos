/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.filtersetting.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.rap.rwt.SingletonUtil;

import com.clustercontrol.util.RestConnectManager;

/**
 * フィルタ条件として選択しているマネージャ名を登録するクラスです。
 * 選択しているマネージャからログアウトした場合は登録を解除します。
 */
public class FilterSettingManagerNameUpdater {

	private ConcurrentHashMap<String, String> filterManagerNameMap = new ConcurrentHashMap<>();

	private FilterSettingManagerNameUpdater() {
	}

	public static FilterSettingManagerNameUpdater getInstance() {
		return SingletonUtil.getSessionInstance( FilterSettingManagerNameUpdater.class );
	}

	/**
	 * フィルタ条件として選択しているマネージャ名を登録します。
	 * @param viewId フィルタ条件を設定しているビューID。
	 * @param filterManagerName フィルタ条件のマネージャ名。 null の場合は登録済みのマネージャ名を解除します。
	 */
	public void setFilterManagerName(String viewId, String filterManagerName) {
		if (filterManagerName == null) {
			filterManagerNameMap.remove(viewId);
		} else {
			filterManagerNameMap.put(viewId, filterManagerName);
		}
	}

	/**
	 * フィルタ条件として選択しているマネージャ名を取得します（登録なしなら全接続マネージャ）。
	 * @param viewId フィルタ条件を設定しているビューID。
	 * @return フィルタ条件として選択しているマネージャ名（nullなら全接続マネージャ）。
	 */
	public String getFilterManagerName(String viewId) {
		return filterManagerNameMap.get(viewId);
	}

	/**
	 * フィルタ条件として選択しているマネージャ名についてログアウトしているものは解除します。
	 */
	public void updateFilterManagerNames() {
		List<String> currentActiveManagerList = Arrays.asList(RestConnectManager.createManagerSelectValues());
		for (Map.Entry<String, String> entry : filterManagerNameMap.entrySet()) {
			if (!currentActiveManagerList.contains(entry.getValue())) {
				filterManagerNameMap.remove(entry.getKey());
			}
		}
	}
}
