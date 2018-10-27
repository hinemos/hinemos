/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.custom.factory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.custom.model.CustomCheckInfo;
import com.clustercontrol.custom.util.QueryUtil;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitorStringValueType;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * コマンド監視(文字列)の特有設定に対する変更処理実装クラス<br/>
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class ModifyCustomString extends ModifyMonitorStringValueType {
	/**
	 * コマンド監視特有の設定情報を登録する。<br/>
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// コマンド監視設定を登録する
			CustomCheckInfo checkInfo = m_monitorInfo.getCustomCheckInfo();
			checkInfo.setMonitorId(m_monitorInfo.getMonitorId());
			em.persist(checkInfo);
			return true;
		}
	}

	/**
	 * コマンド監視特有の設定情報を更新する。<br/>
	 * また、コマンド監視設定が変更されたことを影響するエージェントに通知する。<br/>
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole {

		MonitorInfo monitorEntity
		= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorInfo.getMonitorId());

		CustomCheckInfo checkInfo = m_monitorInfo.getCustomCheckInfo();

		// 変更前の実行対象の一覧を取得する
		CustomCheckInfo entity = QueryUtil.getMonitorCustomInfoPK(m_monitorInfo.getMonitorId());

		// コマンド監視設定を更新する
		entity.setCommandExecType(checkInfo.getCommandExecType());
		entity.setSelectedFacilityId(checkInfo.getSelectedFacilityId());
		entity.setSpecifyUser(checkInfo.getSpecifyUser());
		entity.setEffectiveUser(checkInfo.getEffectiveUser());
		entity.setCommand(checkInfo.getCommand());
		entity.setTimeout(checkInfo.getTimeout());
		monitorEntity.setCustomCheckInfo(entity);
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
