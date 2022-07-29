/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.filter;

import java.io.IOException;
import java.util.Arrays;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;

/**
 * CORSを利用したリクエストを処理するフィルタです
 * 
 */

@Priority(FilterPriorities.INDIVIDUAL)
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

	private static final Log log = LogFactory.getLog(CorsFilter.class);

	// preflight request method
	private static final String OPTIONS_METHOD = "OPTIONS";
	// preflight request headers
	private static final String ORIGIN = "Origin";
	private static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
	private static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
	// preflight response headers
	private static final String VARY = "Vary";
	private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
	private static final String ACCESS_CONTROL_ALLOW_METHOD = "Access-Control-Allow-Method";
	private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";

	@Override
	public void filter(ContainerRequestContext reqContext) {

		String method = reqContext.getMethod();
		MultivaluedMap<String, String> reqHeaders = reqContext.getHeaders();

		if (log.isDebugEnabled()) {
			log.debug(String.format("method = %s", method));
			log.debug(String.format("reqHeaders = %s", reqHeaders));
		}

		if (OPTIONS_METHOD.equals(method)
				&& reqHeaders.keySet().containsAll(Arrays.asList(ORIGIN, ACCESS_CONTROL_REQUEST_METHOD))) {

			String requestMethods = reqContext.getHeaderString(ACCESS_CONTROL_REQUEST_METHOD);
			String allowHeaders = reqContext.getHeaderString(ACCESS_CONTROL_REQUEST_HEADERS);

			Response response = Response.status(Status.NO_CONTENT)
					.header(ACCESS_CONTROL_ALLOW_ORIGIN, reqContext.getHeaderString(ORIGIN))
					.header(ACCESS_CONTROL_ALLOW_METHOD, requestMethods)
					.header(ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders).header(VARY, ORIGIN).build();
			reqContext.abortWith(response);
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {

		if (requestContext.getHeaderString(ORIGIN) == null
				|| OPTIONS_METHOD.equalsIgnoreCase(requestContext.getMethod())) {
			return;
		}

		responseContext.getHeaders().add(ACCESS_CONTROL_ALLOW_ORIGIN,
				HinemosPropertyCommon.rest_cors_access_control_allow_origin.getStringValue());
		responseContext.getHeaders().add(VARY, ORIGIN);
	}
}
