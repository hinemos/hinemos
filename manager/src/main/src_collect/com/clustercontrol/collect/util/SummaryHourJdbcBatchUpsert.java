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
import com.clustercontrol.collect.model.SummaryHour;
import com.clustercontrol.commons.util.JdbcBatchQuery;
import com.clustercontrol.platform.collect.JdbcBatchUpsertUtil;

/**
 *  SummaryHourにマッピングするテーブルにデータを登録するクラス
 */
public class SummaryHourJdbcBatchUpsert extends JdbcBatchQuery {
	private static final String SQL = JdbcBatchUpsertUtil.SUMMARY_HOUR_SQL;
	
	private List<SummaryHour> entities = null;

	public SummaryHourJdbcBatchUpsert(List<SummaryHour> entities) {
		this.entities = entities;
	}

	@Override
	public String getSql() {
		return SQL;
	}

	@Override
	public void addBatch(PreparedStatement pstmt) throws SQLException {
		for (SummaryHour entity : entities) {
			size++;
			CollectDataPK pk = entity.getId();
			Object[] params = JdbcBatchUpsertUtil.getParameters(pk, entity);
			setParameters(pstmt, params);
			pstmt.addBatch();
		}
	}
}
