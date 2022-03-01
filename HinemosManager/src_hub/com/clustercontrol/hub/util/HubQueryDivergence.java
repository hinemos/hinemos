/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.util;

/**
 * 固定値を格納するクラス<BR>
 *
 * @version 7.0.0
 * @since 7.0.0
 */
public class HubQueryDivergence {

	private static final String SEQUENCE_SQL = "SELECT COALESCE(last_value,1), max_value FROM pg_sequences WHERE schemaname = '%s' AND sequencename = '%s'";
	
	private static final String SEQUENCE_MIN_POS_SQL = "SELECT MIN(t0.position) FROM %s t0 WHERE t0.position > (SELECT last_value FROM pg_sequences WHERE schemaname = '%s' AND sequencename = '%s') "
			+ "AND t0.position <= (SELECT max_value FROM pg_sequences WHERE schemaname = '%s' AND sequencename = '%s')";

	private static final int QUERY_WHERE_IN_PARAM_THREASHOLD = 30000;

	public static String getSequenceSql() {
		return SEQUENCE_SQL;
	}
	
	public static String getSequenceMinPosSql() {
		return SEQUENCE_MIN_POS_SQL;
	}
	
	public static int getQueryWhereInParamThreashold() {
		return QUERY_WHERE_IN_PARAM_THREASHOLD;
	}
}