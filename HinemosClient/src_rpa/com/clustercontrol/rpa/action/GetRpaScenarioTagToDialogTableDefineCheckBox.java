/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * ダイアログ用タグ情報一覧のテーブル定義情報を取得するクライアント側アクションクラス<BR>
 */
public class GetRpaScenarioTagToDialogTableDefineCheckBox {

	/** チェックボックス。 */
	public static final int SELECTION = 0;

	/** タグID */
	public static final int TAG_ID = 1;

	/** タグ名 */
	public static final int TAG_NAME = 2;

	/** 説明 */
	public static final int DESCRIPTION = 3;

	/** ダミー**/
	public static final int DUMMY = 4;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX = TAG_ID;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;

	/**
	 * タグ情報一覧のテーブル定義情報を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 */
	public static ArrayList<TableColumnInfo> get() {
		/** テーブル情報定義配列 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(SELECTION,
				new TableColumnInfo("", TableColumnInfo.CHECKBOX, 25, SWT.LEFT));
		tableDefine.add(TAG_ID,
				new TableColumnInfo(Messages.getString("view.rpa.scenario.operation.result.search.column.tag.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(TAG_NAME,
				new TableColumnInfo(Messages.getString("view.rpa.scenario.operation.result.search.column.tag.name", locale), TableColumnInfo.NONE, 250, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 200, SWT.LEFT));

		return tableDefine;
	}
}
