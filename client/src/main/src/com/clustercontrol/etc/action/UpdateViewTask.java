/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
