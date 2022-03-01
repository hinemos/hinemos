/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.ExecFacilityFlgEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyJobTypeEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyPriorityEnum;
import com.clustercontrol.rest.util.RestItemNameResolver;
import com.clustercontrol.util.MessageConstant;

public class JobNotifyDetailInfoRequest implements RequestDto {
	@RestBeanConvertEnum
	private NotifyJobTypeEnum notifyJobType;

	private Boolean infoValidFlg = Boolean.FALSE;

	private Boolean warnValidFlg = Boolean.FALSE;

	private Boolean criticalValidFlg = Boolean.FALSE;

	private Boolean unknownValidFlg = Boolean.FALSE;

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

	private Boolean retryFlg = Boolean.FALSE;
	@RestValidateInteger(minVal = 0, maxVal = 32767)
	private Integer retryCount;
	private Boolean successInternalFlg = Boolean.FALSE;
	private Boolean failureInternalFlg = Boolean.FALSE;

	@RestItemName(value=MessageConstant.JOBLINK_SEND_SETTING_ID)
	private String joblinkSendSettingId;

	@RestBeanConvertEnum
	private NotifyPriorityEnum infoJobFailurePriority;
	@RestBeanConvertEnum
	private NotifyPriorityEnum warnJobFailurePriority;
	@RestBeanConvertEnum
	private NotifyPriorityEnum criticalJobFailurePriority;
	@RestBeanConvertEnum
	private NotifyPriorityEnum unknownJobFailurePriority;

	private String jobExecFacilityId;

	public JobNotifyDetailInfoRequest() {
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

	@Override
	public void correlationCheck() throws InvalidSetting {
		// [実行モード]が「ジョブ連携メッセージ送信」の場合
		if (notifyJobType == NotifyJobTypeEnum.JOB_LINK_SEND) {
			// [ジョブ連携送信設定ID]必須
			if (joblinkSendSettingId == null || joblinkSendSettingId.isEmpty()) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "joblinkSendSettingId");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}

			// [送信に失敗した場合に再送する]がtrueの場合、[再送回数]必須
			if (retryFlg && retryCount == null) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "retryCount");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
		}
	}
}
