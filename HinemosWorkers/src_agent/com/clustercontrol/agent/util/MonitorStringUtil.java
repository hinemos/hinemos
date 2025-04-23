/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtLogfileCheckInfoResponse;
import org.openapitools.client.model.AgtMessageInfoRequest;
import org.openapitools.client.model.AgtMonitorInfoResponse;
import org.openapitools.client.model.AgtMonitorStringValueInfoResponse;
import org.openapitools.client.model.AgtRpaLogFileCheckInfoResponse;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.cloud.log.CloudLogResultForwarder;
import com.clustercontrol.agent.log.LogfileResultForwarder;
import com.clustercontrol.agent.rpa.RpaLogfileResultForwarder;
import com.clustercontrol.agent.rpa.scenariojob.ScenarioLogCache;
import com.clustercontrol.agent.sdml.SdmlMonitorTypeEnum;
import com.clustercontrol.agent.winevent.WinEventResultForwarder;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.jobmanagement.bean.CommandConstant;
import com.clustercontrol.util.HinemosTime;

public class MonitorStringUtil {

	// ロガー
	private static Log m_log = LogFactory.getLog(MonitorStringUtil.class);

	/**
	 * 監視文字列をパターンマッチし、マネージャに送信する
	 * @param line 監視文字列
	 * @param monitorInfo 監視設定
	 */
	public static void patternMatch(String line, AgtMonitorInfoResponse monitorInfo,
			AgtRunInstructionInfoResponse runInstructionInfo, String filename) {
		patternMatch(line, monitorInfo, runInstructionInfo, null, filename);
	}

