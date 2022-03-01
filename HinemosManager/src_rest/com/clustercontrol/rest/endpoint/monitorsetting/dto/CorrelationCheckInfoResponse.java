/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class CorrelationCheckInfoResponse {
	private String targetMonitorId;
	private String targetItemName;
	private String targetDisplayName;
	private String referMonitorId;
	private String referItemName;
	private String referDisplayName;
	private String referFacilityId;
	private Integer analysysRange;

	public CorrelationCheckInfoResponse() {
	}

	public String getTargetMonitorId() {
		return targetMonitorId;
	}
	public void setTargetMonitorId(String targetMonitorId) {
		this.targetMonitorId = targetMonitorId;
	}
	public String getTargetItemName() {
		return targetItemName;
	}
	public void setTargetItemName(String targetItemName) {
		this.targetItemName = targetItemName;
	}
	public String getTargetDisplayName() {
		return targetDisplayName;
	}
	public void setTargetDisplayName(String targetDisplayName) {
		this.targetDisplayName = targetDisplayName;
	}
	public String getReferMonitorId() {
		return referMonitorId;
	}
	public void setReferMonitorId(String referMonitorId) {
		this.referMonitorId = referMonitorId;
	}
	public String getReferItemName() {
		return referItemName;
	}
	public void setReferItemName(String referItemName) {
		this.referItemName = referItemName;
	}
	public String getReferDisplayName() {
		return referDisplayName;
	}
	public void setReferDisplayName(String referDisplayName) {
		this.referDisplayName = referDisplayName;
	}
	public String getReferFacilityId() {
		return referFacilityId;
	}
	public void setReferFacilityId(String referFacilityId) {
		this.referFacilityId = referFacilityId;
	}
	public Integer getAnalysysRange() {
		return analysysRange;
	}
	public void setAnalysysRange(Integer analysysRange) {
		this.analysysRange = analysysRange;
	}
	@Override
	public String toString() {
		return "CorrelationCheckInfo [targetMonitorId=" + targetMonitorId + ", targetItemName=" + targetItemName + ", targetDisplayName="
				+ targetDisplayName + ", referMonitorId=" + referMonitorId + ", referItemName=" + referItemName
				+ ", referDisplayName=" + referDisplayName + ", referFacilityId=" + referFacilityId + ", analysysRange="
				+ analysysRange + "]";
	}

}