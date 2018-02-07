/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmp.util;

import com.clustercontrol.commons.util.HinemosPropertyCommon;

/**
 * SNMP監視プロパティ情報を取得するクラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class SnmpProperties {
	private static SnmpProperties m_instance = null;

	/** SNMP 取得許容時間（秒） */
	private int m_validSecond;

	/**
	 * このオブジェクトを取得します。
	 *
	 *
	 * @version 2.0.0
	 * @since 2.0.0
	 *
	 * @return ConnectionManager コネクションマネージャ
	 */
	public synchronized static SnmpProperties getProperties() {
		if (m_instance==null) {
			m_instance = new SnmpProperties();
		}
		return m_instance;
	}

	/**
	 * @return m_validSecond を戻します。
	 */
	public int getValidSecond() {
		m_validSecond = HinemosPropertyCommon.monitor_snmp_valid_second.getIntegerValue();
		return m_validSecond;
	}
}
