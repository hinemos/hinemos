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
 * 収集蓄積[転送]ビュー―
 * テーブル定義を取得するクライアント側アクションクラス<BR>
 *
 */
public class GetTransferTableDefine {

	/** マネージャ名 */
	public static final int MANAGER_NAME = 0;

	/** ID */
	public static final int TRANSFER_ID = 1;

	/** 説明 */
	public static final int DESCRIPTION = 2;
	
	/** データ種別 **/
	public static final int EXPORT_DATA_TYPE = 3;
	
	/** 受け渡し先種別ID **/
	public static final int DEST_TYPE_ID = 4;
	
	/** 転送種別 **/
	public static final int TRANS_TYPE = 5;
	
	/** 転送間隔 **/
	public static final int INTERVAL = 6;

	/** カレンダ */
	public static final int CALENDAR = 7;
	
	/** 有効・無効 */
	public static final int VALID = 8;

	/** オーナーロール */
	public static final int OWNER_ROLE = 9;

	/** 作成者 */
	public static final int CREATOR_NAME = 10;

	/** 作成日時 */
	public static final int CREATE_TIME = 11;

	/** 更新者 */
	public static final int MODIFIER_NAME = 12;

	/** 更新日時 */
	public static final int MODIFY_TIME = 13;

	/** ダミー**/
	public static final int DUMMY = 14;

	/** 初期表示時ソートカラム */
	public static final int SORT_COLUMN_INDEX1 = MANAGER_NAME;
	public static final int SORT_COLUMN_INDEX2 = TRANSFER_ID;

	/** 初期表示時ソートオーダー */
	public static final int SORT_ORDER = 1;

	/**
	 * 収集蓄積[転送]テーブル定義を取得します。<BR>
	 *
	 * @return
	 */
	public static ArrayList<TableColumnInfo> get() {
		//テーブル定義配列
		ArrayList<TableColumnInfo> tableDefine = new ArrayList<TableColumnInfo>();
		Locale locale = Locale.getDefault();

		tableDefine.add(MANAGER_NAME,
				new TableColumnInfo(Messages.getString("facility.manager", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(TRANSFER_ID,
				new TableColumnInfo(Messages.getString("hub.log.transfer.id", locale), TableColumnInfo.NONE, 120, SWT.LEFT));
		tableDefine.add(DESCRIPTION,
				new TableColumnInfo(Messages.getString("description", locale), TableColumnInfo.NONE, 200, SWT.LEFT));
		tableDefine.add(EXPORT_DATA_TYPE,
				new TableColumnInfo(Messages.getString("hub.log.transfer.data.type", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(DEST_TYPE_ID,
				new TableColumnInfo(Messages.getString("hub.log.transfer.dest.type.id", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(TRANS_TYPE,
				new TableColumnInfo(Messages.getString("hub.log.transfer.trans.type", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(INTERVAL,
				new TableColumnInfo(Messages.getString("hub.log.transfer.interval", locale), TableColumnInfo.NONE, 70, SWT.LEFT));
		tableDefine.add(CALENDAR,
				new TableColumnInfo(Messages.getString("calendar", locale), TableColumnInfo.NONE, 100, SWT.LEFT));
		tableDefine.add(VALID,
				new TableColumnInfo(Messages.getString("valid", locale) + "/" + Messages.getString("invalid", locale), TableColumnInfo.NONE, 70, SWT.LEFT));
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
