/*

 Copyright (C) 2011 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */


package com.clustercontrol.systemlog.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.systemlog.bean.SyslogMessage;
import com.clustercontrol.util.MessageConstant;

public class SystemLogNotifier {
	/** ログ出力のインスタンス  */
	private final static Log m_log = LogFactory.getLog(SystemLogNotifier.class);

	public void put(
			String receiverId,
			List<SyslogMessage> syslogList,
			MonitorInfo monitorInfo,
			List<MonitorStringValueInfo> ruleList,
			List<String> facilityIdList,
			RunInstructionInfo runInstructionInfo) {

		ArrayList<OutputBasicInfo> logOutputList = new ArrayList<OutputBasicInfo>();
		for (int i = 0; i < syslogList.size(); i++) {
			SyslogMessage syslog = syslogList.get(i);
			MonitorStringValueInfo rule = ruleList.get(i);
			String logFacilityId = facilityIdList.get(i);
			
			String message = "";
			if (HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorInfo.getMonitorTypeId())) {
				message += MessageConstant.LOGFILE_FILENAME.getMessage() + "=" + monitorInfo.getLogfileCheckInfo().getLogfile() + "\n";
			}
			
			message += MessageConstant.LOGFILE_PATTERN.getMessage() + "=" + rule.getPattern() + "\n" + MessageConstant.LOGFILE_LINE.getMessage() + "=" + syslog.rawSyslog;
			
			//マッチした場合
			// メッセージ作成（未登録ノードの時は、送信元ホスト名をスコープ名にする。）
			m_log.debug("call makeMessage.");
			OutputBasicInfo logOutput  = makeMessage(receiverId, message, syslog,
					monitorInfo, rule, logFacilityId, syslog.hostname);
			logOutputList.add(logOutput);
			m_log.debug("called makeMessage.");
		}
		
		//メッセージ送信処理
		if (runInstructionInfo == null) {
			// 監視ジョブ以外
			new NotifyControllerBean().notify(logOutputList, NotifyGroupIdGenerator.generate(monitorInfo));
		} else {
			if (!logOutputList.isEmpty()) {
			// 監視ジョブ
			MonitorJobWorker.endMonitorJob(
					runInstructionInfo,
					HinemosModuleConstant.MONITOR_SYSTEMLOG,
					logOutputList.get(0).getMessageOrg(),
					"",
					RunStatusConstant.END,
					MonitorJobWorker.getReturnValue(runInstructionInfo, logOutputList.get(0).getPriority()));
			}
		}
	}

	/**
	 * 引数で指定された情報より、ログ出力メッセージを生成し返します。
	 *
	 * @param msg メッセージ
	 * @param logmsg syslogメッセージ情報
	 * @param moninfo 監視項目情報
	 * @param filterInfo フィルタ情報
	 * @param facilityID ファシリティID
	 * @param nodeName ノード名
	 * @return ログ出力メッセージ
	 *
	 * @since 1.0.0
	 */
	private static OutputBasicInfo makeMessage(
			String receiverId,
			String msg,
			SyslogMessage syslog,
			MonitorInfo monInfo,
			MonitorStringValueInfo filterInfo,
			String facilityId,
			String nodeName) {

		m_log.debug("Make ObjectMsg");

		OutputBasicInfo output = new OutputBasicInfo();

		output.setMonitorId(filterInfo.getMonitorId());
		output.setFacilityId(facilityId);

		output.setPluginId(HinemosModuleConstant.MONITOR_SYSTEMLOG);

		// 通知抑制を監視項目の「パターンマッチ表現」単位にするため、通知抑制用サブキーを設定する。
		output.setSubKey(filterInfo.getPattern());

		if(FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(facilityId)){
			//未登録ノードの場合には送信元のホスト名を表示する。
			output.setScopeText(nodeName);
		}else{
			// ファシリティのパスを設定する]
			try {
				m_log.debug("call getFacilityPath.");
				String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
				m_log.debug("called getFacilityPath.");

				output.setScopeText(facilityPath);
			} catch (Exception e) {
				m_log.warn("makeMessage() cannot get facility path.(facilityId = " + facilityId + ") : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
		}

		output.setApplication(monInfo.getApplication());

		// メッセージに#[LOG_LINE]が入力されている場合は、
		// オリジナルメッセージに置換する。
		if (filterInfo.getMessage() != null) {
			String str = filterInfo.getMessage().replace("#[LOG_LINE]", syslog.message);
			//DBよりオリジナルメッセージ出力文字数を取得
			int maxLen = HinemosPropertyUtil.getHinemosPropertyNum("monitor.log.line.max.length", Long.valueOf(256)).intValue();
			if (str.length() > maxLen) {
				str = str.substring(0, maxLen);
			}
			output.setMessage(str);
		}

		output.setMessageOrg(msg);
		output.setPriority(filterInfo.getPriority());
		output.setGenerationDate(syslog.date);

		if(receiverId != null && !"".equals(receiverId)){
			output.setMultiId(receiverId);
		}

		return output;
	}
}
