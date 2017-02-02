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

package com.clustercontrol.agent.winevent;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.ws.agent.OutputBasicInfo;
import com.clustercontrol.ws.jobmanagement.RunInstructionInfo;

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
					for (String mapKey : winEventMonitorMap.keySet()) {
						WinEventMonitor winEventMonitor = winEventMonitorMap.get(mapKey);
						winEventMonitor.run();
					}
				} catch (Exception e) {
					log.warn("WinEventThread : " + e.getClass().getCanonicalName() + ", " +
							e.getMessage(), e);
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
	public static void sendMessage(int priority, String message, String messageOrg, String monitorId, RunInstructionInfo runInstructionInfo) {
		OutputBasicInfo output = new OutputBasicInfo();
		output.setPluginId(HinemosModuleConstant.MONITOR_WINEVENT);
		output.setPriority(priority);
		output.setApplication(MessageConstant.AGENT.getMessage());
		output.setMessage(message);
		output.setMessageOrg(messageOrg);
		output.setGenerationDate(HinemosTime.getDateInstance().getTime());
		output.setMonitorId(monitorId);
		output.setFacilityId(""); // マネージャがセットする。
		output.setScopeText(""); // マネージャがセットする。
		output.setRunInstructionInfo(runInstructionInfo);

		sendQueue.put(output);
	}
}
