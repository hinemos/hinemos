/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.hub;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_404;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_409;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

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
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.LogFormatDuplicate;
import com.clustercontrol.fault.LogFormatKeyPatternDuplicate;
import com.clustercontrol.fault.LogFormatNotFound;
import com.clustercontrol.fault.LogFormatUsed;
import com.clustercontrol.fault.LogTransferDuplicate;
import com.clustercontrol.fault.LogTransferNotFound;
import com.clustercontrol.hub.bean.TransferInfoDestTypeMst;
import com.clustercontrol.hub.model.LogFormat;
import com.clustercontrol.hub.model.TransferInfo;
import com.clustercontrol.hub.session.HubControllerBean;
import com.clustercontrol.rest.annotation.RestLog;
import com.clustercontrol.rest.annotation.RestLog.LogAction;
import com.clustercontrol.rest.annotation.RestLog.LogTarget;
import com.clustercontrol.rest.annotation.RestLog.LogType;
import com.clustercontrol.rest.annotation.RestLogFunc;
import com.clustercontrol.rest.annotation.RestLogFunc.LogFuncName;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.ArrayTypeParam;
import com.clustercontrol.rest.endpoint.hub.dto.AddLogFormatRequest;
import com.clustercontrol.rest.endpoint.hub.dto.AddTransferInfoRequest;
import com.clustercontrol.rest.endpoint.hub.dto.LogFormatResponse;
import com.clustercontrol.rest.endpoint.hub.dto.LogFormatResponseP1;
import com.clustercontrol.rest.endpoint.hub.dto.ModifyLogFormatRequest;
import com.clustercontrol.rest.endpoint.hub.dto.ModifyTransferInfoRequest;
import com.clustercontrol.rest.endpoint.hub.dto.SetTransferValidRequest;
import com.clustercontrol.rest.endpoint.hub.dto.TransferInfoDestTypeMstResponse;
import com.clustercontrol.rest.endpoint.hub.dto.TransferInfoResponse;
import com.clustercontrol.rest.endpoint.hub.dto.TransferInfoResponseP1;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;

