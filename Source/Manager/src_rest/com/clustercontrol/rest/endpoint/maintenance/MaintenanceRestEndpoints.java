/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.maintenance;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
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
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MaintenanceDuplicate;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.maintenance.model.MaintenanceTypeMst;
import com.clustercontrol.maintenance.session.MaintenanceControllerBean;
import com.clustercontrol.rest.annotation.RestLog;
import com.clustercontrol.rest.annotation.RestLog.LogAction;
import com.clustercontrol.rest.annotation.RestLog.LogTarget;
import com.clustercontrol.rest.annotation.RestLog.LogType;
import com.clustercontrol.rest.annotation.RestLogFunc;
import com.clustercontrol.rest.annotation.RestLogFunc.LogFuncName;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.ArrayTypeParam;
import com.clustercontrol.rest.endpoint.maintenance.dto.AddMaintenanceRequest;
import com.clustercontrol.rest.endpoint.maintenance.dto.MaintenanceInfoResponse;
import com.clustercontrol.rest.endpoint.maintenance.dto.MaintenanceTypeInfoResponse;
import com.clustercontrol.rest.endpoint.maintenance.dto.ModifyMaintenanceRequest;
import com.clustercontrol.rest.endpoint.maintenance.dto.SetMaintenanceStatusRequest;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;

@Path("/maintenance")
@RestLogFunc(name = LogFuncName.Maintenance)
public class MaintenanceRestEndpoints {

	private static Log m_log = LogFactory.getLog( MaintenanceRestEndpoints.class );

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "maintenance";

	/**
	 * 履歴削除スケジュールを登録するAPI
	 */
	@POST
	@Path("/maintenance")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddMaintenance")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MaintenanceInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.Maintenance, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Maintenance, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.READ })
	public Response addMaintenance(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addMaintenanceRequestBody", content = @Content(schema = @Schema(implementation = AddMaintenanceRequest.class))) String requestBody)
			throws HinemosUnknown, MaintenanceDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addMaintenance()");
		
		AddMaintenanceRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddMaintenanceRequest.class);
		
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MaintenanceInfo infoReq = new MaintenanceInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		MaintenanceInfo infoRes = new MaintenanceControllerBean().addMaintenance(infoReq);
		
