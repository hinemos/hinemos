/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.ws.Endpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.ActivationKeyConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.plugin.HinemosPluginService;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.util.KeyCheck;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

/**
 * JAX-WSによるWEBサービスの初期化(publish)/停止(stop)を制御する基底プラグイン.
 *
 */
public abstract class WebServicePlugin implements HinemosPlugin {

	public static final Log log = LogFactory.getLog(WebServicePlugin.class);

	private static final ArrayList<Endpoint> endpointList = new ArrayList<Endpoint>();

	private static PluginStatus status = PluginStatus.NULL;

	public static PluginStatus getStatus() {
		return status;
	}

	public static void setStatus(PluginStatus status) {
		WebServicePlugin.status = status;
	}

	/** HTTPS通信時に利用 */
	private static ConcurrentHashMap<String, HttpsServer> httpsServerMap =
			new ConcurrentHashMap<String, HttpsServer>();

	/**
	 * 特定のスレッドプールを使ってWebServiceを公開する関数（Agent用スレッドのみ別スレッドとするために作成）
	 * @param addressPrefix 公開アドレスの 「http://x.x.x.x:xxxx」 の部分
	 * @param addressBody 公開アドレスのうち addressPrefix を除いた部分
	 * @param endpointInstance
	 * @param threadPool 使用するスレッドプール
	 */
	protected void publish(String addressPrefix, String addressBody, Object endpointInstance, ThreadPoolExecutor threadPool) {

		try {
			final URL urlPrefix = new URL(addressPrefix);
			final String fulladdress = addressPrefix + addressBody;
			if (log.isDebugEnabled()) {
				log.debug("publish:HTTPS Server urlPrefix=" + urlPrefix.toString() + ",addressPrefix=" + addressPrefix + ",fulladdress=" + fulladdress);
			}
			HttpsServer httpsServer = null;
			// プロトコルが HTTPSの場合には、まずHttpsServiceオブジェクトを作り、それをendpoit.publishに渡す必要がある。
			// URLとポートがまったく同じHttpsServiceを複数作れないので、Hashmapにて重複管理し、もしもHashMapに
			// HTTPSServerが存在する場合にはそれを使いまわす。
			if ("https".equals(urlPrefix.getProtocol())) {
				httpsServer = httpsServerMap.get(addressPrefix);
				if (httpsServer == null) {
					// HTTPS Serverの作成（HTTPSサーバの開始は、後で一括して行うため、ここではインスタンスの生成のみに留める
					String protocol = HinemosPropertyCommon.ws_https_protocol.getStringValue();
					String keystorePath = HinemosPropertyDefault.ws_https_keystore_path.getStringValue();
					String keystorePassword = HinemosPropertyCommon.ws_https_keystore_password.getStringValue();
					String keystoreType = HinemosPropertyCommon.ws_https_keystore_type.getStringValue();
					log.info("Starting HTTPS Server...");
					log.info("SSLContext: " + protocol + ", KeyStore: " + keystoreType);
					SSLContext ssl = SSLContext.getInstance(protocol);
					KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
					KeyStore store = KeyStore.getInstance(keystoreType);
					try (InputStream in = new FileInputStream(keystorePath)) {
						store.load(in, keystorePassword.toCharArray());
					}
					keyFactory.init(store, keystorePassword.toCharArray());
					TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					trustFactory.init(store);
					ssl.init(keyFactory.getKeyManagers(), trustFactory.getTrustManagers(), new SecureRandom());
					HttpsConfigurator configurator = new HttpsConfigurator(ssl);

					// 新規にHTTPSSeverを作って、Hashmapに登録する
					httpsServer = HttpsServer.create(new InetSocketAddress(urlPrefix.getHost(), urlPrefix.getPort()), 0);
					httpsServer.setHttpsConfigurator(configurator);
					// HTTPSSeverにExchangeタスク向けのExecutorを割り付ける。
					setHttpsExchangeExecuter(addressPrefix, httpsServer);
					HttpsServer preSet = httpsServerMap.putIfAbsent(addressPrefix, httpsServer);
					if (preSet != null) {
						httpsServer = preSet;
					}
				}
			}

			// ここからが実際のendpointへの登録作業
			log.info("publish " + fulladdress);
			final Endpoint endpoint = Endpoint.create(endpointInstance);
			endpoint.setExecutor(threadPool);
			if (httpsServer != null) {
				endpoint.publish(httpsServer.createContext(addressBody));
			} else {
				endpoint.publish(fulladdress);
			}
			endpointList.add(endpoint);
		} catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | KeyManagementException | IOException | CertificateException | RuntimeException e) {
			log.warn("failed to publish : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			
		}
	}

