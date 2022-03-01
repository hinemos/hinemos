/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.util;

import java.net.URI;
import java.util.concurrent.ThreadPoolExecutor;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.grizzly.http.server.ErrorPageGenerator;
import org.glassfish.grizzly.http.server.HttpHandlerRegistration;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.rest.exception.ExceptionBody;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 *
 */
public class RestApiHttpServerFactory {
	
	private static final Log log = LogFactory.getLog(RestApiHttpServerFactory.class);
	/**
	 * Prevents instantiation.
	 */
	private RestApiHttpServerFactory() {
	}

	/**
	 * HttpServerの作成.
	 * 
	 * @param uri
	 *            URI
	 * @param secure
	 *            SSL有無
	 * @@param sslEngineConfigurator SSL用設定
	 * @return HttpServer
	 */
	public static HttpServer createHttpServer(final URI uri, final boolean secure,
			final SSLEngineConfigurator sslEngineConfigurator) {
		HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, (RestApiHttpContainer) null, secure,
				sslEngineConfigurator, false);
		
		ErrorPageGenerator generator = getDefaultErrorResponse();
		if(generator != null) {
			server.getServerConfiguration().setDefaultErrorPageGenerator(generator);
		}
		return server;
	}

	/**
	 * HttpServerへのResourceConfigの追加
	 * 
	 * 同一HttpServerへ同じcontextPathを指定した場合は後勝ちになるので注意すること
	 * 
	 * @param httpServer
	 *            HttpServer
	 * @param contextPath
	 *            コンテキストパス
	 * @param resourceConfig
	 *            ResourceConfig
	 * @param threadPool
	 *            スレッドプール
	 * @return
	 */
	public static HttpServer addResourceConfigToServer(HttpServer httpServer, String contextPath,
			final ResourceConfig resourceConfig, ThreadPoolExecutor threadPool) {
		final ServerConfiguration config = httpServer.getServerConfiguration();

		config.addHttpHandler(new RestApiHttpContainer(resourceConfig, threadPool),
				HttpHandlerRegistration.builder().contextPath(contextPath).build());

		return httpServer;
	}
	
	/**
	 * デフォルトエラーレスポンスを生成する
	 */
	private static ErrorPageGenerator getDefaultErrorResponse() {
		
		return new ErrorPageGenerator() {
			
			@Override
			public String generate(Request request, int status, String reasonPhrase, String description, Throwable exception) {
				String responseJson = null;
				try {
					ExceptionBody eb = null;
					// exception は null のことがあるためチェックする
					if(exception != null) {
						eb = new ExceptionBody(status, exception);
						eb.setMessage(reasonPhrase);
					} else {
						// クライアントでNullPointerExceptionが発生するためExceptionを設定する
						// 404の場合UrlNotFoundとし、それ以外はHinemosUnknownとする
						if(status == Status.NOT_FOUND.getStatusCode()) {
							eb = new ExceptionBody(status, new UrlNotFound());
						} else {
							eb = new ExceptionBody(status, new HinemosUnknown());
						}
						eb.setMessage(reasonPhrase);
					}
					ObjectMapper mapper = new ObjectMapper();
					mapper.setSerializationInclusion(Include.NON_NULL);
					responseJson = mapper.writeValueAsString(eb); 
				} catch (JsonProcessingException e) {
					log.warn("getDefaultErrorResponse() failed. : " + e.getMessage());
				}
				
				log.debug("getDefaultErrorResponse() responseJson = " + responseJson);
				return responseJson;
			}
		};
	}
}