		MaintenanceInfoResponse dtoRes = new MaintenanceInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);
		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * 履歴削除スケジュールを変更するAPI
	 */
	@PUT
	@Path("/maintenance/{maintenanceId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyMaintenance")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MaintenanceInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Maintenance, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Maintenance, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ })
	public Response modifyMaintenance(@PathParam("maintenanceId") String maintenanceId, @Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "modifyMaintenanceRequestBody", content = @Content(schema = @Schema(implementation = ModifyMaintenanceRequest.class))) String requestBody)
			throws HinemosUnknown, NotifyNotFound, MaintenanceNotFound, InvalidUserPass, InvalidRole,InvalidSetting {
		m_log.info("call modifyMaintenance()");
		
		ModifyMaintenanceRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyMaintenanceRequest.class);
		
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		MaintenanceInfo infoReq = new MaintenanceInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setMaintenanceId(maintenanceId);
		MaintenanceInfo infoRes = new MaintenanceControllerBean().modifyMaintenance(infoReq);
		
		MaintenanceInfoResponse dtoRes = new MaintenanceInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);
		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * 履歴削除スケジュールを削除するAPI
	 */
	@DELETE
	@Path("/maintenance")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteMaintenance")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MaintenanceInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.Maintenance, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Maintenance, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ })
	public Response deleteMaintenance(@ArrayTypeParam @QueryParam(value = "maintenanceIds") String maintenanceIds,
			@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, NotifyNotFound, MaintenanceNotFound, InvalidUserPass, InvalidRole,InvalidSetting {
		m_log.info("call deleteMaintenance()");

		List<String> maintenanceIdList = new ArrayList<>();
		if(maintenanceIds != null && !maintenanceIds.isEmpty()) {
			maintenanceIdList = Arrays.asList(maintenanceIds.split(","));
		}

		List<MaintenanceInfo> infoResList = new MaintenanceControllerBean().deleteMaintenance(maintenanceIdList);
		List<MaintenanceInfoResponse> dtoResList = new ArrayList<>();
		for(MaintenanceInfo infoRes : infoResList) {
			MaintenanceInfoResponse dtoRes = new MaintenanceInfoResponse();
			RestBeanUtil.convertBean(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		
		RestLanguageConverter.convertMessages(dtoResList);
		
		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * 指定した履歴削除スケジュールを取得するAPI
	 */
	@GET
	@Path("/maintenance/{maintenanceId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMaintenanceInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MaintenanceInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Maintenance, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Maintenance, modeList = { SystemPrivilegeMode.READ })
	public Response getMaintenanceInfo(@PathParam(value = "maintenanceId") String maintenanceId,
			@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, NotifyNotFound, MaintenanceNotFound, InvalidUserPass, InvalidRole,InvalidSetting {
		m_log.info("call getMaintenanceInfo()");
		
		MaintenanceInfo infoRes = new MaintenanceControllerBean().getMaintenanceInfo(maintenanceId);
		
		MaintenanceInfoResponse dtoRes = new MaintenanceInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		RestLanguageConverter.convertMessages(dtoRes);
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 全ての履歴削除スケジュールを取得するAPI
	 */
	@GET
	@Path("/maintenance")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMaintenanceList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MaintenanceInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.Maintenance, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Maintenance, modeList = { SystemPrivilegeMode.READ })
	public Response getMaintenanceList(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, NotifyNotFound, MaintenanceNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call getMaintenanceList()");
		
		List<MaintenanceInfo> infoResList = new MaintenanceControllerBean().getMaintenanceList();
		
		List<MaintenanceInfoResponse> dtoResList = new ArrayList<>();
		for (MaintenanceInfo infoRes : infoResList) {
			MaintenanceInfoResponse dtoRes = new MaintenanceInfoResponse();
			RestBeanUtil.convertBean(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);
		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * 履歴削除の種別を取得するAPI
	 */
	@GET
	@Path("/maintenanceType")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMaintenanceTypeList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MaintenanceTypeInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.MaintenanceType, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Maintenance, modeList = { SystemPrivilegeMode.READ })
	public Response getMaintenanceTypeList(@Context Request request, @Context UriInfo uriInfo)
			throws MaintenanceNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call getMaintenanceTypeList()");
		
		List<MaintenanceTypeMst> infoResList = new MaintenanceControllerBean().getMaintenanceTypeList();
		
		List<MaintenanceTypeInfoResponse> dtoResList = new ArrayList<>();
		for (MaintenanceTypeMst infoRes : infoResList) {
			MaintenanceTypeInfoResponse dtoRes = new MaintenanceTypeInfoResponse();
			RestBeanUtil.convertBean(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		
		RestLanguageConverter.convertMessages(dtoResList);
		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 指定したメンテナンスIDの履歴削除スケジュールを有効/無効化するAPI
	 */
	@PUT
	@Path("/maintenance_valid")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SetMaintenanceStatus")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MaintenanceInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.Maintenance, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Maintenance, modeList = { SystemPrivilegeMode.MODIFY, SystemPrivilegeMode.READ })
	public Response setMaintenanceStatus(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "setMaintenanceStatusRequestBody", content = @Content(schema = @Schema(implementation = SetMaintenanceStatusRequest.class))) String requestBody)
			throws NotifyNotFound, MaintenanceNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting  {
		m_log.info("call setMaintenanceStatus()");
		
		SetMaintenanceStatusRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, SetMaintenanceStatusRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);

		List<MaintenanceInfoResponse> dtoResList = new ArrayList<>();
		for (String maintenanceId : dtoReq.getMaintenanceIdList()) {
			MaintenanceInfoResponse dtoRes = new MaintenanceInfoResponse();
			MaintenanceInfo info = new MaintenanceControllerBean().setMaintenanceStatus(maintenanceId, dtoReq.getFlg());
			RestBeanUtil.convertBean(info, dtoRes);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);
		return Response.status(Status.OK).entity(dtoResList).build();
	}
}