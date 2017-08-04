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

package com.clustercontrol.platform;

import javax.persistence.PersistenceException;

import com.microsoft.sqlserver.jdbc.SQLServerException;

/**
 * 環境差分のあるクエリに関数する操作を格納するクラス（Windows）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class QueryPertial {

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