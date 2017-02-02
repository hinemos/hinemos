/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
