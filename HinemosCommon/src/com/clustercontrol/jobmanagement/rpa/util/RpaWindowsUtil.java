/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.rpa.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.CommandExecutor.CommandResult;
import com.clustercontrol.version.util.VersionUtil;

/**
 * RPA Windows用のユーティリティ
 */
public class RpaWindowsUtil {

	/** ロガー */
	static private Log m_log = LogFactory.getLog(RpaWindowsUtil.class);

    /**
     * TODO ※バージョンアップ時に必ず更新すること！
     *
     * Hinemosエージェントのバージョンを定義する定数。<BR>
     * {メジャーバージョン}.{マイナーバージョン}の形式で定義してください。
     *
     * @see com.clustercontrol.agent.AgentVersion.VERSION
     */
    public static final String VERSION = VersionUtil.getVersionMajor();	

	/** エージェントデータディレクトリ */
    public static final String HINEMOS_AGENT_DATA_DIR_PATH = "%s\\Hinemos\\Agent" + VERSION + "\\rpa";

	/** ユーザ名取得用コマンド */
	private static final String USERNAME_COMMAND = "cmd /c \"echo %USERNAME%\"";

	/**
	 * ログインセッション確認用コマンド
	 * 該当ユーザのセッションがアクティブかを確認する。
	 */
	private static final String CHECK_SESSION_COMMAND = "cmd /c \"quser %USERNAME% | findstr Active\"";

	/**
	 * ログインセッションユーザ確認用コマンド
	 * quserだとユーザ名が全て小文字になるので、qwinstaを使う。
	 */
	private static final String CHECK_SESSION_USER_COMMAND = "cmd /c \"qwinsta | findstr Active\"";

	/** RPAシナリオエグゼキュータープロセス確認用コマンド */
	private static final String GET_PROCESS_BY_ID_COMMAND = "powershell.exe -Command Get-Process -Id %s";

	/** RPAツールプロセス確認用コマンド */
	private static final String GET_PROCESS_BY_NAME_COMMAND = "powershell.exe -Command Get-Process -Name %s";

	/** CommonApplicationData 取得用コマンド */
	private static final String COMMON_APPLICATION_DATA_COMMAND = "powershell.exe -Command \"[Environment]::GetFolderPath(\\\"CommonApplicationData\\\")\"";

	/** ApplicationData、%APPDATA% 取得用コマンド */
	private static final String APPLICATION_DATA_COMMAND = "powershell.exe -Command \"[Environment]::GetFolderPath(\\\"ApplicationData\\\")\"";

	/**
	 * Everyoneフルアクセスの権限に変更するコマンド
	 * powershell.exe 経由だと権限設定できない場合があるため、直接 icacls.exe を呼び出している。
	 */
	private static final String EVERYONE_FULL_ACCESS_COMMAND = "icacls.exe \"%s\" /grant Everyone:F";

	/**
	 * PIDファイルディレクトリ
	 * %sには CommonApplicationData のディレクトリが入る。
	 */
	private static final String PID_FILE_PATH = HINEMOS_AGENT_DATA_DIR_PATH + "\\run\\";

	/**
	 * PIDファイル名
	 * %sには環境変数のユーザ名が入る。
	 */
	private static final String PID_FILE_NAME = "rpa_scenario_executor_%s.pid";

	/**
	 * スクリーンショットテンポラリ出力先
	 * %sには CommonApplicationData のディレクトリが入る。
	 */
	private static final String SCREENSHOT_FILE_PATH = HINEMOS_AGENT_DATA_DIR_PATH + "\\screenshot\\";

	/** スクリーンショットファイル名プレフィックス */
	private static final String SCREENSHOT_FILE_NAME_PREFIX = "screenshot";

	/**
	 * スクリーンショットファイル名
	 * %sには Hinemos時刻 が入る。
	 */
	private static final String SCREENSHOT_FILE_NAME = SCREENSHOT_FILE_NAME_PREFIX + "-%s.png";

	/** 改行コードの正規表現 */
	private static final String REGEX_RETURN_CODE = "\\r\\n|\\r|\\n";

	/**
	 * すべてのユーザーが使用するアプリケーションの共通ディレクトリ CommonApplicationData のキャッシュ。
	 * ユーザに係わらない値なので、保持しておく。
	 */
	private static String commonApplicationData;


