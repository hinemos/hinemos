/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import java.io.Serializable;

/**
 * CommonTableViewerクラス用のテーブルカラム情報クラス<BR>
 *
 * @version 4.0.0
 * @since 1.0.0
 */
public class TableColumnInfo implements Serializable {
	private static final long serialVersionUID = 5941831973367859876L;

	/** 状態(データタイプの種別) */
	public static final int STATE = 0;

	/** ジョブ(データタイプの種別) */
	public static final int JOB = 1;

	/** ファシリティ(データタイプの種別) */
	public static final int FACILITY = 2;

	/** 重要度(データタイプの種別) */
	public static final int PRIORITY = 3;

	/** 有効/無効(データタイプの種別) */
	public static final int VALID = 4;

	/** 判定対象(データタイプの種別) */
	public static final int JUDGMENT_OBJECT = 5;

	/** 開始条件値(データタイプの種別) */
	public static final int WAIT_RULE_VALUE = 6;

	/** スケジュール(データタイプの種別) */
	public static final int SCHEDULE = 7;

	/** 確認/未確認(データタイプの種別) */
	public static final int CONFIRM = 8;

	/** 待ち条件(データタイプの種別) */
	public static final int WAIT_RULE = 9;

	/** 処理(データタイプの種別) */
	public static final int PROCESS = 10;

	/** 終了状態(データタイプの種別) */
	public static final int END_STATUS = 11;

	/** なし(データタイプの種別) */
	public static final int NONE = -1;

	/** ファシリティID(データタイプの種別) */
	public static final int FACILITY_ID = 12;

	/** ファシリティ名(データタイプの種別) */
	public static final int FACILITY_NAME = 13;

	/** チェックボックス(データタイプの種別) */
	public static final int CHECKBOX = 15;

	/** 曜日(データタイプの種別) */
	public static final int DAY_OF_WEEK = 16;

	/** 予定(データタイプの種別) */
	public static final int SCHEDULE_ON_OFF = 17;

	/** ジョブパラメータ種別 */
	public static final int JOB_PARAM_TYPE = 18;

	/** テキストダイアログ種別 */
	public static final int TEXT_DIALOG = 19;

	/** 通知種別 */
	public static final int NOTIFY_TYPE = 20;

	/** コメント */
	public static final int COMMENT = 21;

	/**コメント入力日時 */
	public static final int COMMENT_DATE = 22;

	/**コメント入力ユーザ */
	public static final int COMMENT_USER = 23;

	/** 収集状態 */
	public static final int COLLECT_STATUS = 24;

	/** マネージャ名 */
	public static final int MANAGER_NAME = 25;

	/** マネージャ名 */
	public static final int DUMMY = 26;

	/** ランタイムジョブ変数種別 */
	public static final int JOB_RUNTIME_PARAM_TYPE = 27;

	/** ジョブマップアイコンイメージ */
	public static final int JOBMAP_ICON_IMAGE = 28;

	/** 承認状態 */
	public static final int APPROVAL_STATUS = 29;
	
	/** 承認結果 */
	public static final int APPROVAL_RESULT = 30;

	/** 判定結果 */
	public static final int DECISION_CONDITION = 31;

	/** 監視間隔(データタイプの種別) */
	public static final int RUN_INTERVAL = 32;

	/** カラム名 */
	private String m_name = null;

	/** カラムデータタイプ */
	private int m_type = 0;

	/** カラム幅 */
	private int m_width = 0;

	/** カラムスタイル */
	private int m_style = 0;

	/** ソートオーダー */
	private int m_order = -1;

	/**
	 * コンストラクタ
	 *
	 * @param name
	 * @param type
	 * @since 1.0.0
	 */
	public TableColumnInfo(String name, int type, int width, int style) {
		m_name = name;
		m_type = type;
		m_width = width;
		m_style = style;
	}

	/**
	 * @return
	 * @since 1.0.0
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * @param name
	 * @since 1.0.0
	 */
	public void setName(String name) {
		m_name = name;
	}

	/**
	 * @return
	 * @since 1.0.0
	 */
	public int getType() {
		return m_type;
	}

	/**
	 * @param type
	 * @since 1.0.0
	 */
	public void setType(int type) {
		m_type = type;
	}

	/**
	 * @return
	 * @since 1.0.0
	 */
	public int getWidth() {
		return m_width;
	}

	/**
	 * @param width
	 * @since 1.0.0
	 */
	public void setWidth(int width) {
		m_width = width;
	}

	/**
	 * @return
	 * @since 1.0.0
	 */
	public int getStyle() {
		return m_style;
	}

	/**
	 * @param style
	 * @since 1.0.0
	 */
	public void setStyle(int style) {
		m_style = style;
	}

	/**
	 * @return
	 * @since 1.0.0
	 */
	public int getOrder() {
		return m_order;
	}

	/**
	 * @param order
	 * @since 1.0.0
	 */
	public void setOrder(int order) {
		m_order = order;
	}
}
