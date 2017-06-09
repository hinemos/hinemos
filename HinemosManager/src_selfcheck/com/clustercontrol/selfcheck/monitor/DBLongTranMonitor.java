/*

 Copyright (C) 2016 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.selfcheck.monitor;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.platform.selfcheck.SelfCheckPertial;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * データベースのロングトランザクションを確認する処理の実装クラス
 */
public class DBLongTranMonitor extends SelfCheckMonitorBase {

	private static Log m_log = LogFactory.getLog( DBLongTranMonitor.class );

	public String validationQuery;

	public final String monitorId = "SYS_DBTRAN";
	public final String subKey = "";
	public final String application = "SELFCHECK (DBTran)";
	private static final String PROP_DBTRAN = "selfcheck.monitoring.dbtran";
	private static final String PROP_INTERVAL = "selfcheck.monitoring.dbtran.interval";
	private static final Integer DEFAULT_INTERVAL = 86400; // 1day
	
	/**
	 * コンストラクタ
	 * @param validationQuery 動作確認クエリ
	 */
	public DBLongTranMonitor() {
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
	@SuppressWarnings("unchecked")
	public void execute() {
		if (!HinemosPropertyUtil.getHinemosPropertyBool(PROP_DBTRAN, true)) {
			m_log.debug("skip");
			return;
		}

		/** ローカル変数 */
		JpaTransactionManager tm = null;
		HinemosEntityManager em = null;
		boolean warn = true;
		List<Object> list = null;

		// 時間間隔（秒）
		Integer intervalSec = HinemosPropertyUtil.getHinemosPropertyNum(PROP_INTERVAL, Long.valueOf(DEFAULT_INTERVAL)).intValue();
		// SQL
		validationQuery = String.format(SelfCheckPertial.getDbLongTranValidationQuery(), intervalSec);

		/** メイン処理 */
		m_log.debug("monitoring long running transaction query. (query = " + validationQuery + ")");
		try {
			// データソースのオブジェクトをJNDI経由で取得し、取得したコネクションが正しく動作するかを確認する
			// JPA経由でクエリが正常に発行できることを確認する。
			tm = new JpaTransactionManager();
			tm.begin();

			em = tm.getEntityManager();
			list = (List<Object>)em.createNativeQuery(validationQuery).getResultList();
			
			if (list.size() == 0) {
				warn = false;
			}

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
			if (list != null) {
				for(Object info : list) {
					Object[] resultList = (Object[])info;
					m_log.info("exists long running transaction. (" + (String)resultList[1] + ")");
				}
			}
		}

		if (!isNotify(subKey, warn)) {
			return;
		}

		String[] msgAttr1 = { };
		AplLogger.put(PriorityConstant.TYPE_WARNING, PLUGIN_ID, MessageConstant.MESSAGE_SYS_013_SYS_SFC, msgAttr1,
				"exists long running transaction.");

		return;
	}

	/**
	 * データベースのロングトランザクションを返す。<br/>
	 * 
	 * @return 最も時間のかかっているクエリーの時間
	 */
	public static double getDBLongTransactionTime() {
		// ローカル変数
		JpaTransactionManager tm = null;
		HinemosEntityManager em = null;

		// 時間間隔（秒）
		Integer intervalSec = HinemosPropertyUtil.getHinemosPropertyNum(PROP_INTERVAL, Long.valueOf(DEFAULT_INTERVAL)).intValue();
		// SQL
		String query = String.format(SelfCheckPertial.getDbLongTranValidationQuery(), intervalSec);
		
		double duration = 0.0;

		// メイン処理
		try {
			tm = new JpaTransactionManager();
			tm.begin();
			em = tm.getEntityManager();

			@SuppressWarnings("unchecked")
			List<Object> rowList = (List<Object>) em.createNativeQuery(query).getResultList();
			if (rowList != null) {
				for (Object row : rowList) {
					Object[] resultList = (Object[])row;
					double tempDuration = resultList[0] != null ? Double.parseDouble(String.valueOf(resultList[0])) : 0.0;
					if (tempDuration > duration) {
						duration = tempDuration;
					}
				}
			}

			tm.commit();
		} catch (Exception e) {
			m_log.warn("database query execution failure. (" + query + ")", e);
		} finally {
			if ( tm != null) {
				tm.close();
			}
		}

		return duration;
	}
}
