/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.sdml.agent;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_404;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeFunction;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.SdmlControlSettingNotFound;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.hinemosagent.bean.AgentOutputBasicInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreCommandline;
import com.clustercontrol.rest.endpoint.agent.dto.AgentInfoRequest;
import com.clustercontrol.rest.endpoint.sdml.agent.dto.AgtSdmlControlLogDTORequest;
import com.clustercontrol.rest.endpoint.sdml.agent.dto.AgtSdmlControlSettingInfoResponse;
import com.clustercontrol.rest.endpoint.sdml.agent.dto.ForwardSdmlControlLogRequest;
import com.clustercontrol.rest.endpoint.sdml.agent.dto.GetSdmlControlSettingForAgentResponse;
import com.clustercontrol.rest.endpoint.sdml.agent.dto.SendSdmlMessageRequest;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.session.RestControllerBean;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.sdml.model.SdmlControlSettingInfo;
import com.clustercontrol.sdml.v1.SdmlV1Option;
import com.clustercontrol.sdml.v1.bean.SdmlControlLogDTO;
import com.clustercontrol.sdml.v1.session.SdmlControllerBean;

/**
 * 本クラスのリソースメソッドには@Tag(name = "agentSdml")を付与すること。<br>
 * 上記により、エージェントではcom.clustercontrol.rest.client.AgentSdmlApiクラスからAPIにアクセスされる。
 * (本体側APIのDefaultApiクラスとは別名にする必要がある。)<br>
 * 
 * SDMLのバージョンアップ時にRestKindなどクライアントの本体側のjarに影響を与えないために、クラス名とクラスのPathを共通化する。<br>
 * バージョンごとに要にする必要があるAPIは各メソッドのPathにバージョンを付与すること。
 */
@Path("/agentSdml")
public class AgentSdmlRestEndpoints {
	private static Log logger = LogFactory.getLog(AgentSdmlRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "agentSdml";

	/**
	 * SDML制御設定を返します。
	 */
	@POST
	@Path("/v1/controlSetting")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetSdmlControlSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetSdmlControlSettingForAgentResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.READ })
	@IgnoreCommandline
	@Tag(name = "agentSdml")
	public Response getSdmlControlSettingV1(
			@RequestBody(description = "getSdmlControlSettingBody", content = @Content(schema = @Schema(implementation = AgentInfoRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown, SdmlControlSettingNotFound {
		logger.debug("getSdmlControlSettingV1() : Start.");

		AgentInfoRequest dto = RestObjectMapperWrapper.convertJsonToObject(requestBody, AgentInfoRequest.class);
		AgentInfo info = new AgentInfo();
		RestBeanUtil.convertBeanNoInvalid(dto, info);

		// 対象となるファシリティ一覧を取得
		ArrayList<String> facilityIdList = AgentConnectUtil.getFacilityIds(info);

		// ファシリティIDに対応した制御設定を取得
		GetSdmlControlSettingForAgentResponse res = new GetSdmlControlSettingForAgentResponse();
		res.setList(new ArrayList<>());
		SdmlControllerBean bean = new SdmlControllerBean();
		for (String facilityId : facilityIdList) {
			List<SdmlControlSettingInfo> infoList = bean.getSdmlControlSettingListForFacilityId(facilityId,
					SdmlV1Option.VERSION);
			for (SdmlControlSettingInfo infoRes : infoList) {
				AgtSdmlControlSettingInfoResponse dtoRes = new AgtSdmlControlSettingInfoResponse();
				RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
				res.getList().add(dtoRes);
			}
		}

		return Response.status(Status.OK).entity(res).build();
	}

	/**
	 * ログリーダが読み取ったSDML制御ログをコントローラに送信する。
	 */
	@POST
	@Path("/v1/controlLog")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ForwardSdmlControlLog")
	@APIResponses(value = { @APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	// @Produces(MediaType.APPLICATION_JSON) // レスポンスボディは空なので不要
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	@Tag(name = "agentSdml")
	public Response forwardSdmlControlLogV1(
			@RequestBody(description = "forwardSdmlControlLogBody", content = @Content(schema = @Schema(implementation = ForwardSdmlControlLogRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown {
		logger.debug("forwardSdmlControlLogV1() : Start.");

		// ---- 重複チェック
		boolean first = new RestControllerBean().registerRestAgentRequest("", "forwardSdmlControlLogV1");
		if (!first) {
			return Response.status(Status.OK).build();
		}

		ForwardSdmlControlLogRequest dto = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				ForwardSdmlControlLogRequest.class);
		AgentInfo agentInfo = new AgentInfo();

		RestBeanUtil.convertBean(dto.getAgentInfo(), agentInfo);
		List<SdmlControlLogDTO> resultList = new ArrayList<SdmlControlLogDTO>();
		for (AgtSdmlControlLogDTORequest dtoLog : dto.getLogList()) {
			SdmlControlLogDTO log = new SdmlControlLogDTO();
			RestBeanUtil.convertBean(dtoLog, log);
			resultList.add(log);
		}

		for (String facilityId : AgentConnectUtil.getFacilityIds(agentInfo)) {
			new SdmlControllerBean().run(facilityId, resultList);
		}

		return Response.status(Status.OK).build();
	}

	/**
	 * SDMLログリーダからの通知をSDML制御設定の通知設定で通知します。
	 */
	@POST
	@Path("/v1/notify_exec")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SendSdmlMessage")
	@APIResponses(value = { @APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	// @Produces(MediaType.APPLICATION_JSON) // レスポンスボディは空なので不要
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	@Tag(name = "agentSdml")
	public Response sendSdmlMessageV1(
			@RequestBody(description = "sendSdmlMessageBody", content = @Content(schema = @Schema(implementation = SendSdmlMessageRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown, FacilityNotFound {
		logger.debug("sendSdmlMessageV1() : Start.");

		// ---- 重複チェック
		boolean first = new RestControllerBean().registerRestAgentRequest("", "sendSdmlMessageV1");
		if (!first) {
			return Response.status(Status.OK).build();
		}

		SendSdmlMessageRequest dto = RestObjectMapperWrapper.convertJsonToObject(requestBody,
				SendSdmlMessageRequest.class);
		AgentOutputBasicInfo info = new AgentOutputBasicInfo();
		RestBeanUtil.convertBeanNoInvalid(dto, info);

		ArrayList<String> facilityIdList = AgentConnectUtil.getFacilityIds(info.getAgentInfo());
		if (facilityIdList == null || facilityIdList.size() == 0) {
			logger.info("sendSdmlMessageV1() : facilityId is null");
		} else if (facilityIdList.size() == 0) {
			logger.info("sendSdmlMessageV1() : facilityId.size() is 0");
		} else {
			new SdmlControllerBean().sendMessage(info.getOutputBasicInfo(), facilityIdList);
		}

		return Response.status(Status.OK).build();
	}
}
