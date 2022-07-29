/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent;

import java.io.File;
import java.net.InetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgentInfoRequest;
import org.openapitools.client.model.CancelUpdateRequest;
import org.openapitools.client.model.DownLoadAgentLibRequest;
import org.openapitools.client.model.GetAgentLibMapResponse;
import org.openapitools.client.model.GetFileCheckResponse;
import org.openapitools.client.model.GetHinemosTopicResponse;
import org.openapitools.client.model.GetMonitorCustomResponse;
import org.openapitools.client.model.GetMonitorForAgentResponse;
import org.openapitools.client.model.GetMonitorJobMapRequest;
import org.openapitools.client.model.GetMonitorJobMapResponse;
import org.openapitools.client.model.GetScriptResponse;
import org.openapitools.client.model.SendMessageRequest;
import org.openapitools.client.model.SetAgentProfileRequest;
import org.openapitools.client.model.SetFileCheckResultRequest;
import org.openapitools.client.model.SetFileCheckResultResponse;
import org.openapitools.client.model.SetJobResultRequest;
import org.openapitools.client.model.SetJobStartRequest;
import org.openapitools.client.model.SetJobStartResponse;

import com.clustercontrol.agent.AgentRestConnectManager.RestKind;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.RestUrlSequentialExecuter;
import com.clustercontrol.bean.RestHeaderConstant;
import com.clustercontrol.fault.AgentLibFileNotFound;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.SessionIdLocked;
import com.clustercontrol.rest.client.DefaultApi;

public class AgentRestClientWrapper {

	private static Log m_log = LogFactory.getLog(AgentRestClientWrapper.class);

	private static final RestKind restKind = RestKind.AgentRestEndpoints;

