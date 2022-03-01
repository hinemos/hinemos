/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.reporting;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_404;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_409;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import com.clustercontrol.bean.ActivationKeyConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ReportFileCreateFailed;
import com.clustercontrol.fault.ReportFileNotFound;
import com.clustercontrol.fault.ReportingDuplicate;
import com.clustercontrol.fault.ReportingNotFound;
import com.clustercontrol.platform.util.reporting.ExecReportingProcess;
import com.clustercontrol.reporting.bean.ReportingInfo;
import com.clustercontrol.reporting.bean.TemplateSetDetailInfo;
import com.clustercontrol.reporting.bean.TemplateSetInfo;
import com.clustercontrol.reporting.session.ReportingControllerBean;
import com.clustercontrol.reporting.util.ReportFileFailureListManager;
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
import com.clustercontrol.rest.endpoint.jobmap.dto.CheckPublishResponse;
import com.clustercontrol.rest.endpoint.reporting.dto.AddReportingScheduleRequest;
import com.clustercontrol.rest.endpoint.reporting.dto.AddTemplateSetRequest;
import com.clustercontrol.rest.endpoint.reporting.dto.CreateReportingFileRequest;
import com.clustercontrol.rest.endpoint.reporting.dto.CreateReportingFileResponse;
import com.clustercontrol.rest.endpoint.reporting.dto.ModifyReportingScheduleRequest;
import com.clustercontrol.rest.endpoint.reporting.dto.ModifyTemplateSetRequest;
import com.clustercontrol.rest.endpoint.reporting.dto.ReportingScheduleResponse;
import com.clustercontrol.rest.endpoint.reporting.dto.SetReportingStatusRequest;
import com.clustercontrol.rest.endpoint.reporting.dto.TemplateIdListResponse;
import com.clustercontrol.rest.endpoint.reporting.dto.TemplateSetDetailInfoResponse;
import com.clustercontrol.rest.endpoint.reporting.dto.TemplateSetInfoResponse;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCodecUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestDownloadFile;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.rest.util.RestTempFileUtil;
import com.clustercontrol.util.KeyCheck;

/**
 * レポーティング用のWebAPIエンドポイント
 */
@Path("/reporting")
@RestLogFunc(name = LogFuncName.Reporting)
public class ReportingRestEndpoints {
	private static Log m_log = LogFactory.getLog(ReportingRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "reporting";
	
	/**
	 * レポート作成スケジュールを追加します。
	 * 
	 * ReportingAdd権限が必要
	 */
	@POST
	@Path("/schedule")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddReportingSchedule")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReportingScheduleResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.Schedule, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Reporting, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.READ})
	public Response addReportingSchedule(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addReportingScheduleBody", content = @Content(schema = @Schema(implementation = AddReportingScheduleRequest.class))) String requestBody) throws HinemosUnknown, ReportingDuplicate, InvalidUserPass, InvalidRole,InvalidSetting
	{
		m_log.debug("addReportingSchedule");
		
		AddReportingScheduleRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddReportingScheduleRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();
		
		ReportingInfo infoReq = new ReportingInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		
		ReportingInfo infoRes = new ReportingControllerBean().addReporting(infoReq);
		
		ReportingScheduleResponse dtoRes = new ReportingScheduleResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		
		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * レポート作成スケジュールを変更します。
	 * 
	 * ReportingModify権限が必要
	 */
	@PUT
	@Path("/schedule/{scheduleId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyReportingSchedule")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReportingScheduleResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Schedule, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Reporting, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ })
	public Response modifyReportingSchedule(@Context Request request, @Context UriInfo uriInfo, @PathParam("scheduleId") String scheduleId, 
	@RequestBody(description = "modifyReportingScheduleBody", content = @Content(schema = @Schema(implementation = ModifyReportingScheduleRequest.class))) String requestBody) throws HinemosUnknown, NotifyNotFound, ReportingNotFound, InvalidUserPass, InvalidRole,InvalidSetting
	{
		m_log.debug("modifyReportingSchedule");
		
		ModifyReportingScheduleRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyReportingScheduleRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();
		
		ReportingInfo infoReq = new ReportingInfo();
		infoReq.setReportScheduleId(scheduleId);
		RestBeanUtil.convertBean(dtoReq, infoReq);
		
		ReportingInfo infoRes = new ReportingControllerBean().modifyReporting(infoReq);
		
		ReportingScheduleResponse dtoRes = new ReportingScheduleResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		
		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * レポート作成スケジュールを削除します。
	 * 
	 * ReportingModify権限が必要
	 */
	@DELETE
	@Path("/schedule")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteReportingSchedule")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReportingScheduleResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.Schedule, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Reporting, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ })
	public Response deleteReportingSchedule(@ArrayTypeParam @QueryParam(value = "scheduleIds") String scheduleIdList,
			@Context Request request, @Context UriInfo uriInfo) throws HinemosUnknown, ReportingNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("deleteReportingSchedule");
		
		List<String> idList = new ArrayList<>();
		if(scheduleIdList != null && !scheduleIdList.isEmpty()) {
			idList = Arrays.asList(scheduleIdList.split(","));
		}
		
		List<ReportingInfo> infoResList = new ReportingControllerBean().deleteReporting(idList);
		
		List<ReportingScheduleResponse> dtoResList = new ArrayList<>();
		for (ReportingInfo info : infoResList) {
			ReportingScheduleResponse dto = new ReportingScheduleResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);
		
		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * レポート作成スケジュールを取得します。
	 *
	 * ReportingRead権限が必要
	 */
	@GET
	@Path("/schedule/{scheduleId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetReportingSchedule")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReportingScheduleResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Schedule, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Reporting, modeList = { SystemPrivilegeMode.READ })
	public Response getReportingSchedule(@Context Request request, @Context UriInfo uriInfo,
			@PathParam(value = "scheduleId") String scheduleId) throws ReportingNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getReportingSchedule");
		
		ReportingInfo infoRes = new ReportingControllerBean().getReportingInfo(scheduleId);
		ReportingScheduleResponse dtoRes = new ReportingScheduleResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * レポート作成スケジュールの一覧を取得します。
	 * 
	 * ReportingRead権限が必要
	 * 
	 * @return レポーティング情報の一覧を保持するList
	 */
	@GET
	@Path("/schedule")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetReportingScheduleList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReportingScheduleResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Schedule, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Reporting, modeList = { SystemPrivilegeMode.READ })
	public Response getReportingScheduleList(@Context Request request, @Context UriInfo uriInfo) throws HinemosUnknown, ReportingNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("getReportingScheduleList");
		
