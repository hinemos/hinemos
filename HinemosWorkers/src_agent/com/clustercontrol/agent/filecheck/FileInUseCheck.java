/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.filecheck;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileSystemException;
import java.nio.file.StandardOpenOption;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.CommandExecutor;
import com.clustercontrol.util.CommandExecutor.CommandResult;
import com.clustercontrol.util.EnvUtil;

/**
 * ファイルの使用中判定に使用するクラス
 * 
 */
public class FileInUseCheck {
	private static Log logger = LogFactory.getLog(FileInUseCheck.class);

	private static final boolean isWindows = EnvUtil.isWindows();

	/**
	 * 指定されたファイルが他プロセスによって使用中かどうか判定する。<BR>
	 * 
	 * @param file
	 * @return true:使用中、false:使用中ではない
	 * @throws IOException
	 * @throws HinemosUnknown
	 */
	public static boolean isInUse(File file) throws IOException, HinemosUnknown {
		if (isWindows) {
			return useCheckForWindows(file);
		} else {
			return useCheckForLinux(file);
		}
	}

	/**
	 * WindowsOS向けのファイル使用中判定
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static boolean useCheckForWindows(File file) throws IOException {
		try (FileChannel fc = FileChannel.open(file.toPath(), StandardOpenOption.WRITE)) {
			// 書き込みモードでチャンネルをオープン
			FileLock lock = fc.tryLock();
			if (lock == null) {
				// ロックが取得できない場合、使用中と判定する
				return true;
			}
			lock.release();
		} catch (FileSystemException e) {
			// 想定内例外
			// Java以外の他プロセスでロックしている場合、FileSystemExceptionが投げられる
			logger.debug("useCheckForWindows() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			return true;
		}
		return false;
	}

	// --- for useCheckForLinux()
	private static final String CMD_PREFIX;
	private static final String CMD_SUFFIX;

	private static long timeoutInterval = 10000L; // default 10sec

	static {
		// プロセスによるファイルの使用状況を確認するためのコマンド
		// -F:特定のフィールド指定用, a:access mode, f:file descriptor, l:lock status
		CMD_PREFIX = "lsof -Fafl ";
		logger.debug("CMD_PREFIX=" + CMD_PREFIX);

		// 結果を絞り込むためのコマンド
		// -x:完全一致のみ, -e:OR指定用
		// au:read/write access, aw:write access, fmem: memory-mapped-file
		// l.:（正規表現）ロックの種類を問わず、ロックがかけられている時点で使用中とする
		// -v:否定用オプション（lockがない場合空白で取得され、正規表現にマッチしてしまうため除外する）
		// -m1:表示件数を1件に絞る
		CMD_SUFFIX = " | grep -x -e 'au' -e 'aw' -e 'fmem' -e 'l.' | grep -v 'l ' -m1";
		logger.debug("CMD_SUFFIX=" + CMD_SUFFIX);

		try {
			// タイムアウトはファイルチェック間隔とする
			String intervalStr = AgentProperties.getProperty("job.filecheck.interval", Long.toString(timeoutInterval));
			timeoutInterval = Long.parseLong(intervalStr);
		} catch (Exception e) {
			logger.warn("FileInUseCheck : " + e.getMessage());
		}
	}

	/**
	 * Linux(UNIX)OS向けのファイル使用中判定
	 * 
	 * @param file
	 * @return
	 * @throws HinemosUnknown
	 */
	private static boolean useCheckForLinux(File file) throws HinemosUnknown {
		// プラットフォームやユーザの判定は不要なのでCommandCreatorは利用しない
		// 実行ユーザはエージェント起動ユーザ（rootが前提）とする
		String[] cmd = new String[] { "sh", "-c", CMD_PREFIX + file.getAbsolutePath() + CMD_SUFFIX };
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();
			for (String c : cmd) {
				if (sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(c);
			}
			// CommandExecutor側でも出力するがこのクラスで確認したい用
			logger.debug("useCheckForLinux() : execute command=" + sb.toString());
		}

		// 標準出力の有無で確認するため、バッファサイズおよび文字コードはデフォルトで問題ない
		CommandExecutor cmdExecutor = new CommandExecutor(cmd, timeoutInterval);
		cmdExecutor.execute();
		CommandResult ret = cmdExecutor.getResult();
		if (ret == null) {
			// タイムアウト等でnullが返ってきた場合はエラーとする
			String msg = "command result is null.";
			logger.warn("useCheckForLinux() : " + msg);
			throw new HinemosUnknown(msg);
		}
		if (!ret.stderr.isEmpty()) {
			// コマンドの標準エラー出力に出力がある場合はエラーとする
			// ※lsofの仕様として使用しているプロセスがなかった場合も終了コードが1になるため終了コードでは判定しない
			String msg = "exitCode=" + ret.exitCode + ", stdout=" + ret.stdout + ", stderr=" + ret.stderr;
			logger.warn("useCheckForLinux() : " + msg);
			throw new HinemosUnknown(msg);
		}
		// コマンドの標準出力が多量となった場合の上限問題を極力回避するために、ここでは標準出力の有無のみで判定する
		// そのためコマンド側でそのような結果となるようなコマンドにする必要がある
		if (ret.stdout.isEmpty()) {
			return false;
		}
		// 標準出力が空でない場合に使用中と判定する
		logger.debug("useCheckForLinux() : stdout=" + ret.stdout);
		return true;
	}
}
