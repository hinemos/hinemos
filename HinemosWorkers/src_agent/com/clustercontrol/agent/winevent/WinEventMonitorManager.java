/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.winevent;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtOutputBasicInfoRequest;
import org.openapitools.client.model.AgtRunInstructionInfoRequest;

import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.SendQueue.MessageSendableObject;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

public class WinEventMonitorManager {

	private static Log log = LogFactory.getLog(WinEventMonitorManager.class);
	
	// イベントログ監視間隔
	private static final String RUN_INTERVAL_KEY = "monitor.winevent.filter.interval";
	private static long runInterval = 10000;
	
	// Queue送信
	private static SendQueue sendQueue;
	
	// 監視項目IDとWinEventMonitorを保持するマップ
	// 監視ジョブ以外
	//<monitorid, WinEventMonitor>
	// 監視ジョブ
	//<jobunitId + jobId + facilityId + monitorid, WinEventMonitor>
	private static ConcurrentHashMap<String, WinEventMonitor> winEventMonitorMap = new ConcurrentHashMap<String, WinEventMonitor>();
	
	static {
		String runIntervalStr = AgentProperties.getProperty(RUN_INTERVAL_KEY, Long.toString(runInterval));
		try {
			runInterval = Long.parseLong(runIntervalStr);
		} catch (NumberFormatException e){
			log.info("collecor.winevent.interval uses " + runInterval + ". (" + runIntervalStr + " is invalid)");
		}
		log.info(RUN_INTERVAL_KEY + "=" + runInterval);
	}
		
	public static void setSendQueue(SendQueue sendQueue) {
		WinEventMonitorManager.sendQueue = sendQueue;
	}
	
	public static ConcurrentHashMap<String, WinEventMonitor> getWinEventMonitorMap() {
		return winEventMonitorMap;
	}

	public static void setWinEventMonitorMap(ConcurrentHashMap<String, WinEventMonitor> winEventMonitorMap) {
		WinEventMonitorManager.winEventMonitorMap = winEventMonitorMap;
	}
	
	public static void start() {
		WinEventThread thread = new WinEventThread();
		thread.setName("WinEventMonitor");
		thread.start();
	}
	
	private static class WinEventThread extends Thread {
		@Override
		public void run() {
			log.info("run WinEventThread");
			while (true) {
				try {
					for (WinEventMonitor winEventMonitor : winEventMonitorMap.values()) {
						winEventMonitor.run();
					}
				} catch (Exception e) {
					log.warn("WinEventThread : " + e.getClass().getCanonicalName() + ", " +
							e.getMessage(), e);
				} catch (UnsatisfiedLinkError | NoClassDefFoundError e){
					log.error("WinEventThread : WinEventThread is terminated. " + e.getClass().getCanonicalName() + ", " +
							e.getMessage(), e);
					sendMessage(PriorityConstant.TYPE_CRITICAL,
							MessageConstant.MESSAGE_WINEVENT_STOP_MONITOR_FAILED_TO_GET_WINDOWS_EVENTLOG.getMessage(),
							"Failed to exec winEventMonitor.run(). WinEventThread is terminated. " + e.getClass().getCanonicalName() + ", " + e.getMessage(), HinemosModuleConstant.SYSYTEM, null);
					break;
				} catch (Throwable e) {
					log.error("WinEventThread : " + e.getClass().getCanonicalName() + ", " +
							e.getMessage(), e);
				}
				try {
					Thread.sleep(runInterval);
				} catch (InterruptedException e) {
					log.info("WinEventThread is Interrupted");
					break;
				}
			}
		}
	}
	
	/**
	 * 通知をマネージャに送信する。
	 * @param priority
	 * @param message
	 * @param messageOrg
	 * @param monitorId
	 */
	public static void sendMessage(int priority, String message, String messageOrg, String monitorId, AgtRunInstructionInfoRequest runInstructionInfo) {
		MessageSendableObject sendme = new MessageSendableObject();
		sendme.body = new AgtOutputBasicInfoRequest();
		sendme.body.setPluginId(HinemosModuleConstant.MONITOR_WINEVENT);
		sendme.body.setPriority(priority);
		sendme.body.setApplication(MessageConstant.AGENT.getMessage());
		sendme.body.setMessage(message);
		sendme.body.setMessageOrg(messageOrg);
		sendme.body.setGenerationDate(HinemosTime.getDateInstance().getTime());
		sendme.body.setMonitorId(monitorId);
		sendme.body.setFacilityId(""); // マネージャがセットする。
		sendme.body.setScopeText(""); // マネージャがセットする。
		sendme.body.setRunInstructionInfo(runInstructionInfo);

		sendQueue.put(sendme);
	}
}
