/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.action;

import java.util.Locale;

import com.clustercontrol.bean.Property;
import com.clustercontrol.monitor.util.EventBatchConfirmPropertyUtil;

/**
 * イベント情報一括確認用プロパティを取得するクライアント側アクションクラス<BR>
 * 
 * マネージャにSessionBean経由でアクセスし、イベント情報一括確認用プロパティを取得します。
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetEventBatchConfirmProperty {

	/**
	 * イベント情報一括確認用プロパティを取得します。
	 * 
	 * @return イベント情報一括確認用プロパティ
	 */
	public Property getProperty() {
		return EventBatchConfirmPropertyUtil.getProperty(Locale.getDefault());
	}
}
