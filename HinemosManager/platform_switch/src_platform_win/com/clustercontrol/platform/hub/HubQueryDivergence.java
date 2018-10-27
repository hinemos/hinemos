/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform.hub;

/**
 * 環境差分のある固定値を格納するクラス（Windows）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class HubQueryDivergence {
	
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