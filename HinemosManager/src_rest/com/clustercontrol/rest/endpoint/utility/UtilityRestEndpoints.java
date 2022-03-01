/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.utility;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
import com.clustercontrol.bean.ActivationKeyConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.rest.annotation.RestLog;
import com.clustercontrol.rest.annotation.RestLog.LogAction;
import com.clustercontrol.rest.annotation.RestLog.LogTarget;
import com.clustercontrol.rest.annotation.RestLog.LogType;
import com.clustercontrol.rest.annotation.RestLogFunc;
import com.clustercontrol.rest.annotation.RestLogFunc.LogFuncName;
import com.clustercontrol.rest.annotation.RestSystemAdminPrivilege;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.ArrayTypeParam;
import com.clustercontrol.rest.endpoint.jobmap.dto.CheckPublishResponse;
import com.clustercontrol.rest.endpoint.utility.controller.ImportCalendarController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportCalendarPatternController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportCloudScopeController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportFileCheckController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportFilterSettingJobHistoryController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportFilterSettingMonitorHistoryEventController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportFilterSettingMonitorHistoryStatusController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportHinemosPropertyController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportInfraManagementInfoController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportJmxMasterController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportJobLinkRcvController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportJobLinkSendController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportJobManualController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportJobMasterController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportJobQueueController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportLogFormatController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportMailTemplateController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportMaintenanceController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportMonitorCommonController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportNodeConfigSettingController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportNodeController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportNodeMapModelController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportNotifyController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportObjectPrivilegeInfoController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportPlatformMasterController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportReportingInfoController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportReportingTemplateSetController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportRestAccessInfoController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportRoleController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportRoleUserController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportRpaManagementToolAccountController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportRpaScenarioCoefficientPatternController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportRpaScenarioOperationResultCreateSettingController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportRpaScenarioTagController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportScheduleController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportScopeController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportSystemPrivilegeInfoController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportTransferController;
import com.clustercontrol.rest.endpoint.utility.controller.ImportUserController;
import com.clustercontrol.rest.endpoint.utility.dto.GetImportUnitNumberResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportCalendarPatternRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportCalendarPatternResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportCalendarRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportCalendarResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportCloudScopeRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportCloudScopeResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportEventFilterSettingRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportFileCheckRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportFileCheckResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportFilterSettingResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportHinemosPropertyRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportHinemosPropertyResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportInfraManagementInfoRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportInfraManagementInfoResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportJmxMasterRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportJmxMasterResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportJobHistoryFilterSettingRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportJobLinkRcvRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportJobLinkRcvResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportJobLinkSendRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportJobLinkSendResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportJobManualRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportJobManualResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportJobMasterRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportJobMasterResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportJobQueueRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportJobQueueResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportLogFormatRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportLogFormatResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportMailTemplateRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportMailTemplateResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportMaintenanceRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportMaintenanceResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportMonitorCommonRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportMonitorCommonResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportNodeConfigSettingRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportNodeConfigSettingResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportNodeMapModelRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportNodeMapModelResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportNodeRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportNodeResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportNotifyRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportNotifyResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportObjectPrivilegeInfoRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportObjectPrivilegeInfoResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportPlatformMasterRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportPlatformMasterResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportReportingInfoRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportReportingInfoResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportReportingTemplateSetRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportReportingTemplateSetResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRestAccessInfoRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRestAccessInfoResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRoleRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRoleResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRoleUserRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRoleUserResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRpaManagementToolAccountRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRpaManagementToolAccountResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRpaScenarioCoefficientPatternRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRpaScenarioCoefficientPatternResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRpaScenarioOperationResultCreateSettingRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRpaScenarioOperationResultCreateSettingResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRpaScenarioTagRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportRpaScenarioTagResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportScheduleRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportScheduleResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportScopeRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportScopeResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportStatusFilterSettingRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportSystemPrivilegeInfoRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportSystemPrivilegeInfoResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportTransferRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportTransferResponse;
import com.clustercontrol.rest.endpoint.utility.dto.ImportUserRequest;
import com.clustercontrol.rest.endpoint.utility.dto.ImportUserResponse;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.util.KeyCheck;

