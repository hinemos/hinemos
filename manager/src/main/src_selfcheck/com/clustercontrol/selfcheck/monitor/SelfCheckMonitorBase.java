/*

Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.selfcheck.monitor;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;

/**
 * セルフチェック機能の処理実装の抽象クラス
 */
public abstract class SelfCheckMonitorBase implements SelfCheckMonitor {

	private static Log m_log = LogFactory.getLog(SelfCheckMonitorBase.class);
	public static final String PLUGIN_ID = HinemosModuleConstant.SYSYTEM_SELFCHECK;
	public static final String APL_ID = "selfcheck";
	public static final String FACILITY_ID = FacilityTreeAttributeConstant.INTERNAL_SCOPE;
	public static final String FACILITY_TEXT = FacilityTreeAttributeConstant.INTERNAL_SCOPE_TEXT;
	public static volatile ConcurrentHashMap<String, Integer> invalidCounter = new ConcurrentHashMap<String, Integer>();

	/**
	 * 通知の有無を判定するメソッド
	 * @param monitor
	 * @param warnFlag
	 * @return
	 */
	protected boolean isNotify(String subKey, boolean warnFlag) {

		// counterの初期化
		String key = getMonitorId() + "-selfcheck-" + subKey;

		if (! invalidCounter.containsKey(key)) {
			m_log.debug("initializing monitoring counter. (key = " + key + ")");
			invalidCounter.putIfAbsent(key, 0);
		}

		if (!warnFlag) {
			// 出力判定用カウンターをリセット
			m_log.debug("resetting monitoring counter. (key = " + key + ")");
			invalidCounter.put(key, 0);
		} else {
			// 出力判定用カウンタをインクリメント
			if (invalidCounter.get(key) < Integer.MAX_VALUE) {
				if (m_log.isDebugEnabled())
					m_log.debug("incrementing monitoring counter. (key = " + key + ", current counter = " + invalidCounter.get(key) + ")");
				invalidCounter.replace(key, invalidCounter.get(key) + 1);
			}

			if (invalidCounter.get(key) >= HinemosPropertyUtil.getHinemosPropertyNum("selfcheck.alert.threshold", Long.valueOf(3))) {
				m_log.debug("output result of selfcheck monitoring. (" + this.toString() + ")");
				return true;
			}
		}
		return false;
	}
}
