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

package com.clustercontrol.snmp.util;

import com.clustercontrol.maintenance.util.HinemosPropertyUtil;

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
		m_validSecond = HinemosPropertyUtil.getHinemosPropertyNum("monitor.snmp.valid.second", Long.valueOf(15)).intValue();
		return m_validSecond;
	}
}
