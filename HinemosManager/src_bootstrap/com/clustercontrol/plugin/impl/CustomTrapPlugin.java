/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.customtrap.bean.CustomTraps;
import com.clustercontrol.customtrap.service.CustomTrapMonitorService;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * CustomTrap監視の初期化・終了処理を制御するプラグイン.
 *
 */
public class CustomTrapPlugin implements HinemosPlugin {
	private static final Logger logger = Logger.getLogger(CustomTrapPlugin.class);

	/** CustomTrapのデフォルト文字コード */
	public static final Charset _charsetDefault = Charset.forName("UTF-8");

	private static CustomTrapMonitorService customtrapService;

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(AsyncWorkerPlugin.class.getName());
		return dependency;
	}

	@Override
	public Set<String> getRequiredKeys() {
		return null;
	}

	@Override
	public void create() {
		createService();
	}

	@Override
	public void activate() {
		customtrapService.start();
	}

	@Override
	public void deactivate() {
		customtrapService.shutdown();
	}

	@Override
	public void destroy() {

	}

	/**
	 * HAからのデータ受信時処理
	 * @param custonTraps 受信したカスタムトラップ
	 */
	public static void customTrapReceivedSync(CustomTraps receivedCustomTraps) {
		customtrapService.customtrapReceivedSync(receivedCustomTraps);
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
	public static CustomTraps parseCustomTrap(String senderAddress, String message, Long recvTimestamp) throws JsonProcessingException, ParseException {
		return customtrapService.parseCustomTrap(senderAddress, message, recvTimestamp);
	}

	private static void createService() {

		// URL取得
		String customTrapUrlString = HinemosPropertyCommon.monitor_customtrap_url.getStringValue();
		URL customTrapUrl = null;
		try {
			customTrapUrl = new URL(customTrapUrlString);
		} catch (MalformedURLException e1) {
			logger.warn("Invalid monitor.customtrap.url=" + customTrapUrl);
			return;
		}

		Charset defaultCharset = _charsetDefault;
		try {
			defaultCharset = Charset.forName(HinemosPropertyCommon.monitor_customtrap_charset.getStringValue());
		} catch (Exception e) {
		}

		int httpServerBacklog = HinemosPropertyCommon.monitor_customtrap_http_backlog_size.getIntegerValue();
		int httpPoolSize = HinemosPropertyCommon.monitor_customtrap_http_pool_size.getIntegerValue();
		int httpQueueSize = HinemosPropertyCommon.monitor_customtrap_http_queue_size.getIntegerValue();

		logger.info(String.format(
				"starting CustomTrapPlugin :url = %s, charset = %s, httpServerBacklog = %d, httpPoolSize = %d, httpQueueSize = %d",
				customTrapUrl.toString(), defaultCharset.name(), httpServerBacklog, httpPoolSize, httpQueueSize));

		// HttpServerに設定するスレッドのプール
		ThreadPoolExecutor httpPoolExecutor = new MonitoredThreadPoolExecutor(httpPoolSize, httpPoolSize, 0L,
				TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(httpQueueSize), new ThreadFactory() {
					private volatile int _count = 0;

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "CustomtrapHttpService-" + _count++);
					}
				});
		customtrapService = new CustomTrapMonitorService();
		customtrapService.setHttpExecutor(httpPoolExecutor).setDefaultCharset(defaultCharset).setUrl(customTrapUrl).setBacklog(httpServerBacklog);
	}
}
