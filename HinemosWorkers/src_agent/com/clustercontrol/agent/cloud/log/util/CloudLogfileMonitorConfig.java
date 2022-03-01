/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.cloud.log.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;

public class CloudLogfileMonitorConfig implements FileMonitorConfig{

	// ロガー
	private static Log m_log = LogFactory.getLog(CloudLogfileMonitorConfig.class);

	private static final String RUN_INTERVAL = "monitor.cloudlogfile.filter.interval";
	
	private static final String UNCHANGED_STATS_PERIOD = "monitor.cloudlogfile.filter.filesizecheck.period";

	private static final String FIRST_PART_DATA_CHECK_PERIOD = "monitor.cloudlogfile.filter.fileheadercheck.period";

	private static final String FIRST_PART_DATA_CHECK_SIZE = "monitor.cloudlogfile.filter.fileheadercheck.size";

	private static final String FILE_MAX_SIZE = "monitor.cloudlogfile.filter.maxsize";

	private static final String FILE_MAX_FILES = "monitor.cloudlogfile.filter.maxfiles";

	private static final String PROGRAM = "monitor.cloudlogfile.syslog.program";

	private static final String MONITOR_CLOUDLOGFILE_MESSAGE_LENGTH = "monitor.cloudlogfile.message.length";

	private static final String MONITOR_CLOUDLOGFILE_MESSAGE_LINE = "monitor.cloudlogfile.message.line";
	
	private static final String READ_CARRYOVER_LENGTH = "monitor.cloudlogfile.read.carryover.length";

	private static final String MAX_FILE_NOTIFY_INTERVAL_KEY = "monitor.cloudlogfile.filter.maxfiles.notify.interval";

	/** ログファイル監視間隔 */
	private int runInterval = 1000; // 1sec

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
	private int cloudlogfileReadCarryOverLength = 0;

	private static final String HINEMOS_LOG_AGENT = "hinemos_agent";

	/** ログ先頭に定義するプログラム名 */
	private String program = HINEMOS_LOG_AGENT;

	private long cloudlogfileMaxFileNotifyInterval = 0;
	
	private static CloudLogfileMonitorConfig instance = new CloudLogfileMonitorConfig();

