/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.ExecFacilityFlgEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.SyslogFacilityEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.SyslogSeverityEnum;

public class LogEscalateNotifyDetailInfoResponse {
	private Boolean infoValidFlg;
	private Boolean warnValidFlg;
	private Boolean criticalValidFlg;
	private Boolean unknownValidFlg;

	@RestBeanConvertEnum
	private ExecFacilityFlgEnum escalateFacilityFlg;
	private Integer escalatePort;

	private String infoEscalateMessage;
	private String warnEscalateMessage;
	private String criticalEscalateMessage;
	private String unknownEscalateMessage;

	@RestBeanConvertEnum
	private SyslogSeverityEnum infoSyslogPriority;
	@RestBeanConvertEnum
	private SyslogSeverityEnum warnSyslogPriority;
	@RestBeanConvertEnum
	private SyslogSeverityEnum criticalSyslogPriority;
	@RestBeanConvertEnum
	private SyslogSeverityEnum unknownSyslogPriority;

	@RestBeanConvertEnum
	private SyslogFacilityEnum infoSyslogFacility;
	@RestBeanConvertEnum
	private SyslogFacilityEnum warnSyslogFacility;
	@RestBeanConvertEnum
	private SyslogFacilityEnum criticalSyslogFacility;
	@RestBeanConvertEnum
	private SyslogFacilityEnum unknownSyslogFacility;

	private String escalateFacilityId;

	@RestPartiallyTransrateTarget
	private String escalateScope;

	public LogEscalateNotifyDetailInfoResponse() {
	}

	public Boolean getInfoValidFlg() {
		return infoValidFlg;
	}

	public void setInfoValidFlg(Boolean infoValidFlg) {
		this.infoValidFlg = infoValidFlg;
	}

	public Boolean getWarnValidFlg() {
		return warnValidFlg;
	}

	public void setWarnValidFlg(Boolean warnValidFlg) {
		this.warnValidFlg = warnValidFlg;
	}

	public Boolean getCriticalValidFlg() {
		return criticalValidFlg;
	}

	public void setCriticalValidFlg(Boolean criticalValidFlg) {
		this.criticalValidFlg = criticalValidFlg;
	}

	public Boolean getUnknownValidFlg() {
		return unknownValidFlg;
	}

	public void setUnknownValidFlg(Boolean unknownValidFlg) {
		this.unknownValidFlg = unknownValidFlg;
	}

	public ExecFacilityFlgEnum getEscalateFacilityFlg() {
		return escalateFacilityFlg;
	}

	public void setEscalateFacilityFlg(ExecFacilityFlgEnum escalateFacilityFlg) {
		this.escalateFacilityFlg = escalateFacilityFlg;
	}

	public Integer getEscalatePort() {
		return escalatePort;
	}

	public void setEscalatePort(Integer escalatePort) {
		this.escalatePort = escalatePort;
	}

	public String getInfoEscalateMessage() {
		return infoEscalateMessage;
	}

	public void setInfoEscalateMessage(String infoEscalateMessage) {
		this.infoEscalateMessage = infoEscalateMessage;
	}

	public String getWarnEscalateMessage() {
		return warnEscalateMessage;
	}

	public void setWarnEscalateMessage(String warnEscalateMessage) {
		this.warnEscalateMessage = warnEscalateMessage;
	}

	public String getCriticalEscalateMessage() {
		return criticalEscalateMessage;
	}

	public void setCriticalEscalateMessage(String criticalEscalateMessage) {
		this.criticalEscalateMessage = criticalEscalateMessage;
	}

	public String getUnknownEscalateMessage() {
		return unknownEscalateMessage;
	}

	public void setUnknownEscalateMessage(String unknownEscalateMessage) {
		this.unknownEscalateMessage = unknownEscalateMessage;
	}

	public SyslogSeverityEnum getInfoSyslogPriority() {
		return infoSyslogPriority;
	}

	public void setInfoSyslogPriority(SyslogSeverityEnum infoSyslogPriority) {
		this.infoSyslogPriority = infoSyslogPriority;
	}

	public SyslogSeverityEnum getWarnSyslogPriority() {
		return warnSyslogPriority;
	}

	public void setWarnSyslogPriority(SyslogSeverityEnum warnSyslogPriority) {
		this.warnSyslogPriority = warnSyslogPriority;
	}

	public SyslogSeverityEnum getCriticalSyslogPriority() {
		return criticalSyslogPriority;
	}

	public void setCriticalSyslogPriority(SyslogSeverityEnum criticalSyslogPriority) {
		this.criticalSyslogPriority = criticalSyslogPriority;
	}

	public SyslogSeverityEnum getUnknownSyslogPriority() {
		return unknownSyslogPriority;
	}

	public void setUnknownSyslogPriority(SyslogSeverityEnum unknownSyslogPriority) {
		this.unknownSyslogPriority = unknownSyslogPriority;
	}

	public SyslogFacilityEnum getInfoSyslogFacility() {
		return infoSyslogFacility;
	}

	public void setInfoSyslogFacility(SyslogFacilityEnum infoSyslogFacility) {
		this.infoSyslogFacility = infoSyslogFacility;
	}

	public SyslogFacilityEnum getWarnSyslogFacility() {
		return warnSyslogFacility;
	}

	public void setWarnSyslogFacility(SyslogFacilityEnum warnSyslogFacility) {
		this.warnSyslogFacility = warnSyslogFacility;
	}

	public SyslogFacilityEnum getCriticalSyslogFacility() {
		return criticalSyslogFacility;
	}

	public void setCriticalSyslogFacility(SyslogFacilityEnum criticalSyslogFacility) {
		this.criticalSyslogFacility = criticalSyslogFacility;
	}

	public SyslogFacilityEnum getUnknownSyslogFacility() {
		return unknownSyslogFacility;
	}

	public void setUnknownSyslogFacility(SyslogFacilityEnum unknownSyslogFacility) {
		this.unknownSyslogFacility = unknownSyslogFacility;
	}

	public String getEscalateFacilityId() {
		return escalateFacilityId;
	}

	public void setEscalateFacilityId(String escalateFacilityId) {
		this.escalateFacilityId = escalateFacilityId;
	}

	public String getEscalateScope() {
		return escalateScope;
	}

	public void setEscalateScope(String escalateScope) {
		this.escalateScope = escalateScope;
	}
}
