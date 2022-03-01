/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class LogcountCheckInfoResponse {
	private String targetMonitorId;
	private String keyword;
	private Boolean isAnd;
	private String tag;

	public LogcountCheckInfoResponse() {
	}

	public String getTargetMonitorId() {
		return targetMonitorId;
	}
	public void setTargetMonitorId(String targetMonitorId) {
		this.targetMonitorId = targetMonitorId;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public Boolean getIsAnd() {
		return isAnd;
	}
	public void setIsAnd(Boolean isAnd) {
		this.isAnd = isAnd;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	@Override
	public String toString() {
		return "LogcountCheckInfo [targetMonitorId=" + targetMonitorId + ", keyword=" + keyword + ", isAnd=" + isAnd + ", tag=" + tag
				+ "]";
	}

}