/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform;

import javax.persistence.PersistenceException;

import com.microsoft.sqlserver.jdbc.SQLServerException;

/**
 * 環境差分のあるクエリに関数する操作を格納するクラス（Windows）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class QueryDivergence {

	/**
	 * SQL Server エラーコードタイムアウト
	 * タイムアウト発生時に通知されるエラーコード
	 */
	private static final String ERROR_TIMEOUT = "-2";
	
	/**
	 * COUNTの結果の型の差分を吸収するためのメソッド。<br>
	 * Rhel：Long<br>
	 * Windowns：Integer<br>
	 * 
	 * @param result
	 * @return
	 */
	public static Long countResult(Object result) {
		Long countResult = null;
		if (result != null) {
			countResult = ((Integer)result).longValue();
		}
		return countResult;
	}
	
	public static boolean isQueryTimeout(PersistenceException e) {
		if (e.getCause().getCause() instanceof SQLServerException){
			SQLServerException sqlServerException = (SQLServerException) e.getCause().getCause();
			if (ERROR_TIMEOUT.equals(sqlServerException.getSQLState())){
				return true;
			}
		}
		return false;
	}
	
}