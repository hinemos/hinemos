/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import com.clustercontrol.notify.bean.NotifyRequestMessage;

/**
 * 通知を実行するユーティリティメソッドのインタフェースを規定します。
 */
public interface Notifier {
	/**
	 * 通知を実行します
	 */
	public void notify(NotifyRequestMessage message) throws Exception;
}
