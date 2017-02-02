/*

Copyright (C) since 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.monitor.bean;

/**
 * 
 * EventBatchConfirmPropertyの定数部分を切り出した物。
 *
 */
public class EventBatchConfirmConstant {
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

	/** 受信日時（開始）。 */
	public static final String OUTPUT_FROM_DATE = "outputFromDate";
	/** 受信日時（終了）。 */
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

	/** 受信日時。 */
	public static final String OUTPUT_DATE = "outputDate";
	/** 出力日時。 */
	public static final String GENERATION_DATE = "generationDate";

	/** コメント */
	public static final String COMMENT = "comment";
	/**コメントユーザ */
	public static final String COMMENT_USER = "commentUser";


}
