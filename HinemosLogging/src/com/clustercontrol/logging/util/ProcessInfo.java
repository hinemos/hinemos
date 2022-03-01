/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logging.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.clustercontrol.log.internal.InternalLogManager;
import com.clustercontrol.logging.constant.MessageConstant;
import com.clustercontrol.logging.constant.PropertyConstant;
import com.clustercontrol.logging.property.LoggingProperty;

public class ProcessInfo {
	private static InternalLogManager.Logger internalLog = InternalLogManager.getLogger(ProcessInfo.class);
	private static final ProcessInfo INSTANCE = new ProcessInfo();
	private final Integer pid;
	private final boolean isWin;

	private ProcessInfo() {
		RuntimeMXBean bean = java.lang.management.ManagementFactory.getRuntimeMXBean();
		this.pid = Integer.valueOf(bean.getName().split("@")[0]);

		String osName = System.getProperty("os.name");
		this.isWin = (osName != null && (osName.startsWith("Windows") || osName.startsWith("windows")));
	}

	public static ProcessInfo getInstance() {
		return INSTANCE;
	}

	public HashMap<String, String> getProcInfo() throws TimeoutException, IOException, InterruptedException {
		LoggingProperty prop = LoggingProperty.getInstance();
		// 設定ファイルから取得
		int timeout = Integer.parseInt(prop.getProperty(PropertyConstant.PRC_GET_TIMEOUT));
		String cmdForCommandLine = prop.getProperty(PropertyConstant.PRC_GET_COMMAND_LINE).replace("%p",
				pid.toString());
		String cmdForCommandPath = prop.getProperty(PropertyConstant.PRC_GET_COMMAND_PATH).replace("%p",
				pid.toString());

		// 起動時のコマンドラインを取得
		String commandLine = execCommand(cmdForCommandLine, timeout);

		final String anyChr = ".*";
		// 最終的な値
		String prcCommand = "";
		String prcArgument = "";

		if (isWin && (cmdForCommandPath != null && !cmdForCommandPath.isEmpty())) {
			// Windowsの場合
			String rawCommand = "";
			String rawArguments = "";
			int splitIndex;
			if (commandLine.startsWith("\"") && (splitIndex = commandLine.indexOf("\"", 1)) > -1) {
				// ダブルクォーテーションで囲われている場合
				rawCommand = commandLine.substring(0, splitIndex + 1);
				rawArguments = commandLine.substring(splitIndex + 1);

			} else {
				rawCommand = commandLine.split(" ")[0];

				splitIndex = commandLine.indexOf(" ");
				if (splitIndex > -1) {
					rawArguments = commandLine.substring(splitIndex + 1);
				}
			}

			// 実行ファイルのパスを取得
			String commandPath = execCommand(cmdForCommandPath, timeout);

			// ディレクトリパスとファイル名に分割する
			String path = "";
			String name = "";
			splitIndex = commandPath.lastIndexOf("\\");
			if (splitIndex > -1) {
				path = commandPath.substring(0, splitIndex + 1);
				name = commandPath.substring(splitIndex + 1);
			} else {
				name = commandPath;
			}

			// SNMPの最大値で切る
			path = truncateAndAppend(path, 128, ""); // 末尾は追加しない
			name = truncateAndAppend(name, 64, "");

			if (rawCommand.contains("\\")) {
				// 起動時のコマンドで実行ファイルをパスで指定していた場合
				// SNMPのhrSWRunPathにパスが格納されるため結合する
				prcCommand = path + name;
			} else {
				prcCommand = name;
			}

			prcArgument = truncateAndAppend(rawArguments, 128, anyChr);

		} else {
			// Windows以外、または実行ファイルのパス取得用のコマンドが指定されていない場合
			String rawCommand = commandLine.split(" ")[0];

			String rawArguments = "";
			int wsIndex = commandLine.indexOf(" ");
			if (wsIndex > -1) {
				rawArguments = commandLine.substring(wsIndex + 1);
			}

			// SNMPの最大値で切る
			prcCommand = truncateAndAppend(rawCommand, 128, anyChr);
			prcArgument = truncateAndAppend(rawArguments, 128, anyChr);
		}

		HashMap<String, String> procMap = new HashMap<String, String>();
		procMap.put("PrcCommand", prcCommand);
		procMap.put("PrcArgument", prcArgument);
		return procMap;
	}

	/*
	 * 受け取ったコマンドを実行する
	 * 指定された時間でタイムアウトしTimeoutExceptionをthrowする
	 */
	private String execCommand(String command, int timeout) throws TimeoutException, IOException, InterruptedException {
		String result = null;
		Process p = Runtime.getRuntime().exec(command);
		try {
			if (p.waitFor(timeout, TimeUnit.SECONDS)) {
				try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
					String line;
					while ((line = input.readLine()) != null) {
						result = line;
					}
				} catch (IOException e1) {
					internalLog.error("ProcessInfo : " + e1.getMessage(), e1);
					throw e1;
				}
			} else {
				p.destroy();
				throw new TimeoutException(MessageConstant.getFaildCommandTimeout());
			}
		} finally {
			// ファイルディスクリプタの枯渇回避のため、明示的にクローズ
			p.getErrorStream().close();
			p.getInputStream().close();
			p.getOutputStream().close();
			p.destroy();
		}
		return result;
	}

	public Integer getPid() {
		return pid;
	}

	/*
	 * 引数の文字列が指定された文字数を超過している場合、
	 * 超過している分を切り捨て末尾に指定された文字列を結合する
	 */
	private String truncateAndAppend(String str, int length, String appendStr) {
		if (str.length() <= length) {
			return str;
		}
		return str.substring(0, length) + appendStr;
	}
}
