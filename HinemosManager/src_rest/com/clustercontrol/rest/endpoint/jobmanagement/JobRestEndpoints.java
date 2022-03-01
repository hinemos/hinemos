/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.jobmanagement;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_404;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_409;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.glassfish.grizzly.http.server.Request;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeFunction;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.calendar.util.TimeStringConverter;
import com.clustercontrol.commons.util.HinemosSessionContext;
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
import com.clustercontrol.fault.JobSessionDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.OtherUserGetLock;
import com.clustercontrol.fault.UpdateTimeNotLatest;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.filtersetting.bean.JobHistoryFilterBaseInfo;
import com.clustercontrol.jobmanagement.bean.JobApprovalFilter;
import com.clustercontrol.jobmanagement.bean.JobApprovalInfo;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobDetailInfo;
import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.jobmanagement.bean.JobForwardFile;
import com.clustercontrol.jobmanagement.bean.JobHistoryList;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobKick;
import com.clustercontrol.jobmanagement.bean.JobKickConstant;
import com.clustercontrol.jobmanagement.bean.JobKickFilterInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkExpInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkManualSendInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageFilter;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageList;
import com.clustercontrol.jobmanagement.bean.JobLinkRcv;
import com.clustercontrol.jobmanagement.bean.JobLinkSendMessageResultInfo;
import com.clustercontrol.jobmanagement.bean.JobNodeDetail;
import com.clustercontrol.jobmanagement.bean.JobObjectGroupInfo;
import com.clustercontrol.jobmanagement.bean.JobObjectInfo;
import com.clustercontrol.jobmanagement.bean.JobOperationInfo;
import com.clustercontrol.jobmanagement.bean.JobPlan;
import com.clustercontrol.jobmanagement.bean.JobPlanFilter;
import com.clustercontrol.jobmanagement.bean.JobSchedule;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.RpaJobScreenshot;
import com.clustercontrol.jobmanagement.model.JobLinkSendSettingEntity;
import com.clustercontrol.jobmanagement.model.JobRpaLoginResolutionMstEntity;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueActivityViewFilter;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueActivityViewInfo;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueContentsViewInfo;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueReferrerViewInfo;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueSetting;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueSettingViewFilter;
import com.clustercontrol.jobmanagement.queue.bean.JobQueueSettingViewInfo;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.jobmanagement.session.JobRunManagementBean;
import com.clustercontrol.jobmanagement.util.JobValidator;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rest.annotation.RestLog;
import com.clustercontrol.rest.annotation.RestLog.LogAction;
import com.clustercontrol.rest.annotation.RestLog.LogTarget;
import com.clustercontrol.rest.annotation.RestLog.LogType;
import com.clustercontrol.rest.annotation.RestLogFunc;
import com.clustercontrol.rest.annotation.RestLogFunc.LogFuncName;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.ArrayTypeParam;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreCommandline;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreReference;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.AbstractAddJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.AbstractModifyJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.AddApprovalJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.AddCommandJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.AddFileCheckJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.AddFileCheckRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.AddFileTransferJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.AddJobLinkRcvJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.AddJobLinkRcvRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.AddJobLinkSendJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.AddJobLinkSendSettingRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.AddJobManualRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.AddJobQueueRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.AddJobnetRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.AddMonitorJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.AddReferJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.AddRpaJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.AddScheduleRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ApprovalJobInfoResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.CommandJobInfoResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.EditLockResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.FileCheckJobInfoResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.FileJobInfoResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.GetApprovalJobListRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.GetEditLockRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.GetJobFullListRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.GetJobHistoryListRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.GetJobHistoryListResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.GetJobKickListByConditionRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.GetJobKickListByConditionResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.GetJobLinkMessageListRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.GetJobLinkMessageListResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.GetJobQueueActivityInfoRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.GetJobQueueListSearchRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.GetPlanListRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobApprovalInfoResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobDetailInfoResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobFileCheckResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobForwardFileResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobInfoReferrerQueueResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobInfoRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobInfoRequestP1;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobInfoResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobInfoResponseP1;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobKickFilterInfoRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobKickResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobLinkExpInfoRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobLinkRcvJobInfoResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobLinkRcvResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobLinkSendJobInfoResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobLinkSendSettingResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobManualResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobNodeDetailResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobObjectGroupInfoRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobObjectGroupInfoResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobObjectInfoRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobObjectInfoResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobOperationPropResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobOperationRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobPlanResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobQueueItemContentResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobQueueItemInfoResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobQueueSettingViewInfoResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobQueueResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobRpaLoginResolutionResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobRpaScreenshotResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobScheduleResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobSessionJobDetailResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobTreeItemRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobTreeItemResponseP1;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobTreeItemResponseP2;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobTreeItemResponseP3;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobTreeItemResponseP4;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobnetInfoResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ModifyApprovalInfoRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ModifyApprovalJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ModifyCommandJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ModifyFileCheckJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ModifyFileCheckRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ModifyFileTransferJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ModifyJobLinkRcvJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ModifyJobLinkRcvRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ModifyJobLinkSendJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ModifyJobLinkSendSettingRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ModifyJobManualRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ModifyJobQueueRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ModifyJobnetRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ModifyMonitorJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ModifyReferJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ModifyRpaJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ModifyScheduleRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.MonitorJobInfoResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.PremakeJobsessionResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ReferJobInfoResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.RegistJobLinkMessageRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.RegistJobLinkMessageResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.RegisterJobunitRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.ReplaceJobunitRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.RpaJobInfoResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.RunJobRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.RunJobResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.SendJobLinkMessageManualRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.SendJobLinkMessageManualResponse;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.SetJobKickStatusRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ControlEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobApprovalStatusEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobKickTypeEnum;
import com.clustercontrol.rest.endpoint.jobmap.dto.JobmapIconImageInfoResponseP1;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestByteArrayConverter;
import com.clustercontrol.rest.util.RestCommonConverter;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.rest.util.RestTempFileType;
import com.clustercontrol.rest.util.RestTempFileUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

@Path("/job")
@RestLogFunc(name = LogFuncName.Job)
public class JobRestEndpoints {
	private static Log m_log = LogFactory.getLog(JobRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "jobmanagement";
	
	/**
	 * ジョブツリー情報（ツリー関連情報と表示制御に関わる属性）を取得する。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param ownerRoleId
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws NotifyNotFound
	 * @throws JobMasterNotFound
	 * @throws UserNotFound
	 */
	@GET
	@Path("/setting/job_treeSimple")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobTree")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobTreeItemResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.Job, type = LogType.REFERENCE )
	public Response getJobTree(@Context Request request, @Context UriInfo uriInfo, @QueryParam("ownerRoleId") String ownerRoleId)
			throws NotifyNotFound, HinemosUnknown, JobMasterNotFound, UserNotFound, InvalidRole, InvalidRole ,InvalidUserPass {
		m_log.info("call getJobTree()");
		
		JobTreeItem retOrg= new JobControllerBean().getJobTree(ownerRoleId, true, Locale.getDefault());

		JobTreeItemResponseP1 dto = new JobTreeItemResponseP1();
		RestBeanUtil.convertBeanNoInvalid(retOrg, dto);

		RestLanguageConverter.convertMessages(dto);

		return Response.status(Status.OK).entity(dto).build();
	}

	/**
	 * ジョブツリー情報（ツリーとすべての属性）を取得する。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param ownerRoleId
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws NotifyNotFound
	 * @throws JobMasterNotFound
	 * @throws UserNotFound
	 */
	@GET
	@Path("/setting/job_treeFull")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobTreeJobInfoFull")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobTreeItemResponseP2.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.Job, type = LogType.REFERENCE )
	public Response getJobTreeJobInfoFull(@Context Request request, @Context UriInfo uriInfo, @QueryParam("ownerRoleId") String ownerRoleId)
			throws HinemosUnknown, InvalidRole, InvalidUserPass, NotifyNotFound, JobMasterNotFound, UserNotFound {
		m_log.info("call getJobTreeJobInfoFull()");
		
		JobTreeItem retOrg= new JobControllerBean().getJobTreeFullInfo(ownerRoleId, Locale.getDefault());

		JobTreeItemResponseP2 resDto = convertJobTreeItemP2FromInfo(retOrg);
		if(resDto!=null){
			RestLanguageConverter.convertMessages(resDto);
		}


		return Response.status(Status.OK).entity(resDto).build();
	}

	/**
	 * ジョブ情報の詳細を取得する。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param jobunitId 
	 * @param jobId
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws JobMasterNotFound
	 * @throws UserNotFound
	 */
	@GET
	@Path("/setting/job_info/jobunit/{jobunitId}/job/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobFull")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.Job, type = LogType.REFERENCE )
	public Response getJobFull(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("jobunitId") String jobunitId, @PathParam("jobId") String jobId)
					throws HinemosUnknown, InvalidRole, InvalidUserPass, JobMasterNotFound, UserNotFound {
		m_log.info("call getJobFull()");

		JobInfo findCondition = new JobInfo();
		findCondition.setJobunitId(jobunitId);
		findCondition.setId(jobId);
		findCondition.setPropertyFull(false);
		JobInfo  retOrg = new JobControllerBean().getJobFull(findCondition);

		JobInfoResponse dto = convertJobInfoResponseFromInfo(retOrg);
		RestLanguageConverter.convertMessages(dto);

		return Response.status(Status.OK).entity(dto).build();
	}

	/**
	 * 指定されたジョブについて、ジョブ情報の詳細を一括で取得する。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param GetJobFullListRequest->jobList 取得対象となるジョブの一覧
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws JobMasterNotFound
	 * @throws UserNotFound
	 * @throws InvalidSetting
	 */
	@POST
	@Path("/setting/job_info_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobFullList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.Job, type = LogType.REFERENCE )
	public Response getJobFullList(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "GetJobFullListRequest", content = @Content(schema = @Schema(implementation = GetJobFullListRequest.class))) String requestBody)
					throws HinemosUnknown, InvalidRole,InvalidUserPass,JobMasterNotFound, UserNotFound, InvalidSetting {
		m_log.info("call getJobFullList()");

		GetJobFullListRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, GetJobFullListRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		List<JobInfo> jobList = new ArrayList<JobInfo>();
		for (JobInfoRequestP1 reqJob : dtoReq.getJobList()) {
			JobInfo findCondition = new JobInfo();
			findCondition.setJobunitId(reqJob.getJobunitId());
			findCondition.setId(reqJob.getId());
			findCondition.setPropertyFull(false);
			jobList.add(findCondition);
		}

		List<JobInfo> retOrgInfoList = new JobControllerBean().getJobFullList(jobList);

		// INFOからDTOへ変換
		List<JobInfoResponse> retDtoList = new ArrayList<JobInfoResponse>();
		for (JobInfo retInfo : retOrgInfoList){
			retDtoList.add(convertJobInfoResponseFromInfo(retInfo));
		}

		RestLanguageConverter.convertMessages(retDtoList);

		return Response.status(Status.OK).entity(retDtoList).build();
	}

