/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.factory;


import java.sql.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.monitor.run.util.EventCacheModifyCallback;
import com.clustercontrol.util.HinemosTime;

/**
 * イベント履歴の削除処理
 *
 * @version 4.0.0
 * @since 3.1.0
 *
 */
public class MaintenanceEvent extends MaintenanceObject{

	private static Log m_log = LogFactory.getLog( MaintenanceEvent.class );

	private static final Object _deleteLock = new Object();

	/**
	 * 削除処理
	 */
	@Override
	protected int _delete(Long boundary, boolean status, String ownerRoleId, String maintenanceId) {
		m_log.debug("_delete() start : status = " + status);
		int ret = 0;
		long deletedSince = 0;
		long deletedUntil = 0;
		long startMaintenanceTimestamp = HinemosTime.currentTimeMillis();
		long timeout = HinemosPropertyCommon.maintenance_event_history_deletion_timeout.getNumericValue();

		JpaTransactionManager jtm = null;
		try{

			jtm = new JpaTransactionManager();
			String ownerRoleId2 = null;

			synchronized (_deleteLock) {
				//オーナーロールIDがADMINISTRATORSの場合
				if(RoleIdConstant.isAdministratorRole(ownerRoleId)) {
					//SQL文の実行
					if(status){
						// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0) : タイムアウト値設定
						List<Date> targetDateList = QueryUtil.selectTargetDateEventLogByGenerationDate(boundary);
						m_log.info("_delete() target date list = " + targetDateList);
						for (Date targetDate : targetDateList) {
							long start = HinemosTime.currentTimeMillis();
							if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
								sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout, startMaintenanceTimestamp, maintenanceId);
								ret = -1;
								break;
							}
							Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(targetDate.getTime());
							Long deletionSince = deletionSinceAndUntil[0];
							Long deletionUntil = deletionSinceAndUntil[1];
							m_log.info("_delete() : Oldest startDate of history to be deleted: " + targetDate.getTime());
							m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
							
							jtm.begin();
							QueryUtil.deleteEventLogOperationHistoryByGenerationDate(targetDate, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue());
							int deleteCount = QueryUtil.deleteEventLogByGenerationDate(targetDate, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue());
							ret += deleteCount;
							// cache内も消す
							// status=trueは全削除、status=falseはConfirmFlgが1(確認)のものを削除
							jtm.addCallback(new EventCacheModifyCallback(QueryUtil.parseTargetDateToTargetUnixTime(targetDate), status, ownerRoleId2));
							jtm.commit();
							m_log.info("_delete() targetDate = " + targetDate + ", count = " + deleteCount);
							
							// 削除済み期間の記録
							if (deletedSince == 0) {
								deletedSince = deletionSince;
							}
							deletedUntil = deletionUntil;
						}
					} else {
						// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0) : タイムアウト値設定
						//status=falseの場合は確認済みイベントのみを削除する
						List<Date> targetDateList = QueryUtil.selectTargetDateEventLogByGenerationDateConfigFlg(boundary);
						m_log.info("_delete() target date list = " + targetDateList);
						for (Date targetDate : targetDateList) {
							long start = HinemosTime.currentTimeMillis();
							if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
								sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout, startMaintenanceTimestamp, maintenanceId);
								ret = -1;
								break;
							}
							Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(targetDate.getTime());
							Long deletionSince = deletionSinceAndUntil[0];
							Long deletionUntil = deletionSinceAndUntil[1];
							m_log.info("_delete() : Oldest startDate of history to be deleted: " + targetDate.getTime());
							m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
							
							jtm.begin();
							QueryUtil.deleteEventLogOperationHistoryByGenerationDateConfigFlg(targetDate, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue());
							int deleteCount = QueryUtil.deleteEventLogByGenerationDateConfigFlg(targetDate, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue());
							ret += deleteCount;
							// cache内も消す
							// status=trueは全削除、status=falseはConfirmFlgが1(確認)のものを削除
							jtm.addCallback(new EventCacheModifyCallback(QueryUtil.parseTargetDateToTargetUnixTime(targetDate), status, ownerRoleId2));
							jtm.commit();
							m_log.info("_delete() targetDate = " + targetDate + ", count = " + deleteCount);
							
							// 削除済み期間の記録
							if (deletedSince == 0) {
								deletedSince = deletionSince;
							}
							deletedUntil = deletionUntil;
						}
					}
				}
				//オーナーロールが一般ロールの場合
				else {
					ownerRoleId2 = ownerRoleId;
					//SQL文の実行
					if(status){
						// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0) : タイムアウト値設定
						List<Date> targetDateList = QueryUtil.selectTargetDateEventLogByGenerationDateAndOwnerRoleId(boundary, ownerRoleId);
						m_log.info("_delete() target date list = " + targetDateList);
						for (Date targetDate : targetDateList) {
							long start = HinemosTime.currentTimeMillis();
							if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
								sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout, startMaintenanceTimestamp, maintenanceId);
								ret = -1;
								break;
							}
							Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(targetDate.getTime());
							Long deletionSince = deletionSinceAndUntil[0];
							Long deletionUntil = deletionSinceAndUntil[1];
							m_log.info("_delete() : Oldest startDate of history to be deleted: " + targetDate.getTime());
							m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
							
							jtm.begin();
							QueryUtil.deleteEventLogOperationHistoryByGenerationDateAndOwnerRoleId(targetDate, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue(), ownerRoleId);
							int deleteCount = QueryUtil.deleteEventLogByGenerationDateAndOwnerRoleId(targetDate, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue(), ownerRoleId);
							ret += deleteCount;
							// cache内も消す
							// status=trueは全削除、status=falseはConfirmFlgが1(確認)のものを削除
							jtm.addCallback(new EventCacheModifyCallback(QueryUtil.parseTargetDateToTargetUnixTime(targetDate), status, ownerRoleId2));
							jtm.commit();
							m_log.info("_delete() targetDate = " + targetDate + ", count = " + deleteCount);
							
							// 削除済み期間の記録
							if (deletedSince == 0) {
								deletedSince = deletionSince;
							}
							deletedUntil = deletionUntil;
						}
					} else {
						// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0) : タイムアウト値設定
						//status=falseの場合は確認済みイベントのみを削除する
						List<Date> targetDateList = QueryUtil.selectTargetDateEventLogByGenerationDateConfigFlgAndOwnerRoleId(boundary, ownerRoleId);
						m_log.info("_delete() target date list = " + targetDateList);
						for (Date targetDate : targetDateList) {
							long start = HinemosTime.currentTimeMillis();
							if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
								sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout, startMaintenanceTimestamp, maintenanceId);
								ret = -1;
								break;
							}
							Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(targetDate.getTime());
							Long deletionSince = deletionSinceAndUntil[0];
							Long deletionUntil = deletionSinceAndUntil[1];
							m_log.info("_delete() : Oldest startDate of history to be deleted: " + targetDate.getTime());
							m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
							
							jtm.begin();
							QueryUtil.deleteEventLogOperationHistoryByGenerationDateConfigFlgAndOwnerRoleId(targetDate, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue(), ownerRoleId);
							int deleteCount = QueryUtil.deleteEventLogByGenerationDateConfigFlgAndOwnerRoleId(targetDate, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue(), ownerRoleId);
							ret += deleteCount;
							// cache内も消す
							// status=trueは全削除、status=falseはConfirmFlgが1(確認)のものを削除
							jtm.addCallback(new EventCacheModifyCallback(QueryUtil.parseTargetDateToTargetUnixTime(targetDate), status, ownerRoleId2));
							jtm.commit();
							m_log.info("_delete() targetDate = " + targetDate + ", count = " + deleteCount);
							
							// 削除済み期間の記録
							if (deletedSince == 0) {
								deletedSince = deletionSince;
							}
							deletedUntil = deletionUntil;
						}
					}
				}
	
			}

			//終了
			if (ret > 0) {
				long deleteTime = HinemosTime.currentTimeMillis() - startMaintenanceTimestamp;
				m_log.info("_delete() " + "total delete count = " + ret + ", deleteTime = " + deleteTime + "ms");
			}

		} catch (RuntimeException e) {
			//findbugs対応  RuntimeException のキャッチを明示化
			String countMessage = "delete count : " + ret + " records" + "\n";
			m_log.warn("deleteCollectData() : " + countMessage
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			ret = -1;
			if (jtm != null) {
				jtm.rollback();
			}
		} catch (Exception e) {
			String countMessage = "delete count : " + ret + " records" + "\n";
			m_log.warn("deleteCollectData() : " + countMessage
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			ret = -1;
			if (jtm != null) {
				jtm.rollback();
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return ret;
	}

}
