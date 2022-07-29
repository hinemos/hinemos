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
import com.clustercontrol.rpa.util.LoginResultEnum;
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

	/**
	 * xfreerdp コマンドの戻り値
	 * 正常に接続、ログイン後、処理が終わり切断した場合
	 * ERRINFO_LOGOFF_BY_USER (0x0000000C)
	 */
	final private static Integer ERRINFO_LOGOFF_BY_USER = 12;

	/**
	 * xfreerdp コマンドの戻り値
	 * 接続、ログイン後、別RDPで接続されたため、切断された場合
	 * ERRINFO_DISCONNECTED_BY_OTHER_CONNECTION (0x00000005)
	 */
	final private static Integer ERRINFO_DISCONNECTED_BY_OTHER_CONNECTION = 5;

	/**
	 * xfreerdp コマンドの戻り値
	 * 間違ったパスワードで接続した場合
	 * ERRCONNECT_LOGON_FAILURE [0x00020014]（下記の値とは異なる）
	 */
	final private static Integer ERRCONNECT_LOGON_FAILURE = 131;

	/**
	 * xfreerdp コマンドの戻り値
	 * 間違った接続先で接続した場合
	 * ERRCONNECT_SECURITY_NEGO_CONNECT_FAILED [0x0002000C]（下記の値とは異なる）
	 */
	final private static Integer ERRCONNECT_SECURITY_NEGO_CONNECT_FAILED = 133;


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
	 * ログインに成功した場合、ログアウトまで待機状態となる。
	 * 
	 * @return LoginResultEnumの値（SUCCESS: ログインし、正常にログアウト / その他: 失敗、エラー）
	 */
	public LoginResultEnum login() {
		m_log.info("login() : start");

		String loginCommand = HinemosPropertyDefault.job_rpa_login_command.getStringValue();
		String execUser = HinemosPropertyDefault.job_rpa_login_command_user.getStringValue();
		String homeDir = HinemosPropertyDefault.job_rpa_login_command_home_dir.getStringValue();
		String display = HinemosPropertyDefault.job_rpa_login_display.getStringValue();
		String execCommand = String.format(loginCommand, parameter.getIpAddress(), parameter.getUserId(),
				// パスワード文字列は'で囲む必要があるため、エスケープする（' → '\''）
				parameter.getPassword().replace("\'", "\'\\\'\'"),
				parameter.getWidth(), parameter.getHeight());
		m_log.debug("login() : loginCommand=" + loginCommand + ", execUser=" + execUser + ", homeDir=" + homeDir
				+ ", display=" + display + ", ipAddress="
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
			// ログインに成功した場合、ログアウトまで待機状態となる
			// そうでない場合は待たない（問題発生時はその時点で待機解除）
			CommandResult ret = cmdExec.getResult();
			if (ret == null) {
				// 中断した場合
				m_log.info("login() : login interrupted, commandResult is null");
				return LoginResultEnum.CANCELL;
			}
			m_log.debug("login() : exitCode=" + ret.exitCode + ", stdout=" + ret.stdout + ", stderr=" + ret.stderr + ", bufferDiscarded=" + ret.bufferDiscarded);

			// 正常にログイン、ログアウト
			if (ret.exitCode.equals(ERRINFO_LOGOFF_BY_USER)) {
				m_log.info("login() : logout successfully, exitCode=" + ret.exitCode);
				return LoginResultEnum.SUCCESS;
			}
			// ログイン後、別RDPで接続されたため、切断された場合、RPAシナリオ実行は正常に実行されるため、成功とする
			if (ret.exitCode.equals(ERRINFO_DISCONNECTED_BY_OTHER_CONNECTION)) {
				m_log.warn("login() : logout successfully, but Another user connected to the server, forcing the disconnection of the current connection. exitCode=" + ret.exitCode);
				return LoginResultEnum.CANCELL;
			}
			// ログインエラー
			if (ret.exitCode.equals(ERRCONNECT_LOGON_FAILURE)
					|| ret.exitCode.equals(ERRCONNECT_SECURITY_NEGO_CONNECT_FAILED)
					) {
				m_log.warn("login() : login error, exitCode=" + ret.exitCode);
				return LoginResultEnum.LOGIN_ERROR;
			}
			// その他
			m_log.warn("login() : command exited unexpectedly, exitCode=" + ret.exitCode);
		} catch (HinemosUnknown e) {
			m_log.warn("login() : exception=" + e.getMessage(), e);
		}

		return LoginResultEnum.UNKNOWN;
	}

}
