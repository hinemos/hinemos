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
import org.openapitools.client.model.ForwardBinaryResultRequest;
import org.openapitools.client.model.GetMonitorForAgentResponse;

import com.clustercontrol.agent.AgentRestConnectManager.RestKind;
import com.clustercontrol.agent.util.RestUrlSequentialExecuter;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.client.DefaultApi;

public class AgentBinaryRestClientWrapper {
	private static Log m_log = LogFactory.getLog(AgentBinaryRestClientWrapper.class);

	private static final RestKind restKind = RestKind.AgentBinaryRestEndpoints;

	public static GetMonitorForAgentResponse getMonitorBinary(final AgentInfoRequest agentInfoRequest)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, MonitorNotFound {
		m_log.debug("getMonitorBinary : start");
		RestUrlSequentialExecuter<GetMonitorForAgentResponse> proxy = new RestUrlSequentialExecuter<GetMonitorForAgentResponse>(restKind) {
			@Override
			public GetMonitorForAgentResponse executeMethod(DefaultApi apiClient) throws Exception {
				GetMonitorForAgentResponse result = apiClient.agentBinaryGetMonitorBinary(agentInfoRequest);
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

	public static void forwardBinaryResult(final ForwardBinaryResultRequest forwardBinaryResultRequest, String agentRequestId)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		m_log.debug("forwardBinaryResult : start");
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(restKind) {
			@Override
			public Void executeMethod(DefaultApi apiClient) throws Exception {
				apiClient.agentBinaryForwardBinaryResult(forwardBinaryResultRequest);
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
