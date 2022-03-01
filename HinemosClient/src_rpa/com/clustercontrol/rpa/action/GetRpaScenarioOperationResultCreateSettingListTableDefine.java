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
 * シナリオ実績作成設定一覧テーブル定義を取得するクライアント側アクションクラス<BR>
 */
public class GetRpaScenarioOperationResultCreateSettingListTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;

	/** シナリオ実績作成設定ID */
	public static final int SETTING_ID = 1;

	/** 説明 */
	public static final int DESCRIPTION = 2;

	/** スコープ */
	public static final int SCOPE = 3;

	/** 間隔 */
	public static final int INTERVAL = 4;

	/** 有効/無効 */
	public static final int VALID_FLG = 5;

	/** カレンダ */
	public static final int CALENDAR = 6;

	/** オーナーロール */
	public static final int OWNER_ROLE = 7;

	/** 作成者 */
	public static final int CREATOR_NAME = 8;

	/** 作成日時 */
	public static final int CREATE_TIME = 9;

	/** 更新者 */
	public static final int MODIFIER_NAME = 10;

	/** 更新日時 */
	public static final int MODIFY_TIME = 11;

	/** ダミー**/
	public static final int DUMMY = 12;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = SETTING_ID;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;

	// ----- instance メソッド ----- //

	/**
	 * カレンダ一覧テーブル定義を取得します。<BR>
	 *
	 * @return
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(SETTING_ID,
				new TableColumnInfo(Messages.getString("RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING_ID", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("DESCRIPTION", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(SCOPE,
				new TableColumnInfo(Messages.getString("SCOPE", locale), TableColumnInfo.NONE, 120, SWT.LEFT));
		tableDefine.add(INTERVAL,
				new TableColumnInfo(Messages.getString("CREATE_INTERVAL", locale), TableColumnInfo.NONE, 70, SWT.LEFT));
		tableDefine.add(VALID_FLG,
				new TableColumnInfo(Messages.getString("valid", locale) + "/" + Messages.getString("invalid", locale), TableColumnInfo.NONE, 80, SWT.LEFT));		
		tableDefine.add(CALENDAR,
				new TableColumnInfo(Messages.getString("CALENDAR", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("OWNER_ROLE_ID", locale), TableColumnInfo.NONE, 120, SWT.LEFT));
		tableDefine.add(CREATOR_NAME,
				new TableColumnInfo(Messages.getString("creator.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(CREATE_TIME,
				new TableColumnInfo(Messages.getString("create.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(MODIFIER_NAME,
				new TableColumnInfo(Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(MODIFY_TIME,
				new TableColumnInfo(Messages.getString("update.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(DUMMY, new TableColumnInfo("", TableColumnInfo.DUMMY, 200, SWT.LEFT));

		return tableDefine;
	}
}
