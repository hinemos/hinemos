/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.bean;

public class SummaryTypeConstant {

	public static final int TYPE_DAILY_COUNT = 0;
	public static final int TYPE_HOURLY_REDUCTION = 1;
	public static final int TYPE_SCENARIO_ERRORS = 2;
	public static final int TYPE_NODE_ERRORS = 3;
	public static final int TYPE_SCENARIO_REDUCTION = 4;
	public static final int TYPE_NODE_REDUCTION = 5;
	public static final int TYPE_ERRORS = 6;
	public static final int TYPE_REDUCTION = 7;
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToMessageCode(int type) {
		if (type == TYPE_DAILY_COUNT) {
			return "SUMMARYTYPE_DAILY_COUNT";
		} else if (type == TYPE_HOURLY_REDUCTION) {
			return "SUMMARYTYPE_HOURLY_REDUCTION";
		} else if (type == TYPE_SCENARIO_ERRORS) {
			return "SUMMARYTYPE_SCENARIO_ERRORS";
		} else if (type == TYPE_NODE_ERRORS) {
			return "SUMMARYTYPE_NODE_ERRORS";
		} else if (type == TYPE_SCENARIO_REDUCTION) {
			return "SUMMARYTYPE_SCENARIO_REDUCTION";
		} else if (type == TYPE_NODE_REDUCTION) {
			return "SUMMARYTYPE_NODE_REDUCTION";
		} else if (type == TYPE_ERRORS) {
			return "SUMMARYTYPE_ERRORS";
		} else if (type == TYPE_REDUCTION) {
			return "SUMMARYTYPE_REDUCTION";
		}
		return "";
	}
}
