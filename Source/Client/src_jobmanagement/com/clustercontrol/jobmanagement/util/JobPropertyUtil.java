/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.GetJobFullListRequest;
import org.openapitools.client.model.GetPlanListRequest;
import org.openapitools.client.model.JobEndStatusInfoResponse;
import org.openapitools.client.model.JobInfoRequestP1;
import org.openapitools.client.model.JobInfoResponse;
import org.openapitools.client.model.JobLinkMessageFilterRequest;
import org.openapitools.client.model.JobLinkMessageFilterRequest.PriorityListEnum;
import org.openapitools.client.model.JobOperationRequest.ControlEnum;
import org.openapitools.client.model.JobOperationRequest.EndStatusEnum;
import org.openapitools.client.model.JobParameterInfoResponse;

import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.jobmanagement.OperationMessage;
import com.clustercontrol.jobmanagement.bean.HistoryFilterPropertyConstant;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageFilterPropertyConstant;
import com.clustercontrol.jobmanagement.bean.JobOperationConstant;
import com.clustercontrol.jobmanagement.bean.PlanFilterPropertyConstant;
import com.clustercontrol.monitor.bean.EventFilterConstant;
import com.clustercontrol.repository.util.FacilityTreeItemResponse;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.TimezoneUtil;

public class JobPropertyUtil {
	// ログ
	private static Log m_log = LogFactory.getLog( JobPropertyUtil.class );

	public static JobOperationRequestWrapper property2jobOperation (Property property) {
		JobOperationRequestWrapper info = new JobOperationRequestWrapper();

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
		ControlEnum control = null;
		if(values.get(0) instanceof String){
			String controlString = (String)values.get(0);
			control = OperationMessage.stringToEnum(controlString);
			info.setControl(control);
		}

		if (control == null)
			throw new InternalError("control is null , controlString : " + values.get(0));
		
		//終了状態取得
		values = PropertyUtil.getPropertyValue(property, JobOperationConstant.END_STATUS);
		if(values.size() > 0 && values.get(0) instanceof EndStatusEnum)
			info.setEndStatus((EndStatusEnum)values.get(0));

		//終了値取得
		values = PropertyUtil.getPropertyValue(property, JobOperationConstant.END_VALUE);
		if(values.size() > 0 && values.get(0) instanceof Integer)
			info.setEndValue((Integer)values.get(0));

		// #2360における4.1.0のHinemosマネージャとの互換性のため、停止[スキップ]の場合は、デフォルト値(終了状態:異常、終了値:0)を入れる
		// 4.1.1以降のHinemosマネージャではこの値はマネージャ側で破棄される
		if (control == ControlEnum.STOP_SKIP) {
			info.setEndStatus(EndStatusEnum.ABNORMAL);
			info.setEndValue(EndStatusConstant.INITIAL_VALUE_NORMAL);
		}

		return info;
	}

