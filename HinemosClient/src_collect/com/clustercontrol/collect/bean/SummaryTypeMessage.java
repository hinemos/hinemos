package com.clustercontrol.collect.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.clustercontrol.util.Messages;

public class SummaryTypeMessage {

	/** サマリータイプ(raw) */
	public static final String STRING_RAW = Messages.getString("summarytype.raw");
	public static final String STRING_RAW_EN = Messages.getString("summarytype.raw", Locale.ENGLISH);

	/** サマリータイプ(avg_hour) */
	public static final String STRING_AVG_HOUR = Messages.getString("summarytype.avg.hour");
	public static final String STRING_AVG_HOUR_EN = Messages.getString("summarytype.avg.hour", Locale.ENGLISH);

	/** サマリータイプ(avg_day) */
	public static final String STRING_AVG_DAY = Messages.getString("summarytype.avg.day");
	public static final String STRING_AVG_DAY_EN = Messages.getString("summarytype.avg.day", Locale.ENGLISH);

	/** サマリータイプ(avg_month) */
	public static final String STRING_AVG_MONTH = Messages.getString("summarytype.avg.month");
	public static final String STRING_AVG_MONTH_EN = Messages.getString("summarytype.avg.month", Locale.ENGLISH);

	/** サマリータイプ(min_hour) */
	public static final String STRING_MIN_HOUR = Messages.getString("summarytype.min.hour");
	public static final String STRING_MIN_HOUR_EN = Messages.getString("summarytype.min.hour", Locale.ENGLISH);

	/** サマリータイプ(min_day) */
	public static final String STRING_MIN_DAY = Messages.getString("summarytype.min.day");
	public static final String STRING_MIN_DAY_EN = Messages.getString("summarytype.min.day", Locale.ENGLISH);

	/** サマリータイプ(min_month) */
	public static final String STRING_MIN_MONTH = Messages.getString("summarytype.min.month");
	public static final String STRING_MIN_MONTH_EN = Messages.getString("summarytype.min.month", Locale.ENGLISH);

	/** サマリータイプ(max_hour) */
	public static final String STRING_MAX_HOUR = Messages.getString("summarytype.max.hour");
	public static final String STRING_MAX_HOUR_EN = Messages.getString("summarytype.max.hour", Locale.ENGLISH);

	/** サマリータイプ(max_day) */
	public static final String STRING_MAX_DAY = Messages.getString("summarytype.max.day");
	public static final String STRING_MAX_DAY_EN = Messages.getString("summarytype.max.day", Locale.ENGLISH);

	/** サマリータイプ(max_month) */
	public static final String STRING_MAX_MONTH = Messages.getString("summarytype.max.month");
	public static final String STRING_MAX_MONTH_EN = Messages.getString("summarytype.max.month", Locale.ENGLISH);

