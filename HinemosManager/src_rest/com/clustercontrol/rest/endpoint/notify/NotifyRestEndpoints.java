/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.notify;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_404;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_409;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.util.ArrayList;
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
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.notify.bean.EventNotifyInfo;
import com.clustercontrol.notify.bean.NotifyCheckIdResultInfo;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.notify.util.NotifyUtil;
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
import com.clustercontrol.rest.endpoint.notify.dto.AddCloudNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.AddCommandNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.AddEventNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.AddInfraNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.AddJobNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.AddLogEscalateNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.AddMailNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.AddMessageNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.AddRestNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.AddStatusNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.CloudNotifyInfoResponse;
import com.clustercontrol.rest.endpoint.notify.dto.CommandNotifyInfoResponse;
import com.clustercontrol.rest.endpoint.notify.dto.EventDataInfoResponse;
import com.clustercontrol.rest.endpoint.notify.dto.EventNotifyInfoResponse;
import com.clustercontrol.rest.endpoint.notify.dto.InfraNotifyInfoResponse;
import com.clustercontrol.rest.endpoint.notify.dto.JobNotifyInfoResponse;
import com.clustercontrol.rest.endpoint.notify.dto.LogEscalateNotifyInfoResponse;
import com.clustercontrol.rest.endpoint.notify.dto.MailNotifyInfoResponse;
import com.clustercontrol.rest.endpoint.notify.dto.MessageNotifyInfoResponse;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyCloudNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyCommandNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyEventNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyInfraNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyJobNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyLogEscalateNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyMailNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyMessageNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyRestNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.ModifyStatusNotifyRequest;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyAsMonitorRequest;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyCheckIdResultInfoResponse;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyEventRequest;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyInfoResponse;
import com.clustercontrol.rest.endpoint.notify.dto.RestNotifyInfoResponse;
import com.clustercontrol.rest.endpoint.notify.dto.SetNotifyValidRequest;
import com.clustercontrol.rest.endpoint.notify.dto.StatusNotifyInfoResponse;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonConverter;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;

@Path("/notify")
@RestLogFunc(name = LogFuncName.Notify)
public class NotifyRestEndpoints {

	private static Log m_log = LogFactory.getLog(NotifyRestEndpoints.class);
	
	private static final String ENDPOINT_OPERATION_ID_PREFIX = "notify";

