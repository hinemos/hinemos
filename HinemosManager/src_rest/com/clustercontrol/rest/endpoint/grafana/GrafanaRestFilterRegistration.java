/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.grafana;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.rest.filter.BearerAuthenticationFilter;
import com.clustercontrol.rest.filter.ClientSettingAcquisitionFilter;
import com.clustercontrol.rest.filter.ClientVersionCheckFilter;

public class GrafanaRestFilterRegistration implements DynamicFeature {
	private static final Log log = LogFactory.getLog(GrafanaRestFilterRegistration.class);
	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		if(log.isDebugEnabled()){
			log.debug("HubRestFilterRegistration : resourceMethod="+resourceInfo.getResourceClass().getName()+"#"+resourceInfo.getResourceMethod().getName());
		}
		if (GrafanaRestEndpoints.class.isAssignableFrom(resourceInfo.getResourceClass())) {
			//Bearer認証を設定
			context.register(BearerAuthenticationFilter.class );
			//ヘッダからクライアント向けの設定を取得する。
			context.register(ClientSettingAcquisitionFilter.class );
			//Hinemosクライアントとマネージャのメジャーバージョン一致をチェックする。
			context.register(ClientVersionCheckFilter.class );
		}
	}

}
