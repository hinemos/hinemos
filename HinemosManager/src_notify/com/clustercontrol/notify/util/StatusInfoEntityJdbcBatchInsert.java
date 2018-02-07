/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.clustercontrol.commons.util.JdbcBatchQuery;
import com.clustercontrol.notify.monitor.model.StatusInfoEntity;
import com.clustercontrol.notify.monitor.model.StatusInfoEntityPK;

/**
 * StatusInfoEntityにマッピングするテーブルにデータを登録するクラス
 */
public class StatusInfoEntityJdbcBatchInsert extends JdbcBatchQuery {
	private static final String SQL =  "insert into log.cc_status_info ("
			+ "facility_id, monitor_id, monitor_detail_id, plugin_id, application, "
			+ "message, priority, generation_date, output_date, "
			+ "expiration_flg, expiration_date, owner_role_id) "
			+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private List<StatusInfoEntity> entities = null;

	public StatusInfoEntityJdbcBatchInsert(List<StatusInfoEntity> entities) {
		this.entities = entities;
	}

	@Override
	public String getSql() {
		return SQL;
	}

	@Override
	public void addBatch(PreparedStatement pstmt) throws SQLException {
		for (StatusInfoEntity entity : entities) {
			size++;
			StatusInfoEntityPK pk = entity.getId();
			Object[] params = new Object[] {
					pk.getFacilityId(),
					pk.getMonitorId(),
					pk.getMonitorDetailId(),
					pk.getPluginId(),
					entity.getApplication(),
					entity.getMessage(),
					entity.getPriority(),
					entity.getGenerationDate(),
					entity.getOutputDate(),
					entity.getExpirationFlg(),
					entity.getExpirationDate(),
					entity.getOwnerRoleId()
			};
			setParameters(pstmt, params);
			pstmt.addBatch();
		}
	}
}
