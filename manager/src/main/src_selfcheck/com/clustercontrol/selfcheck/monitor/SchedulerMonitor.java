/*

Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.selfcheck.monitor;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.plugin.impl.SchedulerInfo;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * Quartzスケジューラの稼動状態を確認する処理の実装クラス
 */
public class SchedulerMonitor extends SelfCheckMonitorBase {

	private static Log m_log = LogFactory.getLog( SchedulerMonitor.class );

	public final SchedulerType type;
	public final SchedulerInfo trigger;
	public int thresholdSec;

	public final String monitorId = "SYS_SCHEDULE";
	public final String subKey;
	public final String application = "SELFCHECK (Scheduler)";

	/**
	 * コンストラクタ
	 * @param type スケジューラの種別(DBMS or RAM)
	 * @param thresholdSec 最大遅延時間（この時間より長い場合、スケジュール実行時刻が遅延している場合に非稼動とする）
	 */
	public SchedulerMonitor(SchedulerType type, SchedulerInfo trigger) {
		this.type = type;
		this.trigger = trigger;

		this.subKey = trigger.name + ":" + trigger.group;
	}

	/**
	 * セルフチェック処理名
	 */
	@Override
	public String toString() {
		return String.format("monitoring scheduler (type = %s, name = %s, group = %s, threshold = %d)",
				type, trigger.name, trigger.group, thresholdSec);
	}

	/**
	 * 監視項目ID
	 */
	@Override
	public String getMonitorId() {
		return monitorId;
	}

	/**
	 * Quartzスケジューラの稼動状態を確認する処理
	 * @return 通知情報（アプリケーション名は未格納）
	 */
	@Override
	public void execute() {
		if (!HinemosPropertyUtil.getHinemosPropertyBool("selfcheck.monitoring.scheduler.delay", true)) {
			m_log.debug("skip");
			return;
		}
		
		/** ローカル変数 */
		Date now = null;
		String nextFireTimeStr = null;
		boolean warn = true;

		thresholdSec = HinemosPropertyUtil.getHinemosPropertyNum(
				"selfcheck.monitoring.scheduler.delay.threshold",
				Long.valueOf(300)).intValue();

		/** メイン処理 */
		if (m_log.isDebugEnabled()) m_log.debug("monitoring a quartz scheduler. ");

		now = HinemosTime.getDateInstance();

		nextFireTimeStr = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", trigger.nextFireTime);

		if (trigger.nextFireTime >= 0) {
			if (now.getTime() - trigger.nextFireTime <= thresholdSec * 1000) {
				m_log.debug("scheduler is running without delay. (triggerName = " + trigger.name + ", triggerGroupName = " + trigger.group + ", nextFireTime = " + nextFireTimeStr + ")");
				warn = false;
			}
		} else {
			m_log.info("skipped monitoring a ascheduler. (triggerName = " + trigger.name + ", triggerGroupName = " + trigger.group + ")");
			return;
		}

		if (warn) {
			m_log.info("scheduler may be not running. (triggerName = " + trigger.name + ", triggerGroupName = " + trigger.group + ", nextFireTime = " + nextFireTimeStr + ")");
		}
		if (!isNotify(subKey, warn)) {
			return;
		}
		String[] msgAttr1 = { type.toString(), trigger.name, trigger.group, nextFireTimeStr, Integer.toString(thresholdSec) };
		AplLogger.put(PriorityConstant.TYPE_WARNING, PLUGIN_ID, MessageConstant.MESSAGE_SYS_004_SYS_SFC, msgAttr1,
				"scheduler （" +
						type +
						":" +
						trigger.name +
						":" +
						trigger.group +
						" - next fire time " +
						nextFireTimeStr +
						"） has not been running for " +
						thresholdSec +
				" [sec].");
		return;
	}
	
	/**
	 * スケジューラ情報の遅延時間を返す。
	 * 
	 * @param type スケジューラ情報の保持種別(RAM : オンメモリで管理、DBMS : DBで永続化管理)
	 * @return 指定したスケジューラ種別の最も遅延している時間
	 * @throws HinemosUnknown
	 */
	public static long getSchedulerDelayTime(SchedulerType type) throws HinemosUnknown {
		long delayMillisec = 0L;
		
		// 指定したスケジューラの中で、最も遅延しているものを取得
		List<SchedulerInfo> triggerList = SchedulerPlugin.getSchedulerList(type);
		for (SchedulerInfo schedulerInfo : triggerList) {
			long tempDelayMillisec = schedulerInfo.nextFireTime - HinemosTime.currentTimeMillis();
			if (tempDelayMillisec > delayMillisec) {
				delayMillisec = tempDelayMillisec;
			}
		}
		return delayMillisec;
	}

}
