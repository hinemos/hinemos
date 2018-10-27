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
public class GetPageTableDefine {

	/** 順序。 */
	public static final int ORDER_NO = 0;

	/** URL */
	public static final int URL = 1;

	/** 説明 */
	public static final int DESCRIPTION = 2;

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
		tableDefine.add(URL,
				new TableColumnInfo(Messages.getString("request.url", locale), TableColumnInfo.NONE, 120, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 120, SWT.LEFT));

		return tableDefine;
	}
}
