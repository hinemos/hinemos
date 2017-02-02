/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * 環境構築[モジュール]ビューのテーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class GetInfraModuleTableDefine {

	/** 順序 */
	public static final int ORDER_NO = 0;
	/** モジュールID */
	public static final int MODULE_ID = 1;
	/** モジュール名 */
	public static final int MODULE_NAME = 2;
	/** モジュールタイプ */
	public static final int MODULE_TYPE = 3;
	/** 有効・無効 */
	public static final int VALID = 4;
	/** 詳細 */
	public static final int DETAIL = 5;
	/** チェック状態 */
	public static final int CHECK_CONDITION = 6;
	/** ダミー**/
	public static final int DUMMY=7;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = ORDER_NO;
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

		tableDefine.add(ORDER_NO,
				new TableColumnInfo(Messages.getString("order", locale), TableColumnInfo.NONE, 50, SWT.LEFT));
		tableDefine.add(MODULE_ID,
				new TableColumnInfo(Messages.getString("infra.module.id", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(MODULE_NAME,
				new TableColumnInfo(Messages.getString("infra.module.name", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(MODULE_TYPE,
				new TableColumnInfo(Messages.getString("infra.module.type", locale), TableColumnInfo.NONE, 120, SWT.LEFT));
		tableDefine.add(VALID,
				new TableColumnInfo(Messages.getString("valid", locale) + "/" + Messages.getString("invalid", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(DETAIL,
				new TableColumnInfo(Messages.getString("detail", locale), TableColumnInfo.NONE, 200, SWT.LEFT));
		tableDefine.add(CHECK_CONDITION,
				new TableColumnInfo(Messages.getString("infra.management.check.state", locale), TableColumnInfo.NONE, 400, SWT.LEFT));
		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
