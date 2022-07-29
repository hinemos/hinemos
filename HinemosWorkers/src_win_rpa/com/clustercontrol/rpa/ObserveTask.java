/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.rpa.bean.RoboInfo;
import com.clustercontrol.jobmanagement.rpa.util.RoboFileManager;
import com.clustercontrol.jobmanagement.rpa.util.RpaWindowsUtil;


/**
 * ファイル生成監視タスクの抽象クラス
 */
public abstract class ObserveTask implements Runnable {

	/** ロガー */
	private static Log m_log = LogFactory.getLog(ObserveTask.class);

	/** 実行指示ファイル生成確認の間隔 */
	protected static final int checkInterval = Integer.parseInt(RpaToolExecutorProperties.getProperty("file.check.interval"));

	/** RPAツールエグゼキューター連携用ファイル管理オブジェクト */
	protected RoboFileManager roboFileManager;

	/** スレッド名 */
	private final String threadName;


	/**
	 * コンストラクタ
	 * @param threadName スレッド名
	 * @throws HinemosUnknown RoboFileManagerでの例外
	 */
	public ObserveTask(String threadName) throws HinemosUnknown {
		this.threadName = threadName;
		try {
			roboFileManager = new RoboFileManager();
		} catch (InterruptedException e) {
			m_log.warn("ObserveTask() : interrupted. e=" + e.getMessage(), e);
		}
	}

	/** ファイル生成監視処理を中断します。 */
	public void abort() {
		roboFileManager.abort();
	}

	/**
	 * スレッド名を返します。
	 * @return スレッド名
	 */
	public String getThreadName() {
		return threadName;
	}

	/**
	 * 実行しても良いかを判定する。
	 * 実行してはいけない場合、待ちが入る。
	 *
	 * @param info 指示ファイル情報
	 * @return セッション有無
	 * @throws InterruptedException インタラプトされた場合
	 */
	protected boolean isAllowedToExecute(RoboInfo info) throws InterruptedException {
		// ログインユーザと指示ファイルのユーザが一致する場合
		String loginUserName = RpaWindowsUtil.getUseername();
		if (loginUserName != null && info != null
				&& loginUserName.equals(info.getUserName())) {
			m_log.debug("isAllowedToExecute() : true.");
			return true;
		}

		// 待ち
		int wait = checkInterval * 3;		// 30秒
		m_log.info("isAllowedToExecute() : NOT allowed to execute, so wait " + wait + " ms.");
		Thread.sleep(wait);

		return false;
	}

}
