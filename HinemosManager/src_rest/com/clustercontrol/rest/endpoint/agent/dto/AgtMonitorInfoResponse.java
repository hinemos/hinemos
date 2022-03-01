/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = MonitorInfo.class)
public class AgtMonitorInfoResponse {

	// ---- from ObjectPrivilegeTargetInfo
	private String ownerRoleId;
	// private boolean uncheckFlg;

	// ---- from MonitorInfo
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
	// private CustomCheckInfo customCheckInfo;
	// private CustomTrapCheckInfo customTrapCheckInfo;
	// private HttpCheckInfo httpCheckInfo;
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
	// private List<MonitorNumericValueInfo> numericValueInfo = new ArrayList<>();
	// private PerfCheckInfo perfCheckInfo;
	// private PingCheckInfo pingCheckInfo;
	// private PortCheckInfo portCheckInfo;
	// private ProcessCheckInfo processCheckInfo;
	// private SnmpCheckInfo snmpCheckInfo;
	// private SqlCheckInfo sqlCheckInfo;
	// private TrapCheckInfo trapCheckInfo;
	// private WinServiceCheckInfo winServiceCheckInfo;
	private AgtWinEventCheckInfoResponse winEventCheckInfo;
	private AgtLogfileCheckInfoResponse logfileCheckInfo;
	private AgtBinaryCheckInfoResponse binaryCheckInfo;
	private AgtPacketCheckInfoResponse packetCheckInfo;
	private AgtPluginCheckInfoResponse pluginCheckInfo;

	// private HttpScenarioCheckInfo httpScenarioCheckInfo;
	// private JmxCheckInfo jmxCheckInfo;
	// private LogcountCheckInfo logcountCheckInfo;
	// private CorrelationCheckInfo correlationCheckInfo;
	// private IntegrationCheckInfo integrationCheckInfo;
	private AgtRpaLogFileCheckInfoResponse rpaLogFileCheckInfo;
	private List<AgtMonitorStringValueInfoResponse> stringValueInfo;
	private List<AgtBinaryPatternInfoResponse> binaryPatternInfo;
	// private List<MonitorTruthValueInfo> truthValueInfo = new ArrayList<>();
	// private LogFormat logformat;
	private String scope;
	private AgtCalendarInfoResponse calendar;
	// private List<NotifyRelationInfo> notifyRelationList;
	// private List<NotifyRelationInfo> predictionNotifyRelationList;
	// private List<NotifyRelationInfo> changeNotifyRelationList;
	private Integer priorityChangeJudgmentType;
	private Integer priorityChangeFailureType;

	public AgtMonitorInfoResponse() {
	}

	// ---- accessors

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
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

	public Integer getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(Integer delayTime) {
		this.delayTime = delayTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getFailurePriority() {
		return failurePriority;
	}

	public void setFailurePriority(Integer failurePriority) {
		this.failurePriority = failurePriority;
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

	public Integer getMonitorType() {
		return monitorType;
	}

	public void setMonitorType(Integer monitorType) {
		this.monitorType = monitorType;
	}

	public String getMonitorTypeId() {
		return monitorTypeId;
	}

	public void setMonitorTypeId(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
	}

	public String getNotifyGroupId() {
		return notifyGroupId;
	}

	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
	}

	public Long getRegDate() {
		return regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public Integer getRunInterval() {
		return runInterval;
	}

	public void setRunInterval(Integer runInterval) {
		this.runInterval = runInterval;
	}

	public String getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(String triggerType) {
		this.triggerType = triggerType;
	}

	public Long getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
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

	public AgtWinEventCheckInfoResponse getWinEventCheckInfo() {
		return winEventCheckInfo;
	}

	public void setWinEventCheckInfo(AgtWinEventCheckInfoResponse winEventCheckInfo) {
		this.winEventCheckInfo = winEventCheckInfo;
	}

	public AgtLogfileCheckInfoResponse getLogfileCheckInfo() {
		return logfileCheckInfo;
	}

	public void setLogfileCheckInfo(AgtLogfileCheckInfoResponse logfileCheckInfo) {
		this.logfileCheckInfo = logfileCheckInfo;
	}

	public AgtBinaryCheckInfoResponse getBinaryCheckInfo() {
		return binaryCheckInfo;
	}

	public void setBinaryCheckInfo(AgtBinaryCheckInfoResponse binaryCheckInfo) {
		this.binaryCheckInfo = binaryCheckInfo;
	}

	public AgtPacketCheckInfoResponse getPacketCheckInfo() {
		return packetCheckInfo;
	}

	public void setPacketCheckInfo(AgtPacketCheckInfoResponse packetCheckInfo) {
		this.packetCheckInfo = packetCheckInfo;
	}

	public AgtRpaLogFileCheckInfoResponse getRpaLogFileCheckInfo() {
		return rpaLogFileCheckInfo;
	}

	public void setRpaLogFileCheckInfo(AgtRpaLogFileCheckInfoResponse rpaLogFileCheckInfo) {
		this.rpaLogFileCheckInfo = rpaLogFileCheckInfo;
	}

	public List<AgtMonitorStringValueInfoResponse> getStringValueInfo() {
		return stringValueInfo;
	}

	public void setStringValueInfo(List<AgtMonitorStringValueInfoResponse> stringValueInfo) {
		this.stringValueInfo = stringValueInfo;
	}

	public List<AgtBinaryPatternInfoResponse> getBinaryPatternInfo() {
		return binaryPatternInfo;
	}

	public void setBinaryPatternInfo(List<AgtBinaryPatternInfoResponse> binaryPatternInfo) {
		this.binaryPatternInfo = binaryPatternInfo;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public AgtCalendarInfoResponse getCalendar() {
		return calendar;
	}

	public void setCalendar(AgtCalendarInfoResponse calendar) {
		this.calendar = calendar;
	}
	
	public AgtPluginCheckInfoResponse getPluginCheckInfo() {
		return pluginCheckInfo;
	}

	public void setPluginCheckInfo(AgtPluginCheckInfoResponse pluginCheckInfo) {
		this.pluginCheckInfo = pluginCheckInfo;
	}

	public Integer getPriorityChangeJudgmentType() {
		return priorityChangeJudgmentType;
	}

	public void setPriorityChangeJudgmentType(Integer priorityChangeJudgmentType) {
		this.priorityChangeJudgmentType = priorityChangeJudgmentType;
	}

	public Integer getPriorityChangeFailureType() {
		return priorityChangeFailureType;
	}

	public void setPriorityChangeFailureType(Integer priorityChangeFailureType) {
		this.priorityChangeFailureType = priorityChangeFailureType;
	}

}
