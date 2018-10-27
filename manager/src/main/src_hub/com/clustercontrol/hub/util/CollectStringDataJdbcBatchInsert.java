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
import com.clustercontrol.hub.model.CollectStringData;
import com.clustercontrol.hub.model.CollectStringDataPK;

public class CollectStringDataJdbcBatchInsert extends JdbcBatchQuery {

	private static final String SQL = "INSERT INTO log.cc_collect_data_string"
			+ "(collect_id, data_id, time, value, log_format_id) "
			+ "VALUES (?, ?, ?, ?, ?)";
	
	private List<CollectStringData> entities = null;

	public CollectStringDataJdbcBatchInsert(List<CollectStringData> entities) {
		this.entities = entities;
	}
	
	@Override
	public String getSql() {
		return SQL;
	}

	@Override
	public void addBatch(PreparedStatement pstmt) throws SQLException {
		for (CollectStringData entity : entities) {
			size++;
			CollectStringDataPK pk = entity.getId();
			Object[] params = new Object[] {
					pk.getCollectId(),
					pk.getDataId(),
					entity.getTime(),
					entity.getValue(),
					entity.getLogformatId()
			};
			setParameters(pstmt, params);
			pstmt.addBatch();
		}
	}
}
