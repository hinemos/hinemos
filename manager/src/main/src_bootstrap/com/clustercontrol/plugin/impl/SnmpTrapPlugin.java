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

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.snmptrap.bean.SnmpTrap;
import com.clustercontrol.snmptrap.service.Snmp4JSession;
import com.clustercontrol.snmptrap.service.SnmpTrapMonitorService;
import com.clustercontrol.snmptrap.service.SnmpTrapSession;

/**
 * SNMPTRAP監視の初期化・終了処理(udp:162の待ち受け開始)を制御するプラグイン.
 *
 */
public class SnmpTrapPlugin implements HinemosPlugin {
	private static final Logger logger = Logger.getLogger(SnmpTrapPlugin.class);

	/** snmptrapの待ち受けアドレス */
	public static final String _keyListenAddress = "monitor.snmptrap.listen.address";
	public static final String _listenAddressDefault = "0.0.0.0";

	/** snmptrapの待ち受けポート番号 */
	public static final String _keyListenPort = "monitor.snmptrap.listen.port";
	public static final int _listenPortDefault = 162;

	/** snmptrapのデフォルト文字コード */
	public static final String _keyCharset = "monitor.snmptrap.charset";
	public static final Charset _charsetDefault = Charset.forName("UTF-8");

	/** 受信処理とフィルタリング処理の間に存在するsnmptrap処理待ちキューの最大サイズ*/
	public static final String _keyTaskQueueSize = "monitor.snmptrap.filter.queue.size";
	public static final int _taskQueueSizeDefault = 15 * 60 * 30;	// 15[min] * 30[msg/sec]

	/** フィルタリング処理のスレッド数 */
	public static final String _keyTaskThreadSize = "monitor.snmptrap.filter.thread.size";
	public static final int _taskThreadSizeDefault = 1;

	private static SnmpTrapMonitorService snmpTrapService;

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
		logger.info("activating SnmpTrapPlugin without receiver.");
		snmpTrapService.start();
	}

	@Override
	public void deactivate() {
		snmpTrapService.shutdown();
	}

	@Override
	public void destroy() {

	}

	public static long getReceivedCount() {
		return snmpTrapService.getReceivedCount();
	}

	public static long getNotifiedCount() {
		return snmpTrapService.getNotifiedCount();
	}

	public static long getDiscardedCount() {
		return snmpTrapService.getDiscardedCount();
	}

	public static int getQueuedCount() {
		return snmpTrapService.getQueuedCount();
	}
	
	public static void snmptrapReceivedSync(List<SnmpTrap> receivedTrapList) {
		snmpTrapService.snmptrapReceivedSync(receivedTrapList);
	}

	private static void createService() {
		String address = HinemosPropertyUtil.getHinemosPropertyStr(_keyListenAddress, _listenAddressDefault);
		int port = HinemosPropertyUtil.getHinemosPropertyNum(_keyListenPort, Long.valueOf(_listenPortDefault)).intValue();

		StringBuilder charsetAll = new StringBuilder();
		for (String c : Charset.availableCharsets().keySet()) {
			charsetAll.append(c + ", ");
		}
		logger.info("supported charset : " + charsetAll);

		Charset defaultCharset = _charsetDefault;
		try {
			defaultCharset = Charset.forName(HinemosPropertyUtil.getHinemosPropertyStr(_keyCharset, "UTF-8"));
		} catch (Exception e) { }

		int queueSize = HinemosPropertyUtil.getHinemosPropertyNum(_keyTaskQueueSize, Long.valueOf(_taskQueueSizeDefault)).intValue();
		int threadSize = HinemosPropertyUtil.getHinemosPropertyNum(_keyTaskThreadSize, Long.valueOf(_taskThreadSizeDefault)).intValue();

		logger.info(String.format("starting SnmpTrapPlugin : listenAddress = %s, listenPort = %d, charset = %s, queueSize = %d, threads = %d",
				address, port, defaultCharset.name(), queueSize, threadSize));

		SnmpTrapSession session = new Snmp4JSession();
		session.setListenAddress(address, port);

		ThreadPoolExecutor executor = new MonitoredThreadPoolExecutor(
				threadSize,
				threadSize,
				0L,
				TimeUnit.MICROSECONDS,
				new LinkedBlockingQueue<Runnable>(queueSize),
				new ThreadFactory() {
					private volatile int _count = 0;
					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "snmpTrapService-" + _count++);
					}
				});

		SnmpTrapMonitorService service = new SnmpTrapMonitorService();
		service.setSession(session).setExecutor(executor).setDefaultCharset(defaultCharset);

		snmpTrapService = service;
	}
}
