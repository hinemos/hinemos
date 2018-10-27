/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * ジョブ収集値表示名一覧のテーブル定義情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 6.1.0
 */
public class GetCollectItemJobTableDefine {

	/** チェックボックス */
	public static final int SELECTION = 0;
	/** 収集値表示名 */
	public static final int COLLECT_ITEM_NAME = 1;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = COLLECT_ITEM_NAME;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;

	/**
	 * ジョブ収集値表示名一覧のテーブル定義情報を返します。<BR>
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 */
	public static ArrayList<TableColumnInfo> get() {
		/** テーブル情報定義配列 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(SELECTION,
				new TableColumnInfo("", TableColumnInfo.CHECKBOX, 25, SWT.LEFT));
		tableDefine.add(COLLECT_ITEM_NAME,
				new TableColumnInfo(Messages.getString("collection.display.name", locale),
						TableColumnInfo.NONE, 300, SWT.LEFT));
		return tableDefine;
	}
}
