/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import com.clustercontrol.xcloud.bean.AutoAssignNodePatternEntryType;

public class AutoAssignNodePatternEntryInfoResponse {
	private String cloudScopeId;
	private String scopeId;
	private Integer priority;
	private AutoAssignNodePatternEntryType patternType;
	private String pattern;
	
	public AutoAssignNodePatternEntryInfoResponse() {
	}

	public String getCloudScopeId() {
		return cloudScopeId;
	}
	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}
	public String getScopeId() {
		return scopeId;
	}
	public void setScopeId(String scopeId) {
		this.scopeId = scopeId;
	}
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	public AutoAssignNodePatternEntryType getPatternType() {
		return patternType;
	}
	public void setPatternType(AutoAssignNodePatternEntryType patternType) {
		this.patternType = patternType;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
}
