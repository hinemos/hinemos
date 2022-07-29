/*

Copyright (C) 2020 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.util.apllog;


import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.monitor.bean.EventConfirmConstant;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.notify.util.NotifyUtil;
import com.clustercontrol.notify.util.SendSyslog;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.Messages;

/**
*
* HinemosAgentからの内部メッセージ(sendMessageによる送信)のログ出力を行うクラス<BR>
*
* HinemosAgent内部から送られたメッセージをHinemos上の通知の一種として処理します。
*
*/
public class AgentMessageLogger {

	
	private static final String PRIORITY_INFO = "info";
	private static final String PRIORITY_WARNING = "warning";
	private static final String PRIORITY_CRITICAL = "critical";

	private static Log log = LogFactory.getLog(AplLogger.class);

	/**
	 *
	 * ログを出力します。<BR>
	 *
	 * @param info		通知基本情報
	 */
	public static void put(OutputBasicInfo info) throws HinemosUnknown {

		/////
		// 設定値取得(internal.event.target)とログ出力
		////
		boolean isEvent = HinemosPropertyCommon.internal_event_target_agent.getBooleanValue();		
		int eventLevel = getPriority(HinemosPropertyCommon.internal_event_target_agent_priority.getStringValue());		
		if(isEvent && isOutput(eventLevel, info.getPriority())){
			putEvent(info);
		}

		/////
		// 設定値取得(internal.syslog.target)とログ出力
		////
		boolean isSyslog = HinemosPropertyCommon.internal_syslog_target_agent.getBooleanValue();		
		int syslogLevel = getPriority(HinemosPropertyCommon.internal_syslog_target_agent_priority.getStringValue());		
		if (isSyslog && isOutput(syslogLevel,  info.getPriority())){
			putSyslog(info);
		}
	}

	
	/**
	 *
	 * sendMessageに基づくevent通知を出力します。<BR>
	 * @param info		通知基本情報
	 */
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

	/**
	 *
	 * sendMessageに基づくsyslog通知を出力します。<BR>
	 * @param info		通知基本情報
	 */
	public static void putSyslog(OutputBasicInfo notifyInfo) {

		/**	syslog出力用フォーマット（AplLoggerとは一部違うので注意）
		 * 「hinemos: プラグインID,アプリケーション,監視項目ID,FACILITY_ID,重要度,メッセージ,詳細メッセージ」 */
		MessageFormat syslogfmt = new MessageFormat("hinemos: {0},{1},{2},{3},{4},{5}");

		// メッセージを編集
		Locale locale = NotifyUtil.getNotifyLocale();
		String priorityStr = Messages.getString(PriorityConstant.typeToMessageCode(notifyInfo.getPriority()), locale);
		Object[] args ={notifyInfo.getPluginId(), notifyInfo.getApplication(), notifyInfo.getMonitorId(),notifyInfo.getFacilityId(),
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
						syslogSeverity, notifyInfo.getFacilityId(), logmsg, timeStamp);
			} catch (InvalidRole e) {
				log.warn("fail putSyslog monitorId=" + notifyInfo.getMonitorId() + ", message=" + notifyInfo.getMessage());
			} catch (HinemosUnknown e) {
				log.warn("fail putSyslog monitorId=" + notifyInfo.getMonitorId() + ", message=" + notifyInfo.getMessage());
			}
		}
	}

	/*
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
