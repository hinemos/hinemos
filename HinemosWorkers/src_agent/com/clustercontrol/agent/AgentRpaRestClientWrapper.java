/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.ForwardLogfileResultRequest;
import org.openapitools.client.model.ForwardRpaScreenshotRequest;

import com.clustercontrol.agent.AgentRestConnectManager.RestKind;
import com.clustercontrol.agent.util.RestUrlSequentialExecuter;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.client.DefaultApi;

public class AgentRpaRestClientWrapper {

	private static Log m_log = LogFactory.getLog(AgentRpaRestClientWrapper.class);

	private static final RestKind restKind = RestKind.AgentRpaRestEndpoints;

	/**
	 * スクリーンショットを登録します。
	 * @param forwardRpaScreenshotRequest スクリーンショット情報のDTO
	 * @param file スクリーンショットのファイル
	 * @throws RestConnectFailed
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws InvalidSetting
	 * @throws MonitorNotFound
	 */
	public static void forwardRpaScreenshot(final ForwardRpaScreenshotRequest forwardRpaScreenshotRequest, final File file)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting, MonitorNotFound {
		m_log.debug("forwardRpaScreenshot : start");
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(restKind) {
			@Override
			public Void executeMethod(DefaultApi apiClient) throws Exception {
				ForwardRpaScreenshotRequest request = new com.clustercontrol.agent.ForwardRpaScreenshotJsonRequest();
				request.setSessionId(forwardRpaScreenshotRequest.getSessionId());
				request.setJobunitId(forwardRpaScreenshotRequest.getJobunitId());
				request.setJobId(forwardRpaScreenshotRequest.getJobId());
				request.setFacilityId(forwardRpaScreenshotRequest.getFacilityId());
				request.setTriggerType(forwardRpaScreenshotRequest.getTriggerType());
				request.setOutputDate(forwardRpaScreenshotRequest.getOutputDate());
				apiClient.agentRpaForwardRpaScreenshot(file, request);
				return null;
			}
		};
		try {
			proxy.proxyExecute();
		} catch (RestConnectFailed | HinemosUnknown | InvalidRole | InvalidUserPass | InvalidSetting
				| MonitorNotFound def) {// 想定内例外
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
	
	public static void forwardRpaLogfileResult(final ForwardLogfileResultRequest forwardLogfileResultRequest, String agentRequestId)
			throws RestConnectFailed, HinemosUnknown, InvalidRole, InvalidUserPass, InvalidSetting {
		m_log.debug("forwardRpaLogfileResult : start");
		RestUrlSequentialExecuter<Void> proxy = new RestUrlSequentialExecuter<Void>(restKind) {
			@Override
			public Void executeMethod(DefaultApi apiClient) throws Exception {
				apiClient.agentRpaForwardRpaLogfileResult(forwardLogfileResultRequest);
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
