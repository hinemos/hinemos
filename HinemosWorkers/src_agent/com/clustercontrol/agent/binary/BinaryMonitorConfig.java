/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.util.AgentProperties;

/**
 * バイナリファイルプロパティ定義.
 * 
 * @since 6.1.0
 * @version 6.1.0
 */
public class BinaryMonitorConfig {

	/** ロガー */
	private static Log m_log = LogFactory.getLog(BinaryMonitorConfig.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = " : ()";
	/** 初期化失敗定形分 */
	private static final String LOG_FAILURE_INIT = "failed initializing Config" + DELIMITER;

	// AgentProperties上のプロパティ名
	/** 上限ファイル数(プロパティ名) */
	private static final String FILE_MAX_FILES = "monitor.binary.filter.maxfiles";
	/** ファイル変更詳細チェック（冒頭データ比較）サイズ(プロパティ名) */
	private static final String FIRST_PART_DATA_CHECK_SIZE = "monitor.binary.filter.fileheadercheck.size";
	/** 取得バイナリ長（byte）(プロパティ名) */
	private static final String GET_BINARY_LENGTH = "monitor.binary.get.length";
	/** ファイル変更チェック期間設定(プロパティ名) */
	private static final String UNCHANGED_STATS_PERIOD = "monitor.binary.filter.unchanged.period";
	/** ファイル変更詳細チェック（冒頭データ比較）期間(プロパティ名) */
	private static final String FIRST_PART_DATA_CHECK_PERIOD = "monitor.binary.filter.fileheadercheck.period";
	/** 上限ファイルサイズ（byte）(プロパティ名) */
	private static final String FILE_MAX_SIZE = "monitor.binary.filter.maxsize";
	/** 1度の監視で取得するバイトの上限（Byte）(プロパティ名) */
	private static final String MAX_GET_BYTE = "monitor.binary.max.length";
	/** マネージャーへの送信サイズ(プロパティ名) */
	private static final String SEND_SIZE = "monitor.binary.send.size";
	/** 16進数表記最大長(プロパティ名) */
	private static final String HEXSTR_MAX_LENGTH = "monitor.binary.hexstring.max.length";
	/** ファイル増分監視スレッド間隔(msec)(プロパティ名) */
	private static final String INCREMENTAL_BINARY_INTERVAL = "monitor.incremental.binary.interval";
	/** ファイル全体監視スレッド間隔(msec)(プロパティ名) */
	private static final String WHOLE_BINARY_INTERVAL = "monitor.whole.binary.interval";
	/** 1パケット取得バイト(プロパティ名) */
	private static final String SNAP_LENGTH = "monitor.binary.packetcapture.snaplength";
	/** 読込タイムアウト(プロパティ名) */
	private static final String TIMEOUT_MILLIS = "monitor.binary.packetcapture.timeoutmillis";
	/** 出力PcapDumpサイズ(プロパティ名) */
	private static final String MAX_DUMP_SIZE = "monitor.binary.packetcapture.maxdumpsize";
	/** 出力PcapDump保存期間(プロパティ名) */
	private static final String DUMP_STORAGE_PERIOD = "monitor.binary.packetcapture.dumpstorageperiod";
	/** キャプチャーループ回数(プロパティ名) */
	private static final String CAP_LOOP_COUNT = "monitor.binary.packetcapture.caploopcount";
	/** キャプチャー間隔(プロパティ名) */
	private static final String CAP_INTERVAL = "monitor.binary.packetcapture.capinterval";
	/** パケットキャプチャ出力先ディレクトリ(プロパティ名) */
	private static final String PCAP_EXPORT_DIR = "monitor.binary.packetcapture.directory";
	/** バイナリ送信キュー上限サイズ(プロパティ名) */
	private static final String BIN_FOWRDING_QUEUE_MAX_SIZE = "monitor.binary.forwarding.queue.maxsize";
	/** バイナリ送信上限サイズ(プロパティ名) */
	private static final String BIN_FOWRDING_TRANSPORT_MAX_SIZE = "monitor.binary.forwarding.transport.maxsize";
	/** バイナリ送信上限トライ数(プロパティ名) */
	private static final String BIN_FOWRDING_TRANSPORT_MAX_TRY = "monitor.binary.forwarding.transport.maxtries";
	/** バイナリ送信間隔サイズ(プロパティ名) */
	private static final String BIN_FOWRDING_TRANSPORT_INTERVAL = "monitor.binary.forwarding.transport.interval.size";
	/** バイナリ送信間隔(msec)(プロパティ名) */
	private static final String BIN_FOWRDING_TRANSPORT_INTERVAL_MSEC = "monitor.binary.forwarding.transport.interval.msec";

	// 各プロパティJava定義.
	/** 上限ファイル数 */
	private static long fileMaxFiles = 0;
	/** ファイル変更詳細チェック（冒頭データ比較）サイズ（byte） */
	private static int firstPartDataCheckSize = 0;
	/** 取得バイナリ長（byte）(監視時にファイルから1度に取得するバイト長) */
	private static int gettingBinaryLength = 0;
	/** ファイル変更チェック期間設定（ミリ秒） */
	private static int unchangedStatsPeriod = 0;
	/** ファイル変更詳細チェック（冒頭データ比較）期間（ミリ秒） */
	private static int firstPartDataCheckPeriod = 0;
	/** 上限ファイルサイズ設定（byte） */
	private static long fileMaxSize = 0L;
	/** 1度の監視で取得するバイトの上限（Byte） */
	private static int maxGetByte = 0;
	/** マネージャーへの送信サイズ(Byte) */
	private static int sendSize = 0;
	/** 16進数表記最大長(プロパティ名) */
	private static int hexstrMaxLength = 0;
	/** ファイル増分監視スレッド間隔(msec) */
	private static int incrementBinInterval = 10000; // 10sec
	/** ファイル全体監視スレッド間隔(msec) */
	private static int wholeBinInterval = 10000; // 10sec
	/** 1パケット取得バイト(byte)(最大パケット長65,535) */
	private static int snapLength = 0;
	/** 読込タイムアウト(ミリ秒) */
	private static int timeoutMillis = 0;
	/** 出力PcapDumpサイズ(byte) */
	private static long maxDumpSize = 0L;
	/** 出力PcapDump保存期間(プロパティ名) */
	private static int dumpStoragePeriod = 0;
	/** キャプチャーループ回数 */
	private static int capLoopCount = 0;
	/** キャプチャー間隔(ミリ秒) */
	private static int capInterval = 0;
	/** パケットキャプチャ出力先ディレクトリ */
	private static String pcapExportDir = "";
	/** バイナリ送信キュー上限サイズ */
	private static int binForwaringQueueMaxSize = 0;
	/** バイナリ送信上限サイズ */
	private static int binForwaringTransportMaxSize = 0;
	/** バイナリ送信上限トライ数 */
	private static int binForwaringTransportMaxTry = 0;
	/** バイナリ送信間隔サイズ(プロパティ名) */
	private static int binForwaringTransportInterval = 0;
	/** バイナリ送信間隔(msec)(プロパティ名) */
	private static long binForwaringTransportIntervalMsec = 0;

	/**
	 * プロパティの初期化(設定ファイル変更時、要再起動).
	 */
	static {
		// ファイル変更チェック期間（秒）
		String sleepInterval = AgentProperties.getProperty(UNCHANGED_STATS_PERIOD, "600");
		m_log.info(UNCHANGED_STATS_PERIOD + " = " + sleepInterval + " sec");
		try {
			unchangedStatsPeriod = Integer.parseInt(sleepInterval) * 1000;
		} catch (NumberFormatException e) {
			m_log.warn("BinaryMonitorConfig() : " + UNCHANGED_STATS_PERIOD, e);
		} catch (Exception e) {
			m_log.warn("BinaryMonitorConfig() : " + UNCHANGED_STATS_PERIOD, e);
		}
		m_log.debug(UNCHANGED_STATS_PERIOD + " = " + unchangedStatsPeriod);

		// ファイル変更詳細チェック（冒頭データ比較）期間（秒）
		String firstPartDataCheckPeriodStr = AgentProperties.getProperty(FIRST_PART_DATA_CHECK_PERIOD, "300");
		try {
			firstPartDataCheckPeriod = Integer.parseInt(firstPartDataCheckPeriodStr) * 1000;
		} catch (NumberFormatException e) {
			m_log.warn("BinaryMonitorConfig() : " + FIRST_PART_DATA_CHECK_PERIOD, e);
		} catch (Exception e) {
			m_log.warn("BinaryMonitorConfig() : " + FIRST_PART_DATA_CHECK_PERIOD, e);
		}
		m_log.debug(FIRST_PART_DATA_CHECK_PERIOD + " = " + firstPartDataCheckPeriod);

		// ファイル変更詳細チェック（冒頭データ比較）サイズ（byte）
		String firstPartDataCheckSizeStr = AgentProperties.getProperty(FIRST_PART_DATA_CHECK_SIZE, "256");
		try {
			firstPartDataCheckSize = Integer.parseInt(firstPartDataCheckSizeStr);
		} catch (NumberFormatException e) {
			m_log.warn("BinaryMonitorConfig() : " + FIRST_PART_DATA_CHECK_SIZE, e);
		} catch (Exception e) {
			m_log.warn("BinaryMonitorConfig() : " + FIRST_PART_DATA_CHECK_SIZE, e);
		}
		m_log.debug(FIRST_PART_DATA_CHECK_SIZE + " = " + firstPartDataCheckSize);

		// 上限ファイルサイズ（byte）
		String fileMaxSizeStr = AgentProperties.getProperty(FILE_MAX_SIZE, "8589934592");
		m_log.info(FILE_MAX_SIZE + " = " + fileMaxSizeStr + " byte");
		try {
			fileMaxSize = Long.parseLong(fileMaxSizeStr);
		} catch (NumberFormatException e) {
			m_log.warn("BinaryMonitorConfig() : " + FILE_MAX_SIZE, e);
		} catch (Exception e) {
			m_log.warn("BinaryMonitorConfig() : " + FILE_MAX_SIZE, e);
		}
		m_log.debug(FILE_MAX_SIZE + " = " + fileMaxSize);

		// 上限ファイル数
		String fileMaxStr = AgentProperties.getProperty(FILE_MAX_FILES, "500");
		m_log.info(FILE_MAX_FILES + " = " + fileMaxStr);
		try {
			fileMaxFiles = Integer.parseInt(fileMaxStr);
		} catch (Exception e) {
			m_log.warn("BinaryMonitorConfig : " + e.getMessage());
		}
		m_log.info(FILE_MAX_FILES + "=" + fileMaxFiles);

		// 1度の監視で読込む最大サイズ（byte）
		String logfilMessageLimitLengthStr = AgentProperties.getProperty(MAX_GET_BYTE, "65536");

		m_log.info(MAX_GET_BYTE + " = " + logfilMessageLimitLengthStr + " byte");
		try {
			maxGetByte = Integer.parseInt(logfilMessageLimitLengthStr);
		} catch (NumberFormatException e) {
			m_log.warn("BinaryMonitorConfig() : " + MAX_GET_BYTE, e);
		} catch (Exception e) {
			m_log.warn("BinaryMonitorConfig() : " + MAX_GET_BYTE, e);
		}
		m_log.debug(MAX_GET_BYTE + " = " + maxGetByte);

		// 取得バイナリ長
		String getBinaryLengthStr = AgentProperties.getProperty(GET_BINARY_LENGTH, "1024");

		m_log.info(GET_BINARY_LENGTH + " = " + getBinaryLengthStr + " byte");
		try {
			gettingBinaryLength = Integer.parseInt(getBinaryLengthStr);
		} catch (NumberFormatException e) {
			m_log.warn("BinaryMonitorConfig() : " + GET_BINARY_LENGTH, e);
		} catch (Exception e) {
			m_log.warn("BinaryMonitorConfig() : " + GET_BINARY_LENGTH, e);
		}
		m_log.debug(GET_BINARY_LENGTH + " = " + gettingBinaryLength);

		// マネージャーへの送信サイズ
		String sendSizeStr = AgentProperties.getProperty(SEND_SIZE, "131072");

		m_log.info(SEND_SIZE + " = " + sendSizeStr + " byte");
		try {
			sendSize = Integer.parseInt(sendSizeStr);
		} catch (NumberFormatException e) {
			m_log.warn("BinaryMonitorConfig() : " + SEND_SIZE, e);
		} catch (Exception e) {
			m_log.warn("BinaryMonitorConfig() : " + SEND_SIZE, e);
		}
		m_log.debug(SEND_SIZE + " = " + sendSize);

		// 16進数表記最大長
		String hexstrMaxLengthStr = AgentProperties.getProperty(HEXSTR_MAX_LENGTH, "256");

		m_log.info(HEXSTR_MAX_LENGTH + " = " + hexstrMaxLengthStr);
		try {
			hexstrMaxLength = Integer.parseInt(hexstrMaxLengthStr);
		} catch (NumberFormatException e) {
			m_log.warn("BinaryMonitorConfig() : " + HEXSTR_MAX_LENGTH, e);
		} catch (Exception e) {
			m_log.warn("BinaryMonitorConfig() : " + HEXSTR_MAX_LENGTH, e);
		}
		m_log.debug(HEXSTR_MAX_LENGTH + " = " + hexstrMaxLength);

		// ファイル増分監視スレッド間隔(msec)
		String incrementBinIntervalStr = AgentProperties.getProperty(INCREMENTAL_BINARY_INTERVAL, "10000");

		m_log.info(INCREMENTAL_BINARY_INTERVAL + " = " + incrementBinIntervalStr);
		try {
			incrementBinInterval = Integer.parseInt(incrementBinIntervalStr);
		} catch (NumberFormatException e) {
			m_log.warn("BinaryMonitorConfig() : " + INCREMENTAL_BINARY_INTERVAL, e);
		} catch (Exception e) {
			m_log.warn("BinaryMonitorConfig() : " + INCREMENTAL_BINARY_INTERVAL, e);
		}
		m_log.debug(INCREMENTAL_BINARY_INTERVAL + " = " + incrementBinInterval);

		// ファイル全体監視スレッド間隔(msec)
		String wholeBinIntervalStr = AgentProperties.getProperty(WHOLE_BINARY_INTERVAL, "10000");

		m_log.info(WHOLE_BINARY_INTERVAL + " = " + wholeBinIntervalStr);
		try {
			wholeBinInterval = Integer.parseInt(wholeBinIntervalStr);
		} catch (NumberFormatException e) {
			m_log.warn("BinaryMonitorConfig() : " + WHOLE_BINARY_INTERVAL, e);
		} catch (Exception e) {
			m_log.warn("BinaryMonitorConfig() : " + WHOLE_BINARY_INTERVAL, e);
		}
		m_log.debug(WHOLE_BINARY_INTERVAL + " = " + wholeBinInterval);

		// 1パケット取得バイト(byte)
		String snapLengthStr = AgentProperties.getProperty(SNAP_LENGTH, "65536");
		m_log.info(SNAP_LENGTH + " = " + snapLengthStr + " byte");
		try {
			snapLength = Integer.parseInt(snapLengthStr);
		} catch (NumberFormatException e) {
			m_log.warn(LOG_FAILURE_INIT + SNAP_LENGTH, e);
		} catch (Exception e) {
			m_log.warn(LOG_FAILURE_INIT + SNAP_LENGTH, e);
		}
		m_log.debug(SNAP_LENGTH + " = " + snapLength);

		// 読込タイムアウト(ミリ秒)
		String timeoutMillisStr = AgentProperties.getProperty(TIMEOUT_MILLIS, "10");
		m_log.info(TIMEOUT_MILLIS + " = " + timeoutMillisStr + " ms");
		try {
			timeoutMillis = Integer.parseInt(timeoutMillisStr);
		} catch (NumberFormatException e) {
			m_log.warn(LOG_FAILURE_INIT + TIMEOUT_MILLIS, e);
		} catch (Exception e) {
			m_log.warn(LOG_FAILURE_INIT + TIMEOUT_MILLIS, e);
		}
		m_log.debug(TIMEOUT_MILLIS + " = " + timeoutMillis);

		// 出力PcapDumpサイズ(byte)
		String maxDumpSizeStr = AgentProperties.getProperty(MAX_DUMP_SIZE, "1073741823");
		m_log.info(MAX_DUMP_SIZE + " = " + maxDumpSizeStr + " byte");
		try {
			maxDumpSize = Integer.parseInt(maxDumpSizeStr);
		} catch (NumberFormatException e) {
			m_log.warn(LOG_FAILURE_INIT + MAX_DUMP_SIZE, e);
		} catch (Exception e) {
			m_log.warn(LOG_FAILURE_INIT + MAX_DUMP_SIZE, e);
		}
		m_log.debug(MAX_DUMP_SIZE + " = " + maxDumpSize);

		// 出力PcapDump保存期間(プロパティ名)
		String dumpStoragePeriodStr = AgentProperties.getProperty(DUMP_STORAGE_PERIOD, "24");
		m_log.info(DUMP_STORAGE_PERIOD + " = " + dumpStoragePeriodStr + " hours");
		try {
			dumpStoragePeriod = Integer.parseInt(dumpStoragePeriodStr);
		} catch (NumberFormatException e) {
			m_log.warn(LOG_FAILURE_INIT + DUMP_STORAGE_PERIOD, e);
		} catch (Exception e) {
			m_log.warn(LOG_FAILURE_INIT + DUMP_STORAGE_PERIOD, e);
		}
		m_log.debug(DUMP_STORAGE_PERIOD + " = " + dumpStoragePeriod);

		// キャプチャーループ回数
		String capLoopCountStr = AgentProperties.getProperty(CAP_LOOP_COUNT, "20");
		m_log.info(CAP_LOOP_COUNT + " = " + capLoopCountStr + " counts");
		try {
			capLoopCount = Integer.parseInt(capLoopCountStr);
		} catch (NumberFormatException e) {
			m_log.warn(LOG_FAILURE_INIT + CAP_LOOP_COUNT, e);
		} catch (Exception e) {
			m_log.warn(LOG_FAILURE_INIT + CAP_LOOP_COUNT, e);
		}
		m_log.debug(CAP_LOOP_COUNT + " = " + capLoopCount);

		// キャプチャー間隔
		String capIntervalStr = AgentProperties.getProperty(CAP_INTERVAL, "500");
		m_log.info(CAP_INTERVAL + " = " + capIntervalStr + " ms");
		try {
			capInterval = Integer.parseInt(capIntervalStr);
		} catch (NumberFormatException e) {
			m_log.warn(LOG_FAILURE_INIT + CAP_INTERVAL, e);
		} catch (Exception e) {
			m_log.warn(LOG_FAILURE_INIT + CAP_INTERVAL, e);
		}
		m_log.debug(CAP_INTERVAL + " = " + capInterval);
	}

	// 再起動不要プロパティのgetter.
	/** パケットキャプチャ出力先ディレクトリ */
	public static String getPcapExportDir() {
		String home = Agent.getAgentHome();
		String storeDir = new File(new File(home), "/var/run/pcap_dump").getAbsolutePath();
		pcapExportDir = AgentProperties.getProperty(PCAP_EXPORT_DIR, storeDir);
		m_log.info(PCAP_EXPORT_DIR + " = " + pcapExportDir);
		try {
			int last = 0;
			if (home.endsWith("/")) {
				last = home.lastIndexOf("/");
				home = home.substring(0, last);
			} else if (home.endsWith("\\")) {
				last = home.lastIndexOf("\\");
				home = home.substring(0, last);
			}
			m_log.debug("home = " + home);
			pcapExportDir = pcapExportDir.replace("%%HINEMOS_AGENT_HOME%%", home);
		} catch (Exception e) {
			m_log.warn("BinaryMonitorConfig() : " + PCAP_EXPORT_DIR, e);
		}
		m_log.debug(PCAP_EXPORT_DIR + " = " + pcapExportDir);
		return pcapExportDir;
	}

	/** バイナリ送信キュー上限サイズ */
	public static int getBinForwaringQueueMaxSize() {
		int valueDefault = 5000;
		String binForwaringQueueMaxSizeStr = AgentProperties.getProperty(BIN_FOWRDING_QUEUE_MAX_SIZE,
				Integer.toString(valueDefault));
		m_log.info(BIN_FOWRDING_QUEUE_MAX_SIZE + " = " + binForwaringQueueMaxSizeStr);
		try {
			binForwaringQueueMaxSize = Integer.parseInt(binForwaringQueueMaxSizeStr);
			if (binForwaringQueueMaxSize != -1 && binForwaringQueueMaxSize < 1) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			m_log.warn(LOG_FAILURE_INIT + BIN_FOWRDING_QUEUE_MAX_SIZE, e);
			binForwaringQueueMaxSize = valueDefault;
		} catch (Exception e) {
			m_log.warn(LOG_FAILURE_INIT + BIN_FOWRDING_QUEUE_MAX_SIZE, e);
			binForwaringQueueMaxSize = valueDefault;
		}
		m_log.debug(BIN_FOWRDING_QUEUE_MAX_SIZE + " = " + binForwaringQueueMaxSize);
		return binForwaringQueueMaxSize;
	}

	/** バイナリ送信上限サイズ */
	public static int getBinForwaringTransportMaxSize() {
		int valueDefault = 100;
		String binForwaringTransportMaxSizeStr = AgentProperties.getProperty(BIN_FOWRDING_TRANSPORT_MAX_SIZE,
				Integer.toString(valueDefault));
		m_log.info(BIN_FOWRDING_TRANSPORT_MAX_SIZE + " = " + binForwaringTransportMaxSizeStr);
		try {
			binForwaringTransportMaxSize = Integer.parseInt(binForwaringTransportMaxSizeStr);
			if (binForwaringTransportMaxSize != -1 && binForwaringTransportMaxSize < 1) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			m_log.warn(LOG_FAILURE_INIT + BIN_FOWRDING_TRANSPORT_MAX_SIZE, e);
			binForwaringTransportMaxSize = valueDefault;
		} catch (Exception e) {
			m_log.warn(LOG_FAILURE_INIT + BIN_FOWRDING_TRANSPORT_MAX_SIZE, e);
			binForwaringTransportMaxSize = valueDefault;
		}
		m_log.debug(BIN_FOWRDING_TRANSPORT_MAX_SIZE + " = " + binForwaringTransportMaxSize);
		return binForwaringTransportMaxSize;
	}

	/** バイナリ送信上限トライ数 */
	public static int getBinForwaringTransportMaxTry() {
		int valueDefault = 900;
		String valueString = AgentProperties.getProperty(BIN_FOWRDING_TRANSPORT_MAX_TRY,
				Integer.toString(valueDefault));
		m_log.info(BIN_FOWRDING_TRANSPORT_MAX_TRY + " = " + valueString);
		try {
			binForwaringTransportMaxTry = Integer.parseInt(valueString);
			if (binForwaringTransportMaxTry != -1 && binForwaringTransportMaxTry < 1) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			m_log.warn(LOG_FAILURE_INIT + BIN_FOWRDING_TRANSPORT_MAX_TRY, e);
			binForwaringTransportMaxTry = valueDefault;
		} catch (Exception e) {
			m_log.warn(LOG_FAILURE_INIT + BIN_FOWRDING_TRANSPORT_MAX_TRY, e);
			binForwaringTransportMaxTry = valueDefault;
		}
		m_log.debug(BIN_FOWRDING_TRANSPORT_MAX_TRY + " = " + binForwaringTransportMaxTry);
		return binForwaringTransportMaxTry;
	}

	/** バイナリ送信間隔サイズ(プロパティ名) */
	public static int getBinForwaringTransportInterval() {
		int valueDefault = 15;
		String valueString = AgentProperties.getProperty(BIN_FOWRDING_TRANSPORT_INTERVAL,
				Integer.toString(valueDefault));
		m_log.info(BIN_FOWRDING_TRANSPORT_INTERVAL + " = " + valueString);
		try {
			binForwaringTransportInterval = Integer.parseInt(valueString);
			if (binForwaringTransportInterval != -1 && binForwaringTransportInterval < 1) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			m_log.warn(LOG_FAILURE_INIT + BIN_FOWRDING_TRANSPORT_INTERVAL, e);
			binForwaringTransportInterval = valueDefault;
		} catch (Exception e) {
			m_log.warn(LOG_FAILURE_INIT + BIN_FOWRDING_TRANSPORT_INTERVAL, e);
			binForwaringTransportInterval = valueDefault;
		}
		m_log.debug(BIN_FOWRDING_TRANSPORT_INTERVAL + " = " + binForwaringTransportInterval);
		return binForwaringTransportInterval;
	}

	/** バイナリ送信間隔(プロパティ名) */
	public static long getBinForwaringTransportIntervalMsec() {
		long valueDefault = 1000L;
		String valueString = AgentProperties.getProperty(BIN_FOWRDING_TRANSPORT_INTERVAL_MSEC,
				Long.toString(valueDefault));
		m_log.info(BIN_FOWRDING_TRANSPORT_INTERVAL_MSEC + " = " + valueString);
		try {
			binForwaringTransportIntervalMsec = Long.parseLong(valueString);
			if (binForwaringTransportIntervalMsec != -1 && binForwaringTransportIntervalMsec < 1) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			m_log.warn(LOG_FAILURE_INIT + BIN_FOWRDING_TRANSPORT_INTERVAL_MSEC, e);
			binForwaringTransportIntervalMsec = valueDefault;
		} catch (Exception e) {
			m_log.warn(LOG_FAILURE_INIT + BIN_FOWRDING_TRANSPORT_INTERVAL_MSEC, e);
			binForwaringTransportIntervalMsec = valueDefault;
		}
		m_log.debug(BIN_FOWRDING_TRANSPORT_INTERVAL_MSEC + " = " + binForwaringTransportIntervalMsec);
		return binForwaringTransportIntervalMsec;
	}

	// 各フィールドgetter(書込み禁止なのでsetterはなし).
	/** 上限ファイル数 */
	public static long getFileMaxFiles() {
		return fileMaxFiles;
	}

	/** ファイル変更詳細チェック（冒頭データ比較）サイズ（byte） */
	public static int getFirstPartDataCheckSize() {
		return firstPartDataCheckSize;
	}

	/** 取得バイナリ長（byte）(監視時にファイルから1度に取得するバイト長) */
	public static int getGettingBinaryLength() {
		return gettingBinaryLength;
	}

	/** ファイル変更チェック期間設定（ミリ秒） */
	public static int getUnchangedStatsPeriod() {
		return unchangedStatsPeriod;
	}

	/** ファイル変更詳細チェック（冒頭データ比較）期間（ミリ秒） */
	public static int getFirstPartDataCheckPeriod() {
		return firstPartDataCheckPeriod;
	}

	/** 上限ファイルサイズ設定（byte） */
	public static long getFileMaxSize() {
		return fileMaxSize;
	}

	/** 1度の監視で取得するバイトの上限（Byte） */
	public static int getMaxGetByte() {
		return maxGetByte;
	}

	/** マネージャーへの送信サイズ(Byte) */
	public static int getSendSize() {
		return sendSize;
	}

	/** 16進数表記最大長(プロパティ名) */
	public static int getHexstrMaxLength() {
		return hexstrMaxLength;
	}

	/** ファイル増分監視スレッド間隔(msec) */
	public static int getIncrementBinInterval() {
		return incrementBinInterval;
	}

	/** ファイル全体監視スレッド間隔(msec) */
	public static int getWholeBinInterval() {
		return wholeBinInterval;
	}

	/** 1パケット取得バイト(byte)(最大パケット長65,535) */
	public static int getSnapLength() {
		return snapLength;
	}

	/** 読込タイムアウト(ミリ秒) */
	public static int getTimeoutMillis() {
		return timeoutMillis;
	}

	/** 出力PcapDumpサイズ(byte) */
	public static long getMaxDumpSize() {
		return maxDumpSize;
	}

	/** 出力PcapDump保存期間(プロパティ名) */
	public static int getDumpStoragePeriod() {
		return dumpStoragePeriod;
	}

	/** キャプチャーループ回数 */
	public static int getCapLoopCount() {
		return capLoopCount;
	}

	/** キャプチャー間隔(ミリ秒) */
	public static int getCapInterval() {
		return capInterval;
	}
}
