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
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.systemlog.bean.SyslogMessage;
import com.clustercontrol.systemlog.service.SystemLogMonitor;
import com.clustercontrol.systemlog.util.SyslogReceiver;

/**
 * システムログ監視の初期化・終了処理(udp:24514の待ち受け開始)を制御するプラグイン.
 *
 */
public class SystemLogPlugin implements HinemosPlugin {

	public static final Log log = LogFactory.getLog(SystemLogPlugin.class);

	/** syslog監視の受信クラス */
	private static SyslogReceiver _receiver;

	/** syslog監視のフィルタリングクラス */
	private static SystemLogMonitor _handler;

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
		try {
			_receiver.start();
		} catch (Exception e) {
			log.warn("SystemLogPlugin activation failure.", e);
		}
	}

	@Override
	public void deactivate() {
		if (_receiver != null) {
			_receiver.shutdown();
		}
	}

	@Override
	public void destroy() {

	}
	
	public static SyslogMessage byteToSyslog(byte[] syslogRaw) throws ParseException, HinemosUnknown {
		return _receiver.byteToSyslog(syslogRaw);
	}
	
	public static void syslogReceivedSync(List<SyslogMessage> syslogList) {
		_handler.syslogReceivedSync(syslogList);
	}

	public static long getReceivedCount() {
		return _handler.getReceivedCount();
	}

	public static long getNotifiedCount() {
		return _handler.getNotifiedCount();
	}

	public static long getDiscardedCount() {
		return _handler.getDiscardedCount();
	}

	public static int getQueuedCount() {
		return _handler.getQueuedCount();
	}
	
	private static void createService() {
		/** syslogの待ち受けアドレス */
		String _listenAddress = HinemosPropertyUtil.getHinemosPropertyStr(
				"monitor.systemlog.listen.address", "0.0.0.0");

		/** syslogの待ち受けポート番号 */
		int _listenPort = HinemosPropertyUtil.getHinemosPropertyNum("monitor.systemlog.listen.port", Long.valueOf(24514)).intValue();

		StringBuilder charsetAll = new StringBuilder();
		for (String c : Charset.availableCharsets().keySet()) {
			charsetAll.append(c + ", ");
		}
		log.info("supported charset : " + charsetAll);

		Charset charset = Charset.forName("UTF-8");
		String charsetStr = HinemosPropertyUtil.getHinemosPropertyStr("monitor.systemlog.charset", "UTF-8");
		try {
			charset = Charset.forName(charsetStr);
		} catch (Exception e) { }
		/** syslogのデフォルト文字コード */
		Charset _charset = charset;

		/** 受信処理とフィルタリング処理の間に存在するsyslog処理待ちキューの最大サイズ*/
		int _taskQueueSize = HinemosPropertyUtil.getHinemosPropertyNum(
				"monitor.systemlog.filter.queue.size", Long.valueOf(15 * 60 * 30)).intValue(); // 15[min] * 30[msg/sec] (about 27mbyte)
		/** フィルタリング処理のスレッド数 */
		int _taskThreadSize = HinemosPropertyUtil.getHinemosPropertyNum(
				"monitor.systemlog.filter.thread.size", Long.valueOf(1)).intValue();

		log.info(String.format("starting SystemLogPlugin : listenAddress = %s, listenPort = %d, charset = %s, queueSize = %d, threads = %d",
				_listenAddress, _listenPort, _charset.name(), _taskQueueSize, _taskThreadSize));

		_handler = new SystemLogMonitor(_taskThreadSize, _taskQueueSize);
		_receiver = new SyslogReceiver(_listenAddress, _listenPort, _charset, _handler);
	}
}
