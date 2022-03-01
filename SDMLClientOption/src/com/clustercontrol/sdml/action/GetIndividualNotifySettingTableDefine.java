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
 * 個別通知設定のテーブル定義情報を取得するクライアント側アクションクラス
 *
 */
public class GetIndividualNotifySettingTableDefine {

	/** SDML監視種別 */
	public static final int SDML_MONITOR_TYPE = 0;

	/** 通知ID */
	public static final int NOTIFY_ID = 1;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX = SDML_MONITOR_TYPE;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;

	/**
	 * 個別通知設定のテーブル定義情報を返します
	 * 
	 * @return
	 */
	public static ArrayList<TableColumnInfo> get() {
		// テーブル情報定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(SDML_MONITOR_TYPE, new TableColumnInfo(Messages.getString("sdml.monitor.type", locale),
				TableColumnInfo.NONE, 160, SWT.LEFT));
		tableDefine.add(NOTIFY_ID,
				new TableColumnInfo(Messages.getString("notify.id", locale), TableColumnInfo.NONE, 280, SWT.LEFT));

		return tableDefine;
	}
}
