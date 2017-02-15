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
