/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.binary.model.PacketCheckInfo;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * パケットキャプチャ監視情報をマネージャで変更するクラス<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 */
public class ModifyMonitorPacketCapture extends ModifyMonitorBinary {

	private static Log m_log = LogFactory.getLog(ModifyMonitorPacketCapture.class);

	/**
	 * パケットキャプチャ監視情報追加<br>
	 * <br>
	 * {@inheritDoc}
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, HinemosUnknown, InvalidRole {
		// パケットキャプチャ監視情報を取得.
		PacketCheckInfo checkInfo = m_monitorInfo.getPacketCheckInfo();
		checkInfo.setMonitorId(m_monitorInfo.getMonitorId());

		// パケットキャプチャ監視情報を追加
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			em.persist(checkInfo);
			return true;
		}
	}

	/**
	 * パケットキャプチャ監視情報変更<br>
	 * <br>
	 * {@inheritDoc}
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole, HinemosUnknown {

		// パケットキャプチャ監視情報を取得.
		PacketCheckInfo oldEntity = m_monitorInfo.getPacketCheckInfo();
		PacketCheckInfo entity = QueryUtil.getPacketCheckInfoPK(m_monitorInfo.getMonitorId());

		// パケットキャプチャ監視情報を変更.
		entity.setPromiscuousMode(oldEntity.isPromiscuousMode());
		entity.setFilterStr(oldEntity.getFilterStr());

		// ログ出力.
		m_log.trace("modify() : entity.isPromiscuousMode = " + entity.isPromiscuousMode());
		m_log.trace("modify() : entity.getFilterStr = " + entity.getFilterStr());

		return true;
	}

	/**
	 * パケットキャプチャ監視のスケジュール実行種別としてトリガー指定なしを設定.<br>
	 * <br>
	 * {@inheritDoc}
	 */
	@Override
	protected TriggerType getTriggerType() {
		return TriggerType.NONE;
	}

	/**
	 * パケットキャプチャ監視のスケジュール実行遅延時間として0を設定.<br>
	 * <br>
	 * {@inheritDoc}
	 */
	@Override
	protected int getDelayTime() {
		return 0;
	}

}
