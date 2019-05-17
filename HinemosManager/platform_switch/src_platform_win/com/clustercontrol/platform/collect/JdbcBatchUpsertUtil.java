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
 * 環境差分のあるHinemosPropertyのデフォルト値を定数として格納するクラス（Windows）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class JdbcBatchUpsertUtil {

	// "WITH (UPDLOCK)"を付けないと同時に実行された場合デッドロックになる可能性があるため注意
	// https://blogs.msdn.microsoft.com/dbrowne/2013/02/25/why-is-tsql-merge-failing-with-a-primary-key-violation-isnt-it-atomic/
	private static final String SQL_BASE = "MERGE INTO %s WITH (UPDLOCK) AS A"
			+ " USING (SELECT ? AS collector_id, ? AS time, ? AS avg, ? AS min, ? AS max,"
			+ " ? AS count, ? AS average_avg, ? AS average_count, ? AS standard_deviation_avg, ? AS standard_deviation_count) AS B"
			+ " ON (A.collector_id = B.collector_id AND A.time = B.time)"
			+ " WHEN MATCHED THEN UPDATE SET avg = B.avg, min = B.min, max = B.max,"
			+ " count = B.count, average_avg = B.average_avg, average_count = B.average_count, standard_deviation_avg = B.standard_deviation_avg, standard_deviation_count = B.standard_deviation_count"
			+ " WHEN NOT MATCHED THEN INSERT (collector_id, time, avg, min, max, count, average_avg, average_count, standard_deviation_avg, standard_deviation_count)"
			+ " VALUES (B.collector_id, B.time, B.avg, B.min, B.max, B.count, B.average_avg, B.average_count, B.standard_deviation_avg, B.standard_deviation_count);";

	public static final String COLLECT_DATA_SQL_BASE = "MERGE INTO log.cc_collect_data_raw WITH (UPDLOCK) AS A"
			+ " USING (SELECT ? AS collector_id, ? AS time, ? AS value, ? AS average, ? AS standard_deviation) AS B"
			+ " ON (A.collector_id = B.collector_id AND A.time = B.time)"
			+ " WHEN MATCHED THEN UPDATE SET value = B.value, average = B.average, standard_deviation = B.standard_deviation"
			+ " WHEN NOT MATCHED THEN INSERT (collector_id, time, value, average, standard_deviation)"
			+ " VALUES (B.collector_id, B.time, B.value, B.average, B.standard_deviation);";
	public static final String COLLECT_DATA_SQL;
	public static final String SUMMARY_DAY_SQL;
	public static final String SUMMARY_HOUR_SQL;
	public static final String SUMMARY_MONTH_SQL;

	static {
		COLLECT_DATA_SQL = COLLECT_DATA_SQL_BASE;
		SUMMARY_DAY_SQL = String.format(SQL_BASE, "log.cc_collect_summary_day");
		SUMMARY_HOUR_SQL = String.format(SQL_BASE, "log.cc_collect_summary_hour");
		SUMMARY_MONTH_SQL = String.format(SQL_BASE, "log.cc_collect_summary_month");
	}


	public static Object[] getParameters(CollectDataPK pk, CollectData entity) {
		Object[] params = new Object[] {
				pk.getCollectorid(),
				pk.getTime(),
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
				entity.getStandardDeviationCount()
		};
		return params;
	}
}