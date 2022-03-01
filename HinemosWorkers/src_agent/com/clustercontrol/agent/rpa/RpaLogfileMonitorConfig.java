/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.rpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;

/**
 * RPAログファイル監視の設定を定義するクラス
 */
public class RpaLogfileMonitorConfig implements FileMonitorConfig {

	// ロガー
	private static Log m_log = LogFactory.getLog(RpaLogfileMonitorConfig.class);

	private static final String MAX_THREADS = "monitor.rpalogfile.filter.threads";

	private static final String RUN_INTERVAL = "monitor.rpalogfile.filter.interval";

	private static final String UNCHANGED_STATS_PERIOD = "monitor.rpalogfile.filter.filesizecheck.period";

	private static final String FIRST_PART_DATA_CHECK_PERIOD = "monitor.rpalogfile.filter.fileheadercheck.period";

	private static final String FIRST_PART_DATA_CHECK_SIZE = "monitor.rpalogfile.filter.fileheadercheck.size";

	private static final String FILE_MAX_SIZE = "monitor.rpalogfile.filter.maxsize";

	private static final String FILE_MAX_FILES = "monitor.rpalogfile.filter.maxfiles";

	private static final String MONITOR_LOGFILE_MESSAGE_LENGTH = "monitor.rpalogfile.message.length";

	private static final String MONITOR_LOGFILE_MESSAGE_LINE = "monitor.rpalogfile.message.line";
	
	private static final String READ_CARRYOVER_LENGTH = "monitor.rpalogfile.read.carryover.length";

	private static final String MAX_FILE_NOTIFY_INTERVAL_KEY = "monitor.rpalogfile.filter.maxfiles.notify.interval";

	/** ログファイル監視スレッド数上限 */
	private int maxThreads = 2;

	/** ログファイル監視間隔 */
	private int runInterval = 10000; // 10sec

	/** ファイル変更チェック期間設定（ミリ秒） */
	private int unchangedStatsPeriod = 0;

	/** ファイル変更詳細チェック（冒頭データ比較）期間（ミリ秒） */
	private int firstPartDataCheckPeriod = 0;

	/** ファイル変更詳細チェック（冒頭データ比較）サイズ（byte） */
	private int firstPartDataCheckSize = 0;

	/** 上限ファイルサイズ設定（byte） */
	private long fileMaxSize = 0L;
	
	/** 上限ファイル数 */
	private long fileMaxFiles = 0;

	/** オリジナルメッセージのサイズ上限*/
	private int logfilMessageLength = 0;
	
	/** オリジナルメッセージの読み込み行数上限*/
	private int logfilMessageLine = 0;

	/** ログファイル読込繰越データ長：ログファイル読込（バッファ単位取得、末尾まで連続）次回繰越データ最大長 */
	private int logfileReadCarryOverLength = 0;

	private static final String HINEMOS_LOG_AGENT = "hinemos_agent";

	/** ログ先頭に定義するプログラム名 */
	private String program = HINEMOS_LOG_AGENT;
	
	/** 上限ファイル数超過通知間隔 */
	private long logfileMaxFileNotifyInterval = 0;

	private static RpaLogfileMonitorConfig instance = new RpaLogfileMonitorConfig();

