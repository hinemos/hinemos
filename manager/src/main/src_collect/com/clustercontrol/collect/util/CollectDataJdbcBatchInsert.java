/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.clustercontrol.collect.model.CollectDataPK;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.commons.util.JdbcBatchQuery;

/**
 *  SummaryDayにマッピングするテーブルにデータを登録するクラス
 */
public class CollectDataJdbcBatchInsert extends JdbcBatchQuery {
	private static final String SQL = "INSERT INTO log.cc_collect_data_raw"
			+ "(collector_id, time, value, average, standard_deviation) "
			+ "VALUES (?, ?, ?, ?, ?)";

	private List<CollectData> entities = null;

	public CollectDataJdbcBatchInsert(List<CollectData> entities) {
		this.entities = entities;
	}

	@Override
	public String getSql() {
		return SQL;
	}

	@Override
	public void addBatch(PreparedStatement pstmt) throws SQLException {
		for (CollectData entity : entities) {
			size++;
			CollectDataPK pk = entity.getId();
			Object[] params = new Object[] {
					pk.getCollectorid(),
					pk.getTime(),
					entity.getValue(),
					entity.getAverage(),
					entity.getStandardDeviation()
			};
			setParameters(pstmt, params);
			pstmt.addBatch();
		}
	}
}