/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.action;

import java.util.ArrayList;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * 環境構築ダイアログの変数テーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 6.1.0
 */
public class GetInfraParameterTableDefine {

	/** 名前 */
	public static final int PARAM_ID = 0;
	/** 値 */
	public static final int VALUE = 1;
	/** パスワード表示にする */
	public static final int PASSWORD = 2;
	/** 説明 */
	public static final int DESCRIPTION = 3;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = PARAM_ID;
	/** 初期表示時ソートオーダー (昇順=1, 降順=-1) */
	public static final int SORT_ORDER = 1;

	/**
	 * ジョブ変数タブのテーブル定義を作成する
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();

		tableDefine.add(PARAM_ID,
				new TableColumnInfo(Messages.getString("name"), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(VALUE,
				new TableColumnInfo(Messages.getString("value"), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(PASSWORD,
				new TableColumnInfo(Messages.getString("password"), TableColumnInfo.CHECKBOX, 80, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description"), TableColumnInfo.NONE, 1430, SWT.LEFT));

		return tableDefine;
	}
}
