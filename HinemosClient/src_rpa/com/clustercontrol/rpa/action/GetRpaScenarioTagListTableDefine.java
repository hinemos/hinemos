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
 * シナリオタグ一覧テーブル定義を取得するクライアント側アクションクラス<BR>
 */
public class GetRpaScenarioTagListTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;

	/** UID */
	public static final int TAG_PATH = 1;

	/** UID */
	public static final int TAG_ID = 2;

	/** 名前 */
	public static final int TAG_NAME = 3;

	/** 説明 */
	public static final int DESCRIPTION = 4;

	/** オーナーロール */
	public static final int OWNER_ROLE = 5;

	/** 作成者 */
	public static final int CREATOR_NAME = 6;

	/** 作成日時 */
	public static final int CREATE_TIME = 7;

	/** 更新者 */
	public static final int MODIFIER_NAME = 8;

	/** 更新日時 */
	public static final int MODIFY_TIME = 9;

	/** ダミー**/
	public static final int DUMMY=10;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = TAG_ID;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;

	// ----- instance メソッド ----- //

	/**
	 * シナリオタグ一覧テーブル定義を取得します。<BR>
	 *
	 * @return
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(TAG_PATH,
				new TableColumnInfo(Messages.getString("rpa.tag.path", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(TAG_ID,
				new TableColumnInfo(Messages.getString("rpa.tag.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(TAG_NAME,
				new TableColumnInfo(Messages.getString("rpa.tag.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 200, SWT.LEFT));
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 130, SWT.LEFT));
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
