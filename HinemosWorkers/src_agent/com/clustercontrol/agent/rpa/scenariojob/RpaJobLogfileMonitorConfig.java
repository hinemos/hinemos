/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.rpa.scenariojob;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;

public class RpaJobLogfileMonitorConfig implements FileMonitorConfig {

	// ロガー
	private static Log m_log = LogFactory.getLog(RpaJobLogfileMonitorConfig.class);

	private static final String RUN_INTERVAL = "job.rpalogfile.filter.interval";

	private static final String UNCHANGED_STATS_PERIOD = "job.rpalogfile.filter.filesizecheck.period";

	private static final String FIRST_PART_DATA_CHECK_PERIOD = "job.rpalogfile.filter.fileheadercheck.period";

	private static final String FIRST_PART_DATA_CHECK_SIZE = "job.rpalogfile.filter.fileheadercheck.size";

	private static final String FILE_MAX_SIZE = "job.rpalogfile.filter.maxsize";

	private static final String FILE_MAX_FILES = "job.rpalogfile.filter.maxfiles";

	private static final String MONITOR_LOGFILE_MESSAGE_LENGTH = "job.rpalogfile.message.length";

	private static final String MONITOR_LOGFILE_MESSAGE_LINE = "job.rpalogfile.message.line";

	private static final String READ_CARRYOVER_LENGTH = "job.rpalogfile.read.carryover.length";

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

	/** オリジナルメッセージのサイズ上限 */
	private int logfilMessageLength = 0;

	/** オリジナルメッセージの読み込み行数上限 */
	private int logfilMessageLine = 0;

	/** ログファイル読込繰越データ長：ログファイル読込（バッファ単位取得、末尾まで連続）次回繰越データ最大長 */
	private int logfileReadCarryOverLength = 0;

	private static final String HINEMOS_LOG_AGENT = "hinemos_agent";

	/** ログ先頭に定義するプログラム名 */
	private String program = HINEMOS_LOG_AGENT;

	/** 上限ファイル数超過通知間隔 */
	// シナリオジョブによるファイル監視では定周期通知を行わないため0固定
	private final long logfileMaxFileNotifyInterval = 0;

	private static RpaJobLogfileMonitorConfig instance = new RpaJobLogfileMonitorConfig();

