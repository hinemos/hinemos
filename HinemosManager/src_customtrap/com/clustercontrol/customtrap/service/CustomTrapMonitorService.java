/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.customtrap.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;

import com.clustercontrol.HinemosManagerMain;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.customtrap.bean.CustomTrap;
import com.clustercontrol.customtrap.bean.CustomTraps;
import com.clustercontrol.customtrap.util.CustomTrapNotifier;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.util.HinemosTime;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

/**
 * カスタムトラップ監視のメインクラス。 HttpServerを立ち上げ、受信したトラップをタスクとして並行処理する。
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class CustomTrapMonitorService {
	private Logger logger = Logger.getLogger(this.getClass());

	private CustomTrapNotifier notifier = new CustomTrapNotifier();
	private Charset defaultCharset;
	private URL customTrapUrl;
	private int backlog;

	private HttpServer httpServer;
	private ThreadPoolExecutor httpPoolExecutor;
	private ThreadPoolExecutor mainPoolExecutor;

	private static int HTTP_OK = 200;
	private static int HTTP_BAD_REQUEST = 400;
	private static String HTTP_BAD_REQUEST_MSG = "400 Bad Request";

	
	/**
	 * CustomTrapMonitorService起動します。
	 */
	public void start() {
		if (! HinemosManagerMain._isClustered) {
			try {
				serverProcess();
			} catch (Exception e) {
				logger.warn("CustomTrapMonitorService start Error ", e);
			}
		}
	}

	/**
	 * サーバプロセス起動します。
	 * 
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 */
	private void serverProcess() throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException,
			UnrecoverableKeyException, KeyManagementException {
		logger.info("serverProcess() : CustomTrap serverProcess start Protocol =" + customTrapUrl.getProtocol() + " Host = "
				+ customTrapUrl.getHost() + " port = " + customTrapUrl.getPort() + " path = "
				+ customTrapUrl.getPath());
		
		if (customTrapUrl.getProtocol().equals("http")) {
			httpServer = HttpServer.create(new InetSocketAddress(customTrapUrl.getHost(), customTrapUrl.getPort()),
					this.backlog);
		} else if (customTrapUrl.getProtocol().equals("https")) {
			String protocol = HinemosPropertyCommon.ws_https_protocol.getStringValue();
			String keystorePath = HinemosPropertyDefault.ws_https_keystore_path.getStringValue();
			String keystorePassword = HinemosPropertyCommon.ws_https_keystore_password.getStringValue();
			String keystoreType = HinemosPropertyCommon.ws_https_keystore_type.getStringValue();
			logger.info("CustomTrap protocol: " + protocol + ", keystorePath: " + keystorePath +  ", KeyStore: " + keystoreType);
			SSLContext ssl = SSLContext.getInstance(protocol);
			KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			KeyStore store = KeyStore.getInstance(keystoreType);
			try (InputStream in = new FileInputStream(keystorePath)) {
				store.load(in, keystorePassword.toCharArray());
			}
			keyFactory.init(store, keystorePassword.toCharArray());
			TrustManagerFactory trustFactory = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustFactory.init(store);
			ssl.init(keyFactory.getKeyManagers(), trustFactory.getTrustManagers(), new SecureRandom());
			HttpsConfigurator configurator = new HttpsConfigurator(ssl);
			httpServer = HttpsServer.create(new InetSocketAddress(customTrapUrl.getHost(), customTrapUrl.getPort()), 0);
			((HttpsServer) httpServer).setHttpsConfigurator(configurator);
		} else {
			logger.warn("CustomTrapMonitorService serverProcess protocol error = " + customTrapUrl.getProtocol());
			return;
		}
		
		// jax-ws の実装を元にHttpServerへ全てのリクエストに対応するためのスレッドプールを設定。
		mainPoolExecutor = new MonitoredThreadPoolExecutor(0, Integer.MAX_VALUE, 60L,
			TimeUnit.MICROSECONDS, new SynchronousQueue<Runnable>(), new ThreadFactory() {
				private volatile int _count = 0;
				
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r, "CustomtrapHttpService-" + _count++);
				}
			});
		httpServer.setExecutor(mainPoolExecutor);
		
		// リクエストの一時受けハンドラ。
		HttpHandler handler = new HttpHandler() {
			public void handle(final HttpExchange exchange) throws IOException {
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("serverProcess() : Received HTTP request. url=" + exchange.getRequestURI());
					}
					
					//　リクエストを処理スレッドプールへ委譲。
					httpPoolExecutor.execute(new Runnable() {
						@Override
						public void run() {
							try {
								if (logger.isDebugEnabled()) {
									logger.debug("serverProcess() : CustomTrap customtrapHttpService handle.");
								}
								
								String msgBody;
								try {
									msgBody = getMsgBody(exchange);
									if (logger.isDebugEnabled()) {
										logger.debug("serverProcess() : msgbody=" + msgBody);
									}
								} catch(IOException e) {
									// リクエストのボディ取得に失敗。
									logger.warn("serverProcess() : fail to get request body. message=" + e.getMessage(), e);
									writeResponse(exchange, HTTP_BAD_REQUEST, HTTP_BAD_REQUEST_MSG);
									return;
								}
								
								try {
									CustomTraps receivedCustomTraps = parseCustomTrap(exchange.getRemoteAddress().getHostName(), msgBody, null);
									ReceivedCustomTrapFilter receivedCustomTrapFilter = new ReceivedCustomTrapFilter(receivedCustomTraps, notifier, defaultCharset);
									receivedCustomTrapFilter.work();
								} catch(JsonProcessingException | ParseException e) {
									// リクエストのボディ解析に失敗。
									logger.warn("serverProcess() : fail to parse request body. message=" + e.getMessage(), e);
									writeResponse(exchange, HTTP_BAD_REQUEST, HTTP_BAD_REQUEST_MSG);
									return;
								}
								
								writeResponse(exchange, HTTP_OK);
							} catch(Exception e) {
								logger.warn(e.getMessage(), e);
							} finally {
								exchange.close();
							}
						}
					});
				} catch(Throwable e) {
					logger.warn(e.getMessage(), e);
					exchange.close();
				}
			}
		};
		
		httpServer.createContext(customTrapUrl.getPath(), handler);
		httpServer.start();
	}
	
	/**
	 * 受信データをパースします。
	 * 
	 * @param exchange	HttpExchange
	 * @param msgBody	受信データ
	 * @param recvTimestamp	受信時刻（主にMCでの受信時刻を想定）
	 * @return		パース後の受信データ配列
	 * @throws JsonProcessingException
	 * @throws ParseException
	 */
	public CustomTraps parseCustomTrap(String agentAddr, String msgBody, Long recvTimestamp) throws JsonProcessingException, ParseException {

		ObjectMapper mapper = new ObjectMapper();
		// Jsonパース
		// 受信したJSON情報
		CustomTraps receivedCustomTraps = null;
		// パースしたJSON情報(全体)
		CustomTraps parsedCustomTraps = new CustomTraps();
		// パースしたJSON情報(個別)のリスト
		List<CustomTrap> parsedTrapList = new ArrayList<>();
		try {
			receivedCustomTraps = mapper.readValue(msgBody, CustomTraps.class);
		} catch (IOException e) {
			throw new UnsupportedOperationException();
		}
		
		parsedCustomTraps.setFacilityId(receivedCustomTraps.getFacilityId());
		parsedCustomTraps.setAgentAddr(agentAddr);
		if (receivedCustomTraps.getCustomTraps() == null) {
			throw new UnsupportedOperationException();
		}
		for (CustomTrap receivedCustomTrap : receivedCustomTraps.getCustomTraps()){
			try {
				// Dataチェック
				String strTime = receivedCustomTrap.getDate();
				if ((null != strTime) && !strTime.isEmpty()) {
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date samplingDate = df.parse(receivedCustomTrap.getDate());
					receivedCustomTrap.setSampledTime(samplingDate.getTime());
				} else {
					if (recvTimestamp != null) {
						receivedCustomTrap.setSampledTime(recvTimestamp);
					} else {
						receivedCustomTrap.setSampledTime(HinemosTime.currentTimeMillis());
					}
				}
				// Keyチェック
				String key = receivedCustomTrap.getKey();
				if ((null == key) || key.isEmpty()){
					logger.warn("parseCustomTrap() : CustomTrap KEY==null");
					throw new UnsupportedOperationException();
				}
				// Msgチェック
				String msg = receivedCustomTrap.getMsg();
				if ((null == msg) || msg.isEmpty()){
					logger.warn("parseCustomTrap() : CustomTrap MSG==null");
					throw new UnsupportedOperationException();
				}
				
				receivedCustomTrap.setReceivedTime(HinemosTime.currentTimeMillis());
				receivedCustomTrap.setOrgMsg(mapper.writeValueAsString(receivedCustomTrap));

				if (logger.isDebugEnabled()) {
					logger.debug(receivedCustomTrap.toString());
				}

				if (!receivedCustomTrap.isNumberType() && !receivedCustomTrap.isStringType()) {
					logger.warn("parseCustomTrap() : CustomTrap Type==unknown");
					throw new UnsupportedOperationException();
				}
	
				if (receivedCustomTrap.isNumberType()) {
					try {
						new BigDecimal(receivedCustomTrap.getMsg());
					} catch (NumberFormatException e) {
						logger.warn("parseCustomTrap() : CustomTrap Numeric==Invalid");
						throw new UnsupportedOperationException();
					}
				}
				parsedTrapList.add(receivedCustomTrap);
			} catch (UnsupportedOperationException e) {
				// 処理なし
			}
		}
		parsedCustomTraps.setCustomTraps(parsedTrapList);
		return parsedCustomTraps;
	}

	/**
	 * 受信データを取得します。
	 * 
	 * @param ex	HttpExchange
	 * @return		受信データ
	 * @throws IOException
	 */
	private String getMsgBody(HttpExchange ex) throws IOException {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(ex.getRequestBody(), this.defaultCharset))) {
			String line = in.readLine();
			StringBuilder body = new StringBuilder();

			while (line != null && !line.isEmpty()) {
				body.append(line);
				line = in.readLine();
			}
			return body.toString();
		}
	}

	/**
	 * 送信元にレスポンスを返却します。
	 * 
	 * @param exc			HttpExchange
	 * @param responseCode	レスポンスコード
	 * @param responseMsg	レスポンスメッセージ
	 * @throws IOException
	 */
	private void writeResponse(HttpExchange exc, int responseCode, String responseMsg) throws IOException {
		exc.sendResponseHeaders(responseCode, responseMsg.length());
		OutputStream os = exc.getResponseBody();
		os.write(responseMsg.getBytes());
		os.close();
	}

	/**
	 * 送信元にレスポンスを返却します。
	 * @param exc			HttpExchange
	 * @param responseCode	レスポンスコード
	 * @throws IOException
	 */
	private void writeResponse(HttpExchange exc, int responseCode) throws IOException {
		writeResponse(exc, responseCode, "");
	}

	/**
	 * シャットダウンします。
	 */
	public void shutdown() {
		if (! HinemosManagerMain._isClustered) {
			httpServer.stop(1);
			httpPoolExecutor.shutdownNow();
		}
	}

	/**
	 * HttpServer用のThreadPoolExecutorをセットします。
	 * 
	 * @param executor	HttpServer用のThreadPoolExecutor
	 * @return
	 */
	public CustomTrapMonitorService setHttpExecutor(ThreadPoolExecutor executor) {
		this.httpPoolExecutor = executor;
		final RejectedExecutionHandler handler = executor.getRejectedExecutionHandler();
		executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				if (handler != null) {
					logger.warn("too many customtrap. customtrap discarded : " + r);
					handler.rejectedExecution(r, executor);
				}
			}
		});
		return this;
	}

	/**
	 * キャラクタセットをセットします。
	 * 
	 * @param defaultCharset	キャラクタセット
	 * @return
	 */
	public CustomTrapMonitorService setDefaultCharset(Charset defaultCharset) {
		this.defaultCharset = defaultCharset;
		return this;
	}

	/**
	 * URLをセットします。
	 * 
	 * @param customTrapUrl	URL
	 * @return
	 */
	public CustomTrapMonitorService setUrl(URL customTrapUrl) {
		this.customTrapUrl = customTrapUrl;
		return this;
	}

	/**
	 * Backlogをセットします。
	 * 
	 * @param backlog　Backlog
	 * @return
	 */
	public CustomTrapMonitorService setBacklog(int backlog) {
		this.backlog = backlog;
		return this;
	}
	
	/**
	 * HAからのデータ受信時処理
	 * @param custonTraps 受信したカスタムトラップ
	 */
	public void customtrapReceivedSync(CustomTraps custonTraps) {
		ReceivedCustomTrapFilter receivedCustomTrapFilter = new ReceivedCustomTrapFilter(custonTraps, notifier, defaultCharset);
		receivedCustomTrapFilter.work();
	}
}