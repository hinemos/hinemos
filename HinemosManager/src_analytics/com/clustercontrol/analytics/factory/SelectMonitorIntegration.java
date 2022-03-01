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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.analytics.bean.IntegrationComparisonMethod;
import com.clustercontrol.analytics.bean.MonitorIntegrationConstant;
import com.clustercontrol.analytics.model.IntegrationConditionInfo;
import com.clustercontrol.analytics.util.AnalyticsUtil;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.collect.model.CollectKeyInfoPK;
import com.clustercontrol.fault.CollectKeyNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.factory.SelectMonitor;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.QueryUtil;

/**
 * 収集値統合監視判定情報検索クラス
 *
 * @version 6.1.0
 */
public class SelectMonitorIntegration extends SelectMonitor {

	private static Log m_log = LogFactory.getLog(SelectMonitorIntegration.class);

	@Override
	public MonitorInfo getMonitor(String monitorTypeId, String monitorId)
			throws MonitorNotFound, HinemosUnknown, InvalidRole {
		MonitorInfo bean = super.getMonitor(monitorTypeId, monitorId);
		if (bean.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_INTEGRATION)) {
			if (bean.getIntegrationCheckInfo() != null
					&& bean.getIntegrationCheckInfo().getConditionList() != null) {
				for (IntegrationConditionInfo condition : bean.getIntegrationCheckInfo().getConditionList()) {
					MonitorInfo targetMonitorInfo = null;
					try {
						targetMonitorInfo = QueryUtil.getMonitorInfoPK_OR(
							condition.getTargetMonitorId(), bean.getOwnerRoleId());
						condition.setTargetMonitorType(targetMonitorInfo.getMonitorType());
						if (targetMonitorInfo.getMonitorType() == MonitorTypeConstant.TYPE_NUMERIC
								|| targetMonitorInfo.getMonitorType() == MonitorTypeConstant.TYPE_STRING) {
							condition.setTargetMonitorType(targetMonitorInfo.getMonitorType());
						}
						if (targetMonitorInfo.getMonitorType() == MonitorTypeConstant.TYPE_NUMERIC) {
							condition.setTargetItemDisplayName(
								AnalyticsUtil.getMsgItemName(
										condition.getTargetItemName(), 
										condition.getTargetDisplayName(), 
										condition.getTargetMonitorId()));
						} else if (targetMonitorInfo.getMonitorType() == MonitorTypeConstant.TYPE_STRING) {
							condition.setTargetItemDisplayName(condition.getTargetMonitorId());
						}
					} catch (MonitorNotFound | InvalidRole e) {
						//　エラーにしない
						m_log.info("getMonitor() targetMonitorInfo is not found. monitor_id=" + condition.getTargetMonitorId()
						 + ", owner_role_id=" + bean.getOwnerRoleId());
					}
				}
			}
		}
		return bean;
	}
	
	/**
	 * 指定された条件に一致する収集データを取得する
	 * 
	 * @param condition　IntegrationCondition
	 * @param facilityId　取得対象のファシリティID
	 * @param startDate　取得日時(From)
	 * @param endDate　取得日時(To)
	 * @return 収集データ
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public List<CollectData> getCollectDataForNumeric(
			IntegrationConditionInfo condition,
			String facilityId,
			Long startDate,
			Long endDate)
			throws HinemosUnknown {
		List<CollectData> list = new ArrayList<>();

		if (condition == null
				|| startDate == null || endDate == null
				|| startDate > endDate) {
			return list;
		}

		// 収集キーを取得する
		CollectKeyInfo keyInfo = null;
		try {
			keyInfo = com.clustercontrol.collect.util.QueryUtil.getCollectKeyPK(
					new CollectKeyInfoPK(condition.getTargetItemName(), 
							condition.getTargetDisplayName(), condition.getTargetMonitorId(), facilityId));
		} catch (CollectKeyNotFound e) {
			String message = "getCollectDataForNumeric() : " + e.getClass().getSimpleName() + ", " + e.getMessage();
			throw new HinemosUnknown(message, e);
		}

		// 比較方法が不正な場合は処理終了
		if (!IntegrationComparisonMethod.symbols().contains(condition.getComparisonMethod())) {
			String message = "getCollectDataForNumeric() : comparisonMethod is invalid. comparisonMethod=" 
				+ condition.getComparisonMethod();
			m_log.info(message);
			throw new HinemosUnknown(message);
		}

		Double dblValue = null;
		// 比較値が不正な場合は処理終了
		if (MonitorIntegrationConstant.NUMBER_NAN.equals(condition.getComparisonValue())
			&& IntegrationComparisonMethod.EQ.symbol().equals(condition.getComparisonMethod())) {
			// 比較値NaNのため何もしない
		} else {
			try {
				dblValue = Double.parseDouble(condition.getComparisonValue());
			} catch (NumberFormatException e) {
				String message = "getCollectDataForNumeric() : comparisonValue is invalid. comparisonMethod=" 
						+ condition.getComparisonValue();
					m_log.info(message);
					throw new HinemosUnknown(message);
			}
		}
		// 収集データを取得する
		list = com.clustercontrol.collect.util.QueryUtil.getCollectDataListByCondition(
				keyInfo.getCollectorid(), startDate, endDate, condition.getComparisonMethod(), dblValue);
		return list;
	}
}
