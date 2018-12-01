/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;


/**
 * ジョブで実行で使用するトピックの定義を定数として定義するクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class TopicConstant {
	/** ジョブ実行用トピック名 */
	public static final String TOPIC_NAME_EXECUTE = "topic/clustercontrolJobManagementExecute";

	private TopicConstant() {
		throw new IllegalStateException("ConstClass");
	}
}
