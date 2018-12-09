/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.action;

import java.util.Locale;

import com.clustercontrol.bean.Property;
import com.clustercontrol.jobmanagement.util.JobKickFilterPropertyUtil;

/**
 * ジョブ実行契機情報フィルタ用プロパティを取得するクライアント側アクションクラス<BR>
 * 
 * @version 5.1.0
 */
public class GetJobKickFilterProperty {

	/**
	 * ジョブ実行契機情報フィルタ用プロパティを取得します。
	 * 
	 * @return ジョブ実行契機情報フィルタ用プロパティ
	 */
	public Property getProperty() {
		return JobKickFilterPropertyUtil.getProperty(Locale.getDefault());
	}
}
