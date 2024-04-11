/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck.monitor;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.selfcheck.util.SelfCheckDivergence;
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
		if (!HinemosPropertyCommon.selfcheck_monitoring_dbtran.getBooleanValue()) {
			m_log.debug("skip");
			return;
		}

		/** ローカル変数 */
		JpaTransactionManager tm = null;
		HinemosEntityManager em = null;
		boolean warn = true;
		List<Object> list = null;

		// 時間間隔（秒）
		Integer intervalSec = HinemosPropertyCommon.selfcheck_monitoring_dbtran_interval.getIntegerValue();
		// 指定文字列用の時間間隔（秒）
		Integer longIntervalSec = HinemosPropertyCommon.selfcheck_monitoring_dbtran_long_interval.getIntegerValue();
		// SQL
		if(intervalSec > longIntervalSec) {
			validationQuery = String.format(SelfCheckDivergence.getDbLongTranValidationQuery(), longIntervalSec);
		} else {
			validationQuery = String.format(SelfCheckDivergence.getDbLongTranValidationQuery(), intervalSec);
		}

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
				warn = false;
				for(Object info : list) {
					Object[] resultList = (Object[])info;
					boolean stringBool = designationStringBool((String)resultList[1]);

					if(stringBool && (double)resultList[0] >= longIntervalSec ||
							!stringBool && (double)resultList[0] >= intervalSec) {
						warn = true;
						m_log.info("exists long running transaction. (" + (String)resultList[1] + ")");
					}
				}
			}
		}

		if (!isNotify(subKey, warn)) {
			return;
		}

		String[] msgAttr1 = { };
		AplLogger.put(InternalIdCommon.SYS_SFC_SYS_013, msgAttr1,
				"exists long running transaction.");

		return;
	}

	/**
	 * 実行したクエリの結果内に指定した文字列が一致するかの確認
	 * 
	 * @param resultListString クエリ実行結果の文字列
	 * @return
	 */
	private boolean designationStringBool(String resultListString) {
		boolean stringBool = false;
		// 一致確認用
		final String query = "query = [";
		String designationString = HinemosPropertyCommon.selfcheck_monitoring_dbtran_interval_designation_string.getStringValue();
		String[] designationStringList = designationString.split(",", -1);

		for(String s : designationStringList) {
			if(s.replaceAll("\\s|　", "").equals("")) {
				continue;
			}

			String checkString = query + s;
			// 指定した文字列があった場合はtrueとし、処理を抜ける
			if(resultListString.contains(checkString)) {
				stringBool = true;
				break;
			}
		}

		return stringBool;
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
		Integer intervalSec = HinemosPropertyCommon.selfcheck_monitoring_dbtran_interval.getIntegerValue();
		// SQL
		String query = String.format(SelfCheckDivergence.getDbLongTranValidationQuery(), intervalSec);
		
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
