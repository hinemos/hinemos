/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import java.util.ArrayList;
import java.util.List;

public class WinEventCheckInfoResponse {
	private boolean levelCritical;
	private boolean levelWarning;
	private boolean levelVerbose;
	private boolean levelError;
	private boolean levelInformational;
	private List<String> logName = new ArrayList<>();
	private List<String> source = new ArrayList<>();
	private List<Integer> eventId = new ArrayList<>();
	private List<Integer> category = new ArrayList<>();
	private List<Long> keywords = new ArrayList<>();

	public WinEventCheckInfoResponse() {
	}

	public boolean isLevelCritical() {
		return levelCritical;
	}
	public void setLevelCritical(boolean levelCritical) {
		this.levelCritical = levelCritical;
	}
	public boolean isLevelWarning() {
		return levelWarning;
	}
	public void setLevelWarning(boolean levelWarning) {
		this.levelWarning = levelWarning;
	}
	public boolean isLevelVerbose() {
		return levelVerbose;
	}
	public void setLevelVerbose(boolean levelVerbose) {
		this.levelVerbose = levelVerbose;
	}
	public boolean isLevelError() {
		return levelError;
	}
	public void setLevelError(boolean levelError) {
		this.levelError = levelError;
	}
	public boolean isLevelInformational() {
		return levelInformational;
	}
	public void setLevelInformational(boolean levelInformational) {
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
	@Override
	public String toString() {
		return "WinEventCheckInfoResponse [levelCritical=" + levelCritical + ", levelWarning=" + levelWarning
				+ ", levelVerbose=" + levelVerbose + ", levelError=" + levelError + ", levelInformational="
				+ levelInformational + ", logName=" + logName + ", source=" + source + ", eventId=" + eventId
				+ ", category=" + category + ", keywords=" + keywords + "]";
	}

}