/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.customtrap.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.customtrap.model.CustomTrapCheckInfo;
import com.clustercontrol.fault.MonitorNotFound;

/**
 * カスタムトラップ監視情報取得
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(QueryUtil.class);

	/**
	 * CustomTrapCheckInfoを取得します。
	 * 
	 * @param monitorId		監視ID
	 * @return				CustomTrapCheckInfo
	 * @throws MonitorNotFound
	 */
	public static CustomTrapCheckInfo getMonitorCustomTrapInfoPK(String monitorId) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			CustomTrapCheckInfo entity = em.find(CustomTrapCheckInfo.class, monitorId, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound(
						"MonitorCustomTrapInfoEntity.findByPrimaryKey" + ", monitorId = " + monitorId);
				m_log.info("getMonitorCustomTrapInfoPK() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}
}
