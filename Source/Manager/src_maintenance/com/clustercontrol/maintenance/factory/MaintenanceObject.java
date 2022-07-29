/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.factory;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * メンテナンス機能の削除処理のベースクラス
 *
 * @version 4.0.0
 * @since 3.1.0
 *
 */
public abstract class MaintenanceObject {
	private static Log m_log = LogFactory.getLog( MaintenanceObject.class );

	/**
	 * 汎用削除処理
	 * @param dataRetentionPeriod
	 * @param status
	 * @param ownerRoleId
	 * @param maintenanceId
	 * @return
	 */
	public int delete(Integer dataRetentionPeriod, boolean status, String ownerRoleId, String maintenanceId) {;
	int ret;
	m_log.debug("delete() : dataRetentionPeriod : " + dataRetentionPeriod + ", status : " + status);
	Long boundary = getDataRetentionBoundary(dataRetentionPeriod);
	ret = _delete(boundary, status, ownerRoleId, maintenanceId);

	return ret;
	};

	/**
	 * 削除処理実態
	 * @param keep
	 * @param status
	 * @param ownerRoleId
	 * @param maintenanceId
	 * @return
	 */
	abstract protected int _delete(Long boundary, boolean status, String ownerRoleId, String maintenanceId);

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
	
	/**
	 * 履歴削除がタイムアウト時間を超過しているか否かを判定
	 * @param startTimestamp
	 * @return
	 */
	protected boolean isTimedOut(long startTimestamp, long currentTimestamp, long timeout) {
		long maintenanceDuration = currentTimestamp - startTimestamp;
		return timeout * 1000 < maintenanceDuration;
	}
	
	/**
	 * 履歴削除がタイムアウトした場合のINTERNALメッセージを送信
	 * @param deletedSince
	 * @param deletedUntil
	 * @param boundary
	 * @param timeout
	 * @param startMaintenanceTimestamp
	 * @param maintenanceId
	 */
	protected void sendInternalMessageForTimeout(long deletedSince, long deletedUntil,
			long boundary, Long timeout, long startMaintenanceTimestamp, String maintenanceId) {
		Calendar cal = HinemosTime.getCalendarInstance();

		// 実際に削除された期間の終了日を取得
		// deletedUntilは削除対象期間には含まれないため、その前日を取得する
		cal.setTimeInMillis(deletedUntil);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		long deletedUntilYesterday = cal.getTimeInMillis();
		
		// 削除対象期間の終了日を取得
		// boundaryは削除対象期間に含まれないため、その前日を取得する
		cal.setTimeInMillis(boundary);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		long boundaryYesterday = cal.getTimeInMillis();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		String deletedSinceStr = sdf.format(deletedSince);
		String deletedUntilStr = sdf.format(deletedUntilYesterday);
		String boundaryStr = sdf.format(boundaryYesterday);
		
		String[] args = new String[4];
		args[0] = maintenanceId;
		args[1] = timeout.toString();
		args[2] = deletedSinceStr + " - " + boundaryStr;
		if (deletedUntil == 0) {
			// deletedUntilが0、すなわち1日も削除できていない場合は
			// 削除済み期間にはNoneと表示しておく
			args[3] = "None";
		} else {
			args[3] = deletedSinceStr + " - " + deletedUntilStr;
		}
		
		AplLogger.put(InternalIdCommon.MAINTENANCE_SYS_002, args);
		m_log.warn("_delete() : Maintenance is timed out. " +
				"startMaintenanceTimestamp: " + startMaintenanceTimestamp + ", " +
				"deletion target: " + deletedSince + " -> " + boundaryYesterday + ", " +
				"deleted: " + deletedSince + " -> " + deletedUntilYesterday);
	}
	
	/**
	 * 渡されたUNIXタイムスタンプの同日の00:00:00と翌日の00:00:00を表現するUNIXタイムスタンプの配列を返す
	 * @param timestamp
	 * @return
	 */
	protected Long[] getTimestampOfDayStartAndEnd(Long timestamp) {
		Calendar cal = HinemosTime.getCalendarInstance();
		Long[] startAndEnd = new Long[2];
		
		// 同日の00:00:00
		cal.setTimeInMillis(timestamp);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		startAndEnd[0] = cal.getTimeInMillis();
		
		// 翌日の00:00:00
		cal.add(Calendar.DAY_OF_MONTH, 1);
		startAndEnd[1] = cal.getTimeInMillis();

		return startAndEnd;
	}
}
