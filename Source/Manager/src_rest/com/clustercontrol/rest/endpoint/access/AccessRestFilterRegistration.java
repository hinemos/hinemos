/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.rest.filter.BearerAuthenticationFilter;
import com.clustercontrol.rest.filter.ClientSettingAcquisitionFilter;
import com.clustercontrol.rest.filter.ClientVersionCheckFilter;

@Provider
public class AccessRestFilterRegistration  implements DynamicFeature {
	private static final Log log = LogFactory.getLog(AccessRestFilterRegistration.class);
	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		if(log.isDebugEnabled()){
			log.debug("AccessRestFilterRegistration : resourceMethod="+resourceInfo.getResourceClass().getName()+"#"+resourceInfo.getResourceMethod().getName());
		}
		if (AccessRestEndpoints.class.isAssignableFrom(resourceInfo.getResourceClass())) {
			//Bearer認証を設定（ログイン以外すべて）
			if( !(resourceInfo.getResourceMethod().getName().equals("login"))
					&& !(resourceInfo.getResourceMethod().getName().equals("healthCheck")) ){
				context.register(BearerAuthenticationFilter.class );
			}
			//ヘッダからクライアント向けの設定を取得する。
			context.register(ClientSettingAcquisitionFilter.class );
			
			//Hinemosクライアントとマネージャのメジャーバージョン一致をチェックする。
			context.register(ClientVersionCheckFilter.class );
		}
	}

}
