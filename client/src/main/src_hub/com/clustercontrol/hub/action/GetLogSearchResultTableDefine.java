/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.action;

public class GetLogSearchResultTableDefine {
	
	/** 時刻 */
	public static final int TIME = 0;
	
	/** ファシリティID */
	public static final int FACILITY = 1;
		
	/** オリジナルメッセージ */
	public static final int ORG_MESSAGE = 2;

	/** 備考 */
	public static final int NOTE = 3;
	
	/** ダミー**/
	public static final int DUMMY = 4;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = TIME;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;
}
