/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
