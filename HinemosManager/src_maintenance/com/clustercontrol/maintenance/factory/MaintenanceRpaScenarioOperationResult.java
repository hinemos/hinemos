/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.factory;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.rpa.scenario.model.RpaScenario;
import com.clustercontrol.rpa.session.RpaControllerBean;
import com.clustercontrol.util.HinemosTime;

/**
 * RPAシナリオ実績の削除処理
 *
 */
public class MaintenanceRpaScenarioOperationResult extends MaintenanceObject {

	private static Log m_log = LogFactory.getLog( MaintenanceRpaScenarioOperationResult.class );

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
		long timeout = HinemosPropertyCommon.maintenance_rpa_scenario_ope_result_history_deletion_timeout.getNumericValue();
		
		JpaTransactionManager jtm = null;
		
		try{
			jtm = new JpaTransactionManager();
			//AdminRoleの場合はシナリオIDを条件にせず、全て削除
			if(RoleIdConstant.isAdministratorRole(ownerRoleId)){
				synchronized (_deleteLock) {
					List<Date> targetDateList = QueryUtil.selectTargetDateRpaScenarioOperationResultByDateTime(boundary);
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
				List<RpaScenario> scenarioList = new RpaControllerBean().getRpaScenarioList();
				List<String> scenarioIdList = scenarioList.stream()
						.filter(scenario -> scenario.getOwnerRoleId().equals(ownerRoleId))
						.map(RpaScenario::getScenarioId)
						.collect(Collectors.toList());
				
				if (scenarioIdList.isEmpty()) {
					return ret;
				}
				
				for(int i = 0; i < scenarioIdList.size(); i++){
					synchronized (_deleteLock) {
						List<Date> targetDateList = QueryUtil.selectTargetDateRpaScenarioOperationResultByDateTimeAndScenarioId(boundary, scenarioIdList.get(i));
						m_log.info("_delete() target date list = " + targetDateList + ", scenarioId = " + scenarioIdList.get(i));
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
							int deleteCount = delete(targetDate, scenarioIdList.get(i));
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
			
		} catch(Exception e){
			if (jtm != null) {
				jtm.rollback();
			}
			String countMessage = "delete count : " + ret + " records" + "\n";
			m_log.warn("deleteRpaScenarioOperationResult() : " + countMessage
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			ret = -1;
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return ret;
	}
	
	protected int delete(Date targetDate, String scenarioId) {
		//SQL文の実行
		//for HA (縮退判定時間を延ばすため)、シングルには影響なし(0)：タイムアウト値設定
		int ret = QueryUtil.deleteRpaScenarioOperationResultByDateTimeAndScenarioId(targetDate, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue(), scenarioId);
		return ret;
	}
	
	protected int delete(Date targetDate) {
		//SQL文の実行
		//for HA (縮退判定時間を延ばすため)、シングルには影響なし(0)：タイムアウト値設定
		int ret = QueryUtil.deleteRpaScenarioOperationResultDataByDateTime(targetDate, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue());
		return ret;
	}
}
