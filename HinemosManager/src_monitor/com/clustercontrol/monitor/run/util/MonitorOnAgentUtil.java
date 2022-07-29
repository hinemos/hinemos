/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.monitor.run.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.repository.factory.FacilitySelector;

/**
 * HinemosAgent上で動く監視処理の共通Util
 * 
 * @version 6.1.0
 * @since 6.1.0
 * 
 */
public class MonitorOnAgentUtil {

	/** ログ出力用インスタンス */
	private static Log m_log = LogFactory.getLog(MonitorOnAgentUtil.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	/**
	 * 監視/ジョブ設定に紐づくIDを取得.
	 * 
	 * @param jobInfo
	 *            監視結果に含まれるジョブ設定(nullの場合は監視処理として判定).
	 * @param monitorInfo
	 *            監視結果に含まれる監視設定(監視ジョブの場合も値が設定される).
	 * @return 監視/ジョブ設定に紐づくID(監視処理なら監視ID、ジョブならジョブID)
	 */
	public static String getId(RunInstructionInfo jobInfo, MonitorInfo monitorInfo) {

		if (jobInfo == null) {
			return monitorInfo.getMonitorId();
		}
		return jobInfo.getJobId();
	}

	/**
	 * ファシリティIDが監視/ジョブ設定に紐づくかどうかチェック.<br>
	 * <br>
	 * 実際に監視処理を実行したAgentに紐づくFacilityIDが設定に紐づくFacilityIDかどうかをチェックする.<br>
	 * 同一IPのAgentに対して複数FacilityIDを設定した場合を考慮<br>
	 * 
	 * @param checkFacilityId
	 *            チェック対象のファシリティID.
	 * @param jobInfo
	 *            監視結果に含まれるジョブ設定(nullの場合は監視処理として判定).
	 * @param monitorInfo
	 *            監視結果に含まれる監視設定(監視ジョブの場合も値が設定される).
	 * @return true:紐づく、false:紐づかない
	 */
	public static boolean checkFacilityId(String checkFacilityId, RunInstructionInfo jobInfo, MonitorInfo monitorInfo) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		// 設定に紐づくファシリティIDを取得.
		List<String> facilityIdList = getFacilitiIdList(jobInfo, monitorInfo);
		String id = getId(jobInfo, monitorInfo);
		if (facilityIdList == null || facilityIdList.isEmpty()) {
			m_log.debug(methodName + DELIMITER
					+ String.format("facilityID is empty to be associated with the setting. id=%s", id));
			return false;
		}

		// 設定に紐づくファシリティIDに含まれてるかどうかチェック.
		boolean associated = facilityIdList.contains(checkFacilityId);

		// ログ出力.
		if (m_log.isDebugEnabled()) {
			StringBuilder fidsb = new StringBuilder();
			boolean isTop = true;
			for (String facilityId : facilityIdList) {
				if (!isTop) {
					fidsb.append(" ,");
				}
				fidsb.append(facilityId);
				isTop = false;
			}
			m_log.debug(methodName + DELIMITER
					+ String.format(
							"check facilityID to be associated with the setting."
									+ " settingId=%s, checkFacilityID=%s, associatedFacilityIDs=[%s]",
							id, checkFacilityId, fidsb.toString()));
		}
		return associated;

	}

	/**
	 * 監視/ジョブ設定に紐づくFacilitIDリストを取得.
	 * 
	 * @param jobInfo
	 *            監視結果に含まれるジョブ設定(nullの場合は監視処理として判定).
	 * @param monitorInfo
	 *            監視結果に含まれる監視設定(監視ジョブの場合も値が設定される).
	 * @return 監視/ジョブ設定に紐づくファシリティIDのリスト
	 */
	private static List<String> getFacilitiIdList(RunInstructionInfo jobInfo, MonitorInfo monitorInfo) {

		List<String> facilityIdList = null;
		if (jobInfo == null) {
			// 監視設定一覧で設定した監視の場合は監視設定から取得した情報を元にFacilityIDを取得する.
			facilityIdList = FacilitySelector.getFacilityIdList(monitorInfo.getFacilityId(),
					monitorInfo.getOwnerRoleId(), 0, false, false);
		} else {
			// 監視ジョブの場合はテーブルで管理しているセッションジョブの情報を元にFacilityIDを取得する.
			JobSessionJobEntity jobSessionJobEntity = null;
			try {
				// テーブルから処理対象のセッションジョブを取得.
				jobSessionJobEntity = QueryUtil.getJobSessionJobPK(jobInfo.getSessionId(), jobInfo.getJobunitId(),
						jobInfo.getJobId(), ObjectPrivilegeMode.NONE);
			} catch (InvalidRole | JobInfoNotFound e) {
				// 処理対象のセッションジョブを取得できない場合は処理終了.
				return facilityIdList;
			}
			facilityIdList = FacilitySelector.getFacilityIdList(jobInfo.getFacilityId(),
					jobSessionJobEntity.getOwnerRoleId(), 0, false, false);
		}
		return facilityIdList;
	}

}
