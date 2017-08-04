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

import com.clustercontrol.HinemosManagerMain.StartupMode;
import com.clustercontrol.selfcheck.monitor.SelfCheckMonitor;

/**
 * 環境差分のある固定値を格納するクラス（rhel）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class SelfCheckPertial {

	private static Log logger = LogFactory.getLog( SelfCheckPertial.class );

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