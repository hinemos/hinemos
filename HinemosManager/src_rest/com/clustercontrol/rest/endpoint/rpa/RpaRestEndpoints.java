/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.rpa;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_404;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_409;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RpaManagementToolAccountDuplicate;
import com.clustercontrol.fault.RpaManagementToolAccountNotFound;
import com.clustercontrol.fault.RpaScenarioDuplicate;
import com.clustercontrol.fault.RpaScenarioNotFound;
import com.clustercontrol.fault.RpaScenarioOperationResultCreateSettingDuplicate;
import com.clustercontrol.fault.RpaScenarioOperationResultCreateSettingNotFound;
import com.clustercontrol.fault.RpaScenarioOperationResultNotFound;
import com.clustercontrol.fault.RpaScenarioTagDuplicate;
import com.clustercontrol.fault.RpaScenarioTagNotFound;
import com.clustercontrol.fault.UsedFacility;
import com.clustercontrol.rest.annotation.RestLog;
import com.clustercontrol.rest.annotation.RestLog.LogAction;
import com.clustercontrol.rest.annotation.RestLog.LogTarget;
import com.clustercontrol.rest.annotation.RestLog.LogType;
import com.clustercontrol.rest.annotation.RestLogFunc;
import com.clustercontrol.rest.annotation.RestLogFunc.LogFuncName;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.ArrayTypeParam;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoRequest;
import com.clustercontrol.rest.endpoint.rpa.dto.AddRpaManagementToolAccountRequest;
import com.clustercontrol.rest.endpoint.rpa.dto.AddRpaScenarioOperationResultCreateSettingRequest;
import com.clustercontrol.rest.endpoint.rpa.dto.AddRpaScenarioRequest;
import com.clustercontrol.rest.endpoint.rpa.dto.AddRpaScenarioTagRequest;
import com.clustercontrol.rest.endpoint.rpa.dto.CorrectExecNodeRequest;
import com.clustercontrol.rest.endpoint.rpa.dto.DownloadRpaScenarioOperationResultRecordsRequest;
import com.clustercontrol.rest.endpoint.rpa.dto.GetRpaScenarioCorrectExecNodeResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.GetRpaScenarioListRequest;
import com.clustercontrol.rest.endpoint.rpa.dto.GetRpaScenarioListResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.GetRpaScenarioOperationResultSummaryForBarResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.GetRpaScenarioOperationResultSummaryForPieResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.GetRpaScenarioResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.ModifyRpaManagementToolAccountRequest;
import com.clustercontrol.rest.endpoint.rpa.dto.ModifyRpaScenarioOperationResultCreateSettingRequest;
import com.clustercontrol.rest.endpoint.rpa.dto.ModifyRpaScenarioRequest;
import com.clustercontrol.rest.endpoint.rpa.dto.ModifyRpaScenarioTagRequest;
import com.clustercontrol.rest.endpoint.rpa.dto.RpaManagementToolAccountResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.RpaManagementToolEndStatusResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.RpaManagementToolResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.RpaManagementToolRunParamResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.RpaManagementToolRunTypeResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.RpaManagementToolStopModeResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.RpaScenarioCoefficientPatternResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.RpaScenarioOperationResultCreateSettingResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.RpaScenarioOperationResultWithDetailResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.RpaScenarioResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.RpaScenarioTagResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.RpaToolEnvResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.RpaToolResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.RpaToolRunCommandResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.SearchRpaScenarioOperationResultRequest;
import com.clustercontrol.rest.endpoint.rpa.dto.SearchRpaScenarioOperationResultResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.SetRpaScenarioOperationResultCreateSettingValidRequest;
import com.clustercontrol.rest.endpoint.rpa.dto.SummaryTypeEnum;
import com.clustercontrol.rest.endpoint.rpa.dto.UpdateRpaScenarioOperationResultRequest;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestDownloadFile;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.rest.util.RestTempFileUtil;
import com.clustercontrol.rpa.model.RpaManagementToolAccount;
import com.clustercontrol.rpa.model.RpaManagementToolEndStatusMst;
import com.clustercontrol.rpa.model.RpaManagementToolMst;
import com.clustercontrol.rpa.model.RpaManagementToolRunParamMst;
import com.clustercontrol.rpa.model.RpaManagementToolRunTypeMst;
import com.clustercontrol.rpa.model.RpaManagementToolStopModeMst;
import com.clustercontrol.rpa.model.RpaScenarioCoefficientPattern;
import com.clustercontrol.rpa.model.RpaToolEnvMst;
import com.clustercontrol.rpa.model.RpaToolMst;
import com.clustercontrol.rpa.model.RpaToolRunCommandMst;
import com.clustercontrol.rpa.scenario.bean.RpaScenarioFilterInfo;
import com.clustercontrol.rpa.scenario.bean.RpaScenarioOperationResultFilterInfo;
import com.clustercontrol.rpa.scenario.model.RpaScenario;
import com.clustercontrol.rpa.scenario.model.RpaScenarioExecNode;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResultCreateSetting;
import com.clustercontrol.rpa.scenario.model.RpaScenarioTag;
import com.clustercontrol.rpa.scenario.model.UpdateRpaScenarioOperationResultInfo;
import com.clustercontrol.rpa.session.RpaControllerBean;

/**
 * RPAシナリオ用のWebAPIエンドポイント
 */