		List<ReportingInfo> infoResList = new ReportingControllerBean().getReportingList();
		
		List<ReportingScheduleResponse> dtoResList = new ArrayList<>();
		for (ReportingInfo info : infoResList) {
			ReportingScheduleResponse dto = new ReportingScheduleResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * レポート作成スケジュールの有効、無効を変更するメソッドです。
	 * 
	 * ReportingWrite権限が必要
	 */
	@POST
	@Path("/schedule_valid")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SetReportingScheduleStatus")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ReportingScheduleResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Schedule, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Reporting, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ})
	public Response setReportingScheduleStatus(@Context Request request, @Context UriInfo uriInfo,
	@RequestBody(description = "setReportingScheduleStatusBody", content = @Content(schema = @Schema(implementation = SetReportingStatusRequest.class))) String requestBody) throws NotifyNotFound, ReportingNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.debug("setReportingScheduleStatus");
		
		SetReportingStatusRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, SetReportingStatusRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();
		
		List<ReportingInfo> infoResList = new ReportingControllerBean().setReportingStatus(dtoReq.getReportIdList(), dtoReq.getValidFlg());
		
		List<ReportingScheduleResponse> dtoResList = new ArrayList<>();
		for (ReportingInfo info : infoResList) {
			ReportingScheduleResponse dto = new ReportingScheduleResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);
		
		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * 指定したスケジュールIDに対するレポート情報を作成します。
	 * 追加パラメータとして即時実行時に指定するパラメータを格納するReportingInfoを渡します。
	 * 本メソッドが終了した時点で、Hinemosマネージャ上のレポート作成スレッドを開始し、作成されるファイル名のリストを返却します。
	 * 
	 * ReportingRead権限が必要
	 */
	@POST
	@Path("/schedule_create/{scheduleId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "CreateReportingFileManual")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CreateReportingFileResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Create, target = LogTarget.Schedule, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Reporting, modeList = { SystemPrivilegeMode.EXEC, SystemPrivilegeMode.READ})
	public Response createReportingFileManual(@Context Request request, @Context UriInfo uriInfo, @PathParam("scheduleId") String scheduleId, 
			@RequestBody(description = "createReportingFileManualBody", content = @Content(schema = @Schema(implementation = CreateReportingFileRequest.class))) String requestBody) throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.debug("createReportingFile()");
		
		CreateReportingFileRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, CreateReportingFileRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();
		
		List<String> infoResList = new ReportingControllerBean().runReporting(scheduleId, dtoReq);
		
		CreateReportingFileResponse dtoRes = new CreateReportingFileResponse();
		dtoRes.setFileNameList(infoResList);

		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	
	/**
	 * テンプレートセット情報を追加します。
	 * 
	 * ReportingAdd権限が必要
	 */
	@POST
	@Path("/templateSet")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddTemplateSet")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TemplateSetInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.TemplateSet, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Reporting, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.READ })
	public Response addTemplateSet(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addTemplateSetBody", content = @Content(schema = @Schema(implementation = AddTemplateSetRequest.class))) String requestBody) throws HinemosUnknown, ReportingDuplicate, InvalidUserPass, InvalidRole,InvalidSetting
	{
		m_log.debug("addTemplateSet");
		
		AddTemplateSetRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddTemplateSetRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();
		
		TemplateSetInfo infoReq = new TemplateSetInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		
		TemplateSetInfo infoRes = new ReportingControllerBean().addTemplateSet(infoReq);
		
		TemplateSetInfoResponse dtoRes = new TemplateSetInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		
		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * テンプレートセット情報を変更します。
	 * 
	 * ReportingModify権限が必要
	 */
	@PUT
	@Path("/templateSet/{templateSetId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyTemplateSet")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TemplateSetInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.TemplateSet, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Reporting, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ})
	public Response modifyTemplateSet(@Context Request request, @Context UriInfo uriInfo, @PathParam("templateSetId") String templateSetId, 
			@RequestBody(description = "modifyTemplateSetBody", content = @Content(schema = @Schema(implementation = ModifyTemplateSetRequest.class))) String requestBody) throws HinemosUnknown, NotifyNotFound, ReportingNotFound, InvalidUserPass, InvalidRole,InvalidSetting
	{
		m_log.debug("modifyTemplateSet");
		
		ModifyTemplateSetRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyTemplateSetRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();
		
		TemplateSetInfo infoReq = new TemplateSetInfo();
		infoReq.setTemplateSetId(templateSetId);
		RestBeanUtil.convertBean(dtoReq, infoReq);
		
		TemplateSetInfo infoRes = new ReportingControllerBean().modifyTemplateSet(infoReq);
		
		TemplateSetInfoResponse dtoRes = new TemplateSetInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		
		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * テンプレートセット情報を削除します。
	 * 
	 * ReportingModify権限が必要
	 */
	@DELETE
	@Path("/templateSet")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteTemplateSet")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TemplateSetInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.TemplateSet, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Reporting, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ })
	public Response deleteTemplateSet(@ArrayTypeParam @QueryParam(value = "templateSetIds") String templateSetIdList,
			@Context Request request, @Context UriInfo uriInfo) throws HinemosUnknown, ReportingNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("deleteTemplateSet");
		
		List<String> idList = new ArrayList<>();
		if(templateSetIdList != null && !templateSetIdList.isEmpty()) {
			idList = Arrays.asList(templateSetIdList.split(","));
		}
		
		List<TemplateSetInfo> infoResList = new ReportingControllerBean().deleteTemplateSet(idList);
		
		List<TemplateSetInfoResponse> dtoResList = new ArrayList<>();
		for (TemplateSetInfo info : infoResList) {
			TemplateSetInfoResponse dto = new TemplateSetInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);
		
		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * オーナーロールIDを条件としてテンプレートセット情報の一覧を取得します。
	 * 
	 * ReportingRead権限が必要
	 */
	@GET
	@Path("/templateSet")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetTemplateSetList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TemplateSetInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.TemplateSet, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Reporting, modeList = { SystemPrivilegeMode.READ })
	public Response getTemplateSetList(@QueryParam(value = "ownerRoleId") String ownerRoleId,
			@Context Request request, @Context UriInfo uriInfo) throws HinemosUnknown, ReportingNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("getTemplateSetList");
		
		List<TemplateSetInfo> infoResList = new ReportingControllerBean().getTemplateSetListByOwnerRole(ownerRoleId);
		
		List<TemplateSetInfoResponse> dtoResList = new ArrayList<>();
		for (TemplateSetInfo info : infoResList) {
			TemplateSetInfoResponse dto = new TemplateSetInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * テンプレートセットIDを基にテンプレートセット情報を取得します。
	 * 
	 * ReportingRead権限が必要
	 */
	@GET
	@Path("/templateSet/{templateSetId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetTemplateSetInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TemplateSetInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.TemplateSet, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Reporting, modeList = { SystemPrivilegeMode.READ })
	public Response getTemplateSetInfo(@Context Request request, @Context UriInfo uriInfo,
			@PathParam(value = "templateSetId") String templateSetId) throws HinemosUnknown, ReportingNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("getTemplateSetInfo");
		
		TemplateSetInfo infoRes = new ReportingControllerBean().getTemplateSetInfo(templateSetId);
		TemplateSetInfoResponse dtoRes = new TemplateSetInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * テンプレートセットIDを基にテンプレートセット詳細情報のリストを取得します。
	 * 
	 * ReportingRead権限が必要
	 */
	@GET
	@Path("/templateSet/{templateSetId}/templateSetDetail")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetTemplateSetDetailInfoList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TemplateSetDetailInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.TemplateSetDetail, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Reporting, modeList = { SystemPrivilegeMode.READ })
	public Response getTemplateSetDetailInfoList(@Context Request request, @Context UriInfo uriInfo,
			@PathParam(value = "templateSetId") String templateSetId) throws InvalidRole, HinemosUnknown, InvalidUserPass {
		m_log.debug("getTemplateSetDetailInfoList");
		
		List<TemplateSetDetailInfo> infoResList = new ReportingControllerBean().getTemplateSetDetailInfoList(templateSetId);
		
		List<TemplateSetDetailInfoResponse> dtoResList = new ArrayList<>();
		for (TemplateSetDetailInfo info : infoResList) {
			TemplateSetDetailInfoResponse dto = new TemplateSetDetailInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * テンプレートIDのリストを取得します。
	 */
	@GET
	@Path("/template_id")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetTemplateIdList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TemplateIdListResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Template, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Reporting, modeList = { SystemPrivilegeMode.READ })
	public Response getTemplateIdList(@QueryParam(value = "ownerRoleId") String ownerRoleId,
			@Context Request request, @Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getTemplateIdList");
		
		List<String> dtoResList = new ReportingControllerBean().getTemplateIdList(ownerRoleId);
		
		TemplateIdListResponse dtoRes = new TemplateIdListResponse();
		dtoRes.setTemplateIdList(dtoResList);
		
		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * キーファイルの情報を取得します。 
	 */
	@GET
	@Path("/checkPublish")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "CheckPublish")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CheckPublishResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.CheckPublish, type = LogType.REFERENCE )
	@IgnoreCommandline
	@IgnoreReference
	public Response checkPublish(@Context Request request, @Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call checkPublish()");
		
		boolean publish = KeyCheck.checkKey(ActivationKeyConstant.TYPE_ENTERPRISE);
		
		CheckPublishResponse dtoRes = new CheckPublishResponse();
		dtoRes.setPublish(publish);
		
		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * レポートファイルをDLします。
	 * 
	 * ReportingRead権限が必要
	 */
	@GET
	@Path("/schedule_download/{fileName}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DownloadReportingFile")
	@RestLog(action = LogAction.Download, target = LogTarget.Schedule, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Reporting, modeList = { SystemPrivilegeMode.EXEC })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	public Response downloadReportingFile(@PathParam(value = "fileName") String fileName, @Context Request request,
			@Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, ReportFileNotFound, ReportFileCreateFailed, HinemosUnknown
	{
		String fileNameDecoded = RestCodecUtil.stringDecode(fileName);

		File file = new File(ExecReportingProcess.getBasePath() + File.separator + fileNameDecoded);
		
		if(!file.exists()) {
			String msg = "file not found : " + file.getAbsolutePath();
			m_log.info(msg);
			if(ReportFileFailureListManager.isFailed(fileNameDecoded)) {
				throw new ReportFileCreateFailed(msg);
			} else {
				throw new ReportFileNotFound(msg);
			}
		}
		m_log.info("file found : " + file.getAbsolutePath());
		
		RestDownloadFile restDownloadFile = new RestDownloadFile(file, fileName);
		// レポートは一定期間保存するためisDeleteをfalseにする
		StreamingOutput stream = RestTempFileUtil.getTempFileStream(restDownloadFile.getTempFile(), false);

		return Response.ok(stream).header("Content-Disposition", "filename=\"" + restDownloadFile.getFileName() + "\"").build();
	}
}