	private CloudLogfileMonitorConfig() {
		// ファイル監視間隔 (ミリ秒)
		// 一度のログ取得でローテートが複数発生する場合の
		// ファイル監視間隔を定義
		String runIntervalStr = AgentProperties.getProperty(RUN_INTERVAL, Integer.toString(runInterval));
		try {
			runInterval = Integer.parseInt(runIntervalStr);
		} catch (NumberFormatException e) {
			m_log.warn("cloudlogfileMonitorConfig() : " + RUN_INTERVAL, e);
		} catch (Exception e) {
			m_log.warn("cloudlogfileMonitorConfig() : " + RUN_INTERVAL, e);
		}
		m_log.debug(RUN_INTERVAL + "=" + runInterval);

		// ファイル変更チェック期間（秒）
		// クラウドログ監視の場合ファイル監視は30秒～1時間間隔とまちまちなので、
		// 基本毎回行う
		String sleepInterval = AgentProperties.getProperty(UNCHANGED_STATS_PERIOD, "1");
		m_log.info(UNCHANGED_STATS_PERIOD + " = " + sleepInterval + " sec");
		try {
			unchangedStatsPeriod = Integer.parseInt(sleepInterval) * 1000;
		} catch (NumberFormatException e) {
			m_log.warn("cloudlogfileMonitorConfig() : " + UNCHANGED_STATS_PERIOD, e);
		} catch (Exception e) {
			m_log.warn("cloudlogfileMonitorConfig() : " + UNCHANGED_STATS_PERIOD, e);
		}
		m_log.debug(UNCHANGED_STATS_PERIOD + " = " + unchangedStatsPeriod);

		// ファイル変更詳細チェック（冒頭データ比較）期間（秒）
		// クラウドログ監視の場合ファイル監視は30秒～1時間間隔とまちまちなので、
		// 基本毎回行う
		String firstPartDataCheckPeriodStr = AgentProperties.getProperty(FIRST_PART_DATA_CHECK_PERIOD, "1");
		try {
			firstPartDataCheckPeriod = Integer.parseInt(firstPartDataCheckPeriodStr) * 1000;
		} catch (NumberFormatException e) {
			m_log.warn("cloudlogfileMonitorConfig() : " + FIRST_PART_DATA_CHECK_PERIOD, e);
		} catch (Exception e) {
			m_log.warn("cloudlogfileMonitorConfig() : " + FIRST_PART_DATA_CHECK_PERIOD, e);
		}
		m_log.debug(FIRST_PART_DATA_CHECK_PERIOD + " = " + firstPartDataCheckPeriod);

		// ファイル変更詳細チェック（冒頭データ比較）サイズ（byte）
		String firstPartDataCheckSizeStr = AgentProperties.getProperty(FIRST_PART_DATA_CHECK_SIZE, "256");
		try {
			firstPartDataCheckSize = Integer.parseInt(firstPartDataCheckSizeStr);
		} catch (NumberFormatException e) {
			m_log.warn("cloudlogfileMonitorConfig() : " + FIRST_PART_DATA_CHECK_SIZE, e);
		} catch (Exception e) {
			m_log.warn("cloudlogfileMonitorConfig() : " + FIRST_PART_DATA_CHECK_SIZE, e);
		}
		m_log.debug(FIRST_PART_DATA_CHECK_SIZE + " = " + firstPartDataCheckSize);

		// 上限ファイルサイズ（byte）
		String fileMaxSizeStr = AgentProperties.getProperty(FILE_MAX_SIZE, "8589934592");
		m_log.info(FILE_MAX_SIZE + " = " + fileMaxSizeStr + " byte");
		try {
			fileMaxSize = Long.parseLong(fileMaxSizeStr);
		} catch (NumberFormatException e) {
			m_log.warn("cloudlogfileMonitorConfig() : " + FILE_MAX_SIZE, e);
		} catch (Exception e) {
			m_log.warn("cloudlogfileMonitorConfig() : " + FILE_MAX_SIZE, e);
		}
		m_log.debug(FILE_MAX_SIZE + " = " + fileMaxSize);
		
		// 上限ファイル数
		String fileMaxStr = AgentProperties.getProperty(FILE_MAX_FILES, "500");
		m_log.info(FILE_MAX_FILES + " = " + fileMaxStr);
		try {
			fileMaxFiles = Integer.parseInt(fileMaxStr);
		} catch (Exception e) {
			m_log.warn("cloudlogfileMonitorConfig() : " + e.getMessage());
		}
		m_log.info(FILE_MAX_FILES + "=" + fileMaxFiles);

		// 上限ファイルサイズ（byte）
		String logfilMessageLimitLengthStr = AgentProperties.getProperty(MONITOR_CLOUDLOGFILE_MESSAGE_LENGTH, "1024");
		
		m_log.info(MONITOR_CLOUDLOGFILE_MESSAGE_LENGTH + " = " + logfilMessageLimitLengthStr + " byte");
		try {
			logfilMessageLength = Integer.parseInt(logfilMessageLimitLengthStr);
		} catch (NumberFormatException e) {
			m_log.warn("cloudlogfileMonitorConfig() : " + MONITOR_CLOUDLOGFILE_MESSAGE_LENGTH, e);
		} catch (Exception e) {
			m_log.warn("cloudlogfileMonitorConfig() : " + MONITOR_CLOUDLOGFILE_MESSAGE_LENGTH, e);
		}
		m_log.debug(MONITOR_CLOUDLOGFILE_MESSAGE_LENGTH + " = " + logfilMessageLength);

		// 上限読み込み行数
		String logfilMessageLimitLineStr = AgentProperties.getProperty(MONITOR_CLOUDLOGFILE_MESSAGE_LINE, "1024");
		
		m_log.info(MONITOR_CLOUDLOGFILE_MESSAGE_LINE + " = " + logfilMessageLimitLineStr + " line");
		try {
			logfilMessageLine = Integer.parseInt(logfilMessageLimitLineStr);
		} catch (NumberFormatException e) {
			m_log.warn("cloudlogfileMonitorConfig() : " + MONITOR_CLOUDLOGFILE_MESSAGE_LINE, e);
		} catch (Exception e) {
			m_log.warn("cloudlogfileMonitorConfig() : " + MONITOR_CLOUDLOGFILE_MESSAGE_LINE, e);
		}
		m_log.debug(MONITOR_CLOUDLOGFILE_MESSAGE_LINE + " = " + logfilMessageLine);
		
		// ログファイル読込繰越データ長
		String cloudlogfileReadCarryOverLengthStr = AgentProperties.getProperty(READ_CARRYOVER_LENGTH, "102400");
		
		m_log.info(READ_CARRYOVER_LENGTH + " = " + cloudlogfileReadCarryOverLengthStr + " characters");
		try {
				cloudlogfileReadCarryOverLength = Integer.parseInt(cloudlogfileReadCarryOverLengthStr);
		} catch (NumberFormatException e) {
				m_log.warn("cloudlogfileMonitorConfig() : " + READ_CARRYOVER_LENGTH, e);
		} catch (Exception e) {
				m_log.warn("cloudlogfileMonitorConfig() : " + READ_CARRYOVER_LENGTH, e);
		}
		m_log.debug(READ_CARRYOVER_LENGTH + " = " + cloudlogfileReadCarryOverLength);
		
		// プログラム名を設定
		program = AgentProperties.getProperty(PROGRAM, HINEMOS_LOG_AGENT);
		if ("".equals(program)) {
			program = HINEMOS_LOG_AGENT;
		}
		m_log.debug(PROGRAM + " = " + program);

		String cloudlogfileMaxFileNotifyIntervalStr = AgentProperties.getProperty(MAX_FILE_NOTIFY_INTERVAL_KEY, "0");
		try {
			cloudlogfileMaxFileNotifyInterval = Long.parseLong(cloudlogfileMaxFileNotifyIntervalStr);
		} catch (NumberFormatException e) {
				m_log.warn("cloudlogfileMonitorConfig() : " + MAX_FILE_NOTIFY_INTERVAL_KEY, e);
		} catch (Exception e) {
				m_log.warn("cloudlogfileMonitorConfig() : " + MAX_FILE_NOTIFY_INTERVAL_KEY, e);
		}
		m_log.debug(MAX_FILE_NOTIFY_INTERVAL_KEY + " = " + cloudlogfileMaxFileNotifyInterval);
	}

	public static CloudLogfileMonitorConfig getInstance() {
		return instance;
	}

	@Override
	public int getMaxThreads() {
		// クラウドログ監視は独自起動方法をとっているため、本メソッドはサポートしない。
		m_log.warn("Not supported method.");
		throw new UnsupportedOperationException();
	}

	@Override
	public String getThreadName() {
		// クラウドログ監視は独自起動方法をとっているため、本メソッドはサポートしない。
		m_log.warn("Not supported method.");
		throw new UnsupportedOperationException();
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
		return cloudlogfileReadCarryOverLength;
	}

	@Override
	public String getProgram() {
		return program;
	}

	@Override
	public long getMaxFileNotifyInterval() {
		return cloudlogfileMaxFileNotifyInterval;
	}

}
