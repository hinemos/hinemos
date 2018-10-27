/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.etc.action;

import org.eclipse.swt.widgets.Display;

/**
 * UIに関する操作を行うタスクを、指定した間隔ごとにバックグラウンドで実行するクライアント側アクションクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class BackGroundAction {

	// ----- instance フィールド ----- //

	/** バックグラウンド実行タスク */
	private Runnable task = null;

	/** 実行間隔(ミリ秒) */
	private long interval = 0;

	/** タスク実行フラグ */
	private boolean canStarted = false;

	/** Displayオブジェクト(asyncExec用) */
	private Display display = null;

	// ----- コンストラクタ ----- //

	/**
	 * UIに関する処理を定義したtaskを、intervalミリ秒の間隔で繰り返しバックグラ ウンド実行するインスタンスを返します。
	 * <p>
	 * 
	 * まず、intervalミリ秒待機した後、intervalミリ秒の間隔で繰り返しタスクが実行 されます。
	 * 
	 * @param display
	 *            Displayオブジェクト
	 * @param task
	 *            バックグラウンド実行タスク
	 * @param interval
	 *            実行間隔(ミリ秒)
	 */
	public BackGroundAction(Display display, Runnable task, long interval) {
		this.display = display;
		this.task = task;
		this.interval = interval;
	}

	// ----- instance メソッド ----- //

	/**
	 * バックグラウンド処理を開始します。
	 */
	public void start() {

		this.canStarted = true;

		// タスク実行用スレッド生成
		Thread thread = new Thread() {
			@Override
			public void run() {
				while (canStarted) {
					try {
						Thread.sleep(interval);
					} catch (Exception e) {
					}

					if (canStarted) {
						if (display.isDisposed()) {
							return;
						} else {
							display.asyncExec(task);
						}
					}
				}
			}
		};

		// タスクを実行します。
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * バックグラウンド処理を停止します。
	 */
	public void stop() {
		this.canStarted = false;
	}

	/**
	 * バックグラウンド処理が実行中であるかを返します。
	 * 
	 * @return バックグラウンド処理が実行中である場合、true
	 */
	public boolean isStarted() {
		return this.canStarted;
	}

	/**
	 * 実行間隔(ミリ秒)を返します。
	 * 
	 * @return 実行間隔(ミリ秒)
	 */
	public long getInterval() {
		return this.interval;
	}
}
