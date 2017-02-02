/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
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

import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.util.StringBinder;
import com.clustercontrol.util.XMLUtil;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

/**
 * JAX-WSによるWEBサービスの初期化(publish)/停止(stop)を制御する基底プラグイン.
 *
 */
public abstract class WebServicePlugin implements HinemosPlugin {

	public static final Log log = LogFactory.getLog(WebServicePlugin.class);

	private static final ThreadPoolExecutor _threadPool;
	private static final ArrayList<Endpoint> endpointList = new ArrayList<Endpoint>();

	/** HTTPS通信時に利用 */
	private static ConcurrentHashMap<String, HttpsServer> httpsServerMap =
			new ConcurrentHashMap<String, HttpsServer>();

	/** Invalidな文字を置換する場合の置換文字のキー */
	private static final String MESSAGE_REPLACE_METHOD_KEY = "common.invalid.char.replace";

	/** Invalidな文字を置換する場合の置換文字のキー */
	private static final String MESSAGE_REPLACE_CHAR_KEY = "common.invalid.char.replace.to";

	static {
		int _threadPoolSize = HinemosPropertyUtil.getHinemosPropertyNum("ws.client.threadpool.size" , Long.valueOf(8)).intValue();
		int _queueSize = HinemosPropertyUtil.getHinemosPropertyNum("ws.queue.size" , Long.valueOf(300)).intValue();

		_threadPool = new MonitoredThreadPoolExecutor(_threadPoolSize, _threadPoolSize, 0L, TimeUnit.MICROSECONDS,
				new LinkedBlockingQueue<Runnable>(_queueSize),
				new ThreadFactory() {
			private volatile int _count = 0;
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "WebServiceWorkerForClient-" + _count++);
			}
		}, new ThreadPoolExecutor.AbortPolicy());


		boolean invalidCharReplace = HinemosPropertyUtil.getHinemosPropertyBool(MESSAGE_REPLACE_METHOD_KEY, false);
		XMLUtil.setReplace(invalidCharReplace);
		StringBinder.setReplace(invalidCharReplace);

		String replaceChar = HinemosPropertyUtil.getHinemosPropertyStr(MESSAGE_REPLACE_CHAR_KEY, "?");
		if(replaceChar != null){
			XMLUtil.setReplaceChar(replaceChar);
			StringBinder.setReplaceChar(replaceChar);
		}
	}

	public static int getQueueSize() {
		return _threadPool.getPoolSize();
	}

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
			HttpsServer httpsServer = null;
			// プロトコルが HTTPSの場合には、まずHttpsServiceオブジェクトを作り、それをendpoit.publishに渡す必要がある。
			// URLとポートがまったく同じHttpsServiceを複数作れないので、Hashmapにて重複管理し、もしもHashMapに
			// HTTPSServerが存在する場合にはそれを使いまわす。
			if ("https".equals(urlPrefix.getProtocol())) {
				httpsServer = httpsServerMap.get(addressPrefix);
				if (httpsServer == null) {
					// HTTPS Serverの作成（HTTPSサーバの開始は、後で一括して行うため、ここではインスタンスの生成のみに留める
					String protocol = HinemosPropertyUtil.getHinemosPropertyStr("ws.https.protocol", "TLS");
					String keystorePath = HinemosPropertyUtil.getHinemosPropertyStr("ws.https.keystore.path", "/root/keystore");
					String keystorePassword = HinemosPropertyUtil.getHinemosPropertyStr("ws.https.keystore.password", "hinemos");
					String keystoreType = HinemosPropertyUtil.getHinemosPropertyStr("ws.https.keystore.type", "PKCS12");
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
					httpsServerMap.put(addressPrefix, httpsServer);
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

	/**
	 * デフォルトのスレッドプールを使用してWebServiceを公開する関数
	 * @param addressPrefix 公開アドレスの 「http://x.x.x.x:xxxx」 の部分
	 * @param addressBody 公開アドレスのうち addressPrefix を除いた部分
	 * @param endpointInstance
	 */
	protected void publish(String addressPrefix, String addressBody, Object endpointInstance) {
		publish(addressPrefix, addressBody, endpointInstance, _threadPool);
	}

	@Override
	public void deactivate() {
		/**
		 * webサービスの停止
		 */
		// 許容時間まで待ちリクエストを処理する
		_threadPool.shutdown();
		try {
			long _shutdownTimeout = HinemosPropertyUtil.getHinemosPropertyNum("ws.client.shutdown.timeout" , Long.valueOf(10000));

			if (! _threadPool.awaitTermination(_shutdownTimeout, TimeUnit.MILLISECONDS)) {
				List<Runnable> remained = _threadPool.shutdownNow();
				if (remained != null) {
					log.info("shutdown timeout. runnable remained. (size = " + remained.size() + ")");
				}
			}
		} catch (InterruptedException e) {
			_threadPool.shutdownNow();
		}

		for (Endpoint endpoint : endpointList) {
			log.info("endpoint stop : " + endpoint.getImplementor().getClass().getSimpleName());
			try {
				/**
				 * JAX-WSの不具合により、0.0.0.0でlistenしているwebサービスは
				 * stop時にNullPointerExceptionが出て、stopできないようだ。
				 * http://java.net/jira/browse/JAX_WS-941
				 *
				 * このため、JBoss終了時にwebサービスのアクセスがあると、
				 * jboss.logに、エラーのスタックトレースが出力される。
				 */
				endpoint.stop();
			} catch (NullPointerException e) {
				log.info("stop endpoint :  " + e.getMessage());
			} catch (Exception e) {
				log.warn("stop endpoint : " + e.getMessage(), e);
			}
		}
	}

	@Override
	public void destroy() {

	}

	protected void startHTTPS() {
		for(final HttpsServer server : httpsServerMap.values()) {
			server.start();
		}
	}
}
