/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * SDML制御設定一覧のテーブル定義情報を取得するクライアント側アクションクラス
 *
 */
public class GetSdmlControlSettingListTableDefine {

	/** マネージャ */
	public static final int MANAGER_NAME = 0;

	/** アプリケーションID */
	public static final int APPLICATION_ID = 1;

	/** 説明 */
	public static final int DESCRIPTION = 2;

	/** ファシリティID */
	public static final int FACILITY_ID = 3;

	/** スコープ */
	public static final int SCOPE = 4;

	/** 有効／無効 */
	public static final int VALID_FLG = 5;

	/** 収集 */
	public static final int COLLECTOR_FLG = 6;

	/** オーナーロール */
	public static final int OWNER_ROLE = 7;

	/** 新規作成ユーザ */
	public static final int CREATE_USER = 8;

	/** 作成日時 */
	public static final int CREATE_TIME = 9;

	/** 最終変更ユーザ */
	public static final int UPDATE_USER = 10;

	/** 最終変更日時 */
	public static final int UPDATE_TIME = 11;

	/** ダミー */
	public static final int DUMMY = 12;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = APPLICATION_ID;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;

	/**
	 * SDML制御設定のテーブル定義情報を返します
	 * 
	 * @return
	 */
	public static ArrayList<TableColumnInfo> get() {
		// テーブル情報定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME, new TableColumnInfo(Messages.getString("facility.manager", "Manager", locale),
				TableColumnInfo.MANAGER_NAME, 100, SWT.LEFT));
		tableDefine.add(APPLICATION_ID,
				new TableColumnInfo(Messages.getString("application.id", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(FACILITY_ID,
				new TableColumnInfo(Messages.getString("facility.id", locale), TableColumnInfo.NONE, 110, SWT.LEFT));
		tableDefine.add(SCOPE,
				new TableColumnInfo(Messages.getString("scope", locale), TableColumnInfo.FACILITY, 200, SWT.LEFT));
		tableDefine.add(VALID_FLG,
				new TableColumnInfo(Messages.getString("valid", locale) + "/" + Messages.getString("invalid", locale),
						TableColumnInfo.VALID, 80, SWT.LEFT));
		tableDefine.add(COLLECTOR_FLG,
				new TableColumnInfo(Messages.getString("collector.valid.name", locale), TableColumnInfo.VALID, 40, SWT.LEFT));
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(CREATE_USER,
				new TableColumnInfo(Messages.getString("creator.name", locale), TableColumnInfo.NONE, 110, SWT.LEFT));
		tableDefine.add(CREATE_TIME,
				new TableColumnInfo(Messages.getString("create.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(UPDATE_USER,
				new TableColumnInfo(Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 110, SWT.LEFT));
		tableDefine.add(UPDATE_TIME,
				new TableColumnInfo(Messages.getString("update.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(DUMMY, new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
