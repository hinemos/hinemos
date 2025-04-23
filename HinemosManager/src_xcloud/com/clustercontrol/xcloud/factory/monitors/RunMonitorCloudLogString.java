/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.xcloud.factory.monitors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.hub.bean.CollectStringTag;
import com.clustercontrol.hub.bean.StringSample;
import com.clustercontrol.hub.bean.StringSampleTag;
import com.clustercontrol.hub.util.CollectStringDataUtil;
import com.clustercontrol.jobmanagement.bean.MonitorJobEndNode;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.XMLUtil;
import com.clustercontrol.xcloud.bean.CloudConstant;

/**
 * クラウド通知の監視結果を処理するクラス
 *
 */
public class RunMonitorCloudLogString {
	
	public static final Log _log = LogFactory.getLog(RunMonitorCloudLogString.class);
	private List<MonitorJobEndNode> monitorJobEndNodeList = new ArrayList<>();

	/**
	 * クラウド通知の監視結果の通知メッセージ化、および収集処理を行うメソッド
	 * @param facilityId
	 * @param result
	 * @return
	 * @throws HinemosUnknown
	 */
	public List<OutputBasicInfo> run(String facilityId, CloudLogResultDTO result) throws HinemosUnknown {
		List<OutputBasicInfo> rtn = new ArrayList<>();
		
		// オリジナルメッセージの作成
		String origMessage = "";
		boolean isAWS = false;
		String logGroup = "";
		String filepath = "";
		String logStream = "";
		String col = "";
		String res = "";
		String logStreamInSetting = "";
		// AWSとAzureで異なる
		for (MonitorPluginStringInfo i : result.monitorInfo.getPluginCheckInfo().getMonitorPluginStringInfoList()) {
			if (i.getKey().equals(CloudConstant.cloudLog_platform)) {
				if (i.getValue().equals(CloudConstant.platform_AWS)) {
					isAWS = true;
					continue;
				}
			}
			if (i.getKey().equals(CloudConstant.cloudLog_LogGroup)) {
				logGroup = i.getValue();
			}
			if (i.getKey().equals(CloudConstant.cloudLog_LogStream)) {
				logStreamInSetting = i.getValue();
			}
			if (i.getKey().equals(CloudConstant.cloudLog_Col)) {
				col = i.getValue();
			}
			if (i.getKey().equals(CloudConstant.cloudLog_ResourceGroup)) {
				res = i.getValue();
			}
		}
		// ログストリーム/テーブルの取得
		if (isAWS) {
			filepath = result.monitorInfo.getLogfileCheckInfo().getLogfile();
			// AWSの場合ファイルパスにはログストリーム名が格納される
			if (filepath != null && !filepath.isEmpty()) {
				logStream = filepath;
			} else {
				// 何らかの理由でログストリーム名がからの場合は、監視設定の物を使用する
				if (logStreamInSetting == null) {
					// 監視設定で指定がない場合はnullになるので空文字に置き換え
					logStream = "";
				} else {
					logStream = logStreamInSetting;
				}
			}
		} else {
			// Azureの場合は監視設定の物を使用する
			logStream = logStreamInSetting;
		}
		
		// 監視ジョブ以外の場合、収集処理.
		if (result.runInstructionInfo == null && result.monitorInfo.getCollectorFlg() != null && result.monitorInfo.getCollectorFlg()) {
			StringSample sample = new StringSample(new Date(result.msgInfo.getGenerationDate()),
					result.monitorInfo.getMonitorId());
			List<StringSampleTag> tagsList = new ArrayList<StringSampleTag>();
			tagsList.add(new StringSampleTag(CollectStringTag.TIMESTAMP_IN_LOG,
					Long.toString(result.msgInfo.getGenerationDate())));
			if (isAWS) {
				tagsList.add(new StringSampleTag(CollectStringTag.CLOUDLOG_AWS_GROUP, logGroup));
				tagsList.add(new StringSampleTag(CollectStringTag.CLOUDLOG_AWS_STREAM, logStream));
			} else {
				tagsList.add(new StringSampleTag(CollectStringTag.CLOUDLOG_AZURE_RESOURCEGROUP, res));
				tagsList.add(new StringSampleTag(CollectStringTag.CLOUDLOG_AZURE_WORKSPACE, logGroup));
				tagsList.add(new StringSampleTag(CollectStringTag.CLOUDLOG_AZURE_TABLE, logStream));
				tagsList.add(new StringSampleTag(CollectStringTag.CLOUDLOG_AZURE_COL, col));
			}
			sample.set(facilityId, result.monitorInfo.getMonitorTypeId(), XMLUtil.ignoreInvalidString(result.message.trim()), tagsList);
			CollectStringDataUtil.store(Arrays.asList(sample));
		}

		if (result.monitorStrValueInfo == null) {
			return rtn;
		}
		
		if (isAWS) {
			origMessage = MessageConstant.CLOUDLOG_SOURCE.getMessage() + "= LogGroup: " + logGroup + ", LogStream: "
					+ logStream + "\n" + MessageConstant.LOGFILE_PATTERN.getMessage() + "="
					+ result.monitorStrValueInfo.getPattern() + "\n" + MessageConstant.LOGFILE_LINE.getMessage() + "="
					+ result.message.trim();
		} else {
			origMessage = MessageConstant.CLOUDLOG_SOURCE.getMessage() + "= ResourceGroup: " + res + ", Workspace: "
					+ logGroup + ", Table: " + logStream + ", Column: " + col + "\n"
					+ MessageConstant.LOGFILE_PATTERN.getMessage() + "=" + result.monitorStrValueInfo.getPattern()
					+ "\n" + MessageConstant.LOGFILE_LINE.getMessage() + "=" + result.message.trim();
		}

		// 通知情報の作成
		OutputBasicInfo output = new OutputBasicInfo();
		output.setNotifyGroupId(NotifyGroupIdGenerator.generate(result.monitorInfo));
		output.setMonitorId(result.monitorStrValueInfo.getMonitorId());
		output.setFacilityId(facilityId);
		output.setPluginId(HinemosModuleConstant.MONITOR_CLOUD_LOG);
		output.setSubKey(result.monitorStrValueInfo.getPattern());
		
		String facilityPath = new RepositoryControllerBean().getFacilityPath(facilityId, null);
		output.setScopeText(facilityPath);

		output.setApplication(result.monitorInfo.getApplication());
		
		// 変数の置換
		if (result.monitorStrValueInfo.getMessage() != null) {
			String str = result.monitorStrValueInfo.getMessage().replace("#[LOG_LINE]", result.message.trim());
			int maxLen = HinemosPropertyCommon.monitor_log_line_max_length.getIntegerValue();
			if (str.length() > maxLen) {
				str = str.substring(0, maxLen);
			}
			output.setMessage(str);
		}

		output.setMessageOrg(origMessage);
		output.setPriority(result.monitorStrValueInfo.getPriority());
		output.setGenerationDate(result.msgInfo.getGenerationDate());
		output.setRunInstructionInfo(result.runInstructionInfo);
		output.setMultiId(HinemosPropertyCommon.monitor_systemlog_receiverid.getStringValue());
		
		// 重要度変化関連の設定
		// クラウドログ監視はv.7.0からの機能なので、ログファイル監視にあるような
		// 下位互換のための補完は行わない
		output.setPriorityChangeJudgmentType(result.monitorInfo.getPriorityChangeJudgmentType());
		output.setPriorityChangeFailureType(result.monitorInfo.getPriorityChangeFailureType());

		if (result.runInstructionInfo == null) {
			// 監視ジョブ以外
			rtn.add(output);
		} else {
			// 監視ジョブ
			this.monitorJobEndNodeList.add(new MonitorJobEndNode(
					output.getRunInstructionInfo(),
					HinemosModuleConstant.MONITOR_CLOUD_LOG,
					makeJobOrgMessage(result.monitorInfo, output.getMessageOrg()),
					"",
					RunStatusConstant.END,
					MonitorJobWorker.getReturnValue(output.getRunInstructionInfo(), output.getPriority())));
		}
		return rtn;
	}

	/**
	 * 監視ジョブの実行結果を返す
	 * 
	 * @return 監視ジョブの実行結果
	 */
	public List<MonitorJobEndNode> getMonitorJobEndNodeList() {
		return this.monitorJobEndNodeList;
	}

	/**
	 * 監視ジョブ用のメッセージを作成するメソッド
	 * @param monitorInfo
	 * @param orgMsg
	 * @return
	 */
	private String makeJobOrgMessage(MonitorInfo monitorInfo, String orgMsg) {
		if (monitorInfo == null || monitorInfo.getPluginCheckInfo() == null) {
			return "";
		}
		
		String platform="";
		
		for(MonitorPluginStringInfo i:monitorInfo.getPluginCheckInfo().getMonitorPluginStringInfoList()){
			if(i.getKey().equals(CloudConstant.cloudLog_platform)){
				platform=i.getValue();
			}
		}
		
		String[] args = {
				platform,
				monitorInfo.getFacilityId()};
		
		return MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_CLOUDLOG.getMessage(args)
				+ "\n" + orgMsg;
	}
}
