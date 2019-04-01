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
 * 一覧テーブルの定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class GetNodeConfigSettingListTableDefine {

	/** マネージャ */
	public static final int MANAGER_NAME = 0;

	/** 構成情報収集ID */
	public static final int GET_CONFIG_ID = 1;

	/** 構成情報収集名 */
	public static final int GET_CONFIG_NAME = 2;

	/** 説明 */
	public static final int DESCRIPTION = 3;
	
	/** 構成情報収集対象 */
	public static final int GET_CONFIG_TARGET = 4;
	
	/** 間隔 */
	public static final int RUN_INTERVAL = 5;
	
	/** ファシリティID */
	public static final int FACILITY_ID = 6;
	
	/** スコープ */
	public static final int SCOPE = 7;
	
	/** カレンダ */
	public static final int CALENDAR_ID = 8;
	
	/** 有効／無効 */
	public static final int VALID_FLG = 9;
	
	/** オーナーロール */
	public static final int OWNER_ROLE = 10;
	
	/** 新規作成ユーザ */
	public static final int CREATE_USER = 11;
	
	/** 作成日時 */
	public static final int CREATE_TIME = 12;
	
	/** 最終変更ユーザ */
	public static final int UPDATE_USER = 13;
	
	/** 最終変更日時 */
	public static final int UPDATE_TIME = 14;
	
	/** ダミー */
	public static final int DUMMY = 15;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = GET_CONFIG_ID;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;


	// ----- instance メソッド ----- //

	/**
	 * 全ての構成情報収集一覧を取得します。
	 *
	 * @return 構成情報収集一覧
	 */
	public static ArrayList<TableColumnInfo> get() {
		/** 出力用変数 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		/** メイン処理 */
		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", "Manager", locale), TableColumnInfo.MANAGER_NAME, 100, SWT.LEFT));
		tableDefine.add(GET_CONFIG_ID,
				new TableColumnInfo(Messages.getString("getconfig.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(GET_CONFIG_NAME,
				new TableColumnInfo(Messages.getString("getconfig.name", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 200, SWT.LEFT));
		tableDefine.add(GET_CONFIG_TARGET,
				new TableColumnInfo(Messages.getString("getconfig.target", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(RUN_INTERVAL,
				new TableColumnInfo(Messages.getString("run.interval", locale), TableColumnInfo.RUN_INTERVAL, 50, SWT.LEFT));
		tableDefine.add(FACILITY_ID,
				new TableColumnInfo(Messages.getString("facility.id", locale), TableColumnInfo.NONE, 120, SWT.LEFT));
		tableDefine.add(SCOPE,
				new TableColumnInfo(Messages.getString("scope", locale), TableColumnInfo.FACILITY, 120, SWT.LEFT));
		tableDefine.add(CALENDAR_ID,
				new TableColumnInfo(Messages.getString("calendar", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(VALID_FLG,
				new TableColumnInfo(Messages.getString("valid", locale) 
						+ "/" + Messages.getString("invalid", locale), TableColumnInfo.VALID, 80, SWT.LEFT));
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
		tableDefine.add(CREATE_USER,
				new TableColumnInfo(Messages.getString("creator.name", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(CREATE_TIME,
				new TableColumnInfo(Messages.getString("create.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(UPDATE_USER,
				new TableColumnInfo(Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(UPDATE_TIME,
				new TableColumnInfo(Messages.getString("update.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
