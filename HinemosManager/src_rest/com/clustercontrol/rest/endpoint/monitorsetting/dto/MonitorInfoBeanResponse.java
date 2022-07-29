/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.MonitorTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PredictionMethodEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.RunIntervalEnum;

public class MonitorInfoBeanResponse {

	private String monitorId;
	private String application;
	private Boolean collectorFlg;
	private String description;
	private String itemName;
	private String measure;
	private Boolean monitorFlg;
	@RestBeanConvertEnum
	private MonitorTypeEnum monitorType;
	private String monitorTypeId;
	@RestBeanConvertEnum
	private RunIntervalEnum runInterval;
	private String calendarId;
	private String logFormatId;
	private String facilityId;
	private Boolean predictionFlg;
	@RestBeanConvertEnum
	private PredictionMethodEnum predictionMethod;
	private Integer predictionAnalysysRange;
	private Integer predictionTarget;
	private String predictionApplication;
	private Boolean changeFlg;
	private Integer changeAnalysysRange;
	private String changeApplication;
	private String sdmlMonitorTypeId;
	@RestPartiallyTransrateTarget
	private String scope;
	private String ownerRoleId;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String regDate;
	private String regUser;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String updateDate;
	private String updateUser;

	public MonitorInfoBeanResponse() {
	}

	public String getMonitorId() {
		return monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public Boolean getCollectorFlg() {
		return collectorFlg;
	}
	public void setCollectorFlg(Boolean collectorFlg) {
		this.collectorFlg = collectorFlg;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getMeasure() {
		return measure;
	}
	public void setMeasure(String measure) {
		this.measure = measure;
	}
	public Boolean getMonitorFlg() {
		return monitorFlg;
	}
	public void setMonitorFlg(Boolean monitorFlg) {
		this.monitorFlg = monitorFlg;
	}
	public MonitorTypeEnum getMonitorType() {
		return monitorType;
	}
	public void setMonitorType(MonitorTypeEnum monitorType) {
		this.monitorType = monitorType;
	}
	public String getMonitorTypeId() {
		return monitorTypeId;
	}
	public void setMonitorTypeId(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
	}
	public RunIntervalEnum getRunInterval() {
		return runInterval;
	}
	public void setRunInterval(RunIntervalEnum runInterval) {
		this.runInterval = runInterval;
	}
	public String getCalendarId() {
		return calendarId;
	}
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}
	public String getLogFormatId() {
		return logFormatId;
	}
	public void setLogFormatId(String logFormatId) {
		this.logFormatId = logFormatId;
	}
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	public Boolean getPredictionFlg() {
		return predictionFlg;
	}
	public void setPredictionFlg(Boolean predictionFlg) {
		this.predictionFlg = predictionFlg;
	}
	public PredictionMethodEnum getPredictionMethod() {
		return predictionMethod;
	}
	public void setPredictionMethod(PredictionMethodEnum predictionMethod) {
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
		return predictionApplication;
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
		return changeApplication;
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
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
	public String getRegDate() {
		return regDate;
	}
	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}
	public String getRegUser() {
		return regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
	public String getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}
	public String getUpdateUser() {
		return updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
	@Override
	public String toString() {
		return "MonitorInfoBeanResponse [monitorId=" + monitorId + ", application=" + application + ", collectorFlg="
				+ collectorFlg + ", description=" + description + ", itemName=" + itemName + ", measure=" + measure
				+ ", monitorFlg=" + monitorFlg + ", monitorType=" + monitorType + ", monitorTypeId=" + monitorTypeId
				+ ", runInterval=" + runInterval + ", calendarId=" + calendarId + ", logFormatId=" + logFormatId
				+ ", facilityId=" + facilityId + ", predictionFlg=" + predictionFlg + ", predictionMethod="
				+ predictionMethod + ", predictionAnalysysRange=" + predictionAnalysysRange + ", predictionTarget="
				+ predictionTarget + ", predictionApplication=" + predictionApplication + ", changeFlg=" + changeFlg
				+ ", changeAnalysysRange=" + changeAnalysysRange + ", changeApplication=" + changeApplication
				+ ", scope=" + scope + ", ownerRoleId=" + ownerRoleId + ", regDate=" + regDate + ", regUser=" + regUser
				+ ", updateDate=" + updateDate + ", updateUser=" + updateUser + "]";
	}

}
