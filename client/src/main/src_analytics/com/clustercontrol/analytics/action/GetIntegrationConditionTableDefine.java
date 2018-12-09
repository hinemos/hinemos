/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * 収集値統合監視－判定条件フィルタ一覧テーブル定義情報を取得するクライアント側アクションクラス<BR>
 * 
 * @version 6.1.0
 */
public class GetIntegrationConditionTableDefine {

	/** 順序 */
	public static final int ORDER_NO = 0;

	/** 対象ノード */
	public static final int TARGET_NODE = 1;

	/** 収集値種別 */
	public static final int MONITOR_TYPE = 2;

	/** 収集項目名 */
	public static final int ITEM_NAME = 3;

	/** 比較方法 */
	public static final int COMPARISON_METHOD = 4;

	/** 比較値 */
	public static final int COMPARISON_VALUE = 5;

	/** 説明 */
	public static final int DESCRIPTION = 6;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX = ORDER_NO;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;

	/**
	 * 収集値統合監視－判定条件フィルタ一覧のテーブル定義情報を取得します。
	 * 
	 * @return 収集値統合監視－判定条件フィルタ一覧テーブル定義情報
	 */
	public static ArrayList<TableColumnInfo> get() {
		// テーブル情報定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();
		tableDefine.add(ORDER_NO,
				new TableColumnInfo("", TableColumnInfo.NONE, 20, SWT.RIGHT));
		tableDefine.add(TARGET_NODE,
				new TableColumnInfo(Messages.getString("target.node", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(MONITOR_TYPE,
				new TableColumnInfo(Messages.getString("type", locale), TableColumnInfo.NONE, 40, SWT.LEFT));
		tableDefine.add(ITEM_NAME,
				new TableColumnInfo(Messages.getString("collection.display.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(COMPARISON_METHOD,
				new TableColumnInfo(Messages.getString("comparison.method", locale), TableColumnInfo.NONE, 40, SWT.LEFT));
		tableDefine.add(COMPARISON_VALUE,
				new TableColumnInfo(Messages.getString("comparison.value", locale), TableColumnInfo.NONE, 50, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 110, SWT.LEFT));
		return tableDefine;
	}
}
