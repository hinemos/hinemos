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

package com.clustercontrol.view;

import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.swt.widgets.Display;

import com.clustercontrol.etc.action.BackGroundAction;
import com.clustercontrol.etc.action.UpdateViewTask;

/**
 * 自動更新機能を持つ抽象ビュークラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class AutoUpdateView extends CommonViewPart {

	//	 ----- instance フィールド ----- //

	/** 自動更新処理用アクション */
	private BackGroundAction reloadAction = null;

	/** 自動更新フラグ */
	private boolean autoReloaded = false;

	/** 自動更新間隔(秒) */
	private int interval = 0;
	
	private ServerPushSession pushSession = new ServerPushSession();

	//	 ----- instance メソッド ----- //

	/**
	 * 自動更新処理中であるかを返します。
	 * 
	 * @return 自動更新中である場合、true
	 */
	public boolean isAutoReloaded() {
		return this.autoReloaded;
	}

	/**
	 * 自動更新処理中であるかを設定します。
	 * 
	 * @param autoReloaded
	 *            自動更新中である場合、true
	 */
	private void setAutoReloaded(boolean autoReloaded) {
		this.autoReloaded = autoReloaded;
	}

	/**
	 * 自動更新間隔(秒)を返します。
	 * 
	 * @return 自動更新間隔(秒)
	 */
	public int getInterval() {
		return this.interval;
	}

	/**
	 * 自動更新間隔(秒)を設定します。
	 * 
	 * @param interval
	 *            自動更新間隔(秒)
	 */
	public void setInterval(int interval) {
		this.interval = interval;
	}

	/**
	 * 設定された更新間隔で自動更新を実行します。
	 * <p>
	 * 
	 * 自動更新がすでに実行中の場合、停止後に再開します。 <br>
	 * 自動更新間隔が0以下の場合、処理は実行されません。
	 */
	public void startAutoReload() {
		this.stopAutoReload();

		int interval = this.getInterval();
		if (interval > 0) {
			UpdateViewTask task = new UpdateViewTask(this);

			Display display = this.getViewSite().getShell().getDisplay();
			this.reloadAction = new BackGroundAction(display, task,
					(long)interval * 1000 * 60);

			this.reloadAction.start();
			this.setAutoReloaded(true);
			pushSession.start();
		}
	}

	/**
	 * 自動更新を停止します。
	 */
	public void stopAutoReload() {
		if (this.reloadAction != null) {
			this.reloadAction.stop();
		}

		this.reloadAction = null;
		this.setAutoReloaded(false);
		pushSession.stop();
	}

	/**
	 * 自動更新を終了します。
	 */
	@Override
	public void dispose() {
		super.dispose();
		this.stopAutoReload();
		pushSession.stop();
	}

	/**
	 * ビューの更新を行います。
	 * 
	 * @param リフレッシュフラグ
	 *             リフレッシュする場合、true
	 */
	public abstract void update(boolean refreshFlag);
}
