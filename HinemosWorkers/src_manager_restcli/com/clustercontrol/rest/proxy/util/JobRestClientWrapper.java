/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.proxy.util;

import org.openapitools.client.model.RegistJobLinkMessageRequest;
import org.openapitools.client.model.RegistJobLinkMessageResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.rest.proxy.util.RestUrlSetting.RestKind;

public class JobRestClientWrapper {

	private static final RestKind restKind = RestKind.JobRestEndpoints;
	private ManagerRestConnectManager restConnectManager = null;

	public JobRestClientWrapper(String settingId, String facilityId) {
		restConnectManager = new ManagerRestConnectManager(settingId, facilityId);
	}

	public RegistJobLinkMessageResponse registJobLinkMessage(final RegistJobLinkMessageRequest sendJobLinkMessageRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		RestExecuter<RegistJobLinkMessageResponse> proxy = new RestExecuter<RegistJobLinkMessageResponse>(
				restConnectManager, restKind) {
			@Override
			public RegistJobLinkMessageResponse executeMethod(DefaultApi apiClient) throws Exception {
				RegistJobLinkMessageResponse result = apiClient
						.jobmanagementRegistJobLinkMessage(sendJobLinkMessageRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {// 想定内例外
																							// API個別に判断
			throw def;
		} catch (Exception unknown) { // 想定外の例外の場合HinemosUnknownに変換（通常ここには来ない想定）
			throw new HinemosUnknown(unknown);
		}
	}
}
