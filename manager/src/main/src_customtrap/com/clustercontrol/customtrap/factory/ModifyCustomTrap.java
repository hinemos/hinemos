/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.customtrap.factory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.customtrap.model.CustomTrapCheckInfo;
import com.clustercontrol.customtrap.util.QueryUtil;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitorNumericValueType;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * カスタムトラップ監視の特有設定に対する変更処理実装クラス<br/>
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class ModifyCustomTrap extends ModifyMonitorNumericValueType {
	/**
	 * カスタムトラップ監視特有の設定情報を登録する。<br/>
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// カスタムトラップ監視設定を登録する
			CustomTrapCheckInfo checkInfo = m_monitorInfo.getCustomTrapCheckInfo();
			checkInfo.setMonitorId(m_monitorInfo.getMonitorId());
			em.persist(checkInfo);
			return true;
		}
	}

	/**
	 * カスタムトラップ監視特有の設定情報を更新する。<br/>
	 * また、カスタムトラップ監視設定が変更されたことを影響するエージェントに通知する。<br/>
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole {

		MonitorInfo monitorEntity = com.clustercontrol.monitor.run.util.QueryUtil
				.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		CustomTrapCheckInfo checkInfo = m_monitorInfo.getCustomTrapCheckInfo();

		// 変更前の実行対象の一覧を取得する
		CustomTrapCheckInfo entity = QueryUtil.getMonitorCustomTrapInfoPK(m_monitorInfo.getMonitorId());

		// カスタムトラップ監視設定を更新する
		entity.setTargetKey(checkInfo.getTargetKey());
		entity.setConvertFlg(checkInfo.getConvertFlg());
		monitorEntity.setCustomTrapCheckInfo(entity);
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
