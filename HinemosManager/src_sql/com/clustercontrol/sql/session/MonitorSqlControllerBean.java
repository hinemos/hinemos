/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.sql.session;

import java.util.ArrayList;

import com.clustercontrol.sql.bean.JdbcDriverInfo;
import com.clustercontrol.sql.util.JdbcDriverUtil;

/**
 * SQL監視を制御するSession Bean <BR>
 * 
 */
public class MonitorSqlControllerBean {

	/**
	 * JDBC定義一覧をリストで返却します。<BR>
	 * 
	 * @return JDBC定義のリスト
	 */
	public ArrayList<JdbcDriverInfo> getJdbcDriverList(){
		JdbcDriverUtil util = new JdbcDriverUtil();
		return util.getJdbcDriver();
	}
}
