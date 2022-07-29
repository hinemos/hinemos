/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.util;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.ApiException;
import com.clustercontrol.rest.client.SdmlApi;
import com.clustercontrol.util.ExceptionUtil;
import com.clustercontrol.util.RestConnectUnit;
import com.clustercontrol.util.RestConnectUnit.RestUrlSetting;

public abstract class SdmlRestUrlSequentialExecuter<T> {

	private RestConnectUnit connectUnit = null;
	private RestKind restKind = null;
	private RestUrlSetting targetUrl = null;
	private String restDatetimeFormat = null;

	public SdmlRestUrlSequentialExecuter(RestConnectUnit connectUnit, RestKind restKind) {
		this.restKind = restKind;
		this.connectUnit = connectUnit;
	}

	public SdmlRestUrlSequentialExecuter(RestConnectUnit connectUnit, RestKind restKind, String restDatetimeFormat) {
		this.restKind = restKind;
		this.connectUnit = connectUnit;
		this.restDatetimeFormat = restDatetimeFormat;
	}

	public T proxyExecute() throws Exception {
		RestConnectFailed ape = null;
		for (RestUrlSetting urlSetting : connectUnit.getUrlSettingList(restKind)) {
			try {
				targetUrl = urlSetting;
				if (restDatetimeFormat != null) {
					urlSetting.setRestDatetimeFormat(restDatetimeFormat);
				}
				SdmlApi apiClient = new SdmlApi(urlSetting.getApiClient());
				return executeMethod(apiClient);
			} catch (ApiException e) {
				try {
					throw ExceptionUtil.conversionApiException(e);
				} catch (RestConnectFailed connectFail) { // マネージャ接続エラー
					ape = connectFail;
					connectUnit.setUnreached(urlSetting);
				}
			}
		}
		throw ape;
	}

	public RestUrlSetting getTargetUrl() {
		return targetUrl;
	}

	public abstract T executeMethod(SdmlApi apiClient) throws Exception;
}
