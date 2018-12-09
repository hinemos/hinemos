/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * ログ[フォーマット]ビュー―
 * テーブル定義を取得するクライアント側アクションクラス<BR>
 *
 */
public class GetLogFormatTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;

	/** UID */
	public static final int FORMAT_ID = 1;

	/** 説明 */
	public static final int DESCRIPTION = 2;

	/** キーリスト */
	public static final int KEY_LIST = 3;
	
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
	public static final int SORT_COLUMN_INDEX2 = FORMAT_ID;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;

	/**
	 * ログフォーマットテーブル定義を取得します。<BR>
	 *
	 * @return
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(FORMAT_ID,
				new TableColumnInfo(Messages.getString("hub.log.format.id", locale), TableColumnInfo.NONE, 150, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 200, SWT.LEFT));
		tableDefine.add(KEY_LIST,
				new TableColumnInfo(Messages.getString("hub.log.format.key", locale), TableColumnInfo.NONE, 250, SWT.LEFT));
		tableDefine.add(OWNER_ROLE,
				new TableColumnInfo(Messages.getString("owner.role.id", locale), TableColumnInfo.NONE, 120, SWT.LEFT));
		tableDefine.add(CREATOR_NAME,
				new TableColumnInfo(Messages.getString("creator.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(CREATE_TIME,
				new TableColumnInfo(Messages.getString("create.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(MODIFIER_NAME,
				new TableColumnInfo(Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(MODIFY_TIME,
				new TableColumnInfo(Messages.getString("update.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));

		return tableDefine;
	}
}
