/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.bean;

import com.clustercontrol.util.Messages;

/**
 * グラフ種別の定数クラス
 *
 * @version 6.1.0
 *
 */
public class GraphTypeConstant {
	
	/** 折れ線グラフ */
	public static final int LINE = 1;
	private static final String LINE_STR = Messages.getString("collection.graph.line");

	/** 折れ線(監視項目で集約) */
	public static final int LINE_SUMMARIZED = 2;
	private static final String LINE_SUMMARIZED_STR 
		= Messages.getString("collection.graph.line") + "(" + Messages.getString("collection.graph.summarizedinmonitorid") + ")";

	/** 積み上げ面 */
	public static final int AREA = 3;
	private static final String AREA_STR =Messages.getString("collection.graph.area");

	/** 散布図 */
	public static final int SCATTER = 6;
	private static final String SCATTER_STR 
		= Messages.getString("collection.graph.scatter");

	/** 散布図(集約) */
	public static final int SCATTER_SUMMARIZED = 7;
	private static final String SCATTER_SUMMARIZED_STR 
		= Messages.getString("collection.graph.scatter") + "(" + Messages.getString("collection.graph.summarized") + ")";

	/** 円グラフ */
	public static final int PIE = 4;
	private static final String PIE_STR = Messages.getString("collection.graph.pie");

	/** 円グラフ(監視項目で集約) */
	public static final int PIE_SUMMARIZED = 5;
	private static final String PIE_SUMMARIZED_STR
		= Messages.getString("collection.graph.pie") + "(" + Messages.getString("collection.graph.summarizedinmonitorid") + ")";

	/** 棒線 */
	public static final int STACKEDBAR = 8;
	private static final String STACKEDBAR_STR = Messages.getString("collection.graph.stackedbar");

	/** 棒線(監視項目で集約) */
	public static final int STACKEDBAR_SUMMARIZED = 9;
	private static final String STACKEDBAR_SUMMARIZED_STR 
		= Messages.getString("collection.graph.stackedbar") + "(" + Messages.getString("collection.graph.summarizedinmonitorid") + ")";

	/** 区切り */
	private static final String NONE_STR = "--------------------";


	public static Integer stringToType(String typeStr) {
		if (typeStr.equals(LINE_STR)) {
			return LINE;
		} else if (typeStr.equals(LINE_SUMMARIZED_STR)) {
			return LINE_SUMMARIZED;
		} else if (typeStr.equals(AREA_STR)) {
			return AREA;
		} else if (typeStr.equals(SCATTER_STR)) {
			return SCATTER;
		} else if (typeStr.equals(SCATTER_SUMMARIZED_STR)) {
			return SCATTER_SUMMARIZED;
		} else if (typeStr.equals(PIE_STR)) {
			return PIE;
		} else if (typeStr.equals(PIE_SUMMARIZED_STR)) {
			return PIE_SUMMARIZED;
		} else if (typeStr.equals(STACKEDBAR_STR)) {
			return STACKEDBAR;
		} else if (typeStr.equals(STACKEDBAR_SUMMARIZED_STR)) {
			return STACKEDBAR_SUMMARIZED;
		} else {
			return null;
		}
	}

	public static String typeToString(Integer type) {
		if (type == null) {
			return NONE_STR;
		}
		if (type == LINE) {
			return LINE_STR;
		} else if (type == LINE_SUMMARIZED) {
			return LINE_SUMMARIZED_STR;
		} else if (type == AREA) {
			return AREA_STR;
		} else if (type == SCATTER) {
			return SCATTER_STR;
		} else if (type == SCATTER_SUMMARIZED) {
			return SCATTER_SUMMARIZED_STR;
		} else if (type == PIE) {
			return PIE_STR;
		} else if (type == PIE_SUMMARIZED) {
			return PIE_SUMMARIZED_STR;
		} else if (type == STACKEDBAR) {
			return STACKEDBAR_STR;
		} else if (type == STACKEDBAR_SUMMARIZED) {
			return STACKEDBAR_SUMMARIZED_STR;
		} else {
			return NONE_STR;
		}
	}
}