	/**
	 * ユーザ名 %USERNAME% を取得する。
	 *
	 * @return %USERNAME%、エラーの場合null
	 */
	public static String getUseername() {
		CommandResult result = null;
		try {
			result = CommandProxy.execute(USERNAME_COMMAND);
		} catch (HinemosUnknown e) {
			m_log.error("getUseername() : CommonApplicationData command execution failed, " + e.getMessage(), e);
			return null;
		}
		m_log.debug("getUseername() : result=" + result);
		if (result == null) {
			m_log.debug("getUseername() : CommonApplicationData result is null");
			return null;
		}
		if (result.stdout == null) {
			m_log.debug("getUseername() : CommonApplicationData stdout is null");
			return null;
		}

		// 改行を取り除く
		String username = result.stdout.replaceAll(REGEX_RETURN_CODE, "");
		m_log.debug("getUseername() : username=" + username);

		return username;
	}

	/**
	 * 該当ユーザのセッションがアクティブかを確認する。
	 *
	 * @return ログインセッション有無
	 * @throws HinemosUnknown コマンド実行に失敗した場合
	 * @throws InterruptedException コマンドがインタラプトされた場合
	 */
	public static boolean isUserSessionActive() throws HinemosUnknown, InterruptedException {

		CommandResult result = CommandProxy.execute(CHECK_SESSION_COMMAND);
		m_log.debug("isUserSessionActive() : result=" + result);
		if (result == null) {
			throw new InterruptedException("isUserSessionActive() : command interrupted. result=null");
		}

		boolean ret = result.exitCode == 0;
		m_log.debug("isUserSessionActive() : user session is active: " + ret);
		return ret;
	}

	/**
	 * ログインセッションがあるユーザ名のリストを取得する。
	 * 
	 * @return ユーザ名のリスト
	 * @throws HinemosUnknown コマンド実行にinterruptされた場合
	 * @throws InterruptedException コマンドがインタラプトされた場合
	 */
	public static List<String> getActiveUsers() throws HinemosUnknown, InterruptedException {
		CommandResult result = CommandProxy.execute(CHECK_SESSION_USER_COMMAND);
		m_log.debug("getActiveUsers() : result=" + result);
		if (result == null) {
			throw new InterruptedException("getActiveUsers() : command interrupted. result=null");
		}

		List<String> users = new ArrayList<String>();
		if (result.exitCode != 0) {
			// コマンド結果が0件の場合
			return users;
		}

		// ユーザ名を取得
		String[] lines = result.stdout.trim().split(REGEX_RETURN_CODE);
		for (String line : lines) {
			m_log.debug("getActiveUsers() : line=" + line);
			if (line.indexOf("Active") >= 0) {
				m_log.debug("getActiveUsers() : found 'Active'. line=" + line);
				String s[] = line.trim().split("\\s+");
				users.add(s[1]);
			}
		}
		m_log.debug("getActiveUsers() : users=" + Arrays.toString(users.toArray()));

		return users;
	}

	/**
	 * 該当ユーザのログインセッションがあることを確認する。
	 *
	 * @param userName ユーザ名
	 * @return 有無
	 * @throws HinemosUnknown コマンド実行に失敗した場合
	 * @throws InterruptedException コマンドがインタラプトされた場合
	 */
	public static boolean hasActiveSession(String userName) throws HinemosUnknown, InterruptedException {
		List<String> users = getActiveUsers();
		m_log.debug("hasActiveSession() : users=" + users);
		if (users == null || users.size() <= 0) {
			return false;
		}
		for (String activeUserName : users) {
			if (activeUserName.equals(userName)) {
				m_log.debug("hasActiveSession() : found. userName=" + userName);
				return true;
			}
		}

		m_log.debug("hasActiveSession() : not found userName=" + userName + " in users=" + users);
		return false;
	}

	/**
	 * 該当プロセスIDが存在するかを確認する。
	 * 
	 * @param pid プロセスID
	 * @return 存在有無、コマンド実行中にinterruptされた場合null
	 * @throws HinemosUnknown コマンド実行に失敗した場合
	 * @throws InterruptedException コマンドがインタラプトされた場合
	 */
	public static boolean existsProcessId(String pid) throws HinemosUnknown, InterruptedException {
		String cmd = String.format(GET_PROCESS_BY_ID_COMMAND, pid);
		m_log.debug("existsProcessId() : cmd=" + cmd);
		CommandResult result = CommandProxy.execute(cmd);
		m_log.debug("existsProcessId() : result=" + result);
		if (result == null) {
			throw new InterruptedException("existsProcessId() : command interrupted. result=null");
		}

		boolean ret = result.exitCode == 0;
		m_log.debug("existsProcessId() : process existence is " + ret);
		return ret;
	}

