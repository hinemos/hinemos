/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.RestHeaderConstant;
import com.clustercontrol.commons.util.HinemosSessionContext;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

/**
 * httpヘッダーから処理に必要な設定情報を取得し、スレッドローカルに保存する（エージェントからの情報送信API用）
 */
@Priority(FilterPriorities.HEADER_DETECTOR)
public class AgentSendSettingAcquisitionFilter implements ContainerRequestFilter {
	private static final Log log = LogFactory.getLog(AgentSendSettingAcquisitionFilter.class);

	@Override
	public void filter(ContainerRequestContext requestContext) {
		String requestId = requestContext.getHeaders().getFirst(RestHeaderConstant.AGENT_REQUEST_ID);
		String agentId = requestContext.getHeaders().getFirst(RestHeaderConstant.AGENT_IDENTIFIER);
		HinemosSessionContext.instance().setProperty(RestHeaderConstant.AGENT_REQUEST_ID,requestId );
		if (log.isDebugEnabled()) {
			log.debug("filter() : " + RestHeaderConstant.AGENT_REQUEST_ID + " = " + requestId);
		}
		HinemosSessionContext.instance().setProperty(RestHeaderConstant.AGENT_IDENTIFIER,agentId );
		if (log.isDebugEnabled()) {
			log.debug("filter() : " + RestHeaderConstant.AGENT_IDENTIFIER + " = " + agentId);
		}
	}
}