/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.process.factory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitorNumericValueType;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;
import com.clustercontrol.process.model.ProcessCheckInfo;
import com.clustercontrol.process.util.QueryUtil;

/**
 * プロセス監視情報を更新するクラス<BR>
 *
 * @version 4.1.0
 * @since 2.0.0
 */
public class ModifyMonitorProcess extends ModifyMonitorNumericValueType{
	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.AddMonitor#addCheckInfo()
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {
		// プロセス監視情報を設定
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		
		ProcessCheckInfo process = m_monitorInfo.getProcessCheckInfo();
		process.setMonitorId(m_monitorInfo.getMonitorId());

		em.persist(process);

		return true;
	}
	
	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.ModifyMonitor#modifyCheckInfo()
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole {

		MonitorInfo monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		// プロセス監視情報を取得
		ProcessCheckInfo entity = QueryUtil.getMonitorProcessInfoPK(m_monitorInfo.getMonitorId());

		// プロセス監視情報を設定
		ProcessCheckInfo process = m_monitorInfo.getProcessCheckInfo();
		entity.setCommand(process.getCommand());
		entity.setParam(process.getParam());
		entity.setCaseSensitivityFlg(process.getCaseSensitivityFlg());

		monitorEntity.setProcessCheckInfo(entity);

		return true;
	}
	
	/**
	 * スケジュール実行の遅延時間を返します。
	 */
	@Override
	protected int getDelayTime() {
		// プロセス監視の場合使用しないが、Override必須なのでdummy値を返却
		return 0;
	}

	/**
	 * スケジュール実行種別を返します。
	 */
	@Override
	protected TriggerType getTriggerType() {
		// プロセス監視はノード個別のポーラーによりキックされるため、通常の監視項目のポーラー登録は行なわない
		return TriggerType.NONE;
	}
}
