/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.infra;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_404;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_409;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.activation.DataHandler;
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
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeFunction;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraFileBeingUsed;
import com.clustercontrol.fault.InfraFileNotFound;
import com.clustercontrol.fault.InfraFileTooLarge;
import com.clustercontrol.fault.InfraManagementDuplicate;
import com.clustercontrol.fault.InfraManagementInvalid;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InfraModuleNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NotifyDuplicate;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.SessionNotFound;
import com.clustercontrol.infra.bean.AccessInfo;
import com.clustercontrol.infra.bean.ModuleNodeResult;
import com.clustercontrol.infra.bean.ModuleResult;
import com.clustercontrol.infra.bean.ModuleTypeConstant;
import com.clustercontrol.infra.factory.AsyncModuleWorker.SessionInfo;
import com.clustercontrol.infra.model.CommandModuleInfo;
import com.clustercontrol.infra.model.FileTransferModuleInfo;
import com.clustercontrol.infra.model.InfraCheckResult;
import com.clustercontrol.infra.model.InfraFileInfo;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.infra.model.InfraModuleInfo;
import com.clustercontrol.infra.model.ReferManagementModuleInfo;
import com.clustercontrol.infra.session.InfraControllerBean;
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
import com.clustercontrol.rest.endpoint.infra.dto.AccessInfoRequest;
import com.clustercontrol.rest.endpoint.infra.dto.AddInfraFileRequest;
import com.clustercontrol.rest.endpoint.infra.dto.AddInfraManagementRequest;
import com.clustercontrol.rest.endpoint.infra.dto.CheckInfraModuleRequest;
import com.clustercontrol.rest.endpoint.infra.dto.CommandModuleInfoRequest;
import com.clustercontrol.rest.endpoint.infra.dto.CommandModuleInfoResponse;
import com.clustercontrol.rest.endpoint.infra.dto.CreateAccessInfoListForDialogResponse;
import com.clustercontrol.rest.endpoint.infra.dto.CreateSessionRequest;
import com.clustercontrol.rest.endpoint.infra.dto.FileTransferModuleInfoRequest;
import com.clustercontrol.rest.endpoint.infra.dto.FileTransferModuleInfoResponse;
import com.clustercontrol.rest.endpoint.infra.dto.InfraCheckResultResponse;
import com.clustercontrol.rest.endpoint.infra.dto.InfraFileInfoResponse;
import com.clustercontrol.rest.endpoint.infra.dto.InfraManagementInfoResponse;
import com.clustercontrol.rest.endpoint.infra.dto.InfraManagementInfoResponseP1;
import com.clustercontrol.rest.endpoint.infra.dto.InfraSessionResponse;
import com.clustercontrol.rest.endpoint.infra.dto.ModifyInfraFileRequest;
import com.clustercontrol.rest.endpoint.infra.dto.ModifyInfraManagementRequest;
import com.clustercontrol.rest.endpoint.infra.dto.ModuleNodeResultResponse;
import com.clustercontrol.rest.endpoint.infra.dto.ModuleResultResponse;
import com.clustercontrol.rest.endpoint.infra.dto.ModuleResultResponseP1;
import com.clustercontrol.rest.endpoint.infra.dto.ReferManagementModuleInfoRequest;
import com.clustercontrol.rest.endpoint.infra.dto.ReferManagementModuleInfoResponse;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestByteArrayConverter;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestDataSource;
import com.clustercontrol.rest.util.RestDownloadFile;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.rest.util.RestTempFileUtil;

@Path("/infra")
@RestLogFunc(name = LogFuncName.Infra)
public class InfraRestEndpoints {

