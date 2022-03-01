/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.rest.filter.AgentEnterpriseActivationFilter;
import com.clustercontrol.rest.filter.AgentSendSettingAcquisitionFilter;
import com.clustercontrol.rest.filter.BasicAuthenticationFilter;

public class AgentRpaRestFilterRegistration implements DynamicFeature {
	private static final Log log = LogFactory.getLog(AgentRpaRestFilterRegistration.class);

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		if (log.isDebugEnabled()) {
			log.debug("AgentRpaRestFilterRegistration : resourceMethod=" + resourceInfo.getResourceClass().getName() + "#"
					+ resourceInfo.getResourceMethod().getName());
		}
		if (AgentRpaRestEndpoints.class.isAssignableFrom(resourceInfo.getResourceClass())) {
			// エンタープライズ機能の有効/無効によるフィルタを設定
			context.register(AgentEnterpriseActivationFilter.class);
			// Basic認証を設定
			context.register(BasicAuthenticationFilter.class);
			// 送信取得
			context.register(AgentSendSettingAcquisitionFilter.class);
		}
	}

}
