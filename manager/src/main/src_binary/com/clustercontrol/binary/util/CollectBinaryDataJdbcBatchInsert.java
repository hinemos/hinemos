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

import com.clustercontrol.binary.model.CollectBinaryData;
import com.clustercontrol.commons.util.JdbcBatchQuery;
import com.clustercontrol.hub.model.CollectStringDataPK;

public class CollectBinaryDataJdbcBatchInsert extends JdbcBatchQuery {
	private static final String SQL = "INSERT INTO log.cc_collect_data_binary"
			+ "(collect_id, data_id, collect_type, file_position, file_key, record_key, file_head_size, time, value) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private List<CollectBinaryData> entities = null;

	public CollectBinaryDataJdbcBatchInsert(List<CollectBinaryData> entities) {
		this.entities = entities;
	}
	
	@Override
	public String getSql() {
		return SQL;
	}

	@Override
	public void addBatch(PreparedStatement pstmt) throws SQLException {
		for (CollectBinaryData entity : entities) {
			size++;
			CollectStringDataPK pk = entity.getId();
			Object[] params = new Object[] {
					pk.getCollectId(),
					pk.getDataId(),
					entity.getCollectType(),
					entity.getFilePosition(),
					entity.getFileKey(),
					entity.getRecordKey(),
					entity.getFileHeadSize(),
					entity.getTime(),
					entity.getValue(),
			};
			setParameters(pstmt, params);
			pstmt.addBatch();
		}
	}
}
