/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * 割り当てスコープビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetNodeScopeTableDefine {

	/** スコープ */
	public static final int SCOPE = 0;

	/** ダミー**/
	public static final int DUMMY = 1;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = SCOPE;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;


	// ----- instance メソッド ----- //

	/**
	 * 全てのノード一覧を取得します。<BR>
	 *
	 * @return ノード一覧
	 */
	public static ArrayList<TableColumnInfo> get() {
		/** 出力用変数 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		/** メイン処理 */
		tableDefine.add(SCOPE,
				new TableColumnInfo(Messages.getString("scope", locale), TableColumnInfo.NONE, 300, SWT.LEFT));
		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
