/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.sdml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;
import com.clustercontrol.logfile.bean.LogfileLineSeparatorConstant;

public class SdmlFileMonitorConfig implements FileMonitorConfig {
	// ロガー
	private static Log m_log = LogFactory.getLog(SdmlFileMonitorConfig.class);

	private static final String MAX_THREADS = "sdml.log.reader.threads";

	private static final String RUN_INTERVAL = "sdml.log.reader.read.interval";

	private static final String UNCHANGED_STATS_PERIOD = "sdml.log.reader.filesizecheck.period";

	private static final String FIRST_PART_DATA_CHECK_PERIOD = "sdml.log.reader.fileheadercheck.period";

	private static final String FIRST_PART_DATA_CHECK_SIZE = "sdml.log.reader.fileheadercheck.size";

	private static final String FILE_MAX_SIZE = "sdml.log.reader.maxsize";

	private static final String FILE_MAX_FILES = "sdml.log.reader.maxfiles";

	private static final String MAX_FILE_NOTIFY_INTERVAL_KEY = "sdml.log.reader.maxfiles.notify.interval";

	private static final String CONTROL_LOG_MAX_READ_BYTES = "sdml.control.log.max.bytes";

	private static final String CONTROL_LOG_RETURN_CODE = "sdml.control.log.file.return.code";

	private static final String CONTROL_LOG_CHARSET = "sdml.control.log.charset";

	/** ログファイル監視スレッド数上限 */
	private int maxThreads = 2;

	/** ログファイル監視間隔 */
	private int runInterval = 10000; // 10sec

	/** ファイル変更チェック期間設定（ミリ秒） */
	private int unchangedStatsPeriod = 5000; // 5sec

	/** ファイル変更詳細チェック（冒頭データ比較）期間（ミリ秒） */
	private int firstPartDataCheckPeriod = 300000; // 300sec

	/** ファイル変更詳細チェック（冒頭データ比較）サイズ（byte） */
	private int firstPartDataCheckSize = 256;

	/** 上限ファイルサイズ設定（byte） */
	private long fileMaxSize = 8589934592L;

	/** 上限ファイル数 */
	private long fileMaxFiles = 500;

	/** 上限ファイル数超過時の通知間隔 */
	private long fileMaxFileNotifyInterval = 300000;

	/** オリジナルメッセージのサイズ上限 */
	private int fileMessageLength = Integer.MAX_VALUE;

	/** オリジナルメッセージの読み込み行数上限 */
	private int fileMessageLine = Integer.MAX_VALUE;

	// 制御ログの最大読み取り文字数
	private int controlLogMaxReadBytes = -1;

	// 制御ログの改行コード
	private String controlLogReturnCode = LogfileLineSeparatorConstant.LF;

	// 制御ログのエンコード
	private String controlLogFileEncoding = "UTF-8";

	private static SdmlFileMonitorConfig instance = new SdmlFileMonitorConfig();

