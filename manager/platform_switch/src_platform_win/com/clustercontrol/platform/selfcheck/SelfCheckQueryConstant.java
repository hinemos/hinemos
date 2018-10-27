/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform.selfcheck;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 環境差分のある固定値を定数として格納するクラス（Windows）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class SelfCheckQueryConstant {

	private static Log logger = LogFactory.getLog( SelfCheckQueryConstant.class );

	private static final String VALIDATION_QUERY = "SELECT CAST((DATEDIFF (MS, start_time, GETDATE()) / 1000.0) AS DECIMAL(38,3)),"
			+ " 'query = [' + sql.text + '], start_time = ' + FORMAT(start_time, 'yyyy/MM/dd HH:mm:ss') + ', duration = '"
			+ " + CAST(CAST((DATEDIFF (MS, start_time, GETDATE()) / 1000.0) AS DECIMAL(38,3)) as VARCHAR) + ' sec'"
			+ " FROM sys.dm_exec_requests req"
			+ " CROSS APPLY sys.dm_exec_sql_text(req.sql_handle) sql"
			+ " WHERE DATEDIFF (MS ,start_time, GETDATE()) > (%d * 1000)";

	public static String validationQuery(){
		logger.debug("validationQuery(Windows) : " + VALIDATION_QUERY);
		return VALIDATION_QUERY;
	}
}