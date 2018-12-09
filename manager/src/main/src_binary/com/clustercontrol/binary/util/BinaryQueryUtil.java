/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.binary.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.binary.model.BinaryCheckInfo;
import com.clustercontrol.binary.model.BinaryPatternInfo;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfoPK;

/**
 * バイナリ監視関連のDB照会Util<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 * 
 */
public class BinaryQueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(BinaryQueryUtil.class);

	/**
	 * バイナリ検索条件テーブルの主キー取得
	 */
	public static BinaryPatternInfo getBinaryPatternInfoPK(MonitorStringValueInfoPK pk) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			BinaryPatternInfo entity = em.find(BinaryPatternInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("BinaryPatternInfo.findByPrimaryKey" + pk.toString());
				m_log.info("getBinaryPatternInfoPK() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	/**
	 * バイナリ検索条件テーブルの主キー(ID指定)取得
	 */
	public static BinaryPatternInfo getBinaryPatternInfoPK(String monitorId, Integer orderNo) throws MonitorNotFound {
		return getBinaryPatternInfoPK(new MonitorStringValueInfoPK(monitorId, orderNo));
	}

	/**
	 * バイナリ監視設定の主キー取得
	 */
	public static BinaryCheckInfo getBinaryCheckInfoPK(String monitorId) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			BinaryCheckInfo entity = em.find(BinaryCheckInfo.class, monitorId, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound(
						"BinaryCheckInfooEntity.findByPrimaryKey" + ", monitorId = " + monitorId);
				m_log.info("getBinaryCheckInfoPK() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

}
