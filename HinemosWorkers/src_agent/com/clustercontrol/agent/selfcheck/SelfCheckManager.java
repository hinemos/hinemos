/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.selfcheck;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtOutputBasicInfoRequest;

import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.SendQueue.MessageSendableObject;
import com.clustercontrol.agent.selfcheck.monitor.JVMHeapMonitor;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * セルフチェック機能の定期実行制御クラス
 */
public class SelfCheckManager implements Runnable {

	private static Log log = LogFactory.getLog(SelfCheckManager.class);

	private final static SelfCheckManager instance = new SelfCheckManager();
	private final static ScheduledExecutorService scheduler;
	public static volatile Date lastMonitorDate = null;
	private static SendQueue sendQueue;

	static {
		scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "SelfCheck");
			}
		});
	}

	/**
	 * セルフチェック機能を活性化させるメソッド
	 */
	public static void start() {
		log.info("SelfCheckManager start()");
		
		SelfCheckConfig.init();
		scheduler.scheduleWithFixedDelay(instance, 90L, SelfCheckConfig.getSelfcheckInterval(), TimeUnit.MILLISECONDS);
	}

	/**
	 * セルフチェック機能を非活性化させるメソッド
	 */
	public static void shutdown() {
		log.info("SelfCheckManager shutdown()");

		scheduler.shutdown();
		long shutdownTimeoutMsec = SelfCheckConfig.getSelfcheckShutdownTimeout();

		try {
			if (!scheduler.awaitTermination(shutdownTimeoutMsec, TimeUnit.MILLISECONDS)) {
				List<Runnable> remained = scheduler.shutdownNow();
				if (remained != null) {
					log.info("shutdown timeout. runnable remained. (size = " + remained.size() + ")");
				}
			}
		} catch (InterruptedException e) {
			scheduler.shutdownNow();
		}
	}

	/**
	 * 定期実行間隔(interval)に基づいて、定期的に実行されるメソッド
	 */
	@Override
	public void run() {
		log.debug("SelfCheckScheduler run() lastMonitorDate=" + lastMonitorDate);
		
		// Java VM Heap
		new JVMHeapMonitor().execute();

		// set timestamp of last monitoring
		refreshMonitorDate();
	}

	private static void refreshMonitorDate() {
		lastMonitorDate = HinemosTime.getDateInstance();
	}

	public static void setSendQueue(SendQueue queue) {
		sendQueue = queue;
	}

	/**
	 * Hinemosマネージャへ情報を通知します。<BR>
	 */
	public static void sendMessage(String msg, String msgOrg) {
		MessageSendableObject sendme = new MessageSendableObject();
		sendme.body = new AgtOutputBasicInfoRequest();
		sendme.body.setPluginId(HinemosModuleConstant.SYSYTEM_SELFCHECK);
		sendme.body.setPriority(PriorityConstant.TYPE_WARNING);
		sendme.body.setApplication(MessageConstant.AGENT.getMessage());
		sendme.body.setMessage(msg);
		sendme.body.setMessageOrg(msgOrg);
		sendme.body.setGenerationDate(HinemosTime.getDateInstance().getTime());
		sendme.body.setMonitorId(HinemosModuleConstant.SYSYTEM);
		sendme.body.setFacilityId(""); // マネージャがセットする。
		sendme.body.setScopeText(""); // マネージャがセットする。

		sendQueue.put(sendme);
	}
}
