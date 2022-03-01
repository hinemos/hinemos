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
import com.clustercontrol.jobmanagement.model.JobLinkMessageEntity;

/**
 * ジョブ連携メッセージ登録
 *
 */
public class JobLinkMessageJdbcBatchInsert extends JdbcBatchQuery {

	private static final String SQL = "INSERT INTO log.cc_job_link_message"
			+ "(joblink_message_id, facility_id, send_date, facility_name, ip_address, "
			+ "accept_date, monitor_detail_id, application, priority, message, message_org) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";

	private List<JobLinkMessageEntity> entities = null;

	public JobLinkMessageJdbcBatchInsert(List<JobLinkMessageEntity> entities) {
		this.entities = entities;
	}

	@Override
	public String getSql() {
		return SQL;
	}

	@Override
	public void addBatch(PreparedStatement pstmt) throws SQLException {
		for (JobLinkMessageEntity entity : entities) {
			size++;
			Object[] params = new Object[] { entity.getId().getJoblinkMessageId(), entity.getId().getFacilityId(),
					entity.getId().getSendDate(), entity.getFacilityName(), entity.getIpAddress(),
					entity.getAcceptDate(), entity.getMonitorDetailId(), entity.getApplication(), entity.getPriority(),
					entity.getMessage(), entity.getMessageOrg() };
			setParameters(pstmt, params);
			pstmt.addBatch();
		}
	}
}
