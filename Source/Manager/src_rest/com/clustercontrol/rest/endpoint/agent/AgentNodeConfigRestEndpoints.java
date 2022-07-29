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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
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
import org.glassfish.grizzly.http.server.Request;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeFunction;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.NodeConfigSettingDuplicate;
import com.clustercontrol.fault.NodeConfigSettingNotFound;
import com.clustercontrol.fault.NodeHistoryRegistered;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.repository.bean.AutoRegisterResult;
import com.clustercontrol.repository.bean.NodeConfigRunCollectInfo;
import com.clustercontrol.repository.bean.NodeConfigSetting;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.session.AutoRegisterNodeControllerBean;
import com.clustercontrol.repository.session.NodeConfigSettingControllerBean;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreCommandline;
import com.clustercontrol.rest.endpoint.agent.dto.AgentInfoRequest;
import com.clustercontrol.rest.endpoint.agent.dto.AgtNodeConfigRunCollectInfoResponse;
import com.clustercontrol.rest.endpoint.agent.dto.AgtNodeConfigSettingResponse;
import com.clustercontrol.rest.endpoint.agent.dto.AgtNodeInfoResponse;
import com.clustercontrol.rest.endpoint.agent.dto.AgtNodeNetworkInterfaceInfoRequest;
import com.clustercontrol.rest.endpoint.agent.dto.GetNodeConfigSettingResponse;
import com.clustercontrol.rest.endpoint.agent.dto.GetNodeInfoListResponse;
import com.clustercontrol.rest.endpoint.agent.dto.RegisterNodeConfigInfoRequest;
import com.clustercontrol.rest.endpoint.agent.dto.RegisterNodeRequest;
import com.clustercontrol.rest.endpoint.agent.dto.RegisterNodeResponse;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.session.RestControllerBean;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.util.HinemosTime;

@Path("/agentNodeConfig")
public class AgentNodeConfigRestEndpoints {

