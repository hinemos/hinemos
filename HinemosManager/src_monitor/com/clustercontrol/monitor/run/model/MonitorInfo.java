/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.analytics.model.CorrelationCheckInfo;
import com.clustercontrol.analytics.model.IntegrationCheckInfo;
import com.clustercontrol.analytics.model.LogcountCheckInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.binary.model.BinaryCheckInfo;
import com.clustercontrol.binary.model.BinaryPatternInfo;
import com.clustercontrol.binary.model.PacketCheckInfo;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.custom.model.CustomCheckInfo;
import com.clustercontrol.customtrap.model.CustomTrapCheckInfo;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.http.model.HttpCheckInfo;
import com.clustercontrol.http.model.HttpScenarioCheckInfo;
import com.clustercontrol.hub.model.LogFormat;
import com.clustercontrol.jmx.model.JmxCheckInfo;
import com.clustercontrol.logfile.model.LogfileCheckInfo;
import com.clustercontrol.monitor.plugin.model.PluginCheckInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.performance.monitor.model.PerfCheckInfo;
import com.clustercontrol.ping.model.PingCheckInfo;
import com.clustercontrol.port.model.PortCheckInfo;
import com.clustercontrol.process.model.ProcessCheckInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.rpa.monitor.model.RpaLogFileCheckInfo;
import com.clustercontrol.rpa.monitor.model.RpaManagementToolServiceCheckInfo;
import com.clustercontrol.snmp.model.SnmpCheckInfo;
import com.clustercontrol.snmptrap.model.TrapCheckInfo;
import com.clustercontrol.sql.model.SqlCheckInfo;
import com.clustercontrol.winevent.model.WinEventCheckInfo;
import com.clustercontrol.winservice.model.WinServiceCheckInfo;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;


