/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.util;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AddFileCheckRequest;
import org.openapitools.client.model.AddJobLinkRcvRequest;
import org.openapitools.client.model.AddJobLinkSendSettingRequest;
import org.openapitools.client.model.AddJobManualRequest;
import org.openapitools.client.model.AddJobQueueRequest;
import org.openapitools.client.model.AddScheduleRequest;
import org.openapitools.client.model.EditLockResponse;
import org.openapitools.client.model.GetApprovalJobListRequest;
import org.openapitools.client.model.GetEditLockRequest;
import org.openapitools.client.model.GetJobFullListRequest;
import org.openapitools.client.model.GetJobHistoryListRequest;
import org.openapitools.client.model.GetJobHistoryListResponse;
import org.openapitools.client.model.GetJobKickListByConditionRequest;
import org.openapitools.client.model.GetJobKickListByConditionResponse;
import org.openapitools.client.model.GetJobLinkMessageListRequest;
import org.openapitools.client.model.GetJobLinkMessageListResponse;
import org.openapitools.client.model.GetJobQueueActivityInfoRequest;
import org.openapitools.client.model.GetJobQueueListSearchRequest;
import org.openapitools.client.model.GetPlanListRequest;
import org.openapitools.client.model.JobApprovalInfoResponse;
import org.openapitools.client.model.JobFileCheckResponse;
import org.openapitools.client.model.JobForwardFileResponse;
import org.openapitools.client.model.JobInfoReferrerQueueResponse;
import org.openapitools.client.model.JobInfoResponse;
import org.openapitools.client.model.JobKickResponse;
import org.openapitools.client.model.JobLinkRcvResponse;
import org.openapitools.client.model.JobLinkSendSettingResponse;
import org.openapitools.client.model.JobManualResponse;
import org.openapitools.client.model.JobNodeDetailResponse;
import org.openapitools.client.model.JobOperationPropResponse;
import org.openapitools.client.model.JobOperationRequest;
import org.openapitools.client.model.JobPlanResponse;
import org.openapitools.client.model.JobQueueItemContentResponse;
import org.openapitools.client.model.JobQueueItemInfoResponse;
import org.openapitools.client.model.JobQueueResponse;
import org.openapitools.client.model.JobQueueSettingViewInfoResponse;
import org.openapitools.client.model.JobRpaLoginResolutionResponse;
import org.openapitools.client.model.JobRpaScreenshotResponse;
import org.openapitools.client.model.JobScheduleResponse;
import org.openapitools.client.model.JobTreeItemResponseP1;
import org.openapitools.client.model.JobTreeItemResponseP2;
import org.openapitools.client.model.JobTreeItemResponseP3;
import org.openapitools.client.model.JobTreeItemResponseP4;
import org.openapitools.client.model.JobmapIconImageInfoResponseP1;
import org.openapitools.client.model.ModifyApprovalInfoRequest;
import org.openapitools.client.model.ModifyFileCheckRequest;
import org.openapitools.client.model.ModifyJobLinkRcvRequest;
import org.openapitools.client.model.ModifyJobLinkSendSettingRequest;
import org.openapitools.client.model.ModifyJobManualRequest;
import org.openapitools.client.model.ModifyJobQueueRequest;
import org.openapitools.client.model.ModifyScheduleRequest;
import org.openapitools.client.model.PremakeJobsessionResponse;
import org.openapitools.client.model.RegisterJobunitRequest;
import org.openapitools.client.model.ReplaceJobunitRequest;
import org.openapitools.client.model.RunJobRequest;
import org.openapitools.client.model.RunJobResponse;
import org.openapitools.client.model.SendJobLinkMessageManualRequest;
import org.openapitools.client.model.SendJobLinkMessageManualResponse;
import org.openapitools.client.model.SetJobKickStatusRequest;

import com.clustercontrol.bean.RestKind;
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
import com.clustercontrol.fault.JobMasterDuplicate;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.JobQueueNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.OtherUserGetLock;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.UpdateTimeNotLatest;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestUrlSequentialExecuter;

public class JobRestClientWrapper {
	private static Log m_log = LogFactory.getLog( JobRestClientWrapper.class );
	private RestConnectUnit connectUnit;
	
	private final RestKind restKind = RestKind.JobRestEndpoints;

	public static final String DATETIME_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";

	public static JobRestClientWrapper getWrapper(String managerName) {
		return new JobRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public JobRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}
	
