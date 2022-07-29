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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.rpa.bean.LoginParameter;
import com.clustercontrol.jobmanagement.rpa.bean.LoginTaskAbort;
import com.clustercontrol.jobmanagement.rpa.bean.LoginTaskEnd;
import com.clustercontrol.jobmanagement.rpa.bean.RoboAbortInfo;
import com.clustercontrol.jobmanagement.rpa.bean.RoboLogoutInfo;
import com.clustercontrol.jobmanagement.rpa.bean.RoboResultInfo;
import com.clustercontrol.jobmanagement.rpa.bean.RoboRunInfo;
import com.clustercontrol.jobmanagement.rpa.bean.RoboScreenshotInfo;
import com.clustercontrol.util.HinemosTime;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 連携に使用する指示ファイルを管理するクラス<br>
 */
public class RoboFileManager {

	/** ロガー */
	static private Log m_log = LogFactory.getLog(RoboFileManager.class);

	/** 連携用ファイルのファイル名を保持するマップ */
	private static final Map<Class<?>, String> fileNameMap = new ConcurrentHashMap<>();

	/** エージェント用指示ファイル出力先ディレクトリの定義 */
	private static final String AGENT_ROBO_FILE_DIR = RpaWindowsUtil.HINEMOS_AGENT_DATA_DIR_PATH;

	/** 指示ファイル出力先ディレクトリ */
	private final String roboFileDir;

	/**
	 * 実行結果ファイルの生成待ちを停止するためのフラグ<br>
	 * シナリオ監視スレッドから変更するためvolatileを指定しています。
	 */
	private volatile boolean waiting = true;
	
	/**
	 * InterruptedException の有無を示すフラグ<br>
	 * Future#cancel(true)等に由来するInterruptedException によって、
	 * 待機処理打ち切りがありえる一部メソッドについて、
	 * InterruptedExceptionが原因で終了したかどうかを区別するために導入しています。<br>
	 */
	private boolean isInterrupted = false;

	/** RPAシナリオ実行指示ファイル名 */
	private static final String RPA_RUN_FILE_NAME = "run.json";
	/** RPAシナリオ実行結果ファイル名 */
	private static final String RPA_RESULT_FILE_NAME = "result.json";
	/** RPAシナリオ実行中断ファイル名 */
	private static final String RPA_ABORT_FILE_NAME = "abort.json";
	/** スクリーンショット取得指示ファイル名 */
	private static final String SCREENSHOT_FILE_NAME = "screenshot.json";
	/**
	 * ログインコマンド実行指示ファイル名<br>
	 * Windows版マネージャのタスクトレイプログラムで使用します。
	 */
	private static final String LOGIN_FILE_NAME = "login.json";
	/**
	 * ログインコマンド終了通知ファイル名<br>
	 * Windows版マネージャのタスクトレイプログラムで使用します。
	 */
	private static final String LOGIN_END_FILE_NAME = "end.json";
	/**
	 * ログインコマンド中断指示ファイル名<br>
	 * Windows版マネージャのタスクトレイプログラムで使用します。
	 */
	private static final String LOGIN_ABORT_FILE_NAME = "abort.json";
	/**
	 * ログアウト実行指示ファイル名<br>
	 * ログアウトはRPAツールエグゼキューターから行います。
	 */
	private static final String LOGOUT_FILE_NAME = "logout.json";
	/** 書き込み途中のファイル名 */
	private static final String TEMP_FILE_NAME = "tmp-%s.json";

	static {
		fileNameMap.put(RoboRunInfo.class, RPA_RUN_FILE_NAME);
		fileNameMap.put(RoboResultInfo.class, RPA_RESULT_FILE_NAME);
		fileNameMap.put(RoboAbortInfo.class, RPA_ABORT_FILE_NAME);
		fileNameMap.put(RoboScreenshotInfo.class, SCREENSHOT_FILE_NAME);
		fileNameMap.put(LoginParameter.class, LOGIN_FILE_NAME);
		fileNameMap.put(LoginTaskEnd.class, LOGIN_END_FILE_NAME);
		fileNameMap.put(LoginTaskAbort.class, LOGIN_ABORT_FILE_NAME);
		fileNameMap.put(RoboLogoutInfo.class, LOGOUT_FILE_NAME);
	}


	/**
	 * コンストラクタ
	 * エージェント用（エージェント、シナリオエグゼキューター間）の指示ファイル向け
	 * @throws HinemosUnknown エラー発生時
	 * @throws InterruptedException インタラプトされた場合
	 */
	public RoboFileManager() throws HinemosUnknown, InterruptedException {
		roboFileDir = String.format(AGENT_ROBO_FILE_DIR, RpaWindowsUtil.getCommonApplicationData());
		m_log.debug("RoboFileManager() : roboFileDir=" + roboFileDir);

		File dir = new File(roboFileDir);
		// フォルダが存在しなければ新たに生成する
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				throw new HinemosUnknown("failed to create directory. roboFileDir=" + roboFileDir);
			}
		}

		// 一般ユーザでも変更可能なように権限設定
		RpaWindowsUtil.setEveryoneFullAccessPriv(dir);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param roboFileDir
	 *            ファイル出力先ディレクトリ
	 */
	public RoboFileManager(String roboFileDir) {
		this.roboFileDir = roboFileDir;
		File dir = new File(roboFileDir);
		// フォルダが存在しなければ新たに生成する
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				m_log.warn("RoboFileManager() : failed to create directory");
			}
		}
	}

	/**
	 * 実行指示ファイルを生成します。<br>
	 * 同じファイルが同時に出力されることは無いことが前提となっています。<br>
	 * 書き込み途中のファイルが読み取られることを防ぐため別名で書き込んだ後にリネームします。
	 * 
	 * @param <T>
	 *            ファイルに出力するクラス
	 * @param roboInfo
	 *            ファイルに出力するオブジェクト
	 * @throws IOException
	 *             ファイル出力が失敗した場合
	 */
	public <T> void write(T roboInfo) throws IOException {
		m_log.info("write() : start " + roboInfo);
		ObjectMapper mapper = new ObjectMapper();
		String roboFile = fileNameMap.get(roboInfo.getClass());
		File tempFile = new File(roboFileDir, String.format(TEMP_FILE_NAME, HinemosTime.currentTimeMillis()));
		try {
			mapper.writeValue(tempFile, roboInfo);
			Files.move(tempFile.toPath(), Paths.get(roboFileDir, roboFile));
		} catch (IOException e) {
			m_log.error("write() : " + e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * Everyoneのフルアクセスで、実行指示ファイルを生成します。<br>
	 * 同じファイルが同時に出力されることは無いことが前提となっています。<br>
	 * 書き込み途中のファイルが読み取られることを防ぐため別名で書き込んだ後にリネームします。
	 * アクセス権限変更コマンドに失敗した場合、警告を出し、続行。
	 *
	 * @param <T>
	 *            ファイルに出力するクラス
	 * @param roboInfo
	 *            ファイルに出力するオブジェクト
	 * @throws IOException ファイル書き込み、リネームに失敗した場合
	 */
	public <T> void writeWithEveryoneFullAccess(T roboInfo) throws IOException {
		m_log.info("writeWithEveryoneFullAccess() : start " + roboInfo);
		ObjectMapper mapper = new ObjectMapper();
		File roboFile = new File(roboFileDir, fileNameMap.get(roboInfo.getClass()));
		File tempFile = new File(roboFileDir, String.format(TEMP_FILE_NAME, HinemosTime.currentTimeMillis()));

		// ファイル書き込み、移動
		try {
			mapper.writeValue(tempFile, roboInfo);

			// Everyoneのフルアクセス設定
			if (!RpaWindowsUtil.setEveryoneFullAccessPriv(tempFile)) {
				// 失敗した場合、警告を出し、できるだけ続行
				m_log.warn("writeWithEveryoneFullAccess() : failed to set everyone full access privillege. tempFile=" + tempFile);
			}

			Files.move(tempFile.toPath(), roboFile.toPath());
			m_log.debug("writeWithEveryoneFullAccess() : move tempFile=" + tempFile + " to roboFile=" + roboFile);
		} catch (IOException e) {
			m_log.error("writeWithEveryoneFullAccess() : " + e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * 実行結果ファイルを読み取ります。<br>
	 * 実行結果ファイルの生成を一定間隔で確認します。
	 * 
	 * @param <T>
	 *            読み取るファイルのクラス
	 * @param clazz
	 *            読み取るファイルのクラス
	 * @param checkInterval
	 *            ファイル生成チェック間隔
	 * @return 読み取ったファイルのオブジェクト
	 * @throws IOException
	 *             ファイル読み込みでエラーが発生した場合
	 */
	public <T> T read(Class<T> clazz, int checkInterval) throws IOException {
		m_log.info("read() : start");
		this.isInterrupted = false;
		// シナリオ実行結果ファイルが生成されるのを待つ
		// タイムアウトさせたい場合は終了遅延で行う
		String roboFile = fileNameMap.get(clazz);
		File file = new File(roboFileDir, roboFile);
		while (waiting) {
			try {
				if (file.exists()) {
					m_log.debug("read() : target file detected.");
					break;
				}
				m_log.debug("read() : waiting for target file.");
				Thread.sleep(checkInterval);
			} catch (InterruptedException e) {
				// 停止コマンドにより途中で実行結果ファイル生成待ちがキャンセルされた場合
				m_log.info("read() : thread interrupted");
				this.isInterrupted = true;
				return null;
			}
		}
		// waitingがfalseの場合中断された
		if (!waiting) {
			m_log.info("read() : thread interrupted");
			return null;
		}

		T roboInfo = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			roboInfo = mapper.readValue(file, clazz);
		} catch (IOException e) {
			m_log.error("read() : " + e.getMessage(), e);
			throw e;
		}
		m_log.info("result=" + roboInfo);
		return roboInfo;
	}

	/**
	 * 実行指示ファイル、実行結果ファイルを削除します。
	 * 
	 * @param <T>
	 *            削除するファイルのクラス
	 * @param clazz
	 *            削除するファイルのクラス
	 * @throws IOException
	 *             ファイルの削除が失敗した場合
	 */
	public <T> void delete(Class<T> clazz) throws IOException {
		String roboFile = fileNameMap.get(clazz);
		m_log.info("delete() : start " + roboFile);
		try {
			Files.delete(Paths.get(roboFileDir, roboFile));
			m_log.debug("delete() : " + roboFile);
		} catch (IOException e) {
			m_log.error("delete() : " + e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * 連携用ファイルを削除します。
	 * 
	 * @throws IOException
	 *             ファイルの削除が失敗した場合
	 */
	public void clear() throws IOException {
		m_log.info("clear() : start");
		for (String roboFile : fileNameMap.values()) {
			try {
				if (Files.exists(Paths.get(roboFileDir, roboFile))) {
					Files.delete(Paths.get(roboFileDir, roboFile));
					m_log.debug("clear() : " + roboFile);
				}
			} catch (IOException e) {
				m_log.error("clear() : " + e.getMessage(), e);
				throw e;
			}
		}
	}

	/**
	 * 実行結果ファイル生成待ち処理を中止します。<br>
	 * 停止コマンドからジョブが停止された場合に実行されます。
	 */
	public void abort() {
		m_log.info("abort()");
		waiting = false;
	}

	/**
	 * 連携用ファイルのファイル名を返します。
	 * 
	 * @param clazz
	 *            取得するファイルに対応するクラス
	 * @return ファイル名
	 */
	public static <T> String getFileName(Class<T> clazz) {
		return fileNameMap.get(clazz);
	}

	/**
	 * 連携用ファイルが存在しないこと確認します。<br>
	 * (指示が伝わったかどうかの確認用です。)
	 * 
	 * @param <T>
	 *            存在を確認するクラス
	 * @param doInterrupt
	 *            中断指示（waitingフラグfalse）があった場合、従う。
	 *            ジョブ停止によるabor指示の場合、無視する必要があるためfalseにすること
	 * @param checkInterval
	 *            チェック間隔
	 * @param checkTimeout
	 *            チェック期限
	 */
	public <T> boolean confirmDelete(Class<T> clazz , boolean doInterrupt, int checkInterval , int checkTimeout ) {
		m_log.info("confirmDelete() : start " + clazz);
		this.isInterrupted = false;
		
		String roboFile = fileNameMap.get(clazz);
		File file = new File(roboFileDir, roboFile);
		long limitMills = HinemosTime.currentTimeMillis() + checkTimeout; 
		while (limitMills > HinemosTime.currentTimeMillis()) {
			if( doInterrupt && !waiting ){
				// waitingがfalseの場合中断された
				m_log.info("confirmDelete() : thread interrupted for waiting flag. "+ clazz);
				return false;
			}
			try {
				if (!file.exists()) {
					m_log.debug("confirmDelete() : no target file. " + clazz);
					return true;
				}
				m_log.debug("confirmDelete() : waiting for target file deletion." + clazz);
				Thread.sleep(checkInterval);
			} catch (InterruptedException e) {
				// 途中でファイル削除待ちがキャンセルされた場合
				m_log.info("confirmDelete() : thread interrupted for InterruptedException." + clazz);
				this.isInterrupted = true;
				return false;
			}
		}
		m_log.warn("confirmDelete() : timeout. " + clazz);
		return false;
	}

	/**
	 * 実行指示ファイル、実行結果ファイルを存在する場合は削除します。<br>
	 * 削除できない場合でもエラーは返しません。
	 * @param <T>
	 *            削除するファイルのクラス
	 * @param clazz
	 *            削除するファイルのクラス
	 */
	public <T> void deleteIfExist(Class<T> clazz)  {
		String roboFile = fileNameMap.get(clazz);
		m_log.info("deleteIfExist() : start " + roboFile);
		try {
			Files.delete(Paths.get(roboFileDir, roboFile));
			m_log.debug("deleteIfExist() : " + roboFile);
		} catch (IOException e) {
			m_log.debug("deleteIfExist() : " + e.getMessage(), e);
		}
	}

	/**
	 * 停止指示の有無を返します。
	 */
	public boolean isAborted() {
		return !waiting;
	}

	/**
	 * roboFileDirを取得します。
	 * @return roboFileDir
	 */
	public String getRoboFileDir() {
		return roboFileDir;
	}

	/**
	 * InterruptedException の有無を示すフラグを返します。
	 * confirmDeleteメソッド と readメソッド の実行直後のみ参照可。
	 */
	public boolean isInterrupted() {
		return this.isInterrupted;
	}

}