	@Override
	public void deactivate() {
	}

	@Override
	public void destroy() {

	}

	protected void startHTTPS() {
		for(final HttpsServer server : httpsServerMap.values()) {
			server.start();
		}
	}

	@Override
	public Set<String> getRequiredKeys() {
		Set<String> requiredKeys = new HashSet<>();

		// EnterprisePluginが既に活性化された場合、TYPE_ENTERPRISEキーが必要となる
		if(HinemosPluginService.isActive(EnterprisePlugin.class)){
			requiredKeys.add(ActivationKeyConstant.TYPE_ENTERPRISE);
		}
		return requiredKeys;
	}

	/**
	 * 必要なキーが存在するかどうかをチェックする
	 */
	protected boolean checkRequiredKeys() {
		// findbugs対応 getRequiredKeys()が nullの場合はチェック不要だが、現状nullはあり得ないのでチェック廃止
		for(String key: getRequiredKeys()){
			if(!KeyCheck.checkKey(key)){
				return false;
			}
		}
		return true;
	}

	/**
	 * HTTPSSeverにExchangeタスク向けのExecutorを割り付ける。<br>
	 *  スレッド最大数とタスクキュー最大数はプロパティで調整可能。<br>
	 *  
	 *  HTTPSSeverではExchangeタスクがSSLハンドシェイク中にスレッドを長時間ロックするケースが有り、<br>
	 *  その際に後続リクエストが滞留するので、対応としてExchangeタスクの担当Threadを別途Executor化（複数並列可）する。<br>
	 *  
	 *  Executorはセルフチェックにて監視される。（MonitoredThreadPoolExecutor）<br>
	 *  
	 *  タスクキューがあふれた場合には、警告ログを出力可能としておく。<br>
	 *  
	 * @param addressPrefix 待ち受けアドレス先頭部（https://xxxx.xxxxx.xxxx:nnnn まで）
	 * @param httpsServer  Executorを設定するhttpsServerインスタンス
	 */
	static public void setHttpsExchangeExecuter(String addressPrefix, HttpsServer httpsServer) {

		final int executorThreadSize = HinemosPropertyCommon.ws_https_request_exchange_thread_size.getNumericValue().intValue();
		final int executorQueueSize = HinemosPropertyCommon.ws_https_request_exchange_queue_size.getNumericValue().intValue();
		final String executerBaseName = "https-exchange-" + addressPrefix + "-";
		final String rejectAddrName = addressPrefix;
		if (log.isDebugEnabled()) {
			log.debug("setHttpsExchangeExecuter: addressPrefix=" + addressPrefix + " executorThreadSize=" + executorThreadSize + " executorQueueSize=" + executorQueueSize);
		}

		Executor httpsExecutor = new MonitoredThreadPoolExecutor(executorThreadSize, executorThreadSize,
				0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(executorQueueSize),
				new ThreadFactory() {
					private volatile int _count = 0;
					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, executerBaseName + _count++);
					}
				},
				new ThreadPoolExecutor.DiscardPolicy(){
					@Override
					public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
						if (HinemosPropertyCommon.ws_https_request_exchange_reject_logging.getBooleanValue()) {
							log.warn("too many demand. https request discarded : " + rejectAddrName + ":" + r.toString());
						}
					}
				}
		);
		httpsServer.setExecutor(httpsExecutor);
	}
}