	/**
	 * 監視文字列をパターンマッチし、マネージャに送信する
	 * @param line 監視文字列
	 * @param generationDate 生成日時
	 * @param monitorInfo 監視設定
	 */
	public static void patternMatch(String line, AgtMonitorInfoResponse monitorInfo,
			AgtRunInstructionInfoResponse runInstructionInfo, Date generationDate, String filename) {
				
		//　クラウドログ監視の場合は、ログの出力日時がカレンダの稼働時間帯に含まれているかを確認
		if (HinemosModuleConstant.MONITOR_CLOUD_LOG.equals(monitorInfo.getMonitorTypeId())) {
			if (runInstructionInfo == null && monitorInfo.getCalendar() != null
					&& !RestCalendarUtil.isRun(monitorInfo.getCalendar(), generationDate)) {
				m_log.debug(
						"patternMatch is skipped because of calendar (cloudLog). GenerationDate: " + generationDate);
				return;
			}
		} else {
			if (runInstructionInfo == null && monitorInfo.getCalendar() != null
					&& !RestCalendarUtil.isRun(monitorInfo.getCalendar())) {
				m_log.debug("patternMatch is skipped because of calendar");
				return;
			}
		}
		
		boolean processed = false;
		if (runInstructionInfo != null || monitorInfo.getMonitorFlg().booleanValue()) {
			int order_no = 0;
			for (AgtMonitorStringValueInfoResponse stringInfo : monitorInfo.getStringValueInfo()) {
				++order_no;
				if(m_log.isDebugEnabled()){
					m_log.debug("patternMatch() line = " + line
							+ ", monitorId = " + stringInfo.getMonitorId()
							+ ", orderNo = " + order_no
							+ ", pattern = " + stringInfo.getPattern());
				}
				if (!stringInfo.getValidFlg().booleanValue()) {
					continue;
				}
				String patternText = stringInfo.getPattern();
				String message = line;
				m_log.trace("patternMatch check " + message);
				
				Pattern pattern = null;
				// 大文字・小文字を区別しない場合
				if (stringInfo.getCaseSensitivityFlg().booleanValue()) {
					pattern = Pattern.compile(patternText, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
				}
				// 大文字・小文字を区別する場合
				else{
					pattern = Pattern.compile(patternText, Pattern.DOTALL);
				}
				Matcher matcher = pattern.matcher(line);
				
				if (matcher.matches()) {
					m_log.debug("patternMatch match " + message);

					// RPAシナリオジョブによるログファイル 監視の場合、終了値判定のためにログを格納しておく
					if (runInstructionInfo != null && runInstructionInfo.getCommand().equals(CommandConstant.RPA)) {
						// 条件に一致する場合
						if (stringInfo.getProcessType().booleanValue()) {
							ScenarioLogCache.get(runInstructionInfo).put(stringInfo.getOrderNo(), message, filename);
							break;
						}
					}

					// 「処理する」
					if (stringInfo.getProcessType().booleanValue()) {
						AgtMessageInfoRequest logmsg = generateAgtMessageInfoRequest(line, generationDate);
						
						AgtMonitorInfoResponse monitorInfo2 = null;
						if (filename != null) {
							monitorInfo2 = cloneAgtMonitorInfoResponse(monitorInfo);
							if (HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorInfo.getMonitorTypeId())
									|| HinemosModuleConstant.MONITOR_CLOUD_LOG.equals(monitorInfo.getMonitorTypeId())) {
								monitorInfo2.getLogfileCheckInfo().setLogfile(filename);
							} else if (HinemosModuleConstant.MONITOR_RPA_LOGFILE.equals(monitorInfo.getMonitorTypeId())) {
								monitorInfo2.getRpaLogFileCheckInfo().setFileName(filename);
							}
						}
						
						if (HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorInfo.getMonitorTypeId())) {
							if (filename != null) {
								LogfileResultForwarder.getInstance().add(message, logmsg, monitorInfo2, stringInfo, runInstructionInfo);
							} else {
								LogfileResultForwarder.getInstance().add(message, logmsg, monitorInfo, stringInfo, runInstructionInfo);
							}
						} else if (HinemosModuleConstant.MONITOR_RPA_LOGFILE.equals(monitorInfo.getMonitorTypeId())) {
							if (filename != null) {
								RpaLogfileResultForwarder.getInstance().add(message, logmsg, monitorInfo2, stringInfo, runInstructionInfo);
							} else {
								RpaLogfileResultForwarder.getInstance().add(message, logmsg, monitorInfo, stringInfo, runInstructionInfo);
							}
						} else if (HinemosModuleConstant.MONITOR_WINEVENT.equals(monitorInfo.getMonitorTypeId())) {
							WinEventResultForwarder.getInstance().add(message, logmsg, monitorInfo, stringInfo, runInstructionInfo);
						} else if (HinemosModuleConstant.MONITOR_CLOUD_LOG.equals(monitorInfo.getMonitorTypeId())) {
							if (filename != null) {
								CloudLogResultForwarder.getInstance().add(message, logmsg, monitorInfo2, stringInfo, runInstructionInfo);
							} else {
								CloudLogResultForwarder.getInstance().add(message, logmsg, monitorInfo, stringInfo, runInstructionInfo);
							}
						} 
						
						processed = true;
						
						m_log.debug("patternMatch send message : " + message);
						m_log.debug("patternMatch send logmsg message : " + logmsg.getMessage());
						m_log.debug("patternMatch send logmsg generation date : " + new Date(logmsg.getGenerationDate()));
						m_log.debug("patternMatch send logmsg hostname : " + logmsg.getHostName());
					}
					break;
				} else {
					// RPAシナリオジョブによるログファイル 監視の場合、終了値判定のためにログを格納しておく
					if (runInstructionInfo != null && runInstructionInfo.getCommand().equals(CommandConstant.RPA)) {
						// 条件に一致しない場合
						if (!stringInfo.getProcessType().booleanValue()) {
							ScenarioLogCache.get(runInstructionInfo).put(stringInfo.getOrderNo(), message, filename);
							break;
						}
					}
					
				}
			}
		} else {
			m_log.debug("patternMatch is skipped because of monitor flg");
		}
		
