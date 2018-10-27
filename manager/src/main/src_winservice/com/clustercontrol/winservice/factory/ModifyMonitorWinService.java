/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.winservice.factory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitor;
import com.clustercontrol.monitor.run.factory.ModifyMonitorTruthValueType;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;
import com.clustercontrol.winservice.model.WinServiceCheckInfo;
import com.clustercontrol.winservice.util.QueryUtil;

/**
 * Windowsサービス監視情報を更新するクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class ModifyMonitorWinService extends ModifyMonitorTruthValueType{

	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.AddMonitor#addCheckInfo()
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// Windowsサービス監視情報を設定
			WinServiceCheckInfo winService = m_monitorInfo.getWinServiceCheckInfo();

			winService.setMonitorId(m_monitorInfo.getMonitorId());
			em.persist(winService);

			return true;
		}
	}
	
	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.ModifyMonitor#modifyCheckInfo()
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole {

		MonitorInfo monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		// Windowsサービス監視情報を取得
		WinServiceCheckInfo entity = QueryUtil.getMonitorWinserviceInfoPK(m_monitorInfo.getMonitorId());

		// Windowsサービス監視情報を設定
		WinServiceCheckInfo winService = m_monitorInfo.getWinServiceCheckInfo();
		entity.setServiceName(winService.getServiceName());
		monitorEntity.setWinServiceCheckInfo(entity);

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
}