	/**
	 * ジョブユニット情報を新規登録する。<BR>
	 *
	 * 対象となるジョブユニットに対する編集ロックがある場合
	 * 処理の成功時のみ自動的に編集ロックを解除しているので注意。<BR>
	 *
	 * JobManagementAdd権限とJobManagementWrite権限が必要
	 *
	 * @param RegisterJobunitRequest->jobTreeItem ジョブユニット情報。{@link com.clustercontrol.rest.endpoint.jobmanagement.dto.JobTreeItemRequest}の階層オブジェクト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * @throws JobMasterNotFound
	 * @throws JobInvalid
	 * @throws NotifyNotFound
	 * @throws UserNotFound
	 * @throws JobMasterDuplicate 
	 */
	@POST
	@Path("/setting/jobunit")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "RegisterJobunit")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobTreeItemResponseP2.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ ,SystemPrivilegeMode.MODIFY})
	@RestLog(action = LogAction.Add, target = LogTarget.Jobunit, type = LogType.UPDATE )
	@IgnoreCommandline
	public Response registerJobunit(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "RegisterJobunitRequest", content = @Content(schema = @Schema(implementation = RegisterJobunitRequest.class))) String requestBody )
					throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobMasterNotFound, JobInvalid, NotifyNotFound, UserNotFound, JobMasterDuplicate {
		m_log.info("call registerJobunit()");

		RegisterJobunitRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, RegisterJobunitRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();
		JobTreeItemRequest topItem = dtoReq.getJobTreeItem();
		boolean existJobUnit = false;
		try{
			// jobunitIdとjobIdが既に存在するか
			JobValidator.validateJobId(topItem.getData().getJobunitId(), topItem.getData().getId(),true);
			existJobUnit = true;
			m_log.info("registerJobunit() : jobunit " + topItem.getData().getJobunitId() + " is exist");
		} catch (InvalidSetting e) {
			m_log.info("registerJobunit() : jobunit " +  topItem.getData().getJobunitId() + " is new jobunit");
		}
		if(existJobUnit){
			throw new JobMasterDuplicate(MessageConstant.MESSAGE_JOBUNIT_ID_DUPLICATED.getMessage(topItem.getData().getJobunitId()));
		}
		// DTOからINFOへ変換
		JobTreeItem reqItem = convertJobTreeItemFromDto(topItem);

		//登録
		new JobControllerBean().registerJobunit(reqItem);

		//更新結果を取得
				
		JobTreeItem retOrg= new JobControllerBean().getJobunitTreeFullInfo(reqItem.getData().getJobunitId(), null, Locale.getDefault());
		
		// INFOからDTOへ変換し返却
		JobTreeItemResponseP2 resDto = convertJobTreeItemP2FromInfo(retOrg);
		if (resDto != null) {
			RestLanguageConverter.convertMessages(resDto);
		}
		return Response.status(Status.OK).entity(resDto).build();
	}

	/**
	 * ジョブユニット情報を変更する。<BR>
	 *
	 * 対象となるジョブユニットに対する編集ロックがある場合
	 * 処理の成功時のみ自動的に編集ロックを解除しているので注意。<BR>
	 * 
	 * JobManagementAdd権限とJobManagementWrite権限が必要
	 *
	 * @param ReplaceJobunitRequest->jobTreeItem ジョブユニット情報。{@link com.clustercontrol.rest.endpoint.jobmanagement.dto.JobTreeItemRequest}の階層オブジェクト
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * @throws JobMasterNotFound
	 * @throws JobInvalid
	 * @throws NotifyNotFound
	 * @throws UserNotFound
	 */
	@PUT
	@Path("/setting/jobunit/{jobunitId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ReplaceJobunit")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobTreeItemResponseP2.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ ,SystemPrivilegeMode.MODIFY})
	@RestLog(action = LogAction.Modify, target = LogTarget.Jobunit, type = LogType.UPDATE )
	@IgnoreCommandline
	public Response replaceJobunit(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("jobunitId") String jobunitId, @RequestBody(description = "ReplaceJobunitRequest", content = @Content(schema = @Schema(implementation = ReplaceJobunitRequest.class))) String requestBody)
					throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, NotifyNotFound,UserNotFound {
		m_log.info("call replaceJobunit()");

		ReplaceJobunitRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ReplaceJobunitRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();
		JobTreeItemRequest topItem = dtoReq.getJobTreeItem();
		try{
			// jobunitIdとjobIdが既に存在するか
			JobValidator.validateJobId(topItem.getData().getJobunitId(), topItem.getData().getId(),true);
			m_log.info("replaceJobunit() : jobunit " + topItem.getData().getJobunitId() + " is exist");
		} catch (InvalidSetting e) {
			m_log.info("replaceJobunit() : jobunit " +  topItem.getData().getJobunitId() + " is new jobunit");
			throw new InvalidSetting(MessageConstant.MESSAGE_JOBUNIT_ID_NOT_EXIST.getMessage( topItem.getData().getJobunitId()));
		}
		
		// DTOからINFOへ変換
		JobTreeItem reqItem = convertJobTreeItemFromDto(topItem);

		//登録
		new JobControllerBean().registerJobunit(reqItem);

		//更新結果を取得
		JobTreeItem retOrg= new JobControllerBean().getJobunitTreeFullInfo(reqItem.getData().getJobunitId(), null, Locale.getDefault());
		
		// INFOからDTOへ変換し返却
		JobTreeItemResponseP2 resDto = convertJobTreeItemP2FromInfo(retOrg);
		if (resDto != null) {
			RestLanguageConverter.convertMessages(resDto);
		}
		return Response.status(Status.OK).entity(resDto).build();
	}

	/**
	 * 編集ロックを取得する
	 *
	 * JobManagementAdd権限とJobManagementRead権限とJobManagementWrite権限が必要
	 *
	 * @param jobunitId ジョブユニットID
	 * @param GetEditLockRequest->updateTime 最終更新日時
	 * @param GetEditLockRequest->forceFlag 強制的に編集ロックを取得するか
	 * @return セッションID
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws OtherUserGetLock
	 * @throws UpdateTimeNotLatest
	 * @throws JobInvalid
	 * @throws JobMasterNotFound
	 * @throws InvalidSetting
	 */
	@POST
	@Path("/setting/jobunit/{jobunitId}/lock")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetEditLock")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EditLockResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ ,SystemPrivilegeMode.MODIFY,SystemPrivilegeMode.ADD})
	@RestLog(action = LogAction.Add, target = LogTarget.Lock, type = LogType.UPDATE )
	@IgnoreCommandline
	public Response getEditLock(@Context Request request, @Context UriInfo uriInfo,@PathParam("jobunitId") String jobunitId,
			@RequestBody(description = "GetEditLockRequest", content = @Content(schema = @Schema(implementation = GetEditLockRequest.class))) String requestBody)
					throws 	HinemosUnknown, InvalidRole, InvalidUserPass, OtherUserGetLock, UpdateTimeNotLatest, JobInvalid, JobMasterNotFound,InvalidSetting {
		m_log.info("call getEditLock()");

		GetEditLockRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, GetEditLockRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);

		Long updateTime = null;
		if(dtoReq.getUpdateTime()!=null){
			updateTime = RestCommonConverter.convertDTStringToHinemosTime(dtoReq.getUpdateTime(), MessageConstant.UPDATE_DATE.getMessage());
		}
		Integer editSession = new JobControllerBean().getEditLock(jobunitId, updateTime, dtoReq.getForceFlag(), HinemosSessionContext.getLoginUserId(), request.getRemoteAddr());
		EditLockResponse resDto = new EditLockResponse();
		resDto.setEditSession(editSession);
		resDto.setJobunitId(jobunitId);

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
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
	 * @throws OtherUserGetLock
	 */
	@GET
	@Path("/setting/jobunit/{jobunitId}/lock/{editSession}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "CheckEditLock")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EditLockResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ ,SystemPrivilegeMode.MODIFY})
	@RestLog(action = LogAction.Get, target = LogTarget.Lock, type = LogType.REFERENCE )
	@IgnoreCommandline
	public Response checkEditLock(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("jobunitId") String jobunitId, @PathParam("editSession") Integer editSession)
					throws HinemosUnknown,OtherUserGetLock, InvalidRole, InvalidUserPass {
		m_log.info("call checkEditLock()");

		new JobControllerBean().checkEditLock(jobunitId, editSession);
		EditLockResponse resDto = new EditLockResponse();
		resDto.setEditSession(editSession);
		resDto.setJobunitId(jobunitId);

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * 編集ロックを開放する。
	 *
	 * @param jobunitId ジョブユニットID
	 * @param editSession セッション
	 *
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@DELETE
	@Path("/setting/jobunit/{jobunitId}/lock/{editSession}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ReleaseEditLock")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ ,SystemPrivilegeMode.MODIFY})
	@RestLog(action = LogAction.Delete, target = LogTarget.Lock, type = LogType.UPDATE)
	@IgnoreCommandline
	public Response releaseEditLock(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("jobunitId") String jobunitId, @PathParam("editSession") Integer editSession)
			throws HinemosUnknown, InvalidRole, InvalidUserPass {
		m_log.info("call releaseEditLock()");

		new JobControllerBean().releaseEditLock(editSession,jobunitId, HinemosSessionContext.getLoginUserId(), request.getRemoteAddr());

		// 現状のAPIの設計ポリシーだと[結果がある＝ロックがある]という意味になってしまうので ステータス以外 何も返却しない。
		return Response.status(Status.OK).build();
	}

	/**
	 * ジョブユニット単位でジョブツリー情報を削除する。<BR>
	 *
	 * 対象となるジョブユニットに対する編集ロックがある場合
	 * 処理の成功時のみ自動的に編集ロックを解除しているので注意。<BR>
	 *
	 * JobManagementWrite権限が必要
	 *
	 * @param jobunitId 削除対象ジョブユニットのジョブID
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * @throws JobMasterNotFound
	 * @throws JobInvalid
	 * @throws NotifyNotFound
	 * @throws UserNotFound
	 */
	@DELETE
	@Path("/setting/jobunit/{jobunitId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteJobunit")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobTreeItemResponseP2.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ ,SystemPrivilegeMode.MODIFY})
	@RestLog(action = LogAction.Delete, target = LogTarget.Jobunit, type = LogType.UPDATE )
	@IgnoreCommandline
	public Response deleteJobunit(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("jobunitId") String jobunitId)
			throws HinemosUnknown, InvalidRole, InvalidUserPass,	InvalidSetting, JobMasterNotFound, JobInvalid, NotifyNotFound, UserNotFound {
		m_log.info("call deleteJobunit()");

		//削除前にツリー情報を取得
		JobTreeItem retOrg= new JobControllerBean().getJobunitTreeFullInfo(jobunitId, null, Locale.getDefault());

		new JobControllerBean().deleteJobunit(jobunitId);

		// INFOからDTOへ変換し返却
		JobTreeItemResponseP2 resDto = convertJobTreeItemP2FromInfo(retOrg);
		if (resDto != null) {
			RestLanguageConverter.convertMessages(resDto);
		}

		return Response.status(Status.OK).entity(resDto).build();
	}

	/**
	 * 承認対象ジョブの一覧情報を取得します。<BR>
	 *
	 * JobManagementRead権限が必要
	 * 
	 * @param GetApprovalJobListRequest 一覧フィルタ用プロパティ
	 * @param size 表示履歴数
	 * @return 承認対象ジョブの一覧情報
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	@POST
	@Path("/session_approval_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetApprovalJobList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobApprovalInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.Session, type = LogType.REFERENCE )
	public Response getApprovalJobList(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "getApprovalJobListBody", content = @Content(schema = @Schema(implementation = GetApprovalJobListRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, JobInfoNotFound, InvalidSetting {
		m_log.info("call getApprovalJobList()");

		GetApprovalJobListRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				GetApprovalJobListRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		int limit = 0;
		if (dtoReq.getSize() != null) {
			limit = dtoReq.getSize();
		}

		JobApprovalFilter infoReq = new JobApprovalFilter();
		RestBeanUtil.convertBean(dtoReq, infoReq);

		//Enum配列はConvertできないため、個別代入
		if (dtoReq.getTargetStatusList() != null)  {
			List<Integer> statusList = new ArrayList<>();
			for (JobApprovalStatusEnum enumStatus : dtoReq.getTargetStatusList()) {
				statusList.add(enumStatus.getCode());
			}
			infoReq.setStatusList(statusList.toArray(new Integer[dtoReq.getTargetStatusList().length]));
		}

		List<JobApprovalInfo> infoResList = new JobControllerBean().getApprovalJobList(infoReq, limit);

		List<JobApprovalInfoResponse> dtoResList = new ArrayList<>();
		for (JobApprovalInfo infoRes : infoResList) {
			JobApprovalInfoResponse dtoRes = new JobApprovalInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);
		return Response.status(Status.OK).entity(dtoResList).build();
	}

	@PUT
	@Path("/session_approval/{sessionId}/jobunit/{jobunitId}/job/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyApprovalInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobApprovalInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ, SystemPrivilegeMode.APPROVAL })
	@RestLog(action = LogAction.Modify, target = LogTarget.Session, type = LogType.UPDATE)
	public Response modifyApprovalInfo(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("sessionId") String sessionId, @PathParam("jobunitId") String jobunitId, @PathParam("jobId") String jobId,
			@RequestBody(description = "modifyApprovalInfoBody", content = @Content(schema = @Schema(implementation = ModifyApprovalInfoRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, JobInfoNotFound, InvalidApprovalStatus, InvalidSetting {
		m_log.info("call modifyApprovalInfoBody()");

		ModifyApprovalInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyApprovalInfoRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobApprovalInfo infoReq = new JobApprovalInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setSessionId(sessionId);
		infoReq.setJobunitId(jobunitId);
		infoReq.setJobId(jobId);
		Boolean hasApproveOperation = null;
		if(infoReq.getResult() != null){
			hasApproveOperation = true;
		}
		new JobControllerBean().modifyApprovalInfo(infoReq, hasApproveOperation);

		JobApprovalFilter filter = new JobApprovalFilter();
		filter.setSessionId(sessionId);
		filter.setJobunitId(jobunitId);
		filter.setJobId(jobId);
		List<JobApprovalInfo> infoResList = new JobControllerBean().getApprovalJobList(filter, 1);

		JobApprovalInfoResponse dtoRes = new JobApprovalInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoResList.get(0), dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	@POST
	@Path("/history_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobHistoryList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetJobHistoryListResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.History, type = LogType.REFERENCE )
	public Response getJobHistoryList(
			@Context Request request, 
			@Context UriInfo uriInfo, 
			@RequestBody(description = "getJobHistoryListBody", content = @Content(schema = @Schema(
					implementation = GetJobHistoryListRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, JobInfoNotFound {
		m_log.info("call getJobHistoryList()");

		// size
		int size = 0;

		// requestBody
		JobHistoryFilterBaseInfo filterInfo = JobHistoryFilterBaseInfo.ofClientViewDefault();

		if(requestBody != null && requestBody.length() > 0){
			GetJobHistoryListRequest reqDto = RestObjectMapperWrapper.convertJsonToObject(
					requestBody, GetJobHistoryListRequest.class);
			RestCommonValitater.checkRequestDto(reqDto);
			reqDto.correlationCheck();
			if(reqDto.getSize() != null){
				size = reqDto.getSize();
			}
			if(reqDto.getFilter() != null) {
				RestBeanUtil.convertBean(reqDto.getFilter(), filterInfo);
			}
		}

		//
		JobHistoryList retList = new JobControllerBean().getJobHistoryList(filterInfo, size);

		GetJobHistoryListResponse resDto = new GetJobHistoryListResponse();
		RestBeanUtil.convertBeanNoInvalid(retList, resDto);
		RestLanguageConverter.convertMessages(resDto);

		return Response.status(Status.OK).entity(resDto).build();
	}

	/**
	 * ジョブ詳細一覧情報を返します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param sessionId セッションID
	 * @return ジョブ詳細一覧情報（ JobTreeItemResponseP4にて TOP itemのchildとして一覧が設定されている。）
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getDetailList(String)
	 */
	@GET
	@Path("/sessionJob_detail/{sessionId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobDetailList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema( implementation = JobTreeItemResponseP4.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.SessionJob, type = LogType.REFERENCE )
	public Response getJobDetailList(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("sessionId") String sessionId)
					throws  HinemosUnknown, InvalidRole, InvalidUserPass, JobInfoNotFound {
		m_log.info("call getJobDetailList()");

		JobTreeItem retInfo = new JobControllerBean().getJobDetailList(sessionId);
		JobTreeItemResponseP4 resDto = convertJobTreeItemP4FromInfo(retInfo);

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
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
	 * @return ノード詳細一覧情報（ List ）
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws JobInfoNotFound
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getNodeDetailList(String, String, String, Locale)
	 */
	@GET
	@Path("/sessionNode_detail/{sessionId}/jobunit/{jobunitId}/job/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetNodeDetailList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobNodeDetailResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ})
	@RestLog(action = LogAction.Get, target = LogTarget.SessionNode, type = LogType.REFERENCE )
	public Response getNodeDetailList(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("sessionId") String sessionId, @PathParam("jobunitId") String jobunitId, @PathParam("jobId") String jobId )
					throws  HinemosUnknown, InvalidRole, InvalidUserPass, JobInfoNotFound {
		m_log.info("call getNodeDetailList()");

		List<JobNodeDetail> resInfoList = new JobControllerBean().getNodeDetailList(sessionId, jobunitId, jobId, Locale.getDefault());
		List<JobNodeDetailResponse> resDtoList = new ArrayList<JobNodeDetailResponse>();
		for(JobNodeDetail retInfoRec : resInfoList){
			JobNodeDetailResponse resDto = new JobNodeDetailResponse();
			RestBeanUtil.convertBeanNoInvalid( retInfoRec, resDto);
			resDtoList.add(resDto);
		}
		RestLanguageConverter.convertMessages(resDtoList);
		return Response.status(Status.OK).entity(resDtoList).build();
	}

	/**
	 * ファイル転送一覧情報を返します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @return ファイル転送一覧情報（List）
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getForwardFileList(String, String)
	 */
	@GET
	@Path("/sessionFile_detail/{sessionId}/jobunit/{jobunitId}/job/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetForwardFileList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobForwardFileResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ})
	@RestLog(action = LogAction.Get, target = LogTarget.SessionFile, type = LogType.REFERENCE )
	public Response getForwardFileList(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("sessionId") String sessionId, @PathParam("jobunitId") String jobunitId, @PathParam("jobId") String jobId )
					throws  HinemosUnknown, InvalidRole, InvalidUserPass, JobInfoNotFound {
		m_log.info("call getForwardFileList()");

		List<JobForwardFile> resInfoList =  new JobControllerBean().getForwardFileList(sessionId, jobunitId, jobId);
		List<JobForwardFileResponse> resDtoList = new ArrayList<JobForwardFileResponse>();
		for(JobForwardFile retInfoRec : resInfoList){
			JobForwardFileResponse resDto = new JobForwardFileResponse();
			RestBeanUtil.convertBeanNoInvalid( retInfoRec, resDto);
			resDtoList.add(resDto);
		}
		RestLanguageConverter.convertMessages(resDtoList);
		return Response.status(Status.OK).entity(resDtoList).build();
	}

	/**
	 * セッションジョブ情報を返します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @return ジョブ情報（JobTreeItemResponseP3->data ツリー情報は無し）
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws JobInfoNotFound
	 * @see com.clustercontrol.jobmanagement.factory.SelectJob#getSessionJobInfo(String, String, String)
	 */
	@GET
	@Path("/sessionJob_jobInfo/{sessionId}/jobunit/{jobunitId}/job/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetSessionJobInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobTreeItemResponseP3.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ})
	@RestLog(action = LogAction.Get, target = LogTarget.SessionJob, type = LogType.REFERENCE )
	public Response getSessionJobInfo(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("sessionId") String sessionId, @PathParam("jobunitId") String jobunitId, @PathParam("jobId") String jobId )
					throws  HinemosUnknown, InvalidRole, InvalidUserPass, JobInfoNotFound {
		m_log.info("call getSessionJobInfo()");

		JobTreeItem retInfo = new JobControllerBean().getSessionJobInfo(sessionId, jobunitId, jobId);
		JobTreeItemResponseP3 resDto = new JobTreeItemResponseP3();
		resDto.setData(convertJobInfoResponseFromInfo(retInfo.getData()));

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	/**
	 * ジョブネット設定の追加を行うAPI
	 */
	@POST
	@Path("/setting/jobunit/{jobunitId}/jobnet")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddJobnet")
	@RestLog(action=LogAction.Add, target = LogTarget.Jobnet, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobnetInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response addJobnet(
			@PathParam("jobunitId") String jobunitId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addJobnetBody", content = @Content(schema = @Schema(implementation = AddJobnetRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call addJobnet()");

		AddJobnetRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, AddJobnetRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromAddJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setType(JobConstant.TYPE_JOBNET);
		JobInfo infoRes = new JobControllerBean().addJobChild(jobunitId, infoReq);

		JobnetInfoResponse dtoRes = new JobnetInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}



	/**
	 * コマンドジョブ設定の追加を行うAPI
	 */
	@POST
	@Path("/setting/jobunit/{jobunitId}/commandJob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCommandJob")
	@RestLog(action=LogAction.Add, target = LogTarget.CommandJob, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CommandJobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response addCommandJob(
			@PathParam("jobunitId") String jobunitId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addCommnadJobBody", content = @Content(schema = @Schema(implementation = AddCommandJobRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call addCommandJob()");

		AddCommandJobRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, AddCommandJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromAddJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setType(JobConstant.TYPE_JOB);
		JobInfo infoRes = new JobControllerBean().addJobChild(jobunitId, infoReq);

		CommandJobInfoResponse dtoRes = new CommandJobInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}


	/**
	 * ファイル転送ジョブ設定の追加を行うAPI
	 */
	@POST
	@Path("/setting/jobunit/{jobunitId}/fileJob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddFileTransferJob")
	@RestLog(action=LogAction.Add, target = LogTarget.FileJob, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FileJobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response addFileTransferJob(
			@PathParam("jobunitId") String jobunitId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addFileTransferJobBody", content = @Content(schema = @Schema(implementation = AddFileTransferJobRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call addFileTransferJob()");

		AddFileTransferJobRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, AddFileTransferJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromAddJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setType(JobConstant.TYPE_FILEJOB);
		JobInfo infoRes = new JobControllerBean().addJobChild(jobunitId, infoReq);

		FileJobInfoResponse dtoRes = new FileJobInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}


	/**
	 * 参照ジョブ設定の追加を行うAPI
	 */
	@POST
	@Path("/setting/jobunit/{jobunitId}/referJob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddReferJob")
	@RestLog(action=LogAction.Add, target = LogTarget.ReferJob, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReferJobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response addReferJob(
			@PathParam("jobunitId") String jobunitId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addReferJobBody", content = @Content(schema = @Schema(implementation = AddReferJobRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call addReferJob()");

		AddReferJobRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, AddReferJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromAddJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setType(JobConstant.TYPE_REFERJOB);
		JobInfo infoRes = new JobControllerBean().addJobChild(jobunitId, infoReq);

		ReferJobInfoResponse dtoRes = new ReferJobInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}


	/**
	 * 監視ジョブ設定の追加を行うAPI
	 */
	@POST
	@Path("/setting/jobunit/{jobunitId}/monitorJob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddMonitorJob")
	@RestLog(action=LogAction.Add, target = LogTarget.MonitorJob, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MonitorJobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response addMonitorJob(
			@PathParam("jobunitId") String jobunitId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addMonitorJobBody", content = @Content(schema = @Schema(implementation = AddMonitorJobRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call addMonitorJob()");

		AddMonitorJobRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, AddMonitorJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromAddJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setType(JobConstant.TYPE_MONITORJOB);
		JobInfo infoRes = new JobControllerBean().addJobChild(jobunitId, infoReq);

		MonitorJobInfoResponse dtoRes = new MonitorJobInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}


	/**
	 * 承認ジョブ設定の追加を行うAPI
	 */
	@POST
	@Path("/setting/jobunit/{jobunitId}/approvalJob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddApprovalJob")
	@RestLog(action=LogAction.Add, target = LogTarget.ApprovalJob, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ApprovalJobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response addApprovalJob(
			@PathParam("jobunitId") String jobunitId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addApprovalJobBody", content = @Content(schema = @Schema(implementation = AddApprovalJobRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call addApprovalJob()");

		AddApprovalJobRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, AddApprovalJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromAddJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setType(JobConstant.TYPE_APPROVALJOB);
		JobInfo infoRes = new JobControllerBean().addJobChild(jobunitId, infoReq);

		ApprovalJobInfoResponse dtoRes = new ApprovalJobInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}


	/**
	 * ジョブ連携送信ジョブ設定の追加を行うAPI
	 */
	@POST
	@Path("/setting/jobunit/{jobunitId}/joblinksendJob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddJobLinkSendJob")
	@RestLog(action=LogAction.Add, target=LogTarget.JobLinkSendJob, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobLinkSendJobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response addJobLinkSendJob(
			@PathParam("jobunitId") String jobunitId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addJobLinkSendJobBody", content = @Content(schema = @Schema(implementation = AddJobLinkSendJobRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call addJoblinkSendJob()");

		AddJobLinkSendJobRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, AddJobLinkSendJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromAddJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setType(JobConstant.TYPE_JOBLINKSENDJOB);
		JobInfo infoRes = new JobControllerBean().addJobChild(jobunitId, infoReq);

		JobLinkSendJobInfoResponse dtoRes = new JobLinkSendJobInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}


	/**
	 * ジョブ連携待機ジョブ設定の追加を行うAPI
	 */
	@POST
	@Path("/setting/jobunit/{jobunitId}/joblinkrcvJob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddJobLinkRcvJob")
	@RestLog(action=LogAction.Add, target=LogTarget.JobLinkRcvJob , type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobLinkRcvJobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response addJobLinkRcvJob(
			@PathParam("jobunitId") String jobunitId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addJobLinkRcvJobBody", content = @Content(schema = @Schema(implementation = AddJobLinkRcvJobRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call addJoblinkRcvJob()");

		AddJobLinkRcvJobRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, AddJobLinkRcvJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromAddJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setType(JobConstant.TYPE_JOBLINKRCVJOB);
		JobInfo infoRes = new JobControllerBean().addJobChild(jobunitId, infoReq);

		JobLinkRcvJobInfoResponse dtoRes = new JobLinkRcvJobInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}


	/**
	 * ファイルチェックジョブ設定の追加を行うAPI
	 */
	@POST
	@Path("/setting/jobunit/{jobunitId}/filecheckJob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddFileCheckJob")
	@RestLog(action=LogAction.Add, target=LogTarget.FileCheckJob, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FileCheckJobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response addFileCheckJob(
			@PathParam("jobunitId") String jobunitId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addFileCheckJobBody", content = @Content(schema = @Schema(implementation = AddFileCheckJobRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call addJoblinkRcvJob()");

		AddFileCheckJobRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, AddFileCheckJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromAddJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setType(JobConstant.TYPE_FILECHECKJOB);
		JobInfo infoRes = new JobControllerBean().addJobChild(jobunitId, infoReq);

		FileCheckJobInfoResponse dtoRes = new FileCheckJobInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * RPAシナリオジョブ設定の追加を行うAPI
	 */
	@POST
	@Path("/setting/jobunit/{jobunitId}/rpaJob")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddRpaJob")
	@RestLog(action=LogAction.Add, target=LogTarget.RpaJob, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaJobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response addRpaJob(
			@PathParam("jobunitId") String jobunitId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "addRpaJobBody", content = @Content(schema = @Schema(implementation = AddRpaJobRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call addRpaJob()");

		AddRpaJobRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddRpaJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromAddJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setType(JobConstant.TYPE_RPAJOB);
		JobInfo infoRes = new JobControllerBean().addJobChild(jobunitId, infoReq);

		RpaJobInfoResponse dtoRes = new RpaJobInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}


	/**
	 * ジョブネット設定の更新を行うAPI
	 */
	@PUT
	@Path("/setting/jobunit/{jobunitId}/jobnet/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyJobnet")
	@RestLog(action=LogAction.Modify, target = LogTarget.Jobnet, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobnetInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response modifyJobnet(
			@PathParam("jobunitId") String jobunitId,
			@PathParam("jobId") String jobId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyJobnetBody", content = @Content(schema = @Schema(implementation = ModifyJobnetRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call modifyJobnet()");

		ModifyJobnetRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyJobnetRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromModifyJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setId(jobId);
		infoReq.setType(JobConstant.TYPE_JOBNET);
		JobInfo infoRes = new JobControllerBean().modifyJobChild(jobunitId, infoReq);

		JobnetInfoResponse dtoRes = new JobnetInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * コマンドジョブ設定の更新を行うAPI
	 */
	@PUT
	@Path("/setting/jobunit/{jobunitId}/commandJob/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCommandJob")
	@RestLog(action=LogAction.Modify, target = LogTarget.CommandJob, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CommandJobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response modifyCommandJob(
			@PathParam("jobunitId") String jobunitId,
			@PathParam("jobId") String jobId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyCommnadJobBody", content = @Content(schema = @Schema(implementation = ModifyCommandJobRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call modifyCommandJob()");

		ModifyCommandJobRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyCommandJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromModifyJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setId(jobId);
		infoReq.setType(JobConstant.TYPE_JOB);
		JobInfo infoRes = new JobControllerBean().modifyJobChild(jobunitId, infoReq);

		CommandJobInfoResponse dtoRes = new CommandJobInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ファイル転送ジョブ設定の更新を行うAPI
	 */
	@PUT
	@Path("/setting/jobunit/{jobunitId}/fileJob/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyFileTransferJob")
	@RestLog(action=LogAction.Modify, target = LogTarget.FileJob, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FileJobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response modifyFileTransferJob(
			@PathParam("jobunitId") String jobunitId,
			@PathParam("jobId") String jobId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyFileTransferJobBody", content = @Content(schema = @Schema(implementation = ModifyFileTransferJobRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call modifyFileTransferJob()");

		ModifyFileTransferJobRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyFileTransferJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromModifyJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setId(jobId);
		infoReq.setType(JobConstant.TYPE_FILEJOB);
		JobInfo infoRes = new JobControllerBean().modifyJobChild(jobunitId, infoReq);

		FileJobInfoResponse dtoRes = new FileJobInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 参照ジョブ設定の更新を行うAPI
	 */
	@PUT
	@Path("/setting/jobunit/{jobunitId}/referJob/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyReferJob")
	@RestLog(action=LogAction.Modify, target = LogTarget.ReferJob, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReferJobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response modifyReferJob(
			@PathParam("jobunitId") String jobunitId,
			@PathParam("jobId") String jobId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyReferJobBody", content = @Content(schema = @Schema(implementation = ModifyReferJobRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call modifyReferJob()");

		ModifyReferJobRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyReferJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromModifyJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setId(jobId);
		infoReq.setType(JobConstant.TYPE_REFERJOB);
		JobInfo infoRes = new JobControllerBean().modifyJobChild(jobunitId, infoReq);

		ReferJobInfoResponse dtoRes = new ReferJobInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 監視ジョブ設定の更新を行うAPI
	 */
	@PUT
	@Path("/setting/jobunit/{jobunitId}/monitorJob/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyMonitorJob")
	@RestLog(action=LogAction.Modify, target = LogTarget.MonitorJob, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MonitorJobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response modifyMonitorJob(
			@PathParam("jobunitId") String jobunitId,
			@PathParam("jobId") String jobId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyMonitorJobBody", content = @Content(schema = @Schema(implementation = ModifyMonitorJobRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call modifyMonitorJob()");

		ModifyMonitorJobRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyMonitorJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromModifyJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setId(jobId);
		infoReq.setType(JobConstant.TYPE_MONITORJOB);
		JobInfo infoRes = new JobControllerBean().modifyJobChild(jobunitId, infoReq);

		MonitorJobInfoResponse dtoRes = new MonitorJobInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 承認ジョブ設定の更新を行うAPI
	 */
	@PUT
	@Path("/setting/jobunit/{jobunitId}/approvalJob/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyApprovalJob")
	@RestLog(action=LogAction.Modify, target = LogTarget.ApprovalJob, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ApprovalJobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response modifyApprovalJob(
			@PathParam("jobunitId") String jobunitId,
			@PathParam("jobId") String jobId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyApprovalJobBody", content = @Content(schema = @Schema(implementation = ModifyApprovalJobRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call modifyApprovalJob()");

		ModifyApprovalJobRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyApprovalJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromModifyJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setId(jobId);
		infoReq.setType(JobConstant.TYPE_APPROVALJOB);
		JobInfo infoRes = new JobControllerBean().modifyJobChild(jobunitId, infoReq);

		ApprovalJobInfoResponse dtoRes = new ApprovalJobInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブ連携送信ジョブ設定の更新を行うAPI
	 */
	@PUT
	@Path("/setting/jobunit/{jobunitId}/joblinkSendJob/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyJobLinkSendJob")
	@RestLog(action=LogAction.Modify, target=LogTarget.JobLinkSendJob, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobLinkSendJobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response modifyJobLinkSendJob(
			@PathParam("jobunitId") String jobunitId,
			@PathParam("jobId") String jobId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyJobLinkSendJobBody", content = @Content(schema = @Schema(implementation = ModifyJobLinkSendJobRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call modifyJobLinkSendJob()");

		ModifyJobLinkSendJobRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyJobLinkSendJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromModifyJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setId(jobId);
		infoReq.setType(JobConstant.TYPE_JOBLINKSENDJOB);
		JobInfo infoRes = new JobControllerBean().modifyJobChild(jobunitId, infoReq);

		JobLinkSendJobInfoResponse dtoRes = new JobLinkSendJobInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブ連携待機ジョブ設定の更新を行うAPI
	 */
	@PUT
	@Path("/setting/jobunit/{jobunitId}/joblinkRcvJob/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyJobLinkRcvJob")
	@RestLog(action=LogAction.Modify, target=LogTarget.JobLinkRcvJob, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobLinkRcvJobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response modifyJobLinkRcvJob(
			@PathParam("jobunitId") String jobunitId,
			@PathParam("jobId") String jobId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyJobLinkRcvJobBody", content = @Content(schema = @Schema(implementation = ModifyJobLinkRcvJobRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call modifyJobLinkRcvJob()");

		ModifyJobLinkRcvJobRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyJobLinkRcvJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromModifyJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setId(jobId);
		infoReq.setType(JobConstant.TYPE_JOBLINKRCVJOB);
		JobInfo infoRes = new JobControllerBean().modifyJobChild(jobunitId, infoReq);

		JobLinkRcvJobInfoResponse dtoRes = new JobLinkRcvJobInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ファイルチェックジョブ設定の更新を行うAPI
	 */
	@PUT
	@Path("/setting/jobunit/{jobunitId}/filecheckJob/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyFileCheckJob")
	@RestLog(action=LogAction.Modify, target=LogTarget.FileCheckJob, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FileCheckJobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response modifyFileCheckJob(
			@PathParam("jobunitId") String jobunitId,
			@PathParam("jobId") String jobId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyFileCheckJobBody", content = @Content(schema = @Schema(implementation = ModifyFileCheckJobRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call modifyFileCheckJob()");

		ModifyFileCheckJobRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyFileCheckJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromModifyJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setId(jobId);
		infoReq.setType(JobConstant.TYPE_FILECHECKJOB);
		JobInfo infoRes = new JobControllerBean().modifyJobChild(jobunitId, infoReq);

		FileCheckJobInfoResponse dtoRes = new FileCheckJobInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * RPAシナリオジョブ設定の更新を行うAPI
	 */
	@PUT
	@Path("/setting/jobunit/{jobunitId}/rpaJob/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyRpaJob")
	@RestLog(action=LogAction.Add, target=LogTarget.RpaJob, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaJobInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response modifyRpaJob(
			@PathParam("jobunitId") String jobunitId,
			@PathParam("jobId") String jobId,
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyRpaJobBody", content = @Content(schema = @Schema(implementation = ModifyRpaJobRequest.class))) String requestBody)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call modifyRpaJob()");

		ModifyRpaJobRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyRpaJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobInfo infoReq = converJobInfoFromModifyJobRequestDto(dtoReq);
		infoReq.setJobunitId(jobunitId);
		infoReq.setId(jobId);
		infoReq.setType(JobConstant.TYPE_RPAJOB);
		JobInfo infoRes = new JobControllerBean().modifyJobChild(jobunitId, infoReq);

		RpaJobInfoResponse dtoRes = new RpaJobInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブ設定の削除を行うAPI
	 */
	@DELETE
	@Path("/setting/jobunit/{jobunitId}/job/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteJob")
	@RestLog(action=LogAction.Delete, target = LogTarget.Jobunit, type = LogType.UPDATE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement,modeList={SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobInfoResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response deleteJob(
			@PathParam("jobunitId") String jobunitId,
			@PathParam("jobId") String jobId,
			@Context Request request,
			@Context UriInfo uriInfo)
			throws HinemosUnknown, JobMasterNotFound, JobInvalid, InvalidUserPass, InvalidRole, InvalidSetting, UserNotFound {
		m_log.info("call deleteJob()");

		JobInfo infoRes = new JobControllerBean().deleteJobChild(jobunitId, jobId);

		JobInfoResponseP1 dtoRes = new JobInfoResponseP1();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * セッションジョブの詳細情報の取得を行うAPI
	 */
	@GET
	@Path("/sessionJob_allDetail/{sessionId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobSessionAllDetailList")
	@RestLog(action=LogAction.Get, target = LogTarget.SessionJob, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.JobManagement, modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobSessionJobDetailResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJobSessionAllDetailList (
			@PathParam("sessionId") String sessionId,
			@Context Request request,
			@Context UriInfo uriInfo)
			throws JobInfoNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getAllDetailList()");

		JobControllerBean jobControllerBean = new JobControllerBean();
		JobSessionJobDetailResponse dtoRes = new JobSessionJobDetailResponse();

		// ジョブ詳細一覧情報
		JobTreeItem jobTreeItem = jobControllerBean.getJobDetailList(sessionId);
		setAllDetailList(sessionId, jobTreeItem, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 詳細情報設定
	 * 
	 * @param sessionId セッションID
	 * @param jobTreeItem ジョブ詳細一覧情報
	 * @param dto 詳細情報ResponseDto
	 */
	private void setAllDetailList(String sessionId, JobTreeItem jobTreeItem, JobSessionJobDetailResponse dto) 
		throws JobInfoNotFound, InvalidRole, HinemosUnknown {
		if (jobTreeItem != null) {
			if (jobTreeItem.getData() != null) {
				// ジョブ詳細一覧情報
				if (jobTreeItem.getDetail() != null) {
					JobDetailInfoResponse dtoResponse = new JobDetailInfoResponse();
					RestBeanUtil.convertBeanNoInvalid(jobTreeItem.getDetail(), dtoResponse);
					dto.getJobDetailList().add(dtoResponse);
				}

				// ノード詳細一覧情報
				List<JobNodeDetail> infoResNodeDetailList = new JobControllerBean().getNodeDetailList(
						sessionId, jobTreeItem.getData().getJobunitId(), jobTreeItem.getData().getId(), Locale.getDefault());
				for (JobNodeDetail infoResNodeDetail : infoResNodeDetailList) {
					JobNodeDetailResponse dtoResNodeDetail = new JobNodeDetailResponse();
					RestBeanUtil.convertBeanNoInvalid(infoResNodeDetail, dtoResNodeDetail);
					dto.getJobNodeDetailList().add(dtoResNodeDetail);
				}

				// ファイル転送一覧情報
				List<JobForwardFile> infoResFowardFileList = new JobControllerBean().getForwardFileList(
						sessionId, jobTreeItem.getData().getJobunitId(), jobTreeItem.getData().getId());
				for (JobForwardFile infoResFowardFile : infoResFowardFileList) {
					JobForwardFileResponse dtoResForwardFile = new JobForwardFileResponse();
					RestBeanUtil.convertBeanNoInvalid(infoResFowardFile, dtoResForwardFile);
					dto.getJobForwardFileList().add(dtoResForwardFile);
				}
			}
			if (jobTreeItem.getChildren() != null) {
				for (JobTreeItem childTreeItem : jobTreeItem.getChildren()) {
					setAllDetailList(sessionId, childTreeItem, dto);
				}
			}
		}
	}
	
	// RequestDtoでのJobTreeItem をinfo化。utilityでも利用するため publicとする
	public static JobTreeItem convertJobTreeItemFromDto( JobTreeItemRequest dto ) throws HinemosUnknown,InvalidSetting {
		if (dto == null) {
			return null;
		}
		JobTreeItem info = new JobTreeItem();
		setJobTreeItemRecursive(dto,info);
		return info;
	}
	private static void setJobTreeItemRecursive(JobTreeItemRequest dto ,JobTreeItem info )throws HinemosUnknown,InvalidSetting {

		//JobInfo
		info.setData(converJobInfoFromDto(dto.getData()));

		// JobTreeItemRequest 変換
		info.setChildren(new ArrayList<JobTreeItem>());
		for( JobTreeItemRequest dtoChild : dto.getChildren() ){
			JobTreeItem infoChild = new JobTreeItem();
			setJobTreeItemRecursive(dtoChild,infoChild);
			// 上位方向のリンクを補足
			infoChild.setParent(info);
			info.addChildren(infoChild);
		}
	}

	// requestDto の JobInfo を info化
	private static JobInfo converJobInfoFromDto(JobInfoRequest dto )throws HinemosUnknown,InvalidSetting {
		if (dto == null) {
			return null;
		}
		JobInfo info =new JobInfo();
		RestBeanUtil.convertBean(dto, info);
		//意味合いは同じだが名称が違うのでロジックで変換
		info.setPropertyFull(dto.isUpdateTaget());

		if(dto.getWaitRule() != null){
			// JobInfo->JobWaitRuleInfoの startDelayTimeValue と endDelayTimeValue の 個別変換
			info.getWaitRule()
				.setStart_delay_time_value(convertTimeStringToLong(dto.getWaitRule().getStartDelayTimeValue(), "startDelayTimeValue"));
			info.getWaitRule()
				.setEnd_delay_time_value(convertTimeStringToLong(dto.getWaitRule().getEndDelayTimeValue(), "endDelayTimeValue"));
			// JobInfo->JobWaitRuleInfoの jobRetryEndStatusにnullが入っている場合の適用
			if (dto.getWaitRule().getJobRetryEndStatus() == null) {
				info.getWaitRule().setJobRetryEndStatus(null);
			}
			// JobInfo->WaiRule->JobObjectGroupInfo->JobObjectInfo の個別変換（timeの個別変換向け）
			if (dto.getWaitRule().getObjectGroup() != null) {
				info.getWaitRule().setObjectGroup(new ArrayList<>());
				int orderNo = 0;
				for (JobObjectGroupInfoRequest dtoObjectGroup : dto.getWaitRule().getObjectGroup()) {
					if (dtoObjectGroup.getJobObjectList() == null) {
						continue;
					}
					JobObjectGroupInfo infoObjectGroup = new JobObjectGroupInfo();
					RestBeanUtil.convertBeanNoInvalid(dtoObjectGroup, infoObjectGroup);
					infoObjectGroup.setJobObjectList(new ArrayList<>());
					for (JobObjectInfoRequest dtoObject : dtoObjectGroup.getJobObjectList()) {
						JobObjectInfo infoObject = new JobObjectInfo();
						RestBeanUtil.convertBeanNoInvalid(dtoObject, infoObject);
						infoObject.setTime(convertTimeStringToLong(dtoObject.getTime(), "time"));
						infoObjectGroup.getJobObjectList().add(infoObject);
					}
					infoObjectGroup.setOrderNo(orderNo);
					infoObjectGroup.setIsGroup(infoObjectGroup.getJobObjectList().size() > 1);
					orderNo++;
					info.getWaitRule().getObjectGroup().add(infoObjectGroup);
				}
			}
		}
		
		return info;
	}

	// infoのJobTreeItem をResponseDto(P2)化
	private static JobTreeItemResponseP2 convertJobTreeItemP2FromInfo( JobTreeItem info ) throws HinemosUnknown {
		if(info==null){
			return null;
		}
		JobTreeItemResponseP2 dto = new JobTreeItemResponseP2();
		setJobTreeItemResponseP2Recursive(info,dto);
		return dto;
	}
	private static void setJobTreeItemResponseP2Recursive(JobTreeItem info, JobTreeItemResponseP2 dto)
			throws HinemosUnknown {

		dto.setData(convertJobInfoResponseFromInfo(info.getData()));

		// JobTreeItemRequest 変換
		dto.setChildren(new ArrayList<JobTreeItemResponseP2>());
		for( JobTreeItem infoChild : info.getChildren() ){
			JobTreeItemResponseP2 dtoChild = new JobTreeItemResponseP2();
			setJobTreeItemResponseP2Recursive(infoChild,dtoChild);
			dto.getChildren().add(dtoChild);
		}
	}

	// infoのJobTreeItem をResponseDto(P4)化
	private static JobTreeItemResponseP4 convertJobTreeItemP4FromInfo( JobTreeItem info ) throws HinemosUnknown {
		if(info==null){
			return null;
		}
		JobTreeItemResponseP4 dto = new JobTreeItemResponseP4();
		setJobTreeItemResponseP4Recursive(info,dto);
		return dto;
	}
	private static void setJobTreeItemResponseP4Recursive(JobTreeItem info, JobTreeItemResponseP4 dto)
			throws HinemosUnknown {

		dto.setData(convertJobInfoResponseFromInfo(info.getData()));
		dto.setDetail(convertDetailInfoResponseFromInfo(info.getDetail()));

		// JobTreeItem 変換
		dto.setChildren(new ArrayList<JobTreeItemResponseP4>());
		for( JobTreeItem infoChild : info.getChildren() ){
			JobTreeItemResponseP4 dtoChild = new JobTreeItemResponseP4();
			setJobTreeItemResponseP4Recursive(infoChild,dtoChild);
			dto.getChildren().add(dtoChild);
		}
	}

	// infoのJobInfo をResponseDto化
	private static JobInfoResponse convertJobInfoResponseFromInfo(JobInfo info) throws HinemosUnknown {
		if (info == null) {
			return null;
		}
		JobInfoResponse dto = new JobInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(info, dto);
		if (info.getWaitRule() != null) {

			// JobInfo->JobWaitRuleInfoRequestの startDelayTimeValue と
			// endDelayTimeValue の 個別変換
			dto.getWaitRule()
					.setStartDelayTimeValue(convertTimeLongToString(info.getWaitRule().getStart_delay_time_value()));
			dto.getWaitRule().setEndDelayTimeValue(convertTimeLongToString(info.getWaitRule().getEnd_delay_time_value()));

			// JobInfo->WaiRule->JobObjectGroupInfo->JobObjectInfo の個別変換（timeの個別変換向け）
			if (info.getWaitRule().getObjectGroup() != null) {
				dto.getWaitRule().setObjectGroup(new ArrayList<>());
				for (JobObjectGroupInfo infoObjectGroup : info.getWaitRule().getObjectGroup()) {
					if (infoObjectGroup.getJobObjectList() == null) {
						continue;
					}
					JobObjectGroupInfoResponse dtoObjectGroup = new JobObjectGroupInfoResponse();
					RestBeanUtil.convertBeanNoInvalid(infoObjectGroup, dtoObjectGroup);
					dtoObjectGroup.setJobObjectList(new ArrayList<>());
					for (JobObjectInfo infoObject : infoObjectGroup.getJobObjectList()) {
						JobObjectInfoResponse dtoObject = new JobObjectInfoResponse();
						RestBeanUtil.convertBeanNoInvalid(infoObject, dtoObject);
						dtoObject.setTime(convertTimeLongToString(infoObject.getTime()));
						dtoObjectGroup.getJobObjectList().add(dtoObject);
					}
					dto.getWaitRule().getObjectGroup().add(dtoObjectGroup);
				}
			}
		}
		return dto;
	}
	
	// infoのJobDetailInfo をResponseDto化
	private static JobDetailInfoResponse convertDetailInfoResponseFromInfo(JobDetailInfo info) throws HinemosUnknown {
		if (info == null) {
			return null;
		}
		JobDetailInfoResponse dto = new JobDetailInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(info, dto);
		//時刻はHH:mm:ss形式なので独自変換
		List<String> strTimeList = new ArrayList<String>();
		for (Long waitTime : info.getWaitRuleTimeList()) {
			strTimeList.add(convertTimeLongToString(waitTime));
		}
		dto.setWaitRuleTimeList(strTimeList);
		return dto;
	}

	// 時刻（HH:mm:ss 24時以降対応）をエポックミリ秒に変換
	private static Long convertTimeStringToLong( String time, String itemName ) throws InvalidSetting{
		if (time == null) {
			return null;
		}
		//クライアントとマネージャのタイムゾーンは一致している前提の処理である。
		// TODO REST対応 変換エラー時の InvalidSetting メッセージの編集。パラメータに項目名を追加して、その名称でエラーメッセージを変数する必要あり
		Long longTime;
		Date dateTime = null;
		try {
			dateTime = TimeStringConverter.parseTime(time);
		} catch (Exception e) {
			throw new InvalidSetting(e.getMessage(),e);
		}
		longTime = dateTime.getTime();
		return longTime;

	}

	//エポックミリ秒を 時刻（HH:mm:ss 24時以降対応）に変換
	private static String convertTimeLongToString(Long time) {
		// クライアントとマネージャのタイムゾーンは一致している前提の処理である。
		if (time == null) {
			return null;
		}
		String strTime = null;
		strTime = TimeStringConverter.formatTime(new Date(time));
		return strTime;
	}
	
	private JobInfo converJobInfoFromAddJobRequestDto(AbstractAddJobRequest dto) throws HinemosUnknown,InvalidSetting {
		if (dto == null) {
			return null;
		}
		
		JobInfo info =new JobInfo();
		RestBeanUtil.convertBean(dto, info);

		if(dto.getWaitRule() != null){
			// JobInfo->JobWaitRuleInfoの startDelayTimeValue と endDelayTimeValue の 個別変換
			info.getWaitRule().setStart_delay_time_value(
					convertTimeStringToLong(dto.getWaitRule().getStartDelayTimeValue(), "startDelayTimeValue"));
			info.getWaitRule()
				.setEnd_delay_time_value(convertTimeStringToLong(dto.getWaitRule().getEndDelayTimeValue(), "endDelayTimeValue"));

			// JobInfo->WaiRule->JobObjectGroupInfo->JobObjectInfo の個別変換（timeの個別変換向け）
			if (dto.getWaitRule().getObjectGroup() != null) {
				info.getWaitRule().setObjectGroup(new ArrayList<>());
				for (JobObjectGroupInfoRequest dtoObjectGroup : dto.getWaitRule().getObjectGroup()) {
					if (dtoObjectGroup.getJobObjectList() == null) {
						continue;
					}
					JobObjectGroupInfo infoObjectGroup = new JobObjectGroupInfo();
					RestBeanUtil.convertBeanNoInvalid(dtoObjectGroup, infoObjectGroup);
					infoObjectGroup.setJobObjectList(new ArrayList<>());
					for (JobObjectInfoRequest dtoObject : dtoObjectGroup.getJobObjectList()) {
						JobObjectInfo infoObject = new JobObjectInfo();
						RestBeanUtil.convertBeanNoInvalid(dtoObject, infoObject);
						infoObject.setTime(convertTimeStringToLong(dtoObject.getTime(), "time"));
						infoObjectGroup.getJobObjectList().add(infoObject);
					}
					info.getWaitRule().getObjectGroup().add(infoObjectGroup);
				}
			}
		}
		return info;
	}

	private JobInfo converJobInfoFromModifyJobRequestDto(AbstractModifyJobRequest dto) throws HinemosUnknown,InvalidSetting {
		if (dto == null) {
			return null;
		}
		
		JobInfo info =new JobInfo();
		RestBeanUtil.convertBean(dto, info);

		if(dto.getWaitRule() != null){
			// JobInfo->JobWaitRuleInfoの startDelayTimeValue と endDelayTimeValue の 個別変換
			info.getWaitRule().setStart_delay_time_value(
					convertTimeStringToLong(dto.getWaitRule().getStartDelayTimeValue(), "startDelayTimeValue"));
			info.getWaitRule()
					.setEnd_delay_time_value(convertTimeStringToLong(dto.getWaitRule().getEndDelayTimeValue(), "endDelayTimeValue"));

			// JobInfo->WaiRule->JobObjectGroupInfo->JobObjectInfo の個別変換（timeの個別変換向け）
			if (dto.getWaitRule().getObjectGroup() != null) {
				info.getWaitRule().setObjectGroup(new ArrayList<>());
				for (JobObjectGroupInfoRequest dtoObjectGroup : dto.getWaitRule().getObjectGroup()) {
					if (dtoObjectGroup.getJobObjectList() == null) {
						continue;
					}
					JobObjectGroupInfo infoObjectGroup = new JobObjectGroupInfo();
					RestBeanUtil.convertBeanNoInvalid(dtoObjectGroup, infoObjectGroup);
					infoObjectGroup.setJobObjectList(new ArrayList<>());
					for (JobObjectInfoRequest dtoObject : dtoObjectGroup.getJobObjectList()) {
						JobObjectInfo infoObject = new JobObjectInfo();
						RestBeanUtil.convertBeanNoInvalid(dtoObject, infoObject);
						infoObject.setTime(convertTimeStringToLong(dtoObject.getTime(), "time"));
						infoObjectGroup.getJobObjectList().add(infoObject);
					}
					info.getWaitRule().getObjectGroup().add(infoObjectGroup);
				}
			}
		}
		return info;
	}

	@POST
	@Path("/setting/kick/schedule")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddSchedule")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobKickResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.ADD })
	@RestLog(action = LogAction.Add, target = LogTarget.Schedule, type = LogType.UPDATE)
	public Response addSchedule(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "AddScheduleRequest", content = @Content(schema = @Schema(implementation = AddScheduleRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobKickDuplicate {
		m_log.info("call addSchedule()");

		AddScheduleRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddScheduleRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobSchedule infoReq = new JobSchedule();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setType(JobKickConstant.TYPE_SCHEDULE);

		JobSchedule retOrg = new JobControllerBean().addSchedule(infoReq);

		JobKickResponse resDto = new JobKickResponse();
		RestBeanUtil.convertBean(retOrg, resDto);
		resDto.setType(JobKickTypeEnum.SCHEDULE);

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	@POST
	@Path("/setting/kick/filecheck")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddFileCheck")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobKickResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.ADD })
	@RestLog(action = LogAction.Add, target = LogTarget.Filecheck, type = LogType.UPDATE)
	public Response addFileCheck(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "AddFileCheckRequest", content = @Content(schema = @Schema(implementation = AddFileCheckRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobKickDuplicate {
		m_log.info("call addFileCheck()");

		AddFileCheckRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddFileCheckRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobFileCheck infoReq = new JobFileCheck();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setType(JobKickConstant.TYPE_FILECHECK);

		JobFileCheck retOrg = new JobControllerBean().addFileCheck(infoReq);

		JobKickResponse resDto = new JobKickResponse();
		RestBeanUtil.convertBean(retOrg, resDto);
		resDto.setType(JobKickTypeEnum.FILECHECK);

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	@POST
	@Path("/setting/kick/manual")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddJobManual")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobKickResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.ADD })
	@RestLog(action = LogAction.Add, target = LogTarget.Manual, type = LogType.UPDATE)
	public Response addJobManual(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "AddJobManualRequest", content = @Content(schema = @Schema(implementation = AddJobManualRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobKickDuplicate {
		m_log.info("call addJobManual()");

		AddJobManualRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddJobManualRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobKick infoReq = new JobKick();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setType(JobKickConstant.TYPE_MANUAL);

		JobKick retOrg = new JobControllerBean().addJobManual(infoReq);

		JobKickResponse resDto = new JobKickResponse();
		RestBeanUtil.convertBean(retOrg, resDto);
		resDto.setType(JobKickTypeEnum.MANUAL);

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	@POST
	@Path("/setting/kick/joblinkrcv")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddJobLinkRcv")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobKickResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.ADD })
	@RestLog(action = LogAction.Add, target = LogTarget.JobLinkRcv , type = LogType.UPDATE)
	public Response addJobLinkRcv(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "AddJobLinkRcvRequest", content = @Content(schema = @Schema(implementation = AddJobLinkRcvRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobKickDuplicate {
		m_log.info("call addJobLinkRcv()");

		AddJobLinkRcvRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddJobLinkRcvRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobLinkRcv infoReq = new JobLinkRcv();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setType(JobKickConstant.TYPE_JOBLINKRCV);

		JobLinkRcv retOrg = new JobControllerBean().addJobLinkRcv(infoReq);

		JobKickResponse resDto = new JobKickResponse();
		RestBeanUtil.convertBean(retOrg, resDto);
		resDto.setType(JobKickTypeEnum.JOBLINKRCV);

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	@PUT
	@Path("/setting/kick/schedule/{jobKickId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifySchedule")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobKickResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@RestLog(action = LogAction.Modify, target = LogTarget.Schedule, type = LogType.UPDATE)
	public Response modifySchedule(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("jobKickId") String jobKickId,
			@RequestBody(description = "ModifyScheduleRequest", content = @Content(schema = @Schema(implementation = ModifyScheduleRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobInfoNotFound, JobMasterNotFound {
		m_log.info("call modifySchedule()");

		ModifyScheduleRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyScheduleRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobSchedule infoReq = new JobSchedule();
		RestBeanUtil.convertBean(dtoReq, infoReq);

		infoReq.setId(jobKickId);
		infoReq.setType(JobKickConstant.TYPE_SCHEDULE);

		JobSchedule retOrg = new JobControllerBean().modifySchedule(infoReq);

		JobKickResponse resDto = new JobKickResponse();
		RestBeanUtil.convertBean(retOrg, resDto);
		resDto.setType(JobKickTypeEnum.SCHEDULE);

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	@PUT
	@Path("/setting/kick/filecheck/{jobKickId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyFileCheck")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobKickResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@RestLog(action = LogAction.Modify, target = LogTarget.Filecheck, type = LogType.UPDATE)
	public Response modifyFileCheck(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("jobKickId") String jobKickId,
			@RequestBody(description = "ModifyFileCheckRequest", content = @Content(schema = @Schema(implementation = ModifyFileCheckRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobInfoNotFound, JobMasterNotFound {
		m_log.info("call modifyFileCheck()");

		ModifyFileCheckRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyFileCheckRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobFileCheck infoReq = new JobFileCheck();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setId(jobKickId);
		infoReq.setType(JobKickConstant.TYPE_FILECHECK);

		JobFileCheck retOrg = new JobControllerBean().modifyFileCheck(infoReq);

		JobKickResponse resDto = new JobKickResponse();
		RestBeanUtil.convertBean(retOrg, resDto);
		resDto.setType(JobKickTypeEnum.FILECHECK);

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	@PUT
	@Path("/setting/kick/manual/{jobKickId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyJobManual")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobKickResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@RestLog(action = LogAction.Modify, target = LogTarget.Manual, type = LogType.UPDATE)
	public Response modifyJobManual(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("jobKickId") String jobKickId,
			@RequestBody(description = "ModifyJobManualRequest", content = @Content(schema = @Schema(implementation = ModifyJobManualRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobInfoNotFound, JobMasterNotFound {
		m_log.info("call modifyJobManual()");

		ModifyJobManualRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyJobManualRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobKick infoReq = new JobKick();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setId(jobKickId);
		infoReq.setType(JobKickConstant.TYPE_MANUAL);

		JobKick retOrg = new JobControllerBean().modifyJobManual(infoReq);

		JobKickResponse resDto = new JobKickResponse();
		RestBeanUtil.convertBean(retOrg, resDto);

		infoReq.setId(jobKickId);
		resDto.setType(JobKickTypeEnum.MANUAL);

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	@PUT
	@Path("/setting/kick/joblinkrcv/{jobKickId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyJobLinkRcv")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobKickResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@RestLog(action = LogAction.Modify, target = LogTarget.JobLinkRcv, type = LogType.UPDATE)
	public Response modifyJobLinkRcv(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("jobKickId") String jobKickId,
			@RequestBody(description = "ModifyJobLinkRcvRequest", content = @Content(schema = @Schema(implementation = ModifyJobLinkRcvRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobInfoNotFound, JobMasterNotFound {
		m_log.info("call modifyJobLinkRcv()");

		ModifyJobLinkRcvRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyJobLinkRcvRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobLinkRcv infoReq = new JobLinkRcv();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setId(jobKickId);
		infoReq.setType(JobKickConstant.TYPE_JOBLINKRCV);

		JobLinkRcv retOrg = new JobControllerBean().modifyJobLinkRcv(infoReq);

		JobKickResponse resDto = new JobKickResponse();
		RestBeanUtil.convertBean(retOrg, resDto);
		resDto.setType(JobKickTypeEnum.JOBLINKRCV);

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	@DELETE
	@Path("/setting/kick/schedule")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteSchedule")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobKickResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@RestLog(action = LogAction.Delete, target = LogTarget.Schedule, type = LogType.UPDATE)
	public Response deleteSchedule(@Context Request request, @Context UriInfo uriInfo,
			@ArrayTypeParam @QueryParam(value = "jobkickIds") String jobkickIds)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, JobInfoNotFound, JobMasterNotFound {
		m_log.info("call deleteSchedule()");
		m_log.info("jobkickIds=" + jobkickIds);

		List<String> jobkickIdList = new ArrayList<>();
		if(jobkickIds != null && !jobkickIds.isEmpty()) {
			jobkickIdList = Arrays.asList(jobkickIds.split(","));
		}
		
		List<JobSchedule> infoResList = new JobControllerBean().deleteSchedule(jobkickIdList);

		List<JobKickResponse> resDtoList = new ArrayList<>();
		for (JobSchedule info : infoResList) {
			JobKickResponse dto = new JobKickResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dto.setType(JobKickTypeEnum.SCHEDULE);
			resDtoList.add(dto);
		}

		RestLanguageConverter.convertMessages(resDtoList);
		return Response.status(Status.OK).entity(resDtoList).build();
	}

	@DELETE
	@Path("/setting/kick/filecheck")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteFileCheck")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobKickResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@RestLog(action = LogAction.Delete, target = LogTarget.Filecheck, type = LogType.UPDATE)
	public Response deleteFileCheck(@Context Request request, @Context UriInfo uriInfo,
			@ArrayTypeParam @QueryParam(value = "jobkickIds") String jobkickIds)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, JobInfoNotFound, JobMasterNotFound {
		m_log.info("call deleteFileCheck()");
		m_log.info("jobkickIds=" + jobkickIds);

		List<String> jobkickIdList = new ArrayList<>();
		if(jobkickIds != null && !jobkickIds.isEmpty()) {
			jobkickIdList = Arrays.asList(jobkickIds.split(","));
		}
		
		List<JobFileCheck> infoResList = new JobControllerBean().deleteFileCheck(jobkickIdList);

		List<JobKickResponse> resDtoList = new ArrayList<>();
		for (JobFileCheck info : infoResList) {
			JobKickResponse dto = new JobKickResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dto.setType(JobKickTypeEnum.FILECHECK);
			resDtoList.add(dto);
		}

		RestLanguageConverter.convertMessages(resDtoList);
		return Response.status(Status.OK).entity(resDtoList).build();
	}

	@DELETE
	@Path("/setting/kick/manual")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteJobManual")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobKickResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@RestLog(action = LogAction.Delete, target = LogTarget.Manual, type = LogType.UPDATE)
	public Response deleteJobManual(@Context Request request, @Context UriInfo uriInfo,
			@ArrayTypeParam @QueryParam(value = "jobkickIds") String jobkickIds)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, JobInfoNotFound, JobMasterNotFound {
		m_log.info("call deleteJobManual()");
		m_log.info("jobkickIds=" + jobkickIds);

		List<String> jobkickIdList = new ArrayList<>();
		if(jobkickIds != null && !jobkickIds.isEmpty()) {
			jobkickIdList = Arrays.asList(jobkickIds.split(","));
		}
		
		List<JobKick> infoResList = new JobControllerBean().deleteJobManual(jobkickIdList);

		List<JobKickResponse> resDtoList = new ArrayList<>();
		for (JobKick info : infoResList) {
			JobKickResponse dto = new JobKickResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dto.setType(JobKickTypeEnum.MANUAL);
			resDtoList.add(dto);
		}

		RestLanguageConverter.convertMessages(resDtoList);
		return Response.status(Status.OK).entity(resDtoList).build();
	}

	@DELETE
	@Path("/setting/kick/joblinkrcv")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteJobLinkRcv")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobKickResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@RestLog(action = LogAction.Delete, target = LogTarget.JobLinkRcv, type = LogType.UPDATE)
	public Response deleteJobLinkRcv(@Context Request request, @Context UriInfo uriInfo,
			@ArrayTypeParam @QueryParam(value = "jobkickIds") String jobkickIds)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, JobInfoNotFound, JobMasterNotFound {
		m_log.info("call deleteJobLinkRcv()");
		m_log.info("jobkickIds=" + jobkickIds);

		List<String> jobkickIdList = new ArrayList<>();
		if(jobkickIds != null && !jobkickIds.isEmpty()) {
			jobkickIdList = Arrays.asList(jobkickIds.split(","));
		}
		
		List<JobLinkRcv> infoResList = new JobControllerBean().deleteJobLinkRcv(jobkickIdList);

		List<JobKickResponse> resDtoList = new ArrayList<>();
		for (JobLinkRcv info : infoResList) {
			JobKickResponse dto = new JobKickResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dto.setType(JobKickTypeEnum.JOBLINKRCV);
			resDtoList.add(dto);
		}

		RestLanguageConverter.convertMessages(resDtoList);
		return Response.status(Status.OK).entity(resDtoList).build();
	}

	@DELETE
	@Path("/setting/kick")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteJobKick")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobKickResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@RestLog(action = LogAction.Delete, target = LogTarget.Kick, type = LogType.UPDATE)
	public Response deleteJobKick(@Context Request request, @Context UriInfo uriInfo,
			@ArrayTypeParam @QueryParam(value = "jobkickIds") String jobkickIds)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, JobInfoNotFound, JobMasterNotFound {
		m_log.info("call deleteJobKick()");
		m_log.info("jobkickIds=" + jobkickIds);

		List<String> jobkickIdList = new ArrayList<>();
		if(jobkickIds != null && !jobkickIds.isEmpty()) {
			jobkickIdList = Arrays.asList(jobkickIds.split(","));
		}
		
		List<JobKick> infoResList = new JobControllerBean().deleteJobKick(jobkickIdList);

		List<JobKickResponse> resDtoList = new ArrayList<>();
		for (JobKick info : infoResList) {
			JobKickResponse dto = new JobKickResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			resDtoList.add(dto);
		}

		RestLanguageConverter.convertMessages(resDtoList);
		return Response.status(Status.OK).entity(resDtoList).build();
	}

	@GET
	@Path("/setting/kick/schedule/{jobKickId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobSchedule")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobScheduleResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.Schedule, type = LogType.REFERENCE)
	public Response getJobSchedule(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("jobKickId") String jobKickId)
			throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJobSchedule()");

		JobSchedule retOrg = new JobControllerBean().getJobSchedule(jobKickId);

		JobScheduleResponse dto = new JobScheduleResponse();
		RestBeanUtil.convertBeanNoInvalid(retOrg, dto);

		RestLanguageConverter.convertMessages(dto);
		return Response.status(Status.OK).entity(dto).build();
	}

	@GET
	@Path("/setting/kick/filecheck/{jobKickId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetFileCheck")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobFileCheckResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.Filecheck, type = LogType.REFERENCE)
	public Response getFileCheck(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("jobKickId") String jobKickId)
			throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getFileCheck()");

		JobFileCheck retOrg = new JobControllerBean().getJobFileCheck(jobKickId);

		JobFileCheckResponse dto = new JobFileCheckResponse();
		RestBeanUtil.convertBeanNoInvalid(retOrg, dto);

		RestLanguageConverter.convertMessages(dto);
		return Response.status(Status.OK).entity(dto).build();
	}

	@GET
	@Path("/setting/kick/manual/{jobKickId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobManual")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobManualResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.Manual, type = LogType.REFERENCE)
	public Response getJobManual(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("jobKickId") String jobKickId)
			throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJobManual()");

		JobKick retOrg = new JobControllerBean().getJobManual(jobKickId);

		JobManualResponse dto = new JobManualResponse();
		RestBeanUtil.convertBeanNoInvalid(retOrg, dto);

		RestLanguageConverter.convertMessages(dto);
		return Response.status(Status.OK).entity(dto).build();
	}

	@GET
	@Path("/setting/kick/joblinkrcv/{jobKickId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobLinkRcv")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobLinkRcvResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target =LogTarget.JobLinkRcv, type = LogType.REFERENCE)
	public Response getJobLinkRcv(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("jobKickId") String jobKickId)
			throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJobLinkRcv()");

		JobLinkRcv retOrg = new JobControllerBean().getJobLinkRcv(jobKickId);

		JobLinkRcvResponse dto = new JobLinkRcvResponse();
		RestBeanUtil.convertBeanNoInvalid(retOrg, dto);

		RestLanguageConverter.convertMessages(dto);
		return Response.status(Status.OK).entity(dto).build();
	}

	@GET
	@Path("/setting/kick/{jobKickId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobKick")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobKickResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.Kick, type = LogType.REFERENCE)
	public Response getJobKick(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("jobKickId") String jobKickId)
			throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJobKick()");

		JobKick retOrg = new JobControllerBean().getJobKick(jobKickId);

		JobKickResponse dto = new JobKickResponse();
		RestBeanUtil.convertBeanNoInvalid(retOrg, dto);

		RestLanguageConverter.convertMessages(dto);
		return Response.status(Status.OK).entity(dto).build();
	}

	@PUT
	@Path("/setting/kick_valid")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SetJobKickStatus")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobKickResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@RestLog(action = LogAction.Modify, target = LogTarget.Kick, type = LogType.UPDATE)
	public Response setJobKickStatus(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "SetJobKickStatusRequest", content = @Content(schema = @Schema(implementation = SetJobKickStatusRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, JobMasterNotFound, JobInfoNotFound {
		m_log.info("call setJobKickStatus()");

		SetJobKickStatusRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				SetJobKickStatusRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		List<JobKick> infoResList = new JobControllerBean().setJobKickStatus(dtoReq.getJobKickId(),
				dtoReq.getValidFlag());

		List<JobKickResponse> resDtoList = new ArrayList<>();
		for (JobKick info : infoResList) {
			JobKickResponse dto = new JobKickResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			resDtoList.add(dto);
		}

		RestLanguageConverter.convertMessages(resDtoList);
		return Response.status(Status.OK).entity(resDtoList).build();
	}

	@GET
	@Path("/setting/kick")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobKickList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobKickResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.Kick, type = LogType.REFERENCE)
	public Response getJobKickList(@Context Request request, @Context UriInfo uriInfo)
			throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJobKickList()");

		List<JobKick> retOrgList = new JobControllerBean().getJobKickList();
		List<JobKickResponse> resDtoList = new ArrayList<>();
		for (JobKick info : retOrgList) {
			JobKickResponse dto = new JobKickResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			resDtoList.add(dto);
		}

		RestLanguageConverter.convertMessages(resDtoList);
		return Response.status(Status.OK).entity(resDtoList).build();
	}

	@POST
	@Path("/setting/kick_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobKickListByCondition")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetJobKickListByConditionResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.Kick, type = LogType.REFERENCE)
	public Response getJobKickListByCondition(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "GetJobKickListByConditionRequest", content = @Content(schema = @Schema(implementation = GetJobKickListByConditionRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call getJobKickListByCondition()");
		GetJobKickListByConditionRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				GetJobKickListByConditionRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		Integer size = dtoReq.getSize();

		JobKickFilterInfoRequest dto = dtoReq.getJobKickFilterInfo();
		JobKickFilterInfo infoReq = new JobKickFilterInfo();
		RestBeanUtil.convertBean(dto, infoReq);

		List<JobKick> retOrgList = new JobControllerBean().getJobKickList(infoReq);

		List<JobKickResponse> resDtoList = new ArrayList<>();
		int recCount = 0;
		for (JobKick info : retOrgList) {
			JobKickResponse dtoRec = new JobKickResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dtoRec);
			resDtoList.add(dtoRec);
			recCount++;
			if (size != null && recCount >= size) {
				break;
			}
		}

		RestLanguageConverter.convertMessages(resDtoList);
		GetJobKickListByConditionResponse dtoRes = new GetJobKickListByConditionResponse();
		dtoRes.setTotal(retOrgList.size());
		dtoRes.setJobKickList(resDtoList);
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	@POST
	@Path("/setting/kick/schedule_plan")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetPlanList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobPlanResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.Schedule, type = LogType.REFERENCE)
	public Response getPlanList(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "GetPlanListRequest", content = @Content(schema = @Schema(implementation = GetPlanListRequest.class))) String requestBody)
			throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call getPlanList()");

		GetPlanListRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, GetPlanListRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobPlanFilter infoReq = new JobPlanFilter();
		RestBeanUtil.convertBean(dtoReq, infoReq);

		int limit = 0;
		if (dtoReq.getSize() != null) {
			limit = dtoReq.getSize();
		}

		List<JobPlan> retOrgList = new JobControllerBean().getPlanList(infoReq, limit);
		List<JobPlanResponse> resDtoList = new ArrayList<>();
		for (JobPlan info : retOrgList) {
			JobPlanResponse dto = new JobPlanResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			resDtoList.add(dto);
		}

		RestLanguageConverter.convertMessages(resDtoList);
		return Response.status(Status.OK).entity(resDtoList).build();
	}

	@GET
	@Path("/operationProp_availableStartOperation/{sessionId}/jobunit/{jobunitId}/job/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetAvailableStartOperationSessionJob")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobOperationPropResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.EXEC })
	@RestLog(action = LogAction.Get, target = LogTarget.OperationProp, type = LogType.REFERENCE)
	public Response getAvailableStartOperationSessionJob(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("sessionId") String sessionId, @PathParam("jobunitId") String jobunitId, @PathParam("jobId") String jobId) 
					throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getAvailableStartOperationSessionJob : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);
		
		List<ControlEnum> dtolist = new JobControllerBean().getAvailableStartOperationSessionJob(sessionId, jobunitId, jobId, Locale.getDefault());

		JobOperationPropResponse dto = new JobOperationPropResponse();
		dto.setAvailableOperationList(dtolist);

		RestLanguageConverter.convertMessages(dto);

		return Response.status(Status.OK).entity(dto).build();
	}
	
	@GET
	@Path("/operationProp_availableStartOperation/{sessionId}/jobunit/{jobunitId}/job/{jobId}/facility/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetAvailableStartOperationSessionNode")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobOperationPropResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.EXEC })
	@RestLog(action = LogAction.Get, target = LogTarget.OperationProp, type = LogType.REFERENCE)
	public Response getAvailableStartOperationSessionNode(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("sessionId") String sessionId, @PathParam("jobunitId") String jobunitId, @PathParam("jobId") String jobId, @PathParam("facilityId") String facilityId) 
					throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getAvailableStartOperationSessionNode : sessionId=" + sessionId +
				", jobunitId=" + jobunitId + ", jobId=" + jobId + ", facilityId=" + facilityId);
		
		List<ControlEnum> dtolist = new JobControllerBean().getAvailableStartOperationSessionNode(sessionId, jobunitId, jobId, facilityId, Locale.getDefault());

		JobOperationPropResponse dto = new JobOperationPropResponse();
		dto.setAvailableOperationList(dtolist);

		RestLanguageConverter.convertMessages(dto);

		return Response.status(Status.OK).entity(dto).build();
	}
	
	@GET
	@Path("/operationProp_availableStopOperation/{sessionId}/jobunit/{jobunitId}/job/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetAvailableStopOperationSessionJob")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobOperationPropResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.EXEC })
	@RestLog(action = LogAction.Get, target = LogTarget.OperationProp, type = LogType.REFERENCE)
	public Response getAvailableStopOperationSessionJob(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("sessionId") String sessionId, @PathParam("jobunitId") String jobunitId, @PathParam("jobId") String jobId) 
					throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getAvailableStopOperationSessionJob : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		List<ControlEnum> dtolist = new JobControllerBean().getAvailableStopOperationSessionJob(sessionId, jobunitId, jobId, Locale.getDefault());

		JobOperationPropResponse dto = new JobOperationPropResponse();
		dto.setAvailableOperationList(dtolist);

		RestLanguageConverter.convertMessages(dto);

		return Response.status(Status.OK).entity(dto).build();
	}
	
	@GET
	@Path("/operationProp_availableStopOperation/{sessionId}/jobunit/{jobunitId}/job/{jobId}/facility/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetAvailableStopOperationSessionNode")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobOperationPropResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.EXEC })
	@RestLog(action = LogAction.Get, target = LogTarget.OperationProp, type = LogType.REFERENCE)
	public Response getAvailableStopOperationSessionNode(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("sessionId") String sessionId, @PathParam("jobunitId") String jobunitId, @PathParam("jobId") String jobId, @PathParam("facilityId") String facilityId) 
					throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getAvailableStopOperationSessionNode : sessionId=" + sessionId +
				", jobunitId=" + jobunitId + ", jobId=" + jobId + ", facilityId=" + facilityId);

		List<ControlEnum> dtolist = new JobControllerBean().getAvailableStopOperationSessionNode(sessionId, jobunitId, jobId, facilityId, Locale.getDefault());

		JobOperationPropResponse dto = new JobOperationPropResponse();
		dto.setAvailableOperationList(dtolist);

		RestLanguageConverter.convertMessages(dto);

		return Response.status(Status.OK).entity(dto).build();
	}
	
	@POST
	@Path("/session_exec/jobunit/{jobunitId}/job/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "RunJob")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RunJobResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.EXEC , SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ})
	@RestLog(action = LogAction.Exec, target = LogTarget.Session, type = LogType.UPDATE)
	public Response runJob(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "RunJobRequest", content = @Content(schema = @Schema(implementation = RunJobRequest.class))) String requestBody, 
			@PathParam("jobunitId") String jobunitId, @PathParam("jobId") String jobId)
			throws  FacilityNotFound, HinemosUnknown, JobInfoNotFound, JobMasterNotFound, InvalidUserPass, InvalidRole, JobSessionDuplicate, InvalidSetting
	{
		RunJobRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, RunJobRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();
		
		JobTriggerInfo triggerInfo = new JobTriggerInfo();
		RestBeanUtil.convertBean(dtoReq, triggerInfo);
		
		m_log.debug("runJob : jobunitId=" + jobunitId + ", jobId=" + jobId + ", info=" + null + ", triggerInfo=" + triggerInfo);

		String sessionId = new JobControllerBean().runJob(jobunitId, jobId, null, triggerInfo);
		
		RunJobResponse dto = new RunJobResponse();
		dto.setSessionId(sessionId);

		RestLanguageConverter.convertMessages(dto);

		return Response.status(Status.OK).entity(dto).build();
	}
	
	@POST
	@Path("/sessionJob_operation/{sessionId}/jobunit/{jobunitId}/job/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "OperationSessionJob")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.EXEC })
	@RestLog(action = LogAction.Operation, target = LogTarget.SessionJob, type = LogType.UPDATE)
	public Response operationSessionJob(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "JobOperationRequest", content = @Content(schema = @Schema(implementation = JobOperationRequest.class))) String requestBody, 
			@PathParam("sessionId") String sessionId, @PathParam("jobunitId") String jobunitId, @PathParam("jobId") String jobId) throws HinemosUnknown, JobInfoNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		JobOperationRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, JobOperationRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();
		
		JobOperationInfo property = new JobOperationInfo();
		RestBeanUtil.convertBean(dtoReq, property);
		property.setSessionId(sessionId);
		property.setJobunitId(jobunitId);
		property.setJobId(jobId);
		
		m_log.debug("operationJob : nodeOperationInfo=" + property);

		new JobControllerBean().operationSessionJob(property);
		
		// 戻り値はvoidとする
		return Response.status(Status.OK).build();
	}
	
	@POST
	@Path("/sessionNode_operation/{sessionId}/jobunit/{jobunitId}/job/{jobId}/facilityId/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "OperationSessionNode")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.EXEC })
	@RestLog(action = LogAction.Operation, target = LogTarget.SessionNode, type = LogType.UPDATE)
	public Response operationSessionNode(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "JobOperationRequest", content = @Content(schema = @Schema(implementation = JobOperationRequest.class))) String requestBody, 
			@PathParam("sessionId") String sessionId, @PathParam("jobunitId") String jobunitId, @PathParam("jobId") String jobId, @PathParam("facilityId") String facilityId) throws HinemosUnknown, JobInfoNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		JobOperationRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, JobOperationRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();
		
		JobOperationInfo property = new JobOperationInfo();
		RestBeanUtil.convertBean(dtoReq, property);
		property.setSessionId(sessionId);
		property.setJobunitId(jobunitId);
		property.setJobId(jobId);
		property.setFacilityId(facilityId);
		
		m_log.debug("operationJob : nodeOperationInfo=" + property);

		new JobControllerBean().operationSessionNode(property);
		
		// 戻り値はvoidとする
		return Response.status(Status.OK).build();
	}
	
	@POST
	@Path("/setting/queue_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobQueueListSearch")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobQueueSettingViewInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.Queue, type = LogType.REFERENCE)
	public Response getJobQueueListSearch(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "GetJobQueueListSearchRequest", content = @Content(schema = @Schema(implementation = GetJobQueueListSearchRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call getJobQueueListSearch()");
		GetJobQueueListSearchRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				GetJobQueueListSearchRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobQueueSettingViewFilter infoReq = new JobQueueSettingViewFilter();
		RestBeanUtil.convertBean(dtoReq, infoReq);

		JobQueueSettingViewInfo retOrg = new JobControllerBean().getJobQueueSettingViewInfo(infoReq);
		JobQueueSettingViewInfoResponse resDto = new JobQueueSettingViewInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(retOrg, resDto);

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	@GET
	@Path("/setting/job_referrerQueue/{queueId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobInfoReferrerQueue")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobInfoReferrerQueueResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.Job, type = LogType.REFERENCE)
	public Response getJobInfoReferrerQueue(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("queueId") String queueId)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, JobQueueNotFound {
		m_log.info("call getJobInfoReferrerQueue()");

		JobQueueReferrerViewInfo retOrg = new JobControllerBean().getJobQueueReferrerViewInfo(queueId);

		JobInfoReferrerQueueResponse dto = new JobInfoReferrerQueueResponse();
		RestBeanUtil.convertBeanNoInvalid(retOrg, dto);

		RestLanguageConverter.convertMessages(dto);
		return Response.status(Status.OK).entity(dto).build();
	}

	@POST
	@Path("/queueActivity_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobQueueItemInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobQueueItemInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.QueueActivity, type = LogType.REFERENCE)
	public Response getJobQueueItemInfo(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "GetJobQueueActivityInfoRequest", content = @Content(schema = @Schema(implementation = GetJobQueueActivityInfoRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call getJobQueueItemInfo()");

		GetJobQueueActivityInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				GetJobQueueActivityInfoRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobQueueActivityViewFilter infoReq = new JobQueueActivityViewFilter();
		RestBeanUtil.convertBean(dtoReq, infoReq);

		JobQueueActivityViewInfo retOrg = new JobControllerBean().getJobQueueActivityViewInfo(infoReq);
		List<JobQueueItemInfoResponse> resDtoList = new ArrayList<>();
		JobQueueItemInfoResponse dto = new JobQueueItemInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(retOrg, dto);
		resDtoList.add(dto);

		RestLanguageConverter.convertMessages(resDtoList);
		return Response.status(Status.OK).entity(resDtoList).build();
	}

	@GET
	@Path("/queueActivity_detail/{queueId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobQueueContentsInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobQueueItemContentResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.QueueActivity, type = LogType.REFERENCE)
	public Response getJobQueueContentsInfo(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("queueId") String queueId)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, JobQueueNotFound {
		m_log.info("call getJobQueueContentsInfo()");

		JobQueueContentsViewInfo retOrg = new JobControllerBean().getJobQueueContentsViewInfo(queueId);

		JobQueueItemContentResponse dto = new JobQueueItemContentResponse();
		RestBeanUtil.convertBeanNoInvalid(retOrg, dto);

		RestLanguageConverter.convertMessages(dto);
		return Response.status(Status.OK).entity(dto).build();
	}

	@GET
	@Path("/setting/queue")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobQueueList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobQueueResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.Queue, type = LogType.REFERENCE)
	public Response getJobQueueList(@Context Request request, @Context UriInfo uriInfo,
			@QueryParam("roleId") String roleId) throws HinemosUnknown, InvalidRole {
		m_log.info("call getJobQueueList()");

		List<JobQueueSetting> retOrgList = new JobControllerBean().getJobQueueList(roleId);
		List<JobQueueResponse> resDtoList = new ArrayList<>();
		for (JobQueueSetting info : retOrgList) {
			JobQueueResponse dto = new JobQueueResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			resDtoList.add(dto);
		}

		RestLanguageConverter.convertMessages(resDtoList);
		return Response.status(Status.OK).entity(resDtoList).build();
	}

	@GET
	@Path("/setting/queue/{queueId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobQueue")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobQueueResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.Queue, type = LogType.REFERENCE)
	public Response getJobQueue(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("queueId") String queueId)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, JobQueueNotFound {
		m_log.info("call getJobQueue()");

		JobQueueSetting retOrg = new JobControllerBean().getJobQueue(queueId);

		JobQueueResponse dto = new JobQueueResponse();
		RestBeanUtil.convertBeanNoInvalid(retOrg, dto);

		RestLanguageConverter.convertMessages(dto);
		return Response.status(Status.OK).entity(dto).build();
	}

	@POST
	@Path("/setting/queue")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddJobQueue")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobQueueResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.ADD })
	@RestLog(action = LogAction.Add, target = LogTarget.Queue, type = LogType.UPDATE)
	public Response addJobQueue(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "AddJobQueueRequest", content = @Content(schema = @Schema(implementation = AddJobQueueRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidRole, InvalidSetting {
		m_log.info("call addJobQueue()");

		AddJobQueueRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddJobQueueRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobQueueSetting infoReq = new JobQueueSetting();
		RestBeanUtil.convertBean(dtoReq, infoReq);

		JobQueueSetting retOrg = new JobControllerBean().addJobQueue(infoReq);

		JobQueueResponse resDto = new JobQueueResponse();
		RestBeanUtil.convertBean(retOrg, resDto);

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	@PUT
	@Path("/setting/queue/{queueId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyJobQueue")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobQueueResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@RestLog(action = LogAction.Modify, target = LogTarget.Queue, type = LogType.UPDATE)
	public Response modifyJobQueue(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("queueId") String queueId,
			@RequestBody(description = "ModifyJobQueueRequest", content = @Content(schema = @Schema(implementation = ModifyJobQueueRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidRole, InvalidSetting, JobQueueNotFound {
		m_log.info("call modifyJobQueue()");

		ModifyJobQueueRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyJobQueueRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobQueueSetting infoReq = new JobQueueSetting();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setQueueId(queueId);

		JobQueueSetting retOrg = new JobControllerBean().modifyJobQueue(infoReq);

		JobQueueResponse resDto = new JobQueueResponse();
		RestBeanUtil.convertBean(retOrg, resDto);

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	@DELETE
	@Path("/setting/queue")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteJobQueue")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobQueueResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@RestLog(action = LogAction.Delete, target = LogTarget.Queue, type = LogType.UPDATE)
	public Response deleteJobQueue(@Context Request request, @Context UriInfo uriInfo,
			@ArrayTypeParam @QueryParam(value = "queueIds") String queueIds)
			throws HinemosUnknown, InvalidSetting, InvalidRole, JobQueueNotFound {
		m_log.info("call deleteJobQueue()");
		m_log.info("queueIds=" + queueIds);

		List<String> queueIdList = new ArrayList<>();
		if(queueIds != null && !queueIds.isEmpty()) {
			queueIdList = Arrays.asList(queueIds.split(","));
		}
		
		List<JobQueueSetting> infoResList = new JobControllerBean().deleteJobQueue(queueIdList);

		List<JobQueueResponse> resDtoList = new ArrayList<>();
		for (JobQueueSetting info : infoResList) {
			JobQueueResponse dto = new JobQueueResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			resDtoList.add(dto);
		}

		RestLanguageConverter.convertMessages(resDtoList);
		return Response.status(Status.OK).entity(resDtoList).build();
	}

	@GET
	@Path("/jobmap/iconImage_iconId")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobmapIconImageIdListForSelect")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobmapIconImageInfoResponseP1.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.IconImage, type = LogType.REFERENCE)
	public Response getJobmapIconImageIdListForSelect(@Context Request request, @Context UriInfo uriInfo, @QueryParam("ownerRoleId") String ownerRoleId) throws IconFileNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getJobmapIconImageIdListForSelect : ownerRoleId=" + ownerRoleId);
		
		List<String> iconIdList = new JobControllerBean().getJobmapIconImageIdListForSelect(ownerRoleId);
		JobmapIconImageInfoResponseP1 infoRes = new JobmapIconImageInfoResponseP1();
		infoRes.setIconIdList(iconIdList);
		
		RestLanguageConverter.convertMessages(infoRes);

		return Response.status(Status.OK).entity(infoRes).build();
	}

	@DELETE
	@Path("/setting/premakejobsession")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeletePremakeJobsession")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PremakeJobsessionResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ,
			SystemPrivilegeMode.MODIFY })
	@RestLog(action = LogAction.Delete, target = LogTarget.Session, type = LogType.UPDATE)
	public Response deletePremakeJobsession(@Context Request request, @Context UriInfo uriInfo,
			@ArrayTypeParam @QueryParam(value = "jobkickId") String jobkickId)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, JobInfoNotFound {
		m_log.info("call deletePremakeJobsession()");
		m_log.info("jobkickId=" + jobkickId);

		new JobControllerBean().deletePremakeJobsession(jobkickId);

		PremakeJobsessionResponse resDto = new PremakeJobsessionResponse();
		resDto.setJobkickId(jobkickId);

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	/**
	 * オーナーロールIDを条件としてジョブ連携送信設定情報一覧を取得します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param ownerRoleId
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@GET
	@Path("/joblinksend_setting")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobLinkSendSettingList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobLinkSendSettingResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Joblinksend, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getJobLinkSendSettingList(@QueryParam("ownerRoleId") String ownerRoleId, @Context Request request,
			@Context UriInfo uriInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call getJobLinkSendSettingList()");

		List<JobLinkSendSettingEntity> infoResList = new JobControllerBean().getJobLinkSendSettingList(ownerRoleId);
		List<JobLinkSendSettingResponse> dtoResList = new ArrayList<>();
		for (JobLinkSendSettingEntity info : infoResList) {
			JobLinkSendSettingResponse dto = new JobLinkSendSettingResponse();
			RestBeanUtil.convertBean(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 引数で指定したジョブ連携送信設定IDに対応するジョブ連携送信設定情報を取得します。<BR>
	 *
	 * JobManagementRead権限が必要
	 *
	 * @param joblintSendSettingId
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@GET
	@Path("/joblinksend_setting/{joblinkSendSettingId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobLinkSendSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobLinkSendSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Joblinksend, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	public Response getJobLinkSendSetting(@PathParam(value = "joblinkSendSettingId") String joblinkSendSettingId, @Context Request request,
			@Context UriInfo uriInfo)
			throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call getJobLinkSendSetting()");

		JobLinkSendSettingEntity infoRes = new JobControllerBean().getJobLinkSendSetting(joblinkSendSettingId);
		JobLinkSendSettingResponse dtoRes = new JobLinkSendSettingResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブ連携送信設定情報を登録します。<BR>
	 *
	 * JobManagementAdd権限が必要
	 *
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws JobMasterDuplicate
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@POST
	@Path("/joblinksend_setting")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddJobLinkSendSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobLinkSendSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.Joblinksend, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD })
	public Response addJobLinkSendSetting(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "AddJobLinkSendSettingBody", content = @Content(schema = @Schema(implementation = AddJobLinkSendSettingRequest.class))) String requestBody)
			throws JobMasterDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addJobLinkSendSetting()");

		// JSONからDTOへ変換
		AddJobLinkSendSettingRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddJobLinkSendSettingRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);

		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		JobLinkSendSettingEntity infoReq = new JobLinkSendSettingEntity();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		if (dtoReq.getProxyPort() == null) {
			infoReq.setProxyPort(null);
		}

		// ControllerBean呼び出し
		JobLinkSendSettingEntity infoRes = new JobControllerBean().addJobLinkSendSetting(infoReq);
		JobLinkSendSettingResponse dtoRes = new JobLinkSendSettingResponse();

		// ControllerBeanからのINFOをDTOへ変換
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブ連携送信設定情報を変更します。<BR>
	 *
	 * JobManagementModify権限が必要
	 *
	 * @param joblinkSendSettingId
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws JobMstNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@PUT
	@Path("/joblinksend_setting/{joblinkSendSettingId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyJobLinkSendSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobLinkSendSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Joblinksend, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY })
	public Response modifyJobLinkSendSetting(@PathParam("joblinkSendSettingId") String joblinkSendSettingId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "ModifyJobLinkSendSettingBody", content = @Content(schema = @Schema(implementation = ModifyJobLinkSendSettingRequest.class))) String requestBody)
			throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call modifyJobLinkSendSetting()");

		ModifyJobLinkSendSettingRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyJobLinkSendSettingRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		JobLinkSendSettingEntity infoReq = new JobLinkSendSettingEntity();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setJoblinkSendSettingId(joblinkSendSettingId);
		if (dtoReq.getProxyPort() == null) {
			infoReq.setProxyPort(null);
		}

		JobLinkSendSettingEntity infoRes = new JobControllerBean().modifyJobLinkSendSetting(infoReq);
		JobLinkSendSettingResponse dtoRes = new JobLinkSendSettingResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブ連携送信設定情報を 削除します。<BR>
	 *
	 * JobManagementModify権限が必要
	 *
	 * @param joblinkSendSettingIds
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@DELETE
	@Path("/joblinksend_setting")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteJobLinkSendSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobLinkSendSettingResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.Joblinksend, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY })
	public Response deleteJobLinkSendSetting(@ArrayTypeParam @QueryParam(value = "joblinkSendSettingIds") String joblinkSendIds,
			@Context Request request, @Context UriInfo uriInfo)
			throws JobMasterNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call deleteJobLinkSendSetting()");


		List<String> joblinkSendSettingIdList = new ArrayList<>();
		if(joblinkSendIds != null && !joblinkSendIds.isEmpty()) {
			joblinkSendSettingIdList = Arrays.asList(joblinkSendIds.split(","));
		}
		
		List<JobLinkSendSettingEntity> infoResList = new JobControllerBean().deleteJobLinkSendSetting(joblinkSendSettingIdList);
		List<JobLinkSendSettingResponse> dtoResList = new ArrayList<>();
		for (JobLinkSendSettingEntity infoRes : infoResList) {
			JobLinkSendSettingResponse dtoRes = new JobLinkSendSettingResponse();
			RestBeanUtil.convertBean(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * ジョブ連携メッセージを外部マネージャから登録します。<BR>
	 *
	 * JobManagementAdd権限が必要
	 *
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@POST
	@Path("/joblink_message")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "RegistJobLinkMessage")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RegistJobLinkMessageResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.JoblinkMessage, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD })
	@IgnoreCommandline
	@IgnoreReference
	public Response registJobLinkMessage(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "RegistJobLinkMessageBody", content = @Content(schema = @Schema(implementation = RegistJobLinkMessageRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call registJobLinkMessage()");

		// JSONからDTOへ変換
		RegistJobLinkMessageRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, RegistJobLinkMessageRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);

		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// 処理開始日時
		long now = HinemosTime.currentTimeMillis();

		// ノード情報の特定
		List<InetAddress> inetAddressList = new ArrayList<>();
		for (String sourceIpAddress : dtoReq.getSourceIpAddressList()) {
			try {
				inetAddressList.add(InetAddress.getByName(sourceIpAddress));
			} catch (UnknownHostException e) {
				m_log.warn("registJobLinkMessage() : "
						+ "joblinkMessageId=" + dtoReq.getJoblinkMessageId() + ", "
						+ "facilityId=" + dtoReq.getFacilityId() + ", "
						+ "sourceIpAddress=" + sourceIpAddress
						, e);
				continue;
			}
		}

		List<NodeInfo> nodeList = new ArrayList<>();
		for (InetAddress inetAddress : inetAddressList) {
			List<String> facilityIdList = new RepositoryControllerBean().getFacilityIdByIpAddress(inetAddress);
			for (String facilityId : facilityIdList) {
				try {
					NodeInfo nodeInfo = new RepositoryControllerBean().getNode(facilityId);
					nodeList.add(nodeInfo);
				} catch (FacilityNotFound e) {
					// 以降の処理へ
				}
			}
		}

		if (nodeList.isEmpty()) {
			// 送信元ノード未存在
			throw new HinemosUnknown(MessageConstant.MESSAGE_JOB_LINK_SEND_NODE_NOTFOUND.getMessage());
		}

		// 拡張情報取得
		List<JobLinkExpInfo> infoReqExpList = new ArrayList<>();
		if (dtoReq.getJobLinkExpInfoList() != null) {
			for (JobLinkExpInfoRequest dtoExpReq : dtoReq.getJobLinkExpInfoList()) {
				JobLinkExpInfo infoExpReq = new JobLinkExpInfo();
				RestBeanUtil.convertBean(dtoExpReq, infoExpReq);
				infoReqExpList.add(infoExpReq);
			}
		}

		// 日時情報変換
		Long sendDateLong = RestCommonConverter.convertDTStringToHinemosTime(dtoReq.getSendDate(), MessageConstant.SEND_TIME.getMessage());
		
		// ControllerBean呼び出し
		JobLinkSendMessageResultInfo infoRes = new JobControllerBean().registJobLinkMessage(
				nodeList,
				dtoReq.getJoblinkMessageId(),
				sendDateLong,
				now,
				dtoReq.getMonitorDetailId(),
				dtoReq.getApplication(),
				dtoReq.getPriority().getCode(),
				dtoReq.getMessage(),
				dtoReq.getMessageOrg(),
				infoReqExpList);
		RegistJobLinkMessageResponse dtoRes = new RegistJobLinkMessageResponse();

		// ControllerBeanからのINFOをDTOへ変換
		RestBeanUtil.convertBean(infoRes, dtoRes);
		// 受信日時のみ手動で設定
		if (infoRes.getAcceptDate() != null) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			df.setTimeZone(HinemosTime.getTimeZone());
			dtoRes.setAcceptDate(df.format(new Date(infoRes.getAcceptDate())));
		}
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブ連携メッセージを手動登録します。<BR>
	 *
	 * JobManagementAdd権限が必要
	 *
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws JobMasterNotFound
	 * @throws FacilityNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@POST
	@Path("/joblink_message_manual")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SendJobLinkMessageManual")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SendJobLinkMessageManualResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.JoblinkMessage, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ, SystemPrivilegeMode.EXEC })
	public Response sendJobLinkMessageManual(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "SendJobLinkMessageManualBody", content = @Content(schema = @Schema(implementation = SendJobLinkMessageManualRequest.class))) String requestBody)
			throws JobMasterNotFound, FacilityNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call sendJobLinkMessageManual()");

		// JSONからDTOへ変換
		SendJobLinkMessageManualRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, SendJobLinkMessageManualRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);

		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		JobLinkManualSendInfo infoReq = new JobLinkManualSendInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		new JobRunManagementBean().sendJobLinkMessageManual(infoReq);

		SendJobLinkMessageManualResponse dtoRes = new SendJobLinkMessageManualResponse();
		dtoRes.setJoblinkSendSettingId(dtoReq.getJoblinkSendSettingId());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 
	 * 受信ジョブ連携メッセージ一覧を取得します
	 *
	 * JobManagementRead権限が必要
	 * 
	 * @param request
	 * @param uriInfo
	 * @param size
	 * @param requestBody
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 */
	@POST
	@Path("/joblink_message_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobLinkMessageList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetJobLinkMessageListResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@RestLog(action = LogAction.Get, target = LogTarget.JoblinkMessage, type = LogType.REFERENCE )
	public Response getJobLinkMessageList(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "getJobLinkMessageListBody", content = @Content(schema = @Schema(implementation = GetJobLinkMessageListRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		m_log.info("call getJobLinkMessageList()");

		int histories = 0;
		
		JobLinkMessageFilter filterInfo = null;
		if(requestBody != null && requestBody.length() > 0){
			GetJobLinkMessageListRequest dtoReq = null;
			dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, GetJobLinkMessageListRequest.class);
			RestCommonValitater.checkRequestDto(dtoReq);
			dtoReq.correlationCheck();
			if (dtoReq.getSize() != null) {
				histories=dtoReq.getSize();
			}
			if(dtoReq.getFilterInfo() != null){
				filterInfo = new JobLinkMessageFilter();
				RestBeanUtil.convertBean(dtoReq.getFilterInfo(), filterInfo);
				
				// 個別セット
				if (dtoReq.getFilterInfo().getPriorityList() != null) {
					List<Integer> priorityEnumCodeList = new ArrayList<>();
					for(PriorityEnum priorityEnum : dtoReq.getFilterInfo().getPriorityList()){
						priorityEnumCodeList.add(priorityEnum.getCode());
					}
					Integer[] priorityList = priorityEnumCodeList.toArray(new Integer[priorityEnumCodeList.size()]);
					filterInfo.setPriorityList(priorityList);
				}
			}
		}
		JobLinkMessageList retList = new JobControllerBean().getJobLinkMessageList(filterInfo,histories);

		GetJobLinkMessageListResponse resDto = new GetJobLinkMessageListResponse();
		RestBeanUtil.convertBeanNoInvalid(retList, resDto);

		return Response.status(Status.OK).entity(resDto).build();
	}

	
	@GET
	@Path("/setting/rpa_login_resolution")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobRpaLoginResolution")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobRpaLoginResolutionResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.RpaLoginResolution, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@IgnoreCommandline
	@IgnoreReference
	public Response getRpaLoginResolutionList(@Context Request request, @Context UriInfo uriInfo) throws HinemosUnknown {
		m_log.info("call getRpaLoginResolutionList()");
		List<JobRpaLoginResolutionMstEntity> infoResList = new JobControllerBean().getJobRpaLoginResolutionList();
		List<JobRpaLoginResolutionResponse> dtoResList = new ArrayList<JobRpaLoginResolutionResponse>();
		for (JobRpaLoginResolutionMstEntity infoRes : infoResList) {
			JobRpaLoginResolutionResponse dtoRes = new JobRpaLoginResolutionResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * RPAシナリオジョブのDTOを取得します。<BR>
	 * 画像データはDTOに含まれません。
	 * 
	 * @param request
	 * @param uriInfo
	 * @return スクリーンショットのDTO
	 * @throws IconFileNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	@GET
	@Path("/sessionNode_operation/screenshot/{sessionId}/jobunit/{jobunitId}/job/{jobId}/facility/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaScreenshot")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobRpaScreenshotResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.RpaScreenshot, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@IgnoreCommandline
	@IgnoreReference
	public Response getRpaScreenshot(@Context Request request, @Context UriInfo uriInfo,
			@PathParam("sessionId") String sessionId, @PathParam("jobunitId") String jobunitId,
			@PathParam("jobId") String jobId, @PathParam("facilityId") String facilityId)
				throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getRpaScreenshot()");

		List<RpaJobScreenshot> retOrgList = new JobControllerBean().getJobRpaScreenshotList(sessionId, jobunitId, jobId, facilityId);
		List<JobRpaScreenshotResponse> retDtoList = new ArrayList<JobRpaScreenshotResponse>();
		for (RpaJobScreenshot retOrg : retOrgList) {
			JobRpaScreenshotResponse retDto = new JobRpaScreenshotResponse();
			RestBeanUtil.convertBeanNoInvalid(retOrg, retDto);
			retDtoList.add(retDto);
		}
		RestLanguageConverter.convertMessages(retDtoList);
		return Response.status(Status.OK).entity(retDtoList).build();
	}

	/**
	 * RPAシナリオジョブのスクリーンショットをダウンロードします。
	 * 
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws IconFileNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting 
	 */
	@GET
	@Path("/sessionNode_operation/screenshot_file/{sessionId}/jobunit/{jobunitId}/job/{jobId}/facility/{facilityId}/regdate/{regDate}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DownloadRpaScreenshotFile")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	@RestLog(action = LogAction.Get, target = LogTarget.RpaScreenshot, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@IgnoreCommandline
	@IgnoreReference
	public Response downloadRpaScreenshotFile( @Context Request request, @Context UriInfo uriInfo,
			@PathParam("sessionId") String sessionId, @PathParam("jobunitId") String jobunitId,
			@PathParam("jobId") String jobId, @PathParam("facilityId") String facilityId, @PathParam("regDate") String regDate)
					throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call downloadRpaScreenshotFile()");

		RpaJobScreenshot infoRes = new JobControllerBean().getJobRpaScreenshot(sessionId, jobunitId, jobId, facilityId,
				RestCommonConverter.convertDTStringToHinemosTime(regDate, MessageConstant.TARGET_DATETIME.getMessage()));
		File file = RestByteArrayConverter.convertByteArrayToFile(infoRes.getFiledata(), RestTempFileType.JOB_RPA_SCREENSHOT);
		StreamingOutput stream = RestTempFileUtil.getTempFileStream(file);
		return Response.ok(stream).header("Content-Disposition", "attachment; name=" + file.getName()).build();
	}
}
