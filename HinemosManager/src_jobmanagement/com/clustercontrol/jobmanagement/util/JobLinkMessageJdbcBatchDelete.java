/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.commons.util.JdbcBatchQuery;

/**
 * ColelctDataにマッピングするテーブルからデータを削除するクラス
 */
public class JobLinkMessageJdbcBatchDelete extends JdbcBatchQuery {
	private static final String SQL = "DELETE FROM log.cc_collect_data_raw WHERE collector_id = ? "
			+ "AND time >= ? " + "AND time <= ? ";

	private List<CollectKeyInfo> entities = null;
	private Long fromTime = null;
	private Long toTime = null;

	public JobLinkMessageJdbcBatchDelete(List<CollectKeyInfo> entities, Long fromTime, Long toTime) {
		this.entities = entities;
		this.fromTime = fromTime;
		this.toTime = toTime;
	}

	@Override
	public String getSql() {
		return SQL;
	}

	@Override
	public void addBatch(PreparedStatement pstmt) throws SQLException {
		for (CollectKeyInfo entity : entities) {
			size++;
			Object[] params = new Object[] { entity.getCollectorid(), fromTime, toTime };
			setParameters(pstmt, params);
			pstmt.addBatch();
		}
	}
}