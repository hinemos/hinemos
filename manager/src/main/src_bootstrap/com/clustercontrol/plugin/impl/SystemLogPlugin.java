/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.impl;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.HinemosManagerMain;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.systemlog.bean.SyslogMessage;
import com.clustercontrol.systemlog.service.SyslogService;
import com.clustercontrol.systemlog.service.SystemLogMonitor;

/**
 * システムログ監視の初期化・終了処理(tcp:514, udp:514の待ち受け開始)を制御するプラグイン.
 *
 */
public class SystemLogPlugin implements HinemosPlugin {

	public static final Log log = LogFactory.getLog(SystemLogPlugin.class);
	
	private static final Object shutdownLock = new Object();
	
	private static boolean shutdown = false;
	
	/** syslog監視のフィルタリングクラス */
	private static SystemLogMonitor _handler;

	private static SyslogThread syslogThread;
	
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

	}

	@Override
	public void deactivate() {

	}

	/**
	 * byte配列からSyslogMessageへ変換する
	 * 
	 * @see com.clustercontrol.ws.HinemosHAEndpoint#syslog()
	 */
	public static SyslogMessage byteToSyslog(byte[] syslogRaw, String senderAddress) throws ParseException, HinemosUnknown {
		return _handler.byteToSyslog(syslogRaw,senderAddress);
	}

	@Override
	public void destroy() {

	}
	
	private void createService() {
		syslogThread = new SyslogThread();
		syslogThread.start();
	}
	
	private static class SyslogThread extends Thread{
		
		public void run(){
			log.info("start Syslog.");
		
			try {
				
				/** 受信処理とフィルタリング処理の間に存在するsyslog処理待ちキューの最大サイズ*/
				int _taskQueueSize = HinemosPropertyCommon.monitor_systemlog_filter_queue_size.getIntegerValue(); // 15[min] * 30[msg/sec] (about 27mbyte)
				/** フィルタリング処理のスレッド数 */
				int _taskThreadSize = HinemosPropertyCommon.monitor_systemlog_filter_thread_size.getIntegerValue();

				_handler = new SystemLogMonitor(_taskThreadSize, _taskQueueSize);
				
				_handler.start();
				
				final SyslogService syslog = new SyslogService();
				if(!HinemosManagerMain._isClustered){
					syslog.start(_handler);
				}
				
				final CountDownLatch sync = new CountDownLatch(1);
				
				Runtime.getRuntime().addShutdownHook(
						new Thread() {
							@Override
							public void run() {
								log.info("call shutdown-hook.");
								
								synchronized(shutdownLock) {
									shutdown = true;
									shutdownLock.notify();
									
									syslog.stop();
								}
								
								try {
									sync.await();
								} catch (InterruptedException e) {
								}
							}
						});
				
				synchronized(shutdownLock) {
					while (!shutdown) {
						try {
							shutdownLock.wait();
						} catch (InterruptedException e) {
							log.warn("shutdown lock interrupted.", e);
							try {
								Thread.sleep(1000);
							} catch (InterruptedException sleepE) { };
						}
					}
				}
				
				syslog.join();
				
				log.info("stopped Syslog.");
				
				sync.countDown();
				
			} catch(RuntimeException | IOException e) {
				log.warn(e.getMessage(), e);
			}
		}
		
	}
}
