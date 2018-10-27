/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.factory;

import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitor;
import com.clustercontrol.monitor.run.factory.ModifyMonitorTruthValueType;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * Hinemos Agent監視情報更新クラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class ModifyMonitorAgent extends ModifyMonitorTruthValueType{
	/**
	 * エージェント監視情報を設定します。<BR>
	 */
	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.AddMonitor#addCheckInfo()
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound {
		// Agent監視情報を設定
		// Agent監視は監視の有無のみ。CheckInfoは存在しない。

		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.ModifyMonitor#modifyCheckInfo()
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound {

		// Agent監視情報を取得
		// Agent監視情報は監視の有無のみ。CheckInfoは存在しない。

		return true;
	}

	/**
	 * スケジュール実行の遅延時間を返します。
	 */
	@Override
	protected int getDelayTime() {
		return ModifyMonitor.getDelayTimeBasic(m_monitorInfo);
	}

	/**
	 * スケジュール実行種別を返します。
	 */
	@Override
	protected TriggerType getTriggerType() {
		return TriggerType.SIMPLE;
	}
	
	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.DeleteMonitor#deleteCheckInfo()
	 */
	@Override
	protected boolean deleteCheckInfo() {

		// Agent監視情報を取得
		// Agent監視は監視の有無を設定するのみ。CheckInfoは存在しない。

		return true;
	}
}