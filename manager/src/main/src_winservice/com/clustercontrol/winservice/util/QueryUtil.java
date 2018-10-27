/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.winservice.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.winservice.model.WinServiceCheckInfo;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static WinServiceCheckInfo getMonitorWinserviceInfoPK(String monitorId) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			WinServiceCheckInfo entity = em.find(WinServiceCheckInfo.class, monitorId, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorWinserviceInfoEntity.findByPrimaryKey, "
						+ "monitorId = " + monitorId);
				m_log.info("getMonitorWinserviceInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}
}
