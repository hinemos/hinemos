/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

/**
 *  JdbcクエリのSQL文とパラメータを定義する基本クラス
 */
public abstract class JdbcBatchQuery {

	protected int size = 0;
	
	/**
	 * SQL文を取得
	 * 
	 * @return SQL文
	 */
	abstract public String getSql();

	/**
	 * 行いたい処理をバッチに追加
	 * 
	 * @param pstmt JDBCのPreparedStatementオブジェクト
	 * @throws SQLException
	 */
	abstract public void addBatch(PreparedStatement pstmt) throws SQLException;

	/**
	 * オブジェクトpstmtにパラメータを設定
	 * 
	 * @param pstmt JDBCのPreparedStatementオブジェクト
	 * @param params pstmtに設定するパラメータ
	 * @throws SQLException
	 */
	protected void setParameters(PreparedStatement pstmt, Object[] params) throws SQLException {
		for (int i = 0; i < params.length; i++) {
			// NaN なら、null に変換。
			if (params[i] instanceof Float && params[i].equals(Float.NaN)) {
				pstmt.setNull(i + 1, java.sql.Types.FLOAT);
			}else if (params[i] instanceof Double && params[i].equals(Double.NaN)) {
				pstmt.setNull(i + 1, java.sql.Types.DOUBLE);
			} else if (params[i] instanceof Date) {
				pstmt.setObject(i + 1, new java.sql.Timestamp(((Date)params[i]).getTime()));
			} else {
				pstmt.setObject(i + 1, params[i]);
			}
		}
	}
	
	public int getSize() {
		return size;
	}
}