/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.logging.constant;

public class LoggingConstant {
	public static final String CONFIG_FILE_NAME = "hinemoslogging.cfg";
	public static final String CONFIG_FILE_PATH = "/WEB-INF/lib/" + CONFIG_FILE_NAME;
	public static final String SERVLET_CONTEXT_PARAM_PROPERTIES_PATH = "HinemosLoggingConfigPath";
	public static final String CONTROL_APPENDER_NAME = "ControlLogRollingFile";
	public static final String MONITOR_APPENDER_NAME = "MonitoringLogRollingFile";
	public static final String INTERNAL_APPENDER_NAME = "HinemosLoggingInternalLog";
	public static final String INTERNAL_LOGGER_NAME = "com.clustercontrol.internal";
	public static final String[] PRIORITY = { "info", "warning", "critical", "unknown" };
}