	public static GetPlanListRequest property2jobPlanFilter (Property property) {
		GetPlanListRequest filter = new GetPlanListRequest();
		ArrayList<?> values = null;

		//開始取得
		values = PropertyUtil.getPropertyValue(property, PlanFilterPropertyConstant.FROM_DATE);
		m_log.debug("JobPlanFilter property2JobPlanFilter");
		if (values.get(0) instanceof Date){
			m_log.debug("property2jobPlanFilter : fromDate=" + ((Date)values.get(0)).getTime());
			Date fromDate = ((Date)values.get(0));
			filter.setFromDate(TimezoneUtil.getSimpleDateFormat().format(fromDate));
		}
		//終了取得
		values = PropertyUtil.getPropertyValue(property, PlanFilterPropertyConstant.TO_DATE);
		if (values.get(0) instanceof Date){
			m_log.debug("property2jobPlanFilter : toDate=" + ((Date)values.get(0)).getTime());
			Date toDate = ((Date)values.get(0));
			filter.setToDate(TimezoneUtil.getSimpleDateFormat().format(toDate));
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
	 * 受信ジョブ連携メッセージ一覧フィルタ用のプロパティからBeanに変換する
	 * 
	 * @param property プロパティ
	 * @return Bean
	 */
	public static JobLinkMessageFilterRequest property2jobLinkMessageFilter (Property property) {
		JobLinkMessageFilterRequest filter = new JobLinkMessageFilterRequest();
		ArrayList<?> values = null;

		// 検索結果の日時はyyyy/MM/dd HH:mm:ss.SSSのフォーマット
		SimpleDateFormat dateFormat = new SimpleDateFormat(JobRestClientWrapper.DATETIME_FORMAT);
		dateFormat.setTimeZone(TimezoneUtil.getTimeZone());

		// ジョブ連携メッセージID
		values = PropertyUtil.getPropertyValue(property, JobLinkMessageFilterPropertyConstant.JOBLINK_MESSAGE_ID);
		if(!"".equals(values.get(0))) {
			filter.setJoblinkMessageId((String)values.get(0));
		}
		// 送信元ファシリティID
		values = PropertyUtil.getPropertyValue(property, JobLinkMessageFilterPropertyConstant.SRC_FACILITY_ID);
		if(values.get(0) != null 
				&& (values.get(0) instanceof FacilityTreeItemResponse)
				&& ((FacilityTreeItemResponse)values.get(0)).getData() != null) {
			filter.setSrcFacilityId(((FacilityTreeItemResponse)values.get(0)).getData().getFacilityId());
		}
		// 送信元ファシリティ名
		values = PropertyUtil.getPropertyValue(property, JobLinkMessageFilterPropertyConstant.SRC_FACILITY_NAME);
		if(!"".equals(values.get(0))) {
			filter.setSrcFacilityName((String)values.get(0));
		}
		// 監視詳細
		values = PropertyUtil.getPropertyValue(property, JobLinkMessageFilterPropertyConstant.MONITOR_DETAIL_ID);
		if(!"".equals(values.get(0))) {
			filter.setMonitorDetailId((String)values.get(0));
		}
		// アプリケーション
		values = PropertyUtil.getPropertyValue(property, JobLinkMessageFilterPropertyConstant.APPLICATION);
		if(!"".equals(values.get(0))) {
			filter.setApplication((String)values.get(0));
		}
		// 重要度リスト
		values = PropertyUtil.getPropertyValue(property, EventFilterConstant.PRIORITY_CRITICAL);
		List<PriorityListEnum> priorityList = new ArrayList<>();
		if (!"".equals(values.get(0)) && (Boolean)values.get(0)) {
			priorityList.add(PriorityListEnum.CRITICAL);
		}
		values = PropertyUtil.getPropertyValue(property, EventFilterConstant.PRIORITY_WARNING);
		if (!"".equals(values.get(0)) && (Boolean)values.get(0)) {
			priorityList.add(PriorityListEnum.WARNING);
		}
		values = PropertyUtil.getPropertyValue(property, EventFilterConstant.PRIORITY_INFO);
		if (!"".equals(values.get(0)) && (Boolean)values.get(0)) {
			priorityList.add(PriorityListEnum.INFO);
		}
		values = PropertyUtil.getPropertyValue(property, EventFilterConstant.PRIORITY_UNKNOWN);
		if (!"".equals(values.get(0)) && (Boolean)values.get(0)) {
			priorityList.add(PriorityListEnum.UNKNOWN);
		}
		filter.setPriorityList(priorityList);
		// メッセージ
		values = PropertyUtil.getPropertyValue(property, JobLinkMessageFilterPropertyConstant.MESSAGE);
		if(!"".equals(values.get(0))) {
			filter.setMessage((String)values.get(0));
		}
		// 送信日時（From）
		values = PropertyUtil.getPropertyValue(property, JobLinkMessageFilterPropertyConstant.SEND_FROM_DATE);
		if(values.get(0) != null && values.get(0) instanceof Date){
			filter.setSendFromDate(dateFormat.format( ((Date)values.get(0)).getTime()));
		}
		// 送信日時（To）
		values = PropertyUtil.getPropertyValue(property, JobLinkMessageFilterPropertyConstant.SEND_TO_DATE);
		if(values.get(0) != null && values.get(0) instanceof Date){
			filter.setSendToDate(dateFormat.format( ((Date)values.get(0)).getTime()));
		}
		// 受信日時（From）
		values = PropertyUtil.getPropertyValue(property, JobLinkMessageFilterPropertyConstant.ACCEPT_FROM_DATE);
		if(values.get(0) != null && values.get(0) instanceof Date){
			filter.setAcceptFromDate(dateFormat.format( ((Date)values.get(0)).getTime()));
		}
		// 受信日時（To）
		values = PropertyUtil.getPropertyValue(property, JobLinkMessageFilterPropertyConstant.ACCEPT_TO_DATE);
		if(values.get(0) != null && values.get(0) instanceof Date){
			filter.setAcceptToDate(dateFormat.format( ((Date)values.get(0)).getTime()));
		}
		return filter;
	}

	/**
	 * ジョブツリーを全てFullにする。
	 * 負荷が高いので取り扱い注意。
	 * @param jobTreeItem
	 */
	public static void setJobFullTree (String managerName, JobTreeItemWrapper jobTreeItem) {
		List<JobTreeItemWrapper> children = jobTreeItem.getChildren();
		if (children == null) {
			return;
		}
		
		// FullJob
		List<JobInfoWrapper> list = new ArrayList<JobInfoWrapper>();
		list.add(jobTreeItem.getData()); // Include root's FullJob also
		for (JobTreeItemWrapper info : children) {
			list.add(info.getData());
		}
		JobPropertyUtil.setJobFullList(managerName, list);

		for (JobTreeItemWrapper child : children) {
			setJobFullTree(managerName, child);
		}
	}

	public static void setJobFullList (String managerName, List<JobInfoWrapper> jobList) {
		List<JobInfoWrapper> notFullList = new ArrayList<JobInfoWrapper>();
		List<JobInfoWrapper> fullList = null;
		
		for (JobInfoWrapper jobInfo : jobList) {
			if (jobInfo.getPropertyFull()) {
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
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			GetJobFullListRequest request = new GetJobFullListRequest();
			request.setJobList(new ArrayList<JobInfoRequestP1>());
			for( JobInfoWrapper orgRec : notFullList ){
				JobInfoRequestP1 findRec = new JobInfoRequestP1();
				findRec.setId(orgRec.getId());
				findRec.setJobunitId(orgRec.getJobunitId());
				request.getJobList().add(findRec);
			}
			
			List<JobInfoResponse> fullListRes = wrapper.getJobFullList(request);
			fullList  = JobTreeItemUtil.getInfoListFromDtoList(fullListRes);
			
		} catch (NotifyNotFound e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		} catch (UserNotFound e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		} catch (HinemosUnknown e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		} catch (InvalidRole e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		} catch (Exception e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		}
		
		for (JobInfoWrapper fullInfo : fullList) {
			for (JobInfoWrapper notFullInfo : notFullList) {
				if (notFullInfo.getId().equals(fullInfo.getId())) {
					JobTreeItemUtil.paddingJobInfoWrapper(fullInfo);
					copy(fullInfo, notFullInfo);
				}
			}
		}
	}
	
	/**
	 * @param jobInfo
	 */
	public static void setJobFull (String managerName, JobInfoWrapper jobInfo) {
		if (jobInfo.getPropertyFull()) {
			return;
		}
		if (jobInfo.getId() == null || "".equals(jobInfo.getId())) {
			return;
		}

		JobInfoWrapper ret = null;
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			JobInfoResponse retDto = wrapper.getJobFull(jobInfo.getJobunitId(),jobInfo.getId());
			ret = JobTreeItemUtil.getInfoFromDto(retDto);
		} catch (JobMasterNotFound e) {
			m_log.warn("setJobFull(), " + e.getMessage(), e);
			// 新ジョブの場合は、必ず通る。
			return;
		} catch (NotifyNotFound e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		} catch (UserNotFound e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		} catch (HinemosUnknown e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		} catch (InvalidRole e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		} catch (Exception e) {
			m_log.warn("setJobFull() getJobFull, " + e.getMessage(), e);
			return;
		}

		JobTreeItemUtil.paddingJobInfoWrapper(ret);
		copy(ret, jobInfo);
	}
	
	private static void copy(JobInfoWrapper srcInfo, JobInfoWrapper dstInfo) {
		dstInfo.setCommand(srcInfo.getCommand());
		//		jobInfo.seto
		dstInfo.setCreateTime(srcInfo.getCreateTime());
		dstInfo.setCreateUser(srcInfo.getCreateUser());
		dstInfo.setDescription(srcInfo.getDescription());
		dstInfo.setOwnerRoleId(srcInfo.getOwnerRoleId());
		dstInfo.setRegistered(srcInfo.getRegistered());
		List<JobEndStatusInfoResponse> jobEndStatusInfoList = dstInfo.getEndStatus();
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

		List<JobParameterInfoResponse> jobParameterInfoList = dstInfo.getParam();
		
		jobParameterInfoList.clear();
		if (srcInfo.getParam() != null) {
			jobParameterInfoList.addAll(srcInfo.getParam());
		}
		dstInfo.setPropertyFull(true);
		dstInfo.setType(srcInfo.getType());
		dstInfo.setUpdateTime(srcInfo.getUpdateTime());
		dstInfo.setUpdateUser(srcInfo.getUpdateUser());

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
		dstInfo.setIsUseApprovalReqSentence(srcInfo.getIsUseApprovalReqSentence());
		// 監視ジョブ
		dstInfo.setMonitor(srcInfo.getMonitor());
		// ファイルチェックジョブ
		dstInfo.setJobFileCheck(srcInfo.getJobFileCheck());
		// ジョブ連携送信ジョブ
		dstInfo.setJobLinkSend(srcInfo.getJobLinkSend());
		// ジョブ連携待機ジョブ
		dstInfo.setJobLinkRcv(srcInfo.getJobLinkRcv());
		// リソース制御ジョブ
		dstInfo.setResource(srcInfo.getResource());
		// RPAシナリオジョブ
		dstInfo.setRpa(srcInfo.getRpa());
		// ジョブユニット
		if(srcInfo.getExpNodeRuntimeFlg() != null){
			dstInfo.setExpNodeRuntimeFlg(srcInfo.getExpNodeRuntimeFlg());
		}
	}

	public static String getManagerName (Property property) {
		ArrayList<?> values = null;

		String managerName = null;
		values = PropertyUtil.getPropertyValue(property, HistoryFilterPropertyConstant.MANAGER);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			managerName = (String)values.get(0);
		}

		return managerName;
	}
	
	/**
	 * 特定のジョブユニット配下に関して、JobTreeItemキャッシュ中のpropertyFullをクリアする
	 */
	public static void clearPropertyFull( JobTreeItemWrapper item, String jobunitId ){
		for( JobTreeItemWrapper jobunitItem : item.getChildren() ){
			if( jobunitItem.getData().getJobunitId().equals(jobunitId) ){
				jobunitItem.getData().setPropertyFull(false);
				for( JobTreeItemWrapper child : jobunitItem.getChildren() ){
					child.getData().setPropertyFull(false);
					clearPropertyFullRecursive(child);
				}
			}
		}
	}

	private static void clearPropertyFullRecursive( JobTreeItemWrapper parentItem ){
		for( JobTreeItemWrapper item : parentItem.getChildren() ){
			item.getData().setPropertyFull(false);
			clearPropertyFullRecursive(item);

		}
	}

}
