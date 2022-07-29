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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.ws.rs.Consumes;
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
import com.clustercontrol.accesscontrol.bean.UserIdConstant;
import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.agent.bean.DhcpUpdateMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.custom.bean.CommandExecuteDTO;
import com.clustercontrol.custom.session.MonitorCustomControllerBean;
import com.clustercontrol.fault.AgentLibFileNotFound;
import com.clustercontrol.fault.CustomInvalid;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.SessionIdLocked;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.hinemosagent.bean.AgentJavaInfo;
import com.clustercontrol.hinemosagent.bean.AgentLibMd5s;
import com.clustercontrol.hinemosagent.bean.AgentOutputBasicInfo;
import com.clustercontrol.hinemosagent.bean.TopicInfo;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.hinemosagent.util.AgentLibraryManager;
import com.clustercontrol.hinemosagent.util.AgentProfile;
import com.clustercontrol.hinemosagent.util.AgentProfiles;
import com.clustercontrol.hinemosagent.util.AgentUpdateList;
import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.bean.RunOutputResultInfo;
import com.clustercontrol.jobmanagement.bean.RunResultInfo;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.jobmanagement.session.JobRunManagementBean;
import com.clustercontrol.jobmanagement.util.JobFileCheckDuplicationGuard;
import com.clustercontrol.logfile.session.MonitorLogfileControllerBean;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.DhcpSupport;
import com.clustercontrol.repository.util.RepositoryUtil;
import com.clustercontrol.rest.annotation.RestSystemPrivilege;
import com.clustercontrol.rest.annotation.cmdtool.IgnoreCommandline;
import com.clustercontrol.rest.endpoint.agent.dto.AgentInfoRequest;
import com.clustercontrol.rest.endpoint.agent.dto.AgtCustomMonitorInfoResponse;
import com.clustercontrol.rest.endpoint.agent.dto.AgtJobFileCheckResponse;
import com.clustercontrol.rest.endpoint.agent.dto.AgtMonitorInfoResponse;
import com.clustercontrol.rest.endpoint.agent.dto.AgtRunInstructionInfoResponse;
import com.clustercontrol.rest.endpoint.agent.dto.CancelUpdateRequest;
import com.clustercontrol.rest.endpoint.agent.dto.DownLoadAgentLibRequest;
import com.clustercontrol.rest.endpoint.agent.dto.GetAgentLibMapResponse;
import com.clustercontrol.rest.endpoint.agent.dto.GetFileCheckResponse;
import com.clustercontrol.rest.endpoint.agent.dto.GetHinemosTopicResponse;
import com.clustercontrol.rest.endpoint.agent.dto.GetMonitorCustomResponse;
import com.clustercontrol.rest.endpoint.agent.dto.GetMonitorForAgentResponse;
import com.clustercontrol.rest.endpoint.agent.dto.GetMonitorJobMapRequest;
import com.clustercontrol.rest.endpoint.agent.dto.GetMonitorJobMapResponse;
import com.clustercontrol.rest.endpoint.agent.dto.GetScriptResponse;
import com.clustercontrol.rest.endpoint.agent.dto.SendMessageRequest;
import com.clustercontrol.rest.endpoint.agent.dto.SetAgentProfileRequest;
import com.clustercontrol.rest.endpoint.agent.dto.SetFileCheckResultRequest;
import com.clustercontrol.rest.endpoint.agent.dto.SetFileCheckResultResponse;
import com.clustercontrol.rest.endpoint.agent.dto.SetJobResultRequest;
import com.clustercontrol.rest.endpoint.agent.dto.SetJobStartRequest;
import com.clustercontrol.rest.endpoint.agent.dto.SetJobStartResponse;
import com.clustercontrol.rest.endpoint.agent.dto.SettingUpdateInfoResponse;
import com.clustercontrol.rest.endpoint.agent.dto.TopicInfoResponse;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.clustercontrol.rest.session.RestControllerBean;
import com.clustercontrol.rest.util.RestBeanUtil;
import com.clustercontrol.rest.util.RestObjectMapperWrapper;
import com.clustercontrol.rest.util.RestTempFileUtil;
import com.clustercontrol.rest.util.RestTempFileUtil.TempFileStreamFinisherParams;
import com.clustercontrol.rpa.monitor.session.MonitorRpaLogfileControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Singletons;
import com.clustercontrol.util.StringBinder;
import com.clustercontrol.util.apllog.AplLogger;
import com.clustercontrol.winevent.session.MonitorWinEventControllerBean;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.factory.monitors.MonitorCloudLogControllerBean;

@Path("/agent")
public class AgentRestEndpoints {
	private static Log m_log = LogFactory.getLog(AgentRestEndpoints.class);

	private static final String ENDPOINT_OPERATION_ID_PREFIX = "agent";

