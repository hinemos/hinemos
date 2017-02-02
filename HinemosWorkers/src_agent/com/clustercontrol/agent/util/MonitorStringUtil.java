/*

 Copyright (C) 2013 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.agent.util;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.log.LogfileResultForwarder;
import com.clustercontrol.agent.winevent.WinEventResultForwarder;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.ws.agenthub.MessageInfo;
import com.clustercontrol.ws.jobmanagement.RunInstructionInfo;
import com.clustercontrol.ws.monitor.LogfileCheckInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorStringValueInfo;

public class MonitorStringUtil {

	// ロガー
	private static Log m_log = LogFactory.getLog(MonitorStringUtil.class);

	/**
	 * 監視文字列をパターンマッチし、マネージャに送信する
	 * @param line 監視文字列
	 * @param monitorInfo 監視設定
	 */
	public static void patternMatch(String line, MonitorInfo monitorInfo, RunInstructionInfo runInstructionInfo, String filename) {
		patternMatch(line, monitorInfo, runInstructionInfo, null, filename);
	}

	/**
	 * 監視文字列をパターンマッチし、マネージャに送信する
	 * @param line 監視文字列
	 * @param generationDate 生成日時
	 * @param monitorInfo 監視設定
	 */
	public static void patternMatch(String line, MonitorInfo monitorInfo, RunInstructionInfo runInstructionInfo, Date generationDate, String filename) {
		if (runInstructionInfo == null && monitorInfo.getCalendar() != null &&
				! CalendarWSUtil.isRun(monitorInfo.getCalendar())) {
			m_log.debug("patternMatch is skipped because of calendar");
			return;
		}
		
		boolean processed = false;
		if (runInstructionInfo != null || monitorInfo.isMonitorFlg()) {
			int order_no = 0;
			for(MonitorStringValueInfo stringInfo : monitorInfo.getStringValueInfo()) {
				++order_no;
				if(m_log.isDebugEnabled()){
					m_log.debug("patternMatch() line = " + line
							+ ", monitorId = " + stringInfo.getMonitorId()
							+ ", orderNo = " + order_no
							+ ", pattern = " + stringInfo.getPattern());
				}
				if (!stringInfo.isValidFlg()) {
					continue;
				}
				String patternText = stringInfo.getPattern();
				String message = line;
				m_log.trace("patternMatch check " + message);
				
				Pattern pattern = null;
				// 大文字・小文字を区別しない場合
				if(stringInfo.isCaseSensitivityFlg()){
					pattern = Pattern.compile(patternText, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
				}
				// 大文字・小文字を区別する場合
				else{
					pattern = Pattern.compile(patternText, Pattern.DOTALL);
				}
				Matcher matcher = pattern.matcher(line);
				
				if (matcher.matches()) {
					m_log.debug("patternMatch match " + message);

					// 「処理する」
					if (stringInfo.isProcessType()) {
						MessageInfo logmsg = new MessageInfo();
						logmsg.setMessage(line);

						if(generationDate != null){
							m_log.debug("patternMatch set generation date : " + generationDate);
							logmsg.setGenerationDate(generationDate.getTime());
						}else{
							logmsg.setGenerationDate(HinemosTime.getDateInstance().getTime());
						}
						logmsg.setHostName(Agent.getAgentInfo().getHostname());
						
						MonitorInfo monitorInfo2 = new MonitorInfo();
						if (filename != null) {
							monitorInfo2.setApplication(monitorInfo.getApplication());
							monitorInfo2.setCalendar(monitorInfo.getCalendar());
							monitorInfo2.setCalendarId(monitorInfo.getCalendarId());
							monitorInfo2.setCollectorFlg(monitorInfo.isCollectorFlg());
							monitorInfo2.setCustomCheckInfo(monitorInfo.getCustomCheckInfo());
							monitorInfo2.setCustomTrapCheckInfo(monitorInfo.getCustomTrapCheckInfo());
							monitorInfo2.setDelayTime(monitorInfo.getDelayTime());
							monitorInfo2.setDescription(monitorInfo.getDescription());
							monitorInfo2.setFacilityId(monitorInfo.getFacilityId());
							monitorInfo2.setFailurePriority(monitorInfo.getFailurePriority());
							monitorInfo2.setHttpCheckInfo(monitorInfo.getHttpCheckInfo());
							monitorInfo2.setHttpScenarioCheckInfo(monitorInfo.getHttpScenarioCheckInfo());
							monitorInfo2.setItemName(monitorInfo.getItemName());
							monitorInfo2.setJmxCheckInfo(monitorInfo.getJmxCheckInfo());
							
							// ログファイル監視だけがここを通るのでLogfileCheckInfoだけをコピーしておく
							LogfileCheckInfo logFileCheckInfo = new LogfileCheckInfo();
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
							
							monitorInfo2.setLogfileCheckInfo(logFileCheckInfo);// 
							monitorInfo2.setLogFormatId(monitorInfo.getLogFormatId());
							monitorInfo2.setMeasure(monitorInfo.getMeasure());
							monitorInfo2.setMonitorFlg(monitorInfo.isMonitorFlg());
							monitorInfo2.setMonitorId(monitorInfo.getMonitorId());
							monitorInfo2.setMonitorType(monitorInfo.getMonitorType());
							monitorInfo2.setMonitorTypeId(monitorInfo.getMonitorTypeId());
							monitorInfo2.setNotifyGroupId(monitorInfo.getNotifyGroupId());
							monitorInfo2.setOwnerRoleId(monitorInfo.getOwnerRoleId());
							monitorInfo2.setPerfCheckInfo(monitorInfo.getPerfCheckInfo());
							monitorInfo2.setPingCheckInfo(monitorInfo.getPingCheckInfo());
							monitorInfo2.setPluginCheckInfo(monitorInfo.getPluginCheckInfo());
							monitorInfo2.setPortCheckInfo(monitorInfo.getPortCheckInfo());
							monitorInfo2.setProcessCheckInfo(monitorInfo.getProcessCheckInfo());
							monitorInfo2.setRegDate(monitorInfo.getRegDate());
							monitorInfo2.setRegUser(monitorInfo.getRegUser());
							monitorInfo2.setRunInterval(monitorInfo.getRunInterval());
							monitorInfo2.setScope(monitorInfo.getScope());
							monitorInfo2.setSnmpCheckInfo(monitorInfo.getSnmpCheckInfo());
							monitorInfo2.setSqlCheckInfo(monitorInfo.getSqlCheckInfo());
							monitorInfo2.setTrapCheckInfo(monitorInfo.getTrapCheckInfo());
							monitorInfo2.setTriggerType(monitorInfo.getTriggerType());
							monitorInfo2.setUpdateDate(monitorInfo.getUpdateDate());
							monitorInfo2.setUpdateUser(monitorInfo.getUpdateUser());
							monitorInfo2.setWinEventCheckInfo(monitorInfo.getWinEventCheckInfo());
							monitorInfo2.setWinServiceCheckInfo(monitorInfo.getWinServiceCheckInfo());
							
							monitorInfo2.getLogfileCheckInfo().setLogfile(filename);
						}
						
						if (HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorInfo.getMonitorTypeId())) {
							if (filename != null) {
								LogfileResultForwarder.getInstance().add(message, logmsg, monitorInfo2, stringInfo, runInstructionInfo);
							} else {
								LogfileResultForwarder.getInstance().add(message, logmsg, monitorInfo, stringInfo, runInstructionInfo);
							}
						} else if (HinemosModuleConstant.MONITOR_WINEVENT.equals(monitorInfo.getMonitorTypeId())) {
							WinEventResultForwarder.getInstance().add(message, logmsg, monitorInfo, stringInfo, runInstructionInfo);
						}
						
						processed = true;
						
						m_log.debug("patternMatch send message : " + message);
						m_log.debug("patternMatch send logmsg message : " + logmsg.getMessage());
						m_log.debug("patternMatch send logmsg generation date : " + new Date(logmsg.getGenerationDate()));
						m_log.debug("patternMatch send logmsg hostname : " + logmsg.getHostName());
					}
					break;
				}
			}
		} else {
			m_log.debug("patternMatch is skipped because of monitor flg");
		}
		
		if (!processed && monitorInfo.isCollectorFlg()) {
			MessageInfo logmsg = new MessageInfo();
			logmsg.setMessage(line);
			if(generationDate != null){
				m_log.debug("patternMatch set generation date : " + generationDate);
				logmsg.setGenerationDate(generationDate.getTime());
			}else{
				logmsg.setGenerationDate(HinemosTime.getDateInstance().getTime());
			}
			logmsg.setHostName(Agent.getAgentInfo().getHostname());
			
			if (filename != null) {
				monitorInfo.getLogfileCheckInfo().setLogfile(filename);
			}
			
			if (HinemosModuleConstant.MONITOR_LOGFILE.equals(monitorInfo.getMonitorTypeId())) {
				LogfileResultForwarder.getInstance().add(line, logmsg, monitorInfo, null, runInstructionInfo);
			} else if (HinemosModuleConstant.MONITOR_WINEVENT.equals(monitorInfo.getMonitorTypeId())) {
				WinEventResultForwarder.getInstance().add(line, logmsg, monitorInfo, null, runInstructionInfo);
			}
		} else {
			if (!processed)
				m_log.debug("collected no data.");
		}
	}

}
