/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.selfcheck.monitor;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.selfcheck.SelfCheckConfig;

/**
 * セルフチェック機能の処理実装の抽象クラス
 */
public abstract class SelfCheckMonitorBase implements SelfCheckMonitor {

	private static Log m_log = LogFactory.getLog(SelfCheckMonitorBase.class);
	private static volatile ConcurrentHashMap<String, Integer> invalidCounter = new ConcurrentHashMap<String, Integer>();

	/**
	 * 通知の有無を判定するメソッド
	 * 
	 * @param monitor
	 * @param warnFlag
	 * @return
	 */
	protected boolean isNotify(String subKey, boolean warnFlag) {

		// counterの初期化
		String key = getMonitorId() + "-selfcheck-" + subKey;

		if (!invalidCounter.containsKey(key)) {
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
					m_log.debug("incrementing monitoring counter. (key = " + key + ",current counter = "
							+ invalidCounter.get(key) + ")");
				invalidCounter.replace(key, invalidCounter.get(key) + 1);
			}

			// 初回のアラート
			if (invalidCounter.get(key) == SelfCheckConfig.getSelfcheckAlertThreshold()) {
				m_log.debug("output result of selfcheck monitoring[1]. (" + this.toString() + ")");
				return true;
			}
			
			// 初回以降は selfcheck.alert.threshold.after.first.alert 毎にアラートする
			if (invalidCounter.get(key) > SelfCheckConfig.getSelfcheckAlertThreshold()) {
				m_log.debug("output result of selfcheck monitoring[2]. (" + this.toString() + ")");
				boolean ret = checkNotifySuppression(key);
				return ret;
			}
		}
		return false;
	}

	protected boolean checkNotifySuppression(String key) {
		boolean ret = false;

		int notifySuppression = SelfCheckConfig.getSelfcheckAlertThresholdAfterFirstAlert();
		int threshold = SelfCheckConfig.getSelfcheckAlertThreshold();
		
		// 0以下の場合は通知しない
		if(notifySuppression <= 0) {
			return ret;
		}
		
		// 初回アラート以降の異常検知回数
		int afterThresholdExceedCount = invalidCounter.get(key) - threshold;
		// 初回アラート以降の異常検知回数を notifySuppression で割った余りが 0 であれば
		// notifySuppression 回連続して異常検知している
		int remainder = afterThresholdExceedCount % notifySuppression;
		m_log.debug("checkNotifySuppression() remainder=" + remainder); 
		if (remainder == 0) {
			ret = true;
		}

		return ret;
	}
}
