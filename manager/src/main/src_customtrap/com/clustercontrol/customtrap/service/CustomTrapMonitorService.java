/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.customtrap.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.customtrap.bean.CustomTrap;
import com.clustercontrol.customtrap.bean.CustomTraps;
import com.clustercontrol.customtrap.util.CustomTrapNotifier;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.snmptrap.service.ReceivedTrapFilterTask;
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
		try {
			serverProcess();
		} catch (Exception e) {
			logger.warn("CustomTrapMonitorService start Error" + e.getMessage());
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
			String protocol = HinemosPropertyUtil.getHinemosPropertyStr("ws.https.protocol", "TLS");
			String keystorePath = HinemosPropertyUtil.getHinemosPropertyStr("ws.https.keystore.path", HinemosPropertyDefault.getString(HinemosPropertyDefault.StringKey.WS_HTTPS_KEYSTORE_PATH));
			String keystorePassword = HinemosPropertyUtil.getHinemosPropertyStr("ws.https.keystore.password",
					"hinemos");
			String keystoreType = HinemosPropertyUtil.getHinemosPropertyStr("ws.https.keystore.type", "PKCS12");
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
					logger.debug("serverProcess() : Received HTTP request. url=" + exchange.getRequestURI());
					
					//　リクエストを処理スレッドプールへ委譲。
					httpPoolExecutor.execute(new Runnable() {
						@Override
						public void run() {
							try {
								logger.debug("serverProcess() : CustomTrap customtrapHttpService handle.");
								
								String msgBody;
								try {
									msgBody = getMsgBody(exchange);
									logger.debug("serverProcess() : msgbody=" + msgBody);
								} catch(IOException e) {
									// リクエストのボディ取得に失敗。
									logger.warn("serverProcess() : fail to get request body. message=" + e.getMessage(), e);
									writeResponse(exchange, HTTP_BAD_REQUEST, HTTP_BAD_REQUEST_MSG);
									return;
								}
								
								try {
									CustomTraps receivedCustomTraps = parseCustomTrap(exchange, msgBody);
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
	 * @return		パース後の受信データ配列
	 * @throws JsonProcessingException
	 * @throws ParseException
	 */
	private CustomTraps parseCustomTrap(HttpExchange exchange, String msgBody) throws JsonProcessingException, ParseException {

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
		parsedCustomTraps.setAgentAddr(exchange.getRemoteAddress().getHostName());
		for (CustomTrap receivedCustomTrap : receivedCustomTraps.getCustomTraps()){
			// Dataチェック
			String strTime = receivedCustomTrap.getDate();
			if ((null != strTime) && !strTime.isEmpty()) {
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date samplingDate = df.parse(receivedCustomTrap.getDate());
				receivedCustomTrap.setSampledTime(samplingDate.getTime());
			} else {
				receivedCustomTrap.setSampledTime(HinemosTime.currentTimeMillis());
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

			logger.debug(receivedCustomTrap.toString());
			if (!receivedCustomTrap.isNumberType() && !receivedCustomTrap.isStringType()) {
				throw new UnsupportedOperationException();
			}
			if (receivedCustomTrap.isNumberType()) {
				Integer.parseInt(receivedCustomTrap.getMsg());
			}
			parsedTrapList.add(receivedCustomTrap);
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
		httpServer.stop(1);
		httpPoolExecutor.shutdownNow();
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
				if (handler != null)
					handler.rejectedExecution(r, executor);

				if (r instanceof ReceivedTrapFilterTask) {
					logger.warn("too many customtrap. customtrap discarded : " + r);
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
}