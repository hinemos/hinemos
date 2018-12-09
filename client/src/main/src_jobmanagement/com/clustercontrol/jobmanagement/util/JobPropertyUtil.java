/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.StatusMessage;
import com.clustercontrol.jobmanagement.OperationMessage;
import com.clustercontrol.jobmanagement.bean.HistoryFilterPropertyConstant;
import com.clustercontrol.jobmanagement.bean.JobOperationConstant;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeMessage;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.bean.PlanFilterPropertyConstant;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.ws.jobmanagement.HinemosUnknown_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.JobEndStatusInfo;
import com.clustercontrol.ws.jobmanagement.JobHistoryFilter;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobMasterNotFound_Exception;
import com.clustercontrol.ws.jobmanagement.JobOperationInfo;
import com.clustercontrol.ws.jobmanagement.JobParameterInfo;
import com.clustercontrol.ws.jobmanagement.JobPlanFilter;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.jobmanagement.NotifyNotFound_Exception;
import com.clustercontrol.ws.jobmanagement.UserNotFound_Exception;

public class JobPropertyUtil {
	// ログ
	private static Log m_log = LogFactory.getLog( JobPropertyUtil.class );

	public static JobOperationInfo property2jobOperation (Property property) {
		JobOperationInfo info = new JobOperationInfo();

		//セッションID取得
		ArrayList<?> values = PropertyUtil.getPropertyValue(property, JobOperationConstant.SESSION);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0)
			info.setSessionId((String)values.get(0));

