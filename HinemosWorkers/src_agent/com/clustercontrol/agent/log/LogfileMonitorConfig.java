/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.ws.monitor.MonitorInfo;

public class LogfileMonitorConfig {

	// ロガー
	private static Log m_log = LogFactory.getLog(LogfileMonitorConfig.class);

	private static final String UNCHANGED_STATS_PERIOD = "monitor.logfile.filter.filesizecheck.period";

	private static final String FIRST_PART_DATA_CHECK_PERIOD = "monitor.logfile.filter.fileheadercheck.period";

	private static final String FIRST_PART_DATA_CHECK_SIZE = "monitor.logfile.filter.fileheadercheck.size";

	private static final String FILE_MAX_SIZE = "monitor.logfile.filter.maxsize";

	private static final String FILE_MAX_FILES = "monitor.logfile.filter.maxfiles";

	private static final String PROGRAM = "monitor.logfile.syslog.program";

	private static final String MONITOR_LOGFILE_MESSAGE_LENGTH = "monitor.logfile.message.length";

	private static final String MONITOR_LOGFILE_MESSAGE_LINE = "monitor.logfile.message.line";
	
	/** ファイル変更チェック期間設定（ミリ秒） */
	protected static int unchangedStatsPeriod = 0;

	/** ファイル変更詳細チェック（冒頭データ比較）期間（ミリ秒） */
	protected static int firstPartDataCheckPeriod = 0;

	/** ファイル変更詳細チェック（冒頭データ比較）サイズ（byte） */
	protected static int firstPartDataCheckSize = 0;

	/** 上限ファイルサイズ設定（byte） */
	protected static long fileMaxSize = 0L;
	
	/** 上限ファイル数 */
	protected static long fileMaxFiles = 0;

	/** オリジナルメッセージのサイズ上限（Byte）*/
	protected static int logfilMessageLength = 0;
	
	/** オリジナルメッセージの読み込み行数上限*/
	protected static int logfilMessageLine = 0;

	protected static final String HINEMOS_LOG_AGENT = "hinemos_agent";

	/** ログ先頭に定義するプログラム名 */
	protected static String program = HINEMOS_LOG_AGENT;

