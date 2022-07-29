/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * 監視設定情報を保持するDTO
 * チェック設定、監視種別ごとの情報を含まない監視設定情報。
 * 監視設定一覧等、Hinemosクライアントに最小限の監視設定情報を引き渡す。
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class MonitorInfoBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private String monitorId;
	private String application;
	private Boolean collectorFlg;
	private Integer delayTime;
	private String description;
	private Integer failurePriority;
	private String itemName;
	private String measure;
	private Boolean monitorFlg;
	private Integer monitorType;
	private String monitorTypeId;
	private String notifyGroupId;
	private Long regDate;
	private String regUser;
	private Integer runInterval;
	private String triggerType;
	private Long updateDate;
	private String updateUser;
	private String calendarId;
	private String logFormatId;
	private String facilityId;
	private Boolean predictionFlg;
	private String predictionMethod;
	private Integer predictionAnalysysRange;
	private Integer predictionTarget;
	private String predictionApplication;
	private Boolean changeFlg;
	private Integer changeAnalysysRange;
	private String changeApplication;
	private String sdmlMonitorTypeId;
	private String scope;
	private String ownerRoleId;

	public MonitorInfoBean() {
		super();
	}

	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getApplication() {
		return this.application;
	}
	public void setApplication(String application) {
		this.application = application;
	}

	public Boolean getCollectorFlg() {
		return this.collectorFlg;
	}
	public void setCollectorFlg(Boolean collectorFlg) {
		this.collectorFlg = collectorFlg;
	}

	public Integer getDelayTime() {
		return this.delayTime;
	}
	public void setDelayTime(Integer delayTime) {
		this.delayTime = delayTime;
	}

	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getFailurePriority() {
		return this.failurePriority;
	}
	public void setFailurePriority(Integer failurePriority) {
		this.failurePriority = failurePriority;
	}

	public String getItemName() {
		return this.itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getMeasure() {
		return this.measure;
	}
	public void setMeasure(String measure) {
		this.measure = measure;
	}

	public Boolean getMonitorFlg() {
		return this.monitorFlg;
	}
	public void setMonitorFlg(Boolean monitorFlg) {
		this.monitorFlg = monitorFlg;
	}

	public Integer getMonitorType() {
		return this.monitorType;
	}
	public void setMonitorType(Integer monitorType) {
		this.monitorType = monitorType;
	}

	public String getMonitorTypeId() {
		return this.monitorTypeId;
	}
	public void setMonitorTypeId(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
	}

	public String getNotifyGroupId() {
		return this.notifyGroupId;
	}
	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
	}

	public Long getRegDate() {
		return this.regDate;
	}
	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	public String getRegUser() {
		return this.regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public Integer getRunInterval() {
		return this.runInterval;
	}
	public void setRunInterval(Integer runInterval) {
		this.runInterval = runInterval;
	}

	public String getTriggerType() {
		return this.triggerType;
	}
	public void setTriggerType(String triggerType) {
		this.triggerType = triggerType;
	}

	public Long getUpdateDate() {
		return this.updateDate;
	}
	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	public String getUpdateUser() {
		return this.updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getCalendarId() {
		return this.calendarId;
	}
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getLogFormatId() {
		return logFormatId;
	}
	public void setLogFormatId(String logFormatId) {
		this.logFormatId = logFormatId;
	}

	public Boolean getPredictionFlg() {
		return predictionFlg;
	}
	public void setPredictionFlg(Boolean predictionFlg) {
		this.predictionFlg = predictionFlg;
	}

	public String getPredictionMethod() {
		return predictionMethod;
	}
	public void setPredictionMethod(String predictionMethod) {
		this.predictionMethod = predictionMethod;
	}

	public Integer getPredictionAnalysysRange() {
		return predictionAnalysysRange;
	}
	public void setPredictionAnalysysRange(Integer predictionAnalysysRange) {
		this.predictionAnalysysRange = predictionAnalysysRange;
	}

	public Integer getPredictionTarget() {
		return predictionTarget;
	}
	public void setPredictionTarget(Integer predictionTarget) {
		this.predictionTarget = predictionTarget;
	}

	public String getPredictionApplication() {
		return this.predictionApplication;
	}
	public void setPredictionApplication(String predictionApplication) {
		this.predictionApplication = predictionApplication;
	}

	public Boolean getChangeFlg() {
		return changeFlg;
	}
	public void setChangeFlg(Boolean changeFlg) {
		this.changeFlg = changeFlg;
	}

	public Integer getChangeAnalysysRange() {
		return changeAnalysysRange;
	}
	public void setChangeAnalysysRange(Integer changeAnalysysRange) {
		this.changeAnalysysRange = changeAnalysysRange;
	}

	public String getChangeApplication() {
		return this.changeApplication;
	}
	public void setChangeApplication(String changeApplication) {
		this.changeApplication = changeApplication;
	}

	public String getSdmlMonitorTypeId() {
		return sdmlMonitorTypeId;
	}
	public void setSdmlMonitorTypeId(String sdmlMonitorTypeId) {
		this.sdmlMonitorTypeId = sdmlMonitorTypeId;
	}

	public String getScope() {
		return this.scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getOwnerRoleId() {
		return this.ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
}