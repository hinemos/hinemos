/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.collect.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.clustercontrol.collect.model.CollectDataPK;
import com.clustercontrol.collect.model.SummaryDay;
import com.clustercontrol.commons.util.JdbcBatchQuery;
import com.clustercontrol.platform.collect.JdbcBatchUpsertUtil;

/**
 *  SummaryDayにマッピングするテーブルにデータを登録するクラス
 */
public class SummaryDayJdbcBatchUpsert extends JdbcBatchQuery {
	private static final String SQL = JdbcBatchUpsertUtil.SUMMARY_DAY_SQL;

	private List<SummaryDay> entities = null;

	public SummaryDayJdbcBatchUpsert(List<SummaryDay> entities) {
		this.entities = entities;
	}

	@Override
	public String getSql() {
		return SQL;
	}

	@Override
	public void addBatch(PreparedStatement pstmt) throws SQLException {
		for (SummaryDay entity : entities) {
			CollectDataPK pk = entity.getId();
			Object[] params = JdbcBatchUpsertUtil.getParameters(pk, entity);
			setParameters(pstmt, params);
			pstmt.addBatch();
		}
	}
}