	static {
		// ファイル変更チェック期間（秒）
		String sleepInterval = AgentProperties.getProperty(UNCHANGED_STATS_PERIOD, "5");
		m_log.info(UNCHANGED_STATS_PERIOD + " = " + sleepInterval + " sec");
		try {
			unchangedStatsPeriod = Integer.parseInt(sleepInterval) * 1000;
		} catch (NumberFormatException e) {
			m_log.warn("LogfileManager() : " + UNCHANGED_STATS_PERIOD, e);
		} catch (Exception e) {
			m_log.warn("LogfileManager() : " + UNCHANGED_STATS_PERIOD, e);
		}
		m_log.debug(UNCHANGED_STATS_PERIOD + " = " + unchangedStatsPeriod);

		// ファイル変更詳細チェック（冒頭データ比較）期間（秒）
		String firstPartDataCheckPeriodStr = AgentProperties.getProperty(FIRST_PART_DATA_CHECK_PERIOD, "300");
		try {
			firstPartDataCheckPeriod = Integer.parseInt(firstPartDataCheckPeriodStr) * 1000;
		} catch (NumberFormatException e) {
			m_log.warn("LogfileManager() : " + FIRST_PART_DATA_CHECK_PERIOD, e);
		} catch (Exception e) {
			m_log.warn("LogfileManager() : " + FIRST_PART_DATA_CHECK_PERIOD, e);
		}
		m_log.debug(FIRST_PART_DATA_CHECK_PERIOD + " = " + firstPartDataCheckPeriod);

		// ファイル変更詳細チェック（冒頭データ比較）サイズ（byte）
		String firstPartDataCheckSizeStr = AgentProperties.getProperty(FIRST_PART_DATA_CHECK_SIZE, "256");
		try {
			firstPartDataCheckSize = Integer.parseInt(firstPartDataCheckSizeStr);
		} catch (NumberFormatException e) {
			m_log.warn("LogfileManager() : " + FIRST_PART_DATA_CHECK_SIZE, e);
		} catch (Exception e) {
			m_log.warn("LogfileManager() : " + FIRST_PART_DATA_CHECK_SIZE, e);
		}
		m_log.debug(FIRST_PART_DATA_CHECK_SIZE + " = " + firstPartDataCheckSize);

		// 上限ファイルサイズ（byte）
		String fileMaxSizeStr = AgentProperties.getProperty(FILE_MAX_SIZE, "8589934592");
		m_log.info(FILE_MAX_SIZE + " = " + fileMaxSizeStr + " byte");
		try {
			fileMaxSize = Long.parseLong(fileMaxSizeStr);
		} catch (NumberFormatException e) {
			m_log.warn("LogfileManager() : " + FILE_MAX_SIZE, e);
		} catch (Exception e) {
			m_log.warn("LogfileManager() : " + FILE_MAX_SIZE, e);
		}
		m_log.debug(FILE_MAX_SIZE + " = " + fileMaxSize);
		
		// 上限ファイル数
		String fileMaxStr = AgentProperties.getProperty(FILE_MAX_FILES, "500");
		m_log.info(FILE_MAX_FILES + " = " + fileMaxStr);
		try {
			fileMaxFiles = Integer.parseInt(fileMaxStr);
		} catch (Exception e) {
			m_log.warn("LogfileThread : " + e.getMessage());
		}
		m_log.info(FILE_MAX_FILES + "=" + fileMaxFiles);

		// 上限ファイルサイズ（byte）
		String logfilMessageLimitLengthStr = AgentProperties.getProperty(MONITOR_LOGFILE_MESSAGE_LENGTH, "1024");
		
		m_log.info(MONITOR_LOGFILE_MESSAGE_LENGTH + " = " + logfilMessageLimitLengthStr + " byte");
		try {
			logfilMessageLength = Integer.parseInt(logfilMessageLimitLengthStr);
		} catch (NumberFormatException e) {
			m_log.warn("LogfileManager() : " + MONITOR_LOGFILE_MESSAGE_LENGTH, e);
		} catch (Exception e) {
			m_log.warn("LogfileManager() : " + MONITOR_LOGFILE_MESSAGE_LENGTH, e);
		}
		m_log.debug(MONITOR_LOGFILE_MESSAGE_LENGTH + " = " + logfilMessageLength);

		// 上限読み込み行数
		String logfilMessageLimitLineStr = AgentProperties.getProperty(MONITOR_LOGFILE_MESSAGE_LINE, "1024");
		
		m_log.info(MONITOR_LOGFILE_MESSAGE_LINE + " = " + logfilMessageLimitLineStr + " line");
		try {
			logfilMessageLine = Integer.parseInt(logfilMessageLimitLineStr);
		} catch (NumberFormatException e) {
			m_log.warn("LogfileManager() : " + MONITOR_LOGFILE_MESSAGE_LINE, e);
		} catch (Exception e) {
			m_log.warn("LogfileManager() : " + MONITOR_LOGFILE_MESSAGE_LINE, e);
		}
		m_log.debug(MONITOR_LOGFILE_MESSAGE_LINE + " = " + logfilMessageLine);
		
		// プログラム名を設定
		program = AgentProperties.getProperty(PROGRAM, HINEMOS_LOG_AGENT);
		if ("".equals(program)) {
			program = HINEMOS_LOG_AGENT;
		}
		m_log.debug(PROGRAM + " = " + program);
	}
	/**
	 * 監視文字列を整形する
	 * @param line
	 * @return formatted line
	 */
	public static String formatLine(String line, MonitorInfo monitorInfo){
		String separator = "\n";
		switch(monitorInfo.getLogfileCheckInfo().getFileReturnCode()) {
			case "LF":
				separator = "\n";
				break;
			case "CR":
				separator = "\r";
				break;
			case "CRLF":
				separator = "\r\n";
				break;
			default:
				m_log.warn("ReturnCode:" + monitorInfo.getLogfileCheckInfo().getFileReturnCode());
		}

		// ファイル改行コードが残ってしまうので、ここで削除する。
		line = line.replace(separator, "");
		
		// 長さが上限値を超える場合は切り捨てる
		if (line.length() > logfilMessageLength) {
			m_log.info("log line is too long");
			line = line.substring(0, logfilMessageLength);
		}
		return line;
	}
}
