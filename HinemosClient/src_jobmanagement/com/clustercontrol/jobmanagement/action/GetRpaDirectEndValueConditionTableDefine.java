/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.action;

import java.util.ArrayList;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * RPAシナリオジョブ（直接実行）の終了値判定条件テーブル定義を取得するクライアント側アクションクラス
 */
public class GetRpaDirectEndValueConditionTableDefine {

	/** 順序 */
	public static final int ORDER_NO = 0;
	/** 判定対象 */
	public static final int JUDGMENT_TYPE = 1;
	/** 判定条件 */
	public static final int JUDGMENT_CONDITION = 2;
	/** 判定値 */
	public static final int JUDGMENT_VALUE = 3;
	/** 終了値 */
	public static final int END_VALUE = 4;
	/** 処理 */
	public static final int PROCESS_TYPE = 5;
	/** 大文字・小文字を区別しない */
	public static final int CASE_INSENSITIVE = 6;
	/** 説明 */
	public static final int DESCRIPTION = 7;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = ORDER_NO;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;

	public static ArrayList<TableColumnInfo> get() {
		// テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();

		tableDefine.add(ORDER_NO, new TableColumnInfo(Messages.getString("order"), TableColumnInfo.NONE, 50, SWT.LEFT));
		tableDefine.add(JUDGMENT_TYPE, new TableColumnInfo(Messages.getString("judgment.type"),
				TableColumnInfo.RPA_JUDGMENT_TYPE, 100, SWT.LEFT));
		tableDefine.add(JUDGMENT_CONDITION, new TableColumnInfo(Messages.getString("judgment.condition"),
				TableColumnInfo.RPA_JUDGMENT_CONDITION, 100, SWT.LEFT));
		tableDefine.add(JUDGMENT_VALUE,
				new TableColumnInfo(Messages.getString("judgment.value"), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(END_VALUE,
				new TableColumnInfo(Messages.getString("end.value"), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(PROCESS_TYPE,
				new TableColumnInfo(Messages.getString("process"), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(CASE_INSENSITIVE,
				new TableColumnInfo(Messages.getString("case.sensitive"), TableColumnInfo.CHECKBOX, 150, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description"), TableColumnInfo.NONE, 200, SWT.LEFT));

		return tableDefine;
	}

}
