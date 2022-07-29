/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.server.ResourceConfig;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.plugin.impl.RestServiceClientPlugin;
import com.clustercontrol.plugin.util.RestApiHttpServerFactory;

public class Publisher implements AutoCloseable {

	private static Integer _httpServerHeaderSize = 8192;
	static{
		_httpServerHeaderSize = HinemosPropertyCommon.rest_httpserver_maxheadersize.getIntegerValue();
	}
	public Publisher() {
	}

	public void publish(String resourceClassName, ResourceConfig config) {
		String addressPrefix = HinemosPropertyCommon.rest_client_address.getStringValue();
		RestServiceClientPlugin restPlugin = new RestServiceClientPlugin();
		String baseUrl = restPlugin.getBaseUrl();
		publish(addressPrefix, baseUrl + "/" + resourceClassName, config, restPlugin);
	}
	
	private void publish(String addressPrefix, String addressBody, ResourceConfig resourceConfig, RestServiceClientPlugin restPlugin) {
		Map<String, HttpServer> httpServerMap = restPlugin.getHttpServerMap();
		Map<String, ResourceConfig> resourceConfigMap = restPlugin.getResourceConfigMap();
		ThreadPoolExecutor threadPool = restPlugin.getThreadPool();
		synchronized (httpServerMap) {
			try {
				final URL urlPrefix = new URL(addressPrefix);
				final String fulladdress = addressPrefix + addressBody;

				// 既に登録済みのアドレスはエラー
				if (resourceConfigMap.get(fulladdress) != null) {
					Logger.getLogger(this.getClass()).error("failed to publish : already published this address," + fulladdress);
					return;
				}

				HttpServer httpServer = httpServerMap.get(addressPrefix);
				if (httpServer == null) {
					if ("https".equals(urlPrefix.getProtocol())) {
						SSLEngineConfigurator sslEnginConfg = createSSLConfig();
						httpServer = RestApiHttpServerFactory.createHttpServer(urlPrefix.toURI(), true, sslEnginConfg);
					} else {
						httpServer = RestApiHttpServerFactory.createHttpServer(urlPrefix.toURI(), false, null);
					}
					NetworkListener listner = httpServer.getListener("grizzly");
					listner.setMaxHttpHeaderSize(_httpServerHeaderSize);
					httpServerMap.put(addressPrefix, httpServer);
				}

				// ここからが実際のAPI登録作業
				Logger.getLogger(this.getClass()).info("publish " + fulladdress);
				RestApiHttpServerFactory.addResourceConfigToServer(httpServer, addressBody, resourceConfig, threadPool);
				resourceConfigMap.put(fulladdress, resourceConfig);
			} catch (MalformedURLException | URISyntaxException | RuntimeException e) {
				Logger.getLogger(this.getClass()).warn("failed to publish : " + 
									e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			} finally {

			}
		}
	}
	
	private SSLEngineConfigurator createSSLConfig() {
		SSLContextConfigurator sslContext = new SSLContextConfigurator();
		String protocol = HinemosPropertyCommon.ws_https_protocol.getStringValue();
		sslContext.setSecurityProtocol(protocol);
		String keystorePath = HinemosPropertyCommon.ws_https_keystore_path.getStringValue();
		sslContext.setKeyStoreFile(keystorePath);
		String keystorePassword = HinemosPropertyCommon.ws_https_keystore_password.getStringValue();
		sslContext.setKeyStorePass(keystorePassword);
		String keystoreType = HinemosPropertyCommon.ws_https_keystore_type.getStringValue();
		sslContext.setKeyStoreType(keystoreType);

		Logger.getLogger(this.getClass()).info("get HinemosProperties for HTTPS server." //
				+ "ws.https.protocol=" + protocol //
				+ " , ws.https.keystore.path=" + keystorePath //
				+ " , ws.https.keystore.type=" + keystoreType);

		SSLEngineConfigurator sslEnginConfg = new SSLEngineConfigurator(sslContext).setClientMode(false)
				.setNeedClientAuth(false);

		return sslEnginConfg;
	}
	
	@Override
	public void close() {
	}
}
