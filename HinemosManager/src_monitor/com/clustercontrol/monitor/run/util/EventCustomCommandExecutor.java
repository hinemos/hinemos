/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.util;

import java.nio.charset.Charset;


import java.text.SimpleDateFormat;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.monitor.bean.EventCustomCommandInfo;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.util.CommandCreator;
import com.clustercontrol.util.CommandExecutor;
import com.clustercontrol.util.CommandExecutor.CommandResult;
import com.clustercontrol.util.StringBinder;

/***
 * イベントカスタムコマンド　コマンド実行クラス
 *
 */
public class EventCustomCommandExecutor {
	
	private static Log m_log = LogFactory.getLog(EventCustomCommandExecutor.class);
	
	private EventCustomCommandInfo commandInfo;
	private EventLogEntity eventInfo;
	
	//実行結果
	
	public EventCustomCommandExecutor(EventCustomCommandInfo commandInfo, EventLogEntity eventInfo) {
		this.commandInfo = commandInfo;
		this.eventInfo = eventInfo;
	}
	
	public ExecutorResult executeCommand() {
		ExecutorResult res = new ExecutorResult();
		
		if ("".equals(commandInfo.getCommand())) {
			String detailMsg = "Event Custom Command : command is empty.";
			m_log.info(detailMsg);
			res.setStdout("");
			res.setStderr(detailMsg);
			res.setError(true);
			return res;
		}
		
		//置換文字列変換後のコマンド文字列を取得
		String command = convCommandString(commandInfo.getCommand(), eventInfo, commandInfo.getDateFormat());
		
		/**
		 * 実行
		 */
		// 起動ユーザ名取得
		String sysUserName = System.getProperty("user.name");
		String effectiveUser = commandInfo.getUser();

		
		// Hinemos Managerの起動ユーザがroot以外の場合で、
		// 起動ユーザとコマンド実行ユーザが異なる場合は、コマンド実行しない
		// ※Window版への対応
		if ((!effectiveUser.isEmpty()) && (!sysUserName.equals("root")) && (!sysUserName.equals(effectiveUser))) {
			// 起動失敗
			String detailMsg = "The execution user of the command and hinemos manager's user are different.";
			m_log.info(detailMsg);
			res.setStdout("");
			res.setStderr(detailMsg);
			res.setError(true);
			return res;
		}
		
		m_log.debug("Event Custom Command Submit : " + eventInfo.toString() + " command=" + command);
		
		// コマンドを実行
		//　他のコマンド系の処理はコマンドの実行時にExecuterで実行制御をしているが、
		//　本処理は本処理を呼び出す前に実行制御しているため、同期処理で実行する
		CommandResult ret = null;
		try {
			
			
			ret = new CommandCaller(
					effectiveUser,
					command,
					commandInfo.getEncode(),
					commandInfo.getTimeout(),
					commandInfo.getMode(),
					commandInfo.getBuffer().intValue(),
					commandInfo.getLogin()
					).call();
		} catch (Exception e) {
			
			m_log.info(e);
			res.setError(true);
			res.setStdout("");
			res.setStderr("Internal Error : " + e.getMessage());
			
			return res;
		}
		
		if (ret == null || ret.exitCode == null) {
			//タイムアウトの場合
			res.setError(true);
			res.setTimeout(true);
			//標準出力／エラー出力はセットされない
		} else if (ret.exitCode == -1){
			//エラーの場合
			res.setError(true);
			res.setStdout(ret.stdout);
			res.setStderr(ret.stderr);
		} else {
			//正常終了の場合
			res.setReturnCode(ret.exitCode);
			res.setStdout(ret.stdout);
			res.setStderr(ret.stderr);
			
		}
		
		return res;
	}
	
	private static String convCommandString(String command, EventLogEntity eventInfo, String dateFormat) {
		SimpleDateFormat sdf = null;
		try {
			sdf = new SimpleDateFormat(dateFormat);
		} catch (IllegalArgumentException | NullPointerException e) {
			m_log.info("illegal dateformat:" + dateFormat);
			sdf = new SimpleDateFormat();
		}
		
		Map<String, String> param = EventUtil.createParameter(eventInfo, sdf); 
				
		StringBinder binder = new StringBinder(param);
		return binder.replace(command);
	}
	
	public static class ExecutorResult {
		private boolean timeout = false;
		private boolean error = false;
		private Integer returnCode;
		private String stdout;
		private String stderr;
		
		public boolean getTimeout() {
			return timeout;
		}
		public void setTimeout(boolean timeout) {
			this.timeout = timeout;
		}
		public boolean getError() {
			return error;
		}
		public void setError(boolean error) {
			this.error = error;
		}
		public Integer getReturnCode() {
			return returnCode;
		}
		public void setReturnCode(Integer returnCode) {
			this.returnCode = returnCode;
		}
		public String getStdout() {
			return stdout;
		}
		public void setStdout(String stdout) {
			this.stdout = stdout;
		}
		public void setStderr(String stderr) {
			this.stderr = stderr;
		}
		public String getStderr() {
			return stderr;
		}
	}
	
	public static class CommandCaller {
		// 実効ユーザ
		private final String _effectiveUser;

		// 実行するコマンド
		private final String _execCommand;

		// コマンドタイムアウト時間
		private final long _commadTimeout;

		private final String _charSet;
		
		private final String _mode;
		
		private final int _stdoutBuffer;
		
		private boolean _login;
		
		public CommandCaller(
				String effectiveUser,
				String execCommand,
				String charSet,
				long commadTimeout,
				String mode,
				int stdoutBuffer,
				boolean login) {
			_effectiveUser = effectiveUser;
			_execCommand = execCommand;
			_charSet = charSet;
			_commadTimeout = commadTimeout;
			_mode = mode;
			_stdoutBuffer = stdoutBuffer;
			_login = login;
		}

		/**
		 * CommandTaskを実行しその終了（もしくはタイムアウト）まで待つ処理を実行します
		 */
		public CommandResult call() throws Exception {
			// 初期値（コマンドが時間内に終了せずリターンコードが取得できない場合は、この値が返る）
			
			CommandCreator.PlatformType _modeType = CommandCreator.convertPlatform(_mode);
			
			String[] cmd;
			boolean specifyUser = false;
			// コマンドを実行する(実効ユーザが空欄の場合はマネージャ起動ユーザで実行)
			if (_effectiveUser.isEmpty()) {
				specifyUser = false;
				cmd = CommandCreator.createCommand(_effectiveUser, _execCommand, _modeType, specifyUser);
			} else {
				specifyUser = true;
				cmd = CommandCreator.createCommand(_effectiveUser, _execCommand, _modeType, specifyUser, _login);
			}

			m_log.info("call() excuting command. (effectiveUser = " + _effectiveUser + ", command = " + _execCommand + ", mode = " + _modeType + ", timeout = " + _commadTimeout + ")");

			// 戻り値を格納する
			CommandExecutor cmdExec = new CommandExecutor(
					new CommandExecutor.CommandExecutorParams()
						.setCommand(cmd)
						.setCharset(Charset.forName(_charSet))
						.setTimeout(_commadTimeout)
						.setBufferSize(_stdoutBuffer)
						.setForceSigterm(specifyUser));
			cmdExec.execute();
			CommandResult ret = cmdExec.getResult();

			return ret;
		}
	}
}