/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
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
 * ユーザ任意情報一覧テーブル定義情報を取得するクライアント側アクションクラス<BR>
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
public class GetNodeCustomTableDefine {

	/** ユーザ任意情報ID. */
	public static final int SETTING_CUSTOM_ID = 0;

	/** ユーザ任意情報名. */
	public static final int DISPLAY_NAME = 1;

	/** コマンド. */
	public static final int COMMAND = 2;

	/** 説明. */
	public static final int DESCRIPTION = 3;

	/** 実行ユーザ */
	public static final int EXEC_USER = 4;

	/** 有効・無効フラグ. */
	public static final int VALID_FLG = 5;

	/** 初期表示時ソートカラム. */
	public static final int SORT_COLUMN_INDEX = SETTING_CUSTOM_ID;

	/** 初期表示時ソートオーダー. */
	public static final int SORT_ORDER = 1;

	/**
	 * ユーザ任意情報一覧のテーブル定義情報を取得します。
	 * 
	 * @return ユーザ任意情報一覧テーブル定義情報
	 */
	public static ArrayList<TableColumnInfo> get() {
		// テーブル情報定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(SETTING_CUSTOM_ID, //
				new TableColumnInfo(Messages.getString("node.custom.id", locale), //
						TableColumnInfo.NONE, 110, SWT.LEFT));
		tableDefine.add(DISPLAY_NAME, //
				new TableColumnInfo(Messages.getString("node.custom.name", locale), //
						TableColumnInfo.NONE, 110, SWT.LEFT));
		tableDefine.add(COMMAND, //
				new TableColumnInfo(Messages.getString("command", locale), //
						TableColumnInfo.NONE, 60, SWT.LEFT));
		tableDefine.add(DESCRIPTION, //
				new TableColumnInfo(Messages.getString("description", locale), //
						TableColumnInfo.NONE, 50, SWT.LEFT));
		tableDefine.add(EXEC_USER, //
				new TableColumnInfo(Messages.getString("effective.user", locale), //
						TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(VALID_FLG, //
				new TableColumnInfo(Messages.getString("valid", locale) + "/" + Messages.getString("invalid", locale),
						TableColumnInfo.VALID, 70, SWT.LEFT));

		return tableDefine;
	}
}
