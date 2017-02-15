/*

Copyright (C) 2007 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.ping.util;

import com.clustercontrol.maintenance.util.HinemosPropertyUtil;

/**
 * プロセス監視プロパティ情報取得クラス
 *
 * @version 2.3.0 bata
 * @since 2.3.0
 */
public class PingProperties {
	private static PingProperties m_instance = null;

	/**
	 * このオブジェクトを取得します。
	 *
	 *
	 * @version 2.0.0
	 * @since 2.0.0
	 *
	 * @return ConnectionManager コネクションマネージャ
	 */
	public synchronized static PingProperties getProperties() {
		if (m_instance==null) {
			m_instance = new PingProperties();
		}
		return m_instance;
	}

	protected static int getFpingCount() {
		/** fping利用時のデフォルトの　ping回数 */
		return HinemosPropertyUtil.getHinemosPropertyNum("monitor.ping.fping.count", Long.valueOf(1)).intValue();
	}

	public static boolean isFpingEnable() {
		/** Fpingを使用するか？　falseであれば2.2までのisReachableを利用 */
		return HinemosPropertyUtil.getHinemosPropertyBool("monitor.ping.fping.enable", true);
	}

	protected static int getFpingInterval() {
		/** fping利用時のデフォルトの　pingインターバル msec*/
		return HinemosPropertyUtil.getHinemosPropertyNum("monitor.ping.fping.interval", Long.valueOf(1000)).intValue();
	}

	protected static int getFpingTimeout() {
		/** fping利用時のデフォルトの　pingタイムアウト msec*/
		return HinemosPropertyUtil.getHinemosPropertyNum("monitor.ping.fping.timeout", Long.valueOf(1000)).intValue();
	}

	protected static int getFpingBytes() {
		/** fping利用時のデフォルトの　ping送信データサイズ byte*/
		return HinemosPropertyUtil.getHinemosPropertyNum("monitor.ping.fping.bytes", Long.valueOf(56)).intValue();
	}

	protected static String getFpingPath() {
		/** fping のパス **/
		String homeDir = System.getProperty("hinemos.manager.home.dir");
		return HinemosPropertyUtil.getHinemosPropertyStr("monitor.ping.fping.path", homeDir + "/sbin/fping");
	}

	protected static String getFping6Path() {
		/** fping のパス **/
		String homeDir = System.getProperty("hinemos.manager.home.dir");
		return HinemosPropertyUtil.getHinemosPropertyStr("monitor.ping.fping6.path", homeDir + "/sbin/fping6");
	}

}
