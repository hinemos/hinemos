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
 * RPAシナリオ実績[シナリオ一覧]情報を取得するクライアント側アクションクラス<BR>
 */
public class GetRpaScenarioListTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;

	/** RPA種別 */
	public static final int RPATOOL_NAME = 1;

	/** シナリオID */
	public static final int SCENARIO_ID = 2;

	/** シナリオ名 */
	public static final int SCENARIO_NAME = 3;

	/** シナリオ識別 */
	public static final int SCENARIO_IDENTIFY_STRING = 4;

	/** 説明 */
	public static final int DESCRIPTION = 5;

	/** オーナーロールID**/
	public static final int OWNER_ROLE_ID = 6;

	/** 新規作成ユーザ */
	public static final int CREATOR_NAME = 7;

	/** 作成日時 */
	public static final int CREATE_TIME = 8;

	/** 最終更新ユーザ */
	public static final int MODIFIER_NAME = 9;

	/** 最終更新日時 */
	public static final int MODIFY_TIME = 10;

	/** ダミー**/
	public static final int DUMMY = 11;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = SCENARIO_ID;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;

	/**
	 *RPAシナリオ実績[シナリオ一覧]のテーブル定義情報を返します。
	 *
	 * @return RPAシナリオ実績[シナリオ一覧]テーブル定義情報
	 */
	public static ArrayList<TableColumnInfo> get() {
		// テーブル情報定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		// マネージャ名
		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", "Manager", locale), TableColumnInfo.MANAGER_NAME, 100, SWT.LEFT));

		// RPA識別
		tableDefine.add(RPATOOL_NAME,
				new TableColumnInfo(Messages.getString("rpa.scenario.rpa.tool.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT));

		// シナリオID
		tableDefine.add(SCENARIO_ID,
				new TableColumnInfo(Messages.getString("rpa.scenario.id", locale), TableColumnInfo.NONE, 200, SWT.LEFT));

		// シナリオ名
		tableDefine.add(SCENARIO_NAME,
				new TableColumnInfo(Messages.getString("rpa.scenario.name", locale), TableColumnInfo.NONE, 200, SWT.LEFT));

		// シナリオ識別
		tableDefine.add(SCENARIO_IDENTIFY_STRING,
				new TableColumnInfo(Messages.getString("rpa.scenario.identify.string", locale), TableColumnInfo.NONE, 200, SWT.LEFT));

		// 説明
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 200, SWT.LEFT));

		// オーナーロールID
		tableDefine.add(OWNER_ROLE_ID,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));

		// 新規作成ユーザ
		tableDefine.add(CREATOR_NAME, new TableColumnInfo( Messages.getString("creator.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT) );
		
		// 作成日時
		tableDefine.add(CREATE_TIME, new TableColumnInfo( Messages.getString("create.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT) );
		
		// 最終更新ユーザ
		tableDefine.add(MODIFIER_NAME, new TableColumnInfo( Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT) );
		
		// 最終更新日時
		tableDefine.add(MODIFY_TIME, new TableColumnInfo( Messages.getString("update.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT) );
		
		// ダミー
		tableDefine.add(DUMMY,
				new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
