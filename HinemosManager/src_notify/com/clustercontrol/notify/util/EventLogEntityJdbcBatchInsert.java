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
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.model.EventLogEntityPK;

/**
 * EventLogEntityにマッピングするテーブルにデータを登録するクラス
 */
public class EventLogEntityJdbcBatchInsert extends JdbcBatchQuery {
	private static final String SQL =  "insert into log.cc_event_log ("
			+ "monitor_id, monitor_detail_id, plugin_id, generation_date, facility_id, "
			+ "scope_text, application, message, message_org, priority, "
			+ "confirm_flg, confirm_user, duplication_count, output_date, "
			+ "inhibited_flg, comment_user, comment, collect_graph_flg, owner_role_id) "
			+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private List<EventLogEntity> entities = null;

	public EventLogEntityJdbcBatchInsert(List<EventLogEntity> entities) {
		this.entities = entities;
	}

	@Override
	public String getSql() {
		return SQL;
	}

	@Override
	public void addBatch(PreparedStatement pstmt) throws SQLException {
		for (EventLogEntity entity : entities) {
			size++;
			EventLogEntityPK pk = entity.getId();
			Object[] params = new Object[] {
					pk.getMonitorId(),
					pk.getMonitorDetailId(),
					pk.getPluginId(),
					entity.getGenerationDate(),
					pk.getFacilityId(),
					entity.getScopeText(),
					entity.getApplication(),
					OutputEvent.getNotifyEventMessageMaxString(entity.getMessage()),
					OutputEvent.getNotifyEventMessageOrgMaxString(entity.getMessageOrg()),
					entity.getPriority(),
					entity.getConfirmFlg(),
					entity.getConfirmUser(),
					entity.getDuplicationCount(),
					pk.getOutputDate(),
					entity.getInhibitedFlg(),
					entity.getCommentUser(),
					entity.getComment(),
					entity.getCollectGraphFlg(),
					entity.getOwnerRoleId()
			};
			setParameters(pstmt, params);
			pstmt.addBatch();
		}
	}
}
