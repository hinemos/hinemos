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
import com.clustercontrol.monitor.util.StatusFilterPropertyUtil;

/**
 * ステータス情報フィルタ用プロパティを取得するクライアント側アクションクラス<BR>
 * 
 * マネージャにSessionBean経由でアクセスし、ステータス情報フィルタ用プロパティを取得します。
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetStatusFilterProperty {

	/**
	 * マネージャにSessionBean経由でアクセスし、ステータス情報フィルタ用プロパティを取得します。
	 * 
	 * @return ステータス情報フィルタ用プロパティ
	 * 
	 * @see com.clustercontrol.monitor.ejb.session.MonitorController
	 * @see com.clustercontrol.monitor.ejb.session.MonitorControllerBean#getStatusFilterProperty(Locale)
	 */
	public Property getProperty() {
		return StatusFilterPropertyUtil.getProperty(Locale.getDefault());
	}
}
