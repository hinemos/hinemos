/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck.monitor;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.platform.HinemosPropertyDefault;
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
		if (!HinemosPropertyCommon.selfcheck_monitoring_db.getBooleanValue()) {
			m_log.debug("skip");
			return;
		}

		/** ローカル変数 */
		JpaTransactionManager tm = null;
		HinemosEntityManager em = null;
		boolean warn = true;
		
		validationQuery = HinemosPropertyDefault.selfcheck_monitoring_db_validationquery.getStringValue();

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
		AplLogger.put(InternalIdCommon.SYS_SFC_SYS_001, msgAttr1,
				"database is not avaiable. failure in query(" +
						validationQuery + ").");

		return;
	}

}
