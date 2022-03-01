/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.sdml.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.SdmlControlSettingNotFound;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoRequest;
import com.clustercontrol.rest.endpoint.sdml.dto.enumtype.PriorityEnum;
import com.clustercontrol.sdml.util.QueryUtil;
import com.clustercontrol.util.MessageConstant;

public class ModifySdmlControlSettingRequest implements RequestDto {

	// private String ownerRoleId; // 変更不可

	// private String applicationId; // 変更不可

	@RestItemName(value = MessageConstant.DESCRIPTION)
	@RestValidateString(maxLen = 256)
	private String description;

	@RestItemName(value = MessageConstant.SCOPE)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 512)
	private String facilityId;

	// private String controlLogDirectory; // 変更不可
	// private String controlLogFilename; // 変更不可

	@RestItemName(value = MessageConstant.MONITOR_COLLECT)
	@RestValidateObject(notNull = true)
	private Boolean controlLogCollectFlg;

	@RestItemName(value = MessageConstant.APPLICATION)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64)
	private String application;

	@RestItemName(value = MessageConstant.VALID_FLG)
	@RestValidateObject(notNull = true)
	private Boolean validFlg;

	@RestItemName(value = MessageConstant.SDML_AUTO_DELETE_ENABLE)
	@RestValidateObject(notNull = true)
	private Boolean autoMonitorDeleteFlg;

	@RestItemName(value = MessageConstant.CALENDAR_ID)
	private String autoMonitorCalendarId;

	@RestItemName(value = MessageConstant.SDML_EARLY_STOP_THRESHOLD_SECOND)
	@RestValidateInteger(notNull = true, minVal = 0, maxVal = DataRangeConstant.SMALLINT_HIGH)
	private Integer earlyStopThresholdSecond;

	@RestItemName(value = MessageConstant.SDML_EARLY_STOP_NOTIFY)
	@RestBeanConvertEnum
	private PriorityEnum earlyStopNotifyPriority;

	@RestItemName(value = MessageConstant.SDML_AUTO_CREATE_FINISHED)
	@RestBeanConvertEnum
	private PriorityEnum autoCreateSuccessPriority;

	@RestItemName(value = MessageConstant.SDML_AUTO_ENABLE_FINISHED)
	@RestBeanConvertEnum
	private PriorityEnum autoEnableSuccessPriority;

	@RestItemName(value = MessageConstant.SDML_AUTO_DISABLE_FINISHED)
	@RestBeanConvertEnum
	private PriorityEnum autoDisableSuccessPriority;

	@RestItemName(value = MessageConstant.SDML_AUTO_UPDATE_FINISHED)
	@RestBeanConvertEnum
	private PriorityEnum autoUpdateSuccessPriority;

	@RestItemName(value = MessageConstant.SDML_AUTO_CONTROL_FAILED)
	@RestBeanConvertEnum
	private PriorityEnum autoControlFailedPriority;

	private List<SdmlMonitorNotifyRelationRequest> sdmlMonitorNotifyRelationList = new ArrayList<>();
	private List<NotifyRelationInfoRequest> notifyRelationList = new ArrayList<>();
	private List<NotifyRelationInfoRequest> autoMonitorCommonNotifyRelationList = new ArrayList<>();

	public ModifySdmlControlSettingRequest() {
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public Boolean getControlLogCollectFlg() {
		return controlLogCollectFlg;
	}

	public void setControlLogCollectFlg(Boolean controlLogCollectFlg) {
		this.controlLogCollectFlg = controlLogCollectFlg;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public Boolean getValidFlg() {
		return validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	public Boolean getAutoMonitorDeleteFlg() {
		return autoMonitorDeleteFlg;
	}

	public void setAutoMonitorDeleteFlg(Boolean autoMonitorDeleteFlg) {
		this.autoMonitorDeleteFlg = autoMonitorDeleteFlg;
	}

	public String getAutoMonitorCalendarId() {
		return autoMonitorCalendarId;
	}

	public void setAutoMonitorCalendarId(String autoMonitorCalendarId) {
		this.autoMonitorCalendarId = autoMonitorCalendarId;
	}

	public Integer getEarlyStopThresholdSecond() {
		return earlyStopThresholdSecond;
	}

	public void setEarlyStopThresholdSecond(Integer earlyStopThresholdSecond) {
		this.earlyStopThresholdSecond = earlyStopThresholdSecond;
	}

	public PriorityEnum getEarlyStopNotifyPriority() {
		return earlyStopNotifyPriority;
	}

	public void setEarlyStopNotifyPriority(PriorityEnum earlyStopNotifyPriority) {
		this.earlyStopNotifyPriority = earlyStopNotifyPriority;
	}

	public PriorityEnum getAutoCreateSuccessPriority() {
		return autoCreateSuccessPriority;
	}

	public void setAutoCreateSuccessPriority(PriorityEnum autoCreateSuccessPriority) {
		this.autoCreateSuccessPriority = autoCreateSuccessPriority;
	}

	public PriorityEnum getAutoEnableSuccessPriority() {
		return autoEnableSuccessPriority;
	}

	public void setAutoEnableSuccessPriority(PriorityEnum autoEnableSuccessPriority) {
		this.autoEnableSuccessPriority = autoEnableSuccessPriority;
	}

	public PriorityEnum getAutoDisableSuccessPriority() {
		return autoDisableSuccessPriority;
	}

	public void setAutoDisableSuccessPriority(PriorityEnum autoDisableSuccessPriority) {
		this.autoDisableSuccessPriority = autoDisableSuccessPriority;
	}

	public PriorityEnum getAutoUpdateSuccessPriority() {
		return autoUpdateSuccessPriority;
	}

	public void setAutoUpdateSuccessPriority(PriorityEnum autoUpdateSuccessPriority) {
		this.autoUpdateSuccessPriority = autoUpdateSuccessPriority;
	}

	public PriorityEnum getAutoControlFailedPriority() {
		return autoControlFailedPriority;
	}

	public void setAutoControlFailedPriority(PriorityEnum autoControlFailedPriority) {
		this.autoControlFailedPriority = autoControlFailedPriority;
	}

	public List<SdmlMonitorNotifyRelationRequest> getSdmlMonitorNotifyRelationList() {
		return sdmlMonitorNotifyRelationList;
	}

	public void setSdmlMonitorNotifyRelationList(List<SdmlMonitorNotifyRelationRequest> sdmlMonitorNotifyRelationList) {
		this.sdmlMonitorNotifyRelationList = sdmlMonitorNotifyRelationList;
	}

	public List<NotifyRelationInfoRequest> getNotifyRelationList() {
		return notifyRelationList;
	}

	public void setNotifyRelationList(List<NotifyRelationInfoRequest> notifyRelationList) {
		this.notifyRelationList = notifyRelationList;
	}

	public List<NotifyRelationInfoRequest> getAutoMonitorCommonNotifyRelationList() {
		return autoMonitorCommonNotifyRelationList;
	}

	public void setAutoMonitorCommonNotifyRelationList(
			List<NotifyRelationInfoRequest> autoMonitorCommonNotifyRelationList) {
		this.autoMonitorCommonNotifyRelationList = autoMonitorCommonNotifyRelationList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

	// applicationIdが必要なためこちらを使用すること
	public void correlationCheck(String applicationId) throws InvalidSetting, InvalidRole, SdmlControlSettingNotFound {
		// クライアントからはオーナーロールIDが来ないので最新の情報から取得して設定
		String ownerRoleId = QueryUtil.getSdmlControlSettingInfoPK(applicationId).getOwnerRoleId();

		// facilityId
		try {
			FacilityTreeCache.validateFacilityId(getFacilityId(), ownerRoleId, false);
		} catch (Exception e) {
			throw new InvalidSetting(e.getMessage(), e);
		}

		// notifyId
		if (getNotifyRelationList() != null && getNotifyRelationList().size() > 0) {
			for (NotifyRelationInfoRequest notifyRelation : getNotifyRelationList()) {
				try {
					CommonValidator.validateNotifyId(notifyRelation.getNotifyId(), true, ownerRoleId);
				} catch (Exception e) {
					throw new InvalidSetting(e.getMessage(), e);
				}
			}
		}

		// autoMonitorCalendarId
		try {
			CommonValidator.validateCalenderId(getAutoMonitorCalendarId(), false, ownerRoleId);
		} catch (Exception e) {
			throw new InvalidSetting(e.getMessage(), e);
		}

		// autoMonitorCommonNotifyGroupId
		if (getAutoMonitorCommonNotifyRelationList() != null && getAutoMonitorCommonNotifyRelationList().size() > 0) {
			for (NotifyRelationInfoRequest notifyRelation : getAutoMonitorCommonNotifyRelationList()) {
				try {
					CommonValidator.validateNotifyId(notifyRelation.getNotifyId(), true, ownerRoleId);
				} catch (Exception e) {
					throw new InvalidSetting(e.getMessage(), e);
				}
			}
		}

		// sdmlMonitorNotifyRelationList
		if (getSdmlMonitorNotifyRelationList() != null && getSdmlMonitorNotifyRelationList().size() > 0) {
			List<String> checkedList = new ArrayList<>();
			for (SdmlMonitorNotifyRelationRequest relationInfo : getSdmlMonitorNotifyRelationList()) {
				if (checkedList.contains(relationInfo.getSdmlMonitorTypeId())) {
					// 重複NG
					InvalidSetting e = new InvalidSetting(
							"SdmlMonitorTypeId(" + relationInfo.getSdmlMonitorTypeId() + ") is duplicated");
					throw e;
				}
				relationInfo.correlationCheck(ownerRoleId);
				checkedList.add(relationInfo.getSdmlMonitorTypeId());
			}
		}
	}
}
