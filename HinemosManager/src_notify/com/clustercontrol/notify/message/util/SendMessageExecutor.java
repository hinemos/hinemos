/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.message.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.core5.http.HttpStatus;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.apllog.AplLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * メッセージフィルタへのHinemosメッセージ送信を実行するクラス
 */
public class SendMessageExecutor {

	private static Log log = LogFactory.getLog(SendMessageExecutor.class);

	private static ExecutorService service;

	private static String workerName = "SendMessageExecutor";

	static {
		int maxThreadPoolSize = HinemosPropertyCommon.notify_message_webapi_threads.getIntegerValue();

		service = new MonitoredThreadPoolExecutor(maxThreadPoolSize, maxThreadPoolSize, 0L, TimeUnit.MICROSECONDS,
				new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
					private volatile int _count = 0;

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, workerName + "-" + _count++);
					}
				}, new ThreadPoolExecutor.AbortPolicy());
	}

	/**
	 * メッセージ送信を別スレッド実行します。
	 * 
	 * @param notifyId メッセージ通知のnotifyId
	 * @param hinemosMessage 集約されたHinemosMessage
	 */
	public static Future<?> sendMessage(String hinemosMessage) {
		return service.submit(new SendMessageTask(hinemosMessage));
	}

	/**
	 * メッセージ送信を実行するスレッドクラス
	 */
	private static class SendMessageTask extends Thread {

		private static Log taskLog = LogFactory.getLog(SendMessageTask.class);

		private String hinemosMessage;

		public SendMessageTask(String hinemosMessage) {
			this.hinemosMessage = hinemosMessage;
		}

		@Override
		public void run() {
			try {
				taskLog.debug("run() : start : " + this.hinemosMessage);
				sendReqest(this.hinemosMessage);
				taskLog.debug("run() : end :  " + this.hinemosMessage);
			} catch (Exception e) {
				taskLog.error("run() : ", e);
				String notifyIds = String.join(",", readNotifyIds(hinemosMessage));
				String[] args = { notifyIds };
				AplLogger.put(InternalIdCommon.PLT_NTF_SYS_007, args, e.getMessage());
			}
		}
	}

	/**
	 * リクエスト送信
	 */
	private static void sendReqest(String hinemosMessage) throws Exception {

		List<String> bearerTokenList = TokenManager.getTokenList();

		String url = HinemosPropertyCommon.notify_message_webapi_url.getStringValue();

		// URLが不正な場合、通信エラー扱いとする
		if (!url.matches("^https?://.+")) {
			throw new HinemosUnknown("HinemosProperty 'notify.message.webapi.url' is invalid");
		}

		SendMessageHttpClient.SendMessageClientBuilder builder = createRequest();

		boolean result = false;
		String internalMessage = null;
		int retryCount = 0;
		int maxRetry = HinemosPropertyCommon.notify_message_webapi_retry_count.getIntegerValue();
		int retryInterval = HinemosPropertyCommon.notify_message_webapi_retry_interval.getIntegerValue();

		// 試行回数が1未満の場合は、1にする
		if (maxRetry < 1) {
			log.warn("HinemosProperty 'notify.message.webapi.retry.count' is less than 1.");
			maxRetry = 1;
		}

		// 試行回数(maxRetry)分まで、API成功するまで実行する
		while (retryCount < maxRetry && !result) {
			retryCount++;
			if (retryCount > 1) {
				Thread.sleep(retryInterval);
			}

			// 1回の試行の中で、フィルタマネージャの認証に失敗した場合、トークンを切り替えていく
			for (int tokenIndex = 0; tokenIndex < bearerTokenList.size(); tokenIndex++) {
				try (SendMessageHttpClient client = builder.setBearerToken(bearerTokenList.get(tokenIndex)).build()) {

					result = client.execute(url, hinemosMessage);

					// 認証失敗した場合、かつ最終ループでない場合、次のトークンを使う
					if (client.getStatusCode() == HttpStatus.SC_UNAUTHORIZED
							&& bearerTokenList.size() != (tokenIndex + 1)) {
						continue;
					}

					if (!result) {
						StringBuilder detailMsg = new StringBuilder();
						detailMsg.append("HTTP Status: ").append(client.getStatusCode()).append("\n");
						detailMsg.append("Reqeust: ").append(hinemosMessage).append("\n");
						detailMsg.append("Response: ").append(client.getResponseBody()).append("\n");
						internalMessage = detailMsg.toString();
					} else {
						TokenManager.notifySuccessToken(bearerTokenList.get(tokenIndex));
					}
					break;
				}
			}

			// 処理した結果失敗ならINTERNALイベント出力用メッセージを例外に入れてthrow
			if (!result) {
				throw new HinemosUnknown(internalMessage);
			}
		}
	}

	/**
	 * リクエスト作成
	 */
	private static SendMessageHttpClient.SendMessageClientBuilder createRequest() {

		SendMessageHttpClient.SendMessageClientBuilder builder = null;

		builder = SendMessageHttpClient.custom();

		// リクエスト作成
		int connectionTimeout = HinemosPropertyCommon.notify_message_webapi_connection_timeout.getIntegerValue();
		if (connectionTimeout < 0) {
			log.warn("HinemosProperty 'notify.message.webapi.connection.timeout' is negative number.");
			connectionTimeout = 0;
		}

		int readTimeout = HinemosPropertyCommon.notify_message_webapi_read_timeout.getIntegerValue();
		if (readTimeout < 0) {
			log.warn("HinemosProperty 'notify.message.webapi.read.timeout' is negative number.");
			readTimeout = 0;
		}

		builder.setConnectTimeout(connectionTimeout);
		builder.setRequestTimeout(readTimeout);
		builder.setCancelProxyCache(true);
		builder.setKeepAlive(true);
		builder.setNeedAuthSSLCert(true);
		builder.setProxyHost(HinemosPropertyCommon.notify_message_webapi_proxy_host.getStringValue());
		builder.setProxyPort(HinemosPropertyCommon.notify_message_webapi_proxy_port.getIntegerValue());
		builder.setProxyUser(HinemosPropertyCommon.notify_message_webapi_proxy_user.getStringValue());
		builder.setProxyPassword(HinemosPropertyCommon.notify_message_webapi_proxy_password.getStringValue());

		return builder;
	}

	/**
	 * 集約されたHinemosMessage（JSON）からNotifyID一覧を読み取り、返却します。
	 * 
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	private static List<String> readNotifyIds(String hinemosMessage) {
		List<String> result = new ArrayList<>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(hinemosMessage);
			for (JsonNode messageNode : rootNode.get("messages")) {
				result.add(messageNode.get("notifyId").asText());
			}
		} catch (JsonProcessingException e) {
			// HinemosMessageManager経由の場合、ここは通らない想定
			log.warn("readNotifyIds() : HinemosMessage is invalid");
		}
		return result;
	}
}
