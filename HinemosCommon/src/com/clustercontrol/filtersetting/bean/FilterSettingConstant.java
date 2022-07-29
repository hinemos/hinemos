/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.bean;

/**
 * フィルタ設定に関するコンポーネント共通定数です。
 */
public class FilterSettingConstant {

	/** フィルタ設定IDの最大長 */
	public static final int ID_LEN_MAX = 512;

	/** フィルタ名の最大長 */
	public static final int NAME_LEN_MAX = 1024;

	/** フィルタ設定検索パターンの最大長 */
	public static final int SEARCH_PATTERN_LEN_MAX = 1024;

	/** フィルタ条件の最大数 */
	public static final int CONDITION_COUNT_MAX = 10;

	/** フィルタ条件の説明文の最大長 */
	public static final int CONDITION_DESC_LEN_MAX = 1024;

	/** フィルタ条件項目の最大数 ＝ AND結合可能な数 */
	public static final int ITEM_COUNT_MAX = 10;

	/** フィルタ条件項目入力値の最大長 */
	public static final int ITEM_VALUE_LEN_MAX = 4096;

}
