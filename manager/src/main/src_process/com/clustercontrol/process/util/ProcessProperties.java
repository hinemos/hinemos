/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.process.util;

import com.clustercontrol.commons.util.HinemosPropertyCommon;

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
		return HinemosPropertyCommon.monitor_process_start_second.getIntegerValue();
	}

	/**
	 * @return SNMPポーラー収集許容時間（秒） を戻します。
	 */
	public int getValidSecond() {
		/** SNMPポーラー収集許容時間（秒） */
		return HinemosPropertyCommon.monitor_process_valid_second.getIntegerValue();
	}

	/**
	 * @return オリジナルメッセージにプロセスを表示させるかのフラグを戻します。
	 */
	public boolean isDetailedDisplay() {
		/** オリジナルメッセージにプロセスを表示させるかのフラグ */
		return HinemosPropertyCommon.monitor_process_details_display.getBooleanValue();
	}
	
	/**
	 * @return マネージャ起動直後の初回閾値判定処理を行うまでの待ち時間を返します。 
	 */
	public int getCollectInitDelaySecond() {
		return HinemosPropertyCommon.monitor_process_collect_initdelay_second.getIntegerValue();
	}
}
