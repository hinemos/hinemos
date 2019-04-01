/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.factory;


import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.EventLogNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.monitor.bean.EventLogHistoryTypeConstant;
import com.clustercontrol.monitor.run.util.EventCacheModifyCallback;
import com.clustercontrol.monitor.run.util.EventLogOperationHistoryUtil;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;

public class ModifyEventComment {

	/**
	 * 引数で指定されたイベント情報のコメントを更新します。
	 * 
	 * @param monitorId
	 * @param monitorDetailId
	 * @param pluginId
	 * @param facilityId
	 * @param outputDate
	 * @param comment
	 * @param commentDate
	 * @param commentUser
	 * @throws EventLogNotFound
	 * @throws InvalidRole
	 */

	public void modifyComment(String monitorId, String monitorDetailId, String pluginId,
			String facilityId, Long outputDate, 
			String comment, Long commentDate, String commentUser) 
					throws EventLogNotFound, InvalidRole {

		if (comment == null) {
			//NOT NULL 列のため、nullの場合は何もしない
			return;
		}
		
		// イベントログ情報を取得
		EventLogEntity event = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			
			try {
				event = QueryUtil.getEventLogPK(monitorId, monitorDetailId, pluginId, outputDate, facilityId, ObjectPrivilegeMode.MODIFY);
			} catch (EventLogNotFound e) {
				throw e;
			} catch (InvalidRole e) {
				throw e;
			}
			StringBuilder detail = new StringBuilder();
			commentDate = HinemosTime.currentTimeMillis();
			
			boolean isChange = ModifyEventInfo.setCommentChange(event, comment, commentDate, commentUser, detail);
			
			if (isChange) {
				EventLogOperationHistoryUtil.addEventLogOperationHistory(
						jtm, event, commentDate, commentUser, EventLogHistoryTypeConstant.TYPE_CHANGE_VALUE, detail.toString()
					);
			}
			
			jtm.addCallback(new EventCacheModifyCallback(false, event));
		}
	}
}