		//ジョブユニットID取得
		values = PropertyUtil.getPropertyValue(property, JobOperationConstant.JOB_UNIT);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0)
			info.setJobunitId((String)values.get(0));

		//ジョブID取得
		values = PropertyUtil.getPropertyValue(property, JobOperationConstant.JOB);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0)
			info.setJobId((String)values.get(0));

		//ファシリティID取得
		values = PropertyUtil.getPropertyValue(property, JobOperationConstant.FACILITY);
		if(values.size() > 0 && values.get(0) instanceof String && ((String)values.get(0)).length() > 0)
			info.setFacilityId((String)values.get(0));

		//制御取得
		values = PropertyUtil.getPropertyValue(property, JobOperationConstant.CONTROL);
		Integer control = null;
		if(values.get(0) instanceof String){
			String controlString = (String)values.get(0);
			control = Integer.valueOf(OperationMessage.stringToType(controlString));
			info.setControl(control);
		}

		if (control == null)
			throw new InternalError("control is null , controlString : " + values.get(0));
		
		//終了状態取得
		values = PropertyUtil.getPropertyValue(property, JobOperationConstant.END_STATUS);
		if(values.size() > 0 && values.get(0) instanceof Integer)
			info.setEndStatus((Integer)values.get(0));

		//終了値取得
		values = PropertyUtil.getPropertyValue(property, JobOperationConstant.END_VALUE);
		if(values.size() > 0 && values.get(0) instanceof Integer)
			info.setEndValue((Integer)values.get(0));

		// #2360における4.1.0のHinemosマネージャとの互換性のため、停止[スキップ]の場合は、デフォルト値(終了状態:異常、終了値:0)を入れる
		// 4.1.1以降のHinemosマネージャではこの値はマネージャ側で破棄される
		if (control == OperationConstant.TYPE_STOP_SKIP) {
			info.setEndStatus(EndStatusConstant.TYPE_ABNORMAL);
			info.setEndValue(EndStatusConstant.INITIAL_VALUE_NORMAL);
		}

		return info;
	}

	public static JobHistoryFilter property2jobHistoryFilter (Property property) {
		JobHistoryFilter filter = new JobHistoryFilter();
		ArrayList<?> values = null;

		//開始・再実行日時（自）取得
		values = PropertyUtil.getPropertyValue(property, HistoryFilterPropertyConstant.START_FROM_DATE);
		if(values.get(0) != null && values.get(0) instanceof Date){
			filter.setStartFromDate(((Date)values.get(0)).getTime());
		}
		//開始・再実行日時（至）取得
		values = PropertyUtil.getPropertyValue(property, HistoryFilterPropertyConstant.START_TO_DATE);
		if(values.get(0) != null && values.get(0) instanceof Date){
			filter.setStartToDate(((Date)values.get(0)).getTime());
		}
		//終了・中断日時（自）取得
		values = PropertyUtil.getPropertyValue(property, HistoryFilterPropertyConstant.END_FROM_DATE);
		if(values.get(0) != null && values.get(0) instanceof Date){
			filter.setEndFromDate(((Date)values.get(0)).getTime());
		}
		//終了・中断日時（至）取得
		values = PropertyUtil.getPropertyValue(property, HistoryFilterPropertyConstant.END_TO_DATE);
		if(values.get(0) != null && values.get(0) instanceof Date){
			filter.setEndToDate(((Date)values.get(0)).getTime());
		}

		//ジョブID取得
		values = PropertyUtil.getPropertyValue(property, HistoryFilterPropertyConstant.JOB_ID);
		if(values.get(0) instanceof JobTreeItem){
			filter.setJobId(((JobTreeItem)values.get(0)).getData().getId());
		}
		else if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			filter.setJobId((String)values.get(0));
		}

		//実行状態取得
		values = PropertyUtil.getPropertyValue(property, HistoryFilterPropertyConstant.STATUS);
		Integer status = null;
		if(values.get(0) instanceof String){
			String statusString = (String)values.get(0);
			status = Integer.valueOf(StatusMessage.stringToType(statusString));
			filter.setStatus(status);
		}

		//終了状態取得
		values = PropertyUtil.getPropertyValue(property, HistoryFilterPropertyConstant.END_STATUS);
		Integer endStatus = null;
		if(values.get(0) instanceof String){
			String statusString = (String)values.get(0);
			endStatus = Integer.valueOf(EndStatusMessage.stringToType(statusString));
			filter.setEndStatus(endStatus);
		}

		//実行契機種別取得
		values = PropertyUtil.getPropertyValue(property, HistoryFilterPropertyConstant.TRIGGER_TYPE);
		Integer triggerType = null;
		if(values.get(0) instanceof String){
			String triggerTypeString = (String)values.get(0);
			triggerType = Integer.valueOf(JobTriggerTypeMessage.stringToType(triggerTypeString));
			filter.setTriggerType(triggerType);
		}

		//実行契機情報取得
		values = PropertyUtil.getPropertyValue(property, HistoryFilterPropertyConstant.TRIGGER_INFO);
		String triggerInfo = null;
		if(!"".equals(values.get(0))) {
			triggerInfo = (String) values.get(0);
			filter.setTriggerInfo(triggerInfo);
		}

		//オーナーロールID
		values = PropertyUtil.getPropertyValue(property, HistoryFilterPropertyConstant.OWNER_ROLE_ID);
		String ownerRoleId = null;
		if(!"".equals(values.get(0))) {
			ownerRoleId = (String) values.get(0);
			filter.setOwnerRoleId(ownerRoleId);
		}
		return filter;
	}
	public static JobPlanFilter property2jobPlanFilter (Property property) {
		JobPlanFilter filter = new JobPlanFilter();
		ArrayList<?> values = null;

		//開始取得
		values = PropertyUtil.getPropertyValue(property, PlanFilterPropertyConstant.FROM_DATE);
		m_log.debug("JobPlanFilter property2JobPlanFilter");
		if (values.get(0) instanceof Date){
			m_log.debug("property2jobPlanFilter : fromDate=" + ((Date)values.get(0)).getTime());
			filter.setFromDate(((Date)values.get(0)).getTime());
		}
		//終了取得
		values = PropertyUtil.getPropertyValue(property, PlanFilterPropertyConstant.TO_DATE);
		if (values.get(0) instanceof Date){
			m_log.debug("property2jobPlanFilter : toDate=" + ((Date)values.get(0)).getTime());
			filter.setToDate(((Date)values.get(0)).getTime());
		}
		//実行契機ID
		values = PropertyUtil.getPropertyValue(property, PlanFilterPropertyConstant.JOBKICK_ID);
		if(values.get(0) != null){
			m_log.debug("property2jobPlanFilter : id=" + ((String)values.get(0)));
			filter.setJobKickId((String)values.get(0));
		}
		return filter;
	}

	/**
	 * ジョブツリーを全てFullにする。
	 * 負荷が高いので取り扱い注意。
	 * @param jobTreeItem
	 */
	public static void setJobFullTree (String managerName, JobTreeItem jobTreeItem) {
		List<JobTreeItem> children = jobTreeItem.getChildren();
		if (children == null) {
			return;
		}
		
		// FullJob
		List<JobInfo> list = new ArrayList<JobInfo>();
		list.add(jobTreeItem.getData()); // Include root's FullJob also
		for (JobTreeItem info : children) {
			list.add(info.getData());
		}
		JobPropertyUtil.setJobFullList(managerName, list);

		for (JobTreeItem child : children) {
			setJobFullTree(managerName, child);
		}
	}

	public static void setJobFullList (String managerName, List<JobInfo> jobList) {
		List<JobInfo> notFullList = new ArrayList<JobInfo>();
		List<JobInfo> fullList = null;
		
		for (JobInfo jobInfo : jobList) {
			if (jobInfo.isPropertyFull()) {
				continue;
			}
			if (jobInfo.getId() == null || "".equals(jobInfo.getId())) {
				return;
			}
			notFullList.add(jobInfo);
		}
		
		if (notFullList.size() == 0) {
			return;
		}
		
		try {
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
			fullList = wrapper.getJobFullList(notFullList);
		} catch (NotifyNotFound_Exception e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		} catch (UserNotFound_Exception e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		} catch (HinemosUnknown_Exception e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		} catch (InvalidRole_Exception e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		} catch (Exception e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		}
		
		for (JobInfo fullInfo : fullList) {
			for (JobInfo notFullInfo : notFullList) {
				if (notFullInfo.getId().equals(fullInfo.getId())) {
					copy(fullInfo, notFullInfo);
				}
			}
		}
	}
	
	/**
	 * @param jobInfo
	 */
	public static void setJobFull (String managerName, JobInfo jobInfo) {
		if (jobInfo.isPropertyFull()) {
			return;
		}
		if (jobInfo.getId() == null || "".equals(jobInfo.getId())) {
			return;
		}

		JobInfo ret = null;
		try {
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
			ret = wrapper.getJobFull(jobInfo);
		} catch (JobMasterNotFound_Exception e) {
			m_log.warn("setJobFull(), " + e.getMessage(), e);
			// 新ジョブの場合は、必ず通る。
			return;
		} catch (NotifyNotFound_Exception e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		} catch (UserNotFound_Exception e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		} catch (HinemosUnknown_Exception e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		} catch (InvalidRole_Exception e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		} catch (Exception e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		}

		copy(ret, jobInfo);
	}
	
	private static void copy(JobInfo srcInfo, JobInfo dstInfo) {
		dstInfo.setCommand(srcInfo.getCommand());
		//		jobInfo.seto
		dstInfo.setCreateTime(srcInfo.getCreateTime());
		dstInfo.setCreateUser(srcInfo.getCreateUser());
		dstInfo.setDescription(srcInfo.getDescription());
		dstInfo.setOwnerRoleId(srcInfo.getOwnerRoleId());
		dstInfo.setRegisteredModule(srcInfo.isRegisteredModule());
		List<JobEndStatusInfo> jobEndStatusInfoList = dstInfo.getEndStatus();
		jobEndStatusInfoList.clear();
		if (srcInfo.getEndStatus() != null) {
			jobEndStatusInfoList.addAll(srcInfo.getEndStatus());
		}
		dstInfo.setFile(srcInfo.getFile());
		// jobInfo.setId(ret.getId());
		// jobInfo.setJobunitId(ret.getJobunitId());
		// jobInfo.setName(ret.getName());
		dstInfo.setBeginPriority(srcInfo.getBeginPriority());
		dstInfo.setNormalPriority(srcInfo.getNormalPriority());
		dstInfo.setWarnPriority(srcInfo.getWarnPriority());
		dstInfo.setAbnormalPriority(srcInfo.getAbnormalPriority());
		dstInfo.getNotifyRelationInfos().clear();
		dstInfo.getNotifyRelationInfos().addAll(srcInfo.getNotifyRelationInfos());

		List<JobParameterInfo> jobParameterInfoList = dstInfo.getParam();
		jobParameterInfoList.clear();
		if (srcInfo.getParam() != null) {
			jobParameterInfoList.addAll(srcInfo.getParam());
		}
		dstInfo.setPropertyFull(true);
		dstInfo.setType(srcInfo.getType());
		dstInfo.setUpdateTime(srcInfo.getUpdateTime());
		dstInfo.setUpdateUser(srcInfo.getUpdateUser());
		dstInfo.setWaitRule(srcInfo.getWaitRule());

		//参照ジョブ
		if(srcInfo.getReferJobUnitId() != null){
			dstInfo.setReferJobUnitId(srcInfo.getReferJobUnitId());
		}
		if(srcInfo.getReferJobId() != null){
			dstInfo.setReferJobId(srcInfo.getReferJobId());
		}
		if(srcInfo.getReferJobSelectType() != null){
			dstInfo.setReferJobSelectType(srcInfo.getReferJobSelectType());
		}
		//承認ジョブ
		if(srcInfo.getApprovalReqRoleId() != null){
			dstInfo.setApprovalReqRoleId(srcInfo.getApprovalReqRoleId());
		}
		if(srcInfo.getApprovalReqUserId() != null){
			dstInfo.setApprovalReqUserId(srcInfo.getApprovalReqUserId());
		}
		if(srcInfo.getApprovalReqSentence() != null){
			dstInfo.setApprovalReqSentence(srcInfo.getApprovalReqSentence());
		}
		if(srcInfo.getApprovalReqMailTitle() != null){
			dstInfo.setApprovalReqMailTitle(srcInfo.getApprovalReqMailTitle());
		}
		if(srcInfo.getApprovalReqMailBody() != null){
			dstInfo.setApprovalReqMailBody(srcInfo.getApprovalReqMailBody());
		}
		dstInfo.setUseApprovalReqSentence(srcInfo.isUseApprovalReqSentence());
		// 監視ジョブ
		dstInfo.setMonitor(srcInfo.getMonitor());
	}

	public static String getManagerName (Property property) {
		ArrayList<?> values = null;

		String managerName = null;
		//開始・再実行日時（自）取得
		values = PropertyUtil.getPropertyValue(property, HistoryFilterPropertyConstant.MANAGER);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			managerName = (String)values.get(0);
		}

		return managerName;
	}
	
	/**
	 * 特定のジョブユニット配下に関して、JobTreeItemキャッシュ中のpropertyFullをクリアする
	 */
	public static void clearPropertyFull( JobTreeItem item, String jobunitId ){
		for( JobTreeItem jobunitItem : item.getChildren() ){
			if( jobunitItem.getData().getJobunitId().equals(jobunitId) ){
				jobunitItem.getData().setPropertyFull(false);
				for( JobTreeItem child : jobunitItem.getChildren() ){
					child.getData().setPropertyFull(false);
					clearPropertyFullRecursive(child);
				}
			}
		}
	}

	private static void clearPropertyFullRecursive( JobTreeItem parentItem ){
		for( JobTreeItem item : parentItem.getChildren() ){
			item.getData().setPropertyFull(false);
			clearPropertyFullRecursive(item);

		}
	}

}