	/**
	 * Hinemosエージェントへ HinemosTopic 情報を返します。
	 */
	@POST
	@Path("/topic_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetHinemosTopic")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetHinemosTopicResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	// AgentEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response getHinemosTopic(@Context Request req,
			@RequestBody(description = "getHinemosTopicBody",
			content = @Content(schema = @Schema(implementation = AgentInfoRequest.class))) String requestBody,
			@QueryParam("updateNode") boolean updateNode)
			throws InvalidSetting, HinemosUnknown {
		m_log.debug("getHinemosTopic: Start.");

		// 送信元IPを取得
		String remoteIp = req.getRemoteAddr();
		m_log.debug("request from IP Address:" + remoteIp);

		AgentInfoRequest dto = RestObjectMapperWrapper.convertJsonToObject(requestBody, AgentInfoRequest.class);
		AgentInfo info = new AgentInfo();
		RestBeanUtil.convertBean(dto, info);

		GetHinemosTopicResponse hinemosTopicInfoRes = new GetHinemosTopicResponse();

		m_log.debug("updateNode = " + updateNode);
		if (updateNode) {
			// エージェント認識されるまで、またはIPアドレスが変更された場合
			// DHCPサポート機能により、エージェント情報からノード情報の更新を行う。
			DhcpSupport.updateNodes(info, remoteIp);
		}

		ArrayList<String> facilityIdList = AgentConnectUtil.getFacilityIds(info);
		AgentProfiles agentProfiles = Singletons.get(AgentProfiles.class);
		for (String facilityId : facilityIdList) {
			// 未登録エージェントの場合は、プロファイル(ライブラリやJavaの情報)を送信するように指示する。
			// また、割当て先スコープが指定されている場合は割当てを行う。
			// ※直下のコードですぐに処理される。
			if (!agentProfiles.hasProfile(facilityId)) {
				// スコープへの自動割当てを行う。
				autoAssignAgentToScope(facilityId, info);				
				
				TopicInfo topicInfo = new TopicInfo();
				topicInfo.setNewFacilityFlag(true);
				AgentConnectUtil.setTopic(facilityId, topicInfo);
				break;
			}
		}

		// TopicInfo のリストを設定
		ArrayList<TopicInfoResponse> topicInfoList = new ArrayList<TopicInfoResponse>();
		for (String facilityId : facilityIdList) {
			ArrayList<TopicInfo> list = AgentConnectUtil.getTopic(facilityId);
			/*
			 * Agent.propertiesでfacilityIdが直接指定されていない場合、
			 * agentInfoにfacilityIdが含まれていないので、ここで詰める。
			 */
			info.setFacilityId(facilityId);
			AgentConnectUtil.putAgentMap(info);
			if (list != null && list.size() != 0) {
				for (TopicInfo topicInfo : list) {
					TopicInfoResponse topicInfoRes = new TopicInfoResponse();
					RestBeanUtil.convertBean(topicInfo, topicInfoRes);
					topicInfoList.add(topicInfoRes);
				}
			}
		}
		hinemosTopicInfoRes.setTopicInfoList(topicInfoList);

		//AwakePort を設定
		int awakePort = 0;
		for (String facilityId : facilityIdList) {
			try {
				int tmp = NodeProperty.getProperty(facilityId).getAgentAwakePort();
				if (awakePort != 0 && tmp != awakePort) {
					m_log.warn("getHinemosTopic() different awake port " + tmp);
				}
				awakePort = tmp;
			} catch (FacilityNotFound e) {
				m_log.info("getHinemosTopic() : FacilityNotFound " + facilityId);
			}
		}
		hinemosTopicInfoRes.setAwakePort(awakePort);

		// SettingUpdateInfo を設定(RestBeanUtil.convertBeanではコピーできないので手動)
		SettingUpdateInfoResponse settingUpdateInfoRes = new SettingUpdateInfoResponse();
		settingUpdateInfoRes.setBinaryMonitorUpdateTime(SettingUpdateInfo.getInstance().getBinaryMonitorUpdateTime());
		settingUpdateInfoRes.setCalendarUpdateTime(SettingUpdateInfo.getInstance().getCalendarUpdateTime());
		settingUpdateInfoRes.setCustomMonitorUpdateTime(SettingUpdateInfo.getInstance().getCustomMonitorUpdateTime());
		settingUpdateInfoRes.setCustomTrapMonitorUpdateTime(SettingUpdateInfo.getInstance().getCustomTrapMonitorUpdateTime());
		settingUpdateInfoRes.setHinemosTimeOffset(SettingUpdateInfo.getInstance().getHinemosTimeOffset());
		settingUpdateInfoRes.setHinemosTimeZoneOffset(SettingUpdateInfo.getInstance().getHinemosTimeZoneOffset());
		settingUpdateInfoRes.setJobFileCheckUpdateTime(SettingUpdateInfo.getInstance().getJobFileCheckUpdateTime());
		settingUpdateInfoRes.setLogFileMonitorUpdateTime(SettingUpdateInfo.getInstance().getLogFileMonitorUpdateTime());
		settingUpdateInfoRes.setNodeConfigRunCollectUpdateTime(SettingUpdateInfo.getInstance().getNodeConfigRunCollectUpdateTime());
		settingUpdateInfoRes.setNodeConfigSettingUpdateTime(SettingUpdateInfo.getInstance().getNodeConfigSettingUpdateTime());
		settingUpdateInfoRes.setRepositoryUpdateTime(SettingUpdateInfo.getInstance().getRepositoryUpdateTime());
		settingUpdateInfoRes.setSnmptrapMonitorUpdateTime(SettingUpdateInfo.getInstance().getSnmptrapMonitorUpdateTime());
		settingUpdateInfoRes.setSystemLogMonitorUpdateTime(SettingUpdateInfo.getInstance().getSystemLogMonitorUpdateTime());
		settingUpdateInfoRes.setBinaryMonitorUpdateTime(SettingUpdateInfo.getInstance().getBinaryMonitorUpdateTime());
		settingUpdateInfoRes.setWinEventMonitorUpdateTime(SettingUpdateInfo.getInstance().getWinEventMonitorUpdateTime());
		settingUpdateInfoRes.setSdmlControlSettingUpdateTime(SettingUpdateInfo.getInstance().getSdmlControlSettingUpdateTime());
		settingUpdateInfoRes.setRpaLogFileMonitorUpdateTime(SettingUpdateInfo.getInstance().getRpaLogFileMonitorUpdateTime());
		settingUpdateInfoRes.setCloudLogMonitorUpdateTime(SettingUpdateInfo.getInstance().getCloudLogMonitorUpdateTime());
		hinemosTopicInfoRes.setSettingUpdateInfo(settingUpdateInfoRes);
		
		// 送信元のエージェントが認識されている場合はtrue、されていない場合はfalse
		hinemosTopicInfoRes.setRegistered(facilityIdList.stream().anyMatch(AgentConnectUtil::isValidAgent));
		m_log.debug("agent registered:" + hinemosTopicInfoRes.isRegistered());

		return Response.status(Status.OK).entity(hinemosTopicInfoRes).build();
	}

	/**
	 * Hinemosエージェントの登録を削除します。
	 */
	@POST
	@Path("/agent_unregister")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DeleteAgent")
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
	// AgentEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response deleteAgent(@RequestBody(description = "deleteAgentBody",
			content = @Content(schema = @Schema(implementation = AgentInfoRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown {
		m_log.debug("deleteAgent: Start.");

		// ---- リクエスト解析
		AgentInfoRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, AgentInfoRequest.class);
		AgentInfo agentInfo = new AgentInfo();
		RestBeanUtil.convertBean(req, agentInfo);

		// ---- 主処理
		ArrayList<String> facilityIdList = AgentConnectUtil.getFacilityIds(agentInfo);
		AgentProfiles agentProfiles = Singletons.get(AgentProfiles.class);
		AgentUpdateList agentUpdateList = Singletons.get(AgentUpdateList.class);
		for (String facilityId : facilityIdList) {
			m_log.info("deleteAgent: " + facilityId + " is shutdown");
			AgentConnectUtil.deleteAgent(facilityId, agentInfo);
			agentProfiles.removeProfile(facilityId);
			agentUpdateList.release(facilityId);
		}
		
		// DHCPサポート機能が有効な場合ノードを無効化する。
		if (agentInfo.getDhcpUpdateMode().equals(DhcpUpdateMode.ip)) {
			try {
				DhcpSupport.nullfyNodes(facilityIdList);
			} catch (FacilityNotFound e) {
				// ノードが削除されている場合、何もしない
			} catch (HinemosUnknown e) {
				// 通常来ない想定
				m_log.warn(e.getMessage(), e);
			}
		}

		return Response.status(Status.OK).build();
	}

	/**
	 * Hinemosエージェントからのイベントを通知します。
	 */
	@POST
	@Path("/notify_exec")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SendMessageToInternalEvent")
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
	// AgentEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response sendMessageToInternalEvent(@RequestBody(description = "sendMessageToInternalEventBody", content = @Content(schema = @Schema(implementation = SendMessageRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown, FacilityNotFound {
		m_log.debug("sendMessageToInternalEvent: Start.");

		// ---- 重複チェック
		boolean first = new RestControllerBean().registerRestAgentRequest("", "sendMessageToInternalEvent");
		if (!first) {
			return Response.status(Status.OK).build();
		}

		SendMessageRequest dto = RestObjectMapperWrapper.convertJsonToObject(requestBody, SendMessageRequest.class);
		AgentOutputBasicInfo info = new AgentOutputBasicInfo();
		RestBeanUtil.convertBeanNoInvalid(dto, info);

		ArrayList<String> facilityIdList = AgentConnectUtil.getFacilityIds(info.getAgentInfo());
		if (facilityIdList == null || facilityIdList.size() == 0) {
			m_log.info("sendMessage facilityId is null");
		} else if (facilityIdList.size() == 0) {
			m_log.info("sendMessage facilityId.size() is 0");
		} else {
			AgentConnectUtil.sendMessageLocal(info.getOutputBasicInfo(), facilityIdList);
		}

		return Response.status(Status.OK).build();
	}

	/**
	 * ジョブ実行開始確認を行う
	 */
	@POST
	@Path("/job_startCheck/session/{sessionId}/jobunit/{jobunitId}/job/{jobId}/facility/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SetJobStart")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SetJobStartResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	// AgentEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response setJobStart(@RequestBody(description = "setJobStartBody",
			content = @Content(schema = @Schema(implementation = SetJobStartRequest.class))) String requestBody,
			@PathParam("sessionId") String sessionId,
			@PathParam("jobunitId") String jobunitId,
			@PathParam("jobId") String jobId,
			@PathParam("facilityId") String facilityId)
			throws InvalidSetting, InvalidRole, JobInfoNotFound, HinemosUnknown, SessionIdLocked {
		m_log.debug("setjobStart: Start.");

		// ---- 重複チェック
		boolean first = new RestControllerBean().registerRestAgentRequest("", "setjobStart");
		if (!first) {
			return Response.status(Status.OK).build();
		}
		
		// ---- リクエスト解析
		SetJobStartRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, SetJobStartRequest.class);
		RunResultInfo info = new RunResultInfo();
		RestBeanUtil.convertBean(req, info);
		info.setSessionId(sessionId);
		info.setJobunitId(jobunitId);
		info.setJobId(jobId);
		info.setFacilityId(facilityId);

		// ---- 主処理
		// ログが見にくなるので、短くして、改行を取り除く
		String command = info.getCommand();
		int length = 32;
		if (command != null) {
			if (length < command.length()) {
				command = command.substring(0, length);
			}
			command = command.replaceAll("\n", "");
		}

		m_log.info("setJobStart : " +
				info.getSessionId() + ", " +
				info.getJobunitId() + ", " +
				info.getJobId() + ", " +
				info.getCommandType() + ", " +
				command + ", " +
				info.getStatus() + ", " +
				info.getFacilityId() + ", ");

		try{
			new JobRunManagementBean().checkSessionIdLocked(info);
		} catch(SessionIdLocked e){
			// セッション処理中の応答をエージェントに返せなかった場合を想定し、
			// リクエストIDを削除する
			new RestControllerBean().deleteRestAgentRequest();
			throw e;
		}
		boolean jobRunnable;
		jobRunnable = new JobRunManagementBean().endNode(info);

		// ---- レスポンス構築
		SetJobStartResponse res = new SetJobStartResponse();
		res.setSessionId(info.getSessionId());
		res.setJobunitId(info.getJobunitId());
		res.setJobId(info.getJobId());
		res.setFacilityId(info.getFacilityId());
		res.setJobRunnable(jobRunnable);

		return Response.status(Status.OK).entity(res).build();
	}
	
	/**
	 * ジョブ実行開始確認を行う、あるいはジョブ実行結果を受け取ります。
	 */
	@PUT
	@Path("/job/session/{sessionId}/jobunit/{jobunitId}/job/{jobId}/facility/{facilityId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SetJobResult")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	// AgentEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response setJobResult(@RequestBody(description = "setJobResultBody",
			content = @Content(schema = @Schema(implementation = SetJobResultRequest.class))) String requestBody,
			@PathParam("sessionId") String sessionId,
			@PathParam("jobunitId") String jobunitId,
			@PathParam("jobId") String jobId,
			@PathParam("facilityId") String facilityId)
			throws InvalidSetting, InvalidRole, JobInfoNotFound, HinemosUnknown, SessionIdLocked {
		m_log.debug("setJobResult: Start.");

		// ---- 重複チェック
		boolean first = new RestControllerBean().registerRestAgentRequest("", "setJobResult");
		if (!first) {
			return Response.status(Status.OK).build();
		}
		
		// ---- リクエスト解析
		SetJobResultRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, SetJobResultRequest.class);
		RunResultInfo info = new RunResultInfo();
		RestBeanUtil.convertBean(req, info);
		info.setSessionId(sessionId);
		info.setJobunitId(jobunitId);
		info.setJobId(jobId);
		info.setFacilityId(facilityId);

		// ---- 主処理
		// ログが見にくなるので、短くして、改行を取り除く
		String command = info.getCommand();
		int length = 32;
		if (command != null) {
			if (length < command.length()) {
				command = command.substring(0, length);
			}
			command = command.replaceAll("\n", "");
		}

		RunOutputResultInfo outputInfo = new RunOutputResultInfo();
		if (req.getJobOutput() != null) {
			RestBeanUtil.convertBean(req.getJobOutput(), outputInfo);
		}
		
		m_log.info("setJobResult : " +
				info.getSessionId() + ", " +
				info.getJobunitId() + ", " +
				info.getJobId() + ", " +
				info.getCommandType() + ", " +
				command + ", " +
				info.getStatus() + ", " +
				info.getFacilityId() + ", ");

		try{
			new JobRunManagementBean().checkSessionIdLocked(info);
		} catch(SessionIdLocked e){
			// セッション処理中の応答をエージェントに返せなかった場合を想定し、
			// リクエストIDを削除する
			new RestControllerBean().deleteRestAgentRequest();
			throw e;
		}
		new JobRunManagementBean().endNode(info, outputInfo);

		return Response.status(Status.OK).build();
	}

	/**
	 * ログファイル監視の監視設定を返します。
	 */
	@POST
	@Path("/monitorsetting/logfile_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMonitorLogfile")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetMonitorForAgentResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	// AgentEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response getMonitorLogfile(@RequestBody(description = "getMonitorLogfileBody", content = @Content(schema = @Schema(implementation = AgentInfoRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown, MonitorNotFound {
		m_log.debug("getMonitorLogfile: Start.");

		AgentInfoRequest dto = RestObjectMapperWrapper.convertJsonToObject(requestBody, AgentInfoRequest.class);
		AgentInfo info = new AgentInfo();
		RestBeanUtil.convertBeanNoInvalid(dto, info);

		// 収集の対象となるファシリティ一覧を取得
		ArrayList<String> facilityIdList = AgentConnectUtil.getFacilityIds(info);

		//ファシリティIDに対応した監視項目を取得
		GetMonitorForAgentResponse res = new GetMonitorForAgentResponse();
		res.setList(new ArrayList<>());
		MonitorLogfileControllerBean bean = new MonitorLogfileControllerBean();
		for (String facilityId : facilityIdList) {
			ArrayList<MonitorInfo> infoList = bean.getLogfileListForFacilityId(facilityId, true);
			for (MonitorInfo infoRes : infoList) {
				AgtMonitorInfoResponse dtoRes = new AgtMonitorInfoResponse();
				RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);
				
				// 「ディレクトリ」設定内のノード変数を置換する。
				// beanで置換するとキャッシュが上書きされるため、レスポンスDTOで置換する。
				int maxReplaceWord = HinemosPropertyCommon.replace_param_max.getIntegerValue().intValue();
				String directory = dtoRes.getLogfileCheckInfo().getDirectory();
				ArrayList<String> inKeyList = StringBinder.getKeyList(directory, maxReplaceWord);
				Map<String, String> nodeParameter = new HashMap<>();
				try {
					nodeParameter = RepositoryUtil.createNodeParameter(new RepositoryControllerBean().getNode(facilityId), inKeyList);
					m_log.debug(String.format("replace Parameters: directory=%s, nodeParameter=%s", directory, nodeParameter));
				} catch (FacilityNotFound e) {
					m_log.warn("getMonitorLogfile() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				}
				StringBinder strbinder = new StringBinder(nodeParameter);
				dtoRes.getLogfileCheckInfo().setDirectory(strbinder.bindParam(directory));

				res.getList().add(dtoRes);
			}
		}

		return Response.status(Status.OK).entity(res).build();
	}

	/**
	 * RPAログファイル監視の監視設定を返します。
	 */
	@POST
	@Path("/monitorsetting/rpalogfile_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMonitorRpaLogfile")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetMonitorForAgentResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	public Response getRpaMonitorLogfile(@RequestBody(description = "getMonitorLogfileBody", content = @Content(schema = @Schema(implementation = AgentInfoRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown, MonitorNotFound {
		m_log.debug("getMonitorRpaLogfile: Start.");

		AgentInfoRequest dto = RestObjectMapperWrapper.convertJsonToObject(requestBody, AgentInfoRequest.class);
		AgentInfo info = new AgentInfo();
		RestBeanUtil.convertBeanNoInvalid(dto, info);

		// 収集の対象となるファシリティ一覧を取得
		ArrayList<String> facilityIdList = AgentConnectUtil.getFacilityIds(info);

		//ファシリティIDに対応した監視項目を取得
		GetMonitorForAgentResponse res = new GetMonitorForAgentResponse();
		res.setList(new ArrayList<>());
		MonitorRpaLogfileControllerBean bean = new MonitorRpaLogfileControllerBean();
		for (String facilityId : facilityIdList) {
			ArrayList<MonitorInfo> infoList = bean.getRpaLogfileListForFacilityId(facilityId, true);
			for (MonitorInfo infoRes : infoList) {
				AgtMonitorInfoResponse dtoRes = new AgtMonitorInfoResponse();
				RestBeanUtil.convertBeanNoInvalid(infoRes, dtoRes);

				// 「ディレクトリ」設定内のノード変数を置換する。
				// beanで置換するとキャッシュが上書きされるため、レスポンスDTOで置換する。
				int maxReplaceWord = HinemosPropertyCommon.replace_param_max.getIntegerValue().intValue();
				String directory = dtoRes.getRpaLogFileCheckInfo().getDirectory();
				ArrayList<String> inKeyList = StringBinder.getKeyList(directory, maxReplaceWord);
				Map<String, String> nodeParameter = new HashMap<>();
				try {
					nodeParameter = RepositoryUtil.createNodeParameter(new RepositoryControllerBean().getNode(facilityId), inKeyList);
					m_log.debug(String.format("replace Parameters: directory=%s, nodeParameter=%s", directory, nodeParameter));
				} catch (FacilityNotFound e) {
					m_log.warn("getRpaMonitorLogfile() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				}
				StringBinder strbinder = new StringBinder(nodeParameter);
				dtoRes.getRpaLogFileCheckInfo().setDirectory(strbinder.bindParam(directory));

				res.getList().add(dtoRes);
			}
		}

		return Response.status(Status.OK).entity(res).build();
	}

	/**
	 * ジョブのファイルチェック契機の設定を返します。
	 */
	@POST
	@Path("/job/kick/filecheck_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetFileCheck")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetFileCheckResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	// AgentEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response getFileCheck(
			@RequestBody(description = "getFileCheckBody", content = @Content(schema = @Schema(implementation = AgentInfoRequest.class))) String requestBody)
			throws InvalidSetting, InvalidRole, JobMasterNotFound, HinemosUnknown {
		m_log.debug("getFileCheck: Start.");

		// ---- リクエスト解析
		AgentInfoRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, AgentInfoRequest.class);
		AgentInfo agentInfo = new AgentInfo();
		RestBeanUtil.convertBean(req, agentInfo);

		// ---- 主処理
		ArrayList<String> facilityIdList = AgentConnectUtil.getFacilityIds(agentInfo);

		ArrayList<JobFileCheck> result = new JobControllerBean().getJobFileCheck(facilityIdList);

		// ---- レスポンス構築
		GetFileCheckResponse res = new GetFileCheckResponse();
		res.setList(new ArrayList<>());
		for (JobFileCheck src : result) {
			AgtJobFileCheckResponse dst = new AgtJobFileCheckResponse();
			RestBeanUtil.convertBeanNoInvalid(src, dst);
			res.getList().add(dst);
		}

		return Response.status(Status.OK).entity(res).build();
	}

	/**
	 * ファイルチェックの結果を受け取ります。
	 */
	@POST
	@Path("/job/kick/filecheck/{kickId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SetFileCheckResult")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SetFileCheckResultResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	// AgentEndpoint にも同様の処理(jobFileCheckResult)があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response setFileCheckResult(
			@PathParam("kickId") String kickId,
			@RequestBody(description = "setFileCheckResultBody", content = @Content(schema = @Schema(implementation = SetFileCheckResultRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown {
		m_log.debug("setFileCheckResult: Start.");

		// ---- リクエスト解析
		SetFileCheckResultRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, SetFileCheckResultRequest.class);

		JobFileCheck jobFileCheck = new JobFileCheck();
		RestBeanUtil.convertBean(req.getJobFileCheckRequest(), jobFileCheck);
		jobFileCheck.setId(kickId);

		AgentInfo agentInfo = new AgentInfo();
		RestBeanUtil.convertBean(req.getAgentInfo(), agentInfo);

		// ---- 主処理
		String sessionId = null;
		// 重複ガード
		try (JobFileCheckDuplicationGuard dg = new JobFileCheckDuplicationGuard(jobFileCheck)) {
			String id = jobFileCheck.getId();
			String jobunitId = jobFileCheck.getJobunitId();
			String jobId = jobFileCheck.getJobId();
			String filename = jobFileCheck.getFileName();
			String directory = jobFileCheck.getDirectory();
			Integer eventType = jobFileCheck.getEventType();
			Integer modifyType = jobFileCheck.getModifyType();
			m_log.info("setFileCheckResult : id=" + id + ", jobunitId=" + jobunitId + ", jobId=" + jobId
					+ ", filename=" + filename + ", directory=" + directory + ", eventType=" + eventType + ", modifyType=" + modifyType
					+ ", uniqueId=" + dg.getUniqueId());

			sessionId = dg.getSessionId();
			if (sessionId != null) {
				m_log.info("setFileCheckResult : Detected duplication. jobSessionId=" + sessionId);
			} else {
				JobTriggerInfo trigger = new JobTriggerInfo();
				trigger.setJobkickId(jobFileCheck.getId());
				trigger.setTrigger_type(JobTriggerTypeConstant.TYPE_FILECHECK);
				trigger.setTrigger_info(dg.getTriggerInfo());
				trigger.setFilename(filename);
				trigger.setDirectory(directory);
				OutputBasicInfo output = null;
				for (String facilityId : AgentConnectUtil.getFacilityIds(agentInfo)) {
					ArrayList<String> facilityList =
							FacilitySelector.getFacilityIdList(jobFileCheck.getFacilityId(), jobFileCheck.getOwnerRoleId(), 0, false, false);
					if (facilityList.contains(facilityId)) {
						output = new OutputBasicInfo();
						output.setFacilityId(facilityId);
						try {
							sessionId = new JobControllerBean().runJob(jobunitId, jobId, output, trigger);
						} catch (Exception e) {
							m_log.warn("setFileCheckResult : " + e.getMessage());
							String[] args = { jobId, trigger.getTrigger_info() };
							AplLogger.put(InternalIdCommon.JOB_SYS_017, args);
							throw new HinemosUnknown(e.getMessage(), e);
						}
					}
				}
			}
		}

		// ---- レスポンス構築
		SetFileCheckResultResponse res = new SetFileCheckResultResponse();
		res.setSessionId(sessionId);

		return Response.status(Status.OK).entity(res).build();
	}

	/**
	 * Windowsイベント監視の監視設定を返します。
	 */
	@POST
	@Path("/monitorsetting/winevent_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMonitorWinEvent")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetMonitorForAgentResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	// AgentEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response getMonitorWinEvent(
			@RequestBody(description = "getMonitorWinEventBody", content = @Content(schema = @Schema(implementation = AgentInfoRequest.class))) String requestBody)
			throws InvalidSetting, InvalidRole, HinemosUnknown {
		m_log.debug("getMonitorWinEvent: Start.");

		// ---- リクエスト解析
		AgentInfoRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, AgentInfoRequest.class);
		AgentInfo agentInfo = new AgentInfo();
		RestBeanUtil.convertBean(req, agentInfo);

		// ---- 主処理
		ArrayList<String> facilityIdList = AgentConnectUtil.getFacilityIds(agentInfo);

		ArrayList<MonitorInfo> list = new ArrayList<MonitorInfo>();
		MonitorWinEventControllerBean bean = new MonitorWinEventControllerBean();
		for (String facilityId : facilityIdList) {
			list.addAll(bean.getWinEventList(facilityId));
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
	 * カスタム監視の監視設定を返します。
	 */
	@POST
	@Path("/monitorsetting/custom_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMonitorCustom")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetMonitorCustomResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	// AgentEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response getMonitorCustom(
			@QueryParam("ForMonitorJob") Boolean forMonitorJob,
			@RequestBody(
					description = "getMonitorCustomBody",
					content = @Content(schema = @Schema(implementation = AgentInfoRequest.class))) String requestBody)
			throws InvalidSetting, InvalidRole, HinemosUnknown {
		m_log.debug("getMonitorCustom: Start. forMonitorJob=" + forMonitorJob);

		// ---- リクエスト解析
		AgentInfoRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, AgentInfoRequest.class);
		AgentInfo agentInfo = new AgentInfo();
		RestBeanUtil.convertBean(req, agentInfo);

		// ---- 主処理
		ArrayList<String> facilityIds = AgentConnectUtil.getFacilityIds(agentInfo);
		ArrayList<CommandExecuteDTO> dtos = new ArrayList<CommandExecuteDTO>();

		try {
			if (forMonitorJob == null || forMonitorJob.booleanValue() == false) {
				// ---- from getCommandExecuteDTO
				MonitorCustomControllerBean monitorCmdCtrl = new MonitorCustomControllerBean();
				for (String facilityId : facilityIds) {
					dtos.addAll(monitorCmdCtrl.getCommandExecuteDTO(facilityId));
				}
			} else {
				// ---- from getCommandExecuteDTOForMonitorJob
				JobRunManagementBean jobRunManagement = new JobRunManagementBean();
				for (String facilityId : facilityIds) {
					dtos.addAll(jobRunManagement.getCommandExecuteDTOForMonitorJob(facilityId));
				}
			}
		} catch (CustomInvalid e) {
			throw new HinemosUnknown(e.getMessage(), e);
		}

		// ---- レスポンス構築
		GetMonitorCustomResponse res = new GetMonitorCustomResponse();
		res.setList(new ArrayList<>());
		for (CommandExecuteDTO src : dtos) {
			AgtCustomMonitorInfoResponse dst = new AgtCustomMonitorInfoResponse();
			RestBeanUtil.convertBeanNoInvalid(src, dst);
			res.getList().add(dst);
		}

		return Response.status(Status.OK).entity(res).build();
	}

	/**
	 * Hinemosマネージャが保持しているエージェント用ライブラリファイルのうち、指定されたファイルの内容を返します。
	 */
	@POST
	@Path("/profile_library_download")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "DownloadAgentLib")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(
					mediaType = MediaType.APPLICATION_OCTET_STREAM,
					schema = @Schema(type = SchemaType.STRING, format = "binary")),
					description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces({ MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON })
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	// AgentEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response downloadAgentLib(
			@RequestBody(description = "downloadAgentLibBody", content = @Content(schema = @Schema(implementation = DownLoadAgentLibRequest.class))) String requestBody)
			throws InvalidSetting, AgentLibFileNotFound, HinemosUnknown {

		// ---- リクエスト解析
		DownLoadAgentLibRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, DownLoadAgentLibRequest.class);

		String libPath = req.getLibPath();

		AgentInfo agentInfo = new AgentInfo();
		RestBeanUtil.convertBean(req.getAgentInfo(), agentInfo);

		// ---- 主処理
		List<String> facilityIds = AgentConnectUtil.getFacilityIds(agentInfo);

		m_log.debug("downloadAgentLib: libPath=" + libPath + " facilityIds=" + String.join(",", facilityIds));

		// 更新中エージェントからの要求でない場合は拒否する
		AgentUpdateList uplist = Singletons.get(AgentUpdateList.class);
		if (!uplist.isUpdating(facilityIds)) {
			throw new HinemosUnknown("Not marked as updating.");
		}

		// ファイルオブジェクトを取得する
		File file = Singletons.get(AgentLibraryManager.class).getFile(libPath);
		if (file == null) {
			m_log.info("downloadAgentLib: File not found. path=" + libPath + " facilityIds=" + String.join(",", facilityIds));
			throw new AgentLibFileNotFound("Failed to find " + libPath);
		}

		// 更新中エージェントリストへダウンロード開始を記録
		for (String facilityId : facilityIds) {
			uplist.recordDownloadStart(facilityId);
		}

		// ---- レスポンス構築

		// ダウンロード完了(正確には送信完了、クライアントが受け取り切ったかは関知しない)を検知してダウンロード終了を記録
		Consumer<TempFileStreamFinisherParams> finisher = unused -> {
			facilityIds.forEach(uplist::recordDownloadEnd);
		};

		return Response.ok(RestTempFileUtil.getTempFileStream(file, null, false, finisher))
				.header("Content-Disposition", "filename=\"" + file.getName() + "\"")
				.build();
	}

	/**
	 * あるエージェントがアップデート実行中であるという状態を取り消します。
	 * エージェント側でアップデートに失敗した場合に呼びます。
	 */
	@POST
	@Path("/update_cancel")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "CancelUpdate")
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
	// AgentEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response cancelUpdate(
			@RequestBody(description = "cancelUpdateBody", content = @Content(schema = @Schema(implementation = CancelUpdateRequest.class))) String requestBody)
			throws InvalidSetting, FacilityNotFound, HinemosUnknown {
		m_log.debug("cancelUpdate: Start.");

		// ---- リクエスト解析
		CancelUpdateRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, CancelUpdateRequest.class);

		String cause = req.getCause();

		AgentInfo agentInfo = new AgentInfo();
		RestBeanUtil.convertBean(req.getAgentInfo(), agentInfo);

		// ---- 主処理
		ArrayList<String> facilityIds = AgentConnectUtil.getFacilityIds(agentInfo);
		if (facilityIds.size() == 0) {
			throw new HinemosUnknown("Facility ID not specified.");
		}

		// 通知
		OutputBasicInfo info = new OutputBasicInfo();
		info.setPluginId(HinemosModuleConstant.PLATFORM_REPOSITORY);
		info.setPriority(PriorityConstant.TYPE_WARNING);
		info.setApplication(MessageConstant.AGENT.getMessage());
		info.setMessage(MessageConstant.MESSAGE_AGENT_UPDATE_FAILURE.getMessage());
		info.setMessageOrg(MessageConstant.MESSAGE_AGENT_UPDATE_FAILURE.getMessage() + "(" + cause + ")");
		info.setGenerationDate(HinemosTime.getDateInstance().getTime());
		info.setMonitorId("SYS"); // これ以外を指定すると監視設定を参照しようとしてしまう
		info.setFacilityId(""); // 後でセット
		info.setScopeText(""); // 後でセット
		info.setRunInstructionInfo(null);
		try {
			AgentConnectUtil.sendMessageLocal(info, facilityIds);
		} finally {
			// 更新中リストから当該ノードを除去する。
			Singletons.get(AgentUpdateList.class).release(facilityIds);
		}

		return Response.status(Status.OK).build();
	}

	/**
	 * マネージャが保持している、エージェントのライブラリファイルの一覧を返します。
	 * <p>
	 * ver.6.2正式版以降に対しては HinemosJava を含む一覧、
	 * ver.6.2先行版に対しては HinemosJava を含まない一覧、
	 * それ以外(通常、ver.6.1以前)に対しては空の一覧を返します。<br/>
	 */
	@POST
	@Path("/profile_library_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetAgentLibMap")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetAgentLibMapResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	// AgentEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response getAgentLibMap(
			@RequestBody(description = "getAgentLibMapBody", content = @Content(schema = @Schema(implementation = AgentInfoRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown {
		m_log.debug("getAgentLibMap: Start.");

		// ---- リクエスト解析
		AgentInfoRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, AgentInfoRequest.class);
		AgentInfo agentInfo = new AgentInfo();
		RestBeanUtil.convertBean(req, agentInfo);

		// ---- 主処理
		// ファシリティIDを解決
		List<String> facilityIds = AgentConnectUtil.getFacilityIds(agentInfo);

		// 更新中エージェントからの要求でない場合は拒否する
		AgentUpdateList uplist = Singletons.get(AgentUpdateList.class);
		if (!uplist.isUpdating(facilityIds)) {
			throw new HinemosUnknown("Not marked as updating.");
		}

		// ライブラリファイル一覧を取得
		AgentLibraryManager libMgr = Singletons.get(AgentLibraryManager.class);
		AgentLibMd5s libMd5s = libMgr.getAgentLibMd5s(facilityIds);

		// 更新中エージェントリストへ時刻を記録
		AgentUpdateList agentUpdateList = Singletons.get(AgentUpdateList.class);
		facilityIds.forEach(facilityId -> agentUpdateList.recordLibMapAccessTime(facilityId));

		// ---- レスポンス構築
		GetAgentLibMapResponse res = new GetAgentLibMapResponse();
		res.setMd5Map(libMd5s.asMap());

		return Response.status(Status.OK).entity(res).build();
	}

	/**
	 * エージェント側のライブラリファイルとJavaの情報をマネージャへ登録します。
	 */
	@POST
	@Path("/profile")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "SetAgentProfile")
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
	// AgentEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response setAgentProfile(
			@RequestBody(description = "setAgentProfileBody", content = @Content(schema = @Schema(implementation = SetAgentProfileRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown {

		// ---- リクエスト解析
		SetAgentProfileRequest req = RestObjectMapperWrapper.convertJsonToObject(requestBody, SetAgentProfileRequest.class);

		Map<String, String> md5Map = req.getMd5Map();

		AgentJavaInfo javaInfo = new AgentJavaInfo();
		RestBeanUtil.convertBean(req.getJavaInfo(), javaInfo);

		AgentInfo agentInfo = new AgentInfo();
		RestBeanUtil.convertBean(req.getAgentInfo(), agentInfo);

		// ---- 主処理
		List<String> facilityIds = AgentConnectUtil.getFacilityIds(agentInfo);
		AgentLibMd5s libMd5s = new AgentLibMd5s(md5Map);

		if (m_log.isDebugEnabled()) {
			m_log.debug(String.format("setAgentProfile: agentInfo=%s, facilityIds=[%s]", agentInfo.toString(),
					String.join(",", facilityIds)));
		}

		Singletons.get(AgentProfiles.class).registerProfile(facilityIds, new AgentProfile(libMd5s, javaInfo));

		return Response.status(Status.OK).build();
	}

	/**
	 * スクリプト情報を取得する
	 */
	@GET
	@Path("/job/jobInfo_script/sessionId/{sessionId}/jobunitId/{jobunitId}/jobId/{jobId}")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetScript")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetScriptResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	// @Consumes(MediaType.APPLICATION_JSON) // リクエストボディは空なので不要
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.READ })
	@IgnoreCommandline
	// AgentEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response getScript(
			@PathParam("sessionId") String sessionId,
			@PathParam("jobunitId") String jobunitId,
			@PathParam("jobId") String jobId)
			throws InvalidRole, JobInfoNotFound, HinemosUnknown {
		m_log.debug("getScript : sessionId=" + sessionId + ", jobunitId=" + jobunitId + ", jobId=" + jobId);

		// ---- リクエスト解析

		// ---- 主処理
		List<String> result = new JobControllerBean().getJobScriptInfo(sessionId, jobunitId, jobId);

		// ---- レスポンス構築
		// ver.6.2 以前のエージェント用ロジックを使用しているため、entity -> list -> dto と、間に list を挟む無駄な形になっている。
		GetScriptResponse res = new GetScriptResponse();
		if (result.size() > 0) {
			res.setEmpty(false);
			res.setScriptName(result.get(0));
			res.setScriptEncoding(result.get(1));
			res.setScriptContent(result.get(2));
		} else {
			res.setEmpty(true);
		}

		return Response.status(Status.OK).entity(res).build();
	}

	/**
	 * 監視ジョブの監視設定を取得。
	 */
	@POST
	@Path("/monitorsetting/monitor_forMonitorJob_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMonitorJobMap")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetMonitorJobMapResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	// AgentEndpoint にも同様の処理があります。v6.2以前のエージェントに対しても必要な修正はそちらにも適用してください。
	public Response getMonitorJobMap(@RequestBody(description = "getMonitorJobMaptBody", content = @Content(schema = @Schema(implementation = GetMonitorJobMapRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown {
		m_log.debug("getMonitorJobMap: Start.");

		GetMonitorJobMapRequest dto = RestObjectMapperWrapper.convertJsonToObject(requestBody, GetMonitorJobMapRequest.class);
		AgentInfo agentInfo = new AgentInfo();
		RestBeanUtil.convertBeanNoInvalid(dto.getAgentInfo(), agentInfo);

		ArrayList<String> facilityIdList = AgentConnectUtil.getFacilityIds(agentInfo);

		GetMonitorJobMapResponse res = new GetMonitorJobMapResponse();
		res.setMonitorInfoList(new ArrayList<>());
		res.setRunInstructionInfoList(new ArrayList<>());
		JobRunManagementBean bean = new JobRunManagementBean();
		for (String facilityId : facilityIdList) {
			HashMap<RunInstructionInfo, MonitorInfo> jobMap = bean.getMonitorJobMap(dto.getMonitorTypeId(), facilityId);
			for (Entry<RunInstructionInfo, MonitorInfo> entry : jobMap.entrySet()) {
				AgtRunInstructionInfoResponse runInfo = new AgtRunInstructionInfoResponse();
				AgtMonitorInfoResponse monInfo = new AgtMonitorInfoResponse();
				RestBeanUtil.convertBeanNoInvalid(entry.getKey(), runInfo);
				RestBeanUtil.convertBeanNoInvalid(entry.getValue(), monInfo);
				// クラウドログ監視向けの個別変換
				if (monInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CLOUD_LOG)){
					try {
						new MonitorCloudLogControllerBean().reflectCloudLogInfo(monInfo);
					} catch (CloudManagerException e) {
						m_log.error("getMonitorJobMap:", e);
					}
				}
				res.getMonitorInfoList().add(monInfo);
				res.getRunInstructionInfoList().add(runInfo);
			}
		}
		
		return Response.status(Status.OK).entity(res).build();
	}
	
	/**
	 * MC機能のヘルスチェック用API
	 * 
	 * ログイン不要、権限必要なし
	 * 
	 * @param request
	 * @param uriInfo
	 * @return
	 * @throws HinemosUnknown
	 */
	@GET
	@Path("/healthCheck_forMC")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "HealthCheck")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@IgnoreCommandline
	public Response healthCheck(@Context Request request, @Context UriInfo uriInfo)
			throws HinemosUnknown {
		// ヘルスチェックのたびに出力されログが増加するためコメントアウトする
		// m_log.info("call healthCheck()");
		return Response.status(Status.OK).build();
	}
	/**
	 * クラウドログ監視の監視設定を返します。 
	 */
	@POST
	@Path("/monitorsetting/cloudlog_search")
	@Operation(operationId = ENDPOINT_OPERATION_ID_PREFIX + "GetMonitorCloudLog")
	@APIResponses(value = {
			@APIResponse(responseCode = STATUS_CODE_200, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetMonitorForAgentResponse.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_400, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_401, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_404, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_403, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response"),
			@APIResponse(responseCode = STATUS_CODE_500, content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ExceptionBody.class)), description = "response") })
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RestSystemPrivilege(function = SystemPrivilegeFunction.HinemosAgent, modeList = { SystemPrivilegeMode.MODIFY })
	@IgnoreCommandline
	public Response getMonitorCloudLog(@RequestBody(description = "getMonitorCloudLogBody", content = @Content(schema = @Schema(implementation = AgentInfoRequest.class))) String requestBody)
			throws InvalidSetting, HinemosUnknown, MonitorNotFound, CloudManagerException {
		m_log.debug("getMonitorCloudLog: Start.");

		AgentInfoRequest dto = RestObjectMapperWrapper.convertJsonToObject(requestBody, AgentInfoRequest.class);
		AgentInfo info = new AgentInfo();
		RestBeanUtil.convertBeanNoInvalid(dto, info);

		// 収集の対象となるファシリティ一覧を取得
		ArrayList<String> facilityIdList = AgentConnectUtil.getFacilityIds(info);

		//ファシリティIDに対応した監視項目を取得
		GetMonitorForAgentResponse res = new GetMonitorForAgentResponse();
		res.setList(new ArrayList<>());
		MonitorCloudLogControllerBean bean = new MonitorCloudLogControllerBean();
		for (String facilityId : facilityIdList) {
			ArrayList<MonitorInfo> infoList = bean.getCloudLogListForFacilityId(facilityId, true);
			for (MonitorInfo infoRec : infoList) {
				AgtMonitorInfoResponse dtoRec = new AgtMonitorInfoResponse();
				RestBeanUtil.convertBeanNoInvalid(infoRec, dtoRec);
				//変換後のDTOに必要な情報を詰める
				bean.reflectCloudLogInfo(dtoRec);
				res.getList().add(dtoRec);
			}
		}

		return Response.status(Status.OK).entity(res).build();
	}
	
	/**
	 * Agentプロパティに基づきスコープの自動割当てを行う。
	 */
	private void autoAssignAgentToScope(String facilityId, AgentInfo info){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			String execUser = HinemosSessionContext.getLoginUserId();
			// 管理者ユーザで変更を実施
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, UserIdConstant.HINEMOS);
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, true);

			List<String> scopeIdList = info.getAssignScopeList();
			List<String> invalidIdList = new ArrayList<>();
			RepositoryControllerBean repositoryControllerBean = new RepositoryControllerBean();

			for (String scopeId : scopeIdList) {
				try {
					// 既にスコープに割当て済の場合は、何も変更されない。
					repositoryControllerBean.assignNodeScope(scopeId, new String[]{facilityId});
				} catch (InvalidSetting e) {
					// スコープIDが存在しない。
					invalidIdList.add(scopeId);
				}
			}
			
			if (!invalidIdList.isEmpty()) {
				// 存在しないスコープIDが指定されていた場合、INTERNAL通知を行う。
				AplLogger.put(InternalIdCommon.AGENT_SYS_001, new String[]{String.join(",", invalidIdList), facilityId});
			}
			
			// ThreadLocalを戻す
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, execUser);
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.IS_ADMINISTRATOR, new AccessControllerBean().isAdministrator());
		} catch (InvalidRole | HinemosUnknown e) {
			// 想定外エラー
			m_log.warn(e.getMessage(), e);
		}
	}
}

