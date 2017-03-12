/**********************************************************************
 * Copyright (C) 2006 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

package com.clustercontrol.accesscontrol.util;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;


/**
 * Session Timer
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class SessionTimer {
	// ログ
	private static Log m_log = LogFactory.getLog( SessionTimer.class );

	/** ファシリティID更新用タイマー * */
	private Timer m_timer = null;

	/**
	 * チェックタスクを開始する
	 * 
	 * @param inverval 間隔(分)
	 */
	public void start(final int interval) {
		// タイマーが存在しない場合のみ実行
		if (m_timer == null && interval > 0) {
			// タイマー作成
			m_timer = new Timer(true);
			final ServiceContext context = ContextProvider.getContext();

			m_log.debug("SessionTimer start");

			// スケジュール設定
			m_timer.schedule(new TimerTask() {
				@Override
				public void run() {
					m_log.trace("SessionTimer start at Date : " + (new Date()).toString());
					ContextProvider.releaseContextHolder();
					ContextProvider.setContext(context);
					ClientSession.doCheck();
				}
			}, 1000, (long)interval * 60 * 1000 );
		}
	}
	public void cancel(){
		if(m_timer != null){
			// スケジュール削除
			m_timer.cancel();

			// タイマー削除
			m_timer = null;
		}
	}
}
