/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.jobmanagement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidApprovalStatus;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobInvalid;
import com.clustercontrol.fault.JobKickDuplicate;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.JobQueueNotFound;
import com.clustercontrol.fault.JobSessionDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.OtherUserGetLock;
import com.clustercontrol.fault.UpdateTimeNotLatest;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.jobmanagement.bean.JobApprovalFilter;
import com.clustercontrol.jobmanagement.bean.JobApprovalInfo;
import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.jobmanagement.bean.JobForwardFile;
import com.clustercontrol.jobmanagement.bean.JobHistoryFilter;
import com.clustercontrol.jobmanagement.bean.JobHistoryList;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobKick;
import com.clustercontrol.jobmanagement.bean.JobKickFilterInfo;
import com.clustercontrol.jobmanagement.bean.JobNodeDetail;
import com.clustercontrol.jobmanagement.bean.JobOperationInfo;
import com.clustercontrol.jobmanagement.bean.JobPlan;
import com.clustercontrol.jobmanagement.bean.JobPlanFilter;
import com.clustercontrol.jobmanagement.bean.JobSchedule;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobmapIconImage;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueActivityViewFilter;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueActivityViewInfo;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueContentsViewInfo;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueReferrerViewInfo;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueSetting;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueSettingViewFilter;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueSettingViewInfo;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * ジョブ操作用のWebAPIエンドポイント
 */
