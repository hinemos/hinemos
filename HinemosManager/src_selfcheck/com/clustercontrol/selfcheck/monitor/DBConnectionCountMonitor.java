/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.persistence.sessions.server.ConnectionPool;
import org.eclipse.persistence.sessions.server.ServerSession;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * データベースのコネクション数を確認する処理の実装クラス
 */
public class DBConnectionCountMonitor extends SelfCheckMonitorBase {

	private static Log m_log = LogFactory.getLog( DBConnectionCountMonitor.class );

	public final String monitorId = "SYS_DB_CONNECT_COUNT";
	public final String subKey = "";
	public final String application = "SELFCHECK (DB Connection Count)";

	/**
	 * セルフチェック処理名
	 */
	@Override
	public String toString() {
		int threshold = HinemosPropertyCommon.selfcheck_monitoring_db_connection_count_threshold.getIntegerValue();
		return "monitoring database connection (threshold = " + threshold + ")";
	}

	/**
	 * 監視項目ID
	 */
	@Override
	public String getMonitorId() {
		return monitorId;
	}

	/**
	 * 利用されているコネクション数が閾値以上であるかを確認する処理
	 * @return 通知情報（アプリケーション名は未格納）
	 */
	@Override
	public void execute() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			if(!HinemosPropertyCommon.selfcheck_monitoring_db_connection_count.getBooleanValue()) {
				m_log.debug("skip");
				return;
			}
			
			/** メイン処理 */
			if (m_log.isDebugEnabled()) m_log.debug("monitoring the number of database connection.");

			/** ローカル変数 */
			ServerSession ss = em.unwrap(ServerSession.class);
			
			for (ConnectionPool pool : ss.getConnectionPools().values()) {
				int threshold = pool.getMaxNumberOfConnections() *
						HinemosPropertyCommon.selfcheck_monitoring_db_connection_count_threshold.getIntegerValue()
						/ 100;
				boolean warn = true;
				
				// プール毎のデータベースのセッションコネクション数を取得する
				int count = (pool.getTotalNumberOfConnections() - pool.getConnectionsAvailable().size());
				
				if (m_log.isDebugEnabled()) {
					m_log.debug("total=" + pool.getTotalNumberOfConnections() +
							", max=" + pool.getMaxNumberOfConnections() +
							", available=" + pool.getConnectionsAvailable().size() +
							", threshold(%)=" + HinemosPropertyCommon.selfcheck_monitoring_db_connection_count_threshold.getIntegerValue() +
							", threshold=" + threshold + 
							", count=" + count);
				}
				
				if (count == -1) {
					m_log.info("skipped monitoring database connection. (threshold=" + threshold);
					continue;
				} else if (count <= threshold) {
					m_log.debug("the number of database connection is low. (count = " + count + ", threshold = " + threshold + ")");
					warn = false;
				}

				if (warn) {
					m_log.info("the number of database connection is too many. (count= "  + count + ", threshold = " + threshold + ")");
				}
				if (!isNotify(subKey, warn)) {
					continue;
				}

				String[] msgAttr1 = { Integer.toString(count),
						Integer.toString(threshold) };
				AplLogger.put(InternalIdCommon.SYS_SFC_SYS_021, msgAttr1,
						"the number of database connection is too many (" +
								count +
								" > threshold " +
								threshold +
						").");
			}

			return;
		}
	}
	
	/**
	 * データベースのコネクション数を返す<br/>
	 * @return データベースのコネクション数
	 */
	public static int getDBConnectionCount() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			ServerSession ss = em.unwrap(ServerSession.class);
			
			int used = 0;
			for (ConnectionPool pool : ss.getConnectionPools().values()) {
				used += (pool.getTotalNumberOfConnections() - pool.getConnectionsAvailable().size());
			}
			return used;
		}
	}
}
