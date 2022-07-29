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

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeFunction;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.binary.bean.BinaryResultDTO;
import com.clustercontrol.binary.session.BinaryControllerBean;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreCommandline;
import com.clustercontrol.rest.endpoint.agent.dto.AgentInfoRequest;
import com.clustercontrol.rest.endpoint.agent.dto.AgtBinaryResultDTORequest;
import com.clustercontrol.rest.endpoint.agent.dto.AgtMonitorInfoResponse;
import com.clustercontrol.rest.endpoint.agent.dto.ForwardBinaryResultRequest;
import com.clustercontrol.rest.endpoint.agent.dto.GetMonitorForAgentResponse;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.session.RestControllerBean;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;

@Path("/agentBinary")
public class AgentBinaryRestEndpoints {

	private static Log m_log = LogFactory.getLog(AgentBinaryRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "agentBinary";

	/**
	 * バイナリ監視の監視設定を取得
	 */
	@POST
	@Path("monitorsetting/binary_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMonitorBinary")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetMonitorForAgentResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	// AgentBinaryEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response getMonitorBinary(
			@RequestBody(description = "getMonitorBinaryBody", content = @Content(schema = @Schema(implementation = AgentInfoRequest.class))) String requestBody)
			throws InvalidSetting, MonitorNotFound, HinemosUnknown {
		m_log.debug("getMonitorBinary: Start.");

		// ---- リクエスト解析
		AgentInfoRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, AgentInfoRequest.class);
		AgentInfo agentInfo = new AgentInfo();
		RestBeanUtil.convertBean(req, agentInfo);

		// ---- 主処理
		ArrayList<String> facilityIdList = AgentConnectUtil.getFacilityIds(agentInfo);

		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		BinaryControllerBean bean = new BinaryControllerBean();
		for (String facilityId : facilityIdList) {
			list.addAll(bean.getBinaryListForFacilityId(facilityId, true));
		}

		// ---- レスポンス構築
		GetMonitorForAgentResponse res = new GetMonitorForAgentResponse();
		res.setList(new ArrayList<>());
		for (MonitorInfo src : list) {
			AgtMonitorInfoResponse dst = new AgtMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(src, dst);
			res.getList().add(dst);
		}

		return Response.status(Status.OK).entity(res).build();
	}

	/**
	 * バイナリ監視でマッチしたものに対して通知をマネージャに依頼する。
	 * ひとつの HTTP Request で多数の監視実行結果を送信できるため、リソース観点から効率的に処理できる。
	 */
	@POST
	@Path("binaryResult")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "ForwardBinaryResult")
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
	// AgentBinaryEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response forwardBinaryResult(
			@RequestBody(description = "forwardBinaryResultBody", content = @Content(schema = @Schema(implementation = ForwardBinaryResultRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown {
		m_log.debug("forwardBinaryResult: Start.");

		// ---- 重複チェック
		boolean first = new RestControllerBean().registerRestAgentRequest("", "forwardBinaryResult");
		if (!first) {
			return Response.status(Status.OK).build();
		}

		// ---- リクエスト解析
		ForwardBinaryResultRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, ForwardBinaryResultRequest.class);

		List<BinaryResultDTO> resultList = new ArrayList<>();
		for (AgtBinaryResultDTORequest src : req.getResultList()) {
			BinaryResultDTO dst = new BinaryResultDTO();
			RestBeanUtil.convertBean(src, dst);
			resultList.add(dst);
		}

		AgentInfo agentInfo = new AgentInfo();
		RestBeanUtil.convertBean(req.getAgentInfo(), agentInfo);

		// ---- 主処理
		// ファシリティIDを取得(Agentから送られた場合は1件、Agentで設定されていない場合はMangerから全件取得).
		ArrayList<String> facilityIdList = AgentConnectUtil.getFacilityIds(agentInfo);
		m_log.info("forwardBinaryResult: " + makeLog(facilityIdList));

		for (String facilityId : facilityIdList) {
			new BinaryControllerBean().run(facilityId, resultList);
		}

		// ---- レスポンス構築
		return Response.status(Status.OK).build();
	}

	/**
	 * ファシリティIDのリストをログ出力用文字列に整形して返します。
	 */
	private static String makeLog(List<String> facilityIdList) {
		return String.format("facilityId (%d) [%s]",
				facilityIdList.size(),
				String.join(",", facilityIdList));
	}

}
