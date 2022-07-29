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

import com.clustercontrol.commons.util.JdbcBatchQuery;
import com.clustercontrol.jobmanagement.model.JobLinkMessageExpInfoEntity;

/**
 * ジョブ連携メッセージ拡張情報登録
 *
 */
public class JobLinkMessageExpInfoJdbcBatchInsert extends JdbcBatchQuery {

	private static final String SQL = "INSERT INTO log.cc_job_link_message_exp_info"
			+ "(joblink_message_id, facility_id, send_date, key, value) "
			+ "VALUES (?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";

	private List<JobLinkMessageExpInfoEntity> entities = null;

	public JobLinkMessageExpInfoJdbcBatchInsert(List<JobLinkMessageExpInfoEntity> entities) {
		this.entities = entities;
	}

	@Override
	public String getSql() {
		return SQL;
	}

	@Override
	public void addBatch(PreparedStatement pstmt) throws SQLException {
		for (JobLinkMessageExpInfoEntity entity : entities) {
			size++;
			Object[] params = new Object[] { entity.getId().getJoblinkMessageId(), entity.getId().getFacilityId(),
					entity.getId().getSendDate(), entity.getId().getKey(), entity.getValue() };
			setParameters(pstmt, params);
			pstmt.addBatch();
		}
	}
}
