/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.platform.rpa;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.jobmanagement.rpa.bean.LoginParameter;
import com.clustercontrol.jobmanagement.rpa.bean.LoginTaskAbort;
import com.clustercontrol.jobmanagement.rpa.bean.LoginTaskEnd;
import com.clustercontrol.jobmanagement.rpa.util.RoboFileManager;
import com.clustercontrol.platform.HinemosPropertyDefault;

/**
 * タスクトレイプログラムでコマンドを実行してログインを行うクラス<br>
 * Windows版マネージャで使用します。
 */
public class LoginExecutor {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(LoginExecutor.class);
	/** ログインに使用するパラメータ */
	private LoginParameter parameter;
	/** ログイン指示/終了ファイル出力先ディレクトリ */
	private static final String loginFileDir = HinemosPropertyDefault.job_rpa_login_file_dir.getStringValue();
	/** ログイン終了ファイルチェック間隔 */
	private static final int checkInterval = HinemosPropertyDefault.job_rpa_login_end_file_check_interval
			.getIntegerValue();

	public LoginExecutor(LoginParameter parameter) {
		this.parameter = parameter;
	}

	/**
	 * ログインを実行します。
	 * 
	 * @return true: ログインし、正常にログアウト / false: エラーが発生
	 */
	public boolean login() {
		m_log.info("login() : start");
		m_log.debug("login() : loginFileDir=" + loginFileDir + ", checkInterval=" + checkInterval);
		// IPアドレスごとにディレクトリを作成する
		File dir = new File(loginFileDir, parameter.getIpAddress());
		dir.mkdirs(); // rpaディレクトリが存在しなければ合わせて作成する
		RoboFileManager roboFileManager = new RoboFileManager(dir.toString());
		try {
			// 既にファイルが存在する場合は削除する
			roboFileManager.clear();
			// ログイン指示ファイルを生成
			roboFileManager.write(parameter);
			// ログイン処理終了ファイルが生成されるのを待つ
			LoginTaskEnd endInfo = roboFileManager.read(LoginTaskEnd.class, checkInterval);
			// 終了ファイルを削除しtrueを返す
			// ジョブの停止等で終了ファイル生成待ちを中断した場合はnullになる
			if (endInfo != null) {
				m_log.info("login() : login end");
				roboFileManager.delete(LoginTaskEnd.class);
			} else {
				m_log.info("login() : login wait cancelled");
				// ログイン中断指示ファイルを生成
				roboFileManager.write(new LoginTaskAbort(parameter.getIpAddress()));
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
