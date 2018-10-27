/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform.hub;

/**
 * 環境差分のある固定値を格納するクラス（rhel）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class HubQueryDivergence {

	private static final String SEQUENCE_SQL = "SELECT last_value, max_value FROM %s";
	
	private static final String SEQUENCE_MIN_POS_SQL = "SELECT MIN(t0.position) FROM %s t0 WHERE t0.position > (SELECT last_value FROM %s) AND t0.position <= (SELECT max_value FROM %s)";

	public static String getSequenceSql() {
		return SEQUENCE_SQL;
	}
	
	public static String getSequenceMinPosSql() {
		return SEQUENCE_MIN_POS_SQL;
	}
	
	public static String getSequenceTableNameEventLog() {
		return "log.cc_event_log_position_seq";
	}
	
	public static String getSequenceTableNameJobSession() {
		return "log.cc_job_session_position_seq";
	}
	
	public static String getSequenceTableNameCollectDataRaw() {
		return "log.cc_collect_data_raw_position_seq";
	}
}