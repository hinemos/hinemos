/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.winevent.model.WinEventCheckInfo;

@RestBeanConvertAssertion(from = WinEventCheckInfo.class)
public class AgtWinEventCheckInfoResponse {

	// ---- from WinEventCheckInfo 

	private String monitorId;
	private Boolean levelCritical;
	private Boolean levelWarning;
	private Boolean levelVerbose;
	private Boolean levelError;
	private Boolean levelInformational;

	private List<String> logName;
	private List<String> source;
	private List<Integer> eventId;
	private List<Integer> category;
	private List<Long> keywords;

	// private List<MonitorWinEventLogInfoEntity> monitorWinEventLogInfoEntities; // 必要な情報はlogNameListへ変換されるので不要
	// private List<MonitorWinEventSourceInfoEntity> monitorWinEventSourceInfoEntities; // 必要な情報はsourceListへ変換されるので不要
	// private List<MonitorWinEventIdInfoEntity> monitorWinEventIdInfoEntities; // 必要な情報はeventIdListへ変換されるので不要
	// private List<MonitorWinEventCategoryInfoEntity> monitorWinEventCategoryInfoEntities; // 必要な情報はcategoryListへ変換されるので不要
	// private List<MonitorWinEventKeywordInfoEntity> monitorWinEventKeywordInfoEntities; // 必要な情報はkeywordListへ変換されるので不要

	// private MonitorInfo monitorInfo; // 循環参照させない

	// ---- from MonitorCheckInfo

	// private String m_monitorId; // WinEventCheckInfo#monitorId がオーバーライドしているので不要
	private String monitorTypeId;

	public AgtWinEventCheckInfoResponse() {
	}

	// ---- accessors

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public Boolean getLevelCritical() {
		return levelCritical;
	}

	public void setLevelCritical(Boolean levelCritical) {
		this.levelCritical = levelCritical;
	}

	public Boolean getLevelWarning() {
		return levelWarning;
	}

	public void setLevelWarning(Boolean levelWarning) {
		this.levelWarning = levelWarning;
	}

	public Boolean getLevelVerbose() {
		return levelVerbose;
	}

	public void setLevelVerbose(Boolean levelVerbose) {
		this.levelVerbose = levelVerbose;
	}

	public Boolean getLevelError() {
		return levelError;
	}

	public void setLevelError(Boolean levelError) {
		this.levelError = levelError;
	}

	public Boolean getLevelInformational() {
		return levelInformational;
	}

	public void setLevelInformational(Boolean levelInformational) {
		this.levelInformational = levelInformational;
	}

	public List<String> getLogName() {
		return logName;
	}

	public void setLogName(List<String> logName) {
		this.logName = logName;
	}

	public List<String> getSource() {
		return source;
	}

	public void setSource(List<String> source) {
		this.source = source;
	}

	public List<Integer> getEventId() {
		return eventId;
	}

	public void setEventId(List<Integer> eventId) {
		this.eventId = eventId;
	}

	public List<Integer> getCategory() {
		return category;
	}

	public void setCategory(List<Integer> category) {
		this.category = category;
	}

	public List<Long> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<Long> keywords) {
		this.keywords = keywords;
	}

	public String getMonitorTypeId() {
		return monitorTypeId;
	}

	public void setMonitorTypeId(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
	}

}
