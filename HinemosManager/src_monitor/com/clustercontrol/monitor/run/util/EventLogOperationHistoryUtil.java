/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.util;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.model.EventLogOperationHistoryEntity;

/**
 * イベントログ操作履歴用のユーティリティクラス。<br/>
 * 
 */
public class EventLogOperationHistoryUtil {
	
	public static void addEventLogOperationHistory(
			JpaTransactionManager jtm,
			EventLogEntity event,
			Long operationDate,
			String operationUser,
			int historyType,
			String detail
			) {
		
		HinemosEntityManager em = jtm.getEntityManager();
		EventLogOperationHistoryEntity history = new EventLogOperationHistoryEntity();
		history.setMonitorId(event.getId().getMonitorId());
		history.setMonitorDetailId(event.getId().getMonitorDetailId());
		history.setPluginId(event.getId().getPluginId());
		history.setOutputDate(event.getId().getOutputDate());
		history.setFacilityId(event.getId().getFacilityId());
		history.setOperationDate(operationDate);
		history.setOperationUser(operationUser);
		history.setHistoryType(historyType);
		history.setDetail(detail);
		
		em.persist(history);
	}
}
