/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.ExecFacilityFlgEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyPriorityEnum;

public class InfraNotifyDetailInfoRequest implements RequestDto {

	private Boolean infoValidFlg;

	private Boolean warnValidFlg;

	private Boolean criticalValidFlg;

	private Boolean unknownValidFlg;

	@RestBeanConvertEnum
	private ExecFacilityFlgEnum infraExecFacilityFlg;

	private String infoInfraId;
	private String warnInfraId;
	private String criticalInfraId;
	private String unknownInfraId;

	@RestBeanConvertEnum
	private NotifyPriorityEnum infoInfraFailurePriority;
	@RestBeanConvertEnum
	private NotifyPriorityEnum warnInfraFailurePriority;
	@RestBeanConvertEnum
	private NotifyPriorityEnum criticalInfraFailurePriority;
	@RestBeanConvertEnum
	private NotifyPriorityEnum unknownInfraFailurePriority;

	private String infraExecFacilityId;

	public InfraNotifyDetailInfoRequest() {
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

	public ExecFacilityFlgEnum getInfraExecFacilityFlg() {
		return infraExecFacilityFlg;
	}

	public void setInfraExecFacilityFlg(ExecFacilityFlgEnum infraExecFacilityFlg) {
		this.infraExecFacilityFlg = infraExecFacilityFlg;
	}

	public String getInfoInfraId() {
		return infoInfraId;
	}

	public void setInfoInfraId(String infoInfraId) {
		this.infoInfraId = infoInfraId;
	}

	public String getWarnInfraId() {
		return warnInfraId;
	}

	public void setWarnInfraId(String warnInfraId) {
		this.warnInfraId = warnInfraId;
	}

	public String getCriticalInfraId() {
		return criticalInfraId;
	}

	public void setCriticalInfraId(String criticalInfraId) {
		this.criticalInfraId = criticalInfraId;
	}

	public String getUnknownInfraId() {
		return unknownInfraId;
	}

	public void setUnknownInfraId(String unknownInfraId) {
		this.unknownInfraId = unknownInfraId;
	}

	public NotifyPriorityEnum getInfoInfraFailurePriority() {
		return infoInfraFailurePriority;
	}

	public void setInfoInfraFailurePriority(NotifyPriorityEnum infoInfraFailurePriority) {
		this.infoInfraFailurePriority = infoInfraFailurePriority;
	}

	public NotifyPriorityEnum getWarnInfraFailurePriority() {
		return warnInfraFailurePriority;
	}

	public void setWarnInfraFailurePriority(NotifyPriorityEnum warnInfraFailurePriority) {
		this.warnInfraFailurePriority = warnInfraFailurePriority;
	}

	public NotifyPriorityEnum getCriticalInfraFailurePriority() {
		return criticalInfraFailurePriority;
	}

	public void setCriticalInfraFailurePriority(NotifyPriorityEnum criticalInfraFailurePriority) {
		this.criticalInfraFailurePriority = criticalInfraFailurePriority;
	}

	public NotifyPriorityEnum getUnknownInfraFailurePriority() {
		return unknownInfraFailurePriority;
	}

	public void setUnknownInfraFailurePriority(NotifyPriorityEnum unknownInfraFailurePriority) {
		this.unknownInfraFailurePriority = unknownInfraFailurePriority;
	}

	public String getInfraExecFacilityId() {
		return infraExecFacilityId;
	}

	public void setInfraExecFacilityId(String infraExecFacilityId) {
		this.infraExecFacilityId = infraExecFacilityId;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
