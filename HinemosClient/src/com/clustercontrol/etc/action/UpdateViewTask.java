/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.etc.action;

import com.clustercontrol.view.AutoUpdateView;

/**
 * AutoReloadViewの再描画を実行するクライアント側アクションクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class UpdateViewTask implements Runnable {

	// ----- instance フィールド ----- //

	/** 再描画するビュー */
	private AutoUpdateView target = null;

	// ----- コンストラクタ ----- //

	/**
	 * ビューの再描画を行うインスタンスを返します。
	 * 
	 * @param composite
	 *            再描画するAutoReloadViewのインスタンス
	 */
	public UpdateViewTask(AutoUpdateView view) {
		this.target = view;
	}

	// ----- instance メソッド ----- //

	/**
	 * ビューの再描画を行います。
	 * <p>
	 * 
	 * AutoUpdateView#reload()を実行します。
	 */
	@Override
	public void run() {
		this.target.update(true);
	}
}
