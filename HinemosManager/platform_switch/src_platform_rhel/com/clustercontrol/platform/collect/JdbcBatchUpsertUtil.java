/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform.collect;

import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectDataPK;
import com.clustercontrol.collect.model.SummaryDay;
import com.clustercontrol.collect.model.SummaryHour;
import com.clustercontrol.collect.model.SummaryMonth;

/**
 * 環境差分のあるHinemosPropertyのデフォルト値を定数として格納するクラス（rhel）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class JdbcBatchUpsertUtil {

	private static final String SQL_BASE = "INSERT INTO %s"
			+ "(collector_id, time, avg, min, max, count, average_avg, average_count, standard_deviation_avg, standard_deviation_count) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT ON CONSTRAINT %s "
			+ "DO UPDATE SET avg = ?, min=?, max=?, count=?, average_avg=?, average_count=?, standard_deviation_avg=?, standard_deviation_count=?";

	public static final String COLLECT_DATA_SQL_BASE = "INSERT INTO log.cc_collect_data_raw"
			+ "(collector_id, time, value, average, standard_deviation) "
			+ "VALUES (?, ?, ?, ?, ?) ON CONFLICT ON CONSTRAINT p_key_cc_collect_data_raw "
			+ "DO UPDATE SET value = ?, average=?, standard_deviation=?";
	public static final String COLLECT_DATA_SQL;
	public static final String SUMMARY_DAY_SQL;
	public static final String SUMMARY_HOUR_SQL;
	public static final String SUMMARY_MONTH_SQL;

	static {
		COLLECT_DATA_SQL = COLLECT_DATA_SQL_BASE;
		SUMMARY_DAY_SQL = String.format(SQL_BASE, "log.cc_collect_summary_day", "p_key_cc_collect_summary_day");
		SUMMARY_HOUR_SQL = String.format(SQL_BASE, "log.cc_collect_summary_hour", "p_key_cc_collect_summary_hour");
		SUMMARY_MONTH_SQL = String.format(SQL_BASE, "log.cc_collect_summary_month", "p_key_cc_collect_summary_month");
	}

	public static Object[] getParameters(CollectDataPK pk, CollectData entity) {
		Object[] params = new Object[] {
				pk.getCollectorid(),
				pk.getTime(),
				entity.getValue(),
				entity.getAverage(),
				entity.getStandardDeviation(),
				entity.getValue(),
				entity.getAverage(),
				entity.getStandardDeviation()
		};
		return params;
	}

	public static Object[] getParameters(CollectDataPK pk, SummaryDay entity) {
		Object[] params = new Object[] {
				pk.getCollectorid(),
				pk.getTime(),
				entity.getAvg(),
				entity.getMin(),
				entity.getMax(),
				entity.getCount(),
				entity.getAverageAvg(),
				entity.getAverageCount(),
				entity.getStandardDeviationAvg(),
				entity.getStandardDeviationCount(),
				entity.getAvg(),
				entity.getMin(),
				entity.getMax(),
				entity.getCount(),
				entity.getAverageAvg(),
				entity.getAverageCount(),
				entity.getStandardDeviationAvg(),
				entity.getStandardDeviationCount()
		};
		return params;
	}

	public static Object[] getParameters(CollectDataPK pk, SummaryHour entity) {
		Object[] params = new Object[] {
				pk.getCollectorid(),
				pk.getTime(),
				entity.getAvg(),
				entity.getMin(),
				entity.getMax(),
				entity.getCount(),
				entity.getAverageAvg(),
				entity.getAverageCount(),
				entity.getStandardDeviationAvg(),
				entity.getStandardDeviationCount(),
				entity.getAvg(),
				entity.getMin(),
				entity.getMax(),
				entity.getCount(),
				entity.getAverageAvg(),
				entity.getAverageCount(),
				entity.getStandardDeviationAvg(),
				entity.getStandardDeviationCount()
		};
		return params;
	}

	public static Object[] getParameters(CollectDataPK pk, SummaryMonth entity) {
		Object[] params = new Object[] {
				pk.getCollectorid(),
				pk.getTime(),
				entity.getAvg(),
				entity.getMin(),
				entity.getMax(),
				entity.getCount(),
				entity.getAverageAvg(),
				entity.getAverageCount(),
				entity.getStandardDeviationAvg(),
				entity.getStandardDeviationCount(),
				entity.getAvg(),
				entity.getMin(),
				entity.getMax(),
				entity.getCount(),
				entity.getAverageAvg(),
				entity.getAverageCount(),
				entity.getStandardDeviationAvg(),
				entity.getStandardDeviationCount()
		};
		return params;
	}
}