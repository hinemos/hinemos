/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.CommandTemplateNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.notify.bean.CommandSettingTypeConstant;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.NotifyCommandInfo;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.CommandSettingTypeEnum;
import com.clustercontrol.util.CommandCreator;
import com.clustercontrol.util.CommandExecutor;
import com.clustercontrol.util.CommandExecutor.CommandResult;
import com.clustercontrol.util.StringBinder;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * コマンドを実行するクラス<BR>
 *
 * @version 3.0.0
 * @since 3.0.0
 */
public class ExecCommand implements Notifier {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( ExecCommand.class );

	// コマンド実行にタイムアウトを設けるため、2段のExecutorの構成とする
	// 1.「コマンドを実行するスレッド（CommandTask）」を実行するExecutor
	//   （スレッド起動後直ぐに制御を戻す）
	// 2.コマンドを実行するスレッド（CommandTask）用のExecutor
	//   （スレッド起動後、コマンドの実行終了もしくは、タイムアウトを待つ）

	// 「コマンドを実行するスレッド（CommandTask）」を実行し、その終了を待つスレッド（CommandCallerTask）用の
	// スレッドプールを保持するExecutorService
	private static ExecutorService _callerExecutorService;

	static {
	// hinemos.propertiesからスレッドプール数を取得する
		int threadPoolCount = HinemosPropertyCommon.notify_command_thread_pool_count.getIntegerValue();

		_callerExecutorService = new MonitoredThreadPoolExecutor(threadPoolCount, threadPoolCount,
				0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(),
				new CommandTaskThreadFactory());
	}

	/**
	 * 指定されたコマンドを呼出します。
	 *
	 * @param outputInfo 通知・抑制情報
	 * @throws NotifyNotFound
	 * @throws HinemosUnknown
	 */
	@Override
	public synchronized void notify(NotifyRequestMessage message)
			throws NotifyNotFound, HinemosUnknown {
		if(m_log.isDebugEnabled()){
			m_log.debug("notify() " + message);
		}

		executeCommand(message.getOutputInfo(), message.getNotifyId());
	}

	// 文字列を置換する
	private String getCommandString(OutputBasicInfo outputInfo, NotifyCommandInfo commandInfo, int priority) throws CommandTemplateNotFound, InvalidRole{
		String command;
		if (commandInfo.getCommandSettingType().equals(CommandSettingTypeConstant.CHOICE_TEMPLATE)) {
			command = QueryUtil.getCommandTemplateInfoPK(getCommand(commandInfo, priority)).getCommand();
		} else {
			command = getCommand(commandInfo, priority);
		}
		// 文字列を置換する
		try {
			int maxReplaceWord = HinemosPropertyCommon.replace_param_max.getIntegerValue().intValue();
			
			ArrayList<String> inKeyList = StringBinder.getKeyList(command, maxReplaceWord);
			Map<String, String> param = NotifyUtil.createParameter(outputInfo,
					commandInfo.getNotifyInfoEntity(), inKeyList);
			StringBinder binder = new StringBinder(param);
			return binder.replace(command);
		} catch (Exception e) {
			m_log.warn("notify() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);

			// 例外が発生した場合は、置換前の文字列を返す
			return getCommand(commandInfo, priority);
		}
	}

	private String getCommand(NotifyCommandInfo commandInfo, int priority) {
		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			return commandInfo.getInfoCommand();
		case PriorityConstant.TYPE_WARNING:
			return commandInfo.getWarnCommand();
		case PriorityConstant.TYPE_CRITICAL:
			return commandInfo.getCriticalCommand();
		case PriorityConstant.TYPE_UNKNOWN:
			return commandInfo.getUnknownCommand();

		default:
			break;
		}
		return null;
	}

	/**
	 * 指定されたコマンドを呼出します。
	 *
	 * @param outputInfo 通知・抑制情報
	 */
	private synchronized void executeCommand(
			OutputBasicInfo outputInfo,
			String notifyId
			) {
		if(m_log.isDebugEnabled()){
			m_log.debug("executeCommand() " + outputInfo);
		}

		NotifyCommandInfo commandInfo;
		try {
			commandInfo = QueryUtil.getNotifyCommandInfoPK(notifyId);

			// 「commandInfo.getCommand()」と「command」の違いに注意が必要。
			// 「commandInfo.getCommand()」は、設定時の実行コマンドで、
			// TextReplacerによる文字列置換前の実行コマンド
			// 「command」は、実際に実行対象のコマンド文字列
			String command = getCommandString(outputInfo, commandInfo, outputInfo.getPriority());

			/**
			 * 実行
			 */
			// 起動ユーザ名取得
			String sysUserName = System.getProperty("user.name");
			String effectiveUser = getEffectiveUser(commandInfo, outputInfo.getPriority());
			long commadTimeout = commandInfo.getTimeout();

			// Hinemos Managerの起動ユーザがroot以外の場合で、
			// 起動ユーザとコマンド実行ユーザが異なる場合は、コマンド実行しない
			if ((!effectiveUser.isEmpty()) && (!sysUserName.equals("root")) && (!sysUserName.equals(effectiveUser))) {
				// 起動失敗
				String detailMsg = "The execution user of the command and hinemos manager's user are different.";
				m_log.info(detailMsg);
				String[] args = { notifyId };
				AplLogger.put(InternalIdCommon.PLT_NTF_SYS_007, args, detailMsg);

				return;
			} else {
				m_log.debug("NotifyCommand Submit : " + outputInfo + " command=" + command);

				// 並列でコマンド実行
				Future<Long> ret = _callerExecutorService.submit(
						new CommandCallerTask(
								effectiveUser,
								command,
								notifyId,
								outputInfo,
								commadTimeout));
				
				if (ret.isCancelled())
					m_log.debug("Cancelled NotifyCommand Submit : " + outputInfo + " command=" + command);
			}
		} catch (NotifyNotFound | CommandTemplateNotFound | InvalidRole e) {
			String detailMsg = e.getMessage();
			m_log.info("executeCommand() " + detailMsg + " : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			String[] args = { notifyId };
			AplLogger.put(InternalIdCommon.PLT_NTF_SYS_007, args, detailMsg);
		}
	}

	private String getEffectiveUser(NotifyCommandInfo commandInfo,
			int priority) {
		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			return commandInfo.getInfoEffectiveUser();
		case PriorityConstant.TYPE_WARNING:
			return commandInfo.getWarnEffectiveUser();
		case PriorityConstant.TYPE_CRITICAL:
			return commandInfo.getCriticalEffectiveUser();
		case PriorityConstant.TYPE_UNKNOWN:
			return commandInfo.getUnknownEffectiveUser();

		default:
			break;
		}
		return null;
	}

	// 並列でコマンドを実行するためのクラス
	private static class CommandCallerTask implements Callable<Long> {
		// 実効ユーザ
		private final String _effectiveUser;

		// 実行するコマンド
		private final String _execCommand;

		private final String _notifyId;

		private final long _commadTimeout;

		public CommandCallerTask(
				String effectiveUser,
				String execCommand,
				String notifyId,
				OutputBasicInfo outputInfo,
				long commadTimeout) {
			_effectiveUser = effectiveUser;
			_execCommand = execCommand;
			_notifyId = notifyId;
			_commadTimeout = commadTimeout;
		}

		/**
		 * CommandTaskを実行しその終了（もしくはタイムアウト）まで待つ処理を実行します
		 */
		@Override
		public Long call() throws Exception {
			// 初期値（コマンドが時間内に終了せずリターンコードが取得できない場合は、この値が返る）
			long returnValue = Long.MIN_VALUE;

			// コマンドのエンコーディングを設定
			String charset =  HinemosPropertyDefault.notify_command_charset.getStringValue();

			// コマンド作成モードをプロパティから取得する
			String _mode = HinemosPropertyCommon.notify_command_create_mode.getStringValue();
			CommandCreator.PlatformType _modeType = CommandCreator.convertPlatform(_mode);

			String[] cmd;
			// コマンドを実行する(実効ユーザが空欄の場合はマネージャ起動ユーザで実行)
			if (_effectiveUser.isEmpty()) {
				cmd = CommandCreator.createCommand(_effectiveUser, _execCommand, _modeType, false);
			} else {
				cmd = CommandCreator.createCommand(_effectiveUser, _execCommand, _modeType);
			}

			m_log.info("call() excuting command. (effectiveUser = " + _effectiveUser + ", command = " + _execCommand + ", mode = " + _modeType + ", timeout = " + _commadTimeout + ")");

			// 戻り値を格納する
			CommandExecutor cmdExec = new CommandExecutor(cmd, Charset.forName(charset), _commadTimeout);
			cmdExec.execute();
			CommandResult ret = cmdExec.getResult();

			if (ret != null) {
				m_log.info("call() executed command. (exitCode = " + ret.exitCode + ", stdout = " + ret.stdout + ", stderr = " + ret.stderr + ")");
			}

			if (ret == null || ret.exitCode == null) {
				String[] args = { _notifyId };
				// 通知失敗メッセージを出力
				AplLogger.put(InternalIdCommon.PLT_NTF_SYS_007, args, "command execution failure (timeout). [command = " + _execCommand + "]");

			} else {
				returnValue = ret.exitCode;
				// コマンドの成功時の終了値をDBから取得する
				int _successExitCode = HinemosPropertyCommon.notify_command_success_exit.getIntegerValue();

				if (returnValue != _successExitCode) {
					String[] args = { _notifyId };
					// 通知失敗メッセージを出力
					AplLogger.put(InternalIdCommon.PLT_NTF_SYS_007, args, "command execution failure. [command = "
							+ _execCommand + ", exit code = " +  returnValue + ", stdout = " + ret.stdout + ", stderr = " + ret.stderr + "]");

				}
			}

			return returnValue;
		}
	}

	// CommandTaskのスレッドに名前を定義するためFactoryを実装
	private static class CommandTaskThreadFactory implements ThreadFactory {
		private volatile int _count = 0;

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "NotifyCommandTask-" + _count++);
		}
	}
}
