package com.clustercontrol.collect.bean;

public class SummaryTypeConstant {
	public static final int TYPE_RAW = 0;
	public static final int TYPE_AVG_HOUR = 1;
	public static final int TYPE_AVG_DAY = 2;
	public static final int TYPE_AVG_MONTH = 3;
	public static final int TYPE_MIN_HOUR = 4;
	public static final int TYPE_MIN_DAY = 5;
	public static final int TYPE_MIN_MONTH = 6;
	public static final int TYPE_MAX_HOUR = 7;
	public static final int TYPE_MAX_DAY = 8;
	public static final int TYPE_MAX_MONTH = 9;
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToMessageCode(int type) {
		if (type == TYPE_RAW) {
			return "SUMMARYTYPE_RAW";
		} else if (type == TYPE_AVG_HOUR) {
			return "SUMMARYTYPE_AVG_HOUR";
		} else if (type == TYPE_AVG_DAY) {
			return "SUMMARYTYPE_AVG_DAY";
		} else if (type == TYPE_AVG_MONTH) {
			return "SUMMARYTYPE_AVG_MONTH";
		} else if (type == TYPE_MIN_HOUR) {
			return "SUMMARYTYPE_MIN_HOUR";
		} else if (type == TYPE_MIN_DAY) {
			return "SUMMARYTYPE_MIN_DAY";
		} else if (type == TYPE_MIN_MONTH) {
			return "SUMMARYTYPE_MIN_MONTH";
		} else if (type == TYPE_MAX_HOUR) {
			return "SUMMARYTYPE_MAX_HOUR";
		} else if (type == TYPE_MAX_DAY) {
			return "SUMMARYTYPE_MAX_DAY";
		} else if (type == TYPE_MAX_MONTH) {
			return "SUMMARYTYPE_MAX_MONTH";
		}
		return "";
	}
}
