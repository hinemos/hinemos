/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.selfcheck.monitor.SelfCheckMonitor;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * セルフチェック処理実行処理の実装クラス
 */
public class SelfCheckTask implements Runnable {

	private static Log m_log = LogFactory.getLog(SelfCheckTask.class);

	private final SelfCheckMonitor monitor;

	/**
	 * コンストラクタ
	 * @param config
	 */
	public SelfCheckTask(SelfCheckMonitor monitor){
		this.monitor = monitor;
	}

	/**
	 * セルフチェック処理の実行
	 */
	@Override
	public void run() {
		/** ローカル変数 */
		JpaTransactionManager tm = null;

		/** メイン処理 */
		if (m_log.isDebugEnabled()) m_log.debug("executing self-check. (" + toString() + ")");

		boolean failure = false;
		String errorMessage = "";
		try {
			tm = new JpaTransactionManager();
			tm.begin();

			// 監視処理の実行
			monitor.execute();

			tm.commit();
		} catch (Exception e) {
			m_log.warn("run() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			failure = true;
			errorMessage = e.getMessage();
			if (tm != null)
				tm.rollback();
		} finally {
			if (tm != null)
				tm.close();
			if (failure) {
				AplLogger.put(InternalIdCommon.SYS_SFC_SYS_030, new String[] { toString() }, errorMessage);
			}
		}

		if (m_log.isDebugEnabled()) m_log.debug("selfcheck scheduler task is executed. (" + toString() + ")");
	}

	/**
	 * セルフチェック処理名の取得
	 */
	@Override
	public String toString() {
		/** メイン処理 */
		return monitor.toString();
	}
}
