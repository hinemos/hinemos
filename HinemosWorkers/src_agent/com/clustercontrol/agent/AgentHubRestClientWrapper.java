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
import org.openapitools.client.model.ForwardCloudLogResultRequest;
import org.openapitools.client.model.ForwardCustomResultRequest;
import org.openapitools.client.model.ForwardLogfileResultRequest;
import org.openapitools.client.model.ForwardWinEventResultRequest;

import com.clustercontrol.agent.AgentRestConnectManager.RestKind;
import com.clustercontrol.agent.util.RestUrlSequentialExecuter;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.fault.ValidAgentFacilityNotFound;
import com.clustercontrol.rest.client.DefaultApi;

public class AgentHubRestClientWrapper {
	private static Log m_log = LogFactory.getLog(AgentRestClientWrapper.class);

	private static final RestKind restKind = RestKind.AgentHubRestEndpoints;

	public static void forwardCustomResult(final ForwardCustomResultRequest forwardCustomResultRequest, String agentRequestId)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, MonitorNotFound {
		m_log.debug("forwardCustomResult : start");
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(restKind) {
			@Override
			public Void executeMethod(DefaultApi apiClient) throws Exception {
				apiClient.agentHubForwardCustomResult(forwardCustomResultRequest);
				return null;
			}
		};
		try {
			proxy.setAgentRequestId(agentRequestId);
			proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | MonitorNotFound def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}

	public static void forwardLogfileResult(final ForwardLogfileResultRequest forwardLogfileResultRequest, String agentRequestId)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		m_log.debug("forwardLogfileResult : start");
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(restKind) {
			@Override
			public Void executeMethod(DefaultApi apiClient) throws Exception {
				apiClient.agentHubForwardLogfileResult(forwardLogfileResultRequest);
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

	public static void forwardCloudLogResult(final ForwardCloudLogResultRequest forwardCloudLogResultRequest, String agentRequestId)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, ValidAgentFacilityNotFound {
		m_log.debug("forwardCloudLogResult : start");
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(restKind) {
			@Override
			public Void executeMethod(DefaultApi apiClient) throws Exception {
				apiClient.agentHubForwardCloudLogResult(forwardCloudLogResultRequest);
				return null;
			}
		};
		try {
			proxy.setAgentRequestId(agentRequestId);
			proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting | ValidAgentFacilityNotFound def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public static void forwardWinEventResult(final ForwardWinEventResultRequest forwardWineventResultRequest, String agentRequestId)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		m_log.debug("forwardWinEventResult : start");
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(restKind) {
			@Override
			public Void executeMethod(DefaultApi apiClient) throws Exception {
				apiClient.agentHubForwardWinEventResult(forwardWineventResultRequest);
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
