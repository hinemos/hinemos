/**********************************************************************
 * Copyright (C) 2014 NTT DATA Corporation
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

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Display;

/**
 * Session Timer
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class SessionTimer {

	// Logger
	private static final Log m_log = LogFactory.getLog( SessionTimer.class );

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
			m_timer = new Timer( true );

			// Prepare display for timer running with UI thread
			final Display display = Display.getCurrent();

			// スケジュール設定
			m_timer.schedule( new TimerTask(){
				@Override
				public void run() {
					try{
						// TODO Session will persist but ClientSession is losing after
						// page reload. Implement re-login after reload, including 
						// cancel and start, or directly continue the previous timer.

						// TODO How about if user set session timeout to 0 ? There will
						// be a big memory leak risk.

						// EJB接続確認タイマータスク 通信エラーとなった場合はダイアログを表示する
						RWT.getUISession(display).exec( new Runnable() {
							@Override
							public void run() {
								try{
									ClientSession.doCheck();
								}catch( NullPointerException e ){
									m_log.debug("Timer cancelled becaused of session timeout");
									m_timer.cancel();
								}
							}
						});
					}catch(Exception e){
						m_log.debug( "Timer cancelled, ", e );
						m_timer.cancel();
					}
				}
			}, interval * 60 * 1000l, interval * 60 * 1000l );

			m_log.debug("Timer started");
		}
	}
	public void cancel(){
		m_log.debug("Timer is cancelled");

		if(m_timer != null){
			// スケジュール削除
			m_timer.cancel();

			// タイマー削除
			m_timer = null;
		}
	}
}
