/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util;

import com.clustercontrol.agent.AgentRestConnectManager;
import com.clustercontrol.agent.AgentRestConnectManager.RestKind;
import com.clustercontrol.agent.AgentRestConnectManager.RestUrlSetting;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.rest.ApiException;
import com.clustercontrol.rest.client.DefaultApi;

public abstract class RestUrlSequentialExecuter<T>{
	RestKind restKind = null; 
	RestUrlSetting targetUrl = null; 
	String agentRequestId =null;

	public RestUrlSequentialExecuter( RestKind restKind) {
		this.restKind = restKind;
	}
	public T proxyExecute() throws Exception{
		RestConnectFailed ape = null;
		for (RestUrlSetting urlSetting : AgentRestConnectManager.getUrlSettingList(restKind) ){
			try {
				targetUrl = urlSetting;
				if (agentRequestId != null) {
					targetUrl.setAgentRequestId(agentRequestId);
				}
				DefaultApi apiClient = new DefaultApi(urlSetting.getApiClient());
				return executeMethod( apiClient );
			} catch (ApiException e) {
				try {
					throw ExceptionUtil.conversionApiException(e);
				} catch (RestConnectFailed connectFail) { //マネージャ接続エラー
					ape = connectFail;
					AgentRestConnectManager.changeConnectUrl();
				}
			}
		}
		throw ape;
	}
	public RestUrlSetting getTargetUrl(){
		return targetUrl;
	}
	public void setAgentRequestId(String agentRequestId ){
		this.agentRequestId = agentRequestId;
		return ;
	}
	
	public abstract T executeMethod( DefaultApi apiClient ) throws Exception ;

}
