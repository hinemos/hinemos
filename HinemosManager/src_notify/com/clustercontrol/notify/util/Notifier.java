/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.util.MessageConstant;

/**
 * 通知を実行するユーティリティメソッドのインタフェースを規定します。
 */
public interface Notifier {
	/**
	 * 通知を実行します
	 */
	public void notify(NotifyRequestMessage message) throws Exception;

	/**
	 * 通知失敗時の内部エラー通知を定義します
	 */
	public void internalErrorNotify(int priority, String notifyId, MessageConstant msgCode, String detailMsg) throws Exception;
}
