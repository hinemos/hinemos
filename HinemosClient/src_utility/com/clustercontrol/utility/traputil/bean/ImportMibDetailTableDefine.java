/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.traputil.bean;

/**
 * OID一覧テーブルのカラムを定義するクラス<BR>
 * 
 * @version 6.1.0
 * @since 2.4.0
 */
public class ImportMibDetailTableDefine {
	
	// ----- static フィールド ----- //
	
	/** インポート実行ステータス */
	public static final int RUN_STATUS = 0;
	
	/** MIB */
	public static final int MIB = 1;
	
	/** トラップ名 */
	public static final int TRAP_NAME = 2;
	
	/** OID */
	public static final int TRAP_OID = 3;

	/** generic_id */
	public static final int GENERIC_ID = 4;
	
	/** specific_id */
	public static final int SPECIFIC_ID = 5;
	

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = TRAP_NAME;
	
	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;
	
}