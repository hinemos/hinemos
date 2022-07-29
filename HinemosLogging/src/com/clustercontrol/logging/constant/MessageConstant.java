/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.logging.constant;

public class MessageConstant {

	// SDML制御ログに[Error][Warning][Info]のいずれかで出力するメッセージ
	private static final String FAILD_COMMAND_TIMEOUT = "\"The command for getting the process command has timed out.\"";
	private static final String FAILD_MONITOR_ERROR = "\"Getting the value failed consecutively. monitor_type=%s, retries=%d\"";
	private static final String FAILD_MONITOR_WARN = "\"Failed to get the value. monitor_type=%s\"";
	private static final String INFO_WORKING = "HinemosLogging is working.";
	private static final String VALIDATE_EMPTY = "\"Required parameter is empty. property=%s\"";
	private static final String VALIDATE_FORMAT = "\"The value format is different. property=%s\"";
	private static final String VALIDATE_FORBIDDEN = "\"The parameter contains forbidden characters. property=%s\"";
	private static final String VALIDATE_INTEGER_FILESIZE = "\"Type of the value is Unusual file size. property=%s\"";
	private static final String VALIDATE_NUMERIC_RANGE = "\"The numeric parameter is out of range. property=%s\"";
	private static final String VALIDATE_STRING_LONG = "\"The string parameter is too long. property=%s\"";
	private static final String VALIDATE_STRING_SELECT = "\"The parameter is not a specific value. property=%s\"";
	private static final String VALIDATE_TYPE = "\"Type of the parameter is invalid. property=%s\"";
	public static final String VALIDATE_GCC_ALL = "\"If you use GC method 'all', you must set the number only 1.\"";
	public static final String VALIDATE_GCC_GET_FAILED = "\"Failed to get GC collector during initialization.\"";

	public static String getFaildCommandTimeout() {
		return FAILD_COMMAND_TIMEOUT;
	}

	public static String getFaildMonitorError(String type, int retries) {
		return String.format(FAILD_MONITOR_ERROR, type, retries);
	}

	public static String getFaildMonitorWarn(String type) {
		return String.format(FAILD_MONITOR_WARN, type);
	}

	public static String getInfoWorking() {
		return INFO_WORKING;
	}

	public static String getValidateEmpty(String key) {
		return String.format(VALIDATE_EMPTY, key);
	}

	public static String getValidateFormat(String key) {
		return String.format(VALIDATE_FORMAT, key);
	}

	public static String getValidateForbidden(String key) {
		return String.format(VALIDATE_FORBIDDEN, key);
	}

	public static String getValidateIntFileSize(String key) {
		return String.format(VALIDATE_INTEGER_FILESIZE, key);
	}

	public static String getValidateNumRange(String key) {
		return String.format(VALIDATE_NUMERIC_RANGE, key);
	}

	public static String getValidateStrLong(String key) {
		return String.format(VALIDATE_STRING_LONG, key);
	}

	public static String getValidateStrSerect(String key) {
		return String.format(VALIDATE_STRING_SELECT, key);
	}

	public static String getValidateType(String key) {
		return String.format(VALIDATE_TYPE, key);
	}
}
