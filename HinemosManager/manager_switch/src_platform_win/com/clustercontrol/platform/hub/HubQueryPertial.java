/*

.Copyright (C) 2017 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.platform.hub;

/**
 * 環境差分のある固定値を格納するクラス（Windows）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class HubQueryPertial {
	
	private static final String BIG_INT_MAXIMUM_VALUE = "9223372036854775807";
	
	private static final String SEQUENCE_SQL = "SELECT CAST(IDENT_CURRENT('%s') AS bigint), CAST(" + BIG_INT_MAXIMUM_VALUE + "AS bigint)";

	private static final String SEQUENCE_MIN_POS_SQL = 
			"SELECT MIN(t0.position) FROM %s t0 WHERE t0.position > (SELECT IDENT_CURRENT('%s')) AND t0.position <= " + BIG_INT_MAXIMUM_VALUE;
	
	public static String getSequenceSql() {
		return SEQUENCE_SQL;
	}
	
	public static String getSequenceMinPosSql() {
		return SEQUENCE_MIN_POS_SQL;
	}
	
	public static String getSequenceTableNameEventLog() {
		return "log.cc_event_log";
	}
	
	public static String getSequenceTableNameJobSession() {
		return "log.cc_job_session";
	}
	
	public static String getSequenceTableNameCollectDataRaw() {
		return "log.cc_collect_data_raw";
	}
}