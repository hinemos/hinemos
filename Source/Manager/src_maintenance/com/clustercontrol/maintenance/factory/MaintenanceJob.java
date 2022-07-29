/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.factory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.jobmanagement.factory.JobSessionImpl;
import com.clustercontrol.jobmanagement.factory.ModifyJobKick;
import com.clustercontrol.jobmanagement.util.JobLinkRcvJobWorker;
import com.clustercontrol.jobmanagement.util.JobLinkSendJobWorker;
import com.clustercontrol.jobmanagement.util.JobMultiplicityCache;
import com.clustercontrol.jobmanagement.util.JobSessionChangeDataCache;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.maintenance.util.QueryUtil;
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

	public static final Object _deleteLock = new Object();

	/**
	 * 削除処理
	 */
	@Override
	protected int _delete(Long boundary, boolean status, String ownerRoleId, String maintenanceId) {
		m_log.debug("_delete() start : status = " + status);

		int ret = 0;	// target job_session_id count
		int ret1 = 0;	// target notify_relation_info count
		int ret2 = 0;	// target monitor_status count
		int ret3 = 0;	// target notify_history
		long time1 = 0;	// time job_session_id count
		long time2 = 0;	// time remove cache
		long time3 = 0;	// time remove lock
		long time4 = 0; // time remove db cc_job_session
		long time5 = 0; // time remove db cc_notify_relation_info
		long time6 = 0; // time remove db cc_monitor_status
		long time7 = 0; // time remove db cc_notify_history
		long time8 = 0; // time remove db total

		long deletedSince = 0;
		long deletedUntil = 0;
		long startMaintenanceTimestamp = HinemosTime.currentTimeMillis();
		long timeout = HinemosPropertyCommon.maintenance_job_history_deletion_timeout.getNumericValue();

		JpaTransactionManager jtm = null;

		// #13057対応により追加
		// trueにした場合、「開始・再実行日時が空」かつ「実行予定日時」が入っているジョブを削除対象として追加する(既存処理に追加)
		final boolean isJobHisDelByScheduleDate = HinemosPropertyCommon.maintenance_job_history_deletion_schedule_date.getBooleanValue();

		///////////////////////////////////////////////
		// RUN SQL STATEMENT
		///////////////////////////////////////////////
		

		try {

			synchronized (_deleteLock) {
				
				jtm = new JpaTransactionManager();
				
				while (true) {
					long start = HinemosTime.currentTimeMillis();
					
					if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
						sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout, startMaintenanceTimestamp, maintenanceId);
						ret = -1;
						break;
					}

					jtm.begin();

					// 削除対象の中で最も過去のジョブ履歴の開始・再実行日時を取得
					m_log.debug("_delete() : SELECT MIN(start_date) FROM cc_job_session_job");

					Long oldestStartDateTimestamp = null;
					Long oldestScheduleDateTimestamp = null;
					if(RoleIdConstant.isAdministratorRole(ownerRoleId)) {
						if(status){
							// 全履歴削除
							oldestStartDateTimestamp = QueryUtil.selectOldestStartDate(boundary);
							if (isJobHisDelByScheduleDate) {
								m_log.debug("_delete() : SELECT MIN(schedule_date) FROM cc_job_session");
								// 削除対象の中で最も過去のジョブ履歴の開始予定日時を取得
								oldestScheduleDateTimestamp = QueryUtil.selectOldestScheduleDate(boundary);
							}
						} else {
							// 実行状態が「終了」または「変更済み」の履歴削除
							oldestStartDateTimestamp = QueryUtil.selectOldestStartDateByStatus(boundary);
							if (isJobHisDelByScheduleDate) {
								m_log.debug("_delete() : SELECT MIN(schedule_date) FROM cc_job_session");
								// 削除対象の中で最も過去のジョブ履歴の開始予定日時を取得(ステータスが終了または変更済)
								oldestScheduleDateTimestamp = QueryUtil.selectOldestScheduleDateByStatus(boundary);
							}
						}
					}
					// オーナーロールが一般ロールの場合
					else {
						if(status){
							// 全履歴削除
							oldestStartDateTimestamp = QueryUtil.selectOldestStartDateByOwnerRoleId(boundary, ownerRoleId);
							if (isJobHisDelByScheduleDate) {
								m_log.debug("_delete() : SELECT MIN(schedule_date) FROM cc_job_session");
								// 削除対象の中で最も過去のジョブ履歴の開始予定日時を取得
								oldestScheduleDateTimestamp = QueryUtil.selectOldestScheduleDateByOwnerRoleId(boundary, ownerRoleId);
							}
						} else {
							// 実行状態が「終了」または「変更済み」の履歴削除
							oldestStartDateTimestamp = QueryUtil.selectOldestStartDateByStatusAndOwnerRoleId(boundary, ownerRoleId);
							if (isJobHisDelByScheduleDate) {
								m_log.debug("_delete() : SELECT MIN(schedule_date) FROM cc_job_session");
								// 削除対象の中で最も過去のジョブ履歴の開始予定日時を取得
								oldestScheduleDateTimestamp = QueryUtil.selectOldestScheduleDateByStatusAndOwnerRoleId(boundary, ownerRoleId);
							}
						}
					}
					m_log.debug("_delete() : oldestStartDateTimestamp=" + oldestStartDateTimestamp + ", oldestScheduleDateTimestamp=" + oldestScheduleDateTimestamp);

					Long deletionSince = null;                // 開始再実行日時(00:00:00)
					Long deletionUntil = null;                // 開始再実行日時+1日(00:00:00)
					Long deletionScheduleDateSince = null;    // 開始予定日時(00:00:00)
					Long deletionScheduleDateUntil = null;    // 開始予定日時+1日(00:00:00)
					if (oldestStartDateTimestamp == null && oldestScheduleDateTimestamp == null) {
						// 削除対象のジョブ履歴が存在しない
						m_log.info("_delete() : No more history to be deleted.");
						jtm.commit();
						break;
					}
					
					if (oldestStartDateTimestamp != null) {
						Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(oldestStartDateTimestamp);
						deletionSince = deletionSinceAndUntil[0];
						deletionUntil = deletionSinceAndUntil[1];
						m_log.info("_delete() : Oldest startDate of history to be deleted: " + oldestStartDateTimestamp);
						m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
					}
					if (oldestScheduleDateTimestamp != null) {
						Long[] deletionScheduleDateSinceAndUntil = getTimestampOfDayStartAndEnd(oldestScheduleDateTimestamp);
						deletionScheduleDateSince = deletionScheduleDateSinceAndUntil[0];
						deletionScheduleDateUntil = deletionScheduleDateSinceAndUntil[1];
						m_log.info("_delete() : Oldest scheduleDate of history to be deleted: " + oldestScheduleDateTimestamp);
						m_log.info("_delete() : Deletion range: " + deletionScheduleDateSince + " -> " + deletionScheduleDateUntil);
					}
					// 削除対象となるsession_idを格納するテンポラリテーブル作成(スキーマ定義引継ぎ)
					m_log.debug("_delete() : CREATE TEMPORARY TABLE AND INSERT JOB_SESSION_ID");
					m_log.debug("_delete() : sql = JobCompletedSessionsEntity.createTable");
					QueryUtil.createJobCompletedSessionsTable();
			
					// 削除対象となるsession_idの検索と挿入
					//オーナーロールIDがADMINISTRATORSの場合
					if(RoleIdConstant.isAdministratorRole(ownerRoleId)) {
						m_log.debug("_delete() : isAdministratorRole is true");
						if(status){
							// 全履歴削除
							if (isJobHisDelByScheduleDate && deletionScheduleDateSince != null && deletionScheduleDateUntil != null) {
								m_log.debug("_delete() : isJobHisDelByScheduleDate is true");
								// 「開始・再実行日時が空」かつ「実行予定日時」が入っているジョブを削除対象として追加
								if (deletionSince != null && deletionUntil != null) {
									// 「開始・再実行日時が有効な値」なジョブ、「開始・再実行日時が空」かつ「実行予定日時」が入っているジョブを削除
									QueryUtil.insertJobCompletedAndInterruptedSessionsJobSessionJob(deletionSince, deletionUntil, deletionScheduleDateSince, deletionScheduleDateUntil);
								} else {
									// 「開始・再実行日時が空」かつ「実行予定日時」が入っているジョブを削除
									QueryUtil.insertJobInterruptedSessionsJobSessionJob(deletionScheduleDateSince, deletionScheduleDateUntil);
								}
							} else {
								QueryUtil.insertJobCompletedSessionsJobSessionJob(deletionSince, deletionUntil);
							}
						} else {
							// 実行状態が「終了」または「変更済み」の履歴削除
							if (isJobHisDelByScheduleDate && deletionScheduleDateSince != null && deletionScheduleDateUntil != null) {
								m_log.debug("_delete() : isJobHisDelByScheduleDate is true");
								if (deletionSince != null && deletionUntil != null) {
									// 「開始・再実行日時が有効な値」なジョブ、「開始・再実行日時が空」かつ「実行予定日時」が入っているジョブを削除
									QueryUtil.insertJobCompletedAndInterruptedSessionsJobSessionJobByStatus(deletionSince, deletionUntil, deletionScheduleDateSince, deletionScheduleDateUntil);
								} else {
									// 「開始・再実行日時が空」かつ「実行予定日時」が入っているジョブを削除
									QueryUtil.insertJobInterruptedSessionsJobSessionJobByStatus(deletionScheduleDateSince, deletionScheduleDateUntil);
								}
							} else {
								QueryUtil.insertJobCompletedSessionsJobSessionJobByStatus(deletionSince, deletionUntil);
							}
						}
					}
					//オーナーロールが一般ロールの場合
					else {
						m_log.debug("_delete() : isAdministratorRole is false");
						if(status){
							// 全履歴削除
							if (isJobHisDelByScheduleDate && deletionScheduleDateSince != null && deletionScheduleDateUntil != null) {
								m_log.debug("_delete() : isJobHisDelByScheduleDate is true");
								if (deletionSince != null && deletionUntil != null) {
									// 「開始・再実行日時が空」かつ「実行予定日時」が入っているジョブを削除対象として追加
									QueryUtil.insertJobCompletedAndInterruptedSessionsJobSessionJobByOwnerRoleId(deletionSince, deletionUntil, deletionScheduleDateSince, deletionScheduleDateUntil, ownerRoleId);
								} else {
									// 「開始・再実行日時が空」かつ「実行予定日時」が入っているジョブを削除
									QueryUtil.insertJobInterruptedSessionsJobSessionJobByOwnerRoleId(deletionScheduleDateSince, deletionScheduleDateUntil, ownerRoleId);
								}
							} else {
								QueryUtil.insertJobCompletedSessionsJobSessionJobByOwnerRoleId(deletionSince, deletionUntil, ownerRoleId);
							}
						} else {
							// 実行状態が「終了」または「変更済み」の履歴削除
							if (isJobHisDelByScheduleDate && deletionScheduleDateSince != null && deletionScheduleDateUntil != null) {
								m_log.debug("_delete() : isJobHisDelByScheduleDate is true");
								if (deletionSince != null && deletionUntil != null) {
									// 「開始・再実行日時が有効な値」なジョブ、「開始・再実行日時が空」かつ「実行予定日時」が入っているジョブを削除
									QueryUtil.insertJobCompletedAndInterruptedSessionsJobSessionJobByStatusAndOwnerRoleId(deletionSince, deletionUntil, deletionScheduleDateSince, deletionScheduleDateUntil, ownerRoleId);
								} else {
									// 「開始・再実行日時が空」かつ「実行予定日時」が入っているジョブを削除
									QueryUtil.insertJobInterruptedSessionsJobSessionJobByStatusAndOwnerRoleId(deletionScheduleDateSince, deletionScheduleDateUntil, ownerRoleId);
								}
							} else {
								QueryUtil.insertJobCompletedSessionsJobSessionJobByStatusAndOwnerRoleId(deletionSince, deletionUntil, ownerRoleId);
							}
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
					
					// キャッシュにある場合のみキャッシュのみ削除する
					// puluginId='JOB'の場合は下のdeleteMonitorStatusByCompletedSessions()で削除するためここでは消さない
					if (MonitorStatusCache.isCache(HinemosModuleConstant.JOB)) {
						MonitorStatusCache.remove(HinemosModuleConstant.JOB, sessionMap.keySet());
					}
			
					for(String sessionId : sessionIdList) {
						// 監視ジョブで使用するキャッシュ削除
						MonitorJobWorker.removeInfoBySessionId(sessionId);

						// ジョブ連携送信ジョブで使用するキャッシュ削除
						JobLinkSendJobWorker.removeInfoBySessionId(sessionId);

						// ジョブ連携待機ジョブで使用するキャッシュ削除
						JobLinkRcvJobWorker.removeInfoBySessionId(sessionId);
					}

					// 不要な事前生成ジョブセッションも削除する
					Calendar deletePremakeCalendar = HinemosTime.getCalendarInstance();
					deletePremakeCalendar.set(Calendar.HOUR_OF_DAY, 0);
					deletePremakeCalendar.set(Calendar.MINUTE, 0);
					deletePremakeCalendar.set(Calendar.SECOND, 0);
					deletePremakeCalendar.set(Calendar.MILLISECOND, 0);
					if(RoleIdConstant.isAdministratorRole(ownerRoleId)) {
						new ModifyJobKick().deleteJobSession(deletePremakeCalendar.getTimeInMillis(), null);
					} else {
						new ModifyJobKick().deleteJobSession(deletePremakeCalendar.getTimeInMillis(), ownerRoleId);
					}
			
					time2 = HinemosTime.currentTimeMillis() - start;
					m_log.debug("_delete() : remove cache, TIME = " + time2 + "ms");
					
					start = HinemosTime.currentTimeMillis();
					ILockManager lm = LockManagerFactory.instance().create();
					for (String sessionId : sessionIdList) {
						lm.delete(JobSessionImpl.class.getName() + "-" + sessionId);
					}
					
					time3 = HinemosTime.currentTimeMillis() - start;
					m_log.debug("_delete() : remove lock, TIME = " + time3 + "ms");
			
					start = HinemosTime.currentTimeMillis();
					m_log.debug("_delete() : completed session list size = " + sessionIdList.size()); 
					
					// 削除:cc_job_sessionと関連テーブル(CASCADE)
					m_log.debug("_delete() : DELETE cc_job_session");
					m_log.debug("_delete() : sql = JobCompletedSessionsEntity.deleteByJobCompletedSessions");
					int result = QueryUtil.deleteJobSessionByCompletedSessions();
					ret += result;
					time4 = HinemosTime.currentTimeMillis() - start;
					m_log.debug("_delete() : DELETE cc_job_session COUNT = " + result + ", TIME = " + time4 +"ms");
			
					start = HinemosTime.currentTimeMillis();
			
			
					// 削除:cc_notify_relation_info
					m_log.debug("_delete() : DELETE cc_notify_relation_info");
					m_log.debug("_delete() : sql = NotifyRelationInfoEntity.deleteByJobCompletedSessions");
					ret1 = QueryUtil.deleteNotifyRelationInfoByCompletedSessions();
					time5 = HinemosTime.currentTimeMillis() - start;
					m_log.debug("_delete() : DELETE cc_notify_relation_info COUNT = " + ret1 + ", TIME = " + time5 +"ms");
			
					start = HinemosTime.currentTimeMillis();
			
					// 削除:cc_monitor_status
					m_log.debug("_delete() : DELETE cc_monitor_status");
					m_log.debug("_delete() : sql = MonitorStatusEntity.deleteByJobCompletedSessions");
					ret2 = QueryUtil.deleteMonitorStatusByCompletedSessions();
					time6 = HinemosTime.currentTimeMillis() - start;
					m_log.debug("_delete() : DELETE cc_monitor_status COUNT = " + ret2 + ", TIME = " + time6 +"ms");
			
					// 削除:cc_notify_history
					m_log.debug("_delete() : DELETE cc_notify_history");
					m_log.debug("_delete() : sql = NotifyHistoryEntity.deleteByJobCompletedSessions");
					ret3 = QueryUtil.deleteNotifyHistoryByCompletedSessions();
					time7 = HinemosTime.currentTimeMillis() - start;
			
					// 削除対象となるsession_idを格納するテンポラリテーブル削除
					m_log.debug("_delete() : DROP TEMPORARY TABLE");
					m_log.debug("_delete() : sql = JobCompletedSessionsEntity.dropTable");
					QueryUtil.dropJobCompletedSessionsTable();
					m_log.debug("_delete() : DELETE cc_notify_history COUNT = " + ret3 + ", TIME = " + time7 +"ms");
			
					time8 = time4 + time5 + time6 + time7;
					
					if (deletionSince != null && deletionUntil != null
							&& deletionScheduleDateSince != null && deletionScheduleDateUntil != null) {
						m_log.info("_delete() target startdate and scheduleDate : startdate :" + deletionSince + " -> " + deletionUntil 
								+ ", scheduleDate :" + deletionScheduleDateSince + " -> " + deletionScheduleDateUntil
								+ ", count : " + result + ", count time : "
								+ time1 + "ms, delete time (MonitorStatus) : " + time2 + "ms, delete time (lock) : " + time3
								+ "ms, delete time (DB) : " + time8 + "ms");
					} else 	if (deletionSince != null && deletionUntil != null) {
						m_log.info("_delete() target startdate : " + deletionSince + " -> " + deletionUntil + ", count : " + result + ", count time : "
								+ time1 + "ms, delete time (MonitorStatus) : " + time2 + "ms, delete time (lock) : " + time3
								+ "ms, delete time (DB) : " + time8 + "ms");
					} else if (deletionScheduleDateSince != null && deletionScheduleDateUntil != null) {
						m_log.info("_delete() target scheduleDate : scheduleDate :" + deletionScheduleDateSince + " -> " + deletionScheduleDateUntil
								+ ", count : " + result + ", count time : "
								+ time1 + "ms, delete time (MonitorStatus) : " + time2 + "ms, delete time (lock) : " + time3
								+ "ms, delete time (DB) : " + time8 + "ms");
					}
			
					jtm.commit();
					
					jtm.begin();
					
					// ジョブ多重度のリフレッシュ
					JobMultiplicityCache.refresh();
			
					// ジョブ履歴キャッシュ上の不要なデータを削除する。
					JobSessionChangeDataCache.removeUnnecessaryData();
					
					jtm.commit();

					// 削除済み期間の記録
					if (deletedSince == 0) {
						if (isJobHisDelByScheduleDate) {
							// 開始再実行日時と開始予定日時を比較して古い方の日付を入れる
							if (deletionSince != null && deletionScheduleDateSince != null) {
								if (deletionSince <= deletionScheduleDateSince) {
									deletedSince = deletionSince;
								} else {
									deletedSince = deletionScheduleDateSince;
								}
							} else if (deletionSince != null) {
								deletedSince = deletionSince;
							} else {
								deletedSince = deletionScheduleDateSince;
							}
						} else {
							deletedSince = deletionSince;
						}
					}
					if (isJobHisDelByScheduleDate) {
						// 開始再実行日時+1と開始予定日時+1日を比較して最新の日付を入れる
						long targetUntilDate = 0;
						if (deletionUntil != null && deletionScheduleDateUntil != null) {
							if (deletionUntil < deletionScheduleDateUntil) {
								targetUntilDate = deletionScheduleDateUntil;
							} else {
								targetUntilDate = deletionUntil;
							}
						} else if(deletionUntil != null) {
							targetUntilDate = deletionUntil;
						} else {
							targetUntilDate = deletionScheduleDateUntil;
						}
						// 最新日付がdeletedUntilより最新の日付の場合上書きする
						if (deletedUntil < targetUntilDate) {
							deletedUntil = targetUntilDate;
						}
					} else {
						deletedUntil = deletionUntil;
					}
					m_log.debug("_delete() deletedDate : " + deletedSince + " ->" + deletedUntil);

					
				}
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
		} catch (Exception e) {
			if (jtm != null) {
				jtm.rollback();
			}
			String countMessage = "delete count : " + ret + " records" + "\n";
			m_log.warn("deleteCollectData() : " + countMessage
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			ret = -1;
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}

		return ret;
	}
}