/**
 * The persistent class for the cc_monitor_info database table.
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@Entity
@Table(name="cc_monitor_info", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.MONITOR,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="monitor_id", insertable=false, updatable=false))
public class MonitorInfo extends ObjectPrivilegeTargetInfo {
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
	private CustomCheckInfo customCheckInfo;
	private CustomTrapCheckInfo customTrapCheckInfo;
	private HttpCheckInfo httpCheckInfo;
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
	private List<MonitorNumericValueInfo> numericValueInfo = new ArrayList<>();
	private PerfCheckInfo perfCheckInfo;
	private PingCheckInfo pingCheckInfo;
	private PortCheckInfo portCheckInfo;
	private ProcessCheckInfo processCheckInfo;
	private SnmpCheckInfo snmpCheckInfo;
	private SqlCheckInfo sqlCheckInfo;
	private TrapCheckInfo trapCheckInfo;
	private WinServiceCheckInfo winServiceCheckInfo;
	private WinEventCheckInfo winEventCheckInfo;
	private LogfileCheckInfo logfileCheckInfo;
	private BinaryCheckInfo binaryCheckInfo;
	private PacketCheckInfo packetCheckInfo;
	private PluginCheckInfo pluginCheckInfo;
	private HttpScenarioCheckInfo httpScenarioCheckInfo;
	private JmxCheckInfo jmxCheckInfo;
	private LogcountCheckInfo logcountCheckInfo;
	private CorrelationCheckInfo correlationCheckInfo;
	private IntegrationCheckInfo integrationCheckInfo;
	private RpaLogFileCheckInfo rpaLogFileCheckInfo;
	private RpaManagementToolServiceCheckInfo rpaManagementToolServiceCheckInfo;
	private List<MonitorStringValueInfo> stringValueInfo = new ArrayList<>();
	private List<BinaryPatternInfo> binaryPatternInfo = new ArrayList<>();
	private List<MonitorTruthValueInfo> truthValueInfo = new ArrayList<>();
	private LogFormat logformat;
	
	private String scope;
	
	private CalendarInfo calendar;

	/** 通知 */
	private List<NotifyRelationInfo> notifyRelationList;

	/** 通知(将来予測用) */
	private List<NotifyRelationInfo> predictionNotifyRelationList;

	/** 通知(変化点監視用) */
	private List<NotifyRelationInfo> changeNotifyRelationList;

	/** 判定による重要度変化のタイプ */
	private Integer priorityChangeJudgmentType;

	/** 取得失敗による重要度変化のタイプ */
	private Integer priorityChangeFailureType;

	
	public MonitorInfo() {
		super();
	}

	@Id
	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
		setObjectId(this.monitorId);
	}

	@Column(name="application")
	public String getApplication() {
		return this.application;
	}

	public void setApplication(String application) {
		this.application = application;
	}


	@Column(name="collector_flg")
	public Boolean getCollectorFlg() {
		return this.collectorFlg;
	}

	public void setCollectorFlg(Boolean collectorFlg) {
		this.collectorFlg = collectorFlg;
	}


	@Column(name="delay_time")
	public Integer getDelayTime() {
		return this.delayTime;
	}

	public void setDelayTime(Integer delayTime) {
		this.delayTime = delayTime;
	}


	@Column(name="description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Column(name="failure_priority")
	public Integer getFailurePriority() {
		return this.failurePriority;
	}

	public void setFailurePriority(Integer failurePriority) {
		this.failurePriority = failurePriority;
	}


	@Column(name="item_name")
	public String getItemName() {
		return this.itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	@Column(name="measure")
	public String getMeasure() {
		return this.measure;
	}

	public void setMeasure(String measure) {
		this.measure = measure;
	}


	@Column(name="monitor_flg")
	public Boolean getMonitorFlg() {
		return this.monitorFlg;
	}

	public void setMonitorFlg(Boolean monitorFlg) {
		this.monitorFlg = monitorFlg;
	}


	@Column(name="monitor_type")
	public Integer getMonitorType() {
		return this.monitorType;
	}

	public void setMonitorType(Integer monitorType) {
		this.monitorType = monitorType;
	}


	@Column(name="monitor_type_id")
	public String getMonitorTypeId() {
		return this.monitorTypeId;
	}

	public void setMonitorTypeId(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
	}


	@Column(name="notify_group_id")
	public String getNotifyGroupId() {
		return this.notifyGroupId;
	}

	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
	}


	@Column(name="reg_date")
	public Long getRegDate() {
		return this.regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}


	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}


	@Column(name="run_interval")
	public Integer getRunInterval() {
		return this.runInterval;
	}

	public void setRunInterval(Integer runInterval) {
		this.runInterval = runInterval;
	}


	@Column(name="trigger_type")
	public String getTriggerType() {
		return this.triggerType;
	}

	public void setTriggerType(String triggerType) {
		this.triggerType = triggerType;
	}


	@Column(name="update_date")
	public Long getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}


	@Column(name="update_user")
	public String getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}


	//bi-directional one-to-one association to MonitorCustomInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public CustomCheckInfo getCustomCheckInfo() {
		return this.customCheckInfo;
	}

	public void setCustomCheckInfo(CustomCheckInfo customCheckInfo) {
		this.customCheckInfo = customCheckInfo;
	}

	//bi-directional one-to-one association to MonitorCustomTrapInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public CustomTrapCheckInfo getCustomTrapCheckInfo() {
		return this.customTrapCheckInfo;
	}

	public void setCustomTrapCheckInfo(CustomTrapCheckInfo customTrapCheckInfo) {
		this.customTrapCheckInfo = customTrapCheckInfo;
	}
	//bi-directional one-to-one association to MonitorHttpInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public HttpCheckInfo getHttpCheckInfo() {
		return this.httpCheckInfo;
	}

	public void setHttpCheckInfo(HttpCheckInfo httpCheckInfo) {
		this.httpCheckInfo = httpCheckInfo;
	}


	@Column(name="calendar_id")
	public String getCalendarId() {
		return this.calendarId;
	}
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}
	
	@Transient
	public CalendarInfo getCalendar() {
		return this.calendar;
	}
	public void setCalendar(CalendarInfo calendar) {
		this.calendar = calendar;
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	
	@Column(name="log_format_id")
	public String getLogFormatId() {
		return logFormatId;
	}

	public void setLogFormatId(String logFormatId) {
		this.logFormatId = logFormatId;
	}

	@Column(name="prediction_flg")
	public Boolean getPredictionFlg() {
		return predictionFlg;
	}

	public void setPredictionFlg(Boolean predictionFlg) {
		this.predictionFlg = predictionFlg;
	}

	@Column(name="prediction_method")
	public String getPredictionMethod() {
		return predictionMethod;
	}

	public void setPredictionMethod(String predictionMethod) {
		this.predictionMethod = predictionMethod;
	}

	@Column(name="prediction_analysys_range")
	public Integer getPredictionAnalysysRange() {
		return predictionAnalysysRange;
	}

	public void setPredictionAnalysysRange(Integer predictionAnalysysRange) {
		this.predictionAnalysysRange = predictionAnalysysRange;
	}

	@Column(name="prediction_target")
	public Integer getPredictionTarget() {
		return predictionTarget;
	}

	public void setPredictionTarget(Integer predictionTarget) {
		this.predictionTarget = predictionTarget;
	}

	@Column(name="prediction_application")
	public String getPredictionApplication() {
		return this.predictionApplication;
	}

	public void setPredictionApplication(String predictionApplication) {
		this.predictionApplication = predictionApplication;
	}

	@Column(name="change_flg")
	public Boolean getChangeFlg() {
		return changeFlg;
	}

	public void setChangeFlg(Boolean changeFlg) {
		this.changeFlg = changeFlg;
	}

	@Column(name="change_analysys_range")
	public Integer getChangeAnalysysRange() {
		return changeAnalysysRange;
	}

	public void setChangeAnalysysRange(Integer changeAnalysysRange) {
		this.changeAnalysysRange = changeAnalysysRange;
	}

	@Column(name="change_application")
	public String getChangeApplication() {
		return this.changeApplication;
	}

	public void setChangeApplication(String changeApplication) {
		this.changeApplication = changeApplication;
	}

	@Column(name="sdml_monitor_type_id")
	@XmlTransient
	public String getSdmlMonitorTypeId() {
		return sdmlMonitorTypeId;
	}

	public void setSdmlMonitorTypeId(String sdmlMonitorTypeId) {
		this.sdmlMonitorTypeId = sdmlMonitorTypeId;
	}

	@XmlTransient
	@Column(name="priority_change_judgment_type")
	public Integer getPriorityChangeJudgmentType() {
		return priorityChangeJudgmentType;
	}

	public void setPriorityChangeJudgmentType(Integer priorityChangeJudgmentType) {
		this.priorityChangeJudgmentType = priorityChangeJudgmentType;
	}

	@XmlTransient
	@Column(name="priority_change_failure_type")
	public Integer getPriorityChangeFailureType() {
		return priorityChangeFailureType;
	}

	public void setPriorityChangeFailureType(Integer priorityChangeFailureType) {
		this.priorityChangeFailureType = priorityChangeFailureType;
	}

	//bi-directional many-to-one association to MonitorNumericValueInfoEntity
	@OneToMany(mappedBy="monitorInfo", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorNumericValueInfo> getNumericValueInfo() {
		return this.numericValueInfo;
	}

	public void setNumericValueInfo(List<MonitorNumericValueInfo> numericValueInfo) {
		this.numericValueInfo = numericValueInfo;
	}


	//bi-directional one-to-one association to MonitorPerfInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public PerfCheckInfo getPerfCheckInfo() {
		return this.perfCheckInfo;
	}

	public void setPerfCheckInfo(PerfCheckInfo perfCheckInfo) {
		this.perfCheckInfo = perfCheckInfo;
	}


	//bi-directional one-to-one association to MonitorPingInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public PingCheckInfo getPingCheckInfo() {
		return this.pingCheckInfo;
	}

	public void setPingCheckInfo(PingCheckInfo pingCheckInfo) {
		this.pingCheckInfo = pingCheckInfo;
	}


	//bi-directional one-to-one association to MonitorPortInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public PortCheckInfo getPortCheckInfo() {
		return this.portCheckInfo;
	}

	public void setPortCheckInfo(PortCheckInfo portCheckInfo) {
		this.portCheckInfo = portCheckInfo;
	}


	//bi-directional one-to-one association to MonitorProcessInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public ProcessCheckInfo getProcessCheckInfo() {
		return this.processCheckInfo;
	}

	public void setProcessCheckInfo(ProcessCheckInfo processCheckInfo) {
		this.processCheckInfo = processCheckInfo;
	}


	//bi-directional one-to-one association to MonitorSnmpInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public SnmpCheckInfo getSnmpCheckInfo() {
		return this.snmpCheckInfo;
	}

	public void setSnmpCheckInfo(SnmpCheckInfo snmpCheckInfo) {
		this.snmpCheckInfo = snmpCheckInfo;
	}


	//bi-directional one-to-one association to MonitorSqlInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public SqlCheckInfo getSqlCheckInfo() {
		return this.sqlCheckInfo;
	}

	public void setSqlCheckInfo(SqlCheckInfo sqlCheckInfo) {
		this.sqlCheckInfo = sqlCheckInfo;
	}


	//bi-directional one-to-one association to MonitorTrapInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public TrapCheckInfo getTrapCheckInfo() {
		return this.trapCheckInfo;
	}

	public void setTrapCheckInfo(TrapCheckInfo trapCheckInfo) {
		this.trapCheckInfo = trapCheckInfo;
	}


	//bi-directional one-to-one association to MonitorWinEventInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public WinEventCheckInfo getWinEventCheckInfo() {
		return this.winEventCheckInfo;
	}

	public void setWinEventCheckInfo(WinEventCheckInfo winEventCheckInfo) {
		this.winEventCheckInfo = winEventCheckInfo;
	}

	//bi-directional one-to-one association to MonitorWinserviceInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public WinServiceCheckInfo getWinServiceCheckInfo() {
		return this.winServiceCheckInfo;
	}

	public void setWinServiceCheckInfo(WinServiceCheckInfo winServiceCheckInfo) {
		this.winServiceCheckInfo = winServiceCheckInfo;
	}


	//bi-directional one-to-one association to MonitorLogfileInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public LogfileCheckInfo getLogfileCheckInfo() {
		return this.logfileCheckInfo;
	}

	public void setLogfileCheckInfo(LogfileCheckInfo logfileCheckInfo) {
		this.logfileCheckInfo = logfileCheckInfo;
	}

	//bi-directional one-to-one association to MonitorBinaryInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public BinaryCheckInfo getBinaryCheckInfo() {
		return this.binaryCheckInfo;
	}

	public void setBinaryCheckInfo(BinaryCheckInfo binaryCheckInfo) {
		this.binaryCheckInfo = binaryCheckInfo;
	}

	//bi-directional one-to-one association to MonitorPacketInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public PacketCheckInfo getPacketCheckInfo() {
		return this.packetCheckInfo;
	}

	public void setPacketCheckInfo(PacketCheckInfo packetCheckInfo) {
		this.packetCheckInfo = packetCheckInfo;
	}

	//bi-directional one-to-one association to MonitorHttpScenarioInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public HttpScenarioCheckInfo getHttpScenarioCheckInfo() {
		return httpScenarioCheckInfo;
	}

	public void setHttpScenarioCheckInfo(
			HttpScenarioCheckInfo httpScenarioCheckInfo) {
		this.httpScenarioCheckInfo = httpScenarioCheckInfo;
	}

	//bi-directional one-to-one association to MonitorJmxInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public JmxCheckInfo getJmxCheckInfo() {
		return jmxCheckInfo;
	}

	public void setJmxCheckInfo(
			JmxCheckInfo jmxCheckInfo) {
		this.jmxCheckInfo = jmxCheckInfo;
	}

	//bi-directional one-to-one association to MonitorLogcountInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public LogcountCheckInfo getLogcountCheckInfo() {
		return logcountCheckInfo;
	}

	public void setLogcountCheckInfo(LogcountCheckInfo logcountCheckInfo) {
		this.logcountCheckInfo = logcountCheckInfo;
	}

	//bi-directional one-to-one association to MonitorCorrelationInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public CorrelationCheckInfo getCorrelationCheckInfo() {
		return correlationCheckInfo;
	}

	public void setCorrelationCheckInfo(CorrelationCheckInfo correlationCheckInfo) {
		this.correlationCheckInfo = correlationCheckInfo;
	}

	//bi-directional one-to-one association to MonitorIntegrationInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public IntegrationCheckInfo getIntegrationCheckInfo() {
		return integrationCheckInfo;
	}

	public void setIntegrationCheckInfo(IntegrationCheckInfo integrationCheckInfo) {
		this.integrationCheckInfo = integrationCheckInfo;
	}


	//bi-directional one-to-one association to MonitorLogfileInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public RpaLogFileCheckInfo getRpaLogFileCheckInfo() {
		return rpaLogFileCheckInfo;
	}

	public void setRpaLogFileCheckInfo(RpaLogFileCheckInfo rPALogFileCheckInfo) {
		this.rpaLogFileCheckInfo = rPALogFileCheckInfo;
	}

	//bi-directional one-to-one association to MonitorLogfileInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public RpaManagementToolServiceCheckInfo getRpaManagementToolServiceCheckInfo() {
		return rpaManagementToolServiceCheckInfo;
	}

	public void setRpaManagementToolServiceCheckInfo(RpaManagementToolServiceCheckInfo rpaManagementToolServiceCheckInfo) {
		this.rpaManagementToolServiceCheckInfo = rpaManagementToolServiceCheckInfo;
	}

	//bi-directional one-to-one association to MonitorLogfileInfoEntity
	@OneToOne(mappedBy="monitorInfo", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public PluginCheckInfo getPluginCheckInfo() {
		return this.pluginCheckInfo;
	}

	public void setPluginCheckInfo(PluginCheckInfo pluginCheckInfo) {
		this.pluginCheckInfo = pluginCheckInfo;
	}


	//bi-directional many-to-one association to MonitorStringValueInfoEntity
	@OneToMany(mappedBy="monitorInfo", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorStringValueInfo> getStringValueInfo() {
		return this.stringValueInfo;
	}

	public void setStringValueInfo(List<MonitorStringValueInfo> stringValueInfo) {
		if (stringValueInfo != null && stringValueInfo.size() > 0) {
			Collections.sort(stringValueInfo, new Comparator<MonitorStringValueInfo>() {
				@Override
				public int compare(MonitorStringValueInfo o1, MonitorStringValueInfo o2) {
					return o1.getId().getOrderNo().compareTo(o2.getId().getOrderNo());
				}
			});
		}
		this.stringValueInfo = stringValueInfo;
	}

	//bi-directional many-to-one association to BinaryPatternInfoEntity
	@OneToMany(mappedBy="monitorInfo", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<BinaryPatternInfo> getBinaryPatternInfo() {
		return this.binaryPatternInfo;
	}

	public void setBinaryPatternInfo(List<BinaryPatternInfo> binaryPatternInfo) {
		if (binaryPatternInfo != null && binaryPatternInfo.size() > 0) {
			Collections.sort(binaryPatternInfo, new Comparator<BinaryPatternInfo>() {
				@Override
				public int compare(BinaryPatternInfo o1, BinaryPatternInfo o2) {
					return o1.getId().getOrderNo().compareTo(o2.getId().getOrderNo());
				}
			});
		}
		this.binaryPatternInfo = binaryPatternInfo;
	}

	//bi-directional many-to-one association to MonitorTruthValueInfoEntity
	@OneToMany(mappedBy="monitorInfo", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	public List<MonitorTruthValueInfo> getTruthValueInfo() {
		return this.truthValueInfo;
	}

	public void setTruthValueInfo(List<MonitorTruthValueInfo> truthValueInfo) {
		this.truthValueInfo = truthValueInfo;
	}

	/**
	 * MonitorNumericValueInfoEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteMonitorNumericValueInfoEntities(List<MonitorNumericValueInfoPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorNumericValueInfo> list = this.getNumericValueInfo();
			Iterator<MonitorNumericValueInfo> iter = list.iterator();
			while(iter.hasNext()) {
				MonitorNumericValueInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}

	/**
	 * MonitorStringValueInfoEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteMonitorStringValueInfoEntities(List<MonitorStringValueInfoPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorStringValueInfo> list = this.getStringValueInfo();
			Iterator<MonitorStringValueInfo> iter = list.iterator();
			while(iter.hasNext()) {
				MonitorStringValueInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}
	

	/**
	 * BinaryPatternInfo削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteBinaryPatternInfoEntities(List<MonitorStringValueInfoPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<BinaryPatternInfo> list = this.getBinaryPatternInfo();
			Iterator<BinaryPatternInfo> iter = list.iterator();
			while(iter.hasNext()) {
				BinaryPatternInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}

	/**
	 * MonitorTruthValueInfoEntity削除<BR>
	 * 
	 * 指定されたPK以外の子Entityを削除する。
	 * 
	 */
	public void deleteMonitorTruthValueInfoEntities(List<MonitorTruthValueInfoPK> notDelPkList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorTruthValueInfo> list = this.getTruthValueInfo();
			Iterator<MonitorTruthValueInfo> iter = list.iterator();
			while(iter.hasNext()) {
				MonitorTruthValueInfo entity = iter.next();
				if (!notDelPkList.contains(entity.getId())) {
					iter.remove();
					em.remove(entity);
				}
			}
		}
	}

	/**
	 * 通知情報リストを取得します。
	 * 
	 * @return 通知情報リスト
	 */
	@Transient
	public List<NotifyRelationInfo> getNotifyRelationList(){
		return this.notifyRelationList;
	}

	/**
	 * 通知情報リストを設定します。
	 * 
	 * @param notifyRelationList 通知情報リスト
	 */
	public void setNotifyRelationList(List<NotifyRelationInfo> notifyRelationList ){
		this.notifyRelationList = notifyRelationList;
	}

	/**
	 * 通知情報リスト（将来通知用）を取得します。
	 * 
	 * @return 通知情報リスト（将来通知用）
	 */
	@Transient
	public List<NotifyRelationInfo> getPredictionNotifyRelationList(){
		return this.predictionNotifyRelationList;
	}

	/**
	 * 通知情報リスト（将来通知用）を設定します。
	 * 
	 * @param predictionNotifyRelationList 通知情報リスト（将来通知用）
	 */
	public void setPredictionNotifyRelationList(List<NotifyRelationInfo> predictionNotifyRelationList ){
		this.predictionNotifyRelationList = predictionNotifyRelationList;
	}

	/**
	 * 通知情報リスト（変化点監視用）を取得します。
	 * 
	 * @return 通知情報リスト（変化点監視用）
	 */
	@Transient
	public List<NotifyRelationInfo> getChangeNotifyRelationList(){
		return this.changeNotifyRelationList;
	}

	/**
	 * 通知情報リスト（変化点監視用）を設定します。
	 * 
	 * @param changeNotifyRelationList 通知情報リスト（変化点監視用）
	 */
	public void setChangeNotifyRelationList(List<NotifyRelationInfo> changeNotifyRelationList ){
		this.changeNotifyRelationList = changeNotifyRelationList;
	}

	@Transient
	public String getScope() {
		if (scope == null)
			try {
				scope = new RepositoryControllerBean().getFacilityPath(getFacilityId(), null);
			} catch (HinemosUnknown e) {
				Logger.getLogger(this.getClass()).debug(e.getMessage(), e);
			}
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
	
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@PrimaryKeyJoinColumn(name="log_format_id")
	public LogFormat getLogformat() {
		return logformat;
	}
	public void setLogformat(LogFormat logformat) {
		this.logformat = logformat;
	}
}