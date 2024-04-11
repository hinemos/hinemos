/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.sdml.util.SdmlUtil;
import com.clustercontrol.util.HinemosTime;

/**
 * 収集蓄積情報の削除処理
 *
 * @version 6.0．0
 * @since 6.0．0
 *
 */
public class MaintenanceCollectStringData extends MaintenanceObject {

	private static Log m_log = LogFactory.getLog( MaintenanceCollectStringData.class );

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
		long timeout = HinemosPropertyCommon.maintenance_collect_data_string_history_deletion_timeout.getNumericValue();
		
		JpaTransactionManager jtm = null;
		
		try{
			jtm = new JpaTransactionManager();
			//AdminRoleの場合は監視IDを条件にせず、全て削除
			if(RoleIdConstant.isAdministratorRole(ownerRoleId)){
				synchronized (_deleteLock) {
					List<Date> targetDateList = QueryUtil.selectTargetDateCollectStringDataByDateTime(boundary);
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
				// 指定したオーナーロールIDが設定されたSDML制御設定を取得し、収集データ削除対象の監視項目IDリストに追加する
				monitorIdList.addAll(SdmlUtil.getApplicationIdListByOwnerRoleId(ownerRoleId));
				
				if (monitorIdList.isEmpty()) {
					return ret;
				}
				
				for(int i = 0; i < monitorIdList.size(); i++){
					synchronized (_deleteLock) {
						List<Date> targetDateList = QueryUtil.selectTargetDateCollectStringDataByDateTimeAndMonitorId(boundary, monitorIdList.get(i));
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
		return ret;
	}
	
	protected int delete(Date targetDate, String monitorId) {
		//SQL文の実行
		//for HA (縮退判定時間を延ばすため)、シングルには影響なし(0)：タイムアウト値設定
		int ret = QueryUtil.deleteCollectStringDataByDateTimeAndMonitorId(targetDate, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue(), monitorId);
		return ret;
	}
	
	protected int delete(Date targetDate) {
		//SQL文の実行
		//for HA (縮退判定時間を延ばすため)、シングルには影響なし(0)：タイムアウト値設定
		int ret = QueryUtil.deleteCollectStringDataByDateTime(targetDate, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue());
		return ret;
	}
}
