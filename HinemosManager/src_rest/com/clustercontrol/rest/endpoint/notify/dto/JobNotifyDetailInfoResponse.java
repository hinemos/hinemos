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
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyJobTypeEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyPriorityEnum;

public class JobNotifyDetailInfoResponse {
	@RestBeanConvertEnum
	private NotifyJobTypeEnum notifyJobType;

	private Boolean infoValidFlg;
	private Boolean warnValidFlg;
	private Boolean criticalValidFlg;
	private Boolean unknownValidFlg;

	@RestBeanConvertEnum
	private ExecFacilityFlgEnum jobExecFacilityFlg;

	private String infoJobunitId;
	private String warnJobunitId;
	private String criticalJobunitId;
	private String unknownJobunitId;

	private String infoJobId;
	private String warnJobId;
	private String criticalJobId;
	private String unknownJobId;

	@RestBeanConvertEnum
	private NotifyPriorityEnum infoJobFailurePriority;
	@RestBeanConvertEnum
	private NotifyPriorityEnum warnJobFailurePriority;
	@RestBeanConvertEnum
	private NotifyPriorityEnum criticalJobFailurePriority;
	@RestBeanConvertEnum
	private NotifyPriorityEnum unknownJobFailurePriority;

	private String jobExecFacilityId;

	@RestPartiallyTransrateTarget
	private String jobExecScope;

	private Boolean retryFlg;
	private Integer retryCount;
	private Boolean successInternalFlg;
	private Boolean failureInternalFlg;
	private String joblinkSendSettingId;

	public JobNotifyDetailInfoResponse() {
	}

	public NotifyJobTypeEnum getNotifyJobType() {
		return notifyJobType;
	}

	public void setNotifyJobType(NotifyJobTypeEnum notifyJobType) {
		this.notifyJobType = notifyJobType;
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

	public ExecFacilityFlgEnum getJobExecFacilityFlg() {
		return jobExecFacilityFlg;
	}

	public void setJobExecFacilityFlg(ExecFacilityFlgEnum jobExecFacilityFlg) {
		this.jobExecFacilityFlg = jobExecFacilityFlg;
	}

	public String getInfoJobunitId() {
		return infoJobunitId;
	}

	public void setInfoJobunitId(String infoJobunitId) {
		this.infoJobunitId = infoJobunitId;
	}

	public String getWarnJobunitId() {
		return warnJobunitId;
	}

	public void setWarnJobunitId(String warnJobunitId) {
		this.warnJobunitId = warnJobunitId;
	}

	public String getCriticalJobunitId() {
		return criticalJobunitId;
	}

	public void setCriticalJobunitId(String criticalJobunitId) {
		this.criticalJobunitId = criticalJobunitId;
	}

	public String getUnknownJobunitId() {
		return unknownJobunitId;
	}

	public void setUnknownJobunitId(String unknownJobunitId) {
		this.unknownJobunitId = unknownJobunitId;
	}

	public String getInfoJobId() {
		return infoJobId;
	}

	public void setInfoJobId(String infoJobId) {
		this.infoJobId = infoJobId;
	}

	public String getWarnJobId() {
		return warnJobId;
	}

	public void setWarnJobId(String warnJobId) {
		this.warnJobId = warnJobId;
	}

	public String getCriticalJobId() {
		return criticalJobId;
	}

	public void setCriticalJobId(String criticalJobId) {
		this.criticalJobId = criticalJobId;
	}

	public String getUnknownJobId() {
		return unknownJobId;
	}

	public void setUnknownJobId(String unknownJobId) {
		this.unknownJobId = unknownJobId;
	}

	public NotifyPriorityEnum getInfoJobFailurePriority() {
		return infoJobFailurePriority;
	}

	public void setInfoJobFailurePriority(NotifyPriorityEnum infoJobFailurePriority) {
		this.infoJobFailurePriority = infoJobFailurePriority;
	}

	public NotifyPriorityEnum getWarnJobFailurePriority() {
		return warnJobFailurePriority;
	}

	public void setWarnJobFailurePriority(NotifyPriorityEnum warnJobFailurePriority) {
		this.warnJobFailurePriority = warnJobFailurePriority;
	}

	public NotifyPriorityEnum getCriticalJobFailurePriority() {
		return criticalJobFailurePriority;
	}

	public void setCriticalJobFailurePriority(NotifyPriorityEnum criticalJobFailurePriority) {
		this.criticalJobFailurePriority = criticalJobFailurePriority;
	}

	public NotifyPriorityEnum getUnknownJobFailurePriority() {
		return unknownJobFailurePriority;
	}

	public void setUnknownJobFailurePriority(NotifyPriorityEnum unknownJobFailurePriority) {
		this.unknownJobFailurePriority = unknownJobFailurePriority;
	}

	public String getJobExecFacilityId() {
		return jobExecFacilityId;
	}

	public void setJobExecFacilityId(String jobExecFacilityId) {
		this.jobExecFacilityId = jobExecFacilityId;
	}

	public String getJobExecScope() {
		return jobExecScope;
	}

	public void setJobExecScope(String jobExecScope) {
		this.jobExecScope = jobExecScope;
	}

	public Boolean getRetryFlg() {
		return retryFlg;
	}

	public void setRetryFlg(Boolean retryFlg) {
		this.retryFlg = retryFlg;
	}

	public Integer getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	public Boolean getSuccessInternalFlg() {
		return successInternalFlg;
	}

	public void setSuccessInternalFlg(Boolean successInternalFlg) {
		this.successInternalFlg = successInternalFlg;
	}

	public Boolean getFailureInternalFlg() {
		return failureInternalFlg;
	}

	public void setFailureInternalFlg(Boolean failureInternalFlg) {
		this.failureInternalFlg = failureInternalFlg;
	}
	public String getJoblinkSendSettingId() {
		return joblinkSendSettingId;
	}

	public void setJoblinkSendSettingId(String joblinkSendSettingId) {
		this.joblinkSendSettingId = joblinkSendSettingId;
	}
}
