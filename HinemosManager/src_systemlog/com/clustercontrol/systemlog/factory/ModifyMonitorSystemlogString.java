/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.systemlog.factory;

import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitorStringValueType;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * システムログ監視情報をマネージャで変更するクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 * 
 */
public class ModifyMonitorSystemlogString extends ModifyMonitorStringValueType{
	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.AddMonitor#addCheckInfo()
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound {

		// システムログ監視のチェック条件はないため、常にtrue
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.ModifyMonitor#modifyCheckInfo()
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound {
		// システムログ監視のチェック条件はないため、常にtrue
		return true;
	}

	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.DeleteMonitor#deleteCheckInfo()
	 */
	@Override
	protected boolean deleteCheckInfo() {

		// システムログ監視のチェック条件はないため、常にtrue
		return true;
	}
	
	/**
	 * スケジュール実行の遅延時間を返します。
	 */
	@Override
	protected int getDelayTime() {
		return 0;
	}

	/**
	 * スケジュール実行種別を返します。
	 */
	@Override
	protected TriggerType getTriggerType() {
		return TriggerType.NONE;
	}
}