	private static Log m_log = LogFactory.getLog(AgentNodeConfigRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "agentNodeConfig";

	/**
	 * ノード自動登録。
	 */
	@POST
	@Path("/repository/node")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "RegisterNode")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = RegisterNodeResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	// AgentNodeConfigEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response registerNode(
			@RequestBody(
					description = "registerNodeBody",
					content = @Content(schema = @Schema(implementation = RegisterNodeRequest.class))) String requestBody,
			@Context Request serverRequest)
			throws InvalidSetting, HinemosUnknown, HinemosDbTimeout {
		m_log.debug("registerNode: Start.");

		// ---- リクエスト解析
		RegisterNodeRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, RegisterNodeRequest.class);

		String platform = req.getPlatform();

		List<NodeNetworkInterfaceInfo> nodeNifList = new ArrayList<>();
		for (AgtNodeNetworkInterfaceInfoRequest src : req.getNodeNifList()) {
			NodeNetworkInterfaceInfo dst = new NodeNetworkInterfaceInfo();
			RestBeanUtil.convertBean(src, dst);
			nodeNifList.add(dst);
		}

		// ---- 主処理
		// 引数で渡されたMACアドレスをデバッグログ出力・性能測定用に時間測定.
		long startTime = 0;
		String forLogAddress = "";
		if (nodeNifList != null && !nodeNifList.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (NodeNetworkInterfaceInfo nif : nodeNifList) {
				if (nif.getDeviceIndex() == null) {
					sb.append("Index.null[");
				} else {
					sb.append("Index." + nif.getDeviceIndex().toString() + "[");
				}
				if (nif.getNicMacAddress() == null) {
					sb.append("null");
				} else {
					sb.append(nif.getNicMacAddress());
				}
				sb.append("]");
			}
			forLogAddress = sb.toString();
		} else {
			forLogAddress = "empty";
		}
		if (m_log.isDebugEnabled()) {
			startTime = HinemosTime.currentTimeMillis();
			m_log.debug(String.format("registerNode() : start. MAC addresses=[%s]", forLogAddress));
		}

		// ソースIPを取得する.
		InetAddress sourceIpAddress = null;
		String remoteAddr = serverRequest.getRemoteAddr();
		if (remoteAddr != null && remoteAddr.trim().length() > 0) {
			try {
				sourceIpAddress = InetAddress.getByName(remoteAddr);
			} catch (UnknownHostException ignore) {
				// sourceIpAddress が null のまま以降の処理へ
			}
		}

		// 自動登録処理.
		AutoRegisterResult result = AutoRegisterNodeControllerBean.autoRegister(platform, nodeNifList, forLogAddress, sourceIpAddress);

		// 性能測定用にManager処理時間を出力.
		if (m_log.isDebugEnabled()) {
			long endTime = HinemosTime.currentTimeMillis();
			long millis = endTime - startTime;
			m_log.debug(String.format("registerNode() : end. MAC addresses=[%s], processing-time=%dms", forLogAddress, millis));
		}

		// ---- レスポンス構築
		RegisterNodeResponse res = new RegisterNodeResponse();
		res.setFacilityId(result.getFacilityId());
		res.setResultStatus(result.getResultStatus());

		return Response.status(Status.OK).entity(res).build();
	}

	/**
	 * 対象構成情報取得。
	 */
	@POST
	@Path("/repository/nodeConfig_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetNodeConfigSetting")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetNodeConfigSettingResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.READ })
	@IgnoreCommandline
	// AgentNodeConfigEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response getNodeConfigSetting(
			@RequestBody(description = "getNodeConfigSettingBody", content = @Content(schema = @Schema(implementation = AgentInfoRequest.class))) String requestBody)
			throws InvalidSetting, InvalidRole, HinemosUnknown, FacilityNotFound, NodeConfigSettingNotFound {
		m_log.debug("getNodeConfigSetting: Start.");

		// ---- リクエスト解析
		AgentInfoRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, AgentInfoRequest.class);
		AgentInfo agentInfo = new AgentInfo();
		RestBeanUtil.convertBean(req, agentInfo);

		// ---- 主処理
		// ファシリティIDを取得(Agentから送られた場合は1件、Agentで設定されていない場合はMangerから全件取得).
		ArrayList<String> facilityIdList = AgentConnectUtil.getEffectiveFacilityIds(agentInfo);
		m_log.debug("getNodeConfigSetting: " + makeLog(facilityIdList));

		List<NodeConfigSetting> result = new NodeConfigSettingControllerBean().getNodeConfigSettingListByFacilityIds(facilityIdList);

		// ---- レスポンス構築
		GetNodeConfigSettingResponse res = new GetNodeConfigSettingResponse();
		res.setList(new ArrayList<>());
		for (NodeConfigSetting src : result) {
			AgtNodeConfigSettingResponse dst = new AgtNodeConfigSettingResponse();
			RestBeanUtil.convertBeanNoInvalid(src, dst);
			res.getList().add(dst);
		}

		return Response.status(Status.OK).entity(res).build();
	}

	/**
	 * 構成情報登録。
	 */
	@PUT
	@Path("/repository/node")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "RegisterNodeConfigInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	//@Produces(MediaType.APPLICATION_JSON) // レスポンスボディは空なので不要
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	// AgentNodeConfigEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response registerNodeConfigInfo(
			@RequestBody(description = "registerNodeConfigInfoBody", content = @Content(schema = @Schema(implementation = RegisterNodeConfigInfoRequest.class))) String requestBody)
			throws FacilityNotFound, NodeConfigSettingNotFound, NodeConfigSettingDuplicate, HinemosUnknown,
			InvalidSetting, InvalidRole, NodeHistoryRegistered {
		m_log.debug("registerNodeConfigInfo: Start.");

		// ---- 重複チェック
		boolean first = new RestControllerBean().registerRestAgentRequest("", "registerNodeConfigInfo");
		if (!first) {
			return Response.status(Status.OK).build();
		}

		// ---- リクエスト解析
		RegisterNodeConfigInfoRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, RegisterNodeConfigInfoRequest.class);

		Long registerDatetime = req.getRegisterDatetime();

		NodeInfo nodeInfo = new NodeInfo();
		RestBeanUtil.convertBean(req.getNodeInfo(), nodeInfo);

		// ---- 主処理
		String settingId = "";
		String facilityId = "";
		if (nodeInfo != null) {
			if (nodeInfo.getNodeConfigSettingId() != null) {
				settingId = nodeInfo.getNodeConfigSettingId();
			}
			if (nodeInfo.getFacilityId() != null) {
				facilityId = nodeInfo.getFacilityId();
			}
		}
		m_log.info("registerNodeConfigInfo: " + String.format("registerDatetime=%d, settingId=%s, facilityId=%s", registerDatetime, settingId, facilityId));
		new NodeConfigSettingControllerBean().registerNodeConfigInfo(registerDatetime, nodeInfo);

		return Response.status(Status.OK).build();
	}

	/**
	 * 即時実行情報の取得。
	 */
	@POST
	@Path("/repository/nodeConfig_collectInfo_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetNodeConfigRunCollectInfo")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AgtNodeConfigRunCollectInfoResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.READ })
	@IgnoreCommandline
	// AgentNodeConfigEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response getNodeConfigRunCollectInfo(
			@RequestBody(description = "getNodeConfigRunCollectInfoBody", content = @Content(schema = @Schema(implementation = AgentInfoRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown, FacilityNotFound {
		m_log.debug("getNodeConfigRunCollectInfo: Start.");

		// ---- リクエスト解析
		AgentInfoRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, AgentInfoRequest.class);
		AgentInfo agentInfo = new AgentInfo();
		RestBeanUtil.convertBean(req, agentInfo);

		// ---- 主処理
		// ファシリティIDを取得(Agentから送られた場合は1件、Agentで設定されていない場合はMangerから全件取得).
		ArrayList<String> facilityIdList = AgentConnectUtil.getEffectiveFacilityIds(agentInfo);
		m_log.debug("getNodeConfigRunCollectInfo: " + makeLog(facilityIdList));

		NodeConfigRunCollectInfo result = new NodeConfigSettingControllerBean().getNodeConfigRunCollectInfo(facilityIdList);

		// ---- レスポンス構築
		AgtNodeConfigRunCollectInfoResponse res = new AgtNodeConfigRunCollectInfoResponse();
		if (result != null) {
			res.setLoadDistributionTime(result.getLoadDistributionTime());

			// キーがオブジェクトの Map を、2 つの List へ展開する
			res.setInstructedInfoMapKeys(new ArrayList<>());
			res.setInstructedInfoMapValues(new ArrayList<>());
			for (Entry<NodeConfigSetting, Long> src : result.getInstructedInfoMap().entrySet()) {
				AgtNodeConfigSettingResponse dstKey = new AgtNodeConfigSettingResponse();
				RestBeanUtil.convertBean(src.getKey(), dstKey);

				res.getInstructedInfoMapKeys().add(dstKey);
				res.getInstructedInfoMapValues().add(src.getValue());
			}
		}

		return Response.status(Status.OK).entity(res).build();
	}

	/**
	 * 即時実行を中止。
	 */
	@POST
	@Path("/repository/nodeConfig_stop")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "StopNodeConfigRunCollect")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	//@Produces(MediaType.APPLICATION_JSON) // レスポンスボディは空なので不要
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.READ })
	@IgnoreCommandline
	// AgentNodeConfigEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response stopNodeConfigRunCollect(
			@RequestBody(description = "stopNodeConfigRunCollectBody", content = @Content(schema = @Schema(implementation = AgentInfoRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown, FacilityNotFound {
		m_log.debug("stopNodeConfigRunCollect: Start.");

		// ---- リクエスト解析
		AgentInfoRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, AgentInfoRequest.class);
		AgentInfo agentInfo = new AgentInfo();
		RestBeanUtil.convertBean(req, agentInfo);

		// ---- 主処理
		// ファシリティIDを取得(Agentから送られた場合は1件、Agentで設定されていない場合はMangerから全件取得).
		ArrayList<String> facilityIdList = AgentConnectUtil.getEffectiveFacilityIds(agentInfo);
		m_log.debug("stopNodeConfigRunCollect: " + makeLog(facilityIdList));

		new NodeConfigSettingControllerBean().stopNodeConfigRunCollect(facilityIdList);

		// ---- レスポンス構築

		return Response.status(Status.OK).build();
	}

	/**
	 * NodeInfoの取得。
	 * 構成情報取得スクリプト実行時に、エージェントにノードのSNMP情報を渡す。
	 */
	@POST
	@Path("/repository/node_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetNodeInfoList")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetNodeInfoListResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.READ })
	@IgnoreCommandline
	// AgentNodeConfigEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response getNodeInfoList(
			@RequestBody(description = "getNodeInfoListBody", content = @Content(schema = @Schema(implementation = AgentInfoRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown, FacilityNotFound {
		m_log.debug("getNodeInfoList: Start.");

		// ---- リクエスト解析
		AgentInfoRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, AgentInfoRequest.class);
		AgentInfo agentInfo = new AgentInfo();
		RestBeanUtil.convertBean(req, agentInfo);

		// ---- 主処理
		//Agentに紐づいているFacilityIDを取得;
		ArrayList<String> facilityIdList = AgentConnectUtil.getEffectiveFacilityIds(agentInfo);
		m_log.debug("getNodeInfoList: " + makeLog(facilityIdList));

		ArrayList<NodeInfo> nodeInfoList = new ArrayList<NodeInfo>();
		RepositoryControllerBean rb = new RepositoryControllerBean();
		//FacilityIDごとのNodeInfoを取得
		for (String facilityID : facilityIdList) {
			nodeInfoList.add(rb.getNode(facilityID));
		}

		// ---- レスポンス構築
		GetNodeInfoListResponse res = new GetNodeInfoListResponse();
		res.setList(new ArrayList<>());
		for (NodeInfo src : nodeInfoList) {
			AgtNodeInfoResponse dst = new AgtNodeInfoResponse();
			RestBeanUtil.convertBean(src, dst);
			res.getList().add(dst);
		}

		return Response.status(Status.OK).entity(res).build();
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
