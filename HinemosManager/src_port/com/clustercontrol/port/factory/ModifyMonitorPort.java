/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.port.factory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitor;
import com.clustercontrol.monitor.run.factory.ModifyMonitorNumericValueType;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;
import com.clustercontrol.port.model.MonitorProtocolMstEntity;
import com.clustercontrol.port.model.PortCheckInfo;
import com.clustercontrol.port.util.QueryUtil;

/**
 * port監視情報更新クラス
 *
 * @version 4.0.0
 * @since 2.4.0
 */
public class ModifyMonitorPort extends ModifyMonitorNumericValueType{
	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.AddMonitor#addCheckInfo()
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			PortCheckInfo port = m_monitorInfo.getPortCheckInfo();
			
			port.setMonitorId(m_monitorInfo.getMonitorId());
			em.persist(port);
			
			try {
				MonitorProtocolMstEntity monitorProtocolMstEntity = QueryUtil.getMonitorProtocolMstPK(port.getServiceId());
				port.relateToMonitorProtocolMstEntity(monitorProtocolMstEntity);
			} catch (MonitorNotFound e) {
			}
			
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

		// port監視情報を取得
		PortCheckInfo entity = QueryUtil.getMonitorPortInfoPK(m_monitorInfo.getMonitorId());

		// port監視情報を設定
		PortCheckInfo port = m_monitorInfo.getPortCheckInfo();
		entity.setPortNo(port.getPortNo());  //ポート番号
		entity.setRunCount(port.getRunCount());
		entity.setRunInterval(port.getRunInterval());
		entity.setTimeout(port.getTimeout());
		MonitorProtocolMstEntity monitorProtocolMstEntity = null;
		try {
			monitorProtocolMstEntity = QueryUtil.getMonitorProtocolMstPK(port.getServiceId());
		} catch (MonitorNotFound e) {
		}
		entity.relateToMonitorProtocolMstEntity(monitorProtocolMstEntity);
		monitorEntity.setPortCheckInfo(entity);

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
