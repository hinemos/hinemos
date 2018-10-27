/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform.selfcheck;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.selfcheck.monitor.SelfCheckMonitorBase;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

import com.clustercontrol.commons.util.HinemosPropertyCommon;

/**
 * データベースの蓄積量を確認する処理の実装クラス
 */
public class DBSizeMonitor extends SelfCheckMonitorBase {

	private static Log m_log = LogFactory.getLog( DBSizeMonitor.class );

	public final String monitorId = "SYS_DBSIZE";
	public final String application = "SELFCHECK (DB Size)";
	public final String subKey = "";

	/**
	 * コンストラクタ
	 * @param dataSourceName データソース名（"HinemosDS"など）
	 */
	public DBSizeMonitor() {
	}

	/**
	 * 監視項目ID
	 */
	@Override
	public String getMonitorId() {
		return monitorId;
	}

	/**
	 * テーブルのサイズチェック処理
	 */
	@Override
	public void execute() {
		boolean warn = true;

		if (!HinemosPropertyCommon.selfcheck_monitoring_dbsize.getBooleanValue()) {
			m_log.debug("skip");
			return;
		}

		long sizeThreshold = HinemosPropertyCommon.selfcheck_monitoring_dbsize_threshold.getNumericValue();

		m_log.debug("monitoring database size. (threshold=" + sizeThreshold + ")");
		double dbSize = getDatabaseSize();
		
		if (dbSize == -1) {
			m_log.info("skipped monitoring database size. (threshold = " + sizeThreshold + " [mbyte])");
			return;
		} else {
			if (dbSize <= sizeThreshold) {
				m_log.debug("database size is low. (size = " + String.format("%.2f", dbSize) + " [mbyte], threshold = " + sizeThreshold + " [mbyte])");
				warn = false;
			}
		}
		if (warn) {
			m_log.info("database size is high. (usage = " + String.format("%.2f", dbSize) + " [mbyte], threshold = " + sizeThreshold + " [mbyte])");
		}

		if (!isNotify(subKey, warn)) {
			return;
		}
		
		String[] msgAttr1 = {String.format("%.2f", dbSize), Long.toString(sizeThreshold)};
		AplLogger.put(PriorityConstant.TYPE_WARNING, PLUGIN_ID, MessageConstant.MESSAGE_SYS_023_SYS_SFC, msgAttr1,
				"usage of database has exceeded its threshold (" +
						String.format("%.2f", dbSize) +
						" [mbyte] > threshold " +
						sizeThreshold +
						" [mbyte])" +
				").");
		return;
	}

	private double getDatabaseSize() {
		JpaTransactionManager tm = null;
		HinemosEntityManager em = null;

		String query = "EXEC sp_spaceused";
		double physicalSize = -1;

		// メイン処理
		try {
			tm = new JpaTransactionManager();
			tm.begin();

			em = tm.getEntityManager();
			
			Object[] data = (Object[])(em.createNativeQuery(query).getSingleResult());
			
			if (data != null && data.length == 3) {
				physicalSize = Double.valueOf(((String)data[1]).replace("MB", "").trim());
			}

			tm.commit();
		} catch (Exception e) {
			m_log.warn("database query execution failure. (" + query + ")", e);
		} finally {
			if (tm != null) {
				tm.close();
			}
		}

		return physicalSize;
	}
}
