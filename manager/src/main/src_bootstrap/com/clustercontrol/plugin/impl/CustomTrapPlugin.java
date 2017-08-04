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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.customtrap.service.CustomTrapMonitorService;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.plugin.api.HinemosPlugin;

/**
 * CustomTrap監視の初期化・終了処理を制御するプラグイン.
 *
 */
public class CustomTrapPlugin implements HinemosPlugin {
	private static final Logger logger = Logger.getLogger(CustomTrapPlugin.class);

	/** CustomTrap URL */
	public static final String _keyCustomTrapUrl = "monitor.customtrap.url";
	public static final String _customtrapUrlDefault = "http://0.0.0.0:8082/";

	/** CustomTrapのデフォルト文字コード */
	public static final String _keyCharset = "monitor.customtrap.charset";
	public static final Charset _charsetDefault = Charset.forName("UTF-8");

	/** HttpServer受信Backlog */
	private static final String _keyHttpServerBacklogSize = "monitor.customtrap.http.backlog.size";
	private static final int _httpServerBacklogSizeDefault = 0;

	/** HttpServerスレッド数 */
	private static final String _keyHttpPoolSize = "monitor.customtrap.http.pool.size";
	private static final int _httpPoolSizeDefault = 1;

	private static CustomTrapMonitorService customtrapService;

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(AsyncWorkerPlugin.class.getName());
		return dependency;
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

	private static void createService() {

		// URL取得
		String customTrapUrlString = HinemosPropertyUtil.getHinemosPropertyStr(_keyCustomTrapUrl,
				_customtrapUrlDefault);
		URL customTrapUrl = null;
		try {
			customTrapUrl = new URL(customTrapUrlString);
		} catch (MalformedURLException e1) {
			logger.warn("Invalid monitor.customtrap.url=" + customTrapUrl);
			return;
		}

		Charset defaultCharset = _charsetDefault;
		try {
			defaultCharset = Charset.forName(HinemosPropertyUtil.getHinemosPropertyStr(_keyCharset, "UTF-8"));
		} catch (Exception e) {
		}

		int httpServerBacklog = HinemosPropertyUtil.getHinemosPropertyNum(_keyHttpServerBacklogSize,
				Long.valueOf(_httpServerBacklogSizeDefault)).intValue();
		int httpPoolSize = HinemosPropertyUtil.getHinemosPropertyNum(_keyHttpPoolSize, Long.valueOf(_httpPoolSizeDefault)).intValue();

		logger.info(String.format(
				"starting CustomTrapPlugin :url = %s, charset = %s, httpServerBacklog = %d, httpPoolSize = %d",
				customTrapUrl.toString(), defaultCharset.name(), httpServerBacklog, httpPoolSize));

		// HttpServerに設定するスレッドのプール
		ThreadPoolExecutor httpPoolExecutor = new MonitoredThreadPoolExecutor(httpPoolSize, httpPoolSize, 0L,
				TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(1), new ThreadFactory() {
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
