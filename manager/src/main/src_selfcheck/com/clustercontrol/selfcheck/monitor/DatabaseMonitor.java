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

package com.clustercontrol.selfcheck.monitor;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * データソースの動作を確認する処理の実装クラス
 */
public class DatabaseMonitor extends SelfCheckMonitorBase {

	private static Log m_log = LogFactory.getLog( DatabaseMonitor.class );

	public String validationQuery;

	public final String monitorId = "SYS_DB";
	public final String subKey = "";
	public final String application = "SELFCHECK (Database)";

	/**
	 * コンストラクタ
	 * @param validationQuery 動作確認クエリ
	 */
	public DatabaseMonitor() {
	}

	/**
	 * セルフチェック処理名
	 */
	@Override
	public String toString() {
		return "monitoring datasource (query = " + validationQuery + ")";
	}

	/**
	 * 監視項目ID
	 */
	@Override
	public String getMonitorId() {
		return monitorId;
	}

	/**
	 * データソースへの疎通確認
	 * @return 通知情報（アプリケーション名は未格納）
	 */
	@Override
	public void execute() {
		if (!HinemosPropertyUtil.getHinemosPropertyBool("selfcheck.monitoring.db", true)) {
			m_log.debug("skip");
			return;
		}

		/** ローカル変数 */
		JpaTransactionManager tm = null;
		HinemosEntityManager em = null;
		boolean warn = true;
		
		validationQuery = HinemosPropertyUtil.getHinemosPropertyStr(
				"selfcheck.monitoring.db.validationquery",
				"SELECT 1 FOR UPDATE");

		/** メイン処理 */
		m_log.debug("monitoring datasource. (query = " + validationQuery + ")");
		try {
			// データソースのオブジェクトをJNDI経由で取得し、取得したコネクションが正しく動作するかを確認する
			// JPA経由でクエリが正常に発行できることを確認する。
			tm = new JpaTransactionManager();
			tm.begin();

			em = tm.getEntityManager();
			em.createNativeQuery(validationQuery).getResultList();

			warn = false;

			tm.commit();
		} catch (Exception e) {
			if (tm != null)
				tm.rollback();
			m_log.warn("executing query failure. (query = " + validationQuery + ")");
		} finally {
			if (tm != null) {
				tm.close();
			}
		}
		if (warn) {
			m_log.info("datasource is not avaiable. (query = " + validationQuery + ")");
		}

		if (!isNotify(subKey, warn)) {
			return;
		}

		String[] msgAttr1 = { };
		AplLogger.put(PriorityConstant.TYPE_WARNING, PLUGIN_ID, MessageConstant.MESSAGE_SYS_001_SYS_SFC, msgAttr1,
				"database is not avaiable. failure in query(" +
						validationQuery + ").");

		return;
	}

}
