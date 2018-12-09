/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logfile.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.logfile.model.LogfileCheckInfo;
import com.clustercontrol.logfile.util.QueryUtil;
import com.clustercontrol.monitor.run.factory.ModifyMonitorStringValueType;
import com.clustercontrol.plugin.impl.SchedulerPlugin.TriggerType;

/**
 * ログファイル監視情報をマネージャで変更するクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 * 
 */
public class ModifyMonitorLogfileString extends ModifyMonitorStringValueType{

	private static Log m_log = LogFactory.getLog( ModifyMonitorLogfileString.class );

	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.ModifyMonitor#modifyCheckInfo()
	 */
	@Override
	protected boolean modifyCheckInfo() throws MonitorNotFound {

		// ログファイル監視情報を変更
		LogfileCheckInfo oldEntity = m_monitorInfo.getLogfileCheckInfo();
		LogfileCheckInfo entity = QueryUtil.getMonitorLogfileInfoPK(m_monitorInfo.getMonitorId());

		// ログファイル監視情報を設定
		entity.setDirectory(oldEntity.getDirectory());
		entity.setFileName(oldEntity.getFileName());
		entity.setFileEncoding(oldEntity.getFileEncoding());
		entity.setFileReturnCode(oldEntity.getFileReturnCode());
		entity.setPatternHead(oldEntity.getPatternHead());
		entity.setPatternTail(oldEntity.getPatternTail());
		entity.setMaxBytes(oldEntity.getMaxBytes());
		m_log.trace("modify() : entity.getDirectory = " + entity.getDirectory());
		m_log.trace("modify() : entity.getFileName = " + entity.getFileName());
		m_log.trace("modify() : entity.getFileEncoding = " + entity.getFileEncoding());
		m_log.trace("modify() : entity.getFileReturnCode = " + entity.getFileReturnCode());
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
	
	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.DeleteMonitor#deleteCheckInfo()
	 */
	@Override
	protected boolean deleteCheckInfo() {
		return true;
	}
	/* (non-Javadoc)
	 * @see com.clustercontrol.monitor.run.factory.AddMonitor#addCheckInfo()
	 */
	@Override
	protected boolean addCheckInfo() throws MonitorNotFound, InvalidRole {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			LogfileCheckInfo checkInfo = m_monitorInfo.getLogfileCheckInfo();
			checkInfo.setMonitorId(m_monitorInfo.getMonitorId());

			// ログファイル監視情報を追加
			em.persist(checkInfo);
			return true;
		}
	}
}
