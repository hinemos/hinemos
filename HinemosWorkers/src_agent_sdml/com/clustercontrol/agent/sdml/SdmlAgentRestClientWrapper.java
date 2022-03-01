/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.sdml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgentInfoRequest;
import org.openapitools.client.model.ForwardSdmlControlLogRequest;
import org.openapitools.client.model.GetSdmlControlSettingForAgentResponse;
import org.openapitools.client.model.SendSdmlMessageRequest;

import com.clustercontrol.agent.AgentRestConnectManager.RestKind;
import com.clustercontrol.agent.sdml.util.SdmlRestUrlSequentialExecuter;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.client.AgentSdmlApi;

public class SdmlAgentRestClientWrapper {

	private static Log m_log = LogFactory.getLog(SdmlAgentRestClientWrapper.class);

	private static final RestKind restKind = RestKind.AgentSdmlRestEndpoints;

	public static GetSdmlControlSettingForAgentResponse getSdmlControlSettingV1(final AgentInfoRequest agentInfoRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, MonitorNotFound {
		m_log.debug("getSdmlControlSettingV1 : start");
		SdmlRestUrlSequentialExecuter<GetSdmlControlSettingForAgentResponse> proxy = new SdmlRestUrlSequentialExecuter<GetSdmlControlSettingForAgentResponse>(
				restKind) {
			@Override
			public GetSdmlControlSettingForAgentResponse executeMethod(AgentSdmlApi apiClient) throws Exception {
				GetSdmlControlSettingForAgentResponse result = apiClient
						.agentSdmlGetSdmlControlSetting(agentInfoRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting
				| MonitorNotFound def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static void forwardSdmlControlLog(final ForwardSdmlControlLogRequest forwardSdmlControlLogRequest,
			String agentRequestId)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		m_log.debug("forwardSdmlControlLog : start");
		SdmlRestUrlSequentialExecuter<Void> proxy = new SdmlRestUrlSequentialExecuter<Void>(restKind) {
			@Override
			public Void executeMethod(AgentSdmlApi apiClient) throws Exception {
				apiClient.agentSdmlForwardSdmlControlLog(forwardSdmlControlLogRequest);
				return null;
			}
		};
		try {
			proxy.setAgentRequestId(agentRequestId);
			proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static void agentSdmlSendSdmlMessage(final SendSdmlMessageRequest sendSdmlMessageRequest,
			String agentRequestId)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		m_log.debug("agentSdmlSendSdmlMessage : start");
		SdmlRestUrlSequentialExecuter<Void> proxy = new SdmlRestUrlSequentialExecuter<Void>(restKind) {
			@Override
			public Void executeMethod(AgentSdmlApi apiClient) throws Exception {
				apiClient.agentSdmlSendSdmlMessage(sendSdmlMessageRequest);
				return null;
			}
		};
		try {
			proxy.setAgentRequestId(agentRequestId);
			proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
}
