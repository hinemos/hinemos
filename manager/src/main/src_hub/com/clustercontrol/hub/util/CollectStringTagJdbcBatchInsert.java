/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