	/**
	 * 該当プロセス名が起動しているかを確認する。
	 * 
	 * @param processName プロセス名
	 * @return 存在有無、コマンド実行中にinterruptされた場合null
	 * @throws HinemosUnknown コマンド実行に失敗した場合
	 * @throws InterruptedException コマンドがインタラプトされた場合
	 */
	public static boolean existsProcessName(String processName) throws HinemosUnknown, InterruptedException {
		String cmd = String.format(GET_PROCESS_BY_NAME_COMMAND, processName);
		m_log.debug("existsProcessName() : cmd=" + cmd);
		CommandResult result = CommandProxy.execute(cmd);
		m_log.debug("existsProcessName() : result=" + result);
		if (result == null) {
			throw new InterruptedException("existsProcessName() : command interrupted. result=null");
		}

		boolean ret = result.exitCode == 0;
		m_log.debug("existsProcessName() : process existence is " + ret);
		return ret;
	}

	/**
	 * ユーザに係わらず、RPAエグゼキュータープロセスが1つでも起動しているかどうか確認する。
	 * PIDファイルを起点に確認します。
	 * 異常な状態（PIDファイルファイルがなく、RPAエグゼキュータープロセスが起動している場合）は考慮していない。
	 * 
	 * @return 1つでも起動している場合true、全く起動していない場合false
	 * @throws IOException PIDファイル読込み失敗
	 * @throws HinemosUnknown コマンド実行に失敗した場合
	 * @throws InterruptedException コマンドがインタラプトされた場合
	 */
	public static boolean isRpaExecutorRunnning() throws IOException, HinemosUnknown, InterruptedException {
		// PIDファイルの一覧を取得
		String dirPath = RpaWindowsUtil.getPidDirPath();
		if (dirPath == null || dirPath.isEmpty()) {
			throw new HinemosUnknown("isRpaExecutorRunnning() : PidFilePath is null or empty.");
		}
		// ファイルリストでループし、起動確認
		String[] files = new File(dirPath).list();
		if (files == null || files.length <= 0) {
			m_log.debug("isRpaExecutorRunnning() : pid file directory is null or empty.");
			return false;
		}
		for (String file : files) {
			File pidFile = new File(dirPath, file);
			m_log.debug("isRpaExecutorRunnning() : pid file=" + pidFile);
			String pid = Files.readAllLines(pidFile.toPath(), StandardCharsets.UTF_8).get(0);
			m_log.debug("isRpaExecutorRunnning() : pid=" + pid);
			// RPAツールエグゼキューターが起動中か
			if (RpaWindowsUtil.existsProcessId(pid)) {
				m_log.debug("isRpaExecutorRunnning() : RpaExecutor is runnning, pid=" + pid);
				return true;
			} else {
				// PIDファイルファイルがあるが、RPAエグゼキュータープロセスが起動していない場合は、異常
				// （PIDファイルファイルはゴミファイル）
				m_log.warn("isRpaExecutorRunnning() : dust file, pid file=" + pidFile);
			}
		}

		m_log.debug("isRpaExecutorRunnning() : RpaExecutor is NOT runnning.");
		return false;
	}

	/**
	 * 該当ユーザの、RPAエグゼキュータープロセスが起動しているかどうか確認します。
	 * 
	 * @return 起動有無
	 * @throws IOException PIDファイル読込み失敗
	 * @throws HinemosUnknown コマンド実行に失敗した場合
	 * @throws InterruptedException コマンドがインタラプトされた場合
	 */
	public static boolean isRpaExecutorRunnning(String username) throws IOException, HinemosUnknown, InterruptedException {
		// 該当ユーザのPIDファイルを取得
		File pidFile = RpaWindowsUtil.getPidFile(username);
		m_log.debug("isRpaExecutorRunnning(username=" + username + ") : pidFile=" + pidFile);
		if (!pidFile.exists()) {
			m_log.debug("isRpaExecutorRunnning(username=" + username + ") : pid file not exists. pidFile=" + pidFile);
			return false;
		}
		// ファイルからPID取得
		String pid = Files.readAllLines(pidFile.toPath(), StandardCharsets.UTF_8).get(0);
		// RPAツールエグゼキューターが起動中か
		if (RpaWindowsUtil.existsProcessId(pid)) {
			return true;
		}

		// PIDファイルがあるのにプロセスは起動していない（エグゼキューター起動時にサインアウトした場合）
		m_log.warn("isRpaExecutorRunnning(username=" + username + ") : pid file exists, but no process. pidFile=" + pidFile);
		return false;
	}

	/**
	 * 該当ユーザの、RPAエグゼキュータープロセスが終了しているかどうか確認します。
	 * 
	 * @return true:終了している false:起動中
	 * @throws IOException PIDファイル読込み失敗
	 * @throws HinemosUnknown コマンド実行に失敗した場合
	 * @throws InterruptedException コマンドがインタラプトされた場合
	 */
	public static boolean isRpaExecutorTerminate(String username) throws IOException, HinemosUnknown, InterruptedException {
		// 該当ユーザのPIDファイルを取得
		File pidFile = RpaWindowsUtil.getPidFile(username);
		m_log.debug("isRpaExecutorTerminate(username=" + username + ") : pidFile=" + pidFile);
		if (!pidFile.exists()) {
			// PIDファイルない場合は終了している（PIDファイルがないのに起動している場合は想定外）
			m_log.debug("isRpaExecutorTerminate(username=" + username + ") : result=true, pid file not exists. pidFile=" + pidFile);
			return true;
		}

		// ファイルからPID取得
		String pid = Files.readAllLines(pidFile.toPath(), StandardCharsets.UTF_8).get(0);
		// 該当プロセスがなければtrue
		// （通常はPIDファイルがあれば起動している、エグゼキューター起動時にサインアウトした場合はファイルありプロセスなし）
		boolean result = !RpaWindowsUtil.existsProcessId(pid);
		m_log.debug("isRpaExecutorTerminate(username=" + username + ") : result=" + result);

		return result;
	}

	/**
	 * PIDファイルを取得する。
	 * 
	 * @param username ユーザ名
	 * @return ファイル
	 * @throws HinemosUnknown コマンド実行に失敗した場合、PIDファイルのディレクトリがnullまたは空の場合
	 * @throws InterruptedException コマンドがインタラプトされた場合
	 */
	public static File getPidFile(String username) throws HinemosUnknown, InterruptedException {
		String dirPath = RpaWindowsUtil.getPidDirPath();
		if (dirPath == null || dirPath.isEmpty()) {
			throw new HinemosUnknown("getPidFile() : PidFilePath is null or empty.");
		}

		File pidFile = new File(dirPath, String.format(PID_FILE_NAME, username));
		m_log.debug("getPidFile() : pidFile=" + pidFile);

		return pidFile;
	}

	/**
	 * PIDファイルのパスを取得する。
	 *
	 * @return PIDファイルのパス
	 * @throws HinemosUnknown コマンド実行に失敗した場合、値がnullまたは空の場合
	 * @throws InterruptedException コマンドがインタラプトされた場合
	 */
	public static String getPidDirPath() throws HinemosUnknown, InterruptedException {
		// CommonApplicationDataディレクトリを取得
		String dir = RpaWindowsUtil.getCommonApplicationData();
		if (dir == null || dir.isEmpty()) {
			throw new HinemosUnknown("getPidDirPath() : null or empty.");
		}
		String pidDir = String.format(PID_FILE_PATH, dir);

		m_log.debug("getPidDirPath() : pid dir=" + pidDir);
		return pidDir;
	}

	/**
	 * スクリーンショットテンポラリ出力先のパスを取得する。
	 *
	 * @return スクリーンショットテンポラリ出力先のパス
	 * @throws HinemosUnknown コマンド実行に失敗した場合、値がnullまたは空の場合
	 * @throws InterruptedException コマンドがインタラプトされた場合
	 */
	public static String getScreenshotDirPath() throws HinemosUnknown, InterruptedException {
		// CommonApplicationDataディレクトリを取得
		String dir = RpaWindowsUtil.getCommonApplicationData();
		if (dir == null || dir.isEmpty()) {
			throw new HinemosUnknown("getScreenshotDirPath() : null or empty.");
		}
		String screenshotDir = String.format(SCREENSHOT_FILE_PATH, dir);

		m_log.debug("getScreenshotDirPath() : screenshot dir=" + screenshotDir);
		return screenshotDir;
	}

	/**
	 * CommonApplicationData を取得する。
	 * 例：C:\ProgramData
	 * ユーザに係わらない値なので、キャッシュに保持している。
	 *
	 * @return CommonApplicationData パス
	 * @throws HinemosUnknown コマンド実行に失敗した場合
	 * @throws InterruptedException コマンドがインタラプトされた場合
	 */
	public static String getCommonApplicationData() throws HinemosUnknown, InterruptedException {
		if (commonApplicationData != null && !commonApplicationData.isEmpty()) {
			// キャッシュから返す
			m_log.debug("getCommonApplicationData() : return cache value. commonApplicationData=" + commonApplicationData);
			return commonApplicationData;
		}

		CommandResult result = CommandProxy.execute(COMMON_APPLICATION_DATA_COMMAND);
		m_log.debug("getCommonApplicationData() : result=" + result);
		if (result == null) {
			throw new InterruptedException("getCommonApplicationData() : command interrupted. result=null");
		}
		if (result.exitCode != 0 || result.stdout == null) {
			throw new HinemosUnknown("getCommonApplicationData() : exitCode is not 0, or stdout is empty, result=" + result);
		}

		// 改行を取り除く
		String directory = result.stdout.replaceAll(REGEX_RETURN_CODE, "");
		m_log.debug("getCommonApplicationData() : directory=" + directory);

		// キャッシュに保持しておく
		commonApplicationData = directory;
		return commonApplicationData;
	}
	
	/**
	 * ユーザーのアプリケーション固有のデータの共通ディレクトリ %APPDATA% を取得する。
	 * 例：C:\Users\Administrator\AppData\Roaming
	 *
	 * @return %APPDATA%パス
	 * @throws HinemosUnknown コマンド実行に失敗した場合
	 * @throws InterruptedException コマンドがインタラプトされた場合
	 */
	public static String getApplicationData() throws HinemosUnknown, InterruptedException {
		CommandResult result = CommandProxy.execute(APPLICATION_DATA_COMMAND);
		m_log.debug("getApplicationData() : result=" + result);
		if (result == null) {
			throw new InterruptedException("getApplicationData() : command interrupted. result=null");
		}
		if (result.exitCode != 0 ||  result.stdout == null) {
			throw new HinemosUnknown("getApplicationData() : exitCode is not 0, or stdout is empty, result=" + result);
		}

		// 改行を取り除く
		String directory = result.stdout.replaceAll(REGEX_RETURN_CODE, "");
		m_log.debug("getApplicationData() : directory=" + directory);

		return directory;
	}

	/**
	 * Everyoneにフルアクセス権限を設定する。
	 * 
	 * @param file ファイル
	 * @return 成否
	 */
	public static boolean setEveryoneFullAccessPriv(File file) {
		String cmd = String.format(EVERYONE_FULL_ACCESS_COMMAND, file.getAbsolutePath());
		m_log.debug("setEveryoneFullAccessPriv() : cmd=" + cmd);
		CommandResult result = null;
		try {
			result = CommandProxy.execute(cmd);
		} catch (HinemosUnknown e) {
			m_log.warn("setEveryoneFullAccessPriv() : failed to set everyone full access privillege. e=" + e);
			return false;
		}
		m_log.debug("setEveryoneFullAccessPriv() : result=" + result);
		if (result == null || result.exitCode != 0) {
			m_log.warn("setEveryoneFullAccessPriv() : failed to set everyone full access privillege. result=" + result);
			return false;
		}

		return true;
	}

	/**
	 * スクリーンショットファイル名プレフィックスを返す。
	 *
	 * @return ファイル名プレフィックス
	 */
	public static String getScreenshotFileNamePrefix() {
		return SCREENSHOT_FILE_NAME_PREFIX;
	}

	/**
	 * スクリーンショットファイル名を返す。
	 * %sには Hinemos時刻 を設定する。
	 *
	 * @return ファイル名
	 */
	public static String getScreenshotFileName() {
		return SCREENSHOT_FILE_NAME;
	}

}
