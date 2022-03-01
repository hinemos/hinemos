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
 * RPAツールエグゼキューターとの連携に使用するファイルを管理するクラス<br>
 */
public class RoboFileManager {

	/** ロガー */
	static private Log m_log = LogFactory.getLog(RoboFileManager.class);
	/** 連携用ファイルのファイル名を保持するマップ */
	private static final Map<Class<?>, String> fileNameMap = new ConcurrentHashMap<>();
	/** RPAツールエグゼキューター連携用ファイル出力先ディレクトリ */
	private final String roboFileDir;
	/**
	 * 実行結果ファイルの生成待ちを停止するためのフラグ<br>
	 * シナリオ監視スレッドから変更するためvolatileを指定しています。
	 */
	private volatile boolean waiting = true;

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
}
