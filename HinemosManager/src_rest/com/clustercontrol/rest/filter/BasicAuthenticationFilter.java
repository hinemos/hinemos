/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.filter;

import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.rest.util.RestAuthenticator;

/**
 * BASIC認証を行う（エージェント向けAPI用）
 */
@Priority(FilterPriorities.AUTHORIZATION)
public class BasicAuthenticationFilter implements ContainerRequestFilter {

	private static final Log log = LogFactory.getLog(BasicAuthenticationFilter.class);

	@Context
	private ResourceInfo resourceInfo;

	@Override
	public void filter(ContainerRequestContext reqContext) {
		try {

			// リソース情報から対象メソッドを取得
			Method resourceMethod = resourceInfo.getResourceMethod();

			if (log.isDebugEnabled()) {
				log.debug("filter() : resourceMethod=" + resourceMethod.getName());
			}
			// 認証ヘッダを取得
			List<String> authorizations = reqContext.getHeaders().get("authorization");

			// 認証の可否を判定し、NGならその旨をレスポンスとして返却
			Response checkResult = RestAuthenticator.authBasic4HttpFilter(resourceMethod, authorizations);
			if (checkResult != null) {
				if (log.isDebugEnabled()) {
					log.debug("filter() : checkBasicAuth =" + checkResult.getStatus());
				}

				reqContext.abortWith(checkResult);
			}
		} catch (Exception e) {
			log.error("filter() : Exception=" + e.getMessage());
		}
	}

}