	public static GetHinemosTopicResponse getHinemosTopic(final AgentInfoRequest agentInfoRequest, final boolean updateNode)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass {
		m_log.debug("getHinemosTopic : start");
		RestUrlSequentialExecuter<GetHinemosTopicResponse> proxy = new RestUrlSequentialExecuter<GetHinemosTopicResponse>(restKind) {
			@Override
			public GetHinemosTopicResponse executeMethod(DefaultApi apiClient) throws Exception {
				GetHinemosTopicResponse result = apiClient.agentGetHinemosTopic(updateNode, agentInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static void deleteAgent(final AgentInfoRequest agentInfoRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		m_log.debug("deleteAgent : start");
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(restKind) {
			@Override
			public Void executeMethod(DefaultApi apiClient) throws Exception {
				apiClient.agentDeleteAgent(agentInfoRequest);
				return null;
			}
		};
		try {
			proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static void sendMessageToInternalEvent(final SendMessageRequest sendMessageRequest, String agentRequestId)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, FacilityNotFound {
		m_log.debug("sendMessageToEvent : start");
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(restKind) {
			@Override
			public Void executeMethod(DefaultApi apiClient) throws Exception {
				apiClient.getApiClient().addDefaultHeader(RestHeaderConstant.AGENT_IDENTIFIER, AgentProperties.getProperty("facilityId") + InetAddress.getLocalHost().getHostName());
				apiClient.agentSendMessageToInternalEvent(sendMessageRequest);
				return null;
			}
		};
		try {
			proxy.setAgentRequestId(agentRequestId);
			proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | FacilityNotFound def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static SetJobStartResponse setJobStart(final String sessionId, final String jobunitId, final String jobId,
			final String facilityId, final SetJobStartRequest setJobStartRequest, String agentRequestId)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, JobInfoNotFound, SessionIdLocked {
		m_log.debug("setJobStart : start");
		RestUrlSequentialExecuter<SetJobStartResponse> proxy = new RestUrlSequentialExecuter<SetJobStartResponse>(restKind) {
			@Override
			public SetJobStartResponse executeMethod(DefaultApi apiClient) throws Exception {
				apiClient.getApiClient().addDefaultHeader(RestHeaderConstant.AGENT_IDENTIFIER, AgentProperties.getProperty("facilityId") + InetAddress.getLocalHost().getHostName());
				SetJobStartResponse result = apiClient.agentSetJobStart(sessionId, jobunitId, jobId, facilityId, setJobStartRequest);
				return result;
			}
		};
		try {
			proxy.setAgentRequestId(agentRequestId);
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | JobInfoNotFound | SessionIdLocked def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static void setJobResult(final String sessionId, final String jobunitId, final String jobId,
			final String facilityId, final SetJobResultRequest setJobResultRequest, String agentRequestId)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, JobInfoNotFound, SessionIdLocked {
		m_log.debug("setJobResult : start");
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(restKind) {
			@Override
			public Void executeMethod(DefaultApi apiClient) throws Exception {
				apiClient.getApiClient().addDefaultHeader(RestHeaderConstant.AGENT_IDENTIFIER, AgentProperties.getProperty("facilityId") + InetAddress.getLocalHost().getHostName());
				apiClient.agentSetJobResult(sessionId, jobunitId, jobId, facilityId, setJobResultRequest);
				return null;
			}
		};
		try {
			proxy.setAgentRequestId(agentRequestId);
			proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | JobInfoNotFound | SessionIdLocked def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static GetMonitorForAgentResponse getMonitorLogfile(final AgentInfoRequest agentInfoRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, MonitorNotFound {
		m_log.debug("getMonitorLogfile : start");
		RestUrlSequentialExecuter<GetMonitorForAgentResponse> proxy = new RestUrlSequentialExecuter<GetMonitorForAgentResponse>(restKind) {
			@Override
			public GetMonitorForAgentResponse executeMethod(DefaultApi apiClient) throws Exception {
				GetMonitorForAgentResponse result = apiClient.agentGetMonitorLogfile(agentInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | MonitorNotFound def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static GetMonitorForAgentResponse getMonitorRpaLogfile(final AgentInfoRequest agentInfoRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, MonitorNotFound {
		m_log.debug("getMonitorRpaLogfile : start");
		RestUrlSequentialExecuter<GetMonitorForAgentResponse> proxy = new RestUrlSequentialExecuter<GetMonitorForAgentResponse>(restKind) {
			@Override
			public GetMonitorForAgentResponse executeMethod(DefaultApi apiClient) throws Exception {
				GetMonitorForAgentResponse result = apiClient.agentGetMonitorRpaLogfile(agentInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | MonitorNotFound def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public static GetMonitorForAgentResponse getMonitorCloudLog(final AgentInfoRequest agentInfoRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, MonitorNotFound {
		m_log.debug("getMonitorCloudLog : start");
		RestUrlSequentialExecuter<GetMonitorForAgentResponse> proxy = new RestUrlSequentialExecuter<GetMonitorForAgentResponse>(restKind) {
			@Override
			public GetMonitorForAgentResponse executeMethod(DefaultApi apiClient) throws Exception {
				GetMonitorForAgentResponse result = apiClient.agentGetMonitorCloudLog(agentInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | MonitorNotFound def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static GetFileCheckResponse getFileCheck(final AgentInfoRequest agentInfoRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, JobMasterNotFound {
		m_log.debug("getFileCheck : start");
		RestUrlSequentialExecuter<GetFileCheckResponse> proxy = new RestUrlSequentialExecuter<GetFileCheckResponse>(restKind) {
			@Override
			public GetFileCheckResponse executeMethod(DefaultApi apiClient) throws Exception {
				GetFileCheckResponse result = apiClient.agentGetFileCheck(agentInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | JobMasterNotFound def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static SetFileCheckResultResponse setFileCheckResult(final String kickId, final SetFileCheckResultRequest setFileCheckResultRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		m_log.debug("setFileCheckResult : start");
		RestUrlSequentialExecuter<SetFileCheckResultResponse> proxy = new RestUrlSequentialExecuter<SetFileCheckResultResponse>(restKind) {
			@Override
			public SetFileCheckResultResponse executeMethod(DefaultApi apiClient) throws Exception {
				SetFileCheckResultResponse result = apiClient.agentSetFileCheckResult(kickId, setFileCheckResultRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static GetMonitorForAgentResponse getMonitorWinEvent(final AgentInfoRequest agentInfoRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, MonitorNotFound {
		m_log.debug("getMonitorWinEvent : start");
		RestUrlSequentialExecuter<GetMonitorForAgentResponse> proxy = new RestUrlSequentialExecuter<GetMonitorForAgentResponse>(restKind) {
			@Override
			public GetMonitorForAgentResponse executeMethod(DefaultApi apiClient) throws Exception {
				GetMonitorForAgentResponse result = apiClient.agentGetMonitorWinEvent(agentInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | MonitorNotFound def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static GetMonitorCustomResponse getMonitorCustom(final Boolean forMonitorJob, final AgentInfoRequest agentInfoRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		m_log.debug("getMonitorCustom : start");
		RestUrlSequentialExecuter<GetMonitorCustomResponse> proxy = new RestUrlSequentialExecuter<GetMonitorCustomResponse>(restKind) {
			@Override
			public GetMonitorCustomResponse executeMethod(DefaultApi apiClient) throws Exception {
				GetMonitorCustomResponse result = apiClient.agentGetMonitorCustom(forMonitorJob, agentInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static File downloadAgentLib(final DownLoadAgentLibRequest downLoadAgentLibRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, AgentLibFileNotFound {
		m_log.debug("downloadAgentLib : start");
		RestUrlSequentialExecuter<File> proxy = new RestUrlSequentialExecuter<File>(restKind) {
			@Override
			public File executeMethod(DefaultApi apiClient) throws Exception {
				File result = apiClient.agentDownloadAgentLib(downLoadAgentLibRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | AgentLibFileNotFound def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static void cancelUpdate(final CancelUpdateRequest cancelUpdateRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, FacilityNotFound {
		m_log.debug("cancelUpdate : start");
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(restKind) {
			@Override
			public Void executeMethod(DefaultApi apiClient) throws Exception {
				apiClient.agentCancelUpdate(cancelUpdateRequest);
				return null;
			}
		};
		try {
			proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | FacilityNotFound def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static GetAgentLibMapResponse getAgentLibMap(final AgentInfoRequest agentInfoRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		m_log.debug("getAgentLibMap : start");
		RestUrlSequentialExecuter<GetAgentLibMapResponse> proxy = new RestUrlSequentialExecuter<GetAgentLibMapResponse>(restKind) {
			@Override
			public GetAgentLibMapResponse executeMethod(DefaultApi apiClient) throws Exception {
				GetAgentLibMapResponse result = apiClient.agentGetAgentLibMap(agentInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static void setAgentProfile(final SetAgentProfileRequest setAgentProfileRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		m_log.debug("setAgentProfile : start");
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(restKind) {
			@Override
			public Void executeMethod(DefaultApi apiClient) throws Exception {
				apiClient.agentSetAgentProfile(setAgentProfileRequest);
				return null;
			}
		};
		try {
			proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static GetScriptResponse getScript(final String sessionId, final String jobunitId, final String jobId)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, JobInfoNotFound {
		m_log.debug("getScript : start");
		RestUrlSequentialExecuter<GetScriptResponse> proxy = new RestUrlSequentialExecuter<GetScriptResponse>(restKind) {
			@Override
			public GetScriptResponse executeMethod(DefaultApi apiClient) throws Exception {
				GetScriptResponse result = apiClient.agentGetScript(sessionId, jobunitId, jobId);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | JobInfoNotFound def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static GetMonitorJobMapResponse getMonitorJobMap(final GetMonitorJobMapRequest getMonitorJobMapRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound {
		m_log.debug("getMonitorJobMap : start");
		RestUrlSequentialExecuter<GetMonitorJobMapResponse> proxy = new RestUrlSequentialExecuter<GetMonitorJobMapResponse>(restKind) {
			@Override
			public GetMonitorJobMapResponse executeMethod(DefaultApi apiClient) throws Exception {
				GetMonitorJobMapResponse result = apiClient.agentGetMonitorJobMap(getMonitorJobMapRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | MonitorNotFound def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
}
