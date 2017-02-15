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

package com.clustercontrol.process.util;

import com.clustercontrol.maintenance.util.HinemosPropertyUtil;

/**
 * プロセス監視プロパティ情報を取得するクラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class ProcessProperties {
	private static ProcessProperties m_instance = null;

	/**
	 * このオブジェクトを取得します。
	 *
	 *
	 * @version 2.0.0
	 * @since 2.0.0
	 *
	 * @return ConnectionManager コネクションマネージャ
	 */
	public synchronized static ProcessProperties getProperties() {
		if (m_instance==null) {
			m_instance = new ProcessProperties();
		}
		return m_instance;
	}

	/**
	 * @return プロセス監視値取得開始時間（秒） を戻します。
	 */
	public int getStartSecond() {
		/** プロセス監視値取得開始時間（秒） */
		return HinemosPropertyUtil.getHinemosPropertyNum("monitor.process.start.second", Long.valueOf(30)).intValue();
	}

	/**
	 * @return SNMPポーラー収集許容時間（秒） を戻します。
	 */
	public int getValidSecond() {
		/** SNMPポーラー収集許容時間（秒） */
		return HinemosPropertyUtil.getHinemosPropertyNum("monitor.process.valid.second", Long.valueOf(50)).intValue();
	}

	/**
	 * @return オリジナルメッセージにプロセスを表示させるかのフラグを戻します。
	 */
	public boolean isDetailedDisplay() {
		/** オリジナルメッセージにプロセスを表示させるかのフラグ */
		return HinemosPropertyUtil.getHinemosPropertyBool("monitor.process.details.display", false);
	}
	
	/**
	 * @return マネージャ起動直後の初回閾値判定処理を行うまでの待ち時間を返します。 
	 */
	public int getCollectInitDelaySecond() {
		return HinemosPropertyUtil.getHinemosPropertyNum("monitor.process.collect.initdelay.second", Long.valueOf(60)).intValue();
	}
}
