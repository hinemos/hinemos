/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.maintenance.factory;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.HinemosTime;

/**
 * メンテナンス機能の削除処理のベースクラス
 *
 * @version 4.0.0
 * @since 3.1.0
 *
 */
public abstract class MaintenanceObject {
	private static Log m_log = LogFactory.getLog( MaintenanceObject.class );

	public static final String _QUERY_TIMEOUT_KEY = "maintenance.query.timeout";
	public static final int _queryTimeoutDefault = 0;
	public static int _queryTimeout;

	/**
	 * 汎用削除処理
	 * @param dataRetentionPeriod
	 * @param status
	 * @param ownerRoleId
	 * @return
	 */
	public int delete(Integer dataRetentionPeriod, boolean status, String ownerRoleId) {;
	int ret;
	m_log.debug("delete() : dataRetentionPeriod : " + dataRetentionPeriod + ", status : " + status);
	Long boundary = getDataRetentionBoundary(dataRetentionPeriod);
	ret = _delete(boundary, status, ownerRoleId);

	return ret;
	};

	/**
	 * 削除処理実態
	 * @param keep
	 * @param status
	 * @param ownerRoleId
	 * @return
	 */
	abstract protected int _delete(Long boundary, boolean status, String ownerRoleId);

	/**
	 * 保存期間(dataRetentionPeriod)に対する保存期間初日00:00:00のepoch値(ミリ秒)を返却する
	 * @param dataRetentionPeriod
	 * @return
	 */
	private Long getDataRetentionBoundary(int dataRetentionPeriod) {
		m_log.debug("getDataRetentionBoundary() dataRetentionPeriod : " + dataRetentionPeriod);

		// 現在時刻のカレンダを取得
		Calendar calendar = HinemosTime.getCalendarInstance();
		
		// 本日0時～現在時刻＋dataRetentionPeriod日のデータを残して削除する
		if(dataRetentionPeriod > 0){
			calendar.add(Calendar.DATE, -dataRetentionPeriod);
		}
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		Long boundary = calendar.getTimeInMillis();

		m_log.debug("getDataRetentionBoundary() : boundary is " + boundary + 
				" (" + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(boundary) + ")" );

		return boundary;
	}
}
