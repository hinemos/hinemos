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
			+ "inhibited_flg, comment_user, comment, collect_graph_flg, owner_role_id, "
			+ "user_item01, user_item02, user_item03, user_item04, user_item05,"
			+ "user_item06, user_item07, user_item08, user_item09, user_item10,"
			+ "user_item11, user_item12, user_item13, user_item14, user_item15,"
			+ "user_item16, user_item17, user_item18, user_item19, user_item20,"
			+ "user_item21, user_item22, user_item23, user_item24, user_item25,"
			+ "user_item26, user_item27, user_item28, user_item29, user_item30,"
			+ "user_item31, user_item32, user_item33, user_item34, user_item35,"
			+ "user_item36, user_item37, user_item38, user_item39, user_item40"
			+ ") "
			+ "values ("
			+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
			+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
			+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
			+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
			+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?"
			+ ")";

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
					entity.getOwnerRoleId(),
					entity.getUserItem01(),
					entity.getUserItem02(),
					entity.getUserItem03(),
					entity.getUserItem04(),
					entity.getUserItem05(),
					entity.getUserItem06(),
					entity.getUserItem07(),
					entity.getUserItem08(),
					entity.getUserItem09(),
					entity.getUserItem10(),
					entity.getUserItem11(),
					entity.getUserItem12(),
					entity.getUserItem13(),
					entity.getUserItem14(),
					entity.getUserItem15(),
					entity.getUserItem16(),
					entity.getUserItem17(),
					entity.getUserItem18(),
					entity.getUserItem19(),
					entity.getUserItem20(),
					entity.getUserItem21(),
					entity.getUserItem22(),
					entity.getUserItem23(),
					entity.getUserItem24(),
					entity.getUserItem25(),
					entity.getUserItem26(),
					entity.getUserItem27(),
					entity.getUserItem28(),
					entity.getUserItem29(),
					entity.getUserItem30(),
					entity.getUserItem31(),
					entity.getUserItem32(),
					entity.getUserItem33(),
					entity.getUserItem34(),
					entity.getUserItem35(),
					entity.getUserItem36(),
					entity.getUserItem37(),
					entity.getUserItem38(),
					entity.getUserItem39(),
					entity.getUserItem40(),
			};
			setParameters(pstmt, params);
			pstmt.addBatch();
		}
	}
}
