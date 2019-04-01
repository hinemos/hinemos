/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.etc.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * ノード一覧のテーブル定義を取得するクライアント側アクションクラス<BR>
 * 
 * @version 6.2.0
 */
public class NodeListTableDefine {
	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;

	/** ファシリティID */
	public static final int FACILITY_ID = 1;

	/** ファシリティ名 */
	public static final int FACILITY_NAME = 2;

	/** プラットフォーム */
	public static final int PLATFORM = 3;

	/** オーナーロールID */
	public static final int OWNER_ROLE = 4;

	/** 登録日時 */
	public static final int CREATED_DATE = 5;

	/** 最終更新日時 */
	public static final int LAST_UPDATE_DATE = 6;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = MANAGER_NAME ;

	public static final int SORT_COLUMN_INDEX_SECOND = FACILITY_ID;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;

	// ----- instance メソッド ----- //

	/**
	 *スコープ一覧テーブル定義を取得します。<BR>
	 * 
	 * @return ノード一覧
	 */
	public static ArrayList<TableColumnInfo> get() {
		/** 出力用変数 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		/** メイン処理 */
		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.managername", locale), TableColumnInfo.MANAGER_NAME, 100, SWT.LEFT));
		tableDefine.add(FACILITY_ID,
				new TableColumnInfo(Messages.getString("facility.id", locale), TableColumnInfo.FACILITY_ID, 150, SWT.LEFT));
		tableDefine.add(FACILITY_NAME,
				new TableColumnInfo(Messages.getString("facility.name", locale), TableColumnInfo.FACILITY_NAME, 150, SWT.LEFT));
		tableDefine.add(PLATFORM,
				new TableColumnInfo(Messages.getString("platform.family.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(CREATED_DATE,
				new TableColumnInfo(Messages.getString("create.time", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(LAST_UPDATE_DATE,
				new TableColumnInfo(Messages.getString("word.update_date", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		return tableDefine;
	}
}