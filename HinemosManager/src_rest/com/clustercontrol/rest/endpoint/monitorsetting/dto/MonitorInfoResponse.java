/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoResponse;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityChangeFailureTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityChangeJudgmentTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PredictionMethodEnum;

public class MonitorInfoResponse extends AbstractMonitorResponse {

	private Boolean collectorFlg;
	private String description;
	private String itemName;
	private String measure;
	private CustomCheckInfoResponse customCheckInfo;
	private CustomTrapCheckInfoResponse customTrapCheckInfo;
	private HttpCheckInfoResponse httpCheckInfo;
	private Boolean predictionFlg;
	@RestBeanConvertEnum
	private PredictionMethodEnum predictionMethod;
	private Integer predictionAnalysysRange;
	private Integer predictionTarget;
	private String predictionApplication;
	private Boolean changeFlg;
	private Integer changeAnalysysRange;
	private String changeApplication;
	private List<MonitorNumericValueInfoResponse> numericValueInfo = new ArrayList<>();
	private PerfCheckInfoResponse perfCheckInfo;
	private PingCheckInfoResponse pingCheckInfo;
	private PortCheckInfoResponse portCheckInfo;
	private ProcessCheckInfoResponse processCheckInfo;
	private SnmpCheckInfoResponse snmpCheckInfo;
	private SqlCheckInfoResponse sqlCheckInfo;
	private TrapCheckInfoResponse trapCheckInfo;
	private WinServiceCheckInfoResponse winServiceCheckInfo;
	private WinEventCheckInfoResponse winEventCheckInfo;
	private LogfileCheckInfoResponse logfileCheckInfo;
	private BinaryCheckInfoResponse binaryCheckInfo;
	private PacketCheckInfoResponse packetCheckInfo;
	private PluginCheckInfoResponse pluginCheckInfo;
	private HttpScenarioCheckInfoResponse httpScenarioCheckInfo;
	private JmxCheckInfoResponse jmxCheckInfo;
	private LogcountCheckInfoResponse logcountCheckInfo;
	private CorrelationCheckInfoResponse correlationCheckInfo;
	private IntegrationCheckInfoResponse integrationCheckInfo;
	private RpaLogFileCheckInfoResponse rpaLogFileCheckInfo;
	private RpaManagementToolServiceCheckInfoResponse rpaManagementToolServiceCheckInfo;
	private List<MonitorStringValueInfoResponse> stringValueInfo = new ArrayList<>();
	private List<BinaryPatternInfoResponse> binaryPatternInfo = new ArrayList<>();
	private List<MonitorTruthValueInfoResponse> truthValueInfo = new ArrayList<>();
	private String logFormatId;
	private List<NotifyRelationInfoResponse> predictionNotifyRelationList = new ArrayList<>();
	private List<NotifyRelationInfoResponse> changeNotifyRelationList = new ArrayList<>();
	@RestBeanConvertEnum
	private PriorityChangeJudgmentTypeEnum priorityChangeJudgmentType;
	@RestBeanConvertEnum
	private PriorityChangeFailureTypeEnum priorityChangeFailureType;

	public MonitorInfoResponse() {
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


	public CustomCheckInfoResponse getCustomCheckInfo() {
		return customCheckInfo;
	}


	public void setCustomCheckInfo(CustomCheckInfoResponse customCheckInfo) {
		this.customCheckInfo = customCheckInfo;
	}


	public CustomTrapCheckInfoResponse getCustomTrapCheckInfo() {
		return customTrapCheckInfo;
	}


	public void setCustomTrapCheckInfo(CustomTrapCheckInfoResponse customTrapCheckInfo) {
		this.customTrapCheckInfo = customTrapCheckInfo;
	}


	public HttpCheckInfoResponse getHttpCheckInfo() {
		return httpCheckInfo;
	}


	public void setHttpCheckInfo(HttpCheckInfoResponse httpCheckInfo) {
		this.httpCheckInfo = httpCheckInfo;
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


	public List<MonitorNumericValueInfoResponse> getNumericValueInfo() {
		return numericValueInfo;
	}


	public void setNumericValueInfo(List<MonitorNumericValueInfoResponse> numericValueInfo) {
		this.numericValueInfo = numericValueInfo;
	}


	public PerfCheckInfoResponse getPerfCheckInfo() {
		return perfCheckInfo;
	}


	public void setPerfCheckInfo(PerfCheckInfoResponse perfCheckInfo) {
		this.perfCheckInfo = perfCheckInfo;
	}


	public PingCheckInfoResponse getPingCheckInfo() {
		return pingCheckInfo;
	}


	public void setPingCheckInfo(PingCheckInfoResponse pingCheckInfo) {
		this.pingCheckInfo = pingCheckInfo;
	}


	public PortCheckInfoResponse getPortCheckInfo() {
		return portCheckInfo;
	}


	public void setPortCheckInfo(PortCheckInfoResponse portCheckInfo) {
		this.portCheckInfo = portCheckInfo;
	}


	public ProcessCheckInfoResponse getProcessCheckInfo() {
		return processCheckInfo;
	}


	public void setProcessCheckInfo(ProcessCheckInfoResponse processCheckInfo) {
		this.processCheckInfo = processCheckInfo;
	}


	public SnmpCheckInfoResponse getSnmpCheckInfo() {
		return snmpCheckInfo;
	}


	public void setSnmpCheckInfo(SnmpCheckInfoResponse snmpCheckInfo) {
		this.snmpCheckInfo = snmpCheckInfo;
	}


	public SqlCheckInfoResponse getSqlCheckInfo() {
		return sqlCheckInfo;
	}


	public void setSqlCheckInfo(SqlCheckInfoResponse sqlCheckInfo) {
		this.sqlCheckInfo = sqlCheckInfo;
	}


	public TrapCheckInfoResponse getTrapCheckInfo() {
		return trapCheckInfo;
	}


	public void setTrapCheckInfo(TrapCheckInfoResponse trapCheckInfo) {
		this.trapCheckInfo = trapCheckInfo;
	}


	public WinServiceCheckInfoResponse getWinServiceCheckInfo() {
		return winServiceCheckInfo;
	}


	public void setWinServiceCheckInfo(WinServiceCheckInfoResponse winServiceCheckInfo) {
		this.winServiceCheckInfo = winServiceCheckInfo;
	}


	public WinEventCheckInfoResponse getWinEventCheckInfo() {
		return winEventCheckInfo;
	}


	public void setWinEventCheckInfo(WinEventCheckInfoResponse winEventCheckInfo) {
		this.winEventCheckInfo = winEventCheckInfo;
	}


	public LogfileCheckInfoResponse getLogfileCheckInfo() {
		return logfileCheckInfo;
	}


	public void setLogfileCheckInfo(LogfileCheckInfoResponse logfileCheckInfo) {
		this.logfileCheckInfo = logfileCheckInfo;
	}


	public BinaryCheckInfoResponse getBinaryCheckInfo() {
		return binaryCheckInfo;
	}


	public void setBinaryCheckInfo(BinaryCheckInfoResponse binaryCheckInfo) {
		this.binaryCheckInfo = binaryCheckInfo;
	}


	public PacketCheckInfoResponse getPacketCheckInfo() {
		return packetCheckInfo;
	}


	public void setPacketCheckInfo(PacketCheckInfoResponse packetCheckInfo) {
		this.packetCheckInfo = packetCheckInfo;
	}


	public PluginCheckInfoResponse getPluginCheckInfo() {
		return pluginCheckInfo;
	}


	public void setPluginCheckInfo(PluginCheckInfoResponse pluginCheckInfo) {
		this.pluginCheckInfo = pluginCheckInfo;
	}


	public HttpScenarioCheckInfoResponse getHttpScenarioCheckInfo() {
		return httpScenarioCheckInfo;
	}


	public void setHttpScenarioCheckInfo(HttpScenarioCheckInfoResponse httpScenarioCheckInfo) {
		this.httpScenarioCheckInfo = httpScenarioCheckInfo;
	}


	public JmxCheckInfoResponse getJmxCheckInfo() {
		return jmxCheckInfo;
	}


	public void setJmxCheckInfo(JmxCheckInfoResponse jmxCheckInfo) {
		this.jmxCheckInfo = jmxCheckInfo;
	}


	public LogcountCheckInfoResponse getLogcountCheckInfo() {
		return logcountCheckInfo;
	}


	public void setLogcountCheckInfo(LogcountCheckInfoResponse logcountCheckInfo) {
		this.logcountCheckInfo = logcountCheckInfo;
	}


	public CorrelationCheckInfoResponse getCorrelationCheckInfo() {
		return correlationCheckInfo;
	}


	public void setCorrelationCheckInfo(CorrelationCheckInfoResponse correlationCheckInfo) {
		this.correlationCheckInfo = correlationCheckInfo;
	}


	public IntegrationCheckInfoResponse getIntegrationCheckInfo() {
		return integrationCheckInfo;
	}


	public void setIntegrationCheckInfo(IntegrationCheckInfoResponse integrationCheckInfo) {
		this.integrationCheckInfo = integrationCheckInfo;
	}

	public RpaLogFileCheckInfoResponse getRpaLogFileCheckInfo() {
		return rpaLogFileCheckInfo;
	}

	public void setRpaLogFileCheckInfo(RpaLogFileCheckInfoResponse rpaLogFileCheckInfo) {
		this.rpaLogFileCheckInfo = rpaLogFileCheckInfo;
	}

	public RpaManagementToolServiceCheckInfoResponse getRpaManagementToolServiceCheckInfo() {
		return rpaManagementToolServiceCheckInfo;
	}

	public void setRpaManagementToolServiceCheckInfo(RpaManagementToolServiceCheckInfoResponse rpaManagementToolServiceCheckInfo) {
		this.rpaManagementToolServiceCheckInfo = rpaManagementToolServiceCheckInfo;
	}

	public List<MonitorStringValueInfoResponse> getStringValueInfo() {
		return stringValueInfo;
	}


	public void setStringValueInfo(List<MonitorStringValueInfoResponse> stringValueInfo) {
		this.stringValueInfo = stringValueInfo;
	}


	public List<BinaryPatternInfoResponse> getBinaryPatternInfo() {
		return binaryPatternInfo;
	}


	public void setBinaryPatternInfo(List<BinaryPatternInfoResponse> binaryPatternInfo) {
		this.binaryPatternInfo = binaryPatternInfo;
	}


	public List<MonitorTruthValueInfoResponse> getTruthValueInfo() {
		return truthValueInfo;
	}


	public void setTruthValueInfo(List<MonitorTruthValueInfoResponse> truthValueInfo) {
		this.truthValueInfo = truthValueInfo;
	}


	public String getLogFormatId() {
		return logFormatId;
	}


	public void setLogFormatId(String logFormatId) {
		this.logFormatId = logFormatId;
	}


	public List<NotifyRelationInfoResponse> getPredictionNotifyRelationList() {
		return predictionNotifyRelationList;
	}


	public void setPredictionNotifyRelationList(List<NotifyRelationInfoResponse> predictionNotifyRelationList) {
		this.predictionNotifyRelationList = predictionNotifyRelationList;
	}


	public List<NotifyRelationInfoResponse> getChangeNotifyRelationList() {
		return changeNotifyRelationList;
	}


	public void setChangeNotifyRelationList(List<NotifyRelationInfoResponse> changeNotifyRelationList) {
		this.changeNotifyRelationList = changeNotifyRelationList;
	}


	public PriorityChangeJudgmentTypeEnum getPriorityChangeJudgmentType() {
		return priorityChangeJudgmentType;
	}

	public void setPriorityChangeJudgmentType(PriorityChangeJudgmentTypeEnum priorityChangeJudgmentType) {
		this.priorityChangeJudgmentType = priorityChangeJudgmentType;
	}

	public PriorityChangeFailureTypeEnum getPriorityChangeFailureType() {
		return priorityChangeFailureType;
	}

	public void setPriorityChangeFailureType(PriorityChangeFailureTypeEnum priorityChangeFailureType) {
		this.priorityChangeFailureType = priorityChangeFailureType;
	}

	@Override
	public String toString() {
		return "MonitorInfoResponse [monitorId=" + monitorId + ", monitorType=" + monitorType + ", monitorTypeId="
				+ monitorTypeId + ", application=" + application + ", collectorFlg=" + collectorFlg + ", description="
				+ description + ", itemName=" + itemName + ", measure=" + measure + ", monitorFlg=" + monitorFlg
				+ ", runInterval=" + runInterval + ", calendarId=" + calendarId + ", customCheckInfo=" + customCheckInfo
				+ ", customTrapCheckInfo=" + customTrapCheckInfo + ", httpCheckInfo=" + httpCheckInfo + ", facilityId="
				+ facilityId + ", scope=" + scope + ", predictionFlg=" + predictionFlg + ", predictionMethod="
				+ predictionMethod + ", predictionAnalysysRange=" + predictionAnalysysRange + ", predictionTarget="
				+ predictionTarget + ", predictionApplication=" + predictionApplication + ", changeFlg=" + changeFlg
				+ ", changeAnalysysRange=" + changeAnalysysRange + ", changeApplication=" + changeApplication
				+ ", numericValueInfo=" + numericValueInfo + ", perfCheckInfo=" + perfCheckInfo + ", pingCheckInfo="
				+ pingCheckInfo + ", portCheckInfo=" + portCheckInfo + ", processCheckInfo=" + processCheckInfo
				+ ", snmpCheckInfo=" + snmpCheckInfo + ", sqlCheckInfo=" + sqlCheckInfo + ", trapCheckInfo="
				+ trapCheckInfo + ", winServiceCheckInfo=" + winServiceCheckInfo + ", winEventCheckInfo="
				+ winEventCheckInfo + ", logfileCheckInfo=" + logfileCheckInfo + ", binaryCheckInfo=" + binaryCheckInfo
				+ ", packetCheckInfo=" + packetCheckInfo + ", pluginCheckInfo=" + pluginCheckInfo
				+ ", httpScenarioCheckInfo=" + httpScenarioCheckInfo + ", jmxCheckInfo=" + jmxCheckInfo
				+ ", logcountCheckInfo=" + logcountCheckInfo + ", correlationCheckInfo=" + correlationCheckInfo
				+ ", integrationCheckInfo=" + integrationCheckInfo + ", stringValueInfo=" + stringValueInfo
				+ ", binaryPatternInfo=" + binaryPatternInfo + ", truthValueInfo=" + truthValueInfo + ", logFormatId="
				+ logFormatId + ", notifyRelationList=" + notifyRelationList + ", predictionNotifyRelationList="
				+ predictionNotifyRelationList + ", changeNotifyRelationList=" + changeNotifyRelationList
				+ ", priorityChangeJudgmentType=" + priorityChangeJudgmentType + ", priorityChangeFailureType="
				+ priorityChangeFailureType + ", ownerRoleId=" + ownerRoleId + ", regDate=" + regDate + ", regUser="
				+ regUser + ", updateDate=" + updateDate + ", updateUser=" + updateUser + "]";
	}
	
}