/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util.apllog;


import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.bean.JobLinkConstant;
import com.clustercontrol.jobmanagement.bean.JobLinkExpInfo;
import com.clustercontrol.jobmanagement.bean.JobLinkMessageId;
import com.clustercontrol.monitor.bean.EventConfirmConstant;
import com.clustercontrol.notify.bean.NotifyTriggerType;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.notify.util.NotifyUtil;
import com.clustercontrol.notify.util.SendSyslog;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.platform.PlatformDivergence;
import com.clustercontrol.platform.util.apllog.EventLogger;
import com.clustercontrol.util.CommandCreator;
import com.clustercontrol.util.CommandCreator.PlatformType;
import com.clustercontrol.util.CommandExecutor;
import com.clustercontrol.util.CommandExecutor.CommandResult;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.InternalIdAbstract;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.StringBinder;

/**
 *
 * Hinemosの内部ログの出力を行うクラス<BR>
 *
 * Hinemos内部で発生する事象をログやHinemosのイベントとして
 * 処理します。
 *
 */
public class AplLogger {

	private static final String INTERNAL_SCOPE="INTERNAL";
	private static final String INTERNAL_SCOPE_TEXT="Hinemos_Internal";

	private static final String PRIORITY_INFO = "info";
	private static final String PRIORITY_WARNING = "warning";
	private static final String PRIORITY_CRITICAL = "critical";
	private static final String MONITOR_ID = HinemosModuleConstant.SYSYTEM;

	private static ConcurrentHashMap<Long, Boolean> m_isInternalMode = new ConcurrentHashMap<Long, Boolean>();
	private static Log FILE_LOGGER = LogFactory.getLog("HinemosInternal");
	private static Log log = LogFactory.getLog(AplLogger.class);

	/**
	 *
	 * ログを出力します。<BR>
	 * 
	 * @param internalId	INTERNALイベント情報
	 * @param msgArgs		メッセージ置換項目
	 */
	public static void put(InternalIdAbstract internalId, String[] msgArgs) {
		put(internalId, msgArgs, null);
	}

	/**
	 *
	 * ログを出力します。<BR>
	 *
	 * @param internalId	INTERNALイベント情報
	 * @param msgArgs		メッセージ置換項目
	 * @param detailMsg		詳細メッセージ
	 */
	public static void put(InternalIdAbstract internalId, String[] msgArgs, String detailMsg) {
		put(internalId, internalId.getPriority(), internalId.getPluginId(), internalId.getMessage(msgArgs), detailMsg);
	}

	/**
	 *
	 * ログを出力します。(重要度が動的に変更される場合想定)<BR>
	 *
	 * @param internalId	INTERNALイベント情報
	 * @param priority		重要度（INTERNALイベント情報に含まれていない場合設定可能）
	 * @param msgArgs		メッセージ置換項目
	 * @param detailMsg		詳細メッセージ
	 */
	public static void put(InternalIdAbstract internalId, Integer priority, String[] msgArgs, String detailMsg) {
		if (internalId.getPriority() == null) {
			if (priority != null) {
				put(internalId, priority, internalId.getPluginId(), internalId.getMessage(msgArgs), detailMsg);
			} else {
				put(internalId, PriorityConstant.TYPE_CRITICAL, internalId.getPluginId(), internalId.getMessage(msgArgs), detailMsg);
			}
		} else {
			put(internalId, internalId.getPriority(), internalId.getPluginId(), internalId.getMessage(msgArgs), detailMsg);
		}
	}

	/**
	 *
	 * ログを出力します。(クラウドからの呼び出し想定)<BR>
	 *
	 * @param internalId	INTERNALイベント情報
	 * @param message		メッセージ
	 * @param detailMsg		詳細メッセージ
	 */
	public static void put(InternalIdAbstract internalId, String message, String detailMsg) {
		put(internalId, internalId.getPriority(), internalId.getPluginId(), message, detailMsg);
	}

	/**
	 *
	 * ログを出力します。<BR>
	 * ※INTERNALイベント出力対象はInternalIdAbstractで管理しているため、このメソッドは外部から直接呼び出さないこと。
	 *
	 * @param internalId	INTERNALイベント情報
	 * @param priority		優先度
	 * @param pluginId		プラグインID
	 * @param message		メッセージ
	 * @param detailMsg		詳細メッセージ
	 * @since
	 */
	private static void put(InternalIdAbstract internalId, Integer priority, String pluginId, String message, String detailMsg) {
		// 既にINTERNALモードに入っている場合はこれ以上出力しない
		Boolean isInternalMode = m_isInternalMode.get(Thread.currentThread().getId());
		if(isInternalMode != null && isInternalMode){
			log.debug("AplLogger has been already in INTERNAL mode. No INTERNAL logs are outputted. ");
			return;
		}
		try{
			// INTERNALモードに入る
			log.debug("AplLogger INTERNAL mode start");
			m_isInternalMode.put(Thread.currentThread().getId(), true);

			//現在日時取得
			Date nowDate = HinemosTime.getDateInstance();

			//メッセージ情報作成
			OutputBasicInfo output = new OutputBasicInfo();
			output.setPluginId(pluginId);
			output.setMonitorId(MONITOR_ID);
			output.setFacilityId(INTERNAL_SCOPE);
			output.setScopeText(INTERNAL_SCOPE_TEXT);
			output.setApplication(Messages.getString(HinemosModuleConstant.nameToMessageCode(pluginId), NotifyUtil.getNotifyLocale()));
			output.setMessage(message);
			output.setMessageOrg(detailMsg == null ? "":detailMsg);
			output.setPriority(priority);
			if (nowDate != null) {
				output.setGenerationDate(nowDate.getTime());
			}
			//7.0.0との互換性保持のため、Hinemosプロパティで出力するか制御
			boolean flg = HinemosPropertyCommon.notify_output_trigger_subkey_INTERNAL.getBooleanValue();
			if (flg) {
				//監視詳細：<チェック対象のID>|<通知契機>
				output.setSubKey(internalId.getInternalId() + "|" + NotifyTriggerType.INTERNAL_EVENT.name());
			}

			/////
			// 設定値取得(internal.event)
			////
			boolean isEvent = HinemosPropertyCommon.internal_event.getBooleanValue();
			int eventLevel = getPriority(HinemosPropertyCommon.internal_event_priority.getStringValue());

			if(isEvent && isOutput(eventLevel, priority)){
				putEvent(output);
			}

			/////
			// 設定値取得(internal.file)
			////
			boolean isFile = HinemosPropertyCommon.internal_file.getBooleanValue();
			int fileLevel = getPriority(HinemosPropertyCommon.internal_file_priority.getStringValue());

			if(isFile && isOutput(fileLevel, priority)){
				putFile(output);
			}

			/////
			// 設定値取得(internal.syslog)
			////
			boolean isSyslog = HinemosPropertyCommon.internal_syslog.getBooleanValue();
			int syslogLevel = getPriority(HinemosPropertyCommon.internal_syslog_priority.getStringValue());
			if (isSyslog && isOutput(syslogLevel, priority)){
				putSyslog(output);
			}

			/////
			// 設定値取得(internal.mail)
			////
			boolean isMail = HinemosPropertyCommon.internal_mail.getBooleanValue();
			int mailLevel = getPriority(HinemosPropertyCommon.internal_mail_priority.getStringValue());
			if (isMail && isOutput(mailLevel, priority)) {
				putMail(output);
			}

			/////
			// 設定値取得(internal.command)
			////
			boolean isCommand = HinemosPropertyCommon.internal_command.getBooleanValue();
			int commandLevel = getPriority(HinemosPropertyCommon.internal_command_priority.getStringValue());

			if (isCommand && isOutput(commandLevel, priority)) {
				putCommand(output);
			}

			/////
			// 設定値取得(internal.job(ジョブ連携メッセージ送信))
			////
			boolean isJoblinkMessage = false;
			String joblinkMesForwardSettingId = HinemosPropertyCommon.internal_joblinkmes_forward_setting.getStringValue();
			if (joblinkMesForwardSettingId != null && !joblinkMesForwardSettingId.trim().isEmpty()) {
				if (internalId == InternalIdCommon.PLT_NTF_SYS_019 || internalId == InternalIdCommon.PLT_NTF_SYS_020) {
					// ジョブ連携に関連したINTERNALイベントは対象外
					log.info("put() : " + internalId.getInternalId() + " is not output as a Job Link Message.");
				} else {
					isJoblinkMessage = true;
				}
			}
			int joblinkMesLevel = getPriority(HinemosPropertyCommon.internal_joblinkmes_forward_priority.getStringValue());

			if (isJoblinkMessage && isOutput(joblinkMesLevel, priority)) {
				putJobLinkMessage(output, internalId);
			}

			/////
			// Windowsイベントログ
			////
			EventLogger.internal(priority, output);
			
		}finally{
			// INTERNALモードから出る
			m_isInternalMode.put(Thread.currentThread().getId(), false);
			log.debug("AplLogger INTERNAL mode end");
		}
	}

	private static void putFile(OutputBasicInfo notifyInfo) {
		/**	ログファイル出力用フォーマット「日付  プラグインID,アプリケーション,監視項目ID,メッセージID,ファシリティID,メッセージ,詳細メッセージ」 */
		MessageFormat logfmt = new MessageFormat("{0,date,yyyy/MM/dd HH:mm:ss}  {1},{2},{3},{4},{5},{6}");
		// Locale取得
		Locale locale = NotifyUtil.getNotifyLocale();
		//メッセージを編集
		Object[] args ={notifyInfo.getGenerationDate(),notifyInfo.getPluginId(), notifyInfo.getApplication(),
				notifyInfo.getMonitorId(), notifyInfo.getPriority(),
				HinemosMessage.replace(notifyInfo.getMessage(), locale), notifyInfo.getMessageOrg()};
		String logmsg = logfmt.format(args);
		//ファイル出力
		log.debug("putFile() logmsg = " + logmsg);
		FILE_LOGGER.info(logmsg);
	}

	private static void putSyslog(OutputBasicInfo notifyInfo) {
		/**	syslog出力用フォーマット
		 * 「日付  プラグインID,アプリケーション,監視項目ID,ファシリティID,メッセージ,詳細メッセージ」 */
		MessageFormat syslogfmt = new MessageFormat("hinemos: {0},{1},{2},{3},{4}");

		// メッセージを編集
		Locale locale = NotifyUtil.getNotifyLocale();
		String priorityStr = Messages.getString(PriorityConstant.typeToMessageCode(notifyInfo.getPriority()), locale);
		Object[] args ={notifyInfo.getPluginId(), notifyInfo.getApplication(), notifyInfo.getMonitorId(),
				priorityStr, HinemosMessage.replace(notifyInfo.getMessage(), locale), notifyInfo.getMessageOrg()};
		String logmsg = syslogfmt.format(args);

		// 送信時刻をセット
		SimpleDateFormat sdf = new SimpleDateFormat(SendSyslog.HEADER_DATE_FORMAT, Locale.US);
		sdf.setTimeZone(HinemosTime.getTimeZone());
		String timeStamp = sdf.format(HinemosTime.getDateInstance());

		/////
		// 設定値取得(internal.syslog)
		////
		String hosts = HinemosPropertyCommon.internal_syslog_host.getStringValue();
		String[] syslogHostList = hosts.split(",");
		int syslogPort = HinemosPropertyCommon.internal_syslog_port.getIntegerValue();
		String syslogFacility = HinemosPropertyCommon.internal_syslog_facility.getStringValue();
		String syslogSeverity = HinemosPropertyCommon.internal_syslog_severity.getStringValue();

		for (String syslogHost : syslogHostList) {
			log.debug("putSyslog() syslogHost = " + syslogHost + ", syslogPort = " + syslogPort +
					", syslogFacility = " + syslogFacility + ", syslogSeverity = " + syslogSeverity +
					", logmsg = " + logmsg + ", timeStamp = " + timeStamp);

			try {
				new NotifyControllerBean().sendAfterConvertHostname(syslogHost, syslogPort, syslogFacility,
						syslogSeverity, INTERNAL_SCOPE, logmsg, timeStamp);
			} catch (InvalidRole e) {
				log.warn("fail putSyslog monitorId=" + notifyInfo.getMonitorId() + ", message=" + notifyInfo.getMessage());
			} catch (HinemosUnknown e) {
				log.warn("fail putSyslog monitorId=" + notifyInfo.getMonitorId() + ", message=" + notifyInfo.getMessage());
			}
		}
	}

	private static boolean putEvent(OutputBasicInfo notifyInfo) {
		JpaTransactionManager jtm = null;
		
		try {
			// rollbackするとイベントが出力されなくなるため、rollback用のコールバックメソッドを登録する 
			jtm = new JpaTransactionManager(); 
			jtm.begin(); 
			
			jtm.addCallback(new AplLoggerPutEventAfterRollbackCallback(notifyInfo)); 
			
			jtm.commit(); 
			
			new NotifyControllerBean().insertEventLog(notifyInfo, EventConfirmConstant.TYPE_UNCONFIRMED);
			return true;
		} catch (HinemosUnknown e) {
			log.warn("fail putEvent monitorId=" + notifyInfo.getMonitorId() + ", message=" + notifyInfo.getMessage());
			return false;
		} catch (InvalidRole e) {
			log.warn("fail putEvent monitorId=" + notifyInfo.getMonitorId() + ", message=" + notifyInfo.getMessage());
			return false;
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	private static void putMail(OutputBasicInfo notifyInfo) {
		// メール通知（デフォルトテンプレート）
		try {
			String addr = HinemosPropertyCommon.internal_mail_address.getStringValue();
			String[] mailAddress = addr.split(",");
			new NotifyControllerBean().sendMail(mailAddress, notifyInfo);
		} catch (InvalidRole | HinemosUnknown | RuntimeException e) {
			log.warn("fail putMail monitorId=" + notifyInfo.getMonitorId() + ", message=" + notifyInfo.getMessage());
		}
	}

	private static void putCommand(OutputBasicInfo notifyInfo) {
		// コマンド通知
		try {
			String commandUser = HinemosPropertyDefault.internal_command_user.getStringValue();
			String commandLine = HinemosPropertyDefault.internal_command_commandline.getStringValue();
			int commandTimeout = 0;
			try {
				commandTimeout = HinemosPropertyCommon.internal_command_timeout.getIntegerValue();
			} catch (Exception e) {}

			Map<String, String> param = NotifyUtil.createParameter(notifyInfo, null);
			StringBinder binder = new StringBinder(param);
			String command = binder.bindParam(commandLine);

			PlatformType platformType = PlatformDivergence.getPlatformType();
			
			boolean specifyUser = false;
			
			if (commandUser != null && !"".equals(commandUser)) {
				specifyUser = true;
			}
			
			log.info(String.format(
				"excuting command. (specifyUser = %s, effectiveUser = %s, command = %s, mode = %s, timeout = %d)",
				specifyUser, commandUser, command, platformType.toString(), commandTimeout)
			);
			
			String[] cmd = CommandCreator.createCommand(commandUser, command, platformType, specifyUser);
			CommandExecutor cmdExec = new CommandExecutor(cmd, commandTimeout);
			cmdExec.execute();
			CommandResult ret = cmdExec.getResult();

			if (ret != null) {
				log.info("executed command. (exitCode = " + ret.exitCode + ", stdout = " + ret.stdout + ", stderr = " + ret.stderr + ")");
			}
		} catch (HinemosException | RuntimeException e) {
			log.warn("fail putCommand monitorId=" + notifyInfo.getMonitorId() + ", message=" + notifyInfo.getMessage(), e);
			return;
		}
	}

	private static void putJobLinkMessage(OutputBasicInfo notifyInfo, InternalIdAbstract internalId) {
		// ジョブ通知 (ジョブ連携メッセージ送信)

		// ジョブ連携メッセージID設定
		notifyInfo.setJoblinkMessageId(JobLinkMessageId.getIdForInternal());

		List<JobLinkExpInfo> expList = new ArrayList<>();
		JobLinkExpInfo expInfo = new JobLinkExpInfo();
		expInfo.setKey(JobLinkConstant.EXP_KEY_INTERNAL_ID);
		expInfo.setValue(internalId.getInternalId());
		expList.add(expInfo);

		JpaTransactionManager jtm = null;
		
		try {
			// rollbackするとイベントが出力されなくなるため、rollback用のコールバックメソッドを登録する 
			jtm = new JpaTransactionManager(); 
			jtm.begin(); 
			
			jtm.addCallback(new AplLoggerPutJobLinkMessageAfterRollbackCallback(notifyInfo, expList)); 
			
			jtm.commit(); 
			
			new NotifyControllerBean().sendJobLinkMessage(notifyInfo, expList);
		} catch (HinemosUnknown e) {
			log.warn("fail putJobLinkMessage monitorId=" + notifyInfo.getMonitorId() + ", message=" + notifyInfo.getMessage());
		} catch (InvalidRole e) {
			log.warn("fail putJobLinkMessage monitorId=" + notifyInfo.getMonitorId() + ", message=" + notifyInfo.getMessage());
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 *
	 * 文字列から、Priority区分を取得します。<BR>
	 *
	 * @param priority
	 * @since
	 */
	private static int getPriority(String priority) {
		int ret = PriorityConstant.TYPE_UNKNOWN;
		if(priority.equals(PRIORITY_CRITICAL)){
			ret = PriorityConstant.TYPE_CRITICAL;
		}else if(priority.equals(PRIORITY_WARNING)){
			ret = PriorityConstant.TYPE_WARNING;
		}else if(priority.equals(PRIORITY_INFO)){
			ret = PriorityConstant.TYPE_INFO;
		}
		return ret;
	}

	/**
	 * 送信を行うかのPriority毎の判定を行う
	 */
	private static boolean isOutput(int level, int priority){
		if (priority == PriorityConstant.TYPE_CRITICAL) {
			if (level == PriorityConstant.TYPE_CRITICAL ||
					level == PriorityConstant.TYPE_UNKNOWN ||
					level == PriorityConstant.TYPE_WARNING ||
					level == PriorityConstant.TYPE_INFO) {
				return true;
			} else {
				return false;
			}
		}
		if (priority == PriorityConstant.TYPE_UNKNOWN) {
			if (level == PriorityConstant.TYPE_UNKNOWN ||
					level == PriorityConstant.TYPE_WARNING ||
					level == PriorityConstant.TYPE_INFO) {
				return true;
			} else {
				return false;
			}
		}
		if (priority == PriorityConstant.TYPE_WARNING) {
			if (level == PriorityConstant.TYPE_WARNING ||
					level == PriorityConstant.TYPE_INFO) {
				return true;
			} else {
				return false;
			}
		}
		if (priority == PriorityConstant.TYPE_INFO) {
			if (level == PriorityConstant.TYPE_INFO) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
}
