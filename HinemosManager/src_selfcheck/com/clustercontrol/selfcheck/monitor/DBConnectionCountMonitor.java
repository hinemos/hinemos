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

package com.clustercontrol.selfcheck.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.persistence.sessions.server.ConnectionPool;
import org.eclipse.persistence.sessions.server.ServerSession;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * データベースのコネクション数を確認する処理の実装クラス
 */
public class DBConnectionCountMonitor extends SelfCheckMonitorBase {

	private static Log m_log = LogFactory.getLog( DBConnectionCountMonitor.class );

	public final String monitorId = "SYS_DB_CONNECT_COUNT";
	public final String subKey = "";
	public final String application = "SELFCHECK (DB Connection Count)";
	
	private static final String DB_CONNECTION_COUNT_THRESHOLD = "selfcheck.monitoring.db.connection.count.threshold";
	private static final String DB_CONNECTION_COUNT = "selfcheck.monitoring.db.connection.count";

	/**
	 * セルフチェック処理名
	 */
	@Override
	public String toString() {
		int threshold = HinemosPropertyUtil.getHinemosPropertyNum(DB_CONNECTION_COUNT_THRESHOLD, Long.valueOf(80)).intValue();
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
		if(!HinemosPropertyUtil.getHinemosPropertyBool(DB_CONNECTION_COUNT, true)) {
			m_log.debug("skip");
			return;
		}
		
		/** メイン処理 */
		if (m_log.isDebugEnabled()) m_log.debug("monitoring the number of database connection.");

		/** ローカル変数 */
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		ServerSession ss = em.unwrap(ServerSession.class);
		
		for (ConnectionPool pool : ss.getConnectionPools().values()) {
			int threshold = pool.getMaxNumberOfConnections() *
					HinemosPropertyUtil.getHinemosPropertyNum(DB_CONNECTION_COUNT_THRESHOLD, Long.valueOf(80)).intValue()
					/ 100;
			boolean warn = true;
			
			// プール毎のデータベースのセッションコネクション数を取得する
			int count = (pool.getTotalNumberOfConnections() - pool.getConnectionsAvailable().size());
			
			if (m_log.isDebugEnabled()) {
				m_log.debug("total=" + pool.getTotalNumberOfConnections() +
						", max=" + pool.getMaxNumberOfConnections() +
						", available=" + pool.getConnectionsAvailable().size() +
						", threshold(%)=" + HinemosPropertyUtil.getHinemosPropertyNum(DB_CONNECTION_COUNT_THRESHOLD, Long.valueOf(80)).intValue() +
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
			AplLogger.put(PriorityConstant.TYPE_WARNING, PLUGIN_ID, MessageConstant.MESSAGE_SYS_021_SYS_SFC, msgAttr1,
					"the number of database connection is too many (" +
							count +
							" > threshold " +
							threshold +
					").");
		}

		return;
	}
	
	/**
	 * データベースのコネクション数を返す<br/>
	 * @return データベースのコネクション数
	 */
	public static int getDBConnectionCount() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		ServerSession ss = em.unwrap(ServerSession.class);
		
		int used = 0;
		for (ConnectionPool pool : ss.getConnectionPools().values()) {
			used += (pool.getTotalNumberOfConnections() - pool.getConnectionsAvailable().size());
		}
		return used;
	}
}
