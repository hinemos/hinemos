/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

/**
 * フィルタ機能に関連する定数値です。
 */
public class FilterConstant {

	/** 
	 * 文字列値の条件において、否定値を表現するために使用するプレフィックスです。
	 * 例えば、"NOT:ABC" は「ABCに一致しない」ことを表します。
	 */
	public static final String NEGATION_PREFIX = "NOT:";

	/**
	 * 文字列値の条件において、AND指定したい複数の値は、この文字列で結合します。
	 * {@link #NEGATION_PREFIX}との併用も可能です。
	 * 例えば、"%AB%:AND:%CD%:AND:NOT:%EF%" は「"AB"と"CD"を含み、"EF"を含まない」ことを表します。
	 */
	public static final String AND_SEPARATOR = ":AND:";

}