@Path("/utility")
@RestLogFunc(name = LogFuncName.Utility)
public class UtilityRestEndpoints {
	private static Log m_log = LogFactory.getLog(UtilityRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "utility";
	/**
	 * クライアントがマネージャのサブスクリプション有無の判定に使用します。
	 * 
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/checkPublish")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "CheckPublish")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = CheckPublishResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Get, target = LogTarget.CheckPublish, type = LogType.REFERENCE )
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkPublish(@Context Request request, @Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call checkPublish()");
		
		boolean publish = KeyCheck.checkKey(ActivationKeyConstant.TYPE_ENTERPRISE);
		
		CheckPublishResponse dtoRes = new CheckPublishResponse();
		dtoRes.setPublish(publish);
		
		RestLanguageConverter.convertMessages(dtoRes);
		
		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * 指定された機能のインポート一括処理数を返却します。
	 * 
	 * クライアント側でのインポート制御に利用します。
	 * 
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/getImportUnitNumber")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetImportUnitNumber")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetImportUnitNumberResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Get, target = LogTarget.ImportUnitNumber, type = LogType.REFERENCE )
	@Produces(MediaType.APPLICATION_JSON)
	public Response getImportUnitNumber(@Context Request request, @Context UriInfo uriInfo 	,@ArrayTypeParam @QueryParam("functionIds") String functionIds) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.info("call getImportUnitNumber()");

		List<GetImportUnitNumberResponse> dtoRes = new  ArrayList<GetImportUnitNumberResponse>();

		List<String> idList = new ArrayList<>();
		if(functionIds != null && !functionIds.isEmpty()) {
			idList = Arrays.asList(functionIds.split(","));
		}
		
		// 各機能毎の数を構築して返却
		Integer commonNum = HinemosPropertyCommon.utility_import_unitnum_common.getIntegerValue();
		for(String id : idList){
			Integer funcNum  = HinemosPropertyCommon.utility_import_unitnum_$.getIntegerValue(id,commonNum.longValue());
			GetImportUnitNumberResponse dtoRecord = new GetImportUnitNumberResponse();
			dtoRecord.setFunctionId(id);
			dtoRecord.setImportUnitNumber(funcNum);
			dtoRes.add(dtoRecord);
		}
		RestLanguageConverter.convertMessages(dtoRes);
		return Response.status(Status.OK).entity(dtoRes).build();
	}
	
	/**
	 * ノード情報のインポートを行います。
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/node")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportNode")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportNodeResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.Node , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importNode(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "importNodeRequest", content = @Content(schema = @Schema(implementation = ImportNodeRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportNodeRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportNodeRequest.class);

		m_log.info("call importNode()");

		ImportNodeResponse resDto = new ImportNodeResponse();

		ImportNodeController controller = new ImportNodeController(
				dtoReq.isRollbackIfAbnormal(),dtoReq.getRecordList());

		controller.importExecute();
		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();

	}
	/**
	 * スコープ情報のインポートを行います。
	 * 
	 * スコープ本体および割付ノード情報をまとめて変更します。
	 * 
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/scope")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportScope")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportScopeResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.Scope , type = LogType.UPDATE )
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	public Response importScope(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportScopeRequest", content = @Content(schema = @Schema(implementation = ImportScopeRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportScopeRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportScopeRequest.class);

		m_log.info("call importScope()");

		ImportScopeResponse resDto = new ImportScopeResponse();

		ImportScopeController controller = new ImportScopeController(
				dtoReq.isRollbackIfAbnormal(),dtoReq.getRecordList());
		controller.importExecute();
		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();

	}

	/**
	 * ジョブ（マスタ）情報のインポートを行います。
	 * 	
	 * 実装上の都合で「１件でもエラーの場合、全件ロールバック」 に未対応
	 * 
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/jobmaster")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportJobMaster")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportJobMasterResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.Jobmaster , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importJobMaster(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportScopeRequest", content = @Content(schema = @Schema(implementation = ImportJobMasterRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportJobMasterRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportJobMasterRequest.class);

		m_log.info("call ImportJobMaster()");

		ImportJobMasterResponse resDto = new ImportJobMasterResponse();

		ImportJobMasterController controller = new ImportJobMasterController(
				dtoReq.isRollbackIfAbnormal(),dtoReq.getRecordList());
		controller.setRemoteAddr(request.getRemoteAddr());
		controller.importExecute();
		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();

	}

	/**
	 * ロール情報のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/role")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportRole")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportRoleResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.Role , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importRole(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportRoleRequest", content = @Content(schema = @Schema(implementation = ImportRoleRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportRoleRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportRoleRequest.class);

		m_log.info("call importRole()");

		ImportRoleResponse resDto = new ImportRoleResponse();

		ImportRoleController controller = new ImportRoleController(
				dtoReq.isRollbackIfAbnormal(),dtoReq.getRecordList());
		controller.importExecute();
		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	/**
	 * ユーザ情報のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/user")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportUser")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportUserResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.User , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importUser(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportUserRequest", content = @Content(schema = @Schema(implementation = ImportUserRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportUserRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportUserRequest.class);

		m_log.info("call importUser()");

		ImportUserResponse resDto = new ImportUserResponse();

		ImportUserController controller = new ImportUserController(
				dtoReq.isRollbackIfAbnormal(),dtoReq.getRecordList());
		controller.importExecute();
		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	/**
	 * ロールへのユーザの割り当て情報のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/roleUser")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportRoleUser")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportRoleUserResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.RoleUser , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importRoleUser(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportRoleUserRequest", content = @Content(schema = @Schema(implementation = ImportRoleUserRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportRoleUserRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportRoleUserRequest.class);

		m_log.info("call importRoleUser()");

		ImportRoleUserResponse resDto = new ImportRoleUserResponse();

		ImportRoleUserController controller = new ImportRoleUserController(
				dtoReq.isRollbackIfAbnormal(),dtoReq.getRecordList());
		
		controller.importExecute();
		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	/**
	 * 監視情報（共通）のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/monitor")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportMonitorCommon")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportMonitorCommonResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.Monitor , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.MonitorSetting, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importMonitorCommon(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportMonitorCommonRequest", content = @Content(schema = @Schema(implementation = ImportMonitorCommonRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportMonitorCommonRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportMonitorCommonRequest.class);

		m_log.info("call importMonitorCommon()");

		ImportMonitorCommonResponse resDto = new ImportMonitorCommonResponse();

		ImportMonitorCommonController controller = new ImportMonitorCommonController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	

	/**
	 * オブジェクト権限のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/objectPrivilege")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportObjectPrivilegeInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportObjectPrivilegeInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.ObjectPrivilege , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importObjectPrivilegeInfo(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportObjectPrivilegeInfoRequest", content = @Content(schema = @Schema(implementation = ImportObjectPrivilegeInfoRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportObjectPrivilegeInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportObjectPrivilegeInfoRequest.class);

		m_log.info("call importObjectPrivilegeInfo()");

		ImportObjectPrivilegeInfoResponse resDto = new ImportObjectPrivilegeInfoResponse();

		ImportObjectPrivilegeInfoController controller = new ImportObjectPrivilegeInfoController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	/**
	 * システム権限のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/systemPrivilege")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "importSystemPrivilegeInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportSystemPrivilegeInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.SystemPrivilege , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.AccessControl, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importSystemPrivilegeInfo(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportSystemPrivilegeInfoRequest", content = @Content(schema = @Schema(implementation = ImportSystemPrivilegeInfoRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportSystemPrivilegeInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportSystemPrivilegeInfoRequest.class);

		m_log.info("call importSystemPrivilegeInfo()");

		ImportSystemPrivilegeInfoResponse resDto = new ImportSystemPrivilegeInfoResponse();

		ImportSystemPrivilegeInfoController controller = new ImportSystemPrivilegeInfoController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	/**
	 * 環境構築情報のインポートを行います。	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/infraManagement")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportInfraManagementInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportInfraManagementInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.InfraManagement , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Infra, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importInfraManagementInfo(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportInfraManagementInfoRequest", content = @Content(schema = @Schema(implementation = ImportInfraManagementInfoRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportInfraManagementInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportInfraManagementInfoRequest.class);

		m_log.info("call importSystemPrivilegeInfo()");

		ImportInfraManagementInfoResponse resDto = new ImportInfraManagementInfoResponse();

		ImportInfraManagementInfoController controller = new ImportInfraManagementInfoController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	/**	 * 通知のインポートを行います。
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/notify")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportNotify")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportNotifyResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.Notify , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importNotify(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportNotifyRequest", content = @Content(schema = @Schema(implementation = ImportNotifyRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportNotifyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportNotifyRequest.class);

		m_log.info("call importNotify()");

		ImportNotifyResponse resDto = new ImportNotifyResponse();

		ImportNotifyController controller = new ImportNotifyController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	/**
	 * レポーティング情報のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/reporting")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportReportingInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportReportingInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.Reporting , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Reporting, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importReportingInfo(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportReportingInfoRequest", content = @Content(schema = @Schema(implementation = ImportReportingInfoRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportReportingInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportReportingInfoRequest.class);

		m_log.info("call importReportingInfo()");

		ImportReportingInfoResponse resDto = new ImportReportingInfoResponse();

		ImportReportingInfoController controller = new ImportReportingInfoController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	/**
	 * レポーティングテンプレートセットのインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/reportingTemplateSet")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportReportingTemplateSet")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportReportingTemplateSetResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.ReportingTemplateSet , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Reporting, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importReportingTemplateSet(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportReportingTemplateSetRequest", content = @Content(schema = @Schema(implementation = ImportReportingTemplateSetRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportReportingTemplateSetRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportReportingTemplateSetRequest.class);

		m_log.info("call importReportingTemplateSet()");

		ImportReportingInfoResponse resDto = new ImportReportingInfoResponse();

		ImportReportingTemplateSetController controller = new ImportReportingTemplateSetController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * クラウドスコープのインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/cloudScope")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportCloudScope")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportCloudScopeResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.CloudScope , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.CloudManagement, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@RestSystemAdminPrivilege(isNeed=true)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importCloudScope(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportCloudScopeRequest", content = @Content(schema = @Schema(implementation = ImportCloudScopeRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportCloudScopeRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportCloudScopeRequest.class);

		m_log.info("call importCloudScope()");

		ImportReportingInfoResponse resDto = new ImportReportingInfoResponse();

		ImportCloudScopeController controller = new ImportCloudScopeController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	/**
	 * ログフォーマットのインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/logFormat")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportLogFormat")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportLogFormatResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.LogFormat , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Hub, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importLogFormat(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportLogFormatRequest", content = @Content(schema = @Schema(implementation = ImportLogFormatRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportLogFormatRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportLogFormatRequest.class);

		m_log.info("call importLogFormat()");

		ImportLogFormatResponse resDto = new ImportLogFormatResponse();

		ImportLogFormatController controller = new ImportLogFormatController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * 構成情報取得のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/nodeConfigSetting")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportNodeConfigSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportNodeConfigSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.NodeConfigSetting , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importNodeConfigSetting(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportNodeConfigSetting", content = @Content(schema = @Schema(implementation = ImportNodeConfigSettingRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportNodeConfigSettingRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportNodeConfigSettingRequest.class);

		m_log.info("call importNodeConfigSetting()");

		ImportNodeConfigSettingResponse resDto = new ImportNodeConfigSettingResponse();

		ImportNodeConfigSettingController controller = new ImportNodeConfigSettingController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * メールテンプレートのインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/mailTemplate")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportMailTemplate")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportMailTemplateResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.MailTemplate , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importMailTemplate(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportMailTemplate", content = @Content(schema = @Schema(implementation = ImportMailTemplateRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportMailTemplateRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportMailTemplateRequest.class);

		m_log.info("call importMailTemplate()");

		ImportMailTemplateResponse resDto = new ImportMailTemplateResponse();

		ImportMailTemplateController controller = new ImportMailTemplateController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * カレンダのインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/calendar")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportCalendar")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportCalendarResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.Calendar , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Calendar, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importCalendar(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportCalendar", content = @Content(schema = @Schema(implementation = ImportCalendarRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown {
		ImportCalendarRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportCalendarRequest.class);

		m_log.info("call importCalendar()");

		ImportCalendarResponse resDto = new ImportCalendarResponse();

		ImportCalendarController controller = new ImportCalendarController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * カレンダパターンのインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/calendarPattern")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportCalendarPattern")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportCalendarPatternResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.CalendarPattern , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Calendar, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importCalendarPattern(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportCalendarPattern", content = @Content(schema = @Schema(implementation = ImportCalendarPatternRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown {
		ImportCalendarPatternRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportCalendarPatternRequest.class);

		m_log.info("call importCalendarPattern()");

		ImportCalendarPatternResponse resDto = new ImportCalendarPatternResponse();

		ImportCalendarPatternController controller = new ImportCalendarPatternController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * メンテナンスのインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/maintenance")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportMaintenance")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportMaintenanceResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.Maintenance , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Maintenance, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importMaintenance(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportMaintenance", content = @Content(schema = @Schema(implementation = ImportMaintenanceRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown {
		ImportMaintenanceRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportMaintenanceRequest.class);

		m_log.info("call importMaintenance()");

		ImportMaintenanceResponse resDto = new ImportMaintenanceResponse();

		ImportMaintenanceController controller = new ImportMaintenanceController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * Hinemosプロパティのインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/hinemosProperty")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportHinemosProperty")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportHinemosPropertyResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.HinemosProperty , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Maintenance, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importHinemosProperty(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportHinemosProperty", content = @Content(schema = @Schema(implementation = ImportHinemosPropertyRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown {
		ImportHinemosPropertyRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportHinemosPropertyRequest.class);

		m_log.info("call importHinemosProperty()");

		ImportHinemosPropertyResponse resDto = new ImportHinemosPropertyResponse();

		ImportHinemosPropertyController controller = new ImportHinemosPropertyController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * ファイルチェックのインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/fileCheck")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportFileCheck")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportFileCheckResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.Filecheck , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importFileCheck(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportFileCheck", content = @Content(schema = @Schema(implementation = ImportFileCheckRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown {
		ImportFileCheckRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportFileCheckRequest.class);

		m_log.info("call importFileCheck()");

		ImportFileCheckResponse resDto = new ImportFileCheckResponse();

		ImportFileCheckController controller = new ImportFileCheckController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * スケジュールのインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/schedule")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportSchedule")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportScheduleResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.Schedule , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement , modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importSchedule(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportSchedule", content = @Content(schema = @Schema(implementation = ImportScheduleRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown {
		ImportScheduleRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportScheduleRequest.class);

		m_log.info("call importSchedule()");

		ImportScheduleResponse resDto = new ImportScheduleResponse();

		ImportScheduleController controller = new ImportScheduleController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * マニュアル実行契機のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/jobManual")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportJobManual")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportJobManualResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.JobManual , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importJobManual(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportJobManual", content = @Content(schema = @Schema(implementation = ImportJobManualRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown {
		ImportJobManualRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportJobManualRequest.class);

		m_log.info("call importJobManual()");

		ImportJobManualResponse resDto = new ImportJobManualResponse();

		ImportJobManualController controller = new ImportJobManualController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * ジョブキュー(同時実行制御キュー)のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/jobQueue")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportJobQueue")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportJobQueueResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.JobQueue , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importJobQueue(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportJobQueue", content = @Content(schema = @Schema(implementation = ImportJobQueueRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown {
		ImportJobQueueRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportJobQueueRequest.class);

		m_log.info("call importJobQueue()");

		ImportJobQueueResponse resDto = new ImportJobQueueResponse();

		ImportJobQueueController controller = new ImportJobQueueController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * ジョブ連携受信のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/jobLinkRcv")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportJobLinkRcv")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportJobLinkRcvResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.JobLinkRcv , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importJobLinkRcv(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportJobLinkRcv", content = @Content(schema = @Schema(implementation = ImportJobLinkRcvRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown {
		ImportJobLinkRcvRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportJobLinkRcvRequest.class);

		m_log.info("call importJobLinkRcv()");

		ImportJobLinkRcvResponse resDto = new ImportJobLinkRcvResponse();

		ImportJobLinkRcvController controller = new ImportJobLinkRcvController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * ジョブ連携送信設定のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/jobLinkSend")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportJobLinkSend")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportJobLinkSendResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.Joblinksend , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.JobManagement, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importJobLinkSend(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportJobLinkSend", content = @Content(schema = @Schema(implementation = ImportJobLinkSendRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown {
		ImportJobLinkSendRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportJobLinkSendRequest.class);

		m_log.info("call importJobLinkSend()");

		ImportJobLinkSendResponse resDto = new ImportJobLinkSendResponse();

		ImportJobLinkSendController controller = new ImportJobLinkSendController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * 転送設定定義情報のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/transfer")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportTransfer")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportTransferResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.Transfer , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Hub, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importTransfer(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportTransfer", content = @Content(schema = @Schema(implementation = ImportTransferRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown {
		ImportTransferRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportTransferRequest.class);

		m_log.info("call importTransfer()");

		ImportTransferResponse resDto = new ImportTransferResponse();

		ImportTransferController controller = new ImportTransferController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * プラットフォームマスタのインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/platformMaster")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportPlatformMaster")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportPlatformMasterResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.PlatformMaster , type = LogType.UPDATE )
	@RestSystemAdminPrivilege(isNeed=true)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importPlatformMaster(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportPlatformMaster", content = @Content(schema = @Schema(implementation = ImportPlatformMasterRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown {
		ImportPlatformMasterRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportPlatformMasterRequest.class);

		m_log.info("call importPlatformMaster()");

		ImportPlatformMasterResponse resDto = new ImportPlatformMasterResponse();

		ImportPlatformMasterController controller = new ImportPlatformMasterController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * ノードマップのインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/nodeMapModel")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportNodeMapModel")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportNodeMapModelResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.NodeMapModel , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Repository, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importNodeMapModel(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportNodeMapModel", content = @Content(schema = @Schema(implementation = ImportNodeMapModelRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown {
		ImportNodeMapModelRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportNodeMapModelRequest.class);

		m_log.info("call importNodeMapModel()");

		ImportNodeMapModelResponse resDto = new ImportNodeMapModelResponse();

		ImportNodeMapModelController controller = new ImportNodeMapModelController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * JMXマスタのインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/jmxMaster")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportJmxMaster")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportJmxMasterResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.Jmxmaster , type = LogType.UPDATE )
	@RestSystemAdminPrivilege(isNeed=true)
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importJmxMaster(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportJmxMaster", content = @Content(schema = @Schema(implementation = ImportJmxMasterRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown {
		ImportJmxMasterRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportJmxMasterRequest.class);

		m_log.info("call importJmxMaster()");

		ImportJmxMasterResponse resDto = new ImportJmxMasterResponse();

		ImportJmxMasterController controller = new ImportJmxMasterController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	/**
	 * RESTアクセス情報のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/restAccess")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportRestAccessInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportRestAccessInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.RestAccessInfo, type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify , modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importRestAccessInfo(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "importRestAccessInfo", content = @Content(schema = @Schema(implementation = ImportRestAccessInfoRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown {
		ImportRestAccessInfoRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportRestAccessInfoRequest.class);

		m_log.info("call importRestAccessInfo()");

		ImportRestAccessInfoResponse resDto = new ImportRestAccessInfoResponse();

		ImportRestAccessInfoController controller = new ImportRestAccessInfoController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}

	/**
	 * RPAシナリオタグのインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/rpaScenarioTag")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportRpaScenarioTag")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportRpaScenarioTagResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.RpaScenarioTag , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importRpaScenarioTag(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportRpaScenarioTag", content = @Content(schema = @Schema(implementation = ImportRpaScenarioTagRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportRpaScenarioTagRequest dtoReq = 
				RestObjectMapperWrapper.convertJsonToObject(requestBody, ImportRpaScenarioTagRequest.class);

		m_log.info("call importRpaScenarioTag()");

		ImportRpaScenarioTagResponse resDto = new ImportRpaScenarioTagResponse();

		ImportRpaScenarioTagController controller = new ImportRpaScenarioTagController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * RPAシナリオ実績作成設定のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/rpaScenarioOperationResultCreateSetting")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportRpaScenarioOperationResultCreateSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportRpaScenarioOperationResultCreateSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.RpaScenarioResultCreate , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importRpaScenarioOperationResultCreateSetting(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportRpaScenarioOperationResultCreateSetting", content = @Content(schema = @Schema(implementation = ImportRpaScenarioOperationResultCreateSettingRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportRpaScenarioOperationResultCreateSettingRequest dtoReq = 
				RestObjectMapperWrapper.convertJsonToObject(requestBody, ImportRpaScenarioOperationResultCreateSettingRequest.class);

		m_log.info("call importRpaScenarioOperationResultCreateSetting()");

		ImportRpaScenarioOperationResultCreateSettingResponse resDto = new ImportRpaScenarioOperationResultCreateSettingResponse();

		ImportRpaScenarioOperationResultCreateSettingController controller = new ImportRpaScenarioOperationResultCreateSettingController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * RPA管理ツールアカウントのインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/rpaManagementToolAccount")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportRpaManagementToolAccount")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportRpaManagementToolAccountResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.RpaManagementTool , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importRpaManagementToolAccount(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportRpaManagementToolAccount", content = @Content(schema = @Schema(implementation = ImportRpaManagementToolAccountRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportRpaManagementToolAccountRequest dtoReq = 
				RestObjectMapperWrapper.convertJsonToObject(requestBody, ImportRpaManagementToolAccountRequest.class);

		m_log.info("call importRpaManagementToolAccount()");

		ImportRpaManagementToolAccountResponse resDto = new ImportRpaManagementToolAccountResponse();

		ImportRpaManagementToolAccountController controller = new ImportRpaManagementToolAccountController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * 自動化効果計算マスタのインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/rpaScenarioCoefficientPattern")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportRpaScenarioCoefficientPattern")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportRpaScenarioCoefficientPatternResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Modify, target = LogTarget.Master , type = LogType.UPDATE )
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Rpa, modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importRpaScenarioCoefficientPattern(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "ImportRpaScenarioCoefficientPattern", content = @Content(schema = @Schema(implementation = ImportRpaScenarioCoefficientPatternRequest.class))) String requestBody)
			throws InvalidUserPass, InvalidRole, HinemosUnknown, InvalidSetting {
		ImportRpaScenarioCoefficientPatternRequest dtoReq = 
				RestObjectMapperWrapper.convertJsonToObject(requestBody, ImportRpaScenarioCoefficientPatternRequest.class);

		m_log.info("call importRpaScenarioCoefficientPattern()");

		ImportRpaScenarioCoefficientPatternResponse resDto = new ImportRpaScenarioCoefficientPatternResponse();

		ImportRpaScenarioCoefficientPatternController controller = new ImportRpaScenarioCoefficientPatternController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * フィルタ設定（イベント監視履歴）情報のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/filterSettingMonitorHistoryEvent")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportFilterSettingMonitorHistoryEvent")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Add, target = LogTarget.FilterSetting, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify , modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importFilterSettingMonitorHistoryEvent(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "importFilterSettingMonitorHistoryEvent", content = @Content(schema = @Schema(implementation = ImportEventFilterSettingRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown {
		ImportEventFilterSettingRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportEventFilterSettingRequest.class);

		m_log.info("call importFilterSettingMonitorHistoryEvent()");

		ImportFilterSettingResponse resDto = new ImportFilterSettingResponse();

		ImportFilterSettingMonitorHistoryEventController controller = new ImportFilterSettingMonitorHistoryEventController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * フィルタ設定（ステータス監視履歴）情報のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/filterSettingMonitorHistoryStatus")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportFilterSettingMonitorHistoryStatus")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Add, target = LogTarget.FilterSetting, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify , modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importFilterSettingMonitorHistoryStatus(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "importFilterSettingMonitorHistoryStatus", content = @Content(schema = @Schema(implementation = ImportStatusFilterSettingRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown {
		ImportStatusFilterSettingRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportStatusFilterSettingRequest.class);

		m_log.info("call importFilterSettingMonitorHistoryStatus()");

		ImportFilterSettingResponse resDto = new ImportFilterSettingResponse();

		ImportFilterSettingMonitorHistoryStatusController controller = new ImportFilterSettingMonitorHistoryStatusController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
	
	/**
	 * フィルタ設定（ジョブ履歴）情報のインポートを行います。
	 * 	
	 * 個別のレコードに由来する例外は OKレスポンスの一部として結果返却し、メソッドの例外とはなりません。
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 * 
	 */
	@POST
	@Path("/import/filterSettingJobHistory")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ImportFilterSettingJobHistory")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ImportFilterSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@RestLog(action = LogAction.Add, target = LogTarget.FilterSetting, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.Notify , modeList = { SystemPrivilegeMode.ADD, SystemPrivilegeMode.MODIFY })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importFilterSettingJobHistory(@Context Request request,@Context UriInfo uriInfo,
			@RequestBody(description = "importFilterSettingJobHistory", content = @Content(schema = @Schema(implementation = ImportJobHistoryFilterSettingRequest.class))) String requestBody) 
					throws InvalidSetting, HinemosUnknown {
		ImportJobHistoryFilterSettingRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ImportJobHistoryFilterSettingRequest.class);

		m_log.info("call importFilterSettingJobHistory()");

		ImportFilterSettingResponse resDto = new ImportFilterSettingResponse();

		ImportFilterSettingJobHistoryController controller = new ImportFilterSettingJobHistoryController(dtoReq.isRollbackIfAbnormal(), dtoReq.getRecordList());
		controller.importExecute();

		resDto.setIsOccurException( controller.getOccurException());
		resDto.setResultList(controller.getResultList());

		RestLanguageConverter.convertMessages(resDto);
		return Response.status(Status.OK).entity(resDto).build();
	}
}