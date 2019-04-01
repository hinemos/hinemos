/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * Rest-APIの初期化(publish)/停止(stop)を制御するプラグイン.
 *
 * @since 6.2.0
 * @version 6.2.0
 */
public class RestApiPlugin implements HinemosPlugin {
	private static final Log log = LogFactory.getLog(RestApiPlugin.class);

	private static URL url = null;
	private static HttpServer server = null;

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(AsyncWorkerPlugin.class.getName());
		return dependency;
	}

	@Override
	public void create() {
		setUrl();
	}

	private static void setUrl() {
		// HinemosプロパティからURL文字列取得.
		String urlStr = HinemosPropertyCommon.rest_api_url.getStringValue();
		// URL文字列生成.
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e) {
			url = null;
			log.info("Invalid rest.api.url=" + urlStr, e);
			return;
		}
		// ポート0以下はRest-API無効.
		int port = url.getPort();
		if (port <= 0) {
			url = null;
			log.info("Invalid port in rest.api.url=" + urlStr);
			return;
		}
	}

	@Override
	public void activate() {
		if (url == null) {
			return;
		}

		// ライブラリ存在判定.
		String libPath = System.getProperty("hinemos.manager.home.dir") + File.separator + "lib" + File.separator
				+ "rest";
		File libDir = new File(libPath);
		if (!libDir.exists()) {
			log.warn("not exist required library for REST-API." + "path=[" + libPath + "]");
			String[] arg = { libPath };
			AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.REST_API,
					MessageConstant.MESSAGE_REST_API_NO_LIBRARY.getMessage(),
					MessageConstant.MESSAGE_REST_API_LIBRARY_PATH.getMessage(arg));
			return;
		}
		log.debug("exist required library for REST-API." + "path=[" + libPath + "]");

		ResourceConfig config = new ResourceConfig().packages("com.clustercontrol.repository.api");
		try {

			// プロトコル判定.
			if (url.getProtocol().equals("http")) {
				setServer(config, this.getClass().getSimpleName());
			} else if (url.getProtocol().equals("https")) {
				SSLContextConfigurator sslContext = new SSLContextConfigurator();

				String protocol = HinemosPropertyCommon.ws_https_protocol.getStringValue();
				sslContext.setSecurityProtocol(protocol);

				String keystorePath = HinemosPropertyDefault.ws_https_keystore_path.getStringValue();
				sslContext.setKeyStoreFile(keystorePath);

				String keystorePassword = HinemosPropertyCommon.ws_https_keystore_password.getStringValue();
				sslContext.setKeyStorePass(keystorePassword);

				String keystoreType = HinemosPropertyCommon.ws_https_keystore_type.getStringValue();
				sslContext.setKeyStoreType(keystoreType);

				log.info("get HinemosProperties for HTTPS server." //
						+ "ws.https.protocol=" + protocol //
						+ " , ws.https.keystore.path=" + keystorePath //
						+ " , ws.https.keystore.type=" + keystoreType);

				SSLEngineConfigurator sslEnginConfg = new SSLEngineConfigurator(sslContext).setClientMode(false)
						.setNeedClientAuth(false);
				setSecureServer(config, sslEnginConfg, this.getClass().getSimpleName());

			} else {
				log.info("Invalid protocol in rest.api.url=" + url);
				return;
			}

		} catch (Exception e) {
			log.warn("failed to activate rest.api.url=" + url, e);
			return;
		}

		log.info("successed to activate rest.api.url=" + url);

	}

	private static void setServer(ResourceConfig config, String serverName) throws URISyntaxException {
		server = GrizzlyHttpServerFactory.createHttpServer(url.toURI(), config, serverName);
	}

	private static void setSecureServer(ResourceConfig config, SSLEngineConfigurator sslEnginConfg, String serverName)
			throws URISyntaxException {
		server = GrizzlyHttpServerFactory.createHttpServer(url.toURI(), config, true, sslEnginConfg, serverName);
	}

	@Override
	public void deactivate() {
		if (server == null) {
			return;
		}
		server.shutdown();
	}

	@Override
	public Set<String> getRequiredKeys() {
		return null;
	}

	@Override
	public void destroy() {
		if (server == null) {
			return;
		}
		server.shutdown();

	}

}
