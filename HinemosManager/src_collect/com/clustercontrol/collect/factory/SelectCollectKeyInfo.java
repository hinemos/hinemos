/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.collect.bean.CollectConstant;
import com.clustercontrol.collect.model.CollectKeyInfoPK;
import com.clustercontrol.collect.util.QueryUtil;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.CollectKeyNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jobmanagement.factory.CreateJobSession;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.util.FacilityTreeCache;

public class SelectCollectKeyInfo {
	
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog(SelectCollectKeyInfo.class);
	
	/**
	 * 収集項目IDを取得します。
	 * @throws CollectKeyNotFound 
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 * @throws MonitorNotFound 
	 */
	public Integer getCollectId(String itemName, String displayName, String monitorId, String facilityId) 
			throws CollectKeyNotFound, MonitorNotFound, JobMasterNotFound, HinemosUnknown, InvalidRole {
		String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);

		if (!monitorId.equals(CollectConstant.COLLECT_TYPE_JOB)) { 
			// monitorIdが可視範囲にあるかチェック
			new MonitorSettingControllerBean().getMonitor(monitorId);
		} else {
			// ジョブ履歴
			String[] jobIds = displayName.split(CollectConstant.COLLECT_TYPE_JOB_DELIMITER);
			if (jobIds.length < 2) {
				throw new InvalidRole("jobunitId:jobId=" + displayName + " does not exist");
			}
			// jobunitIdが可視範囲にあるかチェック
			com.clustercontrol.jobmanagement.util.QueryUtil.getJobMstPK(jobIds[0], jobIds[1]);
		}

		if (!facilityId.equals(RoleSettingTreeConstant.ROOT_ID)) {
			// ジョブ履歴(ジョブ)以外の場合
			// facilityIdが可視範囲にあるかチェック
			List<FacilityInfo> facilityList = FacilityTreeCache.getNodeFacilityInfoListByUserId(userId);
			m_log.debug("userId:" + userId + ", facilityList:" + facilityList.toString());
			
			boolean find = false;
			for (FacilityInfo facilityInfo : facilityList) {
				if (facilityInfo.getFacilityId().equals(facilityId)) {
					find = true;
					break;
				}
			}
			if (!find) {
				throw new InvalidRole("facilityId=" + facilityId + " does not exist");
			}
		}
		CollectKeyInfoPK pk = new CollectKeyInfoPK(itemName, displayName, monitorId, facilityId);
		
		return QueryUtil.getCollectKeyPK(pk).getCollectorid();
	}
	
	/**
	 * 収集項目コードを取得します。
	 * 
	 * @param facilityIdList　ファシリティIDリスト
	 * @return 収集項目コードのリスト
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<CollectKeyInfoPK> getCollectKeyList(List<String> facilityIdList) throws InvalidRole, HinemosUnknown {
		List<CollectKeyInfoPK> retCollectList = new ArrayList<>();
		List<CollectKeyInfoPK> collectKeyList = QueryUtil.getCollectKeyAll();

		// monitorIdが可視範囲にあるかチェック
		ArrayList<MonitorInfo> monitorList = new MonitorSettingControllerBean().getMonitorList();
		// jobunitIdが可視範囲にあるかチェック
		List<JobMstEntity> jobMstList = com.clustercontrol.jobmanagement.util.QueryUtil.getJobMstEnityFindByParentId(
				CreateJobSession.TOP_JOBUNIT_ID, CreateJobSession.TOP_JOB_ID);
		HashMap<String, String> facilityIdMap = new HashMap<String, String> ();
		
		// listからmapに変換する。
		for (String facilityId : facilityIdList) {
			facilityIdMap.put(facilityId, facilityId);
		}
		
		for (CollectKeyInfoPK info : collectKeyList) {
			if (facilityIdMap.get(info.getFacilityid()) == null) {
				continue;
			}
			if (!info.getMonitorId().equals(CollectConstant.COLLECT_TYPE_JOB)) {
				// ジョブ履歴以外
				for (MonitorInfo monitorInfo : monitorList) {
					if (monitorInfo.getMonitorId().equals(info.getMonitorId())) {
						retCollectList.add(info);
						continue;
					}
				}
			} else {
				// ジョブ履歴(表示する場合)
				String[] jobIds = info.getDisplayName().split(CollectConstant.COLLECT_TYPE_JOB_DELIMITER);
				if (jobIds.length < 2) {
					continue;
				}
				for (JobMstEntity jobMst : jobMstList) {
					if (jobMst.getId().getJobunitId().equals(jobIds[0])) {
						retCollectList.add(info);
						continue;
					}
				}
			}
		}
		return retCollectList;
	}
}
