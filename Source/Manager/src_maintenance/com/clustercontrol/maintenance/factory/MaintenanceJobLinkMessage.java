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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.repository.bean.FacilityIdConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;

/**
 * ジョブ連携メッセージ情報の削除処理
 *
 */
public class MaintenanceJobLinkMessage extends MaintenanceObject {

	private static Log m_log = LogFactory.getLog(MaintenanceJobLinkMessage.class);

	// ジョブ連携メッセージ情報削除対象のノード上限（1SQLで削除する件数）
	private static final int NODE_DELETE_MAX_COUNT = 1000;

	/**
	 * 削除処理
	 */
	@Override
	protected int _delete(Long boundary, boolean status, String ownerRoleId, String maintenanceId) {
		int ret = 0;
		long deletedSince = 0;
		long deletedUntil = 0;
		long startMaintenanceTimestamp = HinemosTime.currentTimeMillis();
		long timeout = HinemosPropertyCommon.maintenance_job_link_message_history_deletion_timeout.getNumericValue();

		JpaTransactionManager jtm = null;

		try {
			// 履歴削除処理
			jtm = new JpaTransactionManager();

			if (!RoleIdConstant.isAdministratorRole(ownerRoleId)) {

				// AdminRole以外
				// 対象ノードのファシリティID取得
				List<String> facilityIdList = new RepositoryControllerBean()
						.getNodeFacilityIdList(FacilityIdConstant.ROOT, ownerRoleId, RepositoryControllerBean.ALL);

				if (facilityIdList == null || facilityIdList.size() == 0) {
					// 削除対象なし
				} else {
					// NODE_DELETE_MAX_COUNTよりノード数が超過する場合は、NODE_DELETE_MAX_COUNTごとに削除処理を行う。
					for (int i = 0; i < facilityIdList.size(); i = i + NODE_DELETE_MAX_COUNT) {
						String[] tmpFacilityIds = null;
						if ((i + NODE_DELETE_MAX_COUNT) > facilityIdList.size()) {
							tmpFacilityIds = facilityIdList.subList(i, facilityIdList.size())
									.toArray(new String[facilityIdList.size()]);
						} else {
							tmpFacilityIds = facilityIdList.subList(i, i + NODE_DELETE_MAX_COUNT)
									.toArray(new String[NODE_DELETE_MAX_COUNT]);
						}
						// 履歴削除
						List<Date> targetDateList = QueryUtil.selectTargetDateJobLinkMessageBySendDate(tmpFacilityIds, boundary);
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
							int deleteCount = QueryUtil.deleteJobLinkMessageBySendDate(tmpFacilityIds, targetDate);
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
			} else {
				// AdminRole
				List<Date> targetDateList = QueryUtil.selectTargetDateJobLinkMessageBySendDate(null, boundary);
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
					int deleteCount = QueryUtil.deleteJobLinkMessageBySendDate(null, targetDate);
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
			if (ret > 0) {
				long deleteTime = HinemosTime.currentTimeMillis() - startMaintenanceTimestamp;
				m_log.info("_delete() " + "total delete count = " + ret + ", deleteTime = " + deleteTime + "ms");
			}

		} catch (Exception e) {
			String countMessage = "delete count : " + ret + " records" + "\n";
			m_log.warn("deleteMaintenanceJobLinkMessage() : " + countMessage
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
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
