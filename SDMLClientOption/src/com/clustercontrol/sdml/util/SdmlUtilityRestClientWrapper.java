/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.util;

import org.openapitools.client.model.ImportSdmlControlRequest;
import org.openapitools.client.model.ImportSdmlControlResponse;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.client.SdmlApi;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.RestConnectUnit;

/**
 * SDMLのUtility向けのREST用ラッパークラス
 *
 */
public class SdmlUtilityRestClientWrapper {

	private RestConnectUnit connectUnit;

	private final RestKind restKind = RestKind.SdmlUtilityRestEndpoints;

	public static SdmlUtilityRestClientWrapper getWrapper(String managerName) {
		return new SdmlUtilityRestClientWrapper(RestConnectManager.getActive(managerName));
	}

	public SdmlUtilityRestClientWrapper(RestConnectUnit endpointUnit) {
		this.connectUnit = endpointUnit;
	}

	public ImportSdmlControlResponse importSdmlControlSettingV1(ImportSdmlControlRequest importSdmlControlRequest)
			throws RestConnectFailed, InvalidUserPass, InvalidRole, HinemosUnknown {
		SdmlRestUrlSequentialExecuter<ImportSdmlControlResponse> proxy = new SdmlRestUrlSequentialExecuter<ImportSdmlControlResponse>(
				this.connectUnit, this.restKind) {
			@Override
			public ImportSdmlControlResponse executeMethod(SdmlApi apiClient) throws Exception {
				ImportSdmlControlResponse result = apiClient.utilityImportSdmlControlSettingV1(importSdmlControlRequest);
				return result;
			}
		};
		try {
			return proxy.proxyExecute();
		} catch (RestConnectFailed | InvalidUserPass | InvalidRole | HinemosUnknown def) {
			throw def;
		} catch (Exception unknown) {
			throw new HinemosUnknown(unknown);
		}
	}

}
