/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logging;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

import com.clustercontrol.log.control.ControlLogManager;
import com.clustercontrol.log.internal.InternalLogManager;
import com.clustercontrol.log.monitoring.MonitoringLogManager;

/**
 * 各種ログファイルの起動、終了を構成するクラスです<BR>
 */

public class LoggingConfigurator {
	private static final String INFO_LOGGING_START = "Hinemos Logging is started.";
	private static final String INFO_LOGGING_STOP = "Hinemos Logging is stopped.";
	private static ControlLogManager.Logger controlLog = ControlLogManager.getLogger(LoggingConfigurator.class);
	private static InternalLogManager.Logger internalLog = InternalLogManager.getLogger(LoggingConfigurator.class);

	/** ホスト名 */
	private static String hostname;
	/** Servlet（Webアプリケーション）かどうか */
	private static boolean isServlet;

	public static String getHostname() {
		return hostname;
	}

	public static boolean isServlet() {
		return isServlet;
	}

	/**
	 * 起動時の各種ログを構成します。
	 * 
	 * @throws Exception
	 *
	 */
	public static void init(boolean isServlet) throws Exception {
		LoggingConfigurator.isServlet = isServlet;

		try {
			getSysHostname();

			// 制御ログの初期設定
			ControlLogManager.init();

			// 監視ログの初期設定
			MonitoringLogManager.init();

			// プロセス内部監視の開始
			InternalMonitor.getInstance().start();

			internalLog.info("init : " + INFO_LOGGING_START);
		} catch (Exception e) {
			stop();
			throw e;
		}

	}

	public static void stop() {
		// プロセス内部監視の停止
		InternalMonitor.getInstance().stop();
		// 各アペンダーの停止
		stopAppender();
	}

	public static void stopAppender() {
		if (MonitoringLogManager.isRun()) {
			MonitoringLogManager.stop();
		}
		if (ControlLogManager.isRun()) {
			controlLog.stop();
			ControlLogManager.stop();
		}
		internalLog.info("stop : " + INFO_LOGGING_STOP);
		if (InternalLogManager.isRun()) {
			InternalLogManager.stop();
		}
	}

	private static String getSysHostname() throws Exception {
		// 末尾 ":" チェック . <PRI>DATE TAG MSG形式（主に商用Unix）を想定
		// ブランク チェック . <PRI>DATE last message repeat を想定
		// [ ] 囲い込み補正 .特定のsyslogdにてホスト名を[]にて囲う仕様に対応

		// ホスト名の取得
		// 取得するものに差異があるかもしれない（https://qiita.com/zakisanbaiman/items/8c3df06efad92c489e7d）
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			internalLog.error("getSysHostname : get hostname failed. " + e.getMessage());
			throw e;
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
			internalLog.error("getSysHostname : hostname contains forbidden characters.");
			throw new Exception("getSysHostname : hostname contains forbidden characters.");
		}

		if (hostname.length() >= 255) {
			internalLog.error("getSysHostname : hostname exceeds 255 characters.");
			throw new Exception("getSysHostname : hostname exceeds 255 characters.");
		}
		return hostname;
	}

	private static String getSysIpAddress() throws SocketException {
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
				return "localhost";
			} else {
				// IPアドレスは複数ある場合は最初に取得したもの（どれが妥当かまで判断するのは難しい)
				internalLog.debug("getSysIpAddress : " + String.join(",", newIpAddressList));
				return newIpAddressList.get(0);
			}
		} catch (SocketException e) {
			internalLog.error("getSysIpAddress : get IpAddress failed. " + e.getMessage());
			throw e;
		}

	}
}
