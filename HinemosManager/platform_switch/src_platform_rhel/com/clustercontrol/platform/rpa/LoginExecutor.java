/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.platform.rpa;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.rpa.bean.LoginParameter;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.util.CommandCreator;
import com.clustercontrol.util.CommandExecutor;
import com.clustercontrol.util.CommandExecutor.CommandResult;

/**
 * マネージャ上でコマンドを実行してログインを行うクラス<br>
 * Linux版マネージャで使用します。
 */
public class LoginExecutor {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(LoginExecutor.class);
	/** ログインに使用するパラメータ */
	private LoginParameter parameter;

	/**
	 * コンストラクタ
	 * 
	 * @param parameter
	 *            ログインパラメータ
	 */
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
		String loginCommand = HinemosPropertyDefault.job_rpa_login_command.getStringValue();
		String execUser = HinemosPropertyDefault.job_rpa_login_command_user.getStringValue();
		String homeDir = HinemosPropertyDefault.job_rpa_login_command_home_dir.getStringValue();
		String display = HinemosPropertyDefault.job_rpa_login_display.getStringValue();
		Integer exitCodeLogout = HinemosPropertyDefault.job_rpa_login_command_exit_code_logout.getIntegerValue();
		String execCommand = String.format(loginCommand, parameter.getIpAddress(), parameter.getUserId(),
				parameter.getPassword(), parameter.getWidth(), parameter.getHeight());
		m_log.debug("login() : loginCommand=" + loginCommand + ", execUser=" + execUser + ", homeDir=" + homeDir
				+ ", display=" + display + ", exitCodeLogout=" + exitCodeLogout + ", ipAddress="
				+ parameter.getIpAddress() + ", userId=" + parameter.getUserId() + ", width=" + parameter.getWidth()
				+ ", height=" + parameter.getHeight());
		try {
			// 実行ユーザを指定してコマンドを生成
			// Linux版マネージャでのみ使用するためPlatformType.UNIXを指定
			String[] cmd = CommandCreator.createCommand(execUser, execCommand, CommandCreator.PlatformType.UNIX, true);
			if (m_log.isDebugEnabled()) {
				m_log.debug("login() : cmd=" + Arrays.toString(cmd).replace(parameter.getPassword(), "xxx"));
			}
			CommandExecutor cmdExec = new CommandExecutor(cmd, CommandExecutor._disableTimeout);
			cmdExec.addEnvironment("DISPLAY", display); // RDPの画面を出力するディスプレイ番号を指定
			cmdExec.addEnvironment("HOME", homeDir); // 環境変数HOMEを設定しないとコマンドが動作しない
			cmdExec.execute();
			// ログインに成功した場合、ログアウトされるまでここで待ち続ける
			// ログインに失敗した場合はすぐに開始する
			CommandResult ret = cmdExec.getResult();
			if (ret != null && ret.exitCode != null) {
				m_log.debug("login() : stdout=" + ret.stdout + ", stderr=" + ret.stderr);
				if (ret.exitCode.equals(exitCodeLogout)) {
					// 正常にログアウトされた場合
					m_log.info("login() : logout successfully, exitCode=" + ret.exitCode);
					return true;
				} else {
					m_log.info("login() : command exited unexpectedly, exitCode=" + ret.exitCode);
					// 認証エラー等で終了した場合
					return false;
				}
			}
			m_log.warn("login() : commandResult is null");
		} catch (HinemosUnknown e) {
			m_log.warn("login() exception : " + e.getMessage(), e);
		}

		return false;
	}

}
