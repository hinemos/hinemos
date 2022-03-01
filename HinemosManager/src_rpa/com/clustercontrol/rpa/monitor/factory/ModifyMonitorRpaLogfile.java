/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.monitor.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.ModifyMonitorStringValueType;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;
import com.clustercontrol.rpa.monitor.model.RpaLogFileCheckInfo;
import com.clustercontrol.rpa.util.QueryUtil;

public class ModifyMonitorRpaLogfile extends ModifyMonitorStringValueType {
	private static Log m_log = LogFactory.getLog( ModifyMonitorRpaLogfile.class );

	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.ModifyMonitor#modifyCheckInfo()
	 */

	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound, InvalidRole, HinemosUnknown {
		// RPAログファイル監視情報を変更
		RpaLogFileCheckInfo oldEntity = m_monitorInfo.getRpaLogFileCheckInfo();
		RpaLogFileCheckInfo entity = QueryUtil.getMonitorRpaLogfileInfoPK_NONE(m_monitorInfo.getMonitorId());
		
		// RPAログファイル監視情報を設定
		entity.setRpaToolEnvId(oldEntity.getRpaToolEnvId());
		entity.setDirectory(oldEntity.getDirectory());
		entity.setFileEncoding(oldEntity.getFileEncoding());
		entity.setFileName(oldEntity.getFileName());
		m_log.trace("modify() : entity.getRpaToolId = " + entity.getRpaToolEnvId());
		m_log.trace("modify() : entity.getDirectory = " + entity.getDirectory());
		m_log.trace("modify() : entity.getFileName = " + entity.getFileName());
		m_log.trace("modify() : entity.getFileEncoding = " + entity.getFileEncoding());

		return true;
	}

	/**
	 * スケジュール実行種別を返します。
	 */
	@Override
	protected TriggerType getTriggerType() {
		return TriggerType.NONE;
	}

	/**
	 * スケジュール実行の遅延時間を返します。
	 */
	@Override
	protected int getDelayTime() {
		return 0;
	}

	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, HinemosUnknown, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			RpaLogFileCheckInfo checkInfo = m_monitorInfo.getRpaLogFileCheckInfo();
			checkInfo.setMonitorId(m_monitorInfo.getMonitorId());

			// ログファイル監視情報を追加
			em.persist(checkInfo);
			return true;
		}
	}


	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.DeleteMonitor#deleteCheckInfo()
	 */
	@Override
	protected boolean deleteCheckInfo() {
		return true;
	}

}
