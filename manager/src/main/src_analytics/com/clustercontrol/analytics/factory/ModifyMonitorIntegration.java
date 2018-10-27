/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.factory;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.analytics.model.IntegrationCheckInfo;
import com.clustercontrol.analytics.model.IntegrationConditionInfo;
import com.clustercontrol.analytics.model.IntegrationConditionInfoPK;
import com.clustercontrol.analytics.util.QueryUtil;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitor;
import com.clustercontrol.monitor.run.factory.ModifyMonitorTruthValueType;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * 収集値統合監視情報更新クラス
 *
 * @version 6.1.0
 */
public class ModifyMonitorIntegration extends ModifyMonitorTruthValueType {
	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.AddMonitor#addCheckInfo()
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			IntegrationCheckInfo integrationInfo = m_monitorInfo.getIntegrationCheckInfo();
			
			integrationInfo.setMonitorId(m_monitorInfo.getMonitorId());
			em.persist(integrationInfo);

			// 判定条件を設定
			List<IntegrationConditionInfo> list = integrationInfo.getConditionList();
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					IntegrationConditionInfo condition = list.get(i);
					condition.setOrderNo(i + 1);
					condition.setMonitorId(m_monitorInfo.getMonitorId());
					em.persist(condition);
					condition.relateToMonitorIntegrationInfo(integrationInfo);
				}
			}
			return true;
		}
	}
	
	/* (非 Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.ModifyMonitor#modifyCheckInfo()
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			MonitorInfo monitorEntity
			= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

			// 収集値統合監視情報を取得
			IntegrationCheckInfo entity = QueryUtil.getMonitorIntegrationInfoPK(m_monitorInfo.getMonitorId());

			// 収集値統合監視情報を設定
			IntegrationCheckInfo integration = m_monitorInfo.getIntegrationCheckInfo();
			entity.setTimeout(integration.getTimeout());
			if (integration.getNotOrder() != null) {
				entity.setNotOrder(integration.getNotOrder());
			}
			entity.setMessageOk(integration.getMessageOk());
			entity.setMessageNg(integration.getMessageNg());
			
			List<IntegrationConditionInfoPK> integrationConditionInfoPKList = new ArrayList<>();
			if (integration.getConditionList() != null) {

				int orderNo = 0;
				for (IntegrationConditionInfo condition : integration.getConditionList()) {
					if (condition != null) {
						IntegrationConditionInfo conditionInfo = null;
						IntegrationConditionInfoPK conditionInfoPk = new IntegrationConditionInfoPK(
								m_monitorInfo.getMonitorId(),
								Integer.valueOf(++orderNo));
						try {
							conditionInfo = QueryUtil.getMonitorIntegrationConditionInfoPK(conditionInfoPk);
						} catch (MonitorNotFound e) {
							conditionInfo = new IntegrationConditionInfo(conditionInfoPk);
							em.persist(conditionInfo);
							conditionInfo.relateToMonitorIntegrationInfo(entity);
						}
						conditionInfo.setDescription(condition.getDescription());
						conditionInfo.setMonitorNode(condition.getMonitorNode());
						if (conditionInfo.getMonitorNode()) {
							conditionInfo.setTargetFacilityId("");
						} else {
							conditionInfo.setTargetFacilityId(condition.getTargetFacilityId());
						}
						conditionInfo.setTargetMonitorId(condition.getTargetMonitorId());
						conditionInfo.setTargetItemName(condition.getTargetItemName());
						conditionInfo.setTargetDisplayName(condition.getTargetDisplayName());
						conditionInfo.setComparisonMethod(condition.getComparisonMethod());
						conditionInfo.setComparisonValue(condition.getComparisonValue());
						if (condition.getIsAnd() == null) {
							conditionInfo.setIsAnd(true);
						} else {
							conditionInfo.setIsAnd(condition.getIsAnd());
						}
						integrationConditionInfoPKList.add(conditionInfoPk);
					}
				}	
			}
			// 不要なIntegrationConditionInfoを削除
			entity.deleteIntegrationConditionList(integrationConditionInfoPKList);
			monitorEntity.setIntegrationCheckInfo(entity);
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
