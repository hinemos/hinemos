/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform;

import javax.persistence.PersistenceException;

import org.postgresql.util.PSQLException;

/**
 * 環境差分のあるクエリに関数する操作を格納するクラス（rhel）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class QueryDivergence {

	/**
	 * PostgreSQL エラーコードタイムアウト
	 * タイムアウト発生時に通知されるエラーコード
	 */
	private static final String ERROR_TIMEOUT = "57014";
	
	/**
	 * COUNTの結果の型の差分を吸収するためのメソッド。<br>
	 * Rhel：Long<br>
	 * Windowns：Integer<br>
	 * 
	 * @param result
	 * @return
	 */
	public static Long countResult(Object result) {
		return (Long)result;
	}
	
	public static boolean isQueryTimeout(PersistenceException e) {
		if (e.getCause().getCause() instanceof PSQLException){
			PSQLException pSQLException = (PSQLException) e.getCause().getCause();
			if (ERROR_TIMEOUT.equals(pSQLException.getSQLState())){
				return true;
			}
		}
		return false;
	}
	
}