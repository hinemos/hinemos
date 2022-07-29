/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgentInfoRequest;
import org.openapitools.client.model.AgtNodeConfigRunCollectInfoResponse;
import org.openapitools.client.model.GetNodeConfigSettingResponse;
import org.openapitools.client.model.GetNodeInfoListResponse;
import org.openapitools.client.model.RegisterNodeConfigInfoRequest;
import org.openapitools.client.model.RegisterNodeRequest;
import org.openapitools.client.model.RegisterNodeResponse;

import com.clustercontrol.agent.AgentRestConnectManager.RestKind;
import com.clustercontrol.agent.util.RestUrlSequentialExecuter;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.NodeConfigSettingDuplicate;
import com.clustercontrol.fault.NodeConfigSettingNotFound;
import com.clustercontrol.fault.NodeHistoryRegistered;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.client.DefaultApi;

public class AgentNodeConfigRestClientWrapper {
	private static Log m_log = LogFactory.getLog(AgentNodeConfigRestClientWrapper.class);

	private static final RestKind restKind = RestKind.AgentNodeConfigRestEndpoints;

	public static RegisterNodeResponse registerNode(final RegisterNodeRequest registerNodeRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, HinemosDbTimeout {
		m_log.debug("registerNode : start");
		RestUrlSequentialExecuter<RegisterNodeResponse> proxy = new RestUrlSequentialExecuter<RegisterNodeResponse>(restKind) {
			@Override
			public RegisterNodeResponse executeMethod(DefaultApi apiClient) throws Exception {
				RegisterNodeResponse result = apiClient.agentNodeConfigRegisterNode(registerNodeRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | HinemosDbTimeout def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static GetNodeConfigSettingResponse getNodeConfigSetting(final AgentInfoRequest agentInfoRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting,
			FacilityNotFound, NodeConfigSettingNotFound {
		m_log.debug("getNodeConfigSetting : start");
		RestUrlSequentialExecuter<GetNodeConfigSettingResponse> proxy = new RestUrlSequentialExecuter<GetNodeConfigSettingResponse>(restKind) {
			@Override
			public GetNodeConfigSettingResponse executeMethod(DefaultApi apiClient) throws Exception {
				GetNodeConfigSettingResponse result = apiClient.agentNodeConfigGetNodeConfigSetting(agentInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting
				| FacilityNotFound | NodeConfigSettingNotFound def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static void registerNodeConfigInfo(final RegisterNodeConfigInfoRequest registerNodeConfigInfoRequest,String agentRequestId)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting,
			FacilityNotFound, NodeConfigSettingNotFound, NodeConfigSettingDuplicate, NodeHistoryRegistered {
		m_log.debug("registerNodeConfigInfo : start");
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(restKind) {
			@Override
			public Void executeMethod(DefaultApi apiClient) throws Exception {
				apiClient.agentNodeConfigRegisterNodeConfigInfo(registerNodeConfigInfoRequest);
				return null;
			}
		};
		try {
			proxy.setAgentRequestId(agentRequestId);
			proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting
				| FacilityNotFound | NodeConfigSettingNotFound | NodeConfigSettingDuplicate | NodeHistoryRegistered def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static AgtNodeConfigRunCollectInfoResponse getNodeConfigRunCollectInfo(final AgentInfoRequest agentInfoRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, FacilityNotFound {
		m_log.debug("getNodeConfigRunCollectInfo : start");
		RestUrlSequentialExecuter<AgtNodeConfigRunCollectInfoResponse> proxy = new RestUrlSequentialExecuter<AgtNodeConfigRunCollectInfoResponse>(restKind) {
			@Override
			public AgtNodeConfigRunCollectInfoResponse executeMethod(DefaultApi apiClient) throws Exception {
				AgtNodeConfigRunCollectInfoResponse result = apiClient.agentNodeConfigGetNodeConfigRunCollectInfo(agentInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | FacilityNotFound def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static void stopNodeConfigRunCollect(final AgentInfoRequest agentInfoRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, FacilityNotFound {
		m_log.debug("stopNodeConfigRunCollect : start");
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(restKind) {
			@Override
			public Void executeMethod(DefaultApi apiClient) throws Exception {
				apiClient.agentNodeConfigStopNodeConfigRunCollect(agentInfoRequest);
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

	public static GetNodeInfoListResponse getNodeInfoList(final AgentInfoRequest agentInfoRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, FacilityNotFound {
		m_log.debug("getNodeInfoList : start");
		RestUrlSequentialExecuter<GetNodeInfoListResponse> proxy = new RestUrlSequentialExecuter<GetNodeInfoListResponse>(restKind) {
			@Override
			public GetNodeInfoListResponse executeMethod(DefaultApi apiClient) throws Exception {
				GetNodeInfoListResponse result = apiClient.agentNodeConfigGetNodeInfoList(agentInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | FacilityNotFound def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

}
