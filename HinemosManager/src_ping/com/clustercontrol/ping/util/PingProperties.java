/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ping.util;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.platform.HinemosPropertyDefault;

/**
 * PING監視プロパティ情報取得クラス
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
	 * @return PingProperties Pingプロパティ
	 */
	public synchronized static PingProperties getProperties() {
		if (m_instance==null) {
			m_instance = new PingProperties();
		}
		return m_instance;
	}

	public static int getFpingCount() {
		/** fping利用時のデフォルトの　ping回数 */
		return HinemosPropertyCommon.monitor_ping_fping_count.getIntegerValue();
	}

	public static boolean isFpingEnable() {
		/** Fpingを使用するか？　falseであれば2.2までのisReachableを利用 */
		return HinemosPropertyCommon.monitor_ping_fping_enable.getBooleanValue();
	}

	public static int getFpingInterval() {
		/** fping利用時のデフォルトの　pingインターバル msec*/
		return HinemosPropertyCommon.monitor_ping_fping_interval.getIntegerValue();
	}

	public static int getFpingTimeout() {
		/** fping利用時のデフォルトの　pingタイムアウト msec*/
		return HinemosPropertyCommon.monitor_ping_fping_timeout.getIntegerValue();
	}

	public static int getFpingBytes() {
		/** fping利用時のデフォルトの　ping送信データサイズ byte*/
		return HinemosPropertyCommon.monitor_ping_fping_bytes.getIntegerValue();
	}

	public static String getFpingPath() {
		/** fping のパス **/
		return HinemosPropertyDefault.monitor_ping_fping_path.getStringValue();
	}

}
