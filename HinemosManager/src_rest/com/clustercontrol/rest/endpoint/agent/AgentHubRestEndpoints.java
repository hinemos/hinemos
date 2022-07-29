/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent;

import static com.clustercontrol.rest.RestConstant.STATUS_CODE_200;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_400;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_401;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_403;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_404;
import static com.clustercontrol.rest.RestConstant.STATUS_CODE_500;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeFunction;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.custom.bean.CommandResultDTO;
import com.clustercontrol.custom.session.MonitorCustomControllerBean;
import com.clustercontrol.fault.CustomInvalid;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.ValidAgentFacilityNotFound;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.logfile.bean.LogfileResultDTO;
import com.clustercontrol.logfile.session.MonitorLogfileControllerBean;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreCommandline;
import com.clustercontrol.rest.endpoint.agent.dto.AgtCloudLogResultDTORequest;
import com.clustercontrol.rest.endpoint.agent.dto.AgtCustomResultDTORequest;
import com.clustercontrol.rest.endpoint.agent.dto.AgtLogfileResultDTORequest;
import com.clustercontrol.rest.endpoint.agent.dto.AgtWinEventResultDTORequest;
import com.clustercontrol.rest.endpoint.agent.dto.ForwardCloudLogResultRequest;
import com.clustercontrol.rest.endpoint.agent.dto.ForwardCustomResultRequest;
import com.clustercontrol.rest.endpoint.agent.dto.ForwardLogfileResultRequest;
import com.clustercontrol.rest.endpoint.agent.dto.ForwardWinEventResultRequest;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.session.RestControllerBean;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.winevent.bean.WinEventResultDTO;
import com.clustercontrol.winevent.session.MonitorWinEventControllerBean;
import com.clustercontrol.xcloud.factory.monitors.CloudLogResultDTO;
import com.clustercontrol.xcloud.factory.monitors.MonitorCloudLogControllerBean;

@Path("/agentHub")
public class AgentHubRestEndpoints {