@javax.jws.WebService(targetNamespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( JobEndpoint.class );
	private static Log m_opelog = LogFactory.getLog("HinemosOperation");

	/**
	 * echo(WebサービスAPI疎通用)
	 *
	 * 権限必要なし（ユーザ名チェックのみ実施）
	 *
	 * @param str
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public String echo(String str) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return str + ", " + str;
	}


	/**
	 * ジョブツリー情報を取得する。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param ownerRoleId
	 * @param treeOnly
	 * @throws UserNotFound
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public JobTreeItem getJobTree(String ownerRoleId, boolean treeOnly) throws NotifyNotFound, HinemosUnknown, JobMasterNotFound, UserNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("getJobTree : treeOnly=" + treeOnly);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", TreeOnly=");
		msg.append(treeOnly);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobTree, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getJobTree(ownerRoleId, treeOnly, Locale.getDefault());
	}

	/**
	 * ジョブ情報の詳細を取得する。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param jobInfo ジョブ情報(ツリー情報のみ)
	 * @return ジョブ情報(Full)
	 * @throws JobMasterNotFound
	 * @throws UserNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public JobInfo getJobFull(JobInfo jobInfo) throws JobMasterNotFound, UserNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		if (jobInfo == null) {
			throw new HinemosUnknown("jobInfo is null");
		}
		m_log.debug("getJobFull : jobunitId =" + jobInfo.getJobunitId() + ", id = " + jobInfo.getId());

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobID=");
		msg.append(jobInfo.getId());
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobFull, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getJobFull(jobInfo);
	}

	/**
	 * ジョブ情報の詳細を取得する。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param jobInfo ジョブ情報(ツリー情報のみ)
	 * @return ジョブ情報(Full)
	 * @throws UserNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public List<JobInfo> getJobFullList(List<JobInfo> jobList) throws UserNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		
		StringBuilder idStr = new StringBuilder();
		for (JobInfo info : jobList) {
			if (idStr.length() > 0) {
				idStr.append(", ");
			}
			idStr.append(info.getId());
		}
		
		m_log.debug("getJobFullList : id=" + idStr);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobID=");
		msg.append(idStr);
			m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobFull, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
		
		return new JobControllerBean().getJobFullList(jobList);
	}
	
	/**
	 * ジョブユニット情報を登録する。<BR>
	 *
	 * JobManagementAdd権限とJobManagementWrite権限が必要
	 *
	 * @param item ジョブユニット情報{@link com.clustercontrol.jobmanagement.bean.JobTreeItem}の階層オブジェクト
	 * @throws JobInvalid
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJob#registerJob(JobTreeItem, String)
	 */
	public Long registerJobunit(JobTreeItem item) throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting {
		long start = HinemosTime.currentTimeMillis();
		String id = null;
		if (item != null && item.getData() != null) {
			id = item.getData().getId();
		}
		m_log.debug("registerJobunit : Id=" + id + ", item="+ item);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.ADD));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);


		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if (item != null && item.getData() != null) {
			msg.append(", JobunitID=");
			msg.append(item.getData().getId());
		}
		
		Long lastUpdateTime = null;
		
		try {
			lastUpdateTime = new JobControllerBean().registerJobunit(item);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Set Jobunit Failed, Method=registerJobunit, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Set Jobunit, Method=registerJobunit, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		m_log.debug(String.format("registerJobunit: %d ms", HinemosTime.currentTimeMillis() - start));
		
		return lastUpdateTime;
	}

	/**
	 * ジョブユニット単位でジョブツリー情報を削除する。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @param jobunitId 削除対象ジョブユニットのジョブID
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 */
	public void deleteJobunit(String jobunitId) throws HinemosUnknown, JobMasterNotFound, InvalidUserPass, InvalidRole, InvalidSetting, JobInvalid {
		m_log.debug("deleteJobunit : Id=" + jobunitId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobunitID=");
		msg.append(jobunitId);

		try {
			new JobControllerBean().deleteJobunit(jobunitId);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Delete Jobunit Failed, Method=deleteJobunit, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Delete Jobunit, Method=deleteJobunit, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}


	/**
	 * ジョブ操作開始用プロパティを返します。<BR>
	 *
	 * JobManagementExecute権限が必要
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @param locale ロケール情報
	 * @return ジョブ操作開始用プロパティ
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.jobmanagement.factory.JobOperationProperty#getStartProperty(String, String, String, String, Locale)
	 */
	public ArrayList<Integer> getAvailableStartOperation(String sessionId, String jobunitId, String jobId, String facilityId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getAvailableStartOperation : sessionId=" + sessionId +
				", jobunitId=" + jobunitId + ", jobId=" + jobId + ", facilityId=" + facilityId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.EXEC));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SessionID=");
		msg.append(sessionId);
		msg.append(", JobunitID=");
		msg.append(jobunitId);
		msg.append(", JobID=");
		msg.append(jobId);
		msg.append(", FacilityID=");
		msg.append(facilityId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getAvailableStartOperation, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getAvailableStartOperation(sessionId, jobunitId, jobId, facilityId, Locale.getDefault());
	}

	/**
	 * ジョブ操作停止用プロパティを返します。<BR>
	 *
	 * JobManagementExecute権限が必要
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @param locale ロケール情報
	 * @return ジョブ操作停止用プロパティ
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.jobmanagement.factory.JobOperationProperty#getStopProperty(String, String, String, String, Locale)
	 */
	public ArrayList<Integer> getAvailableStopOperation(String sessionId, String jobunitId,  String jobId, String facilityId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getAvailableStopOperation : sessionId=" + sessionId +
				", jobunitId=" + jobunitId + ", jobId=" + jobId + ", facilityId=" + facilityId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.EXEC));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SessionID=");
		msg.append(sessionId);
		msg.append(", JobunitID=");
		msg.append(jobunitId);
		msg.append(", JobID=");
		msg.append(jobId);
		msg.append(", FacilityID=");
		msg.append(facilityId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getAvailableStopOperation, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getAvailableStopOperation(sessionId, jobunitId, jobId, facilityId, Locale.getDefault());
	}

	/**
	 * ジョブを実行します。<BR>
	 *
	 * JobManagementExecute権限が必要
	 *
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param info ログ出力情報
	 * @param triggerInfo 実行契機情報
	 * @throws JobMasterNotFound
	 * @throws JobInfoNotFound
	 * @throws HinemosUnknown
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * @see com.clustercontrol.jobmanagement.ejb.session.JobControllerBean#createJobInfo(String, String, NotifyRequestMessage, JobTriggerInfo}
	 * @see com.clustercontrol.jobmanagement.session.JobRunManagementBean#runJob(String, String)
	 */
	public String runJob(String jobunitId, String jobId, OutputBasicInfo info, JobTriggerInfo triggerInfo)
			throws  FacilityNotFound, HinemosUnknown, JobInfoNotFound, JobMasterNotFound, InvalidUserPass, InvalidRole, JobSessionDuplicate, InvalidSetting
	{
		if (triggerInfo==null)
			throw new HinemosUnknown("triggerInfo is null. jobId" + jobId);
		
		m_log.debug("runJob : jobunitId=" + jobunitId + ", jobId=" + jobId + ", info=" + info +
				", triggerInfo=" + triggerInfo);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.EXEC));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// テスト実行時のシステム権限チェック
		if (triggerInfo.getJobWaitTime() || triggerInfo.getJobWaitMinute() || triggerInfo.getJobCommand()) {
			systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
			HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		}

		String ret = null;

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobID=");
		msg.append(jobId);
		msg.append(", Trigger=");
		msg.append(triggerTypeToString(triggerInfo.getTrigger_type()));

		try {
			ret = new JobControllerBean().runJob(jobunitId, jobId, info, triggerInfo);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Run Job Failed, Method=runJob, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Run Job, Method=runJob, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return ret;
	}

	/**
	 * ジョブ操作を行います。<BR>
	 *
	 * JobManagementExecute権限が必要
	 *
	 * @param property ジョブ操作用プロパティ
	 * @throws JobInfoNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @see com.clustercontrol.jobmanagement.session.JobRunManagementBean#operationJob(JobOperationInfo)
	 */
	public void operationJob(JobOperationInfo property) throws HinemosUnknown, JobInfoNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("operationJob : nodeOperationInfo=" + property);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.EXEC));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SessionID=");
		if(property != null){
			msg.append(property.getSessionId());
			msg.append(", JobID=");
			msg.append(property.getJobId());
			msg.append(", FacilityID=");
			msg.append(property.getFacilityId()==null?"(not set)":property.getFacilityId());
			msg.append(", Operation=");
			msg.append(getOperationString(property.getControl()));
			msg.append(", EndStatus=");
			msg.append(property.getEndStatus()==null?"(not set)":property.getEndStatus());
			msg.append(", EndValue=");
			msg.append(property.getEndValue()==null?"(not set)":property.getEndValue());
		}

		try {
			new JobControllerBean().operationJob(property);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Operate Job Failed, Method=operationJob, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Operate Job, Method=operationJob, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}


	/**
	 * ジョブ履歴一覧情報を返します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param property 履歴フィルタ用プロパティ
	 * @param histories 表示履歴数
	 * @return ジョブ履歴一覧情報（Objectの2次元配列）
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getHistoryList(JobHistoryFilter, int)
	 */
	public JobHistoryList getJobHistoryList(JobHistoryFilter property, int histories) throws JobInfoNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getHistoryList : jobHistoryFilter=" + property + ", histories=" + histories);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(property != null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			sdf.setTimeZone(HinemosTime.getTimeZone());
			msg.append(", StartFromDate=");
			msg.append(property.getStartFromDate()==null?null:sdf.format(new Date(property.getStartFromDate())));
			msg.append(", StartToDate=");
			msg.append(property.getStartToDate()==null?null:sdf.format(new Date(property.getStartToDate())));
			msg.append(", EndFromDate=");
			msg.append(property.getEndFromDate()==null?null:sdf.format(new Date(property.getEndFromDate())));
			msg.append(", EndToDate=");
			msg.append(property.getEndToDate()==null?null:sdf.format(new Date(property.getEndToDate())));
			msg.append(", JobID=");
			msg.append(property.getJobId());
			msg.append(", Status=");
			msg.append(property.getStatus());
			msg.append(", EndStatus=");
			msg.append(property.getEndStatus());
			msg.append(", TriggerType=");
			msg.append(property.getTriggerType());
			msg.append(", TriggerInfo=");
			msg.append(property.getTriggerInfo());
			msg.append(", OwnerRoleId=");
			msg.append(property.getOwnerRoleId());
		}
		msg.append(", Count=");
		msg.append(histories);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobHistoryList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getJobHistoryList(property, histories);
	}

	/**
	 * ジョブ詳細一覧情報を返します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param sessionId セッションID
	 * @return ジョブ詳細一覧情報（Objectの2次元配列）
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getDetailList(String)
	 */
	public JobTreeItem getJobDetailList(String sessionId) throws JobInfoNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SessionID=");
		msg.append(sessionId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobDetailList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getJobDetailList(sessionId);
	}

	/**
	 * ノード詳細一覧情報を返します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param locale ロケール情報
	 * @return ノード詳細一覧情報（Objectの2次元配列）
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws JobInfoNotFound
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getNodeDetailList(String, String, String, Locale)
	 */
	public ArrayList<JobNodeDetail> getNodeDetailList(String sessionId, String jobunitId, String jobId) throws InvalidUserPass, InvalidRole, HinemosUnknown, JobInfoNotFound {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SessionID=");
		msg.append(sessionId);
		msg.append(", JobunitID=");
		msg.append(jobunitId);
		msg.append(", JobID=");
		msg.append(jobId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getNodeDetailList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getNodeDetailList(sessionId, jobunitId, jobId, Locale.getDefault());
	}

	/**
	 * ファイル転送一覧情報を返します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @return ファイル転送一覧情報（Objectの2次元配列）
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getForwardFileList(String, String)
	 */
	public ArrayList<JobForwardFile> getForwardFileList(String sessionId, String jobunitId, String jobId) throws JobInfoNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SessionID=");
		msg.append(sessionId);
		msg.append(", JobunitID=");
		msg.append(jobunitId);
		msg.append(", JobID=");
		msg.append(jobId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getForwardFileList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getForwardFileList(sessionId, jobunitId, jobId);
	}

	/**
	 * スケジュール情報を登録します。<BR>
	 *
	 * JobManagementAdd権限が必要
	 *
	 * @param info スケジュール情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#addJobKick(JobKick, String, Integer)
	 */
	public void addSchedule(JobSchedule info) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobKickDuplicate {
		m_log.debug("addSchedule : jobSchedule=" + info);
		if (info == null)
			throw new HinemosUnknown("jobSchedule is null");
			
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobkickID=");
		msg.append(info.getId());

		try {
			new JobControllerBean().addSchedule(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Add Schedule Failed, Method=addSchedule, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Add Schedule, Method=addSchedule, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	/**
	 * ジョブ[実行契機]ファイルチェック情報を登録します。<BR>
	 *
	 * JobManagementAdd権限が必要
	 *
	 * @param info ジョブ[実行契機]ファイルチェック情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#addJobKick(JobKick, String, Integer)
	 */
	public void addFileCheck(JobFileCheck info) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobKickDuplicate {
		m_log.debug("addFileCheck : jobFileCheck=" + info);
		if (info == null)
			throw new HinemosUnknown("JobFileCheck is null");
		
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobkickID=");
		msg.append(info.getId());

		try {
			new JobControllerBean().addFileCheck(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Add FileCheck Failed, Method=addFileCheck, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Add FileCheck, Method=addFileCheck, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	/**
	 * ジョブ[実行契機]マニュアル実行契機情報を登録します。<BR>
	 *
	 * JobManagementAdd権限が必要
	 *
	 * @param info ジョブ[実行契機]マニュアル実行契機情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 *
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#addJobKick(JobKick, String, Integer)
	 */
	public void addJobManual(JobKick info) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobKickDuplicate {
		m_log.debug("addJobManual : JobManual=" + info);
		if (info == null)
			throw new HinemosUnknown("JobManual is null");
		
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobkickID=");
		msg.append(info.getId());

		try {
			new JobControllerBean().addJobManual(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Add JobManual Failed, Method=addJobManual, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Add JobManual, Method=addJobManual, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}


	/**
	 * スケジュール情報を変更します。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @param info スケジュール情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#modifyJobKick(JobKick, String, Integer)
	 */
	public void modifySchedule(JobSchedule info) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobInfoNotFound {
		m_log.debug("modifySchedule : jobSchedule=" + info);
		if (info == null)
			throw new HinemosUnknown("jobSchedule is null");
		
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobkickID=");
		msg.append(info.getId());

		try {
			new JobControllerBean().modifySchedule(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Change Schedule Failed, Method=modifySchedule, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Change Schedule, Method=modifySchedule, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	/**
	 * ジョブ[実行契機]ファイルチェック情報を変更します。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @param info ジョブ[実行契機]ファイルチェック情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#modifyJobKick(JobKick, String, Integer)
	 */
	public void modifyFileCheck(JobFileCheck info) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobInfoNotFound {
		m_log.debug("modifyFileCheck : jobSchedule=" + info);
		if (info == null)
			throw new HinemosUnknown("jobFileCheck is null");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobkickID=");
		msg.append(info.getId());

		try {
			new JobControllerBean().modifyFileCheck(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Change FileCheck Failed, Method=modifyFileCheck, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Change FileCheck, Method=modifyFileCheck, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	/**
	 * ジョブ[実行契機]マニュアル実行契機情報を変更します。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @param info ジョブ[実行契機]マニュアル実行契機情報
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#modifyJobKick(JobKick, String, Integer)
	 */
	public void modifyJobManual(JobKick info) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobInfoNotFound {
		m_log.debug("modifyJobManual : jobManual=" + info);
		if (info == null)
			throw new HinemosUnknown("jobManual is null");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobkickID=");
		msg.append(info.getId());

		try {
			new JobControllerBean().modifyJobManual(info);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Change JobManual Failed, Method=modifyJobManual, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Change JobManual, Method=modifyJobManual, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	/**
	 * ジョブ[実行契機]スケジュール情報を削除します。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @param jobkickIdList 実行契機IDリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#deleteJobKick(String, Integer)
	 */
	public void deleteSchedule(List<String> jobkickIdList) throws HinemosUnknown, InvalidUserPass, InvalidRole, JobInfoNotFound {
		m_log.debug("deleteSchedule : jobkickId=" + jobkickIdList);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobkickIDList=");
		msg.append(jobkickIdList);

		try {
			new JobControllerBean().deleteSchedule(jobkickIdList);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Delete Schedule Failed, Method=deleteSchedule, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Delete Schedule, Method=deleteSchedule, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}

	/**
	 * ジョブ[実行契機]ファイルチェック情報を削除します。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @param jobkickIdList 実行契機IDリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#deleteJobKick(String, Integer)
	 */
	public void deleteFileCheck(List<String> jobkickIdList) throws HinemosUnknown, InvalidUserPass, InvalidRole, JobInfoNotFound {
		m_log.debug("deleteFileCheck : jobkickId=" + jobkickIdList);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobkickID=");
		msg.append(jobkickIdList);

		try {
			new JobControllerBean().deleteFileCheck(jobkickIdList);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Delete FileCheck Failed, Method=deleteFileCheck, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Delete FileCheck, Method=deleteFileCheck, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	/**
	 * ジョブ[実行契機]マニュアル実行契機情報を削除します。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @param jobkickIdList 実行契機IDリスト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 *
	 * @see com.clustercontrol.jobmanagement.factory.ModifyJobKick#deleteJobKick(String, Integer)
	 */
	public void deleteJobManual(List<String> jobkickIdList) throws HinemosUnknown, InvalidUserPass, InvalidRole, JobInfoNotFound {
		m_log.debug("deleteJobManual : jobkickId=" + jobkickIdList);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobkickID=");
		msg.append(jobkickIdList);

		try {
			new JobControllerBean().deleteFileCheck(jobkickIdList);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Delete FileCheck Failed, Method=deleteFileCheck, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Delete FileCheck, Method=deleteFileCheck, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	/**
	 * 実行契機IDと一致するジョブスケジュールを返します。<BR>
	 * @param jobKickId
	 * @return
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public JobSchedule getJobSchedule(String jobKickId) throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobSchedule :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobSchedule, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getJobSchedule(jobKickId);
	}
	/**
	 * 実行契機IDと一致するジョブファイルチェックを返します。<BR>
	 * @param jobKickId
	 * @return
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public JobFileCheck getJobFileCheck(String jobKickId) throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobFileCheck :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobFileCheck, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getJobFileCheck(jobKickId);
	}
	/**
	 * 実行契機IDと一致するマニュアル実行契機を返します。<BR>
	 * @param jobKickId
	 * @return
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public JobKick getJobManual(String jobKickId) throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobManual :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobManual, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getJobManual(jobKickId);
	}
	/**
	 * 実行契機IDと一致するジョブ実行契機を返します。<BR>
	 * @param jobKickId
	 * @return
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public JobKick getJobKick(String jobKickId) throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobKick :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobKick, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getJobKick(jobKickId);
	}

	/**ジョブ[実行契機]スケジュール情報の有効/無効を変更します。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @param jobkickId 実行契機ID
	 * @param validFlag 有効/無効
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	public void setJobKickStatus(String jobkickId, boolean validFlag) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobMasterNotFound, JobInfoNotFound {
		m_log.debug("setJobKickStatus : jobkickId=" + jobkickId + ", validFlag=" + validFlag);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobkickID=");
		msg.append(jobkickId);
		msg.append(", ValidFlag=");
		msg.append(validFlag);

		try {
			new JobControllerBean().setJobKickStatus(jobkickId, validFlag);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Change Valid Failed, Method=setJobKickStatus, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Change Valid, Method=setJobKickStatus, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

	}
	/**
	 * ジョブ実行契機一覧情報を返します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @return ジョブ実行契機一覧情報
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @see com.clustercontrol.jobmanagement.factory.SelectJobKick#getJobKickList()
	 */
	public ArrayList<JobKick> getJobKickList() throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobKickList :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobKickList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getJobKickList();
	}

	/**
	 * ジョブ実行契機一覧情報を返します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @return ジョブ実行契機一覧情報
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<JobKick> getJobKickListByCondition(JobKickFilterInfo condition) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobKickList(condition) :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		if(condition != null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			sdf.setTimeZone(HinemosTime.getTimeZone());
			StringBuffer msg = new StringBuffer();
			msg.append(", JobkickID=");
			msg.append(condition.getJobkickId());
			msg.append(", JobkickName=");
			msg.append(condition.getJobkickName());
			msg.append(", JobkickType=");
			msg.append(condition.getJobkickType());
			msg.append(", JobunitID=");
			msg.append(condition.getJobunitId());
			msg.append(", JobID=");
			msg.append(condition.getJobId());
			msg.append(", CalendarID=");
			msg.append(condition.getCalendarId());
			msg.append(", ValidFlg=");
			msg.append(condition.getValidFlg());
			msg.append(", RegUser=");
			msg.append(condition.getRegUser());
			msg.append(", RegFromDate=");
			msg.append(condition.getRegFromDate()==null?null:sdf.format(new Date(condition.getRegFromDate())));
			msg.append(", RegToDate=");
			msg.append(condition.getRegToDate()==null?null:sdf.format(new Date(condition.getRegToDate())));
			msg.append(", UpdateUser=");
			msg.append(condition.getUpdateUser());
			msg.append(", UpdateFromDate=");
			msg.append(condition.getUpdateFromDate()==null?null:sdf.format(new Date(condition.getUpdateFromDate())));
			msg.append(", UpdateToDate=");
			msg.append(condition.getUpdateToDate()==null?null:sdf.format(new Date(condition.getUpdateToDate())));
			msg.append(", OwnerRoleId=");
			msg.append(condition.getOwnerRoleId());
			m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get"
					+ ", Method=getJobKickListByCondition, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
		}

		return new JobControllerBean().getJobKickList(condition);
	}

	/**
	 * セッションジョブ情報を返します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @return ジョブツリー情報{@link com.clustercontrol.jobmanagement.bean.JobTreeItem}
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws JobInfoNotFound
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getSessionJobInfo(String, String, String)
	 */
	public JobTreeItem getSessionJobInfo(String sessionId, String jobunitId, String jobId) throws InvalidUserPass, InvalidRole, HinemosUnknown, JobInfoNotFound {
		m_log.debug("getSessionJobInfo : sessionId=" + sessionId + ", jobunitId=" + jobunitId +
				", jobId=" + jobId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", SessionID=");
		msg.append(sessionId);
		msg.append(", JobunitID=");
		msg.append(jobunitId);
		msg.append(", JobID=");
		msg.append(jobId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getSessionJobInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getSessionJobInfo(sessionId, jobunitId, jobId);
	}

	/**
	 * ジョブ[スケジュール予定]の一覧を返します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @return ジョブ[スケジュール予定]の一覧情報
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @see com.clustercontrol.jobmanagement.factory.SelectJobKick#getPlanList()
	 */
	public ArrayList<JobPlan> getPlanList(JobPlanFilter property, int plans) throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getPlanList :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		if(property != null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			sdf.setTimeZone(HinemosTime.getTimeZone());
			msg.append(", fromDate=");
			msg.append(property.getFromDate()==null?null:sdf.format(new Date(property.getFromDate())));
			msg.append(", toDate=");
			msg.append(property.getToDate()==null?null:sdf.format(new Date(property.getToDate())));
			msg.append(", jobKickID=");
			msg.append(property.getJobKickId()==null?null:property.getJobKickId());
		}
		msg.append(", Count=");
		msg.append(plans);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobPlanList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		return new JobControllerBean().getPlanList(property, plans);
	}

	/**
	 * 実行契機の文字列取得
	 *
	 * @param type 実行契機
	 * @return 実行契機文字列
	 */
	private String triggerTypeToString(Integer type){
		if (type != null) {
			if (type == JobTriggerTypeConstant.TYPE_SCHEDULE) {
				return "Schedule";
			} else if (type == JobTriggerTypeConstant.TYPE_MANUAL) {
				return "Manual";
			} else if (type == JobTriggerTypeConstant.TYPE_MONITOR) {
				return "Monitor";
			}
		}
		return "";
	}

	/**
	 * ログ出力用の操作名取得
	 *
	 * @param op 操作名(画面表示)
	 * @return ログ出力用操作名
	 */
	private static String getOperationString(Integer op){
		if (op ==null) {
			return "[Unknown Operation]";
		}

		switch (op.intValue()) {
		case OperationConstant.TYPE_START_AT_ONCE://1
			return "Start[Start]";

		case OperationConstant.TYPE_START_SUSPEND://3
			return "Start[Cancel Suspend]";

		case OperationConstant.TYPE_START_SKIP://5
			return "Start[Cancel Skip]";

		case OperationConstant.TYPE_START_WAIT://7
			return "Start[Cancel Pause]";

		case OperationConstant.TYPE_START_FORCE_RUN:
			return "Start[Force Run]";

		case OperationConstant.TYPE_STOP_AT_ONCE://0
			return "Stop[Command]";

		case OperationConstant.TYPE_STOP_SUSPEND://2
			return "Stop[Suspend]";

		case OperationConstant.TYPE_STOP_SKIP://4
			return "Stop[Skip]";

		case OperationConstant.TYPE_STOP_WAIT://6
			return "Stop[Pause]";

		case OperationConstant.TYPE_STOP_MAINTENANCE://8
			return "Stop[Change End Value]";

		case OperationConstant.TYPE_STOP_SET_END_VALUE://10
			return "Stop[Set End Value]";

		case OperationConstant.TYPE_STOP_FORCE://11
			return "Stop[Force]";

		default:
			return "[Unknown Operation]";
		}
	}

	/**
	 * 指定したジョブユニットの最終更新日時を返す
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param jobunitId ジョブユニットID
	 * @return Long 最終更新日時
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public List<Long> getUpdateTimeList(List<String> jobunitIdList) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobunitUpdateTime :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		StringBuilder jobunitIdStr = new StringBuilder();
		for (String str : jobunitIdList) {
			if (jobunitIdStr.length() > 0) {
				jobunitIdStr.append(", ");
			}
			jobunitIdStr.append(str);
		}
		
		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobunitUpdateTime, jobunitID="
				+ jobunitIdStr + ", User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getUpdateTime(jobunitIdList);

	}

	/**
	 * 編集ロックを取得する
	 *
	 * JobManagementAdd権限とJobManagementRead権限とJobManagementWrite権限が必要
	 *
	 * @param jobunitId ジョブユニットID
	 * @param updateTime 最終更新日時
	 * @param forceFlag 強制的に編集ロックを取得するか
	 *
	 * @return セッション
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws UpdateTimeNotLatest
	 * @throws OtherUserGetLock
	 * @throws JobInvalid
	 * @throws JobMasterNotFound
	 */
	public Integer getEditLock(String jobunitId, Long updateTime, boolean forceFlag) throws HinemosUnknown, InvalidUserPass, InvalidRole, OtherUserGetLock, UpdateTimeNotLatest, JobMasterNotFound, JobInvalid {
		m_log.debug("getEditLock :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.ADD));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobunitID=");
		msg.append(jobunitId);
		msg.append(", UpdateTime=");
		msg.append(updateTime);
		msg.append(", ForceFlag=");
		msg.append(forceFlag);

		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get EditLock, Method=getEditLock, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		String userIpAddr = HttpAuthenticator.getUserAccountString(wsctx);
		// ユーザ名@[IPアドレス]の形式から、ユーザ名とIPアドレスを抜き出す
		String user = userIpAddr.substring(0, userIpAddr.indexOf("@"));
		String ipAddr = userIpAddr.substring(userIpAddr.indexOf("[") + 1, userIpAddr.indexOf("]"));

		return new JobControllerBean().getEditLock(jobunitId, updateTime, forceFlag, user, ipAddr);
	}

	/**
	 * 編集ロックの正当性をチェックする
	 *
	 * JobManagementAdd権限とJobManagementRead権限とJobManagementWrite権限が必要
	 *
	 * @param jobunitId ジョブユニットID
	 * @param editSession セッション
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws UpdateTimeNotLatest
	 * @throws OtherUserGetLock
	 * @throws JobInvalid
	 * @throws JobMasterNotFound
	 */
	public void checkEditLock(String jobunitId, Integer editSession) throws HinemosUnknown, InvalidUserPass, InvalidRole, OtherUserGetLock, UpdateTimeNotLatest, JobMasterNotFound, JobInvalid {
		m_log.debug("checkEditLock :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.ADD));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobunitID=");
		msg.append(jobunitId);
		msg.append(", EditSession=");
		msg.append(editSession);

		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Check EditLock, Method=checkEditLock, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		new JobControllerBean().checkEditLock(jobunitId, editSession);
	}

	/**
	 * 編集ロックを開放する。
	 *
	 * @param editSession セッション
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public void releaseEditLock(Integer editSession) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("releaseEditLock : editSession="+editSession);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.ADD));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", EditSession=");
		msg.append(editSession);

		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Release EditLock, Method=releaseEditLock, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		String userIpAddr = HttpAuthenticator.getUserAccountString(wsctx);
		// ユーザ名@[IPアドレス]の形式から、ユーザ名とIPアドレスを抜き出す
		String user = userIpAddr.substring(0, userIpAddr.indexOf("@"));
		String ipAddr = userIpAddr.substring(userIpAddr.indexOf("[") + 1, userIpAddr.indexOf("]"));
		
		new JobControllerBean().releaseEditLock(editSession, user, ipAddr);
	}
	
	/**
	 * 登録済みモジュール一覧情報を取得する。<BR>
	 *
	 * @param jobunitId ジョブユニットID
	 * @return 登録済みモジュール一覧情報
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public List<JobInfo> getRegisteredModule(String jobunitId) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getRegisteredModule : jobunitId=" + jobunitId);
		if (jobunitId == null)
			throw new HinemosUnknown("jobunitId is null");
		
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobUnitID=");
		msg.append(jobunitId);
			m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getRegisteredModule, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
		
		return new JobControllerBean().getRegisteredModule(jobunitId);
	}

	/**
	 * ジョブマップ用アイコン情報一覧を返します。<BR>
	 * 
	 * @return ジョブマップ用アイコン情報一覧
	 * @throws IconFileNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public List<JobmapIconImage> getJobmapIconImageList() throws IconFileNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobmapIconImageList :");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobmapIconImageList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getJobmapIconImageList();
	}

	/**
	 * ジョブ設定用のジョブマップアイコン情報一覧を返します。<BR>
	 * 
	 * @param ownerRoleId オーナーロールID
	 * @return ジョブマップ用アイコン情報一覧
	 * @throws IconFileNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public List<String> getJobmapIconImageIdListForSelect(String ownerRoleId) throws IconFileNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobmapIconImageIdListForSelect : ownerRoleId=" + ownerRoleId);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobmapIconImageIdListForSelect, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getJobmapIconImageIdListForSelect(ownerRoleId);
	}

	/**
	 * 承認ジョブにおける承認画面へのリンク先アドレスを取得する。<BR>
	 *
	 * @return 承認画面へのリンク先アドレス
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public String getApprovalPageLink() throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getDefaultApprovalViewLink");
		
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getApprovalPageLink, User="
					+ HttpAuthenticator.getUserAccountString(wsctx));
		
		return new JobControllerBean().getApprovalPageLink();
	}

	/**
	 * 承認ジョブにおける参照のオブジェクト権限を持つロールIDのリストを取得。<BR>
	 *
	 * @param objectId オブジェクトID
	 * @return 参照のオブジェクト権限を持つロールIDリスト
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public List<String> getRoleIdListWithReadObjectPrivilege(String objectId) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getRoleIdListWithReadObjectPrivilege");
		
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ObjectId=");
		msg.append(objectId);
			m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getRoleIdListWithReadObjectPrivilege, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
		
		return new JobControllerBean().getRoleIdListWithReadObjectPrivilege(objectId);
	}

	/**
	 * 指定のロールIDに属するユーザIDのリストを取得。<BR>
	 *
	 * @param roleId ロールID
	 * @return ロールIDに属するユーザIDのリスト
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public List<String> getUserIdListBelongToRoleId(String roleId) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getUserIdListBelongToRoleId");
		
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", ObjectId=");
		msg.append(roleId);
			m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getUserIdListBelongToRoleId, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
		
		return new JobControllerBean().getUserIdListBelongToRoleId(roleId);
	}
	
	/**
	 * 監視設定一覧の取得
	 *
	 * MonitorSettingRead権限が必要
	 *
	 * @param monitorTypeIds 監視設定IDリスト
	 * @param ownerRoleId オーナーロールID
	 * @return 監視設定一覧
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	public ArrayList<MonitorInfo> getMonitorListForJobMonitor(String ownerRoleId) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getMonitorListForJobMonitor");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_MONITOR + " Get"
				+ ", Method=getMonitorListForJobMonitor, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getMonitorListForJobMonitor(ownerRoleId);
	}
	
	/**
	 * 承認対象ジョブの一覧情報を取得します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param property 一覧フィルタ用プロパティ
	 * @param limit 表示上限件数
	 * @return 承認対象ジョブの一覧情報
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getApprovalJobList()
	 */
	public ArrayList<JobApprovalInfo> getApprovalJobList(JobApprovalFilter property, int limit) throws InvalidUserPass, InvalidRole, HinemosUnknown, JobInfoNotFound {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getApprovalJobList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return new JobControllerBean().getApprovalJobList(property, limit);
	}
	
	/**
	 * 承認情報を更新します。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @param info 承認情報
	 * @param isApprove 承認操作有無
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getApprovalJobList()
	 */
	public void modifyApprovalInfo(JobApprovalInfo info, Boolean isApprove)	throws InvalidUserPass, InvalidRole, HinemosUnknown, JobInfoNotFound, InvalidApprovalStatus {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.APPROVAL));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=modifyApprovalInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		// 認証済み操作ログ
		StringBuffer msg = new StringBuffer();
		msg.append(", JobID=");
		msg.append(info.getJobId());

		try {
			new JobControllerBean().modifyApprovalInfo(info, isApprove);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Change Approval Job Failed, Method=modifyApprovalInfo, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Change Approval Job, Method=modifyApprovalInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	
	/**
	 * スクリプトの最大サイズを取得します。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public int getScriptContentMaxSize() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		int maxsize = 0;
		maxsize = HinemosPropertyCommon.job_script_maxsize.getIntegerValue();
		
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getScriptContentMaxSize, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		
		return maxsize;
	}

	/**
	 * ジョブキュー(同時実行制御キュー)の設定を一覧表示するビューのための情報を返します。
	 * <p>
	 * ジョブ機能の「参照」システム権限が必要です。
	 * 
	 * @param filter フィルタ条件。nullの場合はフィルタなし。
	 * @throws InvalidUserPass ユーザ認証エラー。
	 * @throws InvalidRole アクセス権限エラー。
	 * @throws InvalidSetting フィルタ条件設定に不備があります。
	 * @throws HinemosUnknown その他のエラー。
	 */
	public JobQueueSettingViewInfo getJobQueueSettingViewInfo(JobQueueSettingViewFilter filter)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		JobQueueSettingViewInfo result = new JobControllerBean().getJobQueueSettingViewInfo(filter);

		if (m_opelog.isDebugEnabled()) {
			String filterStr = "";
			if (filter != null) {
				filterStr = ", " + filter.toString();
			}
			m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobQueueSettingViewInfo, User="
					+ HttpAuthenticator.getUserAccountString(wsctx) + filterStr);
		}

		return result;
	}

	/**
	 * ジョブキュー(同時実行制御キュー)を参照しているジョブを一覧表示するビューのための情報を返します。
	 * <p>
	 * 通常、ジョブユニットの情報以外では{@link JobInfo}のオーナーロールIDはnullに設定されますが、
	 * このメソッドが返す情報に含まれる{@link JobInfo}には、「当該ジョブの上位ジョブユニットのオーナーロールID」が設定されます。
	 * <p>
	 * ジョブ機能の「参照」システム権限が必要です。
	 * 
	 * @param queueId 情報を取得するキューのID。
	 * @throws InvalidUserPass ユーザ認証エラー。
	 * @throws InvalidRole アクセス権限エラー。
	 * @throws JobQueueNotFound 指定されたジョブキューが見つかりません。
	 * @throws HinemosUnknown その他のエラー。
	 */
	public JobQueueReferrerViewInfo getJobQueueReferrerViewInfo(String queueId)
			throws InvalidUserPass, InvalidRole, JobQueueNotFound, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		JobQueueReferrerViewInfo result = new JobControllerBean().getJobQueueReferrerViewInfo(queueId);

		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobQueueReferrerViewInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx) + ", queueId=" + queueId);

		return result;
	}
	
	/**
	 * ジョブキュー(同時実行制御キュー)の活動状況を一覧表示するビューのための情報を返します。
	 * <p>
	 * ジョブ機能の「参照」システム権限が必要です。
	 * 
	 * @param filter フィルタ条件。nullの場合はフィルタなし。同時実行数(concurrency)のフィルタ条件は無効。
	 * @throws InvalidUserPass ユーザ認証エラー。
	 * @throws InvalidRole アクセス権限エラー。
	 * @throws HinemosUnknown その他のエラー。
	 * @throws InvalidSetting フィルタ条件設定に不備があります。
	 */
	public JobQueueActivityViewInfo getJobQueueActivityViewInfo(JobQueueActivityViewFilter filter)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		JobQueueActivityViewInfo result = new JobControllerBean().getJobQueueActivityViewInfo(filter);

		if (m_opelog.isDebugEnabled()) {
			String filterStr = "";
			if (filter != null) {
				filterStr = ", " + filter.toString();
			}
			m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobQueueActivityViewInfo, User="
					+ HttpAuthenticator.getUserAccountString(wsctx) + filterStr);
		}

		return result;
	}
	
	/**
	 * ジョブキュー(同時実行制御キュー)の内部状況を表示するビューのための情報を返します。
	 * <p>
	 * ジョブ機能の「参照」システム権限が必要です。
	 * 
	 * @param queueId 情報を取得するキューのID。
	 * @throws InvalidUserPass ユーザ認証エラー。
	 * @throws InvalidRole アクセス権限エラー。
	 * @throws JobQueueNotFound 指定されたジョブキューが見つかりません。
	 * @throws HinemosUnknown その他のエラー。
	 */
	public JobQueueContentsViewInfo getJobQueueContentsViewInfo(String queueId)
			throws InvalidUserPass, InvalidRole, JobQueueNotFound, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		JobQueueContentsViewInfo result = new JobControllerBean().getJobQueueContentsViewInfo(queueId);

		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobQueueContentsViewInfo, User="
				+ HttpAuthenticator.getUserAccountString(wsctx) + ", queueId=" + queueId);

		return result;
	}

	/**
	 * 指定されたロールから参照可能なジョブキュー(同時実行制御キュー)の設定情報のリストを返します。
	 * <p>
	 * ジョブ機能の「参照」システム権限が必要です。
	 * 
	 * @param roleId ロールID。
	 * @return キューの設定情報のリスト。
	 * @throws InvalidUserPass ユーザ認証エラー。
	 * @throws InvalidRole アクセス権限エラー。
	 * @throws HinemosUnknown その他のエラー。
	 */
	public List<JobQueueSetting> getJobQueueList(String roleId)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		List<JobQueueSetting> result = new JobControllerBean().getJobQueueList(roleId);

		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobQueueList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return result;
	}
	
	/**
	 * 指定されたジョブキュー(同時実行制御キュー)の設定情報を返します。
	 * <p>
	 * ジョブ機能の「参照」システム権限が必要です。
	 * 
	 * @param queueId 設定情報を取得したいキューのID。
	 * @return キューの設定情報。
	 * @throws InvalidUserPass ユーザ認証エラー。
	 * @throws InvalidRole アクセス権限エラー。
	 * @throws JobQueueNotFound 指定されたジョブキューが見つかりません。
	 * @throws HinemosUnknown その他のエラー。
	 */
	public JobQueueSetting getJobQueue(String queueId)
			throws InvalidUserPass, InvalidRole, JobQueueNotFound, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		JobQueueSetting result = new JobControllerBean().getJobQueue(queueId);

		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_JOB + " Get, Method=getJobQueue, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		return result;
	}
	
	/**
	 * ジョブキュー(同時実行制御キュー)の設定を追加します。
	 * <p>
	 * ジョブ機能の「作成」システム権限が必要です。
	 * 
	 * @param setting キューの設定情報。
	 * @throws InvalidUserPass ユーザ認証エラー。
	 * @throws InvalidRole アクセス権限エラー。
	 * @throws InvalidSetting 設定情報に誤りがあります。 
	 * @throws HinemosUnknown その他のエラー。
	 */
	public void addJobQueue(JobQueueSetting setting)
			throws InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		if (setting == null) {
			throw new HinemosUnknown("Argument is null.");
		}

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.ADD));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		boolean success = false;
		try {
			new JobControllerBean().addJobQueue(setting);
			success = true;
		} finally {
			String msg = "Method=addJobQueue, User=" + HttpAuthenticator.getUserAccountString(wsctx) + ", QueueId="
					+ setting.getQueueId();
			if (success) {
				m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Add JobQueue, " + msg);
			} else {
				m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Add JobQueue Failed, " + msg);
			}
		}
	}

	/**
	 * ジョブキュー(同時実行制御キュー)の設定を変更します。
	 * ただし、変更できるのは、「キュー名」と「同時実行可能数」のみです。
	 * <p>
	 * ジョブ機能の「変更」システム権限が必要です。
	 * 
	 * @param setting キューの設定情報。
	 * @throws InvalidUserPass ユーザ認証エラー。
	 * @throws InvalidRole アクセス権限エラー。
	 * @throws JobQueueNotFound 更新対象のキューが存在しません。 
	 * @throws InvalidSetting 設定情報に誤りがあります。 
	 * @throws HinemosUnknown その他のエラー。
	 */
	public void modifyJobQueue(JobQueueSetting setting)
			throws InvalidUserPass, InvalidRole, JobQueueNotFound, InvalidSetting, HinemosUnknown {
		if (setting == null) {
			throw new HinemosUnknown("Argument is null.");
		}

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		boolean success = false;
		try {
			new JobControllerBean().modifyJobQueue(setting);
			success = true;
		} finally {
			String msg = "Method=modifyJobQueue, User=" + HttpAuthenticator.getUserAccountString(wsctx) + ", QueueId="
					+ setting.getQueueId();
			if (success) {
				m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Change JobQueue, " + msg);
			} else {
				m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Change JobQueue Failed, " + msg);
			}
		}
	}

	/**
	 * ジョブキュー(同時実行制御キュー)の設定を削除します。
	 * <p>
	 * ジョブ機能の「変更」システム権限が必要です。
	 * 
	 * @param queueId 削除するキューのID。
	 * @throws InvalidUserPass ユーザ認証エラー。
	 * @throws InvalidRole アクセス権限エラー。
	 * @throws InvalidSetting 設定情報に誤りがあります。 
	 * @throws HinemosUnknown その他のエラー。
	 */
	public void deleteJobQueue(String queueId) throws InvalidUserPass, InvalidRole, InvalidSetting, HinemosUnknown {
		if (queueId == null || queueId.isEmpty()) {
			throw new HinemosUnknown("QueueId is empty.");
		}

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.JOBMANAGEMENT, SystemPrivilegeMode.MODIFY));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		boolean success = false;
		try {
			new JobControllerBean().deleteJobQueue(queueId);
			success = true;
		} finally {
			String msg = "Method=deleteJobQueue, User=" + HttpAuthenticator.getUserAccountString(wsctx) + ", QueueId="
					+ queueId;
			if (success) {
				m_opelog.info(HinemosModuleConstant.LOG_PREFIX_JOB + " Delete JobQueue, " + msg);
			} else {
				m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_JOB + " Delete JobQueue Failed, " + msg);
			}
		}
	}
}