	/**
	 * 種別から文字列に変換します。<BR>
	 *
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == SummaryTypeConstant.TYPE_RAW) {
			return STRING_RAW;
		} else if (type == SummaryTypeConstant.TYPE_AVG_HOUR) {
			return STRING_AVG_HOUR;
		} else if (type == SummaryTypeConstant.TYPE_AVG_DAY) {
			return STRING_AVG_DAY;
		} else if (type == SummaryTypeConstant.TYPE_AVG_MONTH) {
			return STRING_AVG_MONTH;
		} else if (type == SummaryTypeConstant.TYPE_MIN_HOUR) {
			return STRING_MIN_HOUR;
		} else if (type == SummaryTypeConstant.TYPE_MIN_DAY) {
			return STRING_MIN_DAY;
		} else if (type == SummaryTypeConstant.TYPE_MIN_MONTH) {
			return STRING_MIN_MONTH;
		} else if (type == SummaryTypeConstant.TYPE_MAX_HOUR) {
			return STRING_MAX_HOUR;
		} else if (type == SummaryTypeConstant.TYPE_MAX_DAY) {
			return STRING_MAX_DAY;
		} else if (type == SummaryTypeConstant.TYPE_MAX_MONTH) {
			return STRING_MAX_MONTH;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 *
	 * @param type
	 * @return
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_RAW)) {
			return SummaryTypeConstant.TYPE_RAW;
		} else if (string.equals(STRING_AVG_HOUR)) {
			return SummaryTypeConstant.TYPE_AVG_HOUR;
		} else if (string.equals(STRING_AVG_DAY)) {
			return SummaryTypeConstant.TYPE_AVG_DAY;
		} else if (string.equals(STRING_AVG_MONTH)) {
			return SummaryTypeConstant.TYPE_AVG_MONTH;
		} else if (string.equals(STRING_MIN_HOUR)) {
			return SummaryTypeConstant.TYPE_MIN_HOUR;
		} else if (string.equals(STRING_MIN_DAY)) {
			return SummaryTypeConstant.TYPE_MIN_DAY;
		} else if (string.equals(STRING_MIN_MONTH)) {
			return SummaryTypeConstant.TYPE_MIN_MONTH;
		} else if (string.equals(STRING_MAX_HOUR)) {
			return SummaryTypeConstant.TYPE_MAX_HOUR;
		} else if (string.equals(STRING_MAX_DAY)) {
			return SummaryTypeConstant.TYPE_MAX_DAY;
		} else if (string.equals(STRING_MAX_MONTH)) {
			return SummaryTypeConstant.TYPE_MAX_MONTH;
		}
		return -1;
	}
	
	/**
	 * 文字列(英語)から種別に変換します。<BR>
	 *
	 * @param type
	 * @return
	 */
	public static int stringENToType(String string) {
		if (string.equals(STRING_RAW_EN)) {
			return SummaryTypeConstant.TYPE_RAW;
		} else if (string.equals(STRING_AVG_HOUR_EN)) {
			return SummaryTypeConstant.TYPE_AVG_HOUR;
		} else if (string.equals(STRING_AVG_DAY_EN)) {
			return SummaryTypeConstant.TYPE_AVG_DAY;
		} else if (string.equals(STRING_AVG_MONTH_EN)) {
			return SummaryTypeConstant.TYPE_AVG_MONTH;
		} else if (string.equals(STRING_MIN_HOUR_EN)) {
			return SummaryTypeConstant.TYPE_MIN_HOUR;
		} else if (string.equals(STRING_MIN_DAY_EN)) {
			return SummaryTypeConstant.TYPE_MIN_DAY;
		} else if (string.equals(STRING_MIN_MONTH_EN)) {
			return SummaryTypeConstant.TYPE_MIN_MONTH;
		} else if (string.equals(STRING_MAX_HOUR_EN)) {
			return SummaryTypeConstant.TYPE_MAX_HOUR;
		} else if (string.equals(STRING_MAX_DAY_EN)) {
			return SummaryTypeConstant.TYPE_MAX_DAY;
		} else if (string.equals(STRING_MAX_MONTH_EN)) {
			return SummaryTypeConstant.TYPE_MAX_MONTH;
		}
		return SummaryTypeConstant.TYPE_AVG_HOUR;
	}
	
	/**
	 * 種別から文字列(英語)に変換します。
	 * @param type
	 * @return
	 */
	public static String typeToStringEN(int type) {
		if (type == SummaryTypeConstant.TYPE_RAW) {
			return STRING_RAW_EN;
		} else if (type == SummaryTypeConstant.TYPE_AVG_HOUR) {
			return STRING_AVG_HOUR_EN;
		} else if (type == SummaryTypeConstant.TYPE_AVG_DAY) {
			return STRING_AVG_DAY_EN;
		} else if (type == SummaryTypeConstant.TYPE_AVG_MONTH) {
			return STRING_AVG_MONTH_EN;
		} else if (type == SummaryTypeConstant.TYPE_MIN_HOUR) {
			return STRING_MIN_HOUR_EN;
		} else if (type == SummaryTypeConstant.TYPE_MIN_DAY) {
			return STRING_MIN_DAY_EN;
		} else if (type == SummaryTypeConstant.TYPE_MIN_MONTH) {
			return STRING_MIN_MONTH_EN;
		} else if (type == SummaryTypeConstant.TYPE_MAX_HOUR) {
			return STRING_MAX_HOUR_EN;
		} else if (type == SummaryTypeConstant.TYPE_MAX_DAY) {
			return STRING_MAX_DAY_EN;
		} else if (type == SummaryTypeConstant.TYPE_MAX_MONTH) {
			return STRING_MAX_MONTH_EN;
		}
		return "";
	}
	
	public static List<String> getSummaryTypeList() {
		List<String> list = new ArrayList<>();
		list.add(STRING_RAW);
		list.add(STRING_AVG_HOUR);
		list.add(STRING_AVG_DAY);
		list.add(STRING_AVG_MONTH)
		;
		list.add(STRING_MIN_HOUR);
		list.add(STRING_MIN_DAY);
		list.add(STRING_MIN_MONTH);
		
		list.add(STRING_MAX_HOUR);
		list.add(STRING_MAX_DAY);
		list.add(STRING_MAX_MONTH);
		
		return list;
	}
}