	private RpaJobLogfileMonitorConfig() {
		// ログファイル監視間隔 (ミリ秒)
		String runIntervalStr = AgentProperties.getProperty(RUN_INTERVAL, Integer.toString(runInterval));
		try {
			runInterval = Integer.parseInt(runIntervalStr);
		} catch (NumberFormatException e) {
			m_log.warn("RpaJobLogfileMonitorConfig() : " + RUN_INTERVAL, e);
		} catch (Exception e) {
			m_log.warn("RpaJobLogfileMonitorConfig() : " + RUN_INTERVAL, e);
		}
		m_log.debug(RUN_INTERVAL + "=" + runInterval);

		// ファイル変更チェック期間（秒）
		String sleepInterval = AgentProperties.getProperty(UNCHANGED_STATS_PERIOD, "5");
		m_log.info(UNCHANGED_STATS_PERIOD + " = " + sleepInterval + " sec");
		try {
			unchangedStatsPeriod = Integer.parseInt(sleepInterval) * 1000;
		} catch (NumberFormatException e) {
			m_log.warn("RpaJobLogfileMonitorConfig() : " + UNCHANGED_STATS_PERIOD, e);
		} catch (Exception e) {
			m_log.warn("RpaJobLogfileMonitorConfig() : " + UNCHANGED_STATS_PERIOD, e);
		}
		m_log.debug(UNCHANGED_STATS_PERIOD + " = " + unchangedStatsPeriod);

		// ファイル変更詳細チェック（冒頭データ比較）期間（秒）
		String firstPartDataCheckPeriodStr = AgentProperties.getProperty(FIRST_PART_DATA_CHECK_PERIOD, "300");
		try {
			firstPartDataCheckPeriod = Integer.parseInt(firstPartDataCheckPeriodStr) * 1000;
		} catch (NumberFormatException e) {
			m_log.warn("RpaJobLogfileMonitorConfig() : " + FIRST_PART_DATA_CHECK_PERIOD, e);
		} catch (Exception e) {
			m_log.warn("RpaJobLogfileMonitorConfig() : " + FIRST_PART_DATA_CHECK_PERIOD, e);
		}
		m_log.debug(FIRST_PART_DATA_CHECK_PERIOD + " = " + firstPartDataCheckPeriod);

		// ファイル変更詳細チェック（冒頭データ比較）サイズ（byte）
		String firstPartDataCheckSizeStr = AgentProperties.getProperty(FIRST_PART_DATA_CHECK_SIZE, "256");
		try {
			firstPartDataCheckSize = Integer.parseInt(firstPartDataCheckSizeStr);
		} catch (NumberFormatException e) {
			m_log.warn("RpaJobLogfileMonitorConfig() : " + FIRST_PART_DATA_CHECK_SIZE, e);
		} catch (Exception e) {
			m_log.warn("RpaJobLogfileMonitorConfig() : " + FIRST_PART_DATA_CHECK_SIZE, e);
		}
		m_log.debug(FIRST_PART_DATA_CHECK_SIZE + " = " + firstPartDataCheckSize);

		// 上限ファイルサイズ（byte）
		String fileMaxSizeStr = AgentProperties.getProperty(FILE_MAX_SIZE, "8589934592");
		m_log.info(FILE_MAX_SIZE + " = " + fileMaxSizeStr + " byte");
		try {
			fileMaxSize = Long.parseLong(fileMaxSizeStr);
		} catch (NumberFormatException e) {
			m_log.warn("RpaJobLogfileMonitorConfig() : " + FILE_MAX_SIZE, e);
		} catch (Exception e) {
			m_log.warn("RpaJobLogfileMonitorConfig() : " + FILE_MAX_SIZE, e);
		}
		m_log.debug(FILE_MAX_SIZE + " = " + fileMaxSize);

		// 上限ファイル数
		String fileMaxStr = AgentProperties.getProperty(FILE_MAX_FILES, "500");
		m_log.info(FILE_MAX_FILES + " = " + fileMaxStr);
		try {
			fileMaxFiles = Integer.parseInt(fileMaxStr);
		} catch (Exception e) {
			m_log.warn("RpaJobLogfileMonitorConfig() : " + e.getMessage());
		}
		m_log.info(FILE_MAX_FILES + "=" + fileMaxFiles);

		// 上限ファイルサイズ（byte）
		String logfilMessageLimitLengthStr = AgentProperties.getProperty(MONITOR_LOGFILE_MESSAGE_LENGTH, "1048576");

		m_log.info(MONITOR_LOGFILE_MESSAGE_LENGTH + " = " + logfilMessageLimitLengthStr + " byte");
		try {
			logfilMessageLength = Integer.parseInt(logfilMessageLimitLengthStr);
		} catch (NumberFormatException e) {
			m_log.warn("RpaJobLogfileMonitorConfig() : " + MONITOR_LOGFILE_MESSAGE_LENGTH, e);
		} catch (Exception e) {
			m_log.warn("RpaJobLogfileMonitorConfig() : " + MONITOR_LOGFILE_MESSAGE_LENGTH, e);
		}
		m_log.debug(MONITOR_LOGFILE_MESSAGE_LENGTH + " = " + logfilMessageLength);

		// 上限読み込み行数
		String logfilMessageLimitLineStr = AgentProperties.getProperty(MONITOR_LOGFILE_MESSAGE_LINE, "1024");

		m_log.info(MONITOR_LOGFILE_MESSAGE_LINE + " = " + logfilMessageLimitLineStr + " line");
		try {
			logfilMessageLine = Integer.parseInt(logfilMessageLimitLineStr);
		} catch (NumberFormatException e) {
			m_log.warn("RpaJobLogfileMonitorConfig() : " + MONITOR_LOGFILE_MESSAGE_LINE, e);
		} catch (Exception e) {
			m_log.warn("RpaJobLogfileMonitorConfig() : " + MONITOR_LOGFILE_MESSAGE_LINE, e);
		}
		m_log.debug(MONITOR_LOGFILE_MESSAGE_LINE + " = " + logfilMessageLine);

		// ログファイル読込繰越データ長
		String logfileReadCarryOverLengthStr = AgentProperties.getProperty(READ_CARRYOVER_LENGTH, "102400");

		m_log.info(READ_CARRYOVER_LENGTH + " = " + logfileReadCarryOverLengthStr + " characters");
		try {
			logfileReadCarryOverLength = Integer.parseInt(logfileReadCarryOverLengthStr);
		} catch (NumberFormatException e) {
			m_log.warn("RpaJobLogfileMonitorConfig() : " + READ_CARRYOVER_LENGTH, e);
		} catch (Exception e) {
			m_log.warn("RpaJobLogfileMonitorConfig() : " + READ_CARRYOVER_LENGTH, e);
		}
		m_log.debug(READ_CARRYOVER_LENGTH + " = " + logfileReadCarryOverLength);
	}

	public static RpaJobLogfileMonitorConfig getInstance() {
		return instance;
	}

	@Override
	public int getMaxThreads() {
		// RPAシナリオジョブは独自起動方法をとっているため、本メソッドはサポートしない。
		m_log.warn("Not supported method.");
		throw new UnsupportedOperationException();
	}

	@Override
	public String getThreadName() {
		// RPAシナリオジョブは独自起動方法をとっているため、本メソッドはサポートしない。
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
