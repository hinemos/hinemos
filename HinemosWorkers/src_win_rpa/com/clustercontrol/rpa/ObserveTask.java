/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa;

import com.clustercontrol.jobmanagement.rpa.util.RoboFileManager;

/**
 * ファイル生成監視タスクの抽象クラス
 */
public abstract class ObserveTask implements Runnable {
	/** RPAツールエグゼキューター連携用ファイル出力先フォルダ */
	protected static final String roboFileDir = System.getProperty("hinemos.agent.rpa.dir");
	/** 実行指示ファイル生成確認の間隔 */
	protected static final int checkInterval = Integer.parseInt(RpaToolExecutorProperties.getProperty("file.check.interval"));
	/** RPAツールエグゼキューター連携用ファイル管理オブジェクト */
	protected RoboFileManager roboFileManager = new RoboFileManager(roboFileDir);
	/** スレッド名 */
	private final String threadName;
	
	/**
	 * コンストラクタ
	 * @param threadName スレッド名
	 */
	public ObserveTask(String threadName) {
		this.threadName = threadName;
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
}