		m_log.debug("proceessed = " + processed + ", collectorFlg = " + monitorInfo.getCollectorFlg()
				+ ", sdmlMonitorTypeID = " + monitorInfo.getSdmlMonitorTypeId());

		if (!processed && monitorInfo.getCollectorFlg().booleanValue() && monitorInfo.getSdmlMonitorTypeId() == null) {
			AgtMessageInfoRequest logmsg = generateAgtMessageInfoRequest(line, generationDate);
			//processedがtrueとなる監視判定マッチ時の送信では filenameによって monitorInfo と  monitorInfo2 を使い分けているが、
			//ここでは sendMonitorInfoに統一して、filenameがnullでないなら monitorInfo のクローンを立てる形式としている。
			AgtMonitorInfoResponse sendMonitorInfo =  monitorInfo;
			
			if (filename != null) {
				sendMonitorInfo =  cloneAgtMonitorInfoResponse(monitorInfo);
				if (HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorInfo.getMonitorTypeId())
						|| HinemosModuleConstant.MONITOR_CLOUD_LOG.equals(monitorInfo.getMonitorTypeId())) {
					sendMonitorInfo.getLogfileCheckInfo().setLogfile(filename);
				} else if (HinemosModuleConstant.MONITOR_RPA_LOGFILE.equals(monitorInfo.getMonitorTypeId())) {
					sendMonitorInfo.getRpaLogFileCheckInfo().setFileName(filename);
				}
			}
			
