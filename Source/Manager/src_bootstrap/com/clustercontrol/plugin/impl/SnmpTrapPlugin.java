/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
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

	/** snmptrapのデフォルト文字コード */
	public static final Charset _charsetDefault = Charset.forName("UTF-8");

	private static SnmpTrapMonitorService snmpTrapService;

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
		String address = HinemosPropertyCommon.monitor_snmptrap_listen_address.getStringValue();
		int port = HinemosPropertyCommon.monitor_snmptrap_listen_port.getIntegerValue();

		StringBuilder charsetAll = new StringBuilder();
		for (String c : Charset.availableCharsets().keySet()) {
			charsetAll.append(c + ", ");
		}
		logger.info("supported charset : " + charsetAll);

		Charset defaultCharset = _charsetDefault;
		try {
			defaultCharset = Charset.forName(HinemosPropertyCommon.monitor_snmptrap_charset.getStringValue());
		} catch (Exception e) { }

		int queueSize = HinemosPropertyCommon.monitor_snmptrap_filter_queue_size.getIntegerValue();
		int threadSize = HinemosPropertyCommon.monitor_snmptrap_filter_thread_size.getIntegerValue();

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
