/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.factory;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.maintenance.factory.MaintenanceObject;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.MonitorCollectDataCache;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.util.HinemosTime;

/**
 * 性能実績の削除処理
 *
 * @version 5.1.0
 * @since 5.1.0
 *
 */
public class MaintenanceCollectDataRaw extends MaintenanceObject {

	private static Log m_log = LogFactory.getLog( MaintenanceCollectDataRaw.class );

	private static final Object _deleteLock = new Object();

	/**
	 * 削除処理
	 */
	@Override
	protected int _delete(Long boundary, boolean status, String ownerRoleId, String maintenanceId){
		
		m_log.info("deleteCollectData() : start");
		m_log.debug("_delete() start : status = " + status);
		
		int ret = 0;
		long deletedSince = 0;
		long deletedUntil = 0;
		long startMaintenanceTimestamp = HinemosTime.currentTimeMillis();
		long timeout = HinemosPropertyCommon.maintenance_collect_data_raw_history_deletion_timeout.getNumericValue();
		
		JpaTransactionManager jtm = null;
		
		try{
			//AdminRoleの場合は監視IDを条件にせず、全て削除
			jtm = new JpaTransactionManager();
			if(RoleIdConstant.isAdministratorRole(ownerRoleId)){
				synchronized (_deleteLock) {
					List<Date> targetDateList = QueryUtil.selectTargetDateCollectDataByDateTime(boundary);
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
						int deleteCount = delete(targetDate);
						ret += deleteCount;
						jtm.commit();
						m_log.info("_delete() targetDate = " + targetDate + ", count = " + deleteCount);
						
						// 削除済み期間の記録
						if (deletedSince == 0) {
							deletedSince = deletionSince;
						}
						deletedUntil = deletionUntil;
					}
				}
			}else{
				ArrayList<MonitorInfo> monitorList = new MonitorSettingControllerBean().getMonitorList();
				
				ArrayList<String> monitorIdList = new ArrayList<>();
				
				for (MonitorInfo monitorInfo : monitorList) {
					if (RoleIdConstant.isAdministratorRole(ownerRoleId) 
							|| monitorInfo.getOwnerRoleId().equals(ownerRoleId)) {
						monitorIdList.add(monitorInfo.getMonitorId());
					}
				}
				
				if (monitorIdList.isEmpty()) {
					return ret;
				}
				
				for(int i = 0; i < monitorIdList.size(); i++){
					synchronized (_deleteLock) {
						List<Date> targetDateList = QueryUtil.selectTargetDateCollectDataByDateTimeAndMonitorId(boundary, monitorIdList.get(i));
						m_log.info("_delete() target date list = " + targetDateList + ", monitorID = " + monitorIdList.get(i));
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
							int deleteCount = delete(targetDate, monitorIdList.get(i));
							ret += deleteCount;
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

			// 収集データキャッシュ上の不要なデータを削除する。
			MonitorCollectDataCache.removeUnnecessaryData();

		} catch (RuntimeException e) {
			//findbugs対応  RuntimeException のキャッチを明示化
			if (jtm != null) {
				jtm.rollback();
			}
			String countMessage = "delete count : " + ret + " records" + "\n";
			m_log.warn("deleteCollectData() : " + countMessage
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			ret = -1;
		} catch(Exception e){
			String countMessage = "delete count : " + ret + " records" + "\n";
			m_log.warn("deleteCollectData() : " + countMessage
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			ret = -1;
			if (jtm != null)
				jtm.rollback();
		} finally {
			if (jtm != null)
				jtm.close();
		}
		m_log.info("deleteCollectData() result : " + ret +
				 ", time : " + (HinemosTime.currentTimeMillis() - startMaintenanceTimestamp));
		return ret;
	}
	
	protected int delete(Date targetDate, String monitorId) {
		//SQL文の実行
		// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0)：タイムアウト値設定
		int ret = QueryUtil.deleteCollectDataByDateTimeAndMonitorId(
				targetDate, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue(), monitorId);
		return ret;
	}
	
	
	protected int delete(Date targetDate) {
		//SQL文の実行
		// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0)：タイムアウト値設定
		int ret = QueryUtil.deleteCollectDataByDateTime(
				targetDate, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue());
		return ret;
	}
}
