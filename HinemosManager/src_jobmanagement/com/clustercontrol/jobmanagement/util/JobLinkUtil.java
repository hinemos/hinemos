/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.HinemosManagerMain;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.util.HinemosTime;

/**
 * ジョブ連携ユーティリティクラス
 * 
 */
public class JobLinkUtil {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( JobLinkUtil.class );

	/**
	 * ジョブ連携メッセージ 最終送信日時
	 * cc_job_link_messageテーブルは主キーに送信日時を含むため重複を避ける対応
	 * **/
	private volatile static long _lastSendDateTime = 0;

	/**
	 * ジョブ連携メッセージのオリジナルメッセージをHinemosプロパティ定義によって切断する。
	 * @param messageOrg ジョブ連携メッセージのオリジナルメッセージ
	 * @return
	 */
	public static String getMessageOrgMaxString(String messageOrg) {
		return getMaxString(HinemosPropertyCommon.joblinkmes_messageorg_max_length, messageOrg);
	}

	/**
	 * ジョブ連携メッセージのメッセージをHinemosプロパティ定義によって切断する。
	 * @param message ジョブ連携メッセージのメッセージ
	 * @return
	 */
	public static String getMessageMaxString(String message) {
		return getMaxString(HinemosPropertyCommon.joblinkmes_message_max_length, message);
	}

	/**
	 * 指定された文字列がHinemosプロパティ上で定義されているサイズよりも長い場合に切断する。
	 * @param hinemosPropertyCommon Hinemosプロパティ
	 * @param targetString 対象文字列
	 * @return
	 */
	private static String getMaxString(HinemosPropertyCommon hinemosPropertyCommon, String targetString) {
		if (targetString == null) {
			return targetString;
		}
		int maxLen = hinemosPropertyCommon.getIntegerValue();
		String returnString = null;
		if (targetString.length() <= maxLen) {
			returnString = targetString;
		} else {
			returnString = targetString.substring(0, maxLen);
		}
		return returnString;
	}

	/**
	 * ジョブ連携メッセージの送信日時を払い出します。
	 * 前回払い出した日時とは重複しないように払い出します。
	 * 
	 * @return 送信日時
	 */
	public static synchronized Long createSendDate() throws InvalidSetting {
		// 現在時刻を取得
		long now = HinemosTime.currentTimeMillis();
		now = now - now % HinemosManagerMain._instanceCount + HinemosManagerMain._instanceId;
		long sendDateTime = 0;

		if((_lastSendDateTime - 1000) < now && now <= _lastSendDateTime){
			// 現在時刻と最終送信日時の誤差が1秒以内であり（時刻同期により大幅にずれた場合を想定）、
			// 現在時刻が最後に払い出した出力日時より昔の場合は、最終送信日時より1msだけ進める
			sendDateTime = _lastSendDateTime + HinemosManagerMain._instanceCount;
			if (m_log.isDebugEnabled()) {
				m_log.debug("create SendDate=" + sendDateTime);
			}
		} else {
			sendDateTime = now;
		}

		_lastSendDateTime = sendDateTime;

		return Long.valueOf(sendDateTime);
	}
}