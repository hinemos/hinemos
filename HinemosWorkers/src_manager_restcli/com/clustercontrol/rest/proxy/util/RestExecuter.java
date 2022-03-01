/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.proxy.util;

import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.ApiException;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.rest.proxy.util.RestUrlSetting.RestKind;

public abstract class RestExecuter<T> {
	RestUrlSetting targetUrl = null;

	public RestExecuter(ManagerRestConnectManager restConnectManager, RestKind restKind) {
		this.targetUrl = new RestUrlSetting(restConnectManager, restKind);
	}

	public T proxyExecute() throws Exception {
		RestConnectFailed ape = null;
		try {
			targetUrl.connect();
			T t = executeMethod(new DefaultApi(targetUrl.getApiClient()));
			return t;
		} catch (ApiException e) {
			try {
				throw ExceptionUtil.conversionApiException(e);
			} catch (RestConnectFailed connectFail) { // マネージャ接続エラー
				ape = connectFail;
			}
		} finally {
			targetUrl.disconnect();
		}
		throw ape;
	}

	public RestUrlSetting getTargetUrl() {
		return targetUrl;
	}

	public abstract T executeMethod(DefaultApi apiClient) throws Exception;

}
