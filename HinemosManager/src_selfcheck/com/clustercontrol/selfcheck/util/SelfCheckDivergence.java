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

import com.clustercontrol.HinemosManagerMain.StartupMode;
import com.clustercontrol.selfcheck.monitor.SelfCheckMonitor;

/**
 * 固定値を格納するクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class SelfCheckDivergence {

	private static Log logger = LogFactory.getLog( SelfCheckDivergence.class );

	private static final String DB_LONG_TRAN_VALIDATION_QUERY = "SELECT EXTRACT(EPOCH FROM (current_timestamp-xact_start)::interval(3)), 'query = [' || query || '], start_time = ' || TO_CHAR(xact_start,'yyyy/mm/dd hh24:mi:ss') || '"
			+ ", duration = ' || EXTRACT(EPOCH FROM (current_timestamp-xact_start)::interval(3)) || ' sec' FROM pg_stat_activity"
			+ " WHERE (current_timestamp-xact_start)::interval>'%d second'::interval";

	public static String getDbLongTranValidationQuery(){
		logger.debug("db longtran validationQuery(Linux) : " + DB_LONG_TRAN_VALIDATION_QUERY);
		return DB_LONG_TRAN_VALIDATION_QUERY;
	}
	
	public static SelfCheckMonitor[] getMonitors(StartupMode startupMode) {
		return new SelfCheckMonitor[]{};
	}
}