/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.jobmanagement.factory.JobSessionImpl;
import com.clustercontrol.jobmanagement.util.JobMultiplicityCache;
import com.clustercontrol.jobmanagement.util.JobSessionChangeDataCache;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.notify.model.MonitorStatusEntity;
import com.clustercontrol.notify.util.MonitorStatusCache;
import com.clustercontrol.util.HinemosTime;

/**
 * ジョブ履歴の削除処理
 *
 * @version 4.0.0
 * @since 3.1.0
 *
 */
public class MaintenanceJob extends MaintenanceObject{

	private static Log m_log = LogFactory.getLog( MaintenanceJob.class );

	private static final Object _deleteLock = new Object();

	/**
	 * 削除処理
	 */
	@Override
	protected int _delete(Long boundary, boolean status, String ownerRoleId) {
		m_log.debug("_delete() start : status = " + status);

		int ret = 0;	// target job_session_id count
		int ret1 = 0;	// target notify_relation_info count
		int ret2 = 0;	// target monitor_status count
		int ret3 = 0;	// target notify_history
		long time1 = 0;	// time job_session_id count
		long time2 = 0;	// time remove cache
		long time3 = 0;	// time remove lock
		long time4 = 0;	// time remove db

		///////////////////////////////////////////////
		// RUN SQL STATEMENT
		///////////////////////////////////////////////

		long start = HinemosTime.currentTimeMillis();

		try {

			synchronized (_deleteLock) {

				// 削除対象となるsession_idを格納するテンポラリテーブル作成(スキーマ定義引継ぎ)
				m_log.debug("_delete() : CREATE TEMPORARY TABLE AND INSERT JOB_SESSION_ID");
				m_log.debug("_delete() : sql = JobCompletedSessionsEntity.createTable");
				QueryUtil.createJobCompletedSessionsTable();
		
				// 削除対象となるsession_idの検索と挿入
				//オーナーロールIDがADMINISTRATORSの場合
				if(RoleIdConstant.isAdministratorRole(ownerRoleId)) {
					if(status){
						QueryUtil.insertJobCompletedSessionsJobSessionJob(boundary);
					} else {
						QueryUtil.insertJobCompletedSessionsJobSessionJobByStatus(boundary);
					}
				}
				//オーナーロールが一般ロールの場合
				else {
					if(status){
						QueryUtil.insertJobCompletedSessionsJobSessionJobByOwnerRoleId(boundary, ownerRoleId);
					} else {
						QueryUtil.insertJobCompletedSessionsJobSessionJobByStatusAndOwnerRoleId(boundary, ownerRoleId);
					}
				}
				
				ArrayList<String> sessionIdList = QueryUtil.selectCompletedSession();
		
				HashMap<String, String> sessionMap = new HashMap<String, String>();
				for(String sessionId : sessionIdList) {
					sessionMap.put(sessionId, sessionId);
				}
		
				time1 = HinemosTime.currentTimeMillis() - start;
		
				//ロックオブジェクトの削除とキャッシュの削除
				start = HinemosTime.currentTimeMillis();
				
				List<MonitorStatusEntity> monitorStatusList = MonitorStatusCache.getByPluginIdAndMonitorMap(HinemosModuleConstant.JOB, sessionMap);
				m_log.info("_delete() : monitorStatusList = " + monitorStatusList.size());
				for (MonitorStatusEntity monitorStatus : monitorStatusList) {
					MonitorStatusCache.remove(monitorStatus);
				}
		
				// 監視ジョブで使用するキャッシュも削除する
				for(String sessionId : sessionIdList) {
					MonitorJobWorker.removeInfoBySessionId(sessionId);
				}
		
				time2 = HinemosTime.currentTimeMillis() - start;
				
				start = HinemosTime.currentTimeMillis();
				ILockManager lm = LockManagerFactory.instance().create();
				for (String sessionId : sessionIdList) {
					lm.delete(JobSessionImpl.class.getName() + "-" + sessionId);
				}
				
				time3 = HinemosTime.currentTimeMillis() - start;
		
				start = HinemosTime.currentTimeMillis();
				m_log.info("_delete() : completed session list size = " + sessionIdList.size()); 
				
				// 削除:cc_job_sessionと関連テーブル(CASCADE)
				m_log.debug("_delete() : DELETE cc_job_session");
				m_log.debug("_delete() : sql = JobCompletedSessionsEntity.deleteByJobCompletedSessions");
				ret = QueryUtil.deleteJobSessionByCompletedSessions();
				m_log.debug("_delete() : DELETE cc_job_session COUNT = " + ret);
		
		
				// 削除:cc_notify_relation_info
				m_log.debug("_delete() : DELETE cc_notify_relation_info");
				m_log.debug("_delete() : sql = NotifyRelationInfoEntity.deleteByJobCompletedSessions");
				ret1 = QueryUtil.deleteNotifyRelationInfoByCompletedSessions();
				m_log.debug("_delete() : DELETE cc_notify_relation_info COUNT = " + ret1);
		
		
				// 削除:cc_monitor_status
				m_log.debug("_delete() : DELETE cc_monitor_status");
				m_log.debug("_delete() : sql = MonitorStatusEntity.deleteByJobCompletedSessions");
				ret2 = QueryUtil.deleteMonitorStatusByCompletedSessions();
				m_log.debug("_delete() : DELETE cc_monitor_status COUNT = " + ret2);
		
				// 削除:cc_notify_history
				m_log.debug("_delete() : DELETE cc_notify_history");
				m_log.debug("_delete() : sql = NotifyHistoryEntity.deleteByJobCompletedSessions");
				ret3 = QueryUtil.deleteNotifyHistoryByCompletedSessions();
				m_log.debug("_delete() : DELETE cc_notify_history COUNT = " + ret3);
		
				// 削除対象となるsession_idを格納するテンポラリテーブル削除
				m_log.debug("_delete() : DROP TEMPORARY TABLE");
				m_log.debug("_delete() : sql = JobCompletedSessionsEntity.dropTable");
				QueryUtil.dropJobCompletedSessionsTable();
		
				time4 = HinemosTime.currentTimeMillis() - start;
				
				m_log.info("_delete() count : " + ret +", count time : " + time1 +"ms, delete time (MonitorStatus) : " + time2
						+ "ms, delete time (lock) : " + time3 +"ms, delete time (DB) : " + time4 +"ms");
		
				// ジョブ多重度のリフレッシュ
				JobMultiplicityCache.refresh();
		
				// ジョブ履歴キャッシュ上の不要なデータを削除する。
				JobSessionChangeDataCache.removeUnnecessaryData();
			}
		} catch (Exception e) {
			String countMessage = "delete count : " + ret + " records" + "\n";
			m_log.warn("deleteCollectData() : " + countMessage
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			ret = -1;
		}

		return ret;
	}
}
