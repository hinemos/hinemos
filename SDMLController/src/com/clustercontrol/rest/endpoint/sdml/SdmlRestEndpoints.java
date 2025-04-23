/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.sdml;

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
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.glassfish.grizzly.http.server.Request;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeFunction;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.SdmlControlSettingDuplicate;
import com.clustercontrol.fault.SdmlControlSettingNotFound;
import com.clustercontrol.rest.annotation.RestLog;
import com.clustercontrol.rest.annotation.RestLog.LogAction;
import com.clustercontrol.rest.annotation.RestLog.LogTarget;
import com.clustercontrol.rest.annotation.RestLog.LogType;
import com.clustercontrol.rest.annotation.RestLogFunc;
import com.clustercontrol.rest.annotation.RestLogFunc.LogFuncName;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.ArrayTypeParam;
import com.clustercontrol.rest.endpoint.sdml.dto.AddSdmlControlSettingRequest;
import com.clustercontrol.rest.endpoint.sdml.dto.GetSdmlControlSettingListRequest;
import com.clustercontrol.rest.endpoint.sdml.dto.ModifySdmlControlSettingRequest;
import com.clustercontrol.rest.endpoint.sdml.dto.SdmlControlSettingInfoResponse;
import com.clustercontrol.rest.endpoint.sdml.dto.SdmlMonitorTypeMasterResponse;
import com.clustercontrol.rest.endpoint.sdml.dto.SetSdmlControlSettingStatusRequest;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestCommonValitater;
import com.clustercontrol.rest.util.RestLanguageConverter;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.sdml.bean.SdmlControlSettingFilterInfo;
import com.clustercontrol.sdml.model.SdmlControlSettingInfo;
import com.clustercontrol.sdml.model.SdmlMonitorTypeMasterInfo;
import com.clustercontrol.sdml.v1.SdmlV1Option;
import com.clustercontrol.sdml.v1.session.SdmlControllerBean;

/**
 * 本クラスのリソースメソッドには@Tag(name = "sdml")を付与すること。<br>
 * 上記を付与することにより、クライアントではcom.clustercontrol.rest.client.SdmlApiクラスからAPIにアクセスされる。
 * (本体側APIのDefaultApiクラスとは別名にする必要がある。)<br>
 * 
 * SDMLのバージョンアップ時にRestKindなどクライアントの本体側のjarに影響を与えないために、クラス名とクラスのPathを共通化する。<br>
 * バージョンごとに用意する必要があるAPIは各メソッドのPathにバージョンを付与すること。
 */
