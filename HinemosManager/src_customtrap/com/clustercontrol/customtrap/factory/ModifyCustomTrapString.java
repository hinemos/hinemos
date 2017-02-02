/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.customtrap.factory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.customtrap.model.CustomTrapCheckInfo;
import com.clustercontrol.customtrap.util.QueryUtil;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitorStringValueType;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * カスタムトラップ監視(文字列)の特有設定に対する変更処理実装クラス<br/>
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class ModifyCustomTrapString extends ModifyMonitorStringValueType {
	/**
	 * カスタムトラップ監視特有の設定情報を登録する。<br/>
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {
		// カスタムトラップ監視設定を登録する
		CustomTrapCheckInfo checkInfo = m_monitorInfo.getCustomTrapCheckInfo();
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		checkInfo.setMonitorId(m_monitorInfo.getMonitorId());
		em.persist(checkInfo);
		return true;
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
