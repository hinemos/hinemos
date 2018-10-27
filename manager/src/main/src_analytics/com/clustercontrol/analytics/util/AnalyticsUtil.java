/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */


package com.clustercontrol.analytics.util;

import com.clustercontrol.hub.bean.StringQueryInfo;
import com.clustercontrol.hub.bean.StringQueryInfo.Operator;

/**
 * サイレント障害監視用ユーティリティクラス
 *
 */
public class AnalyticsUtil {

	/**
	 * メッセージ用のItemNameを作成する
	 * 
	 * @param itemName ItemName
	 * @param displayName DisplayName
	 * @param monitorId 監視項目ID
	 * @return メッセージ用ItemName
	 */
	public static String getMsgItemName(String itemName, String displayName, String monitorId) {
		// itemNameにdisplayNameが入っているかのチェック
		if (!displayName.equals("") && !itemName.endsWith("[" + displayName + "]")) {
			itemName += "[" + displayName + "]";
		}
		itemName += "(" + monitorId + ")";
		return itemName;
	}

	/**
	 *　収集データ（文字列）取得のための検索条件を作成する
	 * @param from
	 * @param to
	 * @param monitorId
	 * @param keywords
	 * @param ope
	 * @param dispOffset
	 * @param dispSize
	 * @param firstQuery
	 * @return
	 */
	public static StringQueryInfo makeQuery(
			Long from,
			Long to,
			String facilityId,
			String monitorId, 
			String keywords,
			String tag,
			Operator ope){
		StringQueryInfo query = new StringQueryInfo();
		query.setFacilityId(facilityId);
		query.setOperator(ope);
		query.setMonitorId(monitorId);
		query.setTag(tag);
		query.setOffset(0);
		query.setSize(Integer.MAX_VALUE);
		query.setFrom(from);
		query.setTo(to);
		query.setKeywords(keywords);
		query.setNeedCount(true);
		return query;
	}
}