@Path("/sdml")
@RestLogFunc(name = LogFuncName.Sdml)
public class SdmlRestEndpoints {
	private static Log logger = LogFactory.getLog(SdmlRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "sdml";

	/**
	 * SDML制御設定の追加を行うAPI
	 */
	@POST
	@Path("/v1/controlSetting")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "AddSdmlControlSettingV1")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SdmlControlSettingInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_409, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Add, target = LogTarget.controlSetting, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.SdmlSetting, modeList = { SystemPrivilegeMode.ADD,
			SystemPrivilegeMode.READ })
	@Tag(name = "sdml")
	public Response addSdmlControlSettingV1(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "addSdmlControlSettingBody", content = @Content(schema = @Schema(implementation = AddSdmlControlSettingRequest.class))) String requestBody)
			throws InvalidSetting, InvalidUserPass, InvalidRole, SdmlControlSettingDuplicate, HinemosUnknown {
		logger.info("call addSdmlControlSettingV1()");

		AddSdmlControlSettingRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				AddSdmlControlSettingRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		SdmlControlSettingInfo infoReq = new SdmlControlSettingInfo(dtoReq.getApplicationId());
		RestBeanUtil.convertBean(dtoReq, infoReq);

		// このタイミングでバージョンを設定する
		infoReq.setVersion(SdmlV1Option.VERSION);
		SdmlControlSettingInfo infoRes = new SdmlControllerBean().addSdmlControlSetting(infoReq, false);

		SdmlControlSettingInfoResponse dtoRes = new SdmlControlSettingInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * SDML制御設定の変更を行うAPI
	 */
	@PUT
	@Path("/v1/controlSetting/{applicationId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ModifySdmlControlSettingV1")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SdmlControlSettingInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.controlSetting, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.SdmlSetting, modeList = { SystemPrivilegeMode.MODIFY,
			SystemPrivilegeMode.READ })
	@Tag(name = "sdml")
	public Response modifySdmlControlSettingV1(@PathParam(value = "applicationId") String applicationId,
			@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "modifySdmlControlSettingBody", content = @Content(schema = @Schema(implementation = ModifySdmlControlSettingRequest.class))) String requestBody)
			throws InvalidSetting, InvalidUserPass, InvalidRole, SdmlControlSettingNotFound, HinemosUnknown {
		logger.info("call modifySdmlControlSettingV1()");

		ModifySdmlControlSettingRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ModifySdmlControlSettingRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck(applicationId);

		SdmlControlSettingInfo infoReq = new SdmlControlSettingInfo(applicationId);
		RestBeanUtil.convertBean(dtoReq, infoReq);

		SdmlControlSettingInfo infoRes = new SdmlControllerBean().modifySdmlControlSetting(infoReq, false);

		SdmlControlSettingInfoResponse dtoRes = new SdmlControlSettingInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * SDML制御設定の削除を行うAPI
	 */
	@DELETE
	@Path("/v1/controlSetting")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteSdmlControlSettingV1")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SdmlControlSettingInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Delete, target = LogTarget.controlSetting, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.SdmlSetting, modeList = { SystemPrivilegeMode.MODIFY,
			SystemPrivilegeMode.READ })
	@Tag(name = "sdml")
	public Response deleteSdmlControlSettingV1(
			@ArrayTypeParam @QueryParam(value = "applicationIds") String applicationIds, @Context Request request,
			@Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, SdmlControlSettingNotFound, HinemosUnknown {
		logger.info("call deleteSdmlControlSettingV1()");

		String[] applicationIdArray = applicationIds.split(",");

		List<SdmlControlSettingInfo> infoResList = new SdmlControllerBean()
				.deleteSdmlControlSetting(applicationIdArray);

		List<SdmlControlSettingInfoResponse> dtoResList = new ArrayList<SdmlControlSettingInfoResponse>();
		for (SdmlControlSettingInfo info : infoResList) {
			SdmlControlSettingInfoResponse dto = new SdmlControlSettingInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 指定したSDML制御設定の取得を行うAPI
	 */
	@GET
	@Path("/v1/controlSetting/{applicationId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetSdmlControlSettingV1")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SdmlControlSettingInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.controlSetting, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.SdmlSetting, modeList = { SystemPrivilegeMode.READ })
	@Tag(name = "sdml")
	public Response getSdmlControlSettingV1(@PathParam(value = "applicationId") String applicationId,
			@Context Request request, @Context UriInfo uriInfo)
			throws InvalidUserPass, InvalidRole, SdmlControlSettingNotFound, HinemosUnknown {
		logger.info("call getSdmlControlSettingV1()");

		SdmlControlSettingInfo infoRes = new SdmlControllerBean().getSdmlControlSetting(applicationId);

		SdmlControlSettingInfoResponse dtoRes = new SdmlControlSettingInfoResponse();
		RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
		RestLanguageConverter.convertMessages(dtoRes);

		return Response.status(Status.OK).entity(dtoRes).build();
	}

	/**
	 * SDML制御設定の一覧の取得を行うAPI
	 */
	@GET
	@Path("/v1/controlSetting")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetSdmlControlSettingListV1")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SdmlControlSettingInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.controlSetting, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.SdmlSetting, modeList = { SystemPrivilegeMode.READ })
	@Tag(name = "sdml")
	public Response getSdmlControlSettingListV1(@QueryParam(value = "ownerRoleId") String ownerRoleId,
			@Context Request request, @Context UriInfo uriInfo) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		logger.info("call getSdmlControlSettingListV1()");

		// カレントユーザがオーナーロールに所属しているかチェックする
		CommonValidator.validateCurrentUserBelongRole(ownerRoleId);
		
		// バージョンを指定して取得する
		List<SdmlControlSettingInfo> infoResList = new SdmlControllerBean()
				.getSdmlControlSettingList(SdmlV1Option.VERSION, ownerRoleId);

		List<SdmlControlSettingInfoResponse> dtoResList = new ArrayList<SdmlControlSettingInfoResponse>();
		for (SdmlControlSettingInfo info : infoResList) {
			SdmlControlSettingInfoResponse dto = new SdmlControlSettingInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 条件に従いSDML制御設定の一覧の取得を行うAPI
	 */
	@POST
	@Path("/v1/controlSetting_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetSdmlControlSettingListByConditionV1")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SdmlControlSettingInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.controlSetting, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.SdmlSetting, modeList = { SystemPrivilegeMode.READ })
	@Tag(name = "sdml")
	public Response getSdmlControlSettingListByConditionV1(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "getSdmlControlSettingListByConditionBody", content = @Content(schema = @Schema(implementation = GetSdmlControlSettingListRequest.class))) String requestBody)
			throws InvalidSetting, InvalidUserPass, InvalidRole, HinemosUnknown {
		logger.info("call getSdmlControlSettingListByConditionV1()");

		GetSdmlControlSettingListRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				GetSdmlControlSettingListRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		List<SdmlControlSettingInfo> infoResList = new ArrayList<>();
		if (dtoReq.getSdmlControlSettingFilterInfo() != null) {
			SdmlControlSettingFilterInfo infoReq = new SdmlControlSettingFilterInfo();
			RestBeanUtil.convertBean(dtoReq.getSdmlControlSettingFilterInfo(), infoReq);
			infoResList.addAll(new SdmlControllerBean().getSdmlControlSettingList(infoReq, SdmlV1Option.VERSION));
		}

		List<SdmlControlSettingInfoResponse> dtoResList = new ArrayList<SdmlControlSettingInfoResponse>();
		for (SdmlControlSettingInfo info : infoResList) {
			SdmlControlSettingInfoResponse dto = new SdmlControlSettingInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 指定したSDML制御設定の有効化/無効化を行うAPI
	 */
	@PUT
	@Path("/v1/controlSetting_valid")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SetSdmlControlSettingStatusV1")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SdmlControlSettingInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.controlSetting, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.SdmlSetting, modeList = { SystemPrivilegeMode.MODIFY,
			SystemPrivilegeMode.READ })
	@Tag(name = "sdml")
	public Response setSdmlControlSettingStatusV1(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "setSdmlControlSettingStatusBody", content = @Content(schema = @Schema(implementation = SetSdmlControlSettingStatusRequest.class))) String requestBody)
			throws InvalidSetting, InvalidUserPass, InvalidRole, SdmlControlSettingNotFound, HinemosUnknown {
		logger.info("call setSdmlControlSettingStatusV1()");

		SetSdmlControlSettingStatusRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				SetSdmlControlSettingStatusRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		List<SdmlControlSettingInfo> infoResList = new SdmlControllerBean()
				.setSdmlControlSettingStatus(dtoReq.getApplicationIds(), dtoReq.getValidFlg());

		List<SdmlControlSettingInfoResponse> dtoResList = new ArrayList<SdmlControlSettingInfoResponse>();
		for (SdmlControlSettingInfo info : infoResList) {
			SdmlControlSettingInfoResponse dto = new SdmlControlSettingInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * 指定したSDML制御設定のSDML制御ログ収集の有効化/無効化を行うAPI
	 */
	@PUT
	@Path("/v1/controlSetting_logCollectorValid")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SetSdmlControlSettingLogCollectorV1")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SdmlControlSettingInfoResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Modify, target = LogTarget.controlSetting, type = LogType.UPDATE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.SdmlSetting, modeList = { SystemPrivilegeMode.MODIFY,
			SystemPrivilegeMode.READ })
	@Tag(name = "sdml")
	public Response setSdmlControlSettingLogCollectorV1(@Context Request request, @Context UriInfo uriInfo,
			@RequestBody(description = "setSdmlControlSettingLogCollectorBody", content = @Content(schema = @Schema(implementation = SetSdmlControlSettingStatusRequest.class))) String requestBody)
			throws InvalidSetting, InvalidUserPass, InvalidRole, SdmlControlSettingNotFound, HinemosUnknown {
		logger.info("call setSdmlControlSettingLogCollectorV1()");

		SetSdmlControlSettingStatusRequest dtoReq = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				SetSdmlControlSettingStatusRequest.class);
		RestCommonValitater.checkRequestDto(dtoReq);
		dtoReq.correlationCheck();

		List<SdmlControlSettingInfo> infoResList = new SdmlControllerBean()
				.setSdmlControlSettingLogCollector(dtoReq.getApplicationIds(), dtoReq.getValidFlg());

		List<SdmlControlSettingInfoResponse> dtoResList = new ArrayList<SdmlControlSettingInfoResponse>();
		for (SdmlControlSettingInfo info : infoResList) {
			SdmlControlSettingInfoResponse dto = new SdmlControlSettingInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}

	/**
	 * SDML監視種別マスタの取得を行うAPI
	 */
	@GET
	@Path("/sdmlMonitorTypeMaster")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetSdmlMonitorTypeMaster")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SdmlMonitorTypeMasterResponse.class, type = SchemaType.ARRAY)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@RestLog(action = LogAction.Get, target = LogTarget.SdmlMonitorTypeMaster, type = LogType.REFERENCE)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.SdmlSetting, modeList = { SystemPrivilegeMode.READ })
	@Tag(name = "sdml")
	public Response getSdmlMonitorTypeMasterList() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		logger.info("call getSdmlMonitorTypeMasterList()");

		List<SdmlMonitorTypeMasterInfo> infoResList = new SdmlControllerBean().getSdmlMonitorTypeMstList();

		List<SdmlMonitorTypeMasterResponse> dtoResList = new ArrayList<SdmlMonitorTypeMasterResponse>();
		for (SdmlMonitorTypeMasterInfo info : infoResList) {
			SdmlMonitorTypeMasterResponse dto = new SdmlMonitorTypeMasterResponse();
			RestBeanUtil.convertBeanNoInvalid(info, dto);
			dtoResList.add(dto);
		}
		RestLanguageConverter.convertMessages(dtoResList);

		return Response.status(Status.OK).entity(dtoResList).build();
	}
}
