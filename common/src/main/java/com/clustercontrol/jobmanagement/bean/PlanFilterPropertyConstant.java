/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * ジョブ[スケジュール予定]ビューのフィルタ用文字列定義を定数として定義するクラス<BR
 * 
 * 
 */
public class PlanFilterPropertyConstant {
	/** 日時（開始） */
	public static final String FROM_DATE = "fromDate";

	/** 日時（終了） */
	public static final String TO_DATE = "toDate";

	/** スケジュールID */
	public static final String JOBKICK_ID = "jobkickId";

	private PlanFilterPropertyConstant() {
		throw new IllegalStateException("ConstClass");
	}
}