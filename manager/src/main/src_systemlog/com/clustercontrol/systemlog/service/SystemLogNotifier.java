/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.systemlog.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.systemlog.bean.SyslogMessage;
import com.clustercontrol.util.MessageConstant;

public class SystemLogNotifier {
	/** ログ出力のインスタンス  */
	private final static Log m_log = LogFactory.getLog(SystemLogNotifier.class);

	public List<OutputBasicInfo> createOutputBasicInfoList(
			String receiverId,
			List<SyslogMessage> syslogList,
			MonitorInfo monitorInfo,
			List<MonitorStringValueInfo> monitorStringValueInfoList,
			List<String> facilityIdList,
			RunInstructionInfo runInstructionInfo) {

		List<OutputBasicInfo> rtn = new ArrayList<>();

		for (int i = 0; i < syslogList.size(); i++) {
			SyslogMessage syslog = syslogList.get(i);
			MonitorStringValueInfo monitorStringValueInfo = monitorStringValueInfoList.get(i);
			String facilityId = facilityIdList.get(i);
			
			String message = "";
			if (HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorInfo.getMonitorTypeId())) {
				message += MessageConstant.LOGFILE_FILENAME.getMessage() + "=" + monitorInfo.getLogfileCheckInfo().getLogfile() + "\n";
			}
			
			message += MessageConstant.LOGFILE_PATTERN.getMessage() + "=" 
					+ monitorStringValueInfo.getPattern() + "\n" + MessageConstant.LOGFILE_LINE.getMessage() + "=" + syslog.rawSyslog;
			
			//マッチした場合
			// メッセージ作成（未登録ノードの時は、送信元ホスト名をスコープ名にする。）
			m_log.debug("call makeMessage.");
			OutputBasicInfo output = new OutputBasicInfo();

			output.setNotifyGroupId(NotifyGroupIdGenerator.generate(monitorInfo));
			output.setMonitorId(monitorStringValueInfo.getMonitorId());
			output.setFacilityId(facilityId);

			output.setPluginId(HinemosModuleConstant.MONITOR_SYSTEMLOG);

			// 通知抑制を監視項目の「パターンマッチ表現」単位にするため、通知抑制用サブキーを設定する。
			output.setSubKey(monitorStringValueInfo.getPattern());

			if(FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(facilityId)){
				//未登録ノードの場合には送信元のホスト名を表示する。
				output.setScopeText(syslog.hostname);
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

			output.setApplication(monitorInfo.getApplication());

			// メッセージに#[LOG_LINE]が入力されている場合は、
			// オリジナルメッセージに置換する。
			if (monitorStringValueInfo.getMessage() != null) {
				String str = monitorStringValueInfo.getMessage().replace("#[LOG_LINE]", syslog.message);
				//DBよりオリジナルメッセージ出力文字数を取得
				int maxLen = HinemosPropertyCommon.monitor_log_line_max_length.getIntegerValue();
				if (str.length() > maxLen) {
					str = str.substring(0, maxLen);
				}
				output.setMessage(str);
			}

			output.setMessageOrg(message);
			output.setPriority(monitorStringValueInfo.getPriority());
			output.setGenerationDate(syslog.date);

			if(receiverId != null && !"".equals(receiverId)){
				output.setMultiId(receiverId);
			}

			rtn.add(output);
			m_log.debug("called makeMessage.");
		}
		
		//メッセージ送信処理
		if (runInstructionInfo != null) {
			// 監視ジョブ
			if (rtn != null && !rtn.isEmpty()) {
				MonitorJobWorker.endMonitorJob(
					runInstructionInfo,
					HinemosModuleConstant.MONITOR_SYSTEMLOG,
					rtn.get(0).getMessageOrg(),
					"",
					RunStatusConstant.END,
					MonitorJobWorker.getReturnValue(runInstructionInfo, rtn.get(0).getPriority()));
			}
			return new ArrayList<OutputBasicInfo>();
		} else {
			// 監視ジョブ以外
			return rtn;
		}
	}
}
