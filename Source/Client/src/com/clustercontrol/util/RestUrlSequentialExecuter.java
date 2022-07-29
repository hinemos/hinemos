/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.util;

import com.clustercontrol.bean.RestKind;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.ApiException;
import com.clustercontrol.rest.client.DefaultApi;
import com.clustercontrol.util.RestConnectUnit.RestUrlSetting;

public abstract class RestUrlSequentialExecuter<T>{

	private RestConnectUnit connectUnit = null; 
	private RestKind restKind = null; 
	private RestUrlSetting targetUrl = null; 
	private String restDatetimeFormat =null;

	public RestUrlSequentialExecuter( RestConnectUnit connectUnit, RestKind restKind) {
		this.restKind = restKind;
		this.connectUnit = connectUnit;
	}
	public RestUrlSequentialExecuter( RestConnectUnit connectUnit, RestKind restKind, String restDatetimeFormat ) {
		this.restKind = restKind;
		this.connectUnit = connectUnit;
		this.restDatetimeFormat = restDatetimeFormat;
	}
	public T proxyExecute() throws Exception{
		RestConnectFailed ape = null;
		for (RestUrlSetting urlSetting : connectUnit.getUrlSettingList(restKind) ){
			try {
				targetUrl = urlSetting;
				if(restDatetimeFormat!=null){
					urlSetting.setRestDatetimeFormat(restDatetimeFormat);
				}
				DefaultApi apiClient = new DefaultApi(urlSetting.getApiClient());
				return executeMethod( apiClient );
			} catch (ApiException e) {
				try {
					throw ExceptionUtil.conversionApiException(e);
				} catch (RestConnectFailed connectFail) { //マネージャ接続エラー
					ape = connectFail;
					connectUnit.setUnreached(urlSetting);
				}
			}
		}
		throw ape;
	}
	public RestUrlSetting getTargetUrl(){
		return targetUrl;
	}
	
	public abstract T executeMethod( DefaultApi apiClient ) throws Exception ;
}