	/**
	 * ステータス通知の追加を行うAPI
	 */
	@POST
	@Path("/status")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddStatusNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StatusNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.Status, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.READ })
	public Response addStatusNotify(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addStatusNotifyBody", content = @Content(schema = @Schema(implementation = AddStatusNotifyRequest.class))) String requestBody)
			throws HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addStatusNotify()");

		// JSONからDTOへ変換
		AddStatusNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddStatusNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(dtoReq.getNotifyId());
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().addNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		StatusNotifyInfoResponse dtoRes = new StatusNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * イベント通知の追加を行うAPI
	 */
	@POST
	@Path("/event")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddEventNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.Event, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.READ })
	public Response addEventNotify(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addEventNotifyBody", content = @Content(schema = @Schema(implementation = AddEventNotifyRequest.class))) String requestBody)
			throws HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addEventNotify()");

		// JSONからDTOへ変換
		AddEventNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddEventNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(dtoReq.getNotifyId());
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().addNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		EventNotifyInfoResponse dtoRes = new EventNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * メール通知の追加を行うAPI
	 */
	@POST
	@Path("/mail")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddMailNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MailNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.Mail, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.READ })
	public Response addMailNotify(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addMailNotifyBody", content = @Content(schema = @Schema(implementation = AddMailNotifyRequest.class))) String requestBody)
			throws HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addMailNotify()");

		// JSONからDTOへ変換
		AddMailNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddMailNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(dtoReq.getNotifyId());
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().addNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		MailNotifyInfoResponse dtoRes = new MailNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		// 個別変換
		dtoRes.getNotifyMailInfo().setMailTemplateId(infoRes.getNotifyMailInfo().getMailTemplateId());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブ通知の追加を行うAPI
	 */
	@POST
	@Path("/job")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddJobNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.Job, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.READ })
	public Response addJobNotify(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addJobNotifyBody", content = @Content(schema = @Schema(implementation = AddJobNotifyRequest.class))) String requestBody)
			throws HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addJobNotify()");

		// JSONからDTOへ変換
		AddJobNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddJobNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(dtoReq.getNotifyId());
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().addNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		JobNotifyInfoResponse dtoRes = new JobNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		// 個別変換
		dtoRes.getNotifyJobInfo().setJobExecScope(infoRes.getNotifyJobInfo().getJobExecScope());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ログエスカレーション通知の追加を行うAPI
	 */
	@POST
	@Path("/logEscalate")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddLogEscalateNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LogEscalateNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.LogEscalate, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.READ })
	public Response addLogEscalateNotify(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addLogEscalateNotifyBody", content = @Content(schema = @Schema(implementation = AddLogEscalateNotifyRequest.class))) String requestBody)
			throws HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addLogEscalateNotify()");

		// JSONからDTOへ変換
		AddLogEscalateNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddLogEscalateNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(dtoReq.getNotifyId());
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().addNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		LogEscalateNotifyInfoResponse dtoRes = new LogEscalateNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		// 個別変換
		dtoRes.getNotifyLogEscalateInfo().setEscalateScope(infoRes.getNotifyLogEscalateInfo().getEscalateScope());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * コマンド通知の追加を行うAPI
	 */
	@POST
	@Path("/command")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCommandNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CommandNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.Command, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.READ })
	public Response addCommandNotify(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addCommandNotifyBody", content = @Content(schema = @Schema(implementation = AddCommandNotifyRequest.class))) String requestBody)
			throws HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addCommandNotify()");

		// JSONからDTOへ変換
		AddCommandNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddCommandNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(dtoReq.getNotifyId());
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().addNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		CommandNotifyInfoResponse dtoRes = new CommandNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 環境構築通知の追加を行うAPI
	 */
	@POST
	@Path("/infra")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddInfraNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InfraNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.Infra, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.READ })
	public Response addInfraNotify(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addInfraNotifyBody", content = @Content(schema = @Schema(implementation = AddInfraNotifyRequest.class))) String requestBody)
			throws HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addInfraNotify()");

		// JSONからDTOへ変換
		AddInfraNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddInfraNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(dtoReq.getNotifyId());
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().addNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		InfraNotifyInfoResponse dtoRes = new InfraNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		// 個別変換
		dtoRes.getNotifyInfraInfo().setInfraExecScope(infoRes.getNotifyInfraInfo().getInfraExecScope());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * REST通知の追加を行うAPI
	 */
	@POST
	@Path("/rest")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddRestNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RestNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.Rest, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.READ })
	public Response addRestNotify(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addRestNotifyBody", content = @Content(schema = @Schema(implementation = AddRestNotifyRequest.class))) String requestBody)
			throws HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addRestNotify()");

		// JSONからDTOへ変換
		AddRestNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddRestNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(dtoReq.getNotifyId());
		RestBeanUtil.convertBean(dtoReq, infoReq);
		
		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().addNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		RestNotifyInfoResponse dtoRes = new RestNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * クラウド通知の追加を行うAPI
	 */
	@POST
	@Path("/cloud")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddCloudNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.Cloud, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.READ })
	@IgnoreCommandline
	@IgnoreReference
	public Response addCloudNotify(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addCloudNotifyBody", content = @Content(schema = @Schema(implementation = AddCloudNotifyRequest.class))) String requestBody)
			throws HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addCloudNotify()");

		// JSONからDTOへ変換
		AddCloudNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddCloudNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(dtoReq.getNotifyId());
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// 個別変換
		// クラウド通知のディテール/データはDB上ではjsonで保存しているが
		// そのままではUtilityなどでの使い勝手が悪いので、コンポーネント間のやり取りでは
		// キーバリューオブジェクトのリストに変換している
		// ここではリスト→jsonの変換を実施
		String infoJsonData = NotifyUtil.getJsonStringForCloudNotify(dtoReq.getNotifyCloudInfo().getInfoKeyValueDataList());
		String warnJsonData = NotifyUtil.getJsonStringForCloudNotify(dtoReq.getNotifyCloudInfo().getWarnKeyValueDataList());
		String critJsonData = NotifyUtil.getJsonStringForCloudNotify(dtoReq.getNotifyCloudInfo().getCritKeyValueDataList());
		String unkJsonData = NotifyUtil.getJsonStringForCloudNotify(dtoReq.getNotifyCloudInfo().getUnkKeyValueDataList());
		// 情報
		infoReq.getNotifyCloudInfo().setInfoJsonData(infoJsonData);
		// 警告
		infoReq.getNotifyCloudInfo().setWarnJsonData(warnJsonData);
		// 危険
		infoReq.getNotifyCloudInfo().setCritJsonData(critJsonData);
		// 不明
		infoReq.getNotifyCloudInfo().setUnkJsonData(unkJsonData);
		
		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().addNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		CloudNotifyInfoResponse dtoRes = new CloudNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		// 個別変換
		dtoRes.getNotifyCloudInfo().setTextScope(infoRes.getNotifyCloudInfo().getScopeText());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * メッセージ通知の追加を行うAPI
	 */
	@POST
	@Path("/message")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddMessageNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MessageNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.Message, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.READ })
	public Response addMessageNotify(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addMessageNotifyBody", content = @Content(schema = @Schema(implementation = AddMessageNotifyRequest.class))) String requestBody)
			throws HinemosUnknown, NotifyDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addMessageNotify()");

		try{
			// JSONからDTOへ変換
			AddMessageNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddMessageNotifyRequest.class);
			// 共通バリデーション処理
			RestCommonValitater.checkRequestDto(dtoReq);
			// DTOの項目相関チェック処理
			dtoReq.correlationCheck();
	
			// DTOからINFOへ変換
			NotifyInfo infoReq = new NotifyInfo(dtoReq.getNotifyId());
			RestBeanUtil.convertBean(dtoReq, infoReq);
	
			// ControllerBean呼び出し
			NotifyInfo infoRes = new NotifyControllerBean().addNotify(infoReq);
	
			// ControllerBeanからのINFOをDTOへ変換
			MessageNotifyInfoResponse dtoRes = new MessageNotifyInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
	
			RestLanguageConverter.convertMessages(dtoRes);
	
			return Response.status(Status.OK).entity(dtoRes).build();
		} catch (Throwable th){
			m_log.error("", th);
			throw th;
		}
	}

	/**
	 * ステータス通知の変更を行うAPI
	 */
	@PUT
	@Path("/status/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyStatusNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StatusNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Status, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ })
	public Response modifyStatusNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "modifyStatusNotifyBody", content = @Content(schema = @Schema(implementation = ModifyStatusNotifyRequest.class))) String requestBody)
			throws NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, NotifyNotFound {
		m_log.info("call modifyStatusNotify()");

		// JSONからDTOへ変換
		ModifyStatusNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyStatusNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(notifyId);
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().modifyNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		StatusNotifyInfoResponse dtoRes = new StatusNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * イベント通知の変更を行うAPI
	 */
	@PUT
	@Path("/event/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyEventNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Event, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ })
	public Response modifyEventNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "modifyEventNotifyBody", content = @Content(schema = @Schema(implementation = ModifyEventNotifyRequest.class))) String requestBody)
			throws NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, NotifyNotFound {
		m_log.info("call modifyEventNotify()");

		// JSONからDTOへ変換
		ModifyEventNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyEventNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(notifyId);
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().modifyNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		EventNotifyInfoResponse dtoRes = new EventNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * メール通知の変更を行うAPI
	 */
	@PUT
	@Path("/mail/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyMailNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MailNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Mail, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ })
	public Response modifyMailNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "modifyMailNotifyBody", content = @Content(schema = @Schema(implementation = ModifyMailNotifyRequest.class))) String requestBody)
			throws NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, NotifyNotFound {
		m_log.info("call modifyMailNotify()");

		// JSONからDTOへ変換
		ModifyMailNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyMailNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(notifyId);
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().modifyNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		MailNotifyInfoResponse dtoRes = new MailNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		// 個別変換
		dtoRes.getNotifyMailInfo().setMailTemplateId(infoRes.getNotifyMailInfo().getMailTemplateId());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ジョブ通知の変更を行うAPI
	 */
	@PUT
	@Path("/job/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyJobNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Job, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ })
	public Response modifyJobNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "modifyJobNotifyBody", content = @Content(schema = @Schema(implementation = ModifyJobNotifyRequest.class))) String requestBody)
			throws NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, NotifyNotFound {
		m_log.info("call modifyJobNotify()");

		// JSONからDTOへ変換
		ModifyJobNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyJobNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(notifyId);
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().modifyNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		JobNotifyInfoResponse dtoRes = new JobNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		// 個別変換
		dtoRes.getNotifyJobInfo().setJobExecScope(infoRes.getNotifyJobInfo().getJobExecScope());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * ログエスカレーション通知の変更を行うAPI
	 */
	@PUT
	@Path("/logEscalate/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyLogEscalateNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LogEscalateNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.LogEscalate, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ })
	public Response modifyLogEscalateNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "modifyLogEscalateNotifyBody", content = @Content(schema = @Schema(implementation = ModifyLogEscalateNotifyRequest.class))) String requestBody)
			throws NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, NotifyNotFound {
		m_log.info("call modifyLogEscalateNotify()");

		// JSONからDTOへ変換
		ModifyLogEscalateNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyLogEscalateNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(notifyId);
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().modifyNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		LogEscalateNotifyInfoResponse dtoRes = new LogEscalateNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		// 個別変換
		dtoRes.getNotifyLogEscalateInfo().setEscalateScope(infoRes.getNotifyLogEscalateInfo().getEscalateScope());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * コマンド通知の変更を行うAPI
	 */
	@PUT
	@Path("/command/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCommandNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CommandNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Command, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ })
	public Response modifyCommandNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "modifyCommandNotifyBody", content = @Content(schema = @Schema(implementation = ModifyCommandNotifyRequest.class))) String requestBody)
			throws NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, NotifyNotFound {
		m_log.info("call modifyCommandNotify()");

		// JSONからDTOへ変換
		ModifyCommandNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyCommandNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(notifyId);
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().modifyNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		CommandNotifyInfoResponse dtoRes = new CommandNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 環境構築通知の変更を行うAPI
	 */
	@PUT
	@Path("/infra/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyInfraNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InfraNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Infra, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ })
	public Response modifyInfraNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "modifyInfraNotifyBody", content = @Content(schema = @Schema(implementation = ModifyInfraNotifyRequest.class))) String requestBody)
			throws NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, NotifyNotFound {
		m_log.info("call modifyInfraNotify()");

		// JSONからDTOへ変換
		ModifyInfraNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyInfraNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(notifyId);
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().modifyNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		InfraNotifyInfoResponse dtoRes = new InfraNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		// 個別変換
		dtoRes.getNotifyInfraInfo().setInfraExecScope(infoRes.getNotifyInfraInfo().getInfraExecScope());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * REST通知の変更を行うAPI
	 */
	@PUT
	@Path("/rest/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyRestNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RestNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Rest, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.MODIFY,
			SystemPrivilegeMode.READ })
	public Response modifyRestNotify(@PathParam(value = "notifyId") String notifyId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyRestNotifyBody", content = @Content(schema = @Schema(implementation = ModifyRestNotifyRequest.class))) String requestBody)
			throws NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, NotifyNotFound {
		m_log.info("call modifyRestNotify()");

		// JSONからDTOへ変換
		ModifyRestNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyRestNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(notifyId);
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().modifyNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		RestNotifyInfoResponse dtoRes = new RestNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * クラウド通知の変更を行うAPI
	 */
	@PUT
	@Path("/cloud/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyCloudNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Cloud, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ })
	@IgnoreCommandline
	@IgnoreReference
	public Response modifyCloudNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "modifyCloudNotifyBody", content = @Content(schema = @Schema(implementation = ModifyCloudNotifyRequest.class))) String requestBody)
			throws NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, NotifyNotFound {
		m_log.info("call modifyCloudNotify()");

		// JSONからDTOへ変換
		ModifyCloudNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyCloudNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(notifyId);
		RestBeanUtil.convertBean(dtoReq, infoReq);
		
		// 個別変換
		// クラウド通知のディテール/データはDB上ではjsonで保存しているが
		// そのままではUtilityなどでの使い勝手が悪いので、コンポーネント間のやり取りでは
		// キーバリューオブジェクトのリストに変換している
		// ここではリスト→jsonの変換を実施
		String infoJsonData = NotifyUtil.getJsonStringForCloudNotify(dtoReq.getNotifyCloudInfo().getInfoKeyValueDataList());
		String warnJsonData = NotifyUtil.getJsonStringForCloudNotify(dtoReq.getNotifyCloudInfo().getWarnKeyValueDataList());
		String critJsonData = NotifyUtil.getJsonStringForCloudNotify(dtoReq.getNotifyCloudInfo().getCritKeyValueDataList());
		String unkJsonData = NotifyUtil.getJsonStringForCloudNotify(dtoReq.getNotifyCloudInfo().getUnkKeyValueDataList());
		// 情報
		infoReq.getNotifyCloudInfo().setInfoJsonData(infoJsonData);
		// 警告
		infoReq.getNotifyCloudInfo().setWarnJsonData(warnJsonData);
		// 危険
		infoReq.getNotifyCloudInfo().setCritJsonData(critJsonData);
		// 不明
		infoReq.getNotifyCloudInfo().setUnkJsonData(unkJsonData);

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().modifyNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		CloudNotifyInfoResponse dtoRes = new CloudNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		// 個別変換
		dtoRes.getNotifyCloudInfo().setTextScope(infoRes.getNotifyCloudInfo().getScopeText());

		
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * メッセージ通知の変更を行うAPI
	 */
	@PUT
	@Path("/message/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyMessageNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MessageNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Message, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ })
	public Response modifyMessageNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "modifyMessageNotifyBody", content = @Content(schema = @Schema(implementation = ModifyMessageNotifyRequest.class))) String requestBody)
			throws NotifyDuplicate, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, NotifyNotFound {
		m_log.info("call modifyMessageNotify()");

		// JSONからDTOへ変換
		ModifyMessageNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyMessageNotifyRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		NotifyInfo infoReq = new NotifyInfo(notifyId);
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().modifyNotify(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		MessageNotifyInfoResponse dtoRes = new MessageNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 通知の削除を行うAPI
	 */
	@DELETE
	@Path("/notify")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NotifyInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.Notify, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ })
	public Response deleteNotify(@ArrayTypeParam @QueryParam(value = "notifyIds") String notifyIds,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole{
		m_log.info("call deleteNotify()");

		String[] notifyIdArray = new String[0];
		if(notifyIds != null && !notifyIds.isEmpty()) {
			notifyIdArray = notifyIds.split(",");
		}

		// ControllerBean呼び出し
		List<NotifyInfo> infoResList = new NotifyControllerBean().deleteNotify(notifyIdArray);

		// ControllerBeanからのINFOをDTOへ変換
		List<NotifyInfoResponse> dtoResList = new ArrayList<NotifyInfoResponse>();
		for (NotifyInfo info : infoResList) {
			NotifyInfoResponse dto = new NotifyInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 指定したステータス通知の取得を行うAPI
	 */
	@GET
	@Path("/status/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetStatusNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StatusNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Status, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getStatusNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getStatusNotify()");

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().getNotify(notifyId, NotifyTypeConstant.TYPE_STATUS);

		// ControllerBeanからのINFOをDTOへ変換
		StatusNotifyInfoResponse dtoRes = new StatusNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 指定したイベント通知の取得を行うAPI
	 */
	@GET
	@Path("/event/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetEventNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Event, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getEventNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getEventNotify()");

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().getNotify(notifyId, NotifyTypeConstant.TYPE_EVENT);

		// ControllerBeanからのINFOをDTOへ変換
		EventNotifyInfoResponse dtoRes = new EventNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 指定したメール通知の取得を行うAPI
	 */
	@GET
	@Path("/mail/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMailNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MailNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Mail, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getMailNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getMailNotify()");

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().getNotify(notifyId, NotifyTypeConstant.TYPE_MAIL);

		// ControllerBeanからのINFOをDTOへ変換
		MailNotifyInfoResponse dtoRes = new MailNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		// 個別変換
		dtoRes.getNotifyMailInfo().setMailTemplateId(infoRes.getNotifyMailInfo().getMailTemplateId());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 指定したジョブ通知の取得を行うAPI
	 */
	@GET
	@Path("/job/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Job, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getJobNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJobNotify()");

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().getNotify(notifyId, NotifyTypeConstant.TYPE_JOB);

		// ControllerBeanからのINFOをDTOへ変換
		JobNotifyInfoResponse dtoRes = new JobNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		// 個別変換
		dtoRes.getNotifyJobInfo().setJobExecScope(infoRes.getNotifyJobInfo().getJobExecScope());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 指定したログエスカレーション通知の取得を行うAPI
	 */
	@GET
	@Path("/logEscalate/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetLogEscalateNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LogEscalateNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.LogEscalate, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getLogEscalateNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getLogEscalateNotify()");

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().getNotify(notifyId, NotifyTypeConstant.TYPE_LOG_ESCALATE);

		// ControllerBeanからのINFOをDTOへ変換
		LogEscalateNotifyInfoResponse dtoRes = new LogEscalateNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		// 個別変換
		dtoRes.getNotifyLogEscalateInfo().setEscalateScope(infoRes.getNotifyLogEscalateInfo().getEscalateScope());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 指定したコマンド通知の取得を行うAPI
	 */
	@GET
	@Path("/command/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCommandNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CommandNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Command, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getCommandNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getCommandNotify()");

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().getNotify(notifyId, NotifyTypeConstant.TYPE_COMMAND);

		// ControllerBeanからのINFOをDTOへ変換
		CommandNotifyInfoResponse dtoRes = new CommandNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 指定した環境構築通知の取得を行うAPI
	 */
	@GET
	@Path("/infra/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetInfraNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InfraNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Infra, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getInfraNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getInfraNotify()");

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().getNotify(notifyId, NotifyTypeConstant.TYPE_INFRA);

		// ControllerBeanからのINFOをDTOへ変換
		InfraNotifyInfoResponse dtoRes = new InfraNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		// 個別変換
		dtoRes.getNotifyInfraInfo().setInfraExecScope(infoRes.getNotifyInfraInfo().getInfraExecScope());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 指定したREST通知の取得を行うAPI
	 */
	@GET
	@Path("/rest/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRestNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RestNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get , target = LogTarget.Rest, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getRestNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getRestNotify()");

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().getNotify(notifyId, NotifyTypeConstant.TYPE_REST);

		// ControllerBeanからのINFOをDTOへ変換
		RestNotifyInfoResponse dtoRes = new RestNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 指定したクラウド通知の取得を行うAPI
	 */
	@GET
	@Path("/cloud/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCloudNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Cloud, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	@IgnoreCommandline
	@IgnoreReference
	public Response getCloudNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getCloudNotify()");

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().getNotify(notifyId, NotifyTypeConstant.TYPE_CLOUD);

		// ControllerBeanからのINFOをDTOへ変換
		CloudNotifyInfoResponse dtoRes = new CloudNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		// 個別変換
		dtoRes.getNotifyCloudInfo().setTextScope(infoRes.getNotifyCloudInfo().getTextScope());
		// クラウド通知のディテール/データはDB上ではjsonで保存しているが
		// そのままではUtilityなどでの使い勝手が悪いので、コンポーネント間のやり取りでは
		// キーバリューオブジェクトのリストに変換している
		// ここではjson→リストの変換を実施
		dtoRes.getNotifyCloudInfo()
				.setInfoKeyValueDataList(NotifyUtil.getDataListForCloudNotify(infoRes.getNotifyCloudInfo().getInfoJsonData()));
		dtoRes.getNotifyCloudInfo()
				.setWarnKeyValueDataList(NotifyUtil.getDataListForCloudNotify(infoRes.getNotifyCloudInfo().getWarnJsonData()));
		dtoRes.getNotifyCloudInfo()
				.setCritKeyValueDataList(NotifyUtil.getDataListForCloudNotify(infoRes.getNotifyCloudInfo().getCritJsonData()));
		dtoRes.getNotifyCloudInfo()
				.setUnkKeyValueDataList(NotifyUtil.getDataListForCloudNotify(infoRes.getNotifyCloudInfo().getUnkJsonData()));

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 指定したメッセージ通知の取得を行うAPI
	 */
	@GET
	@Path("/message/{notifyId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMessageNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MessageNotifyInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Message, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getMessageNotify(@PathParam(value = "notifyId") String notifyId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getMessageNotify()");

		// ControllerBean呼び出し
		NotifyInfo infoRes = new NotifyControllerBean().getNotify(notifyId, NotifyTypeConstant.TYPE_MESSAGE);

		// ControllerBeanからのINFOをDTOへ変換
		MessageNotifyInfoResponse dtoRes = new MessageNotifyInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 全通知の取得を行うAPI
	 */
	@GET
	@Path("/notify")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetNotifyList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NotifyInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Notify, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getNotifyList(@QueryParam(value = "ownerRoleId") String ownerRoleId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getNotifyList()");

		// ControllerBean呼び出し
		List<NotifyInfo> infoResList = null;
		if (ownerRoleId == null || ownerRoleId.equals("")) {
			infoResList = new NotifyControllerBean().getNotifyList();
		} else {
			infoResList = new NotifyControllerBean().getNotifyListByOwnerRole(ownerRoleId);
		}

		// ControllerBeanからのINFOをDTOへ変換
		List<NotifyInfoResponse> dtoResList = new ArrayList<NotifyInfoResponse>();
		for (NotifyInfo info : infoResList) {
			NotifyInfoResponse dto = new NotifyInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 全ステータス通知の取得を行うAPI
	 */
	@GET
	@Path("/status")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetStatusNotifyList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = StatusNotifyInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Status, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getStatusNotifyList(@QueryParam(value = "ownerRoleId") String ownerRoleId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getStatusNotifyList()");

		List<NotifyInfo> infoResList = null;
		if (ownerRoleId == null || ownerRoleId.equals("")) {
			infoResList = new NotifyControllerBean().getNotifyList(NotifyTypeConstant.TYPE_STATUS);
		} else {
			infoResList = new NotifyControllerBean().getNotifyListByOwnerRole(ownerRoleId, NotifyTypeConstant.TYPE_STATUS);
		}

		List<StatusNotifyInfoResponse> dtoResList = new ArrayList<StatusNotifyInfoResponse>();
		for (NotifyInfo info : infoResList) {
			StatusNotifyInfoResponse dto = new StatusNotifyInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 全イベント通知の取得を行うAPI
	 */
	@GET
	@Path("/event")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetEventNotifyList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventNotifyInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Event, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getEventNotifyList(@QueryParam(value = "ownerRoleId") String ownerRoleId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getEventNotifyList()");

		List<NotifyInfo> infoResList = null;
		if (ownerRoleId == null || ownerRoleId.equals("")) {
			infoResList = new NotifyControllerBean().getNotifyList(NotifyTypeConstant.TYPE_EVENT);
		} else {
			infoResList = new NotifyControllerBean().getNotifyListByOwnerRole(ownerRoleId, NotifyTypeConstant.TYPE_EVENT);
		}

		List<EventNotifyInfoResponse> dtoResList = new ArrayList<EventNotifyInfoResponse>();
		for (NotifyInfo info : infoResList) {
			EventNotifyInfoResponse dto = new EventNotifyInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 全メール通知の取得を行うAPI
	 */
	@GET
	@Path("/mail")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMailNotifyList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MailNotifyInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Mail, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getMailNotifyList(@QueryParam(value = "ownerRoleId") String ownerRoleId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getMailNotifyList()");

		List<NotifyInfo> infoResList = null;
		if (ownerRoleId == null || ownerRoleId.equals("")) {
			infoResList = new NotifyControllerBean().getNotifyList(NotifyTypeConstant.TYPE_MAIL);
		} else {
			infoResList = new NotifyControllerBean().getNotifyListByOwnerRole(ownerRoleId, NotifyTypeConstant.TYPE_MAIL);
		}

		List<MailNotifyInfoResponse> dtoResList = new ArrayList<MailNotifyInfoResponse>();
		for (NotifyInfo info : infoResList) {
			MailNotifyInfoResponse dto = new MailNotifyInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 全ジョブ通知の取得を行うAPI
	 */
	@GET
	@Path("/job")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobNotifyList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = JobNotifyInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Job, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getJobNotifyList(@QueryParam(value = "ownerRoleId") String ownerRoleId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getJobNotifyList()");

		List<NotifyInfo> infoResList = null;
		if (ownerRoleId == null || ownerRoleId.equals("")) {
			infoResList = new NotifyControllerBean().getNotifyList(NotifyTypeConstant.TYPE_JOB);
		} else {
			infoResList = new NotifyControllerBean().getNotifyListByOwnerRole(ownerRoleId, NotifyTypeConstant.TYPE_JOB);
		}

		List<JobNotifyInfoResponse> dtoResList = new ArrayList<JobNotifyInfoResponse>();
		for (NotifyInfo info : infoResList) {
			JobNotifyInfoResponse dto = new JobNotifyInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 全ログエスカレーション通知の取得を行うAPI
	 */
	@GET
	@Path("/logEscalate")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetLogEscalateNotifyList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LogEscalateNotifyInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.LogEscalate, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getLogEscalateNotifyList(@QueryParam(value = "ownerRoleId") String ownerRoleId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getLogEscalateNotifyList()");

		List<NotifyInfo> infoResList = null;
		if (ownerRoleId == null || ownerRoleId.equals("")) {
			infoResList = new NotifyControllerBean().getNotifyList(NotifyTypeConstant.TYPE_LOG_ESCALATE);
		} else {
			infoResList = new NotifyControllerBean().getNotifyListByOwnerRole(ownerRoleId, NotifyTypeConstant.TYPE_LOG_ESCALATE);
		}

		List<LogEscalateNotifyInfoResponse> dtoResList = new ArrayList<LogEscalateNotifyInfoResponse>();
		for (NotifyInfo info : infoResList) {
			LogEscalateNotifyInfoResponse dto = new LogEscalateNotifyInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 全コマンド通知の取得を行うAPI
	 */
	@GET
	@Path("/command")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCommandNotifyList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CommandNotifyInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Command, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getCommandNotifyList(@QueryParam(value = "ownerRoleId") String ownerRoleId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getCommandNotifyList()");

		List<NotifyInfo> infoResList = null;
		if (ownerRoleId == null || ownerRoleId.equals("")) {
			infoResList = new NotifyControllerBean().getNotifyList(NotifyTypeConstant.TYPE_COMMAND);
		} else {
			infoResList = new NotifyControllerBean().getNotifyListByOwnerRole(ownerRoleId, NotifyTypeConstant.TYPE_COMMAND);
		}

		List<CommandNotifyInfoResponse> dtoResList = new ArrayList<CommandNotifyInfoResponse>();
		for (NotifyInfo info : infoResList) {
			CommandNotifyInfoResponse dto = new CommandNotifyInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 全環境構築通知の取得を行うAPI
	 */
	@GET
	@Path("/infra")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetInfraNotifyList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InfraNotifyInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Infra, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getInfraNotifyList(@QueryParam(value = "ownerRoleId") String ownerRoleId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getInfraNotifyList()");

		List<NotifyInfo> infoResList = null;
		if (ownerRoleId == null || ownerRoleId.equals("")) {
			infoResList = new NotifyControllerBean().getNotifyList(NotifyTypeConstant.TYPE_INFRA);
		} else {
			infoResList = new NotifyControllerBean().getNotifyListByOwnerRole(ownerRoleId, NotifyTypeConstant.TYPE_INFRA);
		}

		List<InfraNotifyInfoResponse> dtoResList = new ArrayList<InfraNotifyInfoResponse>();
		for (NotifyInfo info : infoResList) {
			InfraNotifyInfoResponse dto = new InfraNotifyInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 全REST通知の取得を行うAPI
	 */
	@GET
	@Path("/rest")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRestNotifyList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RestNotifyInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get , target = LogTarget.Rest, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getRestNotifyList(@QueryParam(value = "ownerRoleId") String ownerRoleId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getRestNotifyList()");


		List<NotifyInfo> infoResList = null;
		if (ownerRoleId == null || ownerRoleId.equals("")) {
			infoResList = new NotifyControllerBean().getNotifyList(NotifyTypeConstant.TYPE_REST);
		} else {
			infoResList = new NotifyControllerBean().getNotifyListByOwnerRole(ownerRoleId, NotifyTypeConstant.TYPE_REST);
		}

		// ControllerBeanからのINFOをDTOへ変換
		List<RestNotifyInfoResponse> dtoResList = new ArrayList<RestNotifyInfoResponse>();
		for (NotifyInfo info : infoResList) {
			RestNotifyInfoResponse dto = new RestNotifyInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * 全クラウド通知の取得を行うAPI
	 */
	@GET
	@Path("/cloud")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCloudNotifyList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CloudNotifyInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Cloud, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	@IgnoreCommandline
	@IgnoreReference
	public Response getCloudNotifyList(@QueryParam(value = "ownerRoleId") String ownerRoleId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getCloudNotifyList()");


		List<NotifyInfo> infoResList = null;
		if (ownerRoleId == null || ownerRoleId.equals("")) {
			infoResList = new NotifyControllerBean().getNotifyList(NotifyTypeConstant.TYPE_CLOUD);
		} else {
			infoResList = new NotifyControllerBean().getNotifyListByOwnerRole(ownerRoleId, NotifyTypeConstant.TYPE_CLOUD);
		}

		List<CloudNotifyInfoResponse> dtoResList = new ArrayList<CloudNotifyInfoResponse>();
		for (NotifyInfo info : infoResList) {
			CloudNotifyInfoResponse dto = new CloudNotifyInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			// 個別変換
			dto.getNotifyCloudInfo().setTextScope(info.getNotifyCloudInfo().getTextScope());
			// クラウド通知のディテール/データはDB上ではjsonで保存しているが
			// そのままではUtilityなどでの使い勝手が悪いので、コンポーネント間のやり取りでは
			// キーバリューオブジェクトのリストに変換している
			// ここではjson→リストの変換を実施
			dto.getNotifyCloudInfo()
					.setInfoKeyValueDataList(NotifyUtil.getDataListForCloudNotify(info.getNotifyCloudInfo().getInfoJsonData()));
			dto.getNotifyCloudInfo()
					.setWarnKeyValueDataList(NotifyUtil.getDataListForCloudNotify(info.getNotifyCloudInfo().getWarnJsonData()));
			dto.getNotifyCloudInfo()
					.setCritKeyValueDataList(NotifyUtil.getDataListForCloudNotify(info.getNotifyCloudInfo().getCritJsonData()));
			dto.getNotifyCloudInfo()
					.setUnkKeyValueDataList(NotifyUtil.getDataListForCloudNotify(info.getNotifyCloudInfo().getUnkJsonData()));
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 全メッセージ通知の取得を行うAPI
	 */
	@GET
	@Path("/message")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMessageNotifyList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MessageNotifyInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Message, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response getMessageNotifyList(@QueryParam(value = "ownerRoleId") String ownerRoleId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call getMessageNotifyList()");

		List<NotifyInfo> infoResList = null;
		if (ownerRoleId == null || ownerRoleId.equals("")) {
			infoResList = new NotifyControllerBean().getNotifyList(NotifyTypeConstant.TYPE_MESSAGE);
		} else {
			infoResList = new NotifyControllerBean().getNotifyListByOwnerRole(ownerRoleId, NotifyTypeConstant.TYPE_MESSAGE);
		}

		List<MessageNotifyInfoResponse> dtoResList = new ArrayList<MessageNotifyInfoResponse>();
		for (NotifyInfo info : infoResList) {
			MessageNotifyInfoResponse dto = new MessageNotifyInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 指定した通知IDを利用している通知グループIDの取得を行うAPI
	 */
	@GET
	@Path("/notifyRelation_groupId")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "CheckNotifyId")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NotifyCheckIdResultInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.NotifyRelation, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response checkNotifyId(@ArrayTypeParam @QueryParam(value = "notifyIds") String notifyIds,
			@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call checkNotifyId()");

		String[] notifyIdArray = new String[0];
		if(notifyIds != null && !notifyIds.isEmpty()) {
			notifyIdArray = notifyIds.split(",");
		}
		
		// ControllerBean呼び出し
		List<NotifyCheckIdResultInfo> infoResList = new NotifyControllerBean().checkNotifyId(notifyIdArray);

		// ControllerBeanからのINFOをDTOへ変換
		List<NotifyCheckIdResultInfoResponse> dtoResList = new ArrayList<NotifyCheckIdResultInfoResponse>();
		for (NotifyCheckIdResultInfo info : infoResList) {
			NotifyCheckIdResultInfoResponse dto = new NotifyCheckIdResultInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 指定した通知IDの有効化/無効化を行うAPI
	 */
	@PUT
	@Path("/notify_valid")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SetNotifyValid")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = NotifyInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Notify, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ })
	public Response setNotifyValid(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "setNotifyValidBody", content = @Content(schema = @Schema(implementation = SetNotifyValidRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, NotifyNotFound, NotifyDuplicate, InvalidSetting {
		m_log.info("call setNotifyValid()");

		// JSONからDTOへ変換
		SetNotifyValidRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, SetNotifyValidRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// ControllerBean呼び出し
		List<NotifyInfo> infoResList = new NotifyControllerBean().setNotifyValid(dtoReq.getNotifyIds(), dtoReq.getValidFlg());

		// ControllerBeanからのINFOをDTOへ変換
		List<NotifyInfoResponse> dtoResList = new ArrayList<NotifyInfoResponse>();
		for (NotifyInfo info : infoResList) {
			NotifyInfoResponse dto = new NotifyInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 直接通知処理を実行するAPI
	 */
	@POST
	@Path("/notify_execAsMonitor")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "NotifyAsMonitor")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Exec, target = LogTarget.Notify, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response notifyAsMonitor(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "notifyAsMonitorBody", content = @Content(schema = @Schema(implementation = NotifyAsMonitorRequest.class))) String requestBody)
			throws InvalidRole, InvalidUserPass, HinemosUnknown, NotifyNotFound, FacilityNotFound, InvalidSetting {
		m_log.info("call notifyAsMonitor()");

		// JSONからDTOへ変換
		NotifyAsMonitorRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, NotifyAsMonitorRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// ControllerBean呼び出し
		new NotifyControllerBean().notify(
				dtoReq.getPluginId(),
				dtoReq.getMonitorId(),
				dtoReq.getFacilityId(),
				dtoReq.getSubKey(),
				RestCommonConverter.convertDTStringToHinemosTime(dtoReq.getGenerationDate(), ""),
				dtoReq.getPriority().getCode(),
				dtoReq.getApplication(),
				dtoReq.getMessage(),
				dtoReq.getMessageOrg(),
				dtoReq.getNotifyIdList(),
				dtoReq.getSrcId());

		return Response.status(Status.OK).build();
	}

	/**
	 * 直接イベント通知処理を実行するAPI
	 */
	@POST
	@Path("/event_exec")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "NotifyEvent")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventDataInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Exec, target = LogTarget.Event, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.READ })
	public Response notifyEvent(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "notifyEventBody", content = @Content(schema = @Schema(implementation = NotifyEventRequest.class))) String requestBody)
			throws InvalidRole, InvalidUserPass, InvalidSetting, HinemosUnknown, FacilityNotFound {
		m_log.info("call notifyEvent()");

		// JSONからDTOへ変換
		NotifyEventRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, NotifyEventRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		EventNotifyInfo infoReq = new EventNotifyInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		EventDataInfo infoRes = NotifyControllerBean.notifyUserExtentionEvent(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		EventDataInfoResponse dtoRes = new EventDataInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
}
