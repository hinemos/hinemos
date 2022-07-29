/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logging;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import com.clustercontrol.log.control.ControlLogManager;
import com.clustercontrol.log.internal.InternalLogManager;
import com.clustercontrol.log.monitoring.MonitoringLogManager;
import com.clustercontrol.logging.exception.LoggingPropertyException;
import com.clustercontrol.logging.property.LoggingProperty;
import com.clustercontrol.logging.util.ProcessInfo;

/**
 * 各種ログファイルの起動、終了を構成するクラスです<BR>
 */

public class LoggingConfigurator {
	/** HinemosLoggingの起動日時 */
	private static long startupTime;
	/** 起動フラグ */
	private static boolean run = false;
	/** インスタンスマネージャ */
	private static LoggingInstanceManager manager;

	/** 内部ログのロガー */
	private static InternalLogManager.Logger internalLog;
	/** ホスト名 */
	private static String hostname;
	private static final String HOSTNAME_DEFAULT = "localhost";

	/**
	 * HinemosLoggingの起動処理
	 * 順序を考慮して各種ログを構成します。
	 * 
	 * @param properyFile 設定ファイルのInputStream
	 */
	public static void start(InputStream properyFile) throws Exception {
		// 現在の日時を起動日時に設定
		startupTime = System.currentTimeMillis();
		manager = new LoggingInstanceManager();

		// 設定ファイル読み込み
		try {
			manager.initializeLoggingPropety(properyFile);
		} catch (IOException | LoggingPropertyException e) {
			throw e;
		} finally {
			if (properyFile != null) {
				try {
					properyFile.close();
				} catch (IOException e) {
					throw e;
				}
			}
		}

		// 内部ログの初期化
		try {
			InternalLogManager.init();

			internalLog = InternalLogManager.getLogger(LoggingConfigurator.class);
			internalLog.info("start : Hinemos Logging is initializing...");
			internalLog.info("start : Startup Time="
					+ new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date(startupTime)));
		} catch (FileNotFoundException e) {
			throw e;
		}

		// 内部ログの初期化後に起動とする
		run = true;

		try {
			// ホスト名の取得
			hostname = getSysHostname();
			// プロセス情報の初期化
			manager.initializeProcessInfo();

			// 制御ログの初期設定
			ControlLogManager.init();

			// 監視ログの初期設定
			MonitoringLogManager.init();

			// プロセス内部監視の開始
			manager.startInternalMonitor();

			internalLog.info("start : Hinemos Logging is started.");
		} catch (Throwable t) {
			internalLog.error("start : Initialization failed. " + t.getMessage(), t);
			stop(true);
		}
	}

	/**
	 * HinemosLoggingの停止処理<br>
	 * <br>
	 * 初回の呼び出しでHinemosLoggingは停止し、2回目以降の呼び出しは無視されます。<br>
	 * 呼び出しは同期的に行われることを前提としています。
	 * 
	 * @param stopAppender
	 *            独自に追加したAppenderを停止するかどうか<br>
	 *            Log4j2の停止処理が働いている場合はそちらに委ねます<br>
	 */
	public static void stop(boolean stopAppender) {
		if (!run) {
			// 停止処理の2回目起動の防止
			return;
		}
		try {
			internalLog.info("stop : Hinemos Logging is stopping.");

			// プロセス内部監視の停止
			manager.stopInternalMonitor();

			// Stopの出力
			ControlLogManager.getLogger(LoggingConfigurator.class).stop();

			// 各アペンダーの停止
			MonitoringLogManager.stop(stopAppender);
			ControlLogManager.stop(stopAppender);

		} catch (Throwable t) {
			internalLog.error("stop : " + t.getMessage(), t);
		} finally {
			internalLog.info("stop : Hinemos Logging is stopped.");
			// 内部ログの停止
			InternalLogManager.stop(stopAppender);

			// インスタンスの参照を明示的に破棄する
			manager = null;
			run = false;
		}
	}

	public static String getHostname() {
		return hostname;
	}

	public static long getStartupTime() {
		return startupTime;
	}

	public static LoggingProperty getProperty() {
		return manager.getLoggingProperty();
	}

	public static ProcessInfo getProcessInfo() {
		return manager.getProcessInfo();
	}

	private static String getSysHostname() {
		// 末尾 ":" チェック . <PRI>DATE TAG MSG形式（主に商用Unix）を想定
		// ブランク チェック . <PRI>DATE last message repeat を想定
		// [ ] 囲い込み補正 .特定のsyslogdにてホスト名を[]にて囲う仕様に対応
		String hostname = "";
		// ホスト名の取得
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			internalLog.warn("getSysHostname : get hostname failed. " + e.getMessage());
		}

		if (hostname.equals("") || hostname.substring(hostname.length() - 1).equals(":")) {
			return getSysIpAddress();
		} else if (hostname.substring(0, 1).equals("[") && hostname.substring(hostname.length() - 1).equals("]")) {
			hostname = hostname.substring(1, hostname.length() - 1);
			return hostname;
		}

		// ホスト名許容文字チェック 半角英数 . / - _ であること
		for (int cnt = 0; cnt < hostname.length(); cnt++) {
			if (Character.isDigit(hostname.charAt(cnt)) || Character.isUpperCase(hostname.charAt(cnt))
					|| Character.isLowerCase(hostname.charAt(cnt)) || hostname.charAt(cnt) == '.'
					|| hostname.charAt(cnt) == '/' || hostname.charAt(cnt) == '_' || hostname.charAt(cnt) == '-') {
				continue;
			}
			internalLog.warn("getSysHostname : hostname contains forbidden characters.");
			return HOSTNAME_DEFAULT;
		}

		if (hostname.length() >= 255) {
			internalLog.warn("getSysHostname : hostname exceeds 255 characters.");
			return HOSTNAME_DEFAULT;
		}
		return hostname;
	}

	private static String getSysIpAddress() {
		// IPアドレス取得
		Enumeration<NetworkInterface> networkInterfaces;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
			ArrayList<String> newIpAddressList = new ArrayList<String>();
			if (null != networkInterfaces) {
				while (networkInterfaces.hasMoreElements()) {
					NetworkInterface ni = networkInterfaces.nextElement();
					Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
					while (inetAddresses.hasMoreElements()) {
						InetAddress in = inetAddresses.nextElement();
						if (in.isLoopbackAddress()) {
							continue;
						}
						String hostAddress = in.getHostAddress();
						if (hostAddress != null && !hostAddress.startsWith("0:0:0:0:0:0:0:1")
								&& !hostAddress.equals("::1")) {
							newIpAddressList.add(hostAddress);
						}
					}
				}
			}
			if (newIpAddressList.isEmpty()) {
				return HOSTNAME_DEFAULT;
			} else {
				// IPアドレスは複数ある場合は最初に取得したもの（どれが妥当かまで判断するのは難しい)
				internalLog.debug("getSysIpAddress : " + String.join(",", newIpAddressList));
				return newIpAddressList.get(0);
			}
		} catch (SocketException e) {
			internalLog.warn("getSysIpAddress : get IpAddress failed. " + e.getMessage());
			return HOSTNAME_DEFAULT;
		}

	}
}
