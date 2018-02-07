/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.traputil.bean;

/**
 * マスタテーブルのカラムの表示項目と表示順を定義するクラス<BR>
 * 
 * @version 6.1.0
 * @since 2.1.0
 */
public class MasterTableDefine {
	
	// ----- static フィールド ----- //
	
	/** MIB */
	public static final int MIB = 0;
	
	/** トラップ名 */
	public static final int TRAP_NAME = 1;
	
	/** OID */
	public static final int TRAP_OID = 2;

	/** generic_id */
	public static final int GENERIC_ID = 3;
	
	/** specific_id */
	public static final int SPECIFIC_ID = 4;
	
	/** 重要度 */
	public static final int PRIORITY = 5;
	
	/** メッセージ */
	public static final int MESSAGE = 6;
	
	/////////以下、非表示カラム//////////////////
	
	/** 詳細説明 (非表示) */
	public static final int DESCR = 7;

	
	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = TRAP_NAME;
	
	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;
	
}