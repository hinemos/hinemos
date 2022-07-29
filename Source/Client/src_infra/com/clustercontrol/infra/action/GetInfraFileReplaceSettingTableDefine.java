/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * 環境構築[配布モジュール]のファイルない変数を置換するテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class GetInfraFileReplaceSettingTableDefine {

	/** 検索文字列 */
	public static final int SEARCH_WORD = 0;
	/** 置換文字列 */
	public static final int REPLACEMENT_WORD = 1;
	/** ダミー**/
	public static final int DUMMY=2;


	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = SEARCH_WORD;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;

	/**
	 * 環境構築[モジュール]ビューのテーブル定義を取得する
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(SEARCH_WORD,
				new TableColumnInfo(Messages.getString("infra.management.search.words", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(REPLACEMENT_WORD,
				new TableColumnInfo(Messages.getString("infra.management.replacement.words", locale), TableColumnInfo.NONE, 250, SWT.LEFT));
		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