	private RpaLogfileMonitorConfig() {
		// ログファイル監視スレッド数上限
		String maxThreadsStr = AgentProperties.getProperty(MAX_THREADS, Integer.toString(maxThreads));
		try {
			int value = Integer.parseInt(maxThreadsStr);
			if (value < 1) {
				throw new NumberFormatException("invalid value: " + maxThreadsStr);
			}
			maxThreads = value;
		} catch (NumberFormatException e) {
			m_log.warn("RpaLogfileMonitorConfig() : " + MAX_THREADS + ", " + e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("RpaLogfileMonitorConfig() : " + MAX_THREADS + ", " + e.getMessage(), e);
		}
		m_log.debug(MAX_THREADS + "=" + maxThreads);

		// ログファイル監視間隔 (ミリ秒)
		String runIntervalStr = AgentProperties.getProperty(RUN_INTERVAL, Integer.toString(runInterval));
		try {
			runInterval = Integer.parseInt(runIntervalStr);
		} catch (NumberFormatException e) {
			m_log.warn("RpaLogfileMonitorConfig() : " + RUN_INTERVAL, e);
		} catch (Exception e) {
			m_log.warn("RpaLogfileMonitorConfig() : " + RUN_INTERVAL, e);
		}
		m_log.debug(RUN_INTERVAL + "=" + runInterval);

		// ファイル変更チェック期間（秒）
		String sleepInterval = AgentProperties.getProperty(UNCHANGED_STATS_PERIOD, "5");
		m_log.info(UNCHANGED_STATS_PERIOD + " = " + sleepInterval + " sec");
		try {
			unchangedStatsPeriod = Integer.parseInt(sleepInterval) * 1000;
		} catch (NumberFormatException e) {
			m_log.warn("RpaLogfileMonitorConfig() : " + UNCHANGED_STATS_PERIOD, e);
		} catch (Exception e) {
			m_log.warn("RpaLogfileMonitorConfig() : " + UNCHANGED_STATS_PERIOD, e);
		}
		m_log.debug(UNCHANGED_STATS_PERIOD + " = " + unchangedStatsPeriod);

		// ファイル変更詳細チェック（冒頭データ比較）期間（秒）
		String firstPartDataCheckPeriodStr = AgentProperties.getProperty(FIRST_PART_DATA_CHECK_PERIOD, "300");
		try {
			firstPartDataCheckPeriod = Integer.parseInt(firstPartDataCheckPeriodStr) * 1000;
		} catch (NumberFormatException e) {
			m_log.warn("RpaLogfileMonitorConfig() : " + FIRST_PART_DATA_CHECK_PERIOD, e);
		} catch (Exception e) {
			m_log.warn("RpaLogfileMonitorConfig() : " + FIRST_PART_DATA_CHECK_PERIOD, e);
		}
		m_log.debug(FIRST_PART_DATA_CHECK_PERIOD + " = " + firstPartDataCheckPeriod);

		// ファイル変更詳細チェック（冒頭データ比較）サイズ（byte）
		String firstPartDataCheckSizeStr = AgentProperties.getProperty(FIRST_PART_DATA_CHECK_SIZE, "256");
		try {
			firstPartDataCheckSize = Integer.parseInt(firstPartDataCheckSizeStr);
		} catch (NumberFormatException e) {
			m_log.warn("RpaLogfileMonitorConfig() : " + FIRST_PART_DATA_CHECK_SIZE, e);
		} catch (Exception e) {
			m_log.warn("RpaLogfileMonitorConfig() : " + FIRST_PART_DATA_CHECK_SIZE, e);
		}
		m_log.debug(FIRST_PART_DATA_CHECK_SIZE + " = " + firstPartDataCheckSize);

		// 上限ファイルサイズ（byte）
		String fileMaxSizeStr = AgentProperties.getProperty(FILE_MAX_SIZE, "8589934592");
		m_log.info(FILE_MAX_SIZE + " = " + fileMaxSizeStr + " byte");
		try {
			fileMaxSize = Long.parseLong(fileMaxSizeStr);
		} catch (NumberFormatException e) {
			m_log.warn("RpaLogfileMonitorConfig() : " + FILE_MAX_SIZE, e);
		} catch (Exception e) {
			m_log.warn("RpaLogfileMonitorConfig() : " + FILE_MAX_SIZE, e);
		}
		m_log.debug(FILE_MAX_SIZE + " = " + fileMaxSize);
		
		// 上限ファイル数
		String fileMaxStr = AgentProperties.getProperty(FILE_MAX_FILES, "500");
		m_log.info(FILE_MAX_FILES + " = " + fileMaxStr);
		try {
			fileMaxFiles = Integer.parseInt(fileMaxStr);
		} catch (Exception e) {
			m_log.warn("RpaLogfileMonitorConfig() : " + e.getMessage());
		}
		m_log.info(FILE_MAX_FILES + "=" + fileMaxFiles);

		// 上限ファイルサイズ（byte）
		String logfilMessageLimitLengthStr = AgentProperties.getProperty(MONITOR_LOGFILE_MESSAGE_LENGTH, "1048576");
		
		m_log.info(MONITOR_LOGFILE_MESSAGE_LENGTH + " = " + logfilMessageLimitLengthStr + " byte");
		try {
			logfilMessageLength = Integer.parseInt(logfilMessageLimitLengthStr);
		} catch (NumberFormatException e) {
			m_log.warn("RpaLogfileMonitorConfig() : " + MONITOR_LOGFILE_MESSAGE_LENGTH, e);
		} catch (Exception e) {
			m_log.warn("RpaLogfileMonitorConfig() : " + MONITOR_LOGFILE_MESSAGE_LENGTH, e);
		}
		m_log.debug(MONITOR_LOGFILE_MESSAGE_LENGTH + " = " + logfilMessageLength);

		// 上限読み込み行数
		String logfilMessageLimitLineStr = AgentProperties.getProperty(MONITOR_LOGFILE_MESSAGE_LINE, "1024");
		
		m_log.info(MONITOR_LOGFILE_MESSAGE_LINE + " = " + logfilMessageLimitLineStr + " line");
		try {
			logfilMessageLine = Integer.parseInt(logfilMessageLimitLineStr);
		} catch (NumberFormatException e) {
			m_log.warn("RpaLogfileMonitorConfig() : " + MONITOR_LOGFILE_MESSAGE_LINE, e);
		} catch (Exception e) {
			m_log.warn("RpaLogfileMonitorConfig() : " + MONITOR_LOGFILE_MESSAGE_LINE, e);
		}
		m_log.debug(MONITOR_LOGFILE_MESSAGE_LINE + " = " + logfilMessageLine);
		
		// ログファイル読込繰越データ長
		String logfileReadCarryOverLengthStr = AgentProperties.getProperty(READ_CARRYOVER_LENGTH, "102400");
		
		m_log.info(READ_CARRYOVER_LENGTH + " = " + logfileReadCarryOverLengthStr + " characters");
		try {
				logfileReadCarryOverLength = Integer.parseInt(logfileReadCarryOverLengthStr);
		} catch (NumberFormatException e) {
				m_log.warn("RpaLogfileMonitorConfig() : " + READ_CARRYOVER_LENGTH, e);
		} catch (Exception e) {
				m_log.warn("RpaLogfileMonitorConfig() : " + READ_CARRYOVER_LENGTH, e);
		}
		m_log.debug(READ_CARRYOVER_LENGTH + " = " + logfileReadCarryOverLength);

		// 上限ファイル数超過通知間隔
		String logfileMaxFileNotifyIntervalStr = AgentProperties.getProperty(MAX_FILE_NOTIFY_INTERVAL_KEY, "0");
		try {
			logfileMaxFileNotifyInterval = Long.parseLong(logfileMaxFileNotifyIntervalStr);
		} catch (NumberFormatException e) {
				m_log.warn("LogfileMonitorConfig() : " + MAX_FILE_NOTIFY_INTERVAL_KEY, e);
		} catch (Exception e) {
				m_log.warn("LogfileMonitorConfig() : " + MAX_FILE_NOTIFY_INTERVAL_KEY, e);
		}
		m_log.debug(MAX_FILE_NOTIFY_INTERVAL_KEY + " = " + logfileReadCarryOverLength);
	}

	public static RpaLogfileMonitorConfig getInstance() {
		return instance;
	}

	@Override
	public int getMaxThreads() {
		return maxThreads;
	}

	@Override
	public String getThreadName() {
		// RpaLogfileMonitor-execute-
		return this.getClass().getSimpleName().replace("Config", "") + "-execute-";
	}

	@Override
	public int getRunInterval() {
		return runInterval;
	}

	@Override
	public int getUnchangedStatsPeriod() {
		return unchangedStatsPeriod;
	}

	@Override
	public int getFirstPartDataCheckPeriod() {
		return firstPartDataCheckPeriod;
	}

	@Override
	public int getFirstPartDataCheckSize() {
		return firstPartDataCheckSize;
	}

	@Override
	public long getFileMaxSize() {
		return fileMaxSize;
	}

	@Override
	public long getFileMaxFiles() {
		return fileMaxFiles;
	}

	@Override
	public int getFilMessageLength() {
		return logfilMessageLength;
	}

	@Override
	public int getFilMessageLine() {
		return logfilMessageLine;
	}

	@Override
	public int getFileReadCarryOverLength() {
		return logfileReadCarryOverLength;
	}

	@Override
	public String getProgram() {
		return program;
	}

	@Override
	public long getMaxFileNotifyInterval() {
		return logfileMaxFileNotifyInterval;
	}
}
