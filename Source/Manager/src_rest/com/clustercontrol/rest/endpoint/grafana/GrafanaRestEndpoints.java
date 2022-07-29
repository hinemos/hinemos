/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.grafana;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.glassfish.grizzly.http.server.Request;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeFunction;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.grafana.session.GrafanaControllerBean;
import com.clustercontrol.rest.annotation.RestLog;
import com.clustercontrol.rest.annotation.RestLog.LogAction;
import com.clustercontrol.rest.annotation.RestLog.LogTarget;
import com.clustercontrol.rest.annotation.RestLog.LogType;
import com.clustercontrol.rest.annotation.RestLogFunc;
import com.clustercontrol.rest.annotation.RestLogFunc.LogFuncName;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreCommandline;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreReference;
import com.clustercontrol.rest.endpoint.grafana.dto.GetEventAggregationRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetEventAggregationResponse;
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobHistoryAggregationRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobHistoryAggregationResponse;
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobLastRunTimeListRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetJobLastRunTimeListResponse;
import com.clustercontrol.rest.endpoint.grafana.dto.GetStatusAggregationRequest;
import com.clustercontrol.rest.endpoint.grafana.dto.GetStatusAggregationResponse;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;

@Path("/grafana")
@RestLogFunc(name = LogFuncName.Grafana)
public class GrafanaRestEndpoints {
	private static Log m_log = LogFactory.getLog(GrafanaRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "grafana";

	@POST
	@Path("/eventAggregation")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetEventAggregation")
	@RestLog(action = LogAction.Get, target = LogTarget.EventAggregation, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorResult, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetEventAggregationResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getEventAggregation(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "getEventAggregationBody", content = @Content(schema = @Schema(implementation = GetEventAggregationRequest.class))) String requestBody)
			throws InvalidSetting, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getEventAggregation()");

		GetEventAggregationRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				GetEventAggregationRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// ControllerBean呼び出し
		GetEventAggregationResponse dtoRes = new GrafanaControllerBean().getEventAggregation(dtoReq);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	@POST
	@Path("/statusAggregation")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetStatusAggregation")
	@RestLog(action = LogAction.Get, target = LogTarget.StatusAggregation, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorResult, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetStatusAggregationResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getStatusAggregation(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "getStatusAggregationBody", content = @Content(schema = @Schema(implementation = GetStatusAggregationRequest.class))) String requestBody)
			throws InvalidSetting, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getStatusAggregation()");

		GetStatusAggregationRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				GetStatusAggregationRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// ControllerBean呼び出し
		GetStatusAggregationResponse dtoRes = new GrafanaControllerBean().getStatusAggregation(dtoReq);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	@POST
	@Path("/jobHistoryAggregation")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetjobHistoryAggregation")
	@RestLog(action = LogAction.Get, target = LogTarget.JobHistoryAggregation, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetJobHistoryAggregationResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getjobHistoryAggregation(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "getjobHistoryAggregationBody", content = @Content(schema = @Schema(implementation = GetJobHistoryAggregationRequest.class))) String requestBody)
			throws InvalidSetting, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getjobHistoryAggregation()");

		GetJobHistoryAggregationRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				GetJobHistoryAggregationRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// ControllerBean呼び出し
		GetJobHistoryAggregationResponse dtoRes = new GrafanaControllerBean().getJobHistoryAggregation(dtoReq);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	@POST
	@Path("/jobLastRunTime")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetJobLastRunTimeList")
	@RestLog(action = LogAction.Get, target = LogTarget.JobLastRunTime, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetJobLastRunTimeListResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	@IgnoreReference
	public Response getJobLastRunTimeList(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "getJobLastRunTimeListBody", content = @Content(schema = @Schema(implementation = GetJobLastRunTimeListRequest.class))) String requestBody)
			throws InvalidSetting, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getJobLastRunTimeList()");

		GetJobLastRunTimeListRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				GetJobLastRunTimeListRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// ControllerBean呼び出し
		GetJobLastRunTimeListResponse dtoRes = new GrafanaControllerBean().getJobLastRunTimeList(dtoReq);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
}
