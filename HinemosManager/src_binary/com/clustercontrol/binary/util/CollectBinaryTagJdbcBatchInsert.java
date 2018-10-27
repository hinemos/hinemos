/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.clustercontrol.binary.model.CollectBinaryDataTag;
import com.clustercontrol.commons.util.JdbcBatchQuery;

public class CollectBinaryTagJdbcBatchInsert extends JdbcBatchQuery {

	private static final String SQL = "INSERT INTO log.cc_collect_binary_data_tag"
			+ "(collect_id, data_id, tag_key, type, tag_value) "
			+ "VALUES (?, ?, ?, ?, ?)";
	
	private List<CollectBinaryDataTag> entities = null;

	public CollectBinaryTagJdbcBatchInsert(List<CollectBinaryDataTag> entities) {
		this.entities = entities;
	}
	
	@Override
	public String getSql() {
		return SQL;
	}

	@Override
	public void addBatch(PreparedStatement pstmt) throws SQLException {
		for (CollectBinaryDataTag entity : entities) {
			size++;
			Object[] params = new Object[] {
					entity.getCollectId(),
					entity.getDataId(),
					entity.getKey(),
					entity.getType().ordinal(),
					entity.getValue()
			};
			setParameters(pstmt, params);
			pstmt.addBatch();
		}
	}
}
