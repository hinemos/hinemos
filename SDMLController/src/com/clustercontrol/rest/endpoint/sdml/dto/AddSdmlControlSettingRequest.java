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
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoRequest;
import com.clustercontrol.rest.endpoint.sdml.dto.enumtype.PriorityEnum;
import com.clustercontrol.util.MessageConstant;

public class AddSdmlControlSettingRequest implements RequestDto {

	@RestItemName(value = MessageConstant.OWNER_ROLE_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64)
	private String ownerRoleId;

	@RestItemName(value = MessageConstant.APPLICATION_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64, type = CheckType.ID)
	private String applicationId;

	@RestItemName(value = MessageConstant.DESCRIPTION)
	@RestValidateString(maxLen = 256)
	private String description;

	@RestItemName(value = MessageConstant.SCOPE)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 512)
	private String facilityId;

	@RestItemName(value = MessageConstant.DIRECTORY)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 1024)
	private String controlLogDirectory;

	@RestItemName(value = MessageConstant.FILE_NAME)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 1024)
	private String controlLogFilename;

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

	public AddSdmlControlSettingRequest() {
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
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

	public String getControlLogDirectory() {
		return controlLogDirectory;
	}

	public void setControlLogDirectory(String controlLogDirectory) {
		this.controlLogDirectory = controlLogDirectory;
	}

	public String getControlLogFilename() {
		return controlLogFilename;
	}

	public void setControlLogFilename(String controlLogFilename) {
		this.controlLogFilename = controlLogFilename;
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
		// ownerRoleId
		CommonValidator.validateOwnerRoleId(getOwnerRoleId(), true, getApplicationId(),
				HinemosModuleConstant.SDML_CONTROL);

		// facilityId
		try {
			FacilityTreeCache.validateFacilityId(getFacilityId(), getOwnerRoleId(), false);
		} catch (Exception e) {
			throw new InvalidSetting(e.getMessage(), e);
		}

		// notifyId
		if (getNotifyRelationList() != null && getNotifyRelationList().size() > 0) {
			for (NotifyRelationInfoRequest notifyRelation : getNotifyRelationList()) {
				try {
					CommonValidator.validateNotifyId(notifyRelation.getNotifyId(), true, getOwnerRoleId());
				} catch (Exception e) {
					throw new InvalidSetting(e.getMessage(), e);
				}
			}
		}

		// autoMonitorCalendarId
		try {
			CommonValidator.validateCalenderId(getAutoMonitorCalendarId(), false, getOwnerRoleId());
		} catch (Exception e) {
			throw new InvalidSetting(e.getMessage(), e);
		}

		// autoMonitorCommonNotifyGroupId
		if (getAutoMonitorCommonNotifyRelationList() != null && getAutoMonitorCommonNotifyRelationList().size() > 0) {
			for (NotifyRelationInfoRequest notifyRelation : getAutoMonitorCommonNotifyRelationList()) {
				try {
					CommonValidator.validateNotifyId(notifyRelation.getNotifyId(), true, getOwnerRoleId());
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
				relationInfo.correlationCheck(getOwnerRoleId());
				checkedList.add(relationInfo.getSdmlMonitorTypeId());
			}
		}
	}
}
