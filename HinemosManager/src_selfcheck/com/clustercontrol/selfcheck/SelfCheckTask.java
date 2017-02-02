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

package com.clustercontrol.selfcheck;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.selfcheck.monitor.SelfCheckMonitor;

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

		try {
			tm = new JpaTransactionManager();
			tm.begin();

			// 監視処理の実行
			monitor.execute();

			tm.commit();
		} catch (Exception e) {
			m_log.warn("run() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (tm != null)
				tm.rollback();
		} finally {
			if (tm != null)
				tm.close();
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