	private SdmlFileMonitorConfig() {
		// ログファイル監視スレッド数上限
		String maxThreadsStr = AgentProperties.getProperty(MAX_THREADS, Integer.toString(maxThreads));
		try {
			int value = Integer.parseInt(maxThreadsStr);
			if (value < 1) {
				throw new NumberFormatException("invalid value: " + maxThreadsStr);
			}
			maxThreads = value;
		} catch (NumberFormatException e) {
			m_log.warn("SdmlFileMonitorConfig() : " + MAX_THREADS + ", " + e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("SdmlFileMonitorConfig() : " + MAX_THREADS + ", " + e.getMessage(), e);
		}
		m_log.debug(MAX_THREADS + "=" + maxThreads);

		// ログファイル監視間隔 (ミリ秒)
		String runIntervalStr = AgentProperties.getProperty(RUN_INTERVAL, String.valueOf(runInterval));
		try {
			int value = Integer.parseInt(runIntervalStr);
			if (value < 1) {
				throw new NumberFormatException();
			}
			runInterval = value;
		} catch (NumberFormatException e) {
			m_log.warn("SdmlFileMonitorConfig() : " + RUN_INTERVAL, e);
		} catch (Exception e) {
			m_log.warn("SdmlFileMonitorConfig() : " + RUN_INTERVAL, e);
		}
		m_log.debug(RUN_INTERVAL + "=" + runInterval);

		// ファイル変更チェック期間（秒）
		String sleepInterval = AgentProperties.getProperty(UNCHANGED_STATS_PERIOD, "5");
		m_log.info(UNCHANGED_STATS_PERIOD + " = " + sleepInterval + " sec");
		try {
			int value = Integer.parseInt(sleepInterval);
			if (value < 0 || Integer.MAX_VALUE < (long)value * 1000) {
				throw new NumberFormatException();
			}
			unchangedStatsPeriod = value * 1000;
		} catch (NumberFormatException e) {
			m_log.warn("SdmlFileMonitorConfig() : " + UNCHANGED_STATS_PERIOD, e);
		} catch (Exception e) {
			m_log.warn("SdmlFileMonitorConfig() : " + UNCHANGED_STATS_PERIOD, e);
		}
		m_log.debug(UNCHANGED_STATS_PERIOD + " = " + unchangedStatsPeriod);

		// ファイル変更詳細チェック（冒頭データ比較）期間（秒）
		String firstPartDataCheckPeriodStr = AgentProperties.getProperty(FIRST_PART_DATA_CHECK_PERIOD, "300");
		try {
			int value = Integer.parseInt(firstPartDataCheckPeriodStr);
			if (value < 0 || Integer.MAX_VALUE < (long)value * 1000) {
				throw new NumberFormatException();
			}
			firstPartDataCheckPeriod = value * 1000;
		} catch (NumberFormatException e) {
			m_log.warn("SdmlFileMonitorConfig() : " + FIRST_PART_DATA_CHECK_PERIOD, e);
		} catch (Exception e) {
			m_log.warn("SdmlFileMonitorConfig() : " + FIRST_PART_DATA_CHECK_PERIOD, e);
		}
		m_log.debug(FIRST_PART_DATA_CHECK_PERIOD + " = " + firstPartDataCheckPeriod);

		// ファイル変更詳細チェック（冒頭データ比較）サイズ（byte）
		String firstPartDataCheckSizeStr = AgentProperties.getProperty(FIRST_PART_DATA_CHECK_SIZE, "256");
		try {
			int value = Integer.parseInt(firstPartDataCheckSizeStr);
			if (value < 1) {
				throw new NumberFormatException();
			}
			firstPartDataCheckSize = value;
		} catch (NumberFormatException e) {
			m_log.warn("SdmlFileMonitorConfig() : " + FIRST_PART_DATA_CHECK_SIZE, e);
		} catch (Exception e) {
			m_log.warn("SdmlFileMonitorConfig() : " + FIRST_PART_DATA_CHECK_SIZE, e);
		}
		m_log.debug(FIRST_PART_DATA_CHECK_SIZE + " = " + firstPartDataCheckSize);

		// 上限ファイルサイズ（byte）
		String fileMaxSizeStr = AgentProperties.getProperty(FILE_MAX_SIZE, String.valueOf(fileMaxSize));
		m_log.info(FILE_MAX_SIZE + " = " + fileMaxSizeStr + " byte");
		try {
			long value = Long.parseLong(fileMaxSizeStr);
			if (value < 1) {
				throw new NumberFormatException();
			}
			fileMaxSize = value;
		} catch (NumberFormatException e) {
			m_log.warn("SdmlFileMonitorConfig() : " + FILE_MAX_SIZE, e);
		} catch (Exception e) {
			m_log.warn("SdmlFileMonitorConfig() : " + FILE_MAX_SIZE, e);
		}
		m_log.debug(FILE_MAX_SIZE + " = " + fileMaxSize);

		// 上限ファイル数
		String fileMaxStr = AgentProperties.getProperty(FILE_MAX_FILES, "500");
		m_log.info(FILE_MAX_FILES + " = " + fileMaxStr);
		try {
			int value = Integer.parseInt(fileMaxStr);
			if (value < 1) {
				throw new NumberFormatException();
			}
			fileMaxFiles = value;
		} catch (NumberFormatException e) {
			m_log.warn("SdmlFileMonitorConfig() : " + FILE_MAX_FILES, e);
		} catch (Exception e) {
			m_log.warn("SdmlFileMonitorConfig() : " + e.getMessage());
		}
		m_log.debug(FILE_MAX_FILES + " = " + fileMaxFiles);

		// 最大ファイル数超過時の通知間隔
		String logfileMaxFileNotifyIntervalStr = AgentProperties.getProperty(MAX_FILE_NOTIFY_INTERVAL_KEY, String.valueOf(fileMaxFileNotifyInterval));
		try {
			long value = Long.parseLong(logfileMaxFileNotifyIntervalStr);
			if (value < 0) {
				throw new NumberFormatException();
			}
			fileMaxFileNotifyInterval = value;
		} catch (NumberFormatException e) {
			m_log.warn("SdmlFileMonitorConfig() : " + MAX_FILE_NOTIFY_INTERVAL_KEY, e);
		} catch (Exception e) {
			m_log.warn("SdmlFileMonitorConfig() : " + MAX_FILE_NOTIFY_INTERVAL_KEY, e);
		}
		m_log.debug(MAX_FILE_NOTIFY_INTERVAL_KEY + " = " + fileMaxFileNotifyInterval);

		// 制御ログの最大読み取り文字数
		String controlLogMaxReadBytesStr = AgentProperties.getProperty(CONTROL_LOG_MAX_READ_BYTES,
				String.valueOf(Integer.MAX_VALUE));
		try {
			int value = Integer.parseInt(controlLogMaxReadBytesStr);
			if (value != -1 && value < 1) {
				throw new NumberFormatException();
			}
			controlLogMaxReadBytes = value;
		} catch (NumberFormatException e) {
			m_log.warn("SdmlFileMonitorConfig() : " + CONTROL_LOG_MAX_READ_BYTES, e);
		} catch (Exception e) {
			m_log.warn("SdmlFileMonitorConfig() : " + CONTROL_LOG_MAX_READ_BYTES, e);
		}
		m_log.debug(CONTROL_LOG_MAX_READ_BYTES + " = " + controlLogMaxReadBytes);

		// 制御ログの改行コード
		{
			String value = AgentProperties.getProperty(CONTROL_LOG_RETURN_CODE, controlLogReturnCode);
			if (!LogfileLineSeparatorConstant.LF.equals(value) && !LogfileLineSeparatorConstant.CR.equals(value) && !LogfileLineSeparatorConstant.CRLF.equals(value)){
				m_log.warn("SdmlFileMonitorConfig() : " + CONTROL_LOG_RETURN_CODE + " is not support value." + value);
				value = LogfileLineSeparatorConstant.LF;
			}
			controlLogReturnCode = value;
		}
		m_log.debug(CONTROL_LOG_RETURN_CODE + " = " + controlLogReturnCode);

		// 制御ログのエンコーディング
		controlLogFileEncoding = AgentProperties.getProperty(CONTROL_LOG_CHARSET, controlLogFileEncoding);
		m_log.debug(CONTROL_LOG_CHARSET + " = " + controlLogFileEncoding);

	}

	public static SdmlFileMonitorConfig getInstance() {
		return instance;
	}

	@Override
	public int getMaxThreads() {
		return maxThreads;
	}

	@Override
	public String getThreadName() {
		// SdmlFileMonitor-execute-
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
		return fileMessageLength;
	}

	@Override
	public int getFilMessageLine() {
		return fileMessageLine;
	}

	@Override
	public int getFileReadCarryOverLength() {
		return Integer.MAX_VALUE;
	}

	@Override
	public String getProgram() {
		return null;
	}

	@Override
	public long getMaxFileNotifyInterval() {
		return fileMaxFileNotifyInterval;
	}

	public int getControlLogMaxReadBytes() {
		return controlLogMaxReadBytes;
	}

	public String getControlLogReturnCode() {
		return controlLogReturnCode;
	}

	public String getControlLogFileEncoding() {
		return controlLogFileEncoding;
	}
}
