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

import com.clustercontrol.bean.RestHeaderConstant;
import com.clustercontrol.rest.util.RestAuthenticator;

/**
 * BEARER認証を行う（クライアント向けAPI用）
 */
@Priority(FilterPriorities.AUTHORIZATION)
public class BearerAuthenticationFilter implements ContainerRequestFilter {

	private static final Log log = LogFactory.getLog(BearerAuthenticationFilter.class);

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
			// HinemosToken独自ヘッダがあればそちらを優先的に利用、無ければAuthorizationヘッダを利用
			List<String> authorizations = reqContext.getHeaders().get(RestHeaderConstant.HINEMOS_TOKEN);
			if(authorizations == null || authorizations.isEmpty()){
				authorizations = reqContext.getHeaders().get("authorization");
			}

			// 認証の可否を判定し、NGならその旨をレスポンスとして返却
			Response checkResult = RestAuthenticator.authBearer4HttpFilter(resourceMethod, authorizations);
			if (checkResult != null) {
				if (log.isDebugEnabled()) {
					log.debug("filter() : checkBearerAuth =" + checkResult.getStatus());
				}
				reqContext.abortWith(checkResult);
			}
		} catch (Exception e) {
			log.error("filter() : Exception=" + e.getMessage());
		}
	}

}
