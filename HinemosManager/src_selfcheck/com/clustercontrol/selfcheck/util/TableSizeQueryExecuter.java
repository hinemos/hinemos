/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidSetting;

/**
 * TableSizeMonitorクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class TableSizeQueryExecuter {
	private static Log m_log = LogFactory.getLog( TableSizeQueryExecuter.class );
	
	/**
	 * 特定のテーブルの物理サイズを返すメソッド
	 * @param tableName 対象とするテーブル名
	 * @return 物理サイズ(byte)
	 */
	public static long getTableSize(String tableName) {
		// ローカル変数
		JpaTransactionManager tm = null;
		HinemosEntityManager em = null;

		String query = "SELECT pg_total_relation_size('" + tableName + "') as size"; // byte

		// メイン処理
		try {
			tm = new JpaTransactionManager();
			tm.begin();

			em = tm.getEntityManager();

			long physicalSize = (Long)em.createNativeQuery(query).getSingleResult();

			tm.commit();
			return physicalSize;
		} catch (Exception e) {
			m_log.warn("database query execution failure. (" + query + ")", e);
			throw e;
		} finally {
			if (tm != null) {
				tm.close();
			}
		}
	}
	
	/**
	 * 特定のテーブルのレコード数（統計情報から取得した概算値）を返すメソッド
	 * @param tableName 対象とするテーブル名（スキーマ.テーブルの形式でなくてはならない）
	 * @return レコード数
	 * @throws InvalidSetting 
	 */
	public static long getTableCount(String tableName) throws InvalidSetting {
		JpaTransactionManager tm = null;
		HinemosEntityManager em = null;

		String[] tableNamePart = tableName.split("\\.");
		if (tableNamePart.length != 2) {
			String message = "invalid table name. (" + tableName + ")";
			m_log.warn(message);
			throw new InvalidSetting(message);
		}
		
		String query = "SELECT n_live_tup FROM pg_stat_user_tables WHERE schemaname = '" +
				tableNamePart[0] + "' AND relname = '" + tableNamePart[1] + "'";

		// メイン処理
		try {
			tm = new JpaTransactionManager();
			tm.begin();

			em = tm.getEntityManager();

			long count = (Long)em.createNativeQuery(query).getSingleResult();

			tm.commit();
			return count;
		} catch (Exception e) {
			m_log.warn("database query execution failure. (" + query + ")", e);
			throw e;
		} finally {
			if (tm != null) {
				tm.close();
			}
		}
	}

}
