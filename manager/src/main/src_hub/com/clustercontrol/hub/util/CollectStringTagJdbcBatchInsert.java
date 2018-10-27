/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.clustercontrol.commons.util.JdbcBatchQuery;
import com.clustercontrol.hub.model.CollectDataTag;

public class CollectStringTagJdbcBatchInsert extends JdbcBatchQuery {

	private static final String SQL = "INSERT INTO log.cc_collect_data_tag"
			+ "(collect_id, data_id, tag_key, type, tag_value) "
			+ "VALUES (?, ?, ?, ?, ?)";
	
	private List<CollectDataTag> entities = null;

	public CollectStringTagJdbcBatchInsert(List<CollectDataTag> entities) {
		this.entities = entities;
	}
	
	@Override
	public String getSql() {
		return SQL;
	}

	@Override
	public void addBatch(PreparedStatement pstmt) throws SQLException {
		for (CollectDataTag entity : entities) {
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
