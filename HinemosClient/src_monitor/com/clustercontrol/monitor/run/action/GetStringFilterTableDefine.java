/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * 文字列フィルタ一覧テーブル定義情報を取得するクライアント側アクションクラス<BR>
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
public class GetStringFilterTableDefine {

	/** 順序。 */
	public static final int ORDER_NO = 0;

	/**
	 * 処理。 */
	public static final int PROCESS_TYPE = 1;

	/** 重要度。 */
	public static final int PRIORITY = 2;

	/** パターンマッチ文字列。 */
	public static final int PATTERN_STRING = 3;

	/** 説明。 */
	public static final int DESCRIPTION = 4;

	/** 有効・無効フラグ。 */
	public static final int VALID_FLG = 5;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX = ORDER_NO;

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

		tableDefine.add(ORDER_NO,
				new TableColumnInfo(Messages.getString("order", locale), TableColumnInfo.NONE, 40, SWT.RIGHT));
		tableDefine.add(PROCESS_TYPE,
				new TableColumnInfo(Messages.getString("process", locale), TableColumnInfo.PROCESS, 40, SWT.LEFT));
		tableDefine.add(PRIORITY,
				new TableColumnInfo(Messages.getString("priority", locale), TableColumnInfo.PRIORITY, 50, SWT.LEFT));
		tableDefine.add(PATTERN_STRING,
				new TableColumnInfo(Messages.getString("pattern.matching.expression", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 110, SWT.LEFT));
		tableDefine.add(VALID_FLG,
				new TableColumnInfo(Messages.getString("valid", locale) + "/" + Messages.getString("invalid", locale), TableColumnInfo.VALID, 70, SWT.LEFT));;

				return tableDefine;
	}
}
