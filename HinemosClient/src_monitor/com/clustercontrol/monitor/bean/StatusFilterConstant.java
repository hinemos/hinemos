/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

/**
 * 
 * StatusFilterPropertyの定数部分を切り出した物。
 *
 */
public class StatusFilterConstant {

	/**
	 * 重要度。
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public static final String PRIORITY = "priority";
	/** 重要度（危険）。 */
	public static final String PRIORITY_CRITICAL = "priorityCritical";
	/** 重要度（警告）。 */
	public static final String PRIORITY_WARNING = "priorityWarning";
	/** 重要度（情報）。 */
	public static final String PRIORITY_INFO = "priorityInfo";
	/** 重要度（不明）。 */
	public static final String PRIORITY_UNKNOWN = "priorityUnknown";
	
	/** 最終変更日時（開始）。 */
	public static final String OUTPUT_FROM_DATE = "outputFromDate";
	/** 最終変更日時（終了）。 */
	public static final String OUTPUT_TO_DATE = "outputToDate";
	/** 出力日時（開始）。 */
	public static final String GENERATION_FROM_DATE = "generationFromDate";
	/** 出力日時（終了）。 */
	public static final String GENERATION_TO_DATE = "generationToDate";

	/** 監視項目ID。 */
	public static final String MONITOR_ID = "monitorId";
	/** 監視詳細。 */
	public static final String MONITOR_DETAIL_ID = "monitorDetailId";

	/**
	 * ファシリティのターゲット 。
	 * @see com.clustercontrol.repository.bean.FacilityTargetConstant
	 */
	public static final String FACILITY_TYPE = "facilityType";
	/** アプリケーション。 */
	public static final String APPLICATION = "application";
	/** メッセージ。 */
	public static final String MESSAGE = "message";

	/** 最終変更日時。 */
	public static final String OUTPUT_DATE = "outputDate";
	/** 出力日時。 */
	public static final String GENERATION_DATE = "generationDate";
	/** オーナーロールID。 */
	public static final String OWNER_ROLE_ID = "ownerRoleId";

}
