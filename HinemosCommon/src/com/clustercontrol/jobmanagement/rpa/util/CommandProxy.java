/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.rpa.util;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.CommandCreator;
import com.clustercontrol.util.CommandExecutor;
import com.clustercontrol.util.CommandExecutor.CommandResult;

/**
 * RPAツールエグゼキューターがRPAシナリオコマンドを実行する際に使用するクラス<br>
 * タスクトレイプログラムからログインコマンドを実行する際にも使用します。
 */
public class CommandProxy {
	/** ロガー */
	private static Log m_log = LogFactory.getLog(CommandProxy.class);
	/**
	 * コマンドのstdout/stderrの文字コード<br>
	 * Windows環境で使用するためMS932を指定します。 
	 */
	private static final String charset = "MS932";

	/**
	 * コマンドを実行します。
	 * 
	 * @param execCommand
	 *            実行コマンド
	 * @param destroy
	 *            コマンドの実行中断時にプロセスを終了するかどうかのフラグ
	 * @return 実行コマンド結果
	 * @throws HinemosUnknown
	 *             コマンド実行中に発生した例外
	 */
	public static CommandResult execute(String execCommand, boolean destroy) throws HinemosUnknown {
		try {
			String execUser = System.getProperty("user.name");
			String[] cmd = CommandCreator.createCommand(execUser, execCommand, CommandCreator.PlatformType.WINDOWS,
					true);
			if (m_log.isDebugEnabled()) {
				m_log.debug("execute() : cmd=" + Arrays.toString(cmd));
			}
			CommandExecutor cmdExec = new CommandExecutor(cmd, Charset.forName(charset), CommandExecutor._disableTimeout, destroy);
			cmdExec.execute();
			// コマンドの終了待ちを途中でキャンセルした場合nullを返す
			return cmdExec.getResult();
		} catch (HinemosUnknown e) {
			// コマンドの実行に失敗
			m_log.warn("execute() : command execution failed, " + e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * コマンドを実行します。<br>
	 * コマンドの実行中断時にプロセスを終了します。
	 * 
	 * @param execCommand
	 *            実行コマンド
	 * @return 実行コマンド結果
	 * @throws HinemosUnknown
	 *             コマンド実行中に発生した例外
	 */
	public static CommandResult execute(String execCommand) throws HinemosUnknown {
		return execute(execCommand, true);
	}
}
