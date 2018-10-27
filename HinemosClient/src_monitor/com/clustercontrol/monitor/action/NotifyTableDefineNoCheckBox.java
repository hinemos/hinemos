/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.action;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;

import com.clustercontrol.bean.TableColumnInfo;
import com.clustercontrol.util.Messages;

/**
 * 通知情報一覧のテーブル定義情報を取得するクライアント側アクションクラス<BR>
 *
 * @version 4.0.0
 * @since 1.0.0
 */
public class NotifyTableDefineNoCheckBox {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;

	/** 通知ID。 */
	public static final int NOTIFY_ID = 1;

	/** 説明。 */
	public static final int DESCRIPTION = 2;

	/** 通知タイプ。
	 *
	 * @see com.clustercontrol.bean.NotifyTypeConstant
	 */
	public static final int NOTIFY_TYPE = 3;

	/** 有効／無効。*/
	public static final int VALID_FLG = 4;

	/** 新規作成ユーザ。 */
	public static final int CREATE_USER = 5;

	/** 作成日時。 */
	public static final int CREATE_TIME = 6;

	/** 最終変更ユーザ。 */
	public static final int UPDATE_USER = 7;

	/** 最終変更日時。 */
	public static final int UPDATE_TIME = 8;

	/** 初期表示時ソートカラム。 */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = NOTIFY_ID;

	/** 初期表示時ソートオーダー。 */
	public static final int SORT_ORDER = 1;

	/**
	 * 通知情報一覧のテーブル定義情報を返します。<BR>
	 * マネージャにSessionBean経由でアクセスします。
	 *
	 * @return テーブル定義情報（{@link com.clustercontrol.bean.TableColumnInfo}のリスト）
	 *
	 * @see com.clustercontrol.notify.ejb.session.NotifyController
	 * @see com.clustercontrol.notify.ejb.session.NotifyControllerBean#getNotifyListTableDefine(java.util.Locale)
	 */
	public static ArrayList<TableColumnInfo> get() {
		/** テーブル情報定義配列 */
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(NOTIFY_ID,
				new TableColumnInfo(Messages.getString("notify.id", locale), TableColumnInfo.NONE, 170, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 180, SWT.LEFT));
		tableDefine.add(NOTIFY_TYPE,
				new TableColumnInfo(Messages.getString("notify.type.list", locale), TableColumnInfo.NOTIFY_TYPE, 100, SWT.LEFT));
		tableDefine.add(VALID_FLG,
				new TableColumnInfo(Messages.getString("valid", locale) + "/" + Messages.getString("invalid", locale), TableColumnInfo.VALID, 80, SWT.LEFT));
		tableDefine.add(CREATE_USER,
				new TableColumnInfo(Messages.getString("creator.name", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(CREATE_TIME,
				new TableColumnInfo(Messages.getString("create.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));
		tableDefine.add(UPDATE_USER,
				new TableColumnInfo(Messages.getString("modifier.name", locale), TableColumnInfo.NONE, 80, SWT.LEFT));
		tableDefine.add(UPDATE_TIME,
				new TableColumnInfo(Messages.getString("update.time", locale), TableColumnInfo.NONE, 140, SWT.LEFT));

		return tableDefine;
	}
}