	private static Log m_log = LogFactory.getLog(AgentHubRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "agentHub";

	/**
	 * コマンド監視において、コマンドの実行結果をまとめてマネージャに通知する。
	 * ひとつの HTTP Requestで多数のコマンド実行結果を送信できるため、リソース観点から効率的に処理できる。
	 */
	@POST
	@Path("/customResult")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ForwardCustomResult")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	// @Produces(MediaType.APPLICATION_JSON) // レスポンスボディは空なので不要
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	// AgentHubConfigEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response forwardCustomResult(
			@RequestBody(description = "forwardCustomResultBody", content = @Content(schema = @Schema(implementation = ForwardCustomResultRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown, MonitorNotFound {
		m_log.debug("forwardCustomResult: Start.");

		// ---- 重複チェック
		boolean first = new RestControllerBean().registerRestAgentRequest("", "forwardCustomResult");
		if (!first) {
			return Response.status(Status.OK).build();
		}

		// ---- リクエスト解析
		ForwardCustomResultRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, ForwardCustomResultRequest.class);

		List<CommandResultDTO> resultList = new ArrayList<>();
		for (AgtCustomResultDTORequest src : req.getResultList()) {
			CommandResultDTO dst = new CommandResultDTO();
			convertCommandResultDTO(src, dst);
			resultList.add(dst);
		}

		// ---- 主処理
		try {
			new MonitorCustomControllerBean().evalCommandResult(resultList);
		} catch (CustomInvalid e) {
			throw new HinemosUnknown(e.getMessage(), e);
		}

		// ---- レスポンス構築
		return Response.status(Status.OK).build();
	}

	private void convertCommandResultDTO(AgtCustomResultDTORequest src, CommandResultDTO dst) throws HinemosUnknown, InvalidSetting {
		// まずはユーティリティでコピーできるものをコピーする
		RestBeanUtil.convertBean(src, dst);

		// 特殊変換 results (Map<String,String> to Map<String,Double|String>)
		dst.setResults(new HashMap<>());
		for (Entry<String, String> it : src.getResults().entrySet()) {
			switch (src.getType()) {
			case NUMBER:
				try {
					dst.getResults().put(it.getKey(), Double.valueOf(it.getValue()));
				} catch (NumberFormatException never) {
					m_log.error("forwardCustomResult: Illegal double result. key=" + it.getKey() + ", value=" + it.getValue());
				}
				break;
			case STRING:
				dst.getResults().put(it.getKey(), it.getValue());
				break;
			}
		}

		// 特殊変換 invalidLines
		dst.setInvalidLines(new HashMap<>());
		for (Entry<String, String> it : src.getInvalidLines().entrySet()) {
			try {
				dst.getInvalidLines().put(Integer.valueOf(it.getKey()), it.getValue());
			} catch (NumberFormatException never) {
				m_log.error("forwardCustomResult: Illegal invalid-lines key. value=" + it.getKey());
			}
		}
	}

	/**
	 * ログファイル監視でマッチしたものに対して通知をマネージャに依頼する。
	 * ひとつの HTTP Requestで多数の監視実行結果を送信できるため、リソース観点から効率的に処理できる。
	 */
	@POST
	@Path("/logfileResult")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ForwardLogfileResult")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	// @Produces(MediaType.APPLICATION_JSON) // レスポンスボディは空なので不要
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	// AgentHubEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response forwardLogfileResult(@RequestBody(description = "forwardLogfileResultBody", content = @Content(schema = @Schema(implementation = ForwardLogfileResultRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown {
		m_log.debug("forwardLogfileResult: Start.");

		// ---- 重複チェック
		boolean first = new RestControllerBean().registerRestAgentRequest("", "forwardLogfileResult");
		if (!first) {
			return Response.status(Status.OK).build();
		}

		ForwardLogfileResultRequest dto = RestObjectMapperWrapper.convertJsonToObject(requestBody, ForwardLogfileResultRequest.class);
		AgentInfo agentInfo = new AgentInfo();

		RestBeanUtil.convertBean(dto.getAgentInfo(), agentInfo);
		List<LogfileResultDTO> resultList = new ArrayList<LogfileResultDTO>();
		for (AgtLogfileResultDTORequest dtoResult : dto.getResultList()) {
			LogfileResultDTO infoResult = new LogfileResultDTO();
			RestBeanUtil.convertBean(dtoResult, infoResult);
			resultList.add(infoResult);
		}

		for (String facilityId : AgentConnectUtil.getFacilityIds(agentInfo)) {
			new MonitorLogfileControllerBean().run(facilityId, resultList);
		}

		return Response.status(Status.OK).build();
	}
	
	/**
	 * クラウドログ監視でマッチしたものに対して通知をマネージャに依頼する。
	 * ひとつの HTTP Requestで多数の監視実行結果を送信できるため、リソース観点から効率的に処理できる。
	 */
	@POST
	@Path("/cloudLogResult")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ForwardCloudLogResult")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	// @Produces(MediaType.APPLICATION_JSON) // レスポンスボディは空なので不要
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	public Response forwardCloudLogResult(@RequestBody(description = "forwardCloudLogResultBody", content = @Content(schema = @Schema(implementation = ForwardCloudLogResultRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown, ValidAgentFacilityNotFound {
		m_log.debug("forwardCloudLogeResult: Start.");

		// ---- 重複チェック
		boolean first = new RestControllerBean().registerRestAgentRequest("", "forwardCloudLogResult");
		if (!first) {
			return Response.status(Status.OK).build();
		}

		ForwardCloudLogResultRequest dto = RestObjectMapperWrapper.convertJsonToObject(requestBody, ForwardCloudLogResultRequest.class);
		AgentInfo agentInfo = new AgentInfo();

		RestBeanUtil.convertBean(dto.getAgentInfo(), agentInfo);
		
		// マネージャ起動直後にエージェントが認識されていないと
		// クラウドログ優先度の判断を誤る可能性があるので
		// エージェントが認識されているかチェック
		// 認識されていない場合は、エクセプションを投げて認識されるまでリトライさせる
		for (String facilityId : AgentConnectUtil.getFacilityIds(agentInfo)) {
			if (!AgentConnectUtil.isValidAgent(facilityId)) {
				new RestControllerBean().deleteRestAgentRequest();
				throw new ValidAgentFacilityNotFound("Agent not registered yet");
			}
		}
		
		List<CloudLogResultDTO> resultList = new ArrayList<CloudLogResultDTO>();
		for (AgtCloudLogResultDTORequest dtoResult : dto.getResultList()) {
			CloudLogResultDTO infoResult = new CloudLogResultDTO();
			RestBeanUtil.convertBean(dtoResult, infoResult);
			resultList.add(infoResult);
		}

		for (String facilityId : AgentConnectUtil.getFacilityIds(agentInfo)) {
			new MonitorCloudLogControllerBean().run(facilityId, resultList);
		}

		return Response.status(Status.OK).build();
	}

	/**
	 * Windowsイベント監視でマッチしたものに対して通知をマネージャに依頼する。
	 * ひとつの HTTP Requestで多数の監視実行結果を送信できるため、リソース観点から効率的に処理できる。
	 */
	@POST
	@Path("/wineventResult")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ForwardWinEventResult")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	// @Produces(MediaType.APPLICATION_JSON) // レスポンスボディは空なので不要
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	// AgentHubConfigEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response forwardWinEventResult(
			@RequestBody(description = "forwardWinEventResultBody", content = @Content(schema = @Schema(implementation = ForwardWinEventResultRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown {
		m_log.debug("forwardWinEventResult: Start.");

		// ---- 重複チェック
		boolean first = new RestControllerBean().registerRestAgentRequest("", "forwardCustomResult");
		if (!first) {
			return Response.status(Status.OK).build();
		}

		// ---- リクエスト解析
		ForwardWinEventResultRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, ForwardWinEventResultRequest.class);

		List<WinEventResultDTO> resultList = new ArrayList<>();
		for (AgtWinEventResultDTORequest src : req.getResultList()) {
			WinEventResultDTO dst = new WinEventResultDTO();
			RestBeanUtil.convertBean(src, dst);
			resultList.add(dst);
		}

		AgentInfo agentInfo = new AgentInfo();
		RestBeanUtil.convertBean(req.getAgentInfo(), agentInfo);

		// ---- 主処理
		for (String facilityId : AgentConnectUtil.getFacilityIds(agentInfo)) {
			new MonitorWinEventControllerBean().run(facilityId, resultList);
		}

		// ---- レスポンス構築
		return Response.status(Status.OK).build();
	}
}