	private static Log m_log = LogFactory.getLog(InfraRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "infra";

	/**
	 * 環境構築情報を作成します。
	 *
	 * InfraAdd 権限が必要
	 * 
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws InfraManagementDuplicate
	 * @throws InfraManagementNotFound
	 * @throws NotifyDuplicate
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	@POST
	@Path("/management")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddInfraManagement")
	@RestLog(action = LogAction.Add, target = LogTarget.Management, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.ADD })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InfraManagementInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addInfraManagement(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addInfraManagementBody", content = @Content(schema = @Schema(implementation = AddInfraManagementRequest.class))) String requestBody)
			throws InfraManagementDuplicate, InfraManagementNotFound, NotifyDuplicate, InvalidUserPass, InvalidRole,
			InvalidSetting, HinemosUnknown {
		m_log.info("call addInframanagement");

		AddInfraManagementRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddInfraManagementRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		InfraManagementInfo infoReq = new InfraManagementInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		convertInfraDtoToInfo(dtoReq, infoReq);

		InfraManagementInfo infoRes = new InfraControllerBean().addInfraManagement(infoReq);

		InfraManagementInfoResponse dtoRes = new InfraManagementInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		convertInfraInfoToDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);
		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 
	 * 環境構築情報を変更します。
	 * 
	 * InfraModify 権限が必要
	 * 
	 * @param managementId
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws NotifyDuplicate
	 * @throws NotifyNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 * @throws InfraManagementNotFound
	 * @throws InfraManagementDuplicate
	 */
	@PUT
	@Path("/management/{managementId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyInfraManagement")
	@RestLog(action = LogAction.Modify, target = LogTarget.Management, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InfraManagementInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyInfraManagement(@PathParam("managementId") String managementId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyInfraManagementBody", content = @Content(schema = @Schema(implementation = ModifyInfraManagementRequest.class))) String requestBody)
			throws NotifyDuplicate, NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting,
			InfraManagementNotFound, InfraManagementDuplicate {
		m_log.info("call modifyInfraManagement()");

		ModifyInfraManagementRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyInfraManagementRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		InfraManagementInfo infoReq = new InfraManagementInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		convertInfraDtoToInfo(dtoReq, infoReq);
		infoReq.setManagementId(managementId);

		InfraManagementInfo infoRes = new InfraControllerBean().modifyInfraManagement(infoReq);

		InfraManagementInfoResponse dtoRes = new InfraManagementInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		convertInfraInfoToDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 
	 * 環境構築情報を削除します。
	 * 
	 * InfraModify 権限が必要
	 * 
	 * @param managementIds
	 * @param request
	 * @param uriInfoList
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws InfraManagementNotFound
	 */
	@DELETE
	@Path("/management")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteInfraManagement")
	@RestLog(action = LogAction.Delete, target = LogTarget.Management, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InfraManagementInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteInfraManagement(@ArrayTypeParam @QueryParam(value = "managementIds") String managementIds,
			@Context Request request, @Context UriInfo uriInfoList)
			throws HinemosUnknown, InvalidUserPass, InvalidSetting, InvalidRole, InfraManagementNotFound {
		m_log.info("call deleteInfraManagement()");

		String[] managementId = new String[0];
		if(managementIds != null && !managementIds.isEmpty()) {
			managementId = managementIds.split(",");
		}
		
		List<InfraManagementInfo> infoResList = new InfraControllerBean().deleteInfraManagement(managementId);

		List<InfraManagementInfoResponse> dtoResList = new ArrayList<>();
		for (InfraManagementInfo info : infoResList) {
			InfraManagementInfoResponse dto = new InfraManagementInfoResponse();
			RestBeanUtil.convertBean(info, dto);
			convertInfraInfoToDto(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 
	 * 環境構築情報を取得します。
	 * 
	 * InfraRead 権限が必要
	 * 
	 * @param managementId
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InfraManagementNotFound
	 * @throws InvalidSetting
	 */
	@GET
	@Path("/management/{managementId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetInfraManagement")
	@RestLog(action = LogAction.Get, target = LogTarget.Management, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InfraManagementInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInfraManagement(@PathParam(value = "managementId") String managementId, @Context Request request,
			@Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InfraManagementNotFound, InvalidSetting {
		m_log.info("call getInfraManagement()");

		InfraManagementInfo infoRes = new InfraControllerBean().getInfraManagement(managementId);
		InfraManagementInfoResponse dtoRes = new InfraManagementInfoResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		convertInfraInfoToDto(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 環境構築情報の一覧を取得します。
	 * 
	 * InfraRead 権限が必要
	 * 
	 * @param ownerRoleId
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws NotifyNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@GET
	@Path("/management")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetInfraManagementList")
	@RestLog(action = LogAction.Get, target = LogTarget.Management, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InfraManagementInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInfraManagementList(@QueryParam(value = "ownerRoleId") String ownerRoleId,
			@Context Request request, @Context UriInfo uriInfo)
			throws NotifyNotFound, HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call getInfraManagementList()");

		List<InfraManagementInfo> infoResList = null;
		if (ownerRoleId != null) {
			infoResList = new InfraControllerBean().getInfraManagementListByOwnerRole(ownerRoleId);
		} else {
			infoResList = new InfraControllerBean().getInfraManagementList();
		}
		List<InfraManagementInfoResponse> dtoResList = new ArrayList<>();
		for (InfraManagementInfo infoRes : infoResList) {
			InfraManagementInfoResponse dtoRes = new InfraManagementInfoResponse();
			RestBeanUtil.convertBean(infoRes, dtoRes);
			convertInfraInfoToDto(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 
	 * 指定のオーナーロールから参照可能な参照環境構築モジュールの一覧を取得します。
	 * 
	 * InfraRead 権限が必要
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
	@Path("/management_refer/{ownerRoleId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetReferManagementList")
	@RestLog(action = LogAction.Get, target = LogTarget.Management, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InfraManagementInfoResponseP1.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getReferManagementList(@PathParam(value = "ownerRoleId") String ownerRoleId,
			@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call getReferManagementIdList()");

		List<InfraManagementInfo> infoResList = new InfraControllerBean().getReferManagementList(ownerRoleId);
		List<InfraManagementInfoResponseP1> dtoResList = new ArrayList<>();
		for (InfraManagementInfo infoRes : infoResList) {
			InfraManagementInfoResponseP1 dtoRes = new InfraManagementInfoResponseP1();
			RestBeanUtil.convertBean(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 
	 * 環境構築モジュールの実行/チェックのためのセッションを生成します。
	 * 
	 * InfraExec 権限が必要
	 * 
	 * @param request
	 * @param uriInfo
	 * @param requestBody
	 * @return
	 * @throws InfraManagementNotFound
	 * @throws InfraModuleNotFound
	 * @throws FacilityNotFound
	 * @throws InfraManagementInvalid
	 * @throws InvalidSetting
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	@POST
	@Path("/session")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "CreateSession")
	@RestLog(action = LogAction.Add, target = LogTarget.Session, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.EXEC })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InfraSessionResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response createSession(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "createSessionBody", content = @Content(schema = @Schema(implementation = CreateSessionRequest.class))) String requestBody)
			throws InfraManagementNotFound, InfraModuleNotFound, FacilityNotFound, InfraManagementInvalid,
			InvalidSetting, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call createSession()");

		CreateSessionRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				CreateSessionRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		String managementId = dtoReq.getManagementId();
		List<String> moduleList = dtoReq.getModuleIdList();
		Integer nodeInputType = dtoReq.getNodeInputType().getCode();
		List<AccessInfo> accessInfoList = null;
		if(dtoReq.getAccessList() != null) {
			accessInfoList = new ArrayList<>();
			for (AccessInfoRequest accessDto : dtoReq.getAccessList()) {
				AccessInfo accessInfo = new AccessInfo();
				RestBeanUtil.convertBean(accessDto, accessInfo);
				accessInfoList.add(accessInfo);
			}
		}

		SessionInfo dtoInfo = new InfraControllerBean().createSession(managementId, moduleList, nodeInputType,
				accessInfoList);

		InfraSessionResponse dtoRes = new InfraSessionResponse();
		dtoRes.setSessionId(dtoInfo.getSessionId());
		dtoRes.setManagementId(dtoInfo.getManagementId());
		dtoRes.setModuleList(dtoInfo.getModuleList());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 
	 * 環境構築モジュールの実行/チェックのために生成したセッションを削除します。
	 * 
	 * InfraExec 権限が必要
	 * 
	 * @param sessionId
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws SessionNotFound
	 * @throws InfraManagementNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	@DELETE
	@Path("/session/{sessionId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteSession")
	@RestLog(action = LogAction.Delete, target = LogTarget.Session, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.EXEC })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InfraSessionResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response deleteSession(@PathParam(value = "sessionId") String sessionId, @Context Request request,
			@Context UriInfo uriInfo)
			throws SessionNotFound, InfraManagementNotFound, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call deleteSession()");

		SessionInfo dtoInfo = new InfraControllerBean().deleteSession(sessionId);

		InfraSessionResponse dtoRes = new InfraSessionResponse();
		dtoRes.setSessionId(dtoInfo.getSessionId());
		dtoRes.setManagementId(dtoInfo.getManagementId());
		dtoRes.setModuleList(dtoInfo.getModuleList());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * アクセス情報を作成します。
	 * 
	 * InfraExec 権限が必要
	 * 
	 * @param managementId
	 * @param moduleIds
	 * @return
	 * @throws InfraManagementNotFound
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 */
	@GET
	@Path("/accessInfo/{managementId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "CreateAccessInfoListForDialog")
	@RestLog(action = LogAction.Get, target = LogTarget.AccessInfo, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.EXEC })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CreateAccessInfoListForDialogResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response createAccessInfoListForDialog(@PathParam(value = "managementId") String managementId,
			@ArrayTypeParam @QueryParam(value = "moduleIds") String moduleIds)
			throws InfraManagementNotFound, InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		m_log.info("call createAccessInfoListForDialog()");

		List<String> moduleIdList = new ArrayList<>();
		if(moduleIds != null && !moduleIds.isEmpty()) {
			moduleIdList = Arrays.asList(moduleIds.split(","));
		}

		List<AccessInfo> infoResList = new InfraControllerBean().createAccessInfoListForDialog(managementId,
				moduleIdList);
		List<CreateAccessInfoListForDialogResponse> dtoResList = new ArrayList<>();

		for (AccessInfo infoRes : infoResList) {
			CreateAccessInfoListForDialogResponse dtoRes = new CreateAccessInfoListForDialogResponse();
			RestBeanUtil.convertBean(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 
	 * 環境構築モジュールを実行します。
	 * 
	 * InfraRead権限が必要
	 * 
	 * @param sessionId
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InfraManagementNotFound
	 * @throws InfraModuleNotFound
	 * @throws SessionNotFound
	 * @throws InvalidSetting
	 */
	@POST
	@Path("/session_exec/{sessionId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "RunInfraModule")
	@RestLog(action = LogAction.Exec, target = LogTarget.Session, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ModuleResultResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response runInfraModule(@PathParam(value = "sessionId") String sessionId, @Context Request request,
			@Context UriInfo uriInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole, InfraManagementNotFound,
			InfraModuleNotFound, SessionNotFound, InvalidSetting {
		m_log.info("call runInfraModule()");

		ModuleResult infoRes = new InfraControllerBean().runInfraModule(sessionId);

		ModuleResultResponseP1 dtoRes = new ModuleResultResponseP1();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 
	 * 環境構築モジュールをチェックします。
	 * 
	 * InfraModify権限が必要
	 * 
	 * @param sessionId
	 * @param verbose
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InfraManagementNotFound
	 * @throws InfraModuleNotFound
	 * @throws SessionNotFound
	 * @throws InvalidSetting
	 */
	@POST
	@Path("/session_checkExec/{sessionId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "CheckInfraModule")
	@RestLog(action = LogAction.CheckExec, target = LogTarget.Session, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.EXEC })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ModuleResultResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response checkInfraModule(@PathParam(value = "sessionId") String sessionId,
			@RequestBody(description = "checkInfraModuleBody", content = @Content(schema = @Schema(implementation = CheckInfraModuleRequest.class))) String requestBody,
			@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InfraManagementNotFound, InfraModuleNotFound,
			SessionNotFound, InvalidSetting {
		m_log.info("call checkInfraModule()");

		CheckInfraModuleRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				CheckInfraModuleRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		ModuleResult infoRes = new InfraControllerBean().checkInfraModule(sessionId, dtoReq.getVerbose());

		ModuleResultResponse dtoRes = new ModuleResultResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		convertFileContent(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 
	 * 環境構築モジュールのチェック結果を取得します。
	 * 
	 * InfraRead 権限が必要
	 * 
	 * @param managementId
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 */
	@GET
	@Path("/checkResult/{managementId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetCheckResultList")
	@RestLog(action = LogAction.Get, target = LogTarget.CheckResult, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InfraCheckResultResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCheckResultList(@PathParam("managementId") String managementId)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting, InfraManagementNotFound {
		m_log.info("call getCheckResultList()");

		List<InfraCheckResult> infoResList = new InfraControllerBean().getCheckResultList(managementId);
		List<InfraCheckResultResponse> dtoResList = new ArrayList<>();

		for (InfraCheckResult infoRes : infoResList) {
			InfraCheckResultResponse dtoRes = new InfraCheckResultResponse();
			RestBeanUtil.convertBean(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 環境構築ファイルを作成します。
	 * 
	 * InfraAdd 権限が必要
	 * 
	 * @param inputStream
	 * @param requestBody
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws InvalidSetting
	 * @throws InfraFileTooLarge
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InfraManagementDuplicate
	 * @throws HinemosUnknown
	 */
	@POST
	@Path("/file")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddInfraFile")
	@RestLog(action = LogAction.Add, target = LogTarget.File, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.ADD })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InfraFileInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response addInfraFile(
			@RequestBody(description = "addInfraFileBody", content = @Content(schema = @Schema(type = SchemaType.OBJECT, implementation = AddInfraFileRequest.class))) @FormDataParam("file") InputStream inputStream,
			@FormDataParam("infraFileInfo") String requestBody, @Context Request request, @Context UriInfo uriInfo)
			throws InvalidSetting, InfraFileTooLarge, InvalidUserPass, InvalidRole, InfraManagementDuplicate,
			HinemosUnknown {
		m_log.info("call addInfraFile()");

		AddInfraFileRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddInfraFileRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		InfraFileInfo infoReq = new InfraFileInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setOwnerRoleId(dtoReq.getOwnerRoleId());

		InfraFileInfo infoRes = new InfraControllerBean().addInfraFile(infoReq,
				new DataHandler(new RestDataSource(inputStream, dtoReq.getFileName())));
		InfraFileInfoResponse dtoRes = new InfraFileInfoResponse();

		RestBeanUtil.convertBean(infoRes, dtoRes);
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 
	 * 環境構築ファイルを変更します。
	 * 
	 * InfraModify 権限が必要
	 * 
	 * @param fileId
	 * @param inputStream
	 * @param requestBody
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InfraFileTooLarge
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 */
	@PUT
	@Path("/file/{fileId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyInfraFile")
	@RestLog(action = LogAction.Modify, target = LogTarget.File, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InfraFileInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response modifyInfraFile(@PathParam("fileId") String fileId,
			@RequestBody(description = "modifyInfraFileBody", content = @Content(schema = @Schema(type = SchemaType.OBJECT, implementation = ModifyInfraFileRequest.class))) @FormDataParam("file") InputStream inputStream,
			@FormDataParam("infraFileInfo") String requestBody, @Context Request request, @Context UriInfo uriInfo)
			throws InvalidRole, HinemosUnknown, InfraFileTooLarge, InvalidUserPass, InvalidSetting {
		m_log.info("call modifyInfraFile()");

		ModifyInfraFileRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifyInfraFileRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		InfraFileInfo infoReq = new InfraFileInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setFileId(fileId);

		InfraFileInfo infoRes = new InfraControllerBean().modifyInfraFile(infoReq,
				new DataHandler(new RestDataSource(inputStream, dtoReq.getFileName())));
		InfraFileInfoResponse dtoRes = new InfraFileInfoResponse();

		RestBeanUtil.convertBean(infoRes, dtoRes);
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * 
	 * 環境構築ファイルをダウンロードします。
	 * 
	 * InfraAdd権限が必要
	 * 
	 * @param fileId
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws InvalidSetting
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InfraFileNotFound
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/file_download/{fileId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DownloadInfraFile")
	@RestLog(action = LogAction.Download, target = LogTarget.File, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.ADD })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	public Response downloadInfraFile(@PathParam(value = "fileId") String fileId, @Context Request request,
			@Context UriInfo uriInfo)
			throws InvalidSetting, InvalidUserPass, InvalidRole, InfraFileNotFound, HinemosUnknown {
		m_log.info("call downloadInfraFile()");

		RestDownloadFile restDownloadFile = new InfraControllerBean().downloadInfraFile(fileId);
		StreamingOutput stream = RestTempFileUtil.getTempFileStream(restDownloadFile.getTempFile());

		return Response.status(Status.OK).entity(stream)
				.header("Content-Disposition", "filename=\"" + restDownloadFile.getFileName() + "\"").build();
	}

	/**
	 * 
	 * Windows向けファイル配布モジュールで Windows環境へファイルを転送します。
	 * 
	 * @param fileId
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws InvalidSetting
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws InfraFileNotFound
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/file_downloadForWindows/{fileId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DownloadTransferFile")
	@RestLog(action = LogAction.Download, target = LogTarget.File, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	@IgnoreCommandline
	@IgnoreReference
	public Response downloadTransferFile(@PathParam(value = "fileId") String fileId, @Context Request request,
			@Context UriInfo uriInfo)
			throws InvalidSetting, InvalidUserPass, InvalidRole, InfraFileNotFound, HinemosUnknown {
		m_log.info("call downloadTransferFile()");

		RestDownloadFile restDownloadFile = new InfraControllerBean().downloadTransferFile(fileId);
		StreamingOutput stream = RestTempFileUtil.getTempFileStream(restDownloadFile.getTempFile());

		return Response.status(Status.OK).entity(stream)
				.header("Content-Disposition", "filename=\"" + restDownloadFile.getFileName() + "\"").build();
	}

	
	/**
	 * 環境構築ファイルを削除します。
	 * 
	 * InfraModify 権限が必要
	 * 
	 * @param fileIds
	 * @param request
	 * @param uriInfoList
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InfraFileNotFound
	 * @throws InfraFileBeingUsed
	 * @throws InfraManagementNotFound
	 * @throws InvalidSetting
	 */
	@DELETE
	@Path("/file")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteInfraFileList")
	@RestLog(action = LogAction.Delete, target = LogTarget.File, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InfraFileInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteInfraFileList(@ArrayTypeParam @QueryParam(value = "fileIds") String fileIds,
			@Context Request request, @Context UriInfo uriInfoList) throws InvalidUserPass, InvalidRole, HinemosUnknown,
			InfraFileNotFound, InfraFileBeingUsed, InfraManagementNotFound, InvalidSetting {
		m_log.info("call deleteInfraFileList()");

		List<String> fileIdList = new ArrayList<>();
		if(fileIds != null && !fileIds.isEmpty()) {
			fileIdList = Arrays.asList(fileIds.split(","));
		}
		
		List<InfraFileInfo> infoResList = new InfraControllerBean().deleteInfraFileList(fileIdList);

		List<InfraFileInfoResponse> dtoResList = new ArrayList<>();
		for (InfraFileInfo info : infoResList) {
			InfraFileInfoResponse dto = new InfraFileInfoResponse();
			RestBeanUtil.convertBean(info, dto);
			dtoResList.add(dto);
		}
		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 環境構築ファイル情報の一覧を取得します。
	 * 
	 * InfraRead 権限が必要
	 * 
	 * @param ownerRoleId
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 */
	@GET
	@Path("/file")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetInfraFileList")
	@RestLog(action = LogAction.Get, target = LogTarget.File, type = LogType.REFERENCE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = InfraFileInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInfraFileList(@QueryParam(value = "ownerRoleId") String ownerRoleId, @Context Request request,
			@Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		m_log.info("call getInfraFileList()");

		List<InfraFileInfo> infoResList = null;
		if (ownerRoleId != null) {
			infoResList = new InfraControllerBean().getInfraFileListByOwnerRoleId(ownerRoleId);
		} else {
			infoResList = new InfraControllerBean().getInfraFileList();
		}

		List<InfraFileInfoResponse> dtoResList = new ArrayList<>();
		for (InfraFileInfo info : infoResList) {
			InfraFileInfoResponse dto = new InfraFileInfoResponse();
			RestBeanUtil.convertBean(info, dto);
			dtoResList.add(dto);
		}
		return Response.status(Status.OK).entity(dtoResList).build();
	}

	private static void convertFileContent(ModuleResult info, ModuleResultResponse dto) throws HinemosUnknown {
		if (info.getModuleType() != ModuleTypeConstant.TYPE_FILETRANSFER) {
			return;
		}

		List<ModuleNodeResult> infoList = info.getModuleNodeResultList();
		List<ModuleNodeResultResponse> dtoList = dto.getNodeResultList();
		for (int i = 0; i < infoList.size(); i++) {
			ModuleNodeResult infoE = infoList.get(i);
			ModuleNodeResultResponse dtoE = dtoList.get(i);
			if ( infoE.getNewFile() != null) {
				String newFileStr = Base64.getEncoder().encodeToString(convertDataHandlerToByteArray(infoE.getNewFile()));
				dtoE.setNewFile(newFileStr);
			}
			if ( infoE.getOldFile() != null) {
				String oldFileStr = Base64.getEncoder().encodeToString(convertDataHandlerToByteArray(infoE.getOldFile()));
				dtoE.setOldFile(oldFileStr);
			}
		}

	}

	private static byte[] convertDataHandlerToByteArray(DataHandler dh) throws HinemosUnknown {
		try (InputStream is = dh.getInputStream()) {
			return RestByteArrayConverter.convertInputStreamToByteArray(is, 1024 * 1024);
		} catch (IOException | InvalidSetting e) {
			m_log.warn("failed convert datahandler to bytearry.");
			throw new HinemosUnknown(e);
		}
	}

	/**
	 * InfraRestEndpoint で個別に変換が必要な箇所への対応 DTO => INFO への変換時に利用する
	 * 
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * 
	 */
	public static void convertInfraDtoToInfo(Object src, InfraManagementInfo dest)
			throws HinemosUnknown {

		if (src instanceof AddInfraManagementRequest) {
			AddInfraManagementRequest dtoReq = (AddInfraManagementRequest) src;
			List<InfraModuleInfo<?>> moduleInfoList = new ArrayList<>();
			for (CommandModuleInfoRequest cmdModuleDto : dtoReq.getCommandModuleInfoList()) {
				InfraModuleInfo<?> cmdModuleInfo = new CommandModuleInfo();
				RestBeanUtil.convertBeanNoInvalid(cmdModuleDto, cmdModuleInfo);
				moduleInfoList.add(cmdModuleInfo);
			}

			for (FileTransferModuleInfoRequest fileModuleDto : dtoReq.getFileTransferModuleInfoList()) {
				InfraModuleInfo<?> fileModuleInfo = new FileTransferModuleInfo();
				RestBeanUtil.convertBeanNoInvalid(fileModuleDto, fileModuleInfo);
				moduleInfoList.add(fileModuleInfo);
			}

			for (ReferManagementModuleInfoRequest referModuleDto : dtoReq.getReferManagementModuleInfoList()) {
				InfraModuleInfo<?> referModuleInfo = new ReferManagementModuleInfo();
				RestBeanUtil.convertBeanNoInvalid(referModuleDto, referModuleInfo);
				moduleInfoList.add(referModuleInfo);
			}
			Collections.sort(moduleInfoList, new Comparator<InfraModuleInfo<?>>() {
				@Override
				public int compare(InfraModuleInfo<?> o1, InfraModuleInfo<?> o2) {
					return o1.getOrderNo().compareTo(o2.getOrderNo());
				}
			});
			dest.setModuleList(moduleInfoList);
			return;
		}

		if (src instanceof ModifyInfraManagementRequest) {
			ModifyInfraManagementRequest dtoReq = (ModifyInfraManagementRequest) src;
			List<InfraModuleInfo<?>> moduleInfoList = new ArrayList<>();
			for (CommandModuleInfoRequest cmdModuleDto : dtoReq.getCommandModuleInfoList()) {
				InfraModuleInfo<?> cmdModuleInfo = new CommandModuleInfo();
				RestBeanUtil.convertBeanNoInvalid(cmdModuleDto, cmdModuleInfo);
				moduleInfoList.add(cmdModuleInfo);
			}

			for (FileTransferModuleInfoRequest fileModuleDto : dtoReq.getFileTransferModuleInfoList()) {
				InfraModuleInfo<?> fileModuleInfo = new FileTransferModuleInfo();
				RestBeanUtil.convertBeanNoInvalid(fileModuleDto, fileModuleInfo);
				moduleInfoList.add(fileModuleInfo);
			}

			for (ReferManagementModuleInfoRequest referModuleDto : dtoReq.getReferManagementModuleInfoList()) {
				InfraModuleInfo<?> referModuleInfo = new ReferManagementModuleInfo();
				RestBeanUtil.convertBeanNoInvalid(referModuleDto, referModuleInfo);
				moduleInfoList.add(referModuleInfo);
			}
			Collections.sort(moduleInfoList, new Comparator<InfraModuleInfo<?>>() {
				@Override
				public int compare(InfraModuleInfo<?> o1, InfraModuleInfo<?> o2) {
					return o1.getOrderNo().compareTo(o2.getOrderNo());
				}
			});
			dest.setModuleList(moduleInfoList);
			return;
		}
	}

	/**
	 * InfraRestEndpoint で個別に変換が必要な箇所への対応 INFO => DTO への変換時に利用する
	 * 
	 * @throws HinemosUnknown
	 * @throws InvalidSetting
	 * 
	 */
	private static void convertInfraInfoToDto(InfraManagementInfo src, InfraManagementInfoResponse dest)
			throws HinemosUnknown {
		// スコープの設定(InfraManagementInfo#getScopeが自動で呼ばれないので個別に対応する)
		dest.setScope(src.getScope());

		// 環境構築モジュールの設定
		List<InfraModuleInfo<?>> moduleInfoList = src.getModuleList();
		if (moduleInfoList != null && !moduleInfoList.isEmpty()) {
			List<CommandModuleInfoResponse> commandModuleInfoResponseList = new ArrayList<>();
			List<FileTransferModuleInfoResponse> fileTransferModuleInfoResponseList = new ArrayList<>();
			List<ReferManagementModuleInfoResponse> referManagementModuleInfoResponsetList = new ArrayList<>();
			for (InfraModuleInfo<?> moduleInfo : moduleInfoList) {
				if (moduleInfo instanceof CommandModuleInfo) {
					CommandModuleInfo cmdInfo = (CommandModuleInfo) moduleInfo;
					CommandModuleInfoResponse cmdDto = new CommandModuleInfoResponse();
					RestBeanUtil.convertBeanNoInvalid(cmdInfo, cmdDto);
					commandModuleInfoResponseList.add(cmdDto);
				} else if (moduleInfo instanceof FileTransferModuleInfo) {
					FileTransferModuleInfo ftInfo = (FileTransferModuleInfo) moduleInfo;
					FileTransferModuleInfoResponse ftDto = new FileTransferModuleInfoResponse();
					RestBeanUtil.convertBeanNoInvalid(ftInfo, ftDto);
					fileTransferModuleInfoResponseList.add(ftDto);
				} else if (moduleInfo instanceof ReferManagementModuleInfo) {
					ReferManagementModuleInfo refInfo = (ReferManagementModuleInfo) moduleInfo;
					ReferManagementModuleInfoResponse refDto = new ReferManagementModuleInfoResponse();
					RestBeanUtil.convertBeanNoInvalid(refInfo, refDto);
					referManagementModuleInfoResponsetList.add(refDto);
				}
			}
			
			dest.setCommandModuleInfoList(commandModuleInfoResponseList);
			dest.setFileTransferModuleInfoList(fileTransferModuleInfoResponseList);
			dest.setReferManagementModuleInfoList(referManagementModuleInfoResponsetList);
		}
	}
}
