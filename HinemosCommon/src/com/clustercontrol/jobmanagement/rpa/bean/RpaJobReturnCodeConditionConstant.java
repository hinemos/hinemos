/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.rpa.bean;

/**
 * RPAシナリオジョブのリターンコードによる終了値判定条件の定数クラス<BR>
 */
public class RpaJobReturnCodeConditionConstant {
	/** =(数値) */
	public static final int EQUAL_NUMERIC = 0;
	/** !=(数値) */
	public static final int NOT_EQUAL_NUMERIC = 1;
	/** &gt;(数値) */
	public static final int GREATER_THAN = 2;
	/** &gt;=(数値) */
	public static final int GREATER_THAN_OR_EQUAL_TO = 3;
	/** &lt;(数値) */
	public static final int LESS_THAN = 4;
	/** &lt;=(数値) */
	public static final int LESS_THAN_OR_EQUAL_TO = 5;
}
