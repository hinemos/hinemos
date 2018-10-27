/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.http.viewer;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * 文字列フィルタ一覧テーブル定義情報を取得するクライアント側アクションクラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class GetVariableTableDefine {

	/** 名前 */
	public static final int NAME = 0;

	/** 値 */
	public static final int VALUE = 1;

	/** 現在ページから取得 */
	public static final int MATCHING_WITH_RESPONSE = 2;


	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX = NAME;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;


	/**
	 * 文字列フィルタ一覧のテーブル定義情報を取得します。
	 * 
	 * @return 文字列フィルタ一覧テーブル定義情報
	 */
	public static ArrayList<TableColumnInfo> get() {
		// テーブル情報定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(NAME,
				new TableColumnInfo(Messages.getString("variable.name", locale), TableColumnInfo.NONE, 110, SWT.LEFT));
		tableDefine.add(VALUE,
				new TableColumnInfo(Messages.getString("value", locale), TableColumnInfo.NONE, 110, SWT.LEFT));
		tableDefine.add(MATCHING_WITH_RESPONSE,
				new TableColumnInfo(Messages.getString("monitor.http.scenario.page.obtain.from.current.page", locale), TableColumnInfo.NONE, 110, SWT.LEFT));

		return tableDefine;
	}
}
