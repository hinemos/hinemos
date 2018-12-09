/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * カレンダ一覧テーブル定義を取得するクライアント側アクションクラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class GetJobMapImageListTableDefine {

	/** アイコンイメージ */
	public static final int ICON_IMAGE = 0;

	/** マネージャ名 */
	public static final int MANAGER_NAME = 1;

	/** UID */
	public static final int ICON_ID = 2;

	/** 説明 */
	public static final int DESCRIPTION = 3;

	/** オーナーロール */
	public static final int OWNER_ROLE = 4;

	/** 作成者 */
	public static final int CREATOR_NAME = 5;

	/** 作成日時 */
	public static final int CREATE_TIME = 6;

	/** 更新者 */
	public static final int MODIFIER_NAME = 7;

	/** 更新日時 */
	public static final int MODIFY_TIME = 8;

	/** ダミー**/
	public static final int DUMMY = 9;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = ICON_ID;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;

	// ----- instance メソッド ----- //

	/**
	 * ジョブマップ用アイコンファイル一覧テーブル定義を取得します。<BR>
	 *
	 * @return
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(ICON_IMAGE,
				new TableColumnInfo(Messages.getString("icon.image", locale), TableColumnInfo.JOBMAP_ICON_IMAGE, 100, SWT.LEFT));
		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(ICON_ID,
				new TableColumnInfo(Messages.getString("icon.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
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
