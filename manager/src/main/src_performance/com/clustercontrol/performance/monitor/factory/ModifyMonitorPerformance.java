/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.monitor.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitorNumericValueType;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.performance.monitor.model.PerfCheckInfo;
import com.clustercontrol.performance.monitor.util.QueryUtil;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * リソース監視情報更新クラス
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class ModifyMonitorPerformance extends ModifyMonitorNumericValueType{

	private static Log m_log = LogFactory.getLog( ModifyMonitorPerformance.class );

	/**
	 *  リソース監視情報を登録します。
	 * @see com.clustercontrol.monitor.run.factory.AddMonitor#addCheckInfo()
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, HinemosUnknown, InvalidRole {
		m_log.debug("addCheckInfo() monitorId = " + m_monitorInfo.getMonitorId());

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// リソース監視情報を設定
			PerfCheckInfo perf = m_monitorInfo.getPerfCheckInfo();
			perf.setMonitorId(m_monitorInfo.getMonitorId());

			em.persist(perf);

			return true;
		}
	}

	/**
	 * スケジュール実行の遅延時間を返します。
	 */
	@Override
	protected int getDelayTime() {
		// リソース監視の場合使用しないが、Override必須なのでdummy値を返却
		return 0;
	}
	
	/**
	 * スケジュール実行種別を返します。
	 */
	@Override
	protected TriggerType getTriggerType() {
		// リソース監視はノード個別のポーラーによりキックされるため、通常の監視項目のポーラー登録は行なわない
		return TriggerType.NONE;
	}
	
	/**
	 * リソース監視判定情報を更新します。
	 * @see com.clustercontrol.monitor.run.factory.ModifyMonitor#modifyCheckInfo()
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, HinemosUnknown, InvalidRole {
		m_log.debug("modifyCheckInfo() monitorId = " + m_monitorInfo.getMonitorId());

		MonitorInfo monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		// 閾値監視情報を取得
		PerfCheckInfo entity = QueryUtil.getMonitorPerfInfoPK(m_monitorInfo.getMonitorId());
		PerfCheckInfo perfCheck = m_monitorInfo.getPerfCheckInfo();

		// 閾値監視情報を設定
		entity.setItemCode(perfCheck.getItemCode());
		entity.setDeviceDisplayName(perfCheck.getDeviceDisplayName());
		entity.setBreakdownFlg(perfCheck.getBreakdownFlg());
		monitorEntity.setPerfCheckInfo(entity);

		return true;
	}
}
