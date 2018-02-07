/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.bean;

import java.util.ArrayList;



/**
 * レポーティング機能の種別の定義を定数として格納するクラスです。
 *
 * @version 5.0.a
 */
public class ReportingTypeConstant {
	// schedule_type スケジュール種別 (ScheduleConstant とは異なる)
	public static final int SCHEDULE_TYPE_DAY = 0;
	public static final int SCHEDULE_TYPE_WEEK = 1;
	public static final int SCHEDULE_TYPE_MONTH = 2;
	
	// 出力ファイル形式
	public static final int TYPE_OUTPUT_PDF = 0;
	public static final int TYPE_OUTPUT_XLSX = 1;
	public static final int TYPE_OUTPUT_XLS = 2;
	
	public static final String STR_OUTPUT_PDF = "pdf";
	public static final String STR_OUTPUT_XLSX = "xlsx";
	public static final String STR_OUTPUT_XLS = "xls";  // xlsはひとまず作っておくが、開放はしない
	
	// 出力ファイル形式を文字列から数値に変換
	public static int outputStringToType(String str) {
		if (str.equals(STR_OUTPUT_PDF)) {
			return TYPE_OUTPUT_PDF;
		} else if (str.equals(STR_OUTPUT_XLSX)) {
			return TYPE_OUTPUT_XLSX;
		} else if (str.equals(STR_OUTPUT_XLS)) {
			return TYPE_OUTPUT_XLS;
		}
		return -1;
	}
	
	// 出力ファイル形式を数値から文字列に変換
	public static String outputTypeToString(int type) {
		if (type == TYPE_OUTPUT_PDF) {
			return STR_OUTPUT_PDF;
		} else if (type == TYPE_OUTPUT_XLSX) {
			return STR_OUTPUT_XLSX;
		} else if (type == TYPE_OUTPUT_XLS) {
			return STR_OUTPUT_XLS;
		}
		return "";
	}
	
	// 出力ファイル形式の文字列を返す
	public static ArrayList<String> getTypeStrList() {
		
		ArrayList<String> list = new ArrayList<String>();
		list.add(STR_OUTPUT_PDF);
		list.add(STR_OUTPUT_XLSX);
//		list.add(STR_OUTPUT_XLS);
		
		return list;
	}

}