@Path("/rpa")
@RestLogFunc(name = LogFuncName.Rpa)
public class RpaRestEndpoints {
	private static Log m_log = LogFactory.getLog(RpaRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "rpa";

	/**
	 * RPA管理ツールアカウントを取得する。 
	 */
	@GET
	@Path("/managementToolAccount/{rpaScopeId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaManagementToolAccount")
	@RestLog(action = LogAction.Get, target = LogTarget.RpaManagementTool, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaManagementToolAccountResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRpaManagementToolAccount(@Context Request request, @Context UriInfo uriInfo,
			@PathParam(value = "rpaScopeId") String rpaScopeId) throws InvalidRole, HinemosUnknown, RpaManagementToolAccountNotFound {
		m_log.info("call getRpaManagementAccount()");
		RpaManagementToolAccount infoRes = new RpaControllerBean().getRpaAccount(rpaScopeId);
		RpaManagementToolAccountResponse dtoRes = new RpaManagementToolAccountResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * RPA管理ツールアカウント一覧を取得する。
	 */
	@GET
	@Path("/managementToolAccount")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaManagementToolAccountList")
	@RestLog(action = LogAction.Get, target = LogTarget.RpaManagementTool, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaManagementToolAccountResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRpaManagementToolAccountList(@Context Request request, @Context UriInfo uriInfo) throws InvalidRole, HinemosUnknown {
		m_log.info("call getRpaManagementAccountList()");
		List<RpaManagementToolAccount> infoResList = new RpaControllerBean().getRpaAccountList();
		List<RpaManagementToolAccountResponse> dtoResList = new ArrayList<>();
		for (RpaManagementToolAccount infoRes: infoResList) {
			RpaManagementToolAccountResponse dtoRes = new RpaManagementToolAccountResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}

		RestLanguageConverter.convertMessages(dtoResList);
		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * RPA管理ツールアカウントをマネージャに登録する。
	 * 
	 */
	@POST
	@Path("/managementToolAccount")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddRpaManagementToolAccount")
	@RestLog(action = LogAction.Add, target = LogTarget.RpaManagementTool, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ,SystemPrivilegeMode.ADD })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaManagementToolAccountResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addrpaManagementToolAccount(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addRpaManagementAccountBody", content = @Content(schema = @Schema(implementation = AddRpaManagementToolAccountRequest.class))) String requestBody)
			throws HinemosUnknown, RpaManagementToolAccountDuplicate, InvalidRole, InvalidSetting {
		m_log.info("call addRpaManagementAccount()");

		AddRpaManagementToolAccountRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddRpaManagementToolAccountRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();
//		// URL整形
//		dtoReq.setUrl(stripURL(dtoReq.getUrl()));
//		dtoReq.setProxyUrl(stripURL(dtoReq.getProxyUrl()));
		
//		// アカウントの重複チェック
//		checkAccountDuplication(dtoReq.getRpaScopeId(), dtoReq.getAccountId(), dtoReq.getUrl(), dtoReq.getTenantName());
		
//		// アカウント認証チェック
//		checkAuthentication(dtoReq);

		RpaManagementToolAccount infoReq = new RpaManagementToolAccount();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		RpaManagementToolAccount infoRes = new RpaControllerBean().addRpaAccount(infoReq);

		RpaManagementToolAccountResponse dtoRes = new RpaManagementToolAccountResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * RPA管理ツールアカウントを変更する。
	 * 
	 */
	@PUT
	@Path("/managementToolAccount/{rpaScopeId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyRpaManagementToolAccount")
	@RestLog(action = LogAction.Modify, target = LogTarget.RpaManagementTool, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ,SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaManagementToolAccountResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyRpaManagementToolAccount(@PathParam("rpaScopeId") String rpaScopeId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyRpaManagementAccountBody", content = @Content(schema = @Schema(implementation = ModifyRpaManagementToolAccountRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidRole, InvalidSetting, RpaManagementToolAccountNotFound {
		m_log.info("call modifyRpaManagementAccount()");

		ModifyRpaManagementToolAccountRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyRpaManagementToolAccountRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck(rpaScopeId);
//		// URL整形
//		dtoReq.setUrl(stripURL(dtoReq.getUrl()));
//		dtoReq.setProxyUrl(stripURL(dtoReq.getProxyUrl()));
//
//		// アカウントの重複チェック
//		checkAccountDuplication(rpaScopeId, dtoReq.getAccountId(), dtoReq.getUrl(), dtoReq.getTenantName());
//
//		// アカウント認証チェック
//		checkAuthentication(dtoReq, rpaScopeId);

		RpaManagementToolAccount infoReq = new RpaManagementToolAccount();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setRpaScopeId(rpaScopeId);

		RpaManagementToolAccount infoRes = new RpaControllerBean().modifyRpaAccount(infoReq);

		RpaManagementToolAccountResponse dtoRes = new RpaManagementToolAccountResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * RPA管理ツールアカウントを削除する。
	 * 
	 */
	@DELETE
	@Path("/managementToolAccount")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteRpaManagementToolAccount")
	@RestLog(action = LogAction.Delete, target = LogTarget.RpaManagementTool, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ,SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaManagementToolAccountResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteRpaManagementToolAccount(@ArrayTypeParam @QueryParam(value = "rpaScopeIds") String rpaScopeIds,
			@Context Request request, @Context UriInfo uriInfo) throws HinemosUnknown, RpaManagementToolAccountNotFound, InvalidRole, UsedFacility {
		m_log.info("call deleteRpaManagementAccount()");

		List<String> rpaScopeIdList = Arrays.asList(rpaScopeIds.split(","));
		List<RpaManagementToolAccount> infoResList = new RpaControllerBean().deleteRpaAccount(rpaScopeIdList);
		List<RpaManagementToolAccountResponse> dtoResList = new ArrayList<>();
		for (RpaManagementToolAccount info : infoResList) {
			RpaManagementToolAccountResponse dto = new RpaManagementToolAccountResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * RPA管理ツールマスタ一覧を取得する。
	 */
	@GET
	@Path("/managementToolMaster")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaManagementTool")
	@RestLog(action = LogAction.Get, target = LogTarget.Master, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaManagementToolResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRpaManagementTool(@Context Request request, @Context UriInfo uriInfo) throws InvalidRole, HinemosUnknown {
		m_log.info("call getRpaManagementTool()");
		List<RpaManagementToolMst> infoResList = new RpaControllerBean().getRpaManagementToolMstList();
		List<RpaManagementToolResponse> dtoResList = new ArrayList<>();
		for (RpaManagementToolMst infoRes : infoResList){
			RpaManagementToolResponse dtoRes = new RpaManagementToolResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			// RPA管理ツール種別をセットする
			dtoRes.setRpaManagementToolType(infoRes.getRpaManagementToolType().getRpaManagementToolType());
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * RPA管理ツール実行種別マスタを取得する。
	 */
	@GET
	@Path("/managementToolRunTypeMaster/{rpaManagementToolId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaManagementToolRunType")
	@RestLog(action = LogAction.Get, target = LogTarget.Master, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaManagementToolRunTypeResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRpaManagementToolRunType(@PathParam("rpaManagementToolId") String rpaManagementToolId, @Context Request request, @Context UriInfo uriInfo) throws InvalidRole, HinemosUnknown {
		m_log.info("call getRpaManagementToolRunType()");
		List<RpaManagementToolRunTypeMst> infoResList = new RpaControllerBean().getRpaManagementToolRunTypeMstList(rpaManagementToolId);
		List<RpaManagementToolRunTypeResponse> dtoResList = new ArrayList<>();
		for (RpaManagementToolRunTypeMst infoRes : infoResList){
			RpaManagementToolRunTypeResponse dtoRes = new RpaManagementToolRunTypeResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoRes.setRpaManagementToolId(infoRes.getId().getRpaManagementToolId());
			dtoRes.setRunType(infoRes.getId().getRunType());
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * RPA管理ツール停止方法マスタを取得する。
	 */
	@GET
	@Path("/managementToolStopModeMaster/{rpaManagementToolId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaManagementToolStopMode")
	@RestLog(action = LogAction.Get, target = LogTarget.Master, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaManagementToolStopModeResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRpaManagementToolStopMode(@PathParam("rpaManagementToolId") String rpaManagementToolId, @Context Request request, @Context UriInfo uriInfo) throws InvalidRole, HinemosUnknown {
		m_log.info("call getRpaManagementToolStopMode()");
		List<RpaManagementToolStopModeMst> infoResList = new RpaControllerBean().getRpaManagementToolStopModeMstList(rpaManagementToolId);
		List<RpaManagementToolStopModeResponse> dtoResList = new ArrayList<>();
		for (RpaManagementToolStopModeMst infoRes : infoResList){
			RpaManagementToolStopModeResponse dtoRes = new RpaManagementToolStopModeResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoRes.setRpaManagementToolId(infoRes.getId().getRpaManagementToolId());
			dtoRes.setStopMode(infoRes.getId().getStopMode());
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * RPA管理ツール起動パラメータマスタを取得する。
	 */
	@GET
	@Path("/managementToolRunParamMaster/{rpaManagementToolId}/{runType}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaManagementToolRunParam")
	@RestLog(action = LogAction.Get, target = LogTarget.Master, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaManagementToolRunParamResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRpaManagementToolRunParam(@PathParam("rpaManagementToolId") String rpaManagementToolId, @PathParam("runType") Integer runType, @Context Request request, @Context UriInfo uriInfo) throws InvalidRole, HinemosUnknown {
		m_log.info("call getRpaManagementToolRunParam()");
		List<RpaManagementToolRunParamMst> infoResList = new RpaControllerBean().getRpaManagementToolRunParamMstList(rpaManagementToolId, runType);
		List<RpaManagementToolRunParamResponse> dtoResList = new ArrayList<>();
		for (RpaManagementToolRunParamMst infoRes : infoResList){
			RpaManagementToolRunParamResponse dtoRes = new RpaManagementToolRunParamResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * RPA管理ツール実行結果終了状態マスタを取得する。
	 */
	@GET
	@Path("/managementToolEndStatusMaster/{rpaManagementToolId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaManagementToolEndStatus")
	@RestLog(action = LogAction.Get, target = LogTarget.Master, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaManagementToolEndStatusResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRpaManagementToolEndStatus(@PathParam("rpaManagementToolId") String rpaManagementToolId, @Context Request request, @Context UriInfo uriInfo) throws InvalidRole, HinemosUnknown {
		m_log.info("call getRpaManagementToolRunParam()");
		List<RpaManagementToolEndStatusMst> infoResList = new RpaControllerBean().getRpaManagementToolEndStatusMstList(rpaManagementToolId);
		List<RpaManagementToolEndStatusResponse> dtoResList = new ArrayList<>();
		for (RpaManagementToolEndStatusMst infoRes : infoResList){
			RpaManagementToolEndStatusResponse dtoRes = new RpaManagementToolEndStatusResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * RPAシナリオ情報の登録
	 */
	@POST
	@Path("/scenario")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddRpaScenario")
	@RestLog(action = LogAction.Add, target = LogTarget.RpaScenario, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ,SystemPrivilegeMode.ADD })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaScenarioResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addRpaScenario(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addRpaScenarioBody", content = @Content(schema = @Schema(implementation = AddRpaScenarioRequest.class))) String requestBody)
			throws HinemosUnknown, RpaScenarioDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addRpaScenario()");

		AddRpaScenarioRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddRpaScenarioRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		RpaScenario infoReq = new RpaScenario();
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// 実行ノードは自動変換されないので別個に変換
		setExecNodeInfo(infoReq, dtoReq.getExecNodes());
		
		RpaScenario infoRes = new RpaControllerBean().addRpaScenario(infoReq);

		RpaScenarioResponse dtoRes = new RpaScenarioResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		// 実行ノードは自動変換されないので別個に変換
		setExecNodeDto(dtoRes, infoRes.getExecNodes());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * RPAシナリオ情報の変更
	 */
	@PUT
	@Path("/scenario/{scenarioId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyRpaScenario")
	@RestLog(action = LogAction.Modify, target = LogTarget.RpaScenario, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ,SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaScenarioResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyRpaScenario(@PathParam("scenarioId") String scenarioId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyRpaScenarioBody", content = @Content(schema = @Schema(implementation = ModifyRpaScenarioRequest.class))) String requestBody)
			throws HinemosUnknown, RpaScenarioNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call modifyRpaScenario()");

		ModifyRpaScenarioRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,ModifyRpaScenarioRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		RpaScenario infoReq = new RpaScenario();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		// 実行ノードは自動変換されないので別個に変換
		infoReq.setScenarioId(scenarioId);
		setExecNodeInfo(infoReq, dtoReq.getExecNodes());
		RpaScenario infoRes = new RpaControllerBean().modifyRpaScenario(infoReq);

		RpaScenarioResponse dtoRes = new RpaScenarioResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		// 実行ノードは自動変換されないので別個に変換
		setExecNodeDto(dtoRes, infoRes.getExecNodes());

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * RPAシナリオ情報の削除
	 */
	@DELETE
	@Path("/scenario")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteRpaScenario")
	@RestLog(action = LogAction.Delete, target = LogTarget.RpaScenario, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ,SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaScenarioResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteRpaScenario(@ArrayTypeParam @QueryParam(value = "scenarioIds") String scenarioIds,
			@Context Request request, @Context UriInfo uriInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call deleteRpaScenario()");

		List<String> scenarioIdList = Arrays.asList(scenarioIds.split(","));
		List<RpaScenario> infoResList = new RpaControllerBean().deleteRpaScenario(scenarioIdList);
		List<RpaScenarioResponse> dtoResList = new ArrayList<>();
		for (RpaScenario info : infoResList) {
			RpaScenarioResponse dto = new RpaScenarioResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			// 実行ノードは自動変換されないので別個に変換
			setExecNodeDto(dto, info.getExecNodes());
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * 条件に従いPRAシナリオ一覧の取得を行うAPI
	 */
	@POST
	@Path("/scenario_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaScenarioList")
	@RestLog(action=LogAction.Get, target=LogTarget.RpaScenario, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.Rpa, modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetRpaScenarioListResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getRpaScenarioList(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "getRpaScenarioListRequest", content = @Content(schema = @Schema(implementation = GetRpaScenarioListRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidSetting, InvalidRole, HinemosUnknown {
		m_log.info("call getRpaScenarioList()");

		GetRpaScenarioListRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,GetRpaScenarioListRequest.class);
		RpaScenarioFilterInfo infoReq = new RpaScenarioFilterInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		
		List<GetRpaScenarioListResponse> dtoResList = new RpaControllerBean().getRpaScenarioList(infoReq);

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * シナリオIDを基にRPAシナリオ情報の取得を行うAPI
	 */
	@GET
	@Path("/scenario/{scenarioId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaScenario")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetRpaScenarioResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.RpaScenario, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	public Response getRpaScenario(@Context Request request, @Context UriInfo uriInfo,
			@PathParam(value = "scenarioId") String scenarioId) throws HinemosUnknown, RpaScenarioNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("getRpaScenario");
		
		GetRpaScenarioResponse dtoRes = new RpaControllerBean().getRpaScenario(scenarioId);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * RPAツールマスタ一覧を取得する。
	 */
	@GET
	@Path("/toolMaster")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaTool")
	@RestLog(action = LogAction.Get, target = LogTarget.Master, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaToolResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRpaTool(@Context Request request, @Context UriInfo uriInfo) throws InvalidRole, HinemosUnknown {
		m_log.info("call getRpaTool()");
		List<RpaToolMst> infoResList = new RpaControllerBean().getRpaToolMstList();
		List<RpaToolResponse> dtoResList = new ArrayList<>();
		for (RpaToolMst infoRes : infoResList){
			RpaToolResponse dtoRes = new RpaToolResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 環境毎のRPAツール一覧を取得する。
	 */
	@GET
	@Path("/rpaToolEnv")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaToolEnv")
	@RestLog(action = LogAction.Get, target = LogTarget.Master, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaToolEnvResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRpaToolEnv(@Context Request request, @Context UriInfo uriInfo) throws InvalidRole, HinemosUnknown {
		m_log.info("call getRpaToolEnv()");
		List<RpaToolEnvMst> infoResList = new RpaControllerBean().getRpaToolEnvMstList();
		List<RpaToolEnvResponse> dtoResList = new ArrayList<>();
		for (RpaToolEnvMst infoRes : infoResList){
			RpaToolEnvResponse dtoRes = new RpaToolEnvResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * RPAツールマスタ一覧を取得する。
	 */
	@GET
	@Path("/rpaToolRunCommand")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaToolRunCommand")
	@RestLog(action = LogAction.Get, target = LogTarget.Master, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaToolRunCommandResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRpaToolRunCommand(@Context Request request, @Context UriInfo uriInfo) throws InvalidRole, HinemosUnknown {
		m_log.info("call getRpaToolRunCommand()");
		List<RpaToolRunCommandMst> infoResList = new RpaControllerBean().getRpaToolRunCommandMstList();
		List<RpaToolRunCommandResponse> dtoResList = new ArrayList<>();
		for (RpaToolRunCommandMst infoRes : infoResList){
			RpaToolRunCommandResponse dtoRes = new RpaToolRunCommandResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * RPAシナリオタグ情報の登録
	 */
	@POST
	@Path("/scenariotag")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddRpaScenarioTag")
	@RestLog(action = LogAction.Add, target = LogTarget.RpaScenarioTag, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ,SystemPrivilegeMode.ADD })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaScenarioTagResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addRpaScenarioTag(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addRpaScenarioTagBody", content = @Content(schema = @Schema(implementation = AddRpaScenarioTagRequest.class))) String requestBody)
			throws HinemosUnknown, RpaScenarioTagDuplicate, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call addRpaScenarioTag()");

		AddRpaScenarioTagRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddRpaScenarioTagRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		RpaScenarioTag infoReq = new RpaScenarioTag();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		RpaScenarioTag infoRes = new RpaControllerBean().addRpaScenarioTag(infoReq);

		RpaScenarioTagResponse dtoRes = new RpaScenarioTagResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * RPAシナリオタグ情報の変更
	 */
	@PUT
	@Path("/scenariotag/{scenarioTagId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyRpaScenarioTag")
	@RestLog(action = LogAction.Modify, target = LogTarget.RpaScenarioTag, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ,SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaScenarioTagResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyRpaScenarioTag(@PathParam("scenarioTagId") String scenarioTagId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyRpaScenarioTagBody", content = @Content(schema = @Schema(implementation = ModifyRpaScenarioTagRequest.class))) String requestBody)
			throws HinemosUnknown, RpaScenarioTagNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call modifyRpaScenarioTag()");

		ModifyRpaScenarioTagRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,ModifyRpaScenarioTagRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		RpaScenarioTag infoReq = new RpaScenarioTag();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setTagId(scenarioTagId);

		RpaScenarioTag infoRes = new RpaControllerBean().modifyRpaScenarioTag(infoReq);

		RpaScenarioTagResponse dtoRes = new RpaScenarioTagResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * RPAシナリオタグ情報の削除
	 */
	@DELETE
	@Path("/scenariotag")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteRpaScenarioTag")
	@RestLog(action = LogAction.Delete, target = LogTarget.RpaScenarioTag, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ,SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaScenarioTagResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteRpaScenarioTag(@ArrayTypeParam @QueryParam(value = "scenarioTagIds") String scenarioTagIds,
			@Context Request request, @Context UriInfo uriInfo) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.info("call deleteRpaScenarioTag()");

		List<String> scenarioTagIdList = Arrays.asList(scenarioTagIds.split(","));
		List<RpaScenarioTag> infoResList = new RpaControllerBean().deleteRpaScenarioTag(scenarioTagIdList);
		List<RpaScenarioTagResponse> dtoResList = new ArrayList<>();
		for (RpaScenarioTag info : infoResList) {
			RpaScenarioTagResponse dto = new RpaScenarioTagResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * PRAシナリオタグ一覧の取得を行うAPI
	 */
	@GET
	@Path("/scenariotag")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaSinarioTagList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaScenarioTagResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.RpaScenarioTag, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	public Response getRpaSinarioTagList(@QueryParam(value = "ownerRoleId") String ownerRoleId, 
			@Context Request request, @Context UriInfo uriInfo) throws HinemosUnknown, RpaScenarioTagNotFound, InvalidUserPass, InvalidRole {
		m_log.info("call getRpaSinarioTagList()");
		
		List<RpaScenarioTag> infoResList = new RpaControllerBean().getRpaScenarioTagListByOwnerRole(ownerRoleId);
		
		List<RpaScenarioTagResponse> dtoResList = new ArrayList<>();
		for (RpaScenarioTag info : infoResList) {
			RpaScenarioTagResponse dto = new RpaScenarioTagResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * タグIDを基にRPAシナリオタグ情報の取得を行うAPI
	 */
	@GET
	@Path("/scenariotag/{scenarioTagId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetScenarioTag")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaScenarioTagResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.RpaScenarioTag, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	public Response getRpaScenarioTag(@Context Request request, @Context UriInfo uriInfo,
			@PathParam(value = "scenarioTagId") String scenarioTagId) throws HinemosUnknown, RpaScenarioTagNotFound, InvalidUserPass, InvalidRole {
		m_log.debug("getRpaScenarioTag");
		
		RpaScenarioTag infoRes = new RpaControllerBean().getRpaScenarioTag(scenarioTagId);
		RpaScenarioTagResponse dtoRes = new RpaScenarioTagResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * 条件に従いPRAシナリオ実績一覧の取得を行うAPI
	 */
	@POST
	@Path("/scenariooperationresult_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SearchRpaScenarioOperationResult")
	@RestLog(action=LogAction.Get, target=LogTarget.RpaScenarioResult, type = LogType.REFERENCE)
	@RestSystemPrivilege(function=SystemPrivilegeFunction.Rpa, modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SearchRpaScenarioOperationResultResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response")}) 
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response searchRpaScenarioOperationResult(
			@Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "searchRpaScenarioOperationResultRequest", content = @Content(schema = @Schema(implementation = SearchRpaScenarioOperationResultRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidSetting, InvalidRole, HinemosUnknown {
		m_log.info("call searchRpaScenarioOperationResult()");

		SearchRpaScenarioOperationResultRequest dtoReq= RestObjectMapperWrapper.convertJsonToObject(requestBody,SearchRpaScenarioOperationResultRequest.class);
		RpaScenarioOperationResultFilterInfo infoReq = new RpaScenarioOperationResultFilterInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		
		SearchRpaScenarioOperationResultResponse dtoRes = new RpaControllerBean().getRpaScenarioOperationResultList(infoReq);
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * 実績IDを基にRPAシナリオ実績詳細情報の取得を行うAPI
	 */
	@GET
	@Path("/scenariooperationresult_detail/{resultId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaScenarioOperationResultWithDetail")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaScenarioOperationResultWithDetailResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.RpaScenarioResult, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	public Response getRpaScenarioOperationResultWithDetail(
			@Context Request request, 
			@Context UriInfo uriInfo, 
			@PathParam(value = "resultId") Long resultId) throws HinemosUnknown, RpaScenarioOperationResultNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.debug("getRpaScenarioOperationResultWithDetail");
		
		RpaScenarioOperationResultWithDetailResponse dtoRes = new RpaControllerBean().getRpaScenarioOperationResult(resultId);
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * シナリオIDより、同一作成設定ID、同一シナリオ識別子のRPAシナリオの実行ノード情報の取得を行うAPI
	 */
	@GET
	@Path("/scenario_correctExecnode")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaScenarioCorrectExecNode")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetRpaScenarioCorrectExecNodeResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.RpaScenario, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	public Response getRpaScenarioCorrectExecNode(@Context Request request, @Context UriInfo uriInfo,
			@QueryParam(value = "scenarioId") String scenarioId) throws HinemosUnknown, RpaScenarioNotFound, InvalidUserPass, InvalidRole {
		
		GetRpaScenarioCorrectExecNodeResponse dtoRes = new RpaControllerBean().getRpaScenarioModifyExecNode(scenarioId);
		
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * 棒グラフに出力する為の情報の取得を行うAPI
	 */
	@GET
	@Path("/scenariooperationresult_summary_bar")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaScenarioOperationResultSummaryForBar")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetRpaScenarioOperationResultSummaryForBarResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.RpaScenarioResult, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	public Response getRpaScenarioOperationResultSummaryForBar(
			@ArrayTypeParam @QueryParam(value = "facilityId") String facilityId,
			@QueryParam(value = "targetMonth") Long targetMonth, 
			@QueryParam(value = "dataType") SummaryTypeEnum dataType,
			@QueryParam(value = "limit") Integer limit, 
			@Context Request request, @Context UriInfo uriInfo) throws HinemosUnknown, InvalidRole {
		m_log.info("call getRpaScenarioOperationResultSummaryForBar()");
		
		List<String> facilityIds = Arrays.asList(facilityId.split(","));

		GetRpaScenarioOperationResultSummaryForBarResponse dtoRes = new RpaControllerBean().getRpaScenarioOperationResultSummaryForBar(facilityIds, targetMonth, dataType, limit);
		
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * 円グラフに出力する為の情報の取得を行うAPI
	 */
	@GET
	@Path("/scenariooperationresult_summary_pie")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaScenarioOperationResultSummaryForPie")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetRpaScenarioOperationResultSummaryForPieResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.RpaScenarioResult, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	public Response getRpaScenarioOperationResultSummaryForPie(
			@ArrayTypeParam @QueryParam(value = "facilityId") String facilityId,
			@QueryParam(value = "targetMonth") Long targetMonth, 
			@QueryParam(value = "dataType") SummaryTypeEnum dataType,
			@Context Request request, @Context UriInfo uriInfo) throws HinemosUnknown, InvalidRole {
		m_log.info("call getRpaScenarioOperationResultSummaryForPie()");
		
		List<String> facilityIds = Arrays.asList(facilityId.split(","));
		
		GetRpaScenarioOperationResultSummaryForPieResponse dtoRes = new RpaControllerBean().getRpaScenarioOperationResultSummaryForPie(facilityIds, targetMonth, dataType);
		
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * シナリオ実績作成設定を取得する。 
	 */
	@GET
	@Path("/scenarioOperationResultCreateSetting/{operationResultCreateSettingId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaScenarioOperationResultCreateSetting")
	@RestLog(action = LogAction.Get, target = LogTarget.RpaScenarioResultCreate, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaScenarioOperationResultCreateSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRpaScenarioOperationResultCreateSetting(@Context Request request, @Context UriInfo uriInfo,
			@PathParam(value = "operationResultCreateSettingId") String operationResultCreateSettingId) throws InvalidRole, HinemosUnknown, RpaScenarioOperationResultCreateSettingNotFound {
		m_log.info("call getRpaScenarioOperationResultCreateSetting()");
		RpaScenarioOperationResultCreateSetting infoRes = new RpaControllerBean().getRpaScenarioOperationResultCreateSetting(operationResultCreateSettingId);
		RpaScenarioOperationResultCreateSettingResponse dtoRes = new RpaScenarioOperationResultCreateSettingResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * シナリオ実績作成設定一覧を取得する。
	 */
	@GET
	@Path("/scenarioOperationResultCreateSetting")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaScenarioOperationResultCreateSettingList")
	@RestLog(action = LogAction.Get, target = LogTarget.RpaScenarioResultCreate, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaScenarioOperationResultCreateSettingResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRpaScenarioOperationResultCreateSettingList(@Context Request request, @Context UriInfo uriInfo) throws InvalidRole, HinemosUnknown {
		m_log.info("call getRpaScenarioOperationResultCreateSettingList()");
		List<RpaScenarioOperationResultCreateSetting> infoResList = new RpaControllerBean().getRpaScenarioOperationResultCreateSettingList();
		List<RpaScenarioOperationResultCreateSettingResponse> dtoResList = new ArrayList<>();
		for (RpaScenarioOperationResultCreateSetting infoRes : infoResList){
			RpaScenarioOperationResultCreateSettingResponse dtoRes = new RpaScenarioOperationResultCreateSettingResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);
		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * シナリオ実績作成設定をマネージャに登録する。
	 */
	@POST
	@Path("/scenarioOperationResultCreateSetting")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddRpaScenarioOperationResultCreateSetting")
	@RestLog(action = LogAction.Add, target = LogTarget.RpaScenarioResultCreate, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ,SystemPrivilegeMode.ADD })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaScenarioOperationResultCreateSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addRpaScenarioOperationResultCreateSetting(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addRpaScenarioOperationResultCreateSettingBody", content = @Content(schema = @Schema(implementation = AddRpaScenarioOperationResultCreateSettingRequest.class))) String requestBody)
			throws HinemosUnknown, RpaScenarioOperationResultCreateSettingDuplicate, InvalidRole, InvalidSetting {
		m_log.info("call addRpaScenarioOperationResultCreateSetting()");

		AddRpaScenarioOperationResultCreateSettingRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, AddRpaScenarioOperationResultCreateSettingRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		RpaScenarioOperationResultCreateSetting infoReq = new RpaScenarioOperationResultCreateSetting();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		RpaScenarioOperationResultCreateSetting infoRes = new RpaControllerBean().addRpaScenarioOperationResultCreateSetting(infoReq);

		RpaScenarioOperationResultCreateSettingResponse dtoRes = new RpaScenarioOperationResultCreateSettingResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * シナリオ実績作成設定を変更する。
	 * 
	 */
	@PUT
	@Path("/scenarioOperationResultCreateSetting/{operationResultCreateSettingId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifyRpaScenarioOperationResultCreateSetting")
	@RestLog(action = LogAction.Modify, target = LogTarget.RpaScenarioResultCreate, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ,SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaScenarioOperationResultCreateSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyRpaScenarioOperationResultCreateSetting(@PathParam("operationResultCreateSettingId") String operationResultCreateSettingId, @Context Request request,
			@Context UriInfo uriInfo,
			@RequestBody(description = "modifyRpaScenarioOperationResultCreateSettingBody", content = @Content(schema = @Schema(implementation = ModifyRpaScenarioOperationResultCreateSettingRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidRole, InvalidSetting, RpaScenarioOperationResultCreateSettingNotFound {
		m_log.info("call modifyRpaScenarioOperationResultCreateSetting()");

		ModifyRpaScenarioOperationResultCreateSettingRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, ModifyRpaScenarioOperationResultCreateSettingRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();
		m_log.info("dtoReq.getNotifyId() = " + dtoReq.getNotifyId());
		if(dtoReq.getNotifyId() != null){
			for(NotifyRelationInfoRequest n : dtoReq.getNotifyId()){
				m_log.info("dtoReq.getNotifyId() notify = " + n);
			}
		}

		RpaScenarioOperationResultCreateSetting infoReq = new RpaScenarioOperationResultCreateSetting();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		infoReq.setScenarioOperationResultCreateSettingId(operationResultCreateSettingId);

		RpaScenarioOperationResultCreateSetting infoRes = new RpaControllerBean().modifyRpaScenarioOperationResultCreateSetting(infoReq);
		
		RpaScenarioOperationResultCreateSettingResponse dtoRes = new RpaScenarioOperationResultCreateSettingResponse();
		RestBeanUtil.convertBean(infoRes, dtoRes);
		
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * シナリオ実績作成設定を削除する。
	 * 
	 */
	@DELETE
	@Path("/scenarioOperationResultCreateSetting")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteRpaScenarioOperationResultCreateSetting")
	@RestLog(action = LogAction.Delete, target = LogTarget.RpaScenarioResultCreate, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ,SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaScenarioOperationResultCreateSettingResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteRpaScenarioOperationResultCreateSetting(@ArrayTypeParam @QueryParam(value = "operationResultCreateSettingIds") String operationResultCreateSettingIds,
			@Context Request request, @Context UriInfo uriInfo) throws HinemosUnknown, RpaScenarioOperationResultCreateSettingNotFound, InvalidRole {
		m_log.info("call deleteRpaScenarioOperationResultCreateSetting()");

		List<String> operationResultCreateSettingIdList = Arrays.asList(operationResultCreateSettingIds.split(","));
		List<RpaScenarioOperationResultCreateSetting> infoResList = new RpaControllerBean().deleteRpaScenarioOperationResultCreateSetting(operationResultCreateSettingIdList);
		List<RpaScenarioOperationResultCreateSettingResponse> dtoResList = new ArrayList<>();
		for (RpaScenarioOperationResultCreateSetting info : infoResList) {
			RpaScenarioOperationResultCreateSettingResponse dto = new RpaScenarioOperationResultCreateSettingResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}

		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * シナリオ実績作成設定を変更する。
	 * 
	 */
	@PUT
	@Path("/scenarioOperationResultCreateSetting_valid")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SetScenarioOperationResultCreateSettingValid")
	@RestLog(action = LogAction.Modify, target = LogTarget.RpaScenarioResultCreate, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ,SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaScenarioOperationResultCreateSettingResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setScenarioOperationResultCreateSettingValid(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "setScenarioOperationResultCreateSettingValidBody", content = @Content(schema = @Schema(implementation = SetRpaScenarioOperationResultCreateSettingValidRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidRole, InvalidSetting, RpaScenarioOperationResultCreateSettingNotFound {
		m_log.info("call setScenarioOperationResultCreateSettingValid()");

		SetRpaScenarioOperationResultCreateSettingValidRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody, SetRpaScenarioOperationResultCreateSettingValidRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();
		
		List<RpaScenarioOperationResultCreateSetting> infoResList
			= new RpaControllerBean().setRpaScenarioOperationResultCreateSettingValid(dtoReq.getSettingIdList(), dtoReq.getValidFlg());

		List<RpaScenarioOperationResultCreateSettingResponse> dtoResList = new ArrayList<>();
		for (RpaScenarioOperationResultCreateSetting infoRes : infoResList) {
			RpaScenarioOperationResultCreateSettingResponse dtoRes = new RpaScenarioOperationResultCreateSettingResponse();
			RestBeanUtil.convertBean(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * シナリオ実行ノード訂正を行う
	 */
	@POST
	@Path("/scenario_correctExecNode")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "CorrectExecNode")
	@RestLog(action = LogAction.Modify, target = LogTarget.RpaScenario, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ,SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response correctExecNode(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "correctExecNodeBody", content = @Content(schema = @Schema(implementation = CorrectExecNodeRequest.class))) String requestBody)
			throws HinemosUnknown, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call correctExecNode()");

		CorrectExecNodeRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,CorrectExecNodeRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();
		
		// ノード訂正実行
		new RpaControllerBean().correctExecNode(dtoReq.getScenarioOperationResultCreateSettingId(), dtoReq.getScenarioIdentifyString(), dtoReq.getExecNodes());

		return Response.status(Status.OK).build();
	}

	/**
	 * シナリオ実績更新を行う(スケジューリングする)
	 */
	@POST
	@Path("/scenario_updateOperationResult")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "UpdateOperationResult")
	@RestLog(action = LogAction.Modify, target = LogTarget.RpaScenarioResult, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ,SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateOperationResult(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "updateOperationResultBody", content = @Content(schema = @Schema(implementation = UpdateRpaScenarioOperationResultRequest.class))) String requestBody)
			throws HinemosUnknown, RpaScenarioNotFound, InvalidUserPass, InvalidRole, InvalidSetting {
		m_log.info("call updateOperationResult()");

		UpdateRpaScenarioOperationResultRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,UpdateRpaScenarioOperationResultRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		UpdateRpaScenarioOperationResultInfo infoReq = new UpdateRpaScenarioOperationResultInfo();
		RestBeanUtil.convertBean(dtoReq, infoReq);
		new RpaControllerBean().updateRpaScenarioOperationResult(infoReq);

		return Response.status(Status.OK).build();
	}


	/**
	 * シナリオ実行ノードのリクエストDTOをEntityにセットする
	 * @param scenarioInfo
	 * @param execNodesDto
	 */
	private void setExecNodeInfo(RpaScenario scenarioInfo, List<String> execNodesDto) {
		scenarioInfo.setExecNodes(new ArrayList<>());
		for (String execNodeDto : execNodesDto) {
			scenarioInfo.addExecNode(execNodeDto);
		}
	}
	
	/**
	 * シナリオ実行ノードのEntityをレスポンスDTOにセットする
	 * @param scenarioDto
	 * @param execNodesInfo
	 */
	private void setExecNodeDto(RpaScenarioResponse scenarioDto, List<RpaScenarioExecNode> execNodesInfo) {
		scenarioDto.setExecNodes(new ArrayList<>());
		for (RpaScenarioExecNode execNodeInfo : execNodesInfo) {
			scenarioDto.addExecNode(execNodeInfo.getId().getFacilityId());
		}
	}
	
	/**
	 * DB取得したCSVファイルをzipファイルにまとめてクライアント送信用に返却する
	 */
	@POST
	@Path("/scenario_data_zip_download")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DownloadRpaScenarioOperationResultRecords")
	@RestLog(action = LogAction.Get, target = LogTarget.RpaScenarioResult, type = LogType.REFERENCE )
	@RestSystemPrivilege(function=SystemPrivilegeFunction.Rpa,modeList={SystemPrivilegeMode.READ})
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	@Consumes(MediaType.APPLICATION_JSON)
	public Response downloadRpaScenarioOperationResultRecords(
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "downloadRpaScenarioOperationResultRecordsBody", 
			content = @Content(schema = @Schema(implementation = DownloadRpaScenarioOperationResultRecordsRequest.class))) 
			String requestBody)
					throws InvalidSetting, HinemosUnknown {
		m_log.info("call downloadRecords");
		
		DownloadRpaScenarioOperationResultRecordsRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				DownloadRpaScenarioOperationResultRecordsRequest.class);

		RpaControllerBean controller = new RpaControllerBean();
		RestDownloadFile restDownloadFile = null;
		
		// 最優先のLocaleを取得
		List<Locale> localeList = HinemosSessionContext.getLocaleList();
		List<Locale> availableLocaleList = Arrays.asList(NumberFormat.getAvailableLocales());
		Locale targetLocale = null;
		for (Locale locale : localeList) {
			if (availableLocaleList.contains(locale)) {
				m_log.debug(locale.toString() + " is contained in availableLocaleList");
				targetLocale = locale;
			}
		}
		
		// マネージャに一時ファイルを出力
		ArrayList<String> intoZipList = controller.createTmpRecords(dtoReq, targetLocale);

		// ZIPファイルにまとめてクライアント送信用添付ファイル返却
		String fileName = dtoReq.getFilename();
		String clientName = dtoReq.getClientName();

		// TODO 一時ファイル対応
		restDownloadFile = controller.createZipHandler(intoZipList, fileName, clientName);

		// ダウンロード処理
		StreamingOutput stream = RestTempFileUtil.getTempFileStream(restDownloadFile.getTempFile());

		return Response.ok(stream).header("Content-Disposition", "attachment; name=" + restDownloadFile.getFileName()).build();
	}
	
	/**
	 * 自動化効果計算マスタの一覧を取得する。
	 */
	@GET
	@Path("/scenariocoefficientpattern")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetRpaScenarioCoefficientPatternList")
	@RestLog(action = LogAction.Get, target = LogTarget.Master, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaScenarioCoefficientPatternResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRpaScenarioCoefficientPatternList(@Context Request request, @Context UriInfo uriInfo) throws InvalidRole, HinemosUnknown {
		m_log.info("call getRpaScenarioCoefficientPatternList()");
		List<RpaScenarioCoefficientPattern> infoResList = new RpaControllerBean().getRpaScenarioCoefficientPatternList();
		List<RpaScenarioCoefficientPatternResponse> dtoResList = new ArrayList<>();
		for (RpaScenarioCoefficientPattern infoRes : infoResList){
			RpaScenarioCoefficientPatternResponse dtoRes = new RpaScenarioCoefficientPatternResponse();
			RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
			dtoResList.add(dtoRes);
		}
		RestLanguageConverter.convertMessages(dtoResList);
		return Response.status(Status.OK).entity(dtoResList).build();
	}
	
	/**
	 * 自動化効果計算マスタを削除する。
	 * 
	 */
	@DELETE
	@Path("/scenariocoefficientpattern")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteRpaScenarioCoefficientPattern")
	@RestLog(action = LogAction.Delete, target = LogTarget.Master, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.READ,SystemPrivilegeMode.MODIFY })
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RpaScenarioCoefficientPatternResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteRpaScenarioCoefficientPattern(@QueryParam(value = "rpaToolEnvId") String rpaToolEnvId,
			@QueryParam(value = "orderNo") Integer orderNo,
			@Context Request request, @Context UriInfo uriInfo) throws HinemosUnknown, InvalidRole {
		m_log.info("call deleteRpaScenarioCoefficientPattern()");

		RpaScenarioCoefficientPattern info = 
				new RpaControllerBean().deleteRpaScenarioCoefficientPattern(rpaToolEnvId, orderNo);
		RpaScenarioCoefficientPatternResponse dto = new RpaScenarioCoefficientPatternResponse();
		RestBeanUtil.convertBeanNoInvalid(info, dto);

		RestLanguageConverter.convertMessages(dto);

		return Response.status(Status.OK).entity(dto).build();
	}
}
