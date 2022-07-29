/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.server.ResourceConfig;

import com.clustercontrol.bean.ActivationKeyConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.plugin.util.RestApiHttpServerFactory;
import com.clustercontrol.rest.exception.HinemosRestExceptionMapper;
import com.clustercontrol.rest.filter.ClientSettingAcquisitionFilter;
import com.clustercontrol.rest.filter.ClientVersionCheckFilter;
import com.clustercontrol.rest.filter.CorsFilter;
import com.clustercontrol.rest.filter.InitializeFilter;
import com.clustercontrol.util.KeyCheck;

/**
 * Rest-APIの初期化(publish)/停止(stop)を制御するプラグイン.
 */
public abstract class RestServicePlugin implements HinemosPlugin {
	private static final Log log = LogFactory.getLog(RestServicePlugin.class);

	protected static final String BASE_URL = "/HinemosWeb/api";

	private static final ThreadPoolExecutor _threadPool;

	private static Map<String, HttpServer> httpServerMap = new ConcurrentHashMap<String, HttpServer>();

	private static Map<String, ResourceConfig> resourceConfigMap = new ConcurrentHashMap<String, ResourceConfig>();

	private static Integer _httpServerHeaderSize = 8192;

	static {
		int _threadPoolSize = HinemosPropertyCommon.rest_client_threadpool_size.getIntegerValue();
		int _queueSize = HinemosPropertyCommon.rest_queue_size.getIntegerValue();

		_threadPool = new MonitoredThreadPoolExecutor(_threadPoolSize, _threadPoolSize, 0L, TimeUnit.MICROSECONDS,
				new LinkedBlockingQueue<Runnable>(_queueSize), new ThreadFactory() {
					private volatile int _count = 0;

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "RestApiWorkerForClient-" + _count++);
					}
				}, new ThreadPoolExecutor.AbortPolicy());
		_httpServerHeaderSize = HinemosPropertyCommon.rest_httpserver_maxheadersize.getIntegerValue();
	}

	protected String addressPrefix;

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		return dependency;
	}

	/**
	 * 特定のスレッドプールを使ってRestAPIを公開する関数
	 * 
	 * @param addressPrefix
	 *            公開アドレスの 「http://x.x.x.x:xxxx」 の部分
	 * @param addressBody
	 *            公開アドレスのうち addressPrefix を除いた部分
	 * @param resourceConfig
	 * @param threadPool
	 *            使用するスレッドプール
	 */
	protected void publish(String addressPrefix, String addressBody, ResourceConfig resourceConfig,
			ThreadPoolExecutor threadPool) {

		synchronized (httpServerMap) {
			try {
				final URL urlPrefix = new URL(addressPrefix);
				final String fulladdress = addressPrefix + addressBody;

				// 既に登録済みのアドレスはエラー
				if (resourceConfigMap.get(fulladdress) != null) {
					log.error("failed to publish : already published this address," + fulladdress);
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
				
				// サーバの起動
				httpServerMap.entrySet().stream().forEach(entry -> {
					try {
						entry.getValue().start();
					} catch (IOException e) {
						log.warn(String.format("publish() %s server start failed. : %s", entry.getKey(), e.getMessage()));
					}
				});
				
				// ここからが実際のAPI登録作業
				log.info("publish " + fulladdress);
				RestApiHttpServerFactory.addResourceConfigToServer(httpServer, addressBody, resourceConfig, threadPool);
				resourceConfigMap.put(fulladdress, resourceConfig);
			} catch (MalformedURLException | URISyntaxException | RuntimeException e) {
				log.warn("failed to publish : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			} finally {

			}
		}
	}

	/**
	 * デフォルトのスレッドプールを使用してWebServiceを公開する関数
	 * 
	 * @param addressPrefix
	 *            公開アドレスの 「http://x.x.x.x:xxxx」 の部分
	 * @param addressBody
	 *            公開アドレスのうち addressPrefix を除いた部分
	 * @param endpointInstance
	 */
	protected void publish(String addressPrefix, String addressBody, ResourceConfig endpointInstance) {
		publish(addressPrefix, addressBody, endpointInstance, _threadPool);
	}

	@Override
	public void deactivate() {
		/**
		 * RestAPIの停止
		 */
		
		// 許容時間まで待ちリクエストを処理しつつスレッドプールをシャットダウンする。
		shutdownOfThreadPool(_threadPool,"client", HinemosPropertyCommon.rest_client_shutdown_timeout.getNumericValue());

		//httpサーバの停止
		for (Map.Entry<String, HttpServer> entry : httpServerMap.entrySet()) {
			log.info("httpServer stop : " + entry.getKey());
			try {
				entry.getValue().shutdownNow();
			} catch (NullPointerException e) {
				log.info("stop httpServer :  " + e.getMessage());
			} catch (Exception e) {
				log.warn("stop httpServer : " + e.getMessage(), e);
			}
		}
	}

	@Override
	public Set<String> getRequiredKeys() {
		Set<String> requiredKeys = new HashSet<>();

		// ServiceLoaderのLoad順が不定のため下記クラスの存在でキーの必要有無を判断する
		try {
			Class.forName("com.clustercontrol.plugin.enterprise.EnterprisePlugin");
			requiredKeys.add(ActivationKeyConstant.TYPE_ENTERPRISE);
		} catch (ClassNotFoundException e) {
			log.debug("Not enterprise.");
		}
		
		return requiredKeys;
	}
	
	/**
	 * 必要なキーが存在するかどうかをチェックする
	 */
	protected boolean checkRequiredKeys() {
		// nullの場合はチェック不要
		if(null == getRequiredKeys())
			return true;

		for(String key: getRequiredKeys()){
			if(!KeyCheck.checkKey(key)){
				return false;
			}
		}
		return true;
	}

	@Override
	public void destroy() {
	}

	protected static ResourceConfig createResouceConfig(String[] pkgs, Class<?>[] classes) {
		if (log.isDebugEnabled()) {
			log.debug("pkgs=" + Arrays.deepToString(pkgs));
			log.debug("classes=" + Arrays.deepToString(classes));
		}
		return new ResourceConfig().packages(pkgs).registerClasses(classes);
	}

	protected static SSLEngineConfigurator createSSLConfig() {
		SSLContextConfigurator sslContext = new SSLContextConfigurator();
		String protocol = HinemosPropertyCommon.ws_https_protocol.getStringValue();
		sslContext.setSecurityProtocol(protocol);
		String keystorePath = HinemosPropertyCommon.ws_https_keystore_path.getStringValue();
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

		return sslEnginConfg;
	}

	protected void setProperties() {
		// jax-rs に関するプロパティを設定する処理を書く想定
	}
	
	/**
	 * 特定のスレッドプールのshutdownを行う
	 * 
	 * @param threadPool
	 *            対象スレッドプール
	 * @param poolName
	 *            プールの名称(ログ用)
	 * @param shutdownTimeout
	 *            シャットダウン待ちのタイムアウト時間
	 */
	protected static void shutdownOfThreadPool(ThreadPoolExecutor threadPool, String poolName, long shutdownTimeout) {

		threadPool.shutdown();
		try {

			if (!threadPool.awaitTermination(shutdownTimeout, TimeUnit.MILLISECONDS)) {
				List<Runnable> remained = threadPool.shutdownNow();
				if (remained != null) {
					log.info("shutdown timeout. runnable remained. ( poolName = " + poolName + ",  size = "
							+ remained.size() + ")");
				}
			}
		} catch (InterruptedException e) {
			threadPool.shutdownNow();
		}
	}

	public String getBaseUrl() {
		return BASE_URL;
	}
	
	public ThreadPoolExecutor getThreadPool() {
		return _threadPool;
	}
	
	public static ThreadPoolExecutor getClientPool() {
		return _threadPool;
	} 
	
	public Map<String, HttpServer> getHttpServerMap() {
		return httpServerMap;
	}
	
	public Map<String, ResourceConfig> getResourceConfigMap() {
		return resourceConfigMap;
	}
	
	public static int getQueueSize() {
		return _threadPool.getQueue().size();
	}

	public Set<Class<?>> commonRegisterClasses() {
		Set<Class<?>> classSet = new HashSet<>();
		classSet.add(HinemosRestExceptionMapper.class);
		classSet.add(ClientSettingAcquisitionFilter.class);
		classSet.add(ClientVersionCheckFilter.class);
		classSet.add(InitializeFilter.class);
		classSet.add(CorsFilter.class);
		return classSet;
	}
}