@Path("/hub")
@RestLogFunc(name = LogFuncName.Hub)
public class HubRestEndpoints {
	private static Log m_log = LogFactory.getLog(HubRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "hub";

	@GET
	@Path("/logFormat/{logFormatId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetLogFormat")
	@RestLog(action = LogAction.Get, target = LogTarget.LogFormat, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorSetting, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LogFormatResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLogFormat(@Context Request request, @Context UriInfo uriInfo,
			@PathParam(value = "logFormatId") String logFormatId) throws LogFormatNotFound, InvalidRole, HinemosUnknown {
		m_log.info("call getLogFormat()");
		LogFormat infoRes = new HubControllerBean().getLogFormat(logFormatId);
		LogFormatResponse dtoRes = new LogFormatResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	@GET
	@Path("/logFormat_id")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetLogFormatIdList")
	@RestLog(action = LogAction.Get, target = LogTarget.LogFormat, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorSetting, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LogFormatResponseP1.class, type=SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLogFormatIdList(@Context Request request, @Context UriInfo uriInfo,
			@QueryParam("ownerRoleId") String ownerRoleId) throws InvalidRole, HinemosUnknown {
		m_log.info("call getLogFormatIdList()");
		List<LogFormatResponseP1> dtoResList = new ArrayList<>();
		List<String> idList = new HubControllerBean().getLogFormatIdList(ownerRoleId);
		for (String id : idList) {
			LogFormatResponseP1 dtoRes = new LogFormatResponseP1();
			dtoRes.setLogFormatId(id);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	@GET
	@Path("/logFormat")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetLogFormatListByOwnerRole")
	@RestLog(action = LogAction.Get, target = LogTarget.LogFormat, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorSetting, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LogFormatResponse.class, type=SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLogFormatListByOwnerRole(@Context Request request, @Context UriInfo uriInfo,
			@QueryParam("ownerRoleId") String ownerRoleId) throws InvalidRole, HinemosUnknown {
		m_log.info("call getLogFormatListByOwnerRole()");
		List<LogFormatResponse> dtoResList = new ArrayList<>();
		List<LogFormat> infoResList = new HubControllerBean().getLogFormatListByOwnerRole(ownerRoleId);
		for (LogFormat infoRes : infoResList){
			LogFormatResponse dtoRes = new LogFormatResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	@POST
	@Path("/logFormat")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddLogFormat")
	@RestLog(action = LogAction.Add, target = LogTarget.LogFormat, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorSetting, modeList = { SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LogFormatResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addLogFormat(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addLogFormatBody", content = @Content(schema = @Schema(implementation = AddLogFormatRequest.class))) String requestBody)
			throws InvalidRole, HinemosUnknown, InvalidSetting, LogFormatDuplicate, LogFormatKeyPatternDuplicate {
		m_log.info("call addLogFormat()");
		AddLogFormatRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddLogFormatRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		LogFormat infoReq = new LogFormat();
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		LogFormat infoRes = new HubControllerBean().addLogFormat(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		LogFormatResponse dtoRes = new LogFormatResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	@PUT
	@Path("/logFormat/{logFormatId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyLogFormat")
	@RestLog(action = LogAction.Modify, target = LogTarget.LogFormat, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorSetting, modeList = { SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LogFormatResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyLogFormat(@Context Request request, @Context UriInfo uriInfo,
			@PathParam(value = "logFormatId") String logFormatId,
			@RequestBody(description = "modifyLogFormatBody", content = @Content(schema = @Schema(implementation = ModifyLogFormatRequest.class))) String requestBody)
			throws InvalidRole, HinemosUnknown, InvalidSetting, LogFormatNotFound, LogFormatKeyPatternDuplicate {
		m_log.info("call modifyLogFormat()");
		ModifyLogFormatRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyLogFormatRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		LogFormat infoReq = new LogFormat();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setLogFormatId(logFormatId);

		// ControllerBean呼び出し
		LogFormat infoRes = new HubControllerBean().modifyLogFormat(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		LogFormatResponse dtoRes = new LogFormatResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	@DELETE
	@Path("/logFormat")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteLogFormat")
	@RestLog(action = LogAction.Delete, target = LogTarget.LogFormat, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorSetting, modeList = { SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = LogFormatResponse.class, type=SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteLogFormat(@Context Request request, @Context UriInfo uriInfo,
			@ArrayTypeParam @QueryParam("logFormatIds") String logFormatIds)
			throws InvalidRole, HinemosUnknown, LogFormatNotFound, LogFormatUsed {
		m_log.info("call deleteLogFormat()");
		
		List<String> logFormatIdList = new ArrayList<>();
		if(logFormatIds != null && !logFormatIds.isEmpty()) {
			logFormatIdList = Arrays.asList(logFormatIds.split(","));
		}
		
		List<LogFormat> infoResList = new HubControllerBean().deleteLogFormat(logFormatIdList);
		List<LogFormatResponse> dtoResList = new ArrayList<>();
		for (LogFormat infoRes : infoResList) {
			LogFormatResponse dtoRes = new LogFormatResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	@GET
	@Path("/transfer/{transferId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetTransferInfo")
	@RestLog(action = LogAction.Get, target = LogTarget.Transfer, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Hub, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TransferInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTransferInfo(@Context Request request, @Context UriInfo uriInfo,
			@PathParam(value = "transferId") String transferId) throws LogTransferNotFound, InvalidRole, HinemosUnknown {
		m_log.info("call getTransferInfo()");
		TransferInfo infoRes = new HubControllerBean().getTransferInfo(transferId);
		TransferInfoResponse dtoRes = new TransferInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	@GET
	@Path("/transfer_id")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetTransferInfoIdList")
	@RestLog(action = LogAction.Get, target = LogTarget.Transfer, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Hub, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TransferInfoResponseP1.class, type=SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTransferInfoIdList(@Context Request request, @Context UriInfo uriInfo,
			@QueryParam("ownerRoleId") String ownerRoleId) throws InvalidRole, HinemosUnknown {
		m_log.info("call getTransferInfoIdList()");
		List<TransferInfoResponseP1> dtoResList = new ArrayList<>();
		List<String> idList = new HubControllerBean().getTransferInfoIdList(ownerRoleId);
		for (String id : idList) {
			TransferInfoResponseP1 dtoRes = new TransferInfoResponseP1();
			dtoRes.setTransferId(id);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	@GET
	@Path("/transfer")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetTransferInfoListByOwnerRole")
	@RestLog(action = LogAction.Get, target = LogTarget.Transfer, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Hub, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TransferInfoResponse.class, type=SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTransferInfoListByOwnerRole(@Context Request request, @Context UriInfo uriInfo,
			@QueryParam("ownerRoleId") String ownerRoleId) throws InvalidRole, HinemosUnknown {
		m_log.info("call getTransferInfoListByOwnerRole()");
		List<TransferInfoResponse> dtoResList = new ArrayList<>();
		List<TransferInfo> infoResList = new HubControllerBean().getTransferInfoListByOwnerRole(ownerRoleId);
		for (TransferInfo infoRes : infoResList) {
			TransferInfoResponse dtoRes = new TransferInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	@POST
	@Path("/transfer")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddTransferInfo")
	@RestLog(action = LogAction.Add, target = LogTarget.Transfer, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Hub, modeList = { SystemPrivilegeMode.READ, SystemPrivilegeMode.ADD })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TransferInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addTransferInfo(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addTransferInfoBody", content = @Content(schema = @Schema(implementation = AddTransferInfoRequest.class))) String requestBody)
			throws InvalidRole, HinemosUnknown, InvalidSetting, LogTransferDuplicate {
		m_log.info("call addTransferInfo()");
		AddTransferInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddTransferInfoRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		TransferInfo infoReq = new TransferInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		TransferInfo infoRes = new HubControllerBean().addTransferInfo(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		TransferInfoResponse dtoRes = new TransferInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	@PUT
	@Path("/transfer/{transferId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyTransferInfo")
	@RestLog(action = LogAction.Modify, target = LogTarget.Transfer, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Hub, modeList = { SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TransferInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyTransferInfo(@Context Request request, @Context UriInfo uriInfo,
			@PathParam(value = "transferId") String transferId,
			@RequestBody(description = "modifyTransferInfoBody", content = @Content(schema = @Schema(implementation = ModifyTransferInfoRequest.class))) String requestBody)
			throws InvalidRole, HinemosUnknown, InvalidSetting, LogTransferNotFound {
		m_log.info("call modifyTransferInfo()");
		ModifyTransferInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyTransferInfoRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// DTOからINFOへ変換
		TransferInfo infoReq = new TransferInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// ControllerBean呼び出し
		infoReq.setTransferId(transferId);
		TransferInfo infoRes = new HubControllerBean().modifyTransferInfo(infoReq);

		// ControllerBeanからのINFOをDTOへ変換
		TransferInfoResponse dtoRes = new TransferInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	@DELETE
	@Path("/transfer")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteTransferInfo")
	@RestLog(action = LogAction.Delete, target = LogTarget.Transfer, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Hub, modeList = { SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TransferInfoResponse.class, type=SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteTransferInfo(@Context Request request, @Context UriInfo uriInfo,
			@ArrayTypeParam @QueryParam("transferIds") String transferIds)
			throws InvalidRole, HinemosUnknown, LogTransferNotFound {
		m_log.info("call deleteTransferInfo()");
		
		List<String> transferInfoIdList = new ArrayList<>();
		if(transferIds != null && !transferIds.isEmpty()) {
			transferInfoIdList = Arrays.asList(transferIds.split(","));
		}
		
		List<TransferInfo> infoResList = new HubControllerBean().deleteTransferInfo(transferInfoIdList);
		List<TransferInfoResponse> dtoResList = new ArrayList<>();
		for (TransferInfo infoRes : infoResList) {
			TransferInfoResponse dtoRes = new TransferInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	@GET
	@Path("/transfer_destTypeMst")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "getTransferInfoDestTypeMstList")
	@RestLog(action = LogAction.Get, target = LogTarget.Transfer, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Hub, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TransferInfoDestTypeMstResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTransferInfoDestTypeMstList(@Context Request request, @Context UriInfo uriInfo) throws InvalidRole, HinemosUnknown {
		m_log.info("call getTransferInfoDestTypeMstList()");
		List<TransferInfoDestTypeMst> infoResList = new HubControllerBean().getTransferInfoDestTypeMstList();
		List<TransferInfoDestTypeMstResponse> dtoResList = new ArrayList<>();
		for (TransferInfoDestTypeMst infoRes : infoResList){
			TransferInfoDestTypeMstResponse dtoRes = new TransferInfoDestTypeMstResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	@PUT
	@Path("/transfer_valid")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SetTransferValid")
	@RestLog(action = LogAction.Modify, target = LogTarget.Transfer, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Hub, modeList = { SystemPrivilegeMode.READ, SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = TransferInfoResponse.class, type=SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setTransferValid(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "setTransferValidBody", content = @Content(schema = @Schema(implementation = SetTransferValidRequest.class))) String requestBody)
			throws InvalidRole, HinemosUnknown, InvalidSetting, LogTransferNotFound {
		m_log.info("call setTransferValid()");
		SetTransferValidRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				SetTransferValidRequest.class);
		// 共通バリデーション処理
		RestCommonValitater.checkRequestDto(dtoReq);
		// DTOの項目相関チェック処理
		dtoReq.correlationCheck();

		// ControllerBean呼び出し
		List<TransferInfo> infoResList = HubControllerBean.setTransferValid(dtoReq.getTransferIdList(), dtoReq.getFlg());

		// ControllerBeanからのINFOをDTOへ変換
		List<TransferInfoResponse> dtoResList = new ArrayList<>();
		for (TransferInfo infoRes : infoResList) {
			TransferInfoResponse dtoRes = new TransferInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
}
