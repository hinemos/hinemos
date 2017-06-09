/*

Copyright (C) 2017 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.platform.selfcheck;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

/**
 * TableSizeMonitorクラスの環境差分（rhel）<BR>
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
		long physicalSize = -1;

		// メイン処理
		try {
			tm = new JpaTransactionManager();
			tm.begin();

			em = tm.getEntityManager();

			Long row = (Long)em.createNativeQuery(query).getSingleResult();
			if (row != null) {
				physicalSize = row;
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
	
	/**
	 * 特定のテーブルのレコード数（統計情報から取得した概算値）を返すメソッド
	 * @param tableName 対象とするテーブル名（スキーマ.テーブルの形式でなくてはならない）
	 * @return レコード数
	 */
	public static long getTableCount(String tableName) {
		JpaTransactionManager tm = null;
		HinemosEntityManager em = null;
		long count = -1;

		String[] tableNamePart = tableName.split("\\.");
		if (tableNamePart.length != 2) {
			m_log.warn("invalid table name. (" + tableName + ")");
			return count;
		}
		
		String query = "SELECT n_live_tup FROM pg_stat_user_tables WHERE schemaname = '" +
				tableNamePart[0] + "' AND relname = '" + tableNamePart[1] + "'";

		// メイン処理
		try {
			tm = new JpaTransactionManager();
			tm.begin();

			em = tm.getEntityManager();

			Long row = (Long)em.createNativeQuery(query).getSingleResult();
			if (row != null) {
				count = row;
			}

			tm.commit();
		} catch (Exception e) {
			m_log.warn("database query execution failure. (" + query + ")", e);
		} finally {
			if (tm != null) {
				tm.close();
			}
		}

		return count;
	}

}
