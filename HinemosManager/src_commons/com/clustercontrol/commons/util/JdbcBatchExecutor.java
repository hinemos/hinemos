/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.HinemosTime;

/**
 * JDBCドライバを用いて、高速にinsertまたはupdateの一括処理を行うクラス
 */
public class JdbcBatchExecutor {
	private static final Log log = LogFactory.getLog(JdbcBatchExecutor.class);

	public static void execute(JdbcBatchQuery query) {
		List<JdbcBatchQuery> list = new ArrayList<JdbcBatchQuery>();
		list.add(query);
		execute(list);
	}

	/**
	 * クエリを実行する
	 * 
	 * @param query
	 *            insertまたはupdate
	 */
	public static void execute(List<JdbcBatchQuery> queryList) {
		Connection conn = null;
		long start = HinemosTime.currentTimeMillis();
		JpaTransactionManager tm = null;
		PreparedStatement pstmt = null;
		try {
			tm = new JpaTransactionManager();

			// 実行前にJPAの内容をDBに反映させるためflushする。
			tm.flush();

			conn = tm.getEntityManager().unwrap(java.sql.Connection.class);
			conn.setAutoCommit(false);
			for (JdbcBatchQuery query : queryList){
				pstmt = conn.prepareStatement(query.getSql());
				query.addBatch(pstmt);
				pstmt.executeBatch();
			}
			if (!tm.isNestedEm()) {
				conn.commit();
			}
		} catch (Exception e) {
			if (e instanceof BatchUpdateException) {
				BatchUpdateException bue = (BatchUpdateException)e;
				log.warn("BatchUpdateException:" + bue, bue);
				SQLException sqe = bue.getNextException();
				if (sqe != null) {
					log.warn("SQLException: " + sqe, sqe);
				}
			} else {
				log.warn(e);
			}
			if (conn != null) {
				tm.getEntityManager().notifyUpdateError(e);
				try {
					conn.rollback();
				} catch (SQLException e1) {
					log.warn(e1, e1);
				}
			}
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					log.warn("SQLException: " + e, e);
				}
			}
			if (tm != null) {
				tm.flush();
				tm.close();
			}
		}
		long time = HinemosTime.currentTimeMillis() - start;
		String className = "";
		if (queryList.size() != 0) {
			className = queryList.get(0).getClass().getSimpleName();
		}
		String sizeStr = "";
		for (JdbcBatchQuery query : queryList) {
			if (0 < sizeStr.length()) {
				sizeStr += ",";
			}
			sizeStr += query.getSize();
		}
		String message = String.format("Execute [%s] batch: %dms. size=%s", className, time, sizeStr);
		if (time > 3000) {
			log.warn(message);
		} else if (time > 1000) {
			log.info(message);
		} else {
			log.debug(message);
		}
	}
}
