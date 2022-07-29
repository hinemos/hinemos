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

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class WinEventCheckInfoRequest implements RequestDto {
	private Boolean levelCritical;
	private Boolean levelWarning;
	private Boolean levelVerbose;
	private Boolean levelError;
	private Boolean levelInformational;
	private List<String> logName = new ArrayList<>();
	private List<String> source = new ArrayList<>();
	private List<Integer> eventId = new ArrayList<>();
	private List<Integer> category = new ArrayList<>();
	private List<Long> keywords = new ArrayList<>();
	public WinEventCheckInfoRequest() {
	}

	
	public Boolean isLevelCritical() {
		return levelCritical;
	}


	public void setLevelCritical(Boolean levelCritical) {
		this.levelCritical = levelCritical;
	}


	public Boolean isLevelWarning() {
		return levelWarning;
	}


	public void setLevelWarning(Boolean levelWarning) {
		this.levelWarning = levelWarning;
	}


	public Boolean isLevelVerbose() {
		return levelVerbose;
	}


	public void setLevelVerbose(Boolean levelVerbose) {
		this.levelVerbose = levelVerbose;
	}


	public Boolean isLevelError() {
		return levelError;
	}


	public void setLevelError(Boolean levelError) {
		this.levelError = levelError;
	}


	public Boolean isLevelInformational() {
		return levelInformational;
	}


	public void setLevelInformational(Boolean levelInformational) {
		this.levelInformational = levelInformational;
	}

	@Override
	public String toString() {
		return "WinEventCheckInfoRequest [levelCritical=" + levelCritical + ", levelWarning=" + levelWarning
				+ ", levelVerbose=" + levelVerbose + ", levelError=" + levelError + ", levelInformational="
				+ levelInformational + ", logName=" + logName + ", source=" + source + ", eventId=" + eventId
				+ ", category=" + category + ", keywords=" + keywords + "]";
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
	public void correlationCheck() throws InvalidSetting {
	}

}