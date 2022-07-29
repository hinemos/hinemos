/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.factory;

import com.clustercontrol.analytics.model.CorrelationCheckInfo;
import com.clustercontrol.analytics.util.QueryUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitor;
import com.clustercontrol.monitor.run.factory.ModifyMonitorNumericValueType;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.MonitorCollectDataCacheRefreshCallback;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * 相関係数監視情報更新クラス
 *
 * @version 6.1.0
 */
public class ModifyMonitorCorrelation extends ModifyMonitorNumericValueType{
	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.AddMonitor#addCheckInfo()
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			CorrelationCheckInfo correlationInfo = m_monitorInfo.getCorrelationCheckInfo();
			
			correlationInfo.setMonitorId(m_monitorInfo.getMonitorId());
			em.persist(correlationInfo);
			
			
			// この監視で収集値を保持しなければいけない期間を更新する
			jtm.addCallback(new MonitorCollectDataCacheRefreshCallback(
					m_monitorInfo.getCorrelationCheckInfo().getTargetMonitorId(), 
					m_monitorInfo.getMonitorTypeId(), 
					m_monitorInfo.getCorrelationCheckInfo().getAnalysysRange()));
			jtm.addCallback(new MonitorCollectDataCacheRefreshCallback(
					m_monitorInfo.getCorrelationCheckInfo().getReferMonitorId(), 
					m_monitorInfo.getMonitorTypeId(), 
					m_monitorInfo.getCorrelationCheckInfo().getAnalysysRange()));
			
			return true;
		}
	}
	
	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.ModifyMonitor#modifyCheckInfo()
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			MonitorInfo monitorEntity
			= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());
	
			// 相関係数監視情報を取得
			CorrelationCheckInfo entity = QueryUtil.getMonitorCorrelationInfoPK(m_monitorInfo.getMonitorId());
	
			// 相関係数監視情報を設定
			CorrelationCheckInfo correlation = m_monitorInfo.getCorrelationCheckInfo();
			entity.setTargetMonitorId(correlation.getTargetMonitorId());
			entity.setTargetDisplayName(correlation.getTargetDisplayName());
			entity.setTargetItemName(correlation.getTargetItemName());
			entity.setReferMonitorId(correlation.getReferMonitorId());
			entity.setReferDisplayName(correlation.getReferDisplayName());
			entity.setReferItemName(correlation.getReferItemName());
			entity.setReferFacilityId(correlation.getReferFacilityId());
			entity.setAnalysysRange(correlation.getAnalysysRange());
			monitorEntity.setCorrelationCheckInfo(entity);
	
			// この監視で収集値を保持しなければいけない期間を更新する
			jtm.addCallback(new MonitorCollectDataCacheRefreshCallback(
					m_monitorInfo.getCorrelationCheckInfo().getTargetMonitorId(), 
					m_monitorInfo.getMonitorTypeId(), 
					m_monitorInfo.getCorrelationCheckInfo().getAnalysysRange()));
			jtm.addCallback(new MonitorCollectDataCacheRefreshCallback(
					m_monitorInfo.getCorrelationCheckInfo().getReferMonitorId(), 
					m_monitorInfo.getMonitorTypeId(), 
					m_monitorInfo.getCorrelationCheckInfo().getAnalysysRange()));
			
			return true;
		}
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