	public JobTreeItemResponseP1 getJobTree(String ownerRoleId ) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, JobMasterNotFound, NotifyNotFound, UserNotFound {

		RestUrlSequentialExecuter<JobTreeItemResponseP1> proxy = new RestUrlSequentialExecuter<JobTreeItemResponseP1>(this.connectUnit,this.restKind){
			@Override
			public JobTreeItemResponseP1 executeMethod( DefaultApi apiClient) throws Exception{
				 JobTreeItemResponseP1  result =  apiClient.jobmanagementGetJobTree(ownerRoleId);
				if(m_log.isTraceEnabled()){
					m_log.trace("getJobTree(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed| HinemosUnknown| InvalidRole| InvalidUserPass| JobMasterNotFound| NotifyNotFound| UserNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobInfoResponse getJobFull(String jobunitId, String jobId ) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, JobMasterNotFound, NotifyNotFound, UserNotFound {
		RestUrlSequentialExecuter<JobInfoResponse> proxy = new RestUrlSequentialExecuter<JobInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public JobInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobInfoResponse result =  apiClient.jobmanagementGetJobFull(jobunitId, jobId);
				if(m_log.isTraceEnabled()){
					m_log.trace("getJobFull(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown| InvalidRole| InvalidUserPass| JobMasterNotFound| NotifyNotFound| UserNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobInfoResponse> getJobFullList(GetJobFullListRequest request) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, JobMasterNotFound, NotifyNotFound, UserNotFound {
		RestUrlSequentialExecuter<List<JobInfoResponse>> proxy = new RestUrlSequentialExecuter<List<JobInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<JobInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<JobInfoResponse> result =  apiClient.jobmanagementGetJobFullList(request);
				if(m_log.isTraceEnabled()){
					m_log.trace("getJobFullList(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown| InvalidRole| InvalidUserPass| JobMasterNotFound| NotifyNotFound| UserNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public JobTreeItemResponseP2 getJobTreeJobInfoFull(String ownerRoleId )throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, JobMasterNotFound, NotifyNotFound, UserNotFound {
		RestUrlSequentialExecuter<JobTreeItemResponseP2> proxy = new RestUrlSequentialExecuter<JobTreeItemResponseP2>(this.connectUnit,this.restKind){
			@Override
			public JobTreeItemResponseP2 executeMethod( DefaultApi apiClient) throws Exception{
				JobTreeItemResponseP2 result =  apiClient.jobmanagementGetJobTreeJobInfoFull(ownerRoleId);
				if(m_log.isTraceEnabled()){
					m_log.trace("getJobTreeJobInfoFull(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed| HinemosUnknown| InvalidRole| InvalidUserPass| JobMasterNotFound| NotifyNotFound| UserNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	public EditLockResponse getEditLock(String jobunitId, GetEditLockRequest getEditLockRequest) throws RestConnectFailed, 	HinemosUnknown, InvalidRole, InvalidUserPass, OtherUserGetLock, UpdateTimeNotLatest, JobInvalid, JobMasterNotFound{
		RestUrlSequentialExecuter<EditLockResponse> proxy = new RestUrlSequentialExecuter<EditLockResponse>(this.connectUnit,this.restKind){
			@Override
			public EditLockResponse executeMethod( DefaultApi apiClient) throws Exception{
				EditLockResponse result =  apiClient.jobmanagementGetEditLock(jobunitId, getEditLockRequest);
				if(m_log.isTraceEnabled()){
					m_log.trace("getEditLock(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed| HinemosUnknown| InvalidRole| InvalidUserPass| OtherUserGetLock| UpdateTimeNotLatest| JobInvalid| JobMasterNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public EditLockResponse checkEditLock(String jobunitId, Integer editSession) throws RestConnectFailed, 	HinemosUnknown, InvalidRole, InvalidUserPass, OtherUserGetLock, UpdateTimeNotLatest, JobInvalid, JobMasterNotFound{
		RestUrlSequentialExecuter<EditLockResponse> proxy = new RestUrlSequentialExecuter<EditLockResponse>(this.connectUnit,this.restKind){
			@Override
			public EditLockResponse executeMethod( DefaultApi apiClient) throws Exception{
				EditLockResponse result =  apiClient.jobmanagementCheckEditLock(jobunitId, editSession);
				if(m_log.isTraceEnabled()){
					m_log.trace("checkEditLock(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed| HinemosUnknown| InvalidRole| InvalidUserPass| OtherUserGetLock| UpdateTimeNotLatest| JobInvalid| JobMasterNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public void releaseEditLock(String jobunitId, Integer editSession) throws RestConnectFailed, 	HinemosUnknown, InvalidRole, InvalidUserPass{
		RestUrlSequentialExecuter<Object> proxy = new RestUrlSequentialExecuter<Object>(this.connectUnit,this.restKind){
			@Override
			public Object executeMethod( DefaultApi apiClient) throws Exception{
				apiClient.jobmanagementReleaseEditLock(jobunitId, editSession);
				return null;
			}
		};
		try {
			proxy.proxyExecute();
		} catch (RestConnectFailed| HinemosUnknown| InvalidRole| InvalidUserPass def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobTreeItemResponseP2 deleteJobunit(String jobunitId) throws RestConnectFailed, 	HinemosUnknown, InvalidRole, InvalidUserPass, OtherUserGetLock, UpdateTimeNotLatest, JobInvalid, JobMasterNotFound{
		RestUrlSequentialExecuter<JobTreeItemResponseP2 > proxy = new RestUrlSequentialExecuter<JobTreeItemResponseP2 >(this.connectUnit,this.restKind){
			@Override
			public JobTreeItemResponseP2  executeMethod( DefaultApi apiClient) throws Exception{
				JobTreeItemResponseP2  result =  apiClient.jobmanagementDeleteJobunit(jobunitId);
				if(m_log.isTraceEnabled()){
					m_log.trace("deleteJobunit(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed| HinemosUnknown| InvalidRole| InvalidUserPass|  JobInvalid| JobMasterNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobTreeItemResponseP2 registerJobunit(RegisterJobunitRequest registerJobunitRequest ) throws RestConnectFailed,HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, NotifyNotFound,UserNotFound, JobMasterDuplicate {
		RestUrlSequentialExecuter<JobTreeItemResponseP2> proxy = new RestUrlSequentialExecuter<JobTreeItemResponseP2>(this.connectUnit,this.restKind){
			@Override
			public JobTreeItemResponseP2 executeMethod( DefaultApi apiClient) throws Exception{
				if(m_log.isTraceEnabled()){
					m_log.trace("registerJobunit(): request="+ registerJobunitRequest.toString());
				}
				JobTreeItemResponseP2 result = apiClient.jobmanagementRegisterJobunit(true, registerJobunitRequest);
				if(m_log.isTraceEnabled()){
					m_log.trace("registerJobunit(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (  RestConnectFailed| HinemosUnknown| JobMasterNotFound| JobInvalid| InvalidUserPass| InvalidRole| InvalidSetting| NotifyNotFound|UserNotFound | JobMasterDuplicate def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	public JobTreeItemResponseP2 replaceJobunit(String jobunitId, ReplaceJobunitRequest replaceJobunitRequest ) throws RestConnectFailed,HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, NotifyNotFound,UserNotFound {
		RestUrlSequentialExecuter<JobTreeItemResponseP2> proxy = new RestUrlSequentialExecuter<JobTreeItemResponseP2>(this.connectUnit,this.restKind){
			@Override
			public JobTreeItemResponseP2 executeMethod( DefaultApi apiClient) throws Exception{
				if(m_log.isTraceEnabled()){
					m_log.trace("replaceJobunit(): request="+ replaceJobunitRequest.toString());
				}
				JobTreeItemResponseP2  result =  apiClient.jobmanagementReplaceJobunit(jobunitId, true, replaceJobunitRequest);
				if(m_log.isTraceEnabled()){
					m_log.trace("replaceJobunit(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (  RestConnectFailed| HinemosUnknown| JobMasterNotFound| JobInvalid| InvalidUserPass| InvalidRole| InvalidSetting| NotifyNotFound|UserNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public GetJobHistoryListResponse getJobHistoryList(GetJobHistoryListRequest jobHistoryListRequest) throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<GetJobHistoryListResponse> proxy = new RestUrlSequentialExecuter<GetJobHistoryListResponse>(this.connectUnit, this.restKind) {
			@Override
			public GetJobHistoryListResponse executeMethod(DefaultApi apiClient) throws Exception {
				GetJobHistoryListResponse result = apiClient.jobmanagementGetJobHistoryList(jobHistoryListRequest);
				if (m_log.isTraceEnabled()) {
					m_log.trace("getJobHistoryList(): response=" + result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass def) {//想定内例外 API個別に判断
			throw def;
		} catch (Exception unknown) { //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public 	JobTreeItemResponseP4 getJobDetailList(String sessionId ) throws RestConnectFailed ,HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, JobInfoNotFound {
		RestUrlSequentialExecuter<JobTreeItemResponseP4> proxy = new RestUrlSequentialExecuter<	JobTreeItemResponseP4>(this.connectUnit,this.restKind){
			@Override
			public JobTreeItemResponseP4 executeMethod( DefaultApi apiClient) throws Exception{
				JobTreeItemResponseP4 result =  apiClient.jobmanagementGetJobDetailList(sessionId);
				if(m_log.isTraceEnabled()){
					m_log.trace("getJobDetailList(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (  RestConnectFailed |HinemosUnknown| InvalidRole| InvalidUserPass| InvalidSetting| JobInfoNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public  List<JobNodeDetailResponse> getNodeDetailList(String sessionId ,String  jobunitId,String  jobId) throws RestConnectFailed ,HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, JobInfoNotFound {
		RestUrlSequentialExecuter<List<JobNodeDetailResponse>> proxy = new RestUrlSequentialExecuter<List<JobNodeDetailResponse>>(this.connectUnit,this.restKind){
			@Override
			public  List<JobNodeDetailResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<JobNodeDetailResponse> result =  apiClient.jobmanagementGetNodeDetailList(sessionId, jobunitId, jobId);
				if(m_log.isTraceEnabled()){
					m_log.trace("getNodeDetailList(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (  RestConnectFailed |HinemosUnknown| InvalidRole| InvalidUserPass| JobInfoNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public List<JobForwardFileResponse> getForwardFileList(String sessionId ,String  jobunitId,String  jobId) throws RestConnectFailed ,HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, JobInfoNotFound {
		RestUrlSequentialExecuter<List<JobForwardFileResponse> > proxy = new RestUrlSequentialExecuter<List<JobForwardFileResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<JobForwardFileResponse>  executeMethod( DefaultApi apiClient) throws Exception{
				List<JobForwardFileResponse> result = apiClient.jobmanagementGetForwardFileList(sessionId, jobunitId, jobId);
				if(m_log.isTraceEnabled()){
					m_log.trace("getForwardFileList(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (  RestConnectFailed |HinemosUnknown| InvalidRole| InvalidUserPass| JobInfoNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobTreeItemResponseP3 getSessionJobInfo(String sessionId ,String  jobunitId,String  jobId) throws RestConnectFailed ,HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, JobInfoNotFound {
		RestUrlSequentialExecuter<JobTreeItemResponseP3> proxy = new RestUrlSequentialExecuter<JobTreeItemResponseP3>(this.connectUnit,this.restKind){
			@Override
			public JobTreeItemResponseP3 executeMethod( DefaultApi apiClient) throws Exception{
				JobTreeItemResponseP3 result = apiClient.jobmanagementGetSessionJobInfo(sessionId, jobunitId, jobId);
				if(m_log.isTraceEnabled()){
					m_log.trace("getSessionJobInfo(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (  RestConnectFailed |HinemosUnknown| InvalidRole| InvalidUserPass| JobInfoNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public JobmapIconImageInfoResponseP1 getJobmapIconImageIdListForSelect(String ownerRoleId) throws RestConnectFailed , IconFileNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<JobmapIconImageInfoResponseP1> proxy = new RestUrlSequentialExecuter<JobmapIconImageInfoResponseP1>(this.connectUnit,this.restKind){
			@Override
			public JobmapIconImageInfoResponseP1 executeMethod( DefaultApi apiClient) throws Exception{
				JobmapIconImageInfoResponseP1 result = apiClient.jobmanagementGetJobmapIconImageIdListForSelect(ownerRoleId);
				if(m_log.isTraceEnabled()){
					m_log.trace("getJobmapIconImageIdListForSelect(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (  RestConnectFailed | IconFileNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobOperationPropResponse getAvailableStartOperationSessionJob(String sessionId, String jobunitId, String jobId ) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<JobOperationPropResponse> proxy = new RestUrlSequentialExecuter<JobOperationPropResponse>(this.connectUnit,this.restKind){
			@Override
			public JobOperationPropResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobOperationPropResponse  result =  apiClient.jobmanagementGetAvailableStartOperationSessionJob(sessionId, jobunitId, jobId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public JobOperationPropResponse getAvailableStartOperationSessionNode(String sessionId, String jobunitId, String jobId, String facilityId ) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<JobOperationPropResponse> proxy = new RestUrlSequentialExecuter<JobOperationPropResponse>(this.connectUnit,this.restKind){
			@Override
			public JobOperationPropResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobOperationPropResponse  result =  apiClient.jobmanagementGetAvailableStartOperationSessionNode(sessionId, jobunitId, jobId, facilityId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public JobOperationPropResponse getAvailableStopOperationSessionJob(String sessionId, String jobunitId, String jobId ) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<JobOperationPropResponse> proxy = new RestUrlSequentialExecuter<JobOperationPropResponse>(this.connectUnit,this.restKind){
			@Override
			public JobOperationPropResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobOperationPropResponse  result =  apiClient.jobmanagementGetAvailableStopOperationSessionJob(sessionId, jobunitId, jobId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public JobOperationPropResponse getAvailableStopOperationSessionNode(String sessionId, String jobunitId, String jobId, String facilityId ) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<JobOperationPropResponse> proxy = new RestUrlSequentialExecuter<JobOperationPropResponse>(this.connectUnit,this.restKind){
			@Override
			public JobOperationPropResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobOperationPropResponse  result =  apiClient.jobmanagementGetAvailableStopOperationSessionNode(sessionId, jobunitId, jobId, facilityId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public RunJobResponse runJob(String jobunitId, String jobId, RunJobRequest runJobRequest ) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestUrlSequentialExecuter<RunJobResponse> proxy = new RestUrlSequentialExecuter<RunJobResponse>(this.connectUnit,this.restKind){
			@Override
			public RunJobResponse executeMethod( DefaultApi apiClient) throws Exception{
				RunJobResponse result =  apiClient.jobmanagementRunJob(jobunitId, jobId, runJobRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public void operationSessionJob(String sessionId, String jobunitId, String jobId, JobOperationRequest jobOperationRequest ) throws RestConnectFailed, HinemosUnknown, JobInfoNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(this.connectUnit,this.restKind){
			@Override
			public Void executeMethod( DefaultApi apiClient) throws Exception{
				apiClient.jobmanagementOperationSessionJob(sessionId, jobunitId, jobId, jobOperationRequest);
				return null;
			}
		};
		try {
			proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown | JobInfoNotFound | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public void operationSessionNode(String sessionId, String jobunitId, String jobId, String facilityId, JobOperationRequest jobOperationRequest ) throws RestConnectFailed, HinemosUnknown, JobInfoNotFound, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(this.connectUnit,this.restKind){
			@Override
			public Void executeMethod( DefaultApi apiClient) throws Exception{
				apiClient.jobmanagementOperationSessionNode(sessionId, jobunitId, jobId, facilityId, jobOperationRequest);
				return null;
			}
		};
		try {
			proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown | JobInfoNotFound | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public JobKickResponse addSchedule(AddScheduleRequest addScheduleRequest ) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobKickDuplicate {
		RestUrlSequentialExecuter<JobKickResponse> proxy = new RestUrlSequentialExecuter<JobKickResponse>(this.connectUnit,this.restKind){
			@Override
			public JobKickResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobKickResponse result =  apiClient.jobmanagementAddSchedule(addScheduleRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | JobKickDuplicate def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobKickResponse addFileCheck(AddFileCheckRequest addFileCheckRequest ) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobKickDuplicate {
		RestUrlSequentialExecuter<JobKickResponse> proxy = new RestUrlSequentialExecuter<JobKickResponse>(this.connectUnit,this.restKind){
			@Override
			public JobKickResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobKickResponse result =  apiClient.jobmanagementAddFileCheck(addFileCheckRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | JobKickDuplicate def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobKickResponse addJobManual(AddJobManualRequest addJobManualRequest ) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobKickDuplicate {
		RestUrlSequentialExecuter<JobKickResponse> proxy = new RestUrlSequentialExecuter<JobKickResponse>(this.connectUnit,this.restKind){
			@Override
			public JobKickResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobKickResponse result =  apiClient.jobmanagementAddJobManual(addJobManualRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | JobKickDuplicate def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobKickResponse addJobLinkRcv(AddJobLinkRcvRequest addJobLinkRcvRequest ) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobKickDuplicate {
		RestUrlSequentialExecuter<JobKickResponse> proxy = new RestUrlSequentialExecuter<JobKickResponse>(this.connectUnit,this.restKind){
			@Override
			public JobKickResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobKickResponse result =  apiClient.jobmanagementAddJobLinkRcv(addJobLinkRcvRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | JobKickDuplicate def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobKickResponse modifySchedule(String jobKickId, ModifyScheduleRequest modifyScheduleRequest ) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobInfoNotFound, JobMasterNotFound {
		RestUrlSequentialExecuter<JobKickResponse> proxy = new RestUrlSequentialExecuter<JobKickResponse>(this.connectUnit,this.restKind){
			@Override
			public JobKickResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobKickResponse result =  apiClient.jobmanagementModifySchedule(jobKickId, modifyScheduleRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | JobInfoNotFound | JobMasterNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobKickResponse modifyFileCheck(String jobKickId, ModifyFileCheckRequest modifyFileCheckRequest ) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobInfoNotFound, JobMasterNotFound {
		RestUrlSequentialExecuter<JobKickResponse> proxy = new RestUrlSequentialExecuter<JobKickResponse>(this.connectUnit,this.restKind){
			@Override
			public JobKickResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobKickResponse result =  apiClient.jobmanagementModifyFileCheck(jobKickId, modifyFileCheckRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | JobInfoNotFound | JobMasterNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobKickResponse modifyJobManual(String jobKickId, ModifyJobManualRequest modifyJobManualRequest ) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobInfoNotFound, JobMasterNotFound {
		RestUrlSequentialExecuter<JobKickResponse> proxy = new RestUrlSequentialExecuter<JobKickResponse>(this.connectUnit,this.restKind){
			@Override
			public JobKickResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobKickResponse result =  apiClient.jobmanagementModifyJobManual(jobKickId, modifyJobManualRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | JobInfoNotFound | JobMasterNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobKickResponse modifyJobLinkRcv(String jobKickId, ModifyJobLinkRcvRequest modifyJobLinkRcvRequest ) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobInfoNotFound, JobMasterNotFound {
		RestUrlSequentialExecuter<JobKickResponse> proxy = new RestUrlSequentialExecuter<JobKickResponse>(this.connectUnit,this.restKind){
			@Override
			public JobKickResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobKickResponse result =  apiClient.jobmanagementModifyJobLinkRcv(jobKickId, modifyJobLinkRcvRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting | JobInfoNotFound | JobMasterNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobKickResponse> deleteSchedule(String jobKickIds ) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, JobInfoNotFound, JobMasterNotFound {
		RestUrlSequentialExecuter<List<JobKickResponse>> proxy = new RestUrlSequentialExecuter<List<JobKickResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<JobKickResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<JobKickResponse> result =  apiClient.jobmanagementDeleteSchedule(jobKickIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | JobInfoNotFound | JobMasterNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobKickResponse> deleteFileCheck(String jobKickIds ) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, JobInfoNotFound, JobMasterNotFound {
		RestUrlSequentialExecuter<List<JobKickResponse>> proxy = new RestUrlSequentialExecuter<List<JobKickResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<JobKickResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<JobKickResponse> result =  apiClient.jobmanagementDeleteFileCheck(jobKickIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | JobInfoNotFound | JobMasterNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobKickResponse> deleteJobManual(String jobKickIds ) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, JobInfoNotFound, JobMasterNotFound {
		RestUrlSequentialExecuter<List<JobKickResponse>> proxy = new RestUrlSequentialExecuter<List<JobKickResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<JobKickResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<JobKickResponse> result =  apiClient.jobmanagementDeleteJobManual(jobKickIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | JobInfoNotFound | JobMasterNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobKickResponse> deleteJobLinkRcv(String jobKickIds ) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, JobInfoNotFound, JobMasterNotFound {
		RestUrlSequentialExecuter<List<JobKickResponse>> proxy = new RestUrlSequentialExecuter<List<JobKickResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<JobKickResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<JobKickResponse> result =  apiClient.jobmanagementDeleteJobLinkRcv(jobKickIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | JobInfoNotFound | JobMasterNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobKickResponse> deleteJobKick(String jobKickIds ) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, JobInfoNotFound, JobMasterNotFound {
		RestUrlSequentialExecuter<List<JobKickResponse>> proxy = new RestUrlSequentialExecuter<List<JobKickResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<JobKickResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<JobKickResponse> result =  apiClient.jobmanagementDeleteJobKick(jobKickIds);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | JobInfoNotFound | JobMasterNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobScheduleResponse getJobSchedule(String jobKickId ) throws RestConnectFailed, JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<JobScheduleResponse> proxy = new RestUrlSequentialExecuter<JobScheduleResponse>(this.connectUnit,this.restKind){
			@Override
			public JobScheduleResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobScheduleResponse result =  apiClient.jobmanagementGetJobSchedule(jobKickId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | JobMasterNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobFileCheckResponse getFileCheck(String jobKickId ) throws RestConnectFailed, JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<JobFileCheckResponse> proxy = new RestUrlSequentialExecuter<JobFileCheckResponse>(this.connectUnit,this.restKind){
			@Override
			public JobFileCheckResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobFileCheckResponse result =  apiClient.jobmanagementGetFileCheck(jobKickId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | JobMasterNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobManualResponse getJobManual(String jobKickId ) throws RestConnectFailed, JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<JobManualResponse> proxy = new RestUrlSequentialExecuter<JobManualResponse>(this.connectUnit,this.restKind){
			@Override
			public JobManualResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobManualResponse result =  apiClient.jobmanagementGetJobManual(jobKickId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | JobMasterNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobLinkRcvResponse getJobLinkRcv(String jobKickId ) throws RestConnectFailed, JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<JobLinkRcvResponse> proxy = new RestUrlSequentialExecuter<JobLinkRcvResponse>(this.connectUnit,this.restKind){
			@Override
			public JobLinkRcvResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobLinkRcvResponse result =  apiClient.jobmanagementGetJobLinkRcv(jobKickId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | JobMasterNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobKickResponse getJobKick(String jobKickId ) throws RestConnectFailed, JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<JobKickResponse> proxy = new RestUrlSequentialExecuter<JobKickResponse>(this.connectUnit,this.restKind){
			@Override
			public JobKickResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobKickResponse result =  apiClient.jobmanagementGetJobKick(jobKickId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | JobMasterNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobKickResponse> setJobKickStatus(SetJobKickStatusRequest setJobKickStatusRequest) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobMasterNotFound, JobInfoNotFound {
		RestUrlSequentialExecuter<List<JobKickResponse>> proxy = new RestUrlSequentialExecuter<List<JobKickResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<JobKickResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<JobKickResponse> result = apiClient.jobmanagementSetJobKickStatus(setJobKickStatusRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | JobMasterNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobKickResponse> getJobKickList() throws RestConnectFailed, JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<JobKickResponse>> proxy = new RestUrlSequentialExecuter<List<JobKickResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<JobKickResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<JobKickResponse> result = apiClient.jobmanagementGetJobKickList();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | JobMasterNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobKickResponse> getJobKickListByCondition(GetJobKickListByConditionRequest getJobKickListByConditionRequest) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobMasterNotFound, JobInfoNotFound {
		RestUrlSequentialExecuter<List<JobKickResponse>> proxy = new RestUrlSequentialExecuter<List<JobKickResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<JobKickResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<JobKickResponse> result = null;
				GetJobKickListByConditionResponse dtoRes = apiClient.jobmanagementGetJobKickListByCondition(getJobKickListByConditionRequest);
				if (dtoRes != null) {
					result = dtoRes.getJobKickList();
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | JobMasterNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobPlanResponse> getPlanList(GetPlanListRequest getPlanListRequest) throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobMasterNotFound, JobInfoNotFound {
		RestUrlSequentialExecuter<List<JobPlanResponse>> proxy = new RestUrlSequentialExecuter<List<JobPlanResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<JobPlanResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<JobPlanResponse> result = apiClient.jobmanagementGetPlanList(getPlanListRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | JobMasterNotFound | HinemosUnknown | InvalidUserPass | InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobApprovalInfoResponse> getApprovalJobList(GetApprovalJobListRequest getApprovalJobListRequest) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, JobInfoNotFound, InvalidSetting {
		RestUrlSequentialExecuter<List<JobApprovalInfoResponse>> proxy = new RestUrlSequentialExecuter<List<JobApprovalInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<JobApprovalInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<JobApprovalInfoResponse>  result =  apiClient.jobmanagementGetApprovalJobList(getApprovalJobListRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | JobInfoNotFound | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobApprovalInfoResponse modifyApprovalInfo(String sessionId, String jobunitId, String jobId, ModifyApprovalInfoRequest modifyApprovalInfoRequest) throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown, JobInfoNotFound, InvalidApprovalStatus, InvalidSetting {
		RestUrlSequentialExecuter<JobApprovalInfoResponse> proxy = new RestUrlSequentialExecuter<JobApprovalInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public JobApprovalInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobApprovalInfoResponse  result =  apiClient.jobmanagementModifyApprovalInfo(sessionId, jobunitId, jobId, modifyApprovalInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown | JobInfoNotFound | InvalidApprovalStatus | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobQueueSettingViewInfoResponse getJobQueueListSearch(GetJobQueueListSearchRequest getJobQueueListSearchRequest) throws RestConnectFailed ,HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<JobQueueSettingViewInfoResponse> proxy = new RestUrlSequentialExecuter<JobQueueSettingViewInfoResponse>(this.connectUnit,this.restKind){
			@Override
			public JobQueueSettingViewInfoResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobQueueSettingViewInfoResponse result = apiClient.jobmanagementGetJobQueueListSearch(getJobQueueListSearchRequest);
				if(m_log.isTraceEnabled()){
					m_log.trace("getJobQueueListSearch(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown| InvalidUserPass| InvalidRole| InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobInfoReferrerQueueResponse getJobInfoReferrerQueue(String queueId) throws RestConnectFailed ,HinemosUnknown, InvalidUserPass, InvalidRole, JobQueueNotFound {
		RestUrlSequentialExecuter<JobInfoReferrerQueueResponse> proxy = new RestUrlSequentialExecuter<JobInfoReferrerQueueResponse>(this.connectUnit,this.restKind){
			@Override
			public JobInfoReferrerQueueResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobInfoReferrerQueueResponse result = apiClient.jobmanagementGetJobInfoReferrerQueue(queueId);
				if(m_log.isTraceEnabled()){
					m_log.trace("getJobInfoReferrerQueue(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown| InvalidUserPass| InvalidRole| JobQueueNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobQueueItemInfoResponse> getJobQueueItemInfo(GetJobQueueActivityInfoRequest getJobQueueActivityInfoRequest) throws RestConnectFailed ,HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<JobQueueItemInfoResponse>> proxy = new RestUrlSequentialExecuter<List<JobQueueItemInfoResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<JobQueueItemInfoResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<JobQueueItemInfoResponse> result = apiClient.jobmanagementGetJobQueueItemInfo(getJobQueueActivityInfoRequest);
				if(m_log.isTraceEnabled()){
					m_log.trace("getJobQueueItemInfo(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown| InvalidUserPass| InvalidRole| InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobQueueItemContentResponse getJobQueueContentsInfo(String queueId) throws RestConnectFailed ,HinemosUnknown, InvalidUserPass, InvalidRole, JobQueueNotFound {
		RestUrlSequentialExecuter<JobQueueItemContentResponse> proxy = new RestUrlSequentialExecuter<JobQueueItemContentResponse>(this.connectUnit,this.restKind){
			@Override
			public JobQueueItemContentResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobQueueItemContentResponse result = apiClient.jobmanagementGetJobQueueContentsInfo(queueId);
				if(m_log.isTraceEnabled()){
					m_log.trace("getJobQueueContentsInfo(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown| InvalidUserPass| InvalidRole| JobQueueNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobQueueResponse> getJobQueueList(String roleId) throws RestConnectFailed ,HinemosUnknown, InvalidRole {
		RestUrlSequentialExecuter<List<JobQueueResponse>> proxy = new RestUrlSequentialExecuter<List<JobQueueResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<JobQueueResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<JobQueueResponse> result = apiClient.jobmanagementGetJobQueueList(roleId);
				if(m_log.isTraceEnabled()){
					m_log.trace("getJobQueueList(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown| InvalidRole def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobQueueResponse getJobQueue(String queueId) throws RestConnectFailed ,HinemosUnknown, InvalidUserPass, InvalidRole, JobQueueNotFound {
		RestUrlSequentialExecuter<JobQueueResponse> proxy = new RestUrlSequentialExecuter<JobQueueResponse>(this.connectUnit,this.restKind){
			@Override
			public JobQueueResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobQueueResponse result = apiClient.jobmanagementGetJobQueue(queueId);
				if(m_log.isTraceEnabled()){
					m_log.trace("getJobQueue(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown| InvalidUserPass| InvalidRole| JobQueueNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobQueueResponse addJobQueue(AddJobQueueRequest addJobQueueRequest) throws RestConnectFailed ,HinemosUnknown, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<JobQueueResponse> proxy = new RestUrlSequentialExecuter<JobQueueResponse>(this.connectUnit,this.restKind){
			@Override
			public JobQueueResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobQueueResponse result = apiClient.jobmanagementAddJobQueue(addJobQueueRequest);
				if(m_log.isTraceEnabled()){
					m_log.trace("addJobQueue(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown| InvalidRole| InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobQueueResponse modifyJobQueue(String queueId, ModifyJobQueueRequest modifyJobQueueRequest) throws RestConnectFailed ,HinemosUnknown, InvalidRole, InvalidSetting, JobQueueNotFound {
		RestUrlSequentialExecuter<JobQueueResponse> proxy = new RestUrlSequentialExecuter<JobQueueResponse>(this.connectUnit,this.restKind){
			@Override
			public JobQueueResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobQueueResponse result = apiClient.jobmanagementModifyJobQueue(queueId, modifyJobQueueRequest);
				if(m_log.isTraceEnabled()){
					m_log.trace("modifyJobQueue(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown| InvalidRole| InvalidSetting| JobQueueNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobQueueResponse> deleteJobQueue(String roleId) throws RestConnectFailed ,HinemosUnknown, InvalidSetting, InvalidRole, JobQueueNotFound {
		RestUrlSequentialExecuter<List<JobQueueResponse>> proxy = new RestUrlSequentialExecuter<List<JobQueueResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<JobQueueResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<JobQueueResponse> result = apiClient.jobmanagementDeleteJobQueue(roleId);
				if(m_log.isTraceEnabled()){
					m_log.trace("deleteJobQueue(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown| InvalidSetting| InvalidRole | JobQueueNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public PremakeJobsessionResponse deletePremakeJobsession(String jobkickId) throws RestConnectFailed ,HinemosUnknown, InvalidUserPass, InvalidRole, JobInfoNotFound {
		RestUrlSequentialExecuter<PremakeJobsessionResponse> proxy = new RestUrlSequentialExecuter<PremakeJobsessionResponse>(this.connectUnit,this.restKind){
			@Override
			public PremakeJobsessionResponse executeMethod( DefaultApi apiClient) throws Exception{
				PremakeJobsessionResponse result = apiClient.jobmanagementDeletePremakeJobsession(jobkickId);
				if(m_log.isTraceEnabled()){
					m_log.trace("deletePremakeJobsession(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | JobInfoNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobLinkSendSettingResponse> getJobLinkSendSettingList(String ownerRoleId) throws RestConnectFailed ,HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<JobLinkSendSettingResponse>> proxy = new RestUrlSequentialExecuter<List<JobLinkSendSettingResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<JobLinkSendSettingResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<JobLinkSendSettingResponse> result = apiClient.jobmanagementGetJobLinkSendSettingList(ownerRoleId);
				if(m_log.isTraceEnabled()){
					m_log.trace("getJobLinkSendSettingList(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobLinkSendSettingResponse getJobLinkSendSetting(String joblinkSendSettingId) throws RestConnectFailed , JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<JobLinkSendSettingResponse> proxy = new RestUrlSequentialExecuter<JobLinkSendSettingResponse>(this.connectUnit,this.restKind){
			@Override
			public JobLinkSendSettingResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobLinkSendSettingResponse result = apiClient.jobmanagementGetJobLinkSendSetting(joblinkSendSettingId);
				if(m_log.isTraceEnabled()){
					m_log.trace("getJobLinkSendSetting(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | JobMasterNotFound | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobLinkSendSettingResponse addJobLinkSendSetting(AddJobLinkSendSettingRequest addJobLinkSendSettingRequest) throws RestConnectFailed , JobMasterDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<JobLinkSendSettingResponse> proxy = new RestUrlSequentialExecuter<JobLinkSendSettingResponse>(this.connectUnit,this.restKind){
			@Override
			public JobLinkSendSettingResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobLinkSendSettingResponse result = apiClient.jobmanagementAddJobLinkSendSetting(addJobLinkSendSettingRequest);
				if(m_log.isTraceEnabled()){
					m_log.trace("addJobLinkSendSetting(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | JobMasterDuplicate | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public JobLinkSendSettingResponse modifyJobLinkSendSetting(String joblinkSendSettingId, ModifyJobLinkSendSettingRequest modifyJobLinkSendSettingRequest) throws RestConnectFailed ,JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<JobLinkSendSettingResponse> proxy = new RestUrlSequentialExecuter<JobLinkSendSettingResponse>(this.connectUnit,this.restKind){
			@Override
			public JobLinkSendSettingResponse executeMethod( DefaultApi apiClient) throws Exception{
				JobLinkSendSettingResponse result = apiClient.jobmanagementModifyJobLinkSendSetting(joblinkSendSettingId, modifyJobLinkSendSettingRequest);
				if(m_log.isTraceEnabled()){
					m_log.trace("modifyJobLinkSendSetting(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | HinemosUnknown| InvalidRole| InvalidSetting| JobMasterNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobLinkSendSettingResponse> deleteJobLinkSendSetting(String joblinkSendSettingIds) throws RestConnectFailed ,JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		RestUrlSequentialExecuter<List<JobLinkSendSettingResponse>> proxy = new RestUrlSequentialExecuter<List<JobLinkSendSettingResponse>>(this.connectUnit,this.restKind){
			@Override
			public List<JobLinkSendSettingResponse> executeMethod( DefaultApi apiClient) throws Exception{
				List<JobLinkSendSettingResponse> result = apiClient.jobmanagementDeleteJobLinkSendSetting(joblinkSendSettingIds);
				if(m_log.isTraceEnabled()){
					m_log.trace("deleteJobLinkSendSetting(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch ( RestConnectFailed | JobMasterNotFound | HinemosUnknown | InvalidUserPass | InvalidRole | InvalidSetting def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public SendJobLinkMessageManualResponse sendJobLinkMessageManual(SendJobLinkMessageManualRequest sendJobLinkMessageManualRequest)
			throws RestConnectFailed ,HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, JobMasterNotFound, FacilityNotFound {
		RestUrlSequentialExecuter<SendJobLinkMessageManualResponse> proxy = new RestUrlSequentialExecuter<SendJobLinkMessageManualResponse>(this.connectUnit,this.restKind){
			@Override
			public SendJobLinkMessageManualResponse executeMethod( DefaultApi apiClient) throws Exception{
				SendJobLinkMessageManualResponse result =  apiClient.jobmanagementSendJobLinkMessageManual(sendJobLinkMessageManualRequest);
				if(m_log.isTraceEnabled()){
					m_log.trace("sendJobLinkMessageManual(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (  RestConnectFailed |HinemosUnknown| InvalidRole| InvalidUserPass | InvalidSetting | JobMasterNotFound | FacilityNotFound def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public GetJobLinkMessageListResponse getJobLinkMessageList(GetJobLinkMessageListRequest jobLinkMessageListRequest)
			throws RestConnectFailed ,HinemosUnknown, InvalidRole, InvalidUserPass {
		RestUrlSequentialExecuter<GetJobLinkMessageListResponse> proxy = new RestUrlSequentialExecuter<GetJobLinkMessageListResponse>(this.connectUnit,this.restKind,DATETIME_FORMAT){
			@Override
			public GetJobLinkMessageListResponse executeMethod( DefaultApi apiClient) throws Exception{
				GetJobLinkMessageListResponse result =  apiClient.jobmanagementGetJobLinkMessageList(jobLinkMessageListRequest);
				if(m_log.isTraceEnabled()){
					m_log.trace("getJobLinkMessageList(): response="+ result.toString());
				}
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (  RestConnectFailed |HinemosUnknown| InvalidRole| InvalidUserPass def) {//想定内例外 API個別に判断
			throw def;
		} catch ( Exception unknown ){ //想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobRpaLoginResolutionResponse> getJobRpaLoginResolution() throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<JobRpaLoginResolutionResponse>> proxy = new RestUrlSequentialExecuter<List<JobRpaLoginResolutionResponse>>(
				this.connectUnit, this.restKind) {
			@Override
			public List<JobRpaLoginResolutionResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<JobRpaLoginResolutionResponse> result = apiClient.jobmanagementGetJobRpaLoginResolution();
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public List<JobRpaScreenshotResponse> getJobRpaScreenshot(String sessionId, String jobunitId, String jobId, String facilityId)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<List<JobRpaScreenshotResponse>> proxy = new RestUrlSequentialExecuter<List<JobRpaScreenshotResponse>>(
				this.connectUnit, this.restKind, "yyyy-MM-dd HH:mm:ss.SSS") {  // 画像の特定にミリ秒ありの日付が必要なためフォーマットを指定
			@Override
			public List<JobRpaScreenshotResponse> executeMethod(DefaultApi apiClient) throws Exception {
				List<JobRpaScreenshotResponse> result = apiClient.jobmanagementGetRpaScreenshot(sessionId, jobunitId, jobId, facilityId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

	public File downloadJobRpaScreenshotFile(String sessionId, String jobunitId, String jobId, String facilityId, String regDate)
			throws RestConnectFailed, HinemosUnknown, InvalidUserPass, InvalidRole {
		RestUrlSequentialExecuter<File> proxy = new RestUrlSequentialExecuter<File>(
				this.connectUnit, this.restKind, "yyyy-MM-dd HH:mm:ss.SSS") {  // 画像の特定にミリ秒ありの日付が必要なためフォーマットを指定
			@Override
			public File executeMethod(DefaultApi apiClient) throws Exception {
				File result = apiClient.jobmanagementDownloadRpaScreenshotFile(sessionId, jobunitId, jobId, facilityId, regDate);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidUserPass | InvalidRole def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

}
