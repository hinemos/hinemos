/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.factory;

import java.util.ArrayList;

import com.clustercontrol.fault.HinemosPropertyNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.maintenance.model.HinemosPropertyInfo;
import com.clustercontrol.maintenance.util.QueryUtil;

/**
 *
 * 共通設定情報検索クラスです。
 *
 * @since	5.0.0
 * @version	5.0.0
 *
 */
public class SelectHinemosPropertyInfo {

	/**
	 * 共通設定情報リストを取得します。
	 * @return 共通設定情報リスト
	 * @throws HinemosPropertyNotFound
	 */
	public ArrayList<HinemosPropertyInfo> getHinemosPropertyInfoList()
			throws HinemosPropertyNotFound {

		ArrayList<HinemosPropertyInfo> list = new ArrayList<HinemosPropertyInfo>(QueryUtil.getAllHinemosPropertyOrderByKey());

		return list;

	}

	/**
	 * 共通設定情報を取得します。
	 * @param key キー
	 * @return 共通設定情報
	 * @throws HinemosPropertyNotFound
	 * @throws InvalidRole
	 */
	public HinemosPropertyInfo getHinemosPropertyInfo(String key)
		throws HinemosUnknown, InvalidRole, HinemosPropertyNotFound {

		return QueryUtil.getHinemosPropertyInfoPK(key);
	}

	/**
	 * 共通設定情報を取得します。
	 * @param key キー
	 * @return 共通設定情報
	 */
	public HinemosPropertyInfo getHinemosPropertyInfo_None(String key) {
		return HinemosPropertyInfoCache.getProperty(key);
	}
}