			if (HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorInfo.getMonitorTypeId())) {
				LogfileResultForwarder.getInstance().add(line, logmsg, sendMonitorInfo, null, runInstructionInfo);
			} else if (HinemosModuleConstant.MONITOR_RPA_LOGFILE.equals(monitorInfo.getMonitorTypeId())) {
				RpaLogfileResultForwarder.getInstance().add(line, logmsg, sendMonitorInfo, null, runInstructionInfo);
			} else if (HinemosModuleConstant.MONITOR_WINEVENT.equals(monitorInfo.getMonitorTypeId())) {
				WinEventResultForwarder.getInstance().add(line, logmsg, sendMonitorInfo, null, runInstructionInfo);
			} else if (HinemosModuleConstant.MONITOR_CLOUD_LOG.equals(monitorInfo.getMonitorTypeId())) {
				CloudLogResultForwarder.getInstance().add(line, logmsg, sendMonitorInfo, null, runInstructionInfo);
			}
		} else if (!processed && monitorInfo.getCollectorFlg().booleanValue() && monitorInfo.getSdmlMonitorTypeId() != null) {
			for(SdmlMonitorTypeEnum sdmlMonitorType : SdmlMonitorTypeEnum.values()){
				if(monitorInfo.getSdmlMonitorTypeId().equals(sdmlMonitorType.getId())){
					if(sdmlMonitorType.getFormat().matcher(line).matches()){
						AgtMessageInfoRequest logmsg = generateAgtMessageInfoRequest(line, generationDate);
						//processedがtrueとなる監視判定マッチ時の送信では filenameによって monitorInfo と  monitorInfo2 を使い分けているが、
						//ここでは sendMonitorInfoに統一して、filenameがnullでないなら monitorInfo のクローンを立てる形式としている。
						AgtMonitorInfoResponse sendMonitorInfo =  monitorInfo;
						if (filename != null) {
							sendMonitorInfo =  cloneAgtMonitorInfoResponse(monitorInfo);
							sendMonitorInfo.getLogfileCheckInfo().setLogfile(filename);
						}

						// SDMLで自動生成された監視の場合でここに来るのは必ずログファイル監視だが念のため判定する
						if (HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorInfo.getMonitorTypeId())) {
							LogfileResultForwarder.getInstance().add(line, logmsg, sendMonitorInfo, null, runInstructionInfo);
						} 
					}

					break;
				}
			}
		} else {
			if (!processed)
				m_log.debug("collected no data.");
		}
	}

	private static AgtMessageInfoRequest generateAgtMessageInfoRequest(String line, Date generationDate) {
		AgtMessageInfoRequest logmsg = new AgtMessageInfoRequest();
		logmsg.setMessage(line);

		if(generationDate != null){
			m_log.debug("patternMatch set generation date : " + generationDate);
			logmsg.setGenerationDate(generationDate.getTime());
		}else{
			logmsg.setGenerationDate(HinemosTime.getDateInstance().getTime());
		}
		logmsg.setHostName(Agent.getAgentInfoRequest().getHostname());
		return logmsg;
	}
	
	private static AgtMonitorInfoResponse cloneAgtMonitorInfoResponse (AgtMonitorInfoResponse monitorInfo) {
		AgtMonitorInfoResponse monitorInfo2 = new AgtMonitorInfoResponse();
		monitorInfo2.setApplication(monitorInfo.getApplication());
		monitorInfo2.setCalendar(monitorInfo.getCalendar());
		monitorInfo2.setCalendarId(monitorInfo.getCalendarId());
		monitorInfo2.setCollectorFlg(monitorInfo.getCollectorFlg());
		//[REST対応で除外]monitorInfo2.setCustomCheckInfo(monitorInfo.getCustomCheckInfo());
		//[REST対応で除外]monitorInfo2.setCustomTrapCheckInfo(monitorInfo.getCustomTrapCheckInfo());
		monitorInfo2.setDelayTime(monitorInfo.getDelayTime());
		monitorInfo2.setDescription(monitorInfo.getDescription());
		monitorInfo2.setFacilityId(monitorInfo.getFacilityId());
		monitorInfo2.setFailurePriority(monitorInfo.getFailurePriority());
		//[REST対応で除外]monitorInfo2.setHttpCheckInfo(monitorInfo.getHttpCheckInfo());
		//[REST対応で除外]monitorInfo2.setHttpScenarioCheckInfo(monitorInfo.getHttpScenarioCheckInfo());
		monitorInfo2.setItemName(monitorInfo.getItemName());
		//[REST対応で除外]monitorInfo2.setJmxCheckInfo(monitorInfo.getJmxCheckInfo());
		
		// ログファイル監視、クラウドログ監視、RPAログファイル監視だけがここを通る
		if (HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorInfo.getMonitorTypeId())
				|| HinemosModuleConstant.MONITOR_CLOUD_LOG.equals(monitorInfo.getMonitorTypeId())) {
			// LogfileCheckInfoをコピー
			AgtLogfileCheckInfoResponse logFileCheckInfo = new AgtLogfileCheckInfoResponse();
			logFileCheckInfo.setDirectory(monitorInfo.getLogfileCheckInfo().getDirectory());
			logFileCheckInfo.setFileEncoding(monitorInfo.getLogfileCheckInfo().getFileEncoding());
			logFileCheckInfo.setFileName(monitorInfo.getLogfileCheckInfo().getFileName());
			logFileCheckInfo.setFileReturnCode(monitorInfo.getLogfileCheckInfo().getFileReturnCode());
			logFileCheckInfo.setLogfile(monitorInfo.getLogfileCheckInfo().getLogfile());
			logFileCheckInfo.setMaxBytes(monitorInfo.getLogfileCheckInfo().getMaxBytes());
			logFileCheckInfo.setMonitorId(monitorInfo.getLogfileCheckInfo().getMonitorId());
			logFileCheckInfo.setMonitorTypeId(monitorInfo.getLogfileCheckInfo().getMonitorTypeId());
			logFileCheckInfo.setPatternHead(monitorInfo.getLogfileCheckInfo().getPatternHead());
			logFileCheckInfo.setPatternTail(monitorInfo.getLogfileCheckInfo().getPatternTail());
			logFileCheckInfo.setLogfile(monitorInfo.getLogfileCheckInfo().getLogfile());
			monitorInfo2.setLogfileCheckInfo(logFileCheckInfo);

		} else if (HinemosModuleConstant.MONITOR_RPA_LOGFILE.equals(monitorInfo.getMonitorTypeId())) {
			// RpaLogfileCheckInfoをコピー
			AgtRpaLogFileCheckInfoResponse RpalogFileCheckInfo = new AgtRpaLogFileCheckInfoResponse();
			RpalogFileCheckInfo.setDirectory(monitorInfo.getRpaLogFileCheckInfo().getDirectory());
			RpalogFileCheckInfo.setFileEncoding(monitorInfo.getRpaLogFileCheckInfo().getFileEncoding());
			RpalogFileCheckInfo.setFileName(monitorInfo.getRpaLogFileCheckInfo().getFileName());
			monitorInfo2.setRpaLogFileCheckInfo(RpalogFileCheckInfo);
		}

		monitorInfo2.setLogFormatId(monitorInfo.getLogFormatId());
		monitorInfo2.setMeasure(monitorInfo.getMeasure());
		monitorInfo2.setMonitorFlg(monitorInfo.getMonitorFlg());
		monitorInfo2.setMonitorId(monitorInfo.getMonitorId());
		monitorInfo2.setMonitorType(monitorInfo.getMonitorType());
		monitorInfo2.setMonitorTypeId(monitorInfo.getMonitorTypeId());
		monitorInfo2.setNotifyGroupId(monitorInfo.getNotifyGroupId());
		//[REST対応で除外]monitorInfo2.setOwnerRoleId(monitorInfo.getOwnerRoleId());
		//[REST対応で除外]monitorInfo2.setPerfCheckInfo(monitorInfo.getPerfCheckInfo());
		//[REST対応で除外]monitorInfo2.setPingCheckInfo(monitorInfo.getPingCheckInfo());
		//[REST対応で除外]monitorInfo2.setPluginCheckInfo(monitorInfo.getPluginCheckInfo());
		//[REST対応で除外]monitorInfo2.setPortCheckInfo(monitorInfo.getPortCheckInfo());
		//[REST対応で除外]monitorInfo2.setProcessCheckInfo(monitorInfo.getProcessCheckInfo());
		monitorInfo2.setRegDate(monitorInfo.getRegDate());
		monitorInfo2.setRegUser(monitorInfo.getRegUser());
		monitorInfo2.setRunInterval(monitorInfo.getRunInterval());
		monitorInfo2.setScope(monitorInfo.getScope());
		//[REST対応で除外]monitorInfo2.setSnmpCheckInfo(monitorInfo.getSnmpCheckInfo());
		//[REST対応で除外]monitorInfo2.setSqlCheckInfo(monitorInfo.getSqlCheckInfo());
		//[REST対応で除外]monitorInfo2.setTrapCheckInfo(monitorInfo.getTrapCheckInfo());
		monitorInfo2.setTriggerType(monitorInfo.getTriggerType());
		monitorInfo2.setUpdateDate(monitorInfo.getUpdateDate());
		monitorInfo2.setUpdateUser(monitorInfo.getUpdateUser());
		monitorInfo2.setWinEventCheckInfo(monitorInfo.getWinEventCheckInfo());
		//[REST対応で除外]monitorInfo2.setWinServiceCheckInfo(monitorInfo.getWinServiceCheckInfo());
		monitorInfo2.setPluginCheckInfo(monitorInfo.getPluginCheckInfo());
		
		monitorInfo2.setPriorityChangeJudgmentType(monitorInfo.getPriorityChangeJudgmentType());
		monitorInfo2.setPriorityChangeFailureType(monitorInfo.getPriorityChangeFailureType());
		monitorInfo2.setSdmlMonitorTypeId(monitorInfo.getSdmlMonitorTypeId());

		return monitorInfo2;
	}

}
