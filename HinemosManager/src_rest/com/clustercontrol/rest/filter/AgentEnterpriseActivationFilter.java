/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.filter;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.util.OptionManager;

/**
 * エンタープライズ機能向けエージェントAPIの有効/無効を切り替えるためのフィルタ
 */
public class AgentEnterpriseActivationFilter implements ContainerRequestFilter {
	private static final Log m_log = LogFactory.getLog(AgentEnterpriseActivationFilter.class);

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		// エンタープライズ機能が有効化されていない場合、空のレスポンスを返す。
		if(!OptionManager.checkEnterprise()){
			m_log.debug("Enterprise feature isn't activated. request will be aborted");
			requestContext.abortWith(Response.status(Response.Status.OK).build());
		}
		m_log.debug("Enterprise feature is activated.");
	}
}
