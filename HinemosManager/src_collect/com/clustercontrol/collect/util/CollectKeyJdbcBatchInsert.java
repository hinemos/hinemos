package com.clustercontrol.collect.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.commons.util.JdbcBatchQuery;

/**
 * CollectDataにマッピングするテーブルにデータを登録するクラス
 */
public class CollectKeyJdbcBatchInsert extends JdbcBatchQuery {
	private static final String SQL = "Insert into log.cc_collect_key "
			+ "(item_name, display_name, monitor_id, facility_id, collectorid) "
			+ "VALUES (?, ?, ?, ?, ?)";

	private CollectKeyInfo entity = null;

	public CollectKeyJdbcBatchInsert(CollectKeyInfo entity) {
		this.entity = entity;
	}

	@Override
	public String getSql() {
		return SQL;
	}

	@Override
	public void addBatch(PreparedStatement pstmt) throws SQLException {
			Object[] params = new Object[] {
				entity.getItemName(),
				entity.getDisplayName(),
				entity.getMonitorId(),
				entity.getFacilityid(),
				entity.getCollectorid()
			};
			setParameters(pstmt, params);
			pstmt.addBatch();
	}
}