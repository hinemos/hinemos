/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.logging.constant;

public class LoggingConstant {
	/** Hinemos Logging設定ファイルのデフォルト名 */
	public static final String CONFIG_FILE_NAME = "hinemoslogging.cfg";
	/** Hinemos Logging設定ファイルの文字コード */
	public static final String CONFIG_CHARSET = "UTF-8";

	/** ServletContextのパラメータでファイルパスを指定する場合のパラメータ名 */
	public static final String SERVLET_CONTEXT_PARAM_PROPERTIES_PATH = "HinemosLoggingConfigPath";

	public static final String CONTROL_APPENDER_NAME = "ControlLogRollingFile";
	public static final String MONITOR_APPENDER_NAME = "MonitoringLogRollingFile";
	public static final String INTERNAL_APPENDER_NAME = "HinemosLoggingInternalLog";
	public static final String INTERNAL_LOGGER_NAME = "com.clustercontrol.internal";

	/** 制御ログ用Appenderのlayoutに設定する文字コード */
	public static final String CONTROL_APPENDER_CHARSET = "UTF-8";
	/** 監視ログ用Appenderのlayoutに設定する文字コード */
	public static final String MONITOR_APPENDER_CHARSET = "UTF-8";

}
