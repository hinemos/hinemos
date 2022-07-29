/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.endpoint.cloud.dto.enumtype.TargetTypeEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class MakeRebootInstanceCommandRequest implements RequestDto {
	@RestItemName(MessageConstant.XCLOUD_CORE_CLOUDSCOPE_ID)
	@RestValidateString(notNull = true)
	private String cloudScopeId;
	@RestItemName(MessageConstant.XCLOUD_CORE_LOCATION_ID)
	private String locationId;
	@RestItemName(MessageConstant.XCLOUD_CORE_INSTANCE_IDS)
	private List<String> instanceIds;
	@RestItemName(MessageConstant.FACILITY_ID)
	private String facilityId;
	@RestBeanConvertEnum
	@RestValidateObject(notNull = true)
	private TargetTypeEnum type;
	
	public MakeRebootInstanceCommandRequest() {
	}
	public String getCloudScopeId() {
		return cloudScopeId;
	}
	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}
	public String getLocationId() {
		return locationId;
	}
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}
	public List<String> getInstanceIds() {
		return instanceIds;
	}
	public void setInstanceIds(List<String> instanceIds) {
		this.instanceIds = instanceIds;
	}
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	public TargetTypeEnum getType() {
		return type;
	}
	public void setType(TargetTypeEnum type) {
		this.type = type;
	}
	@Override
	public void correlationCheck() throws InvalidSetting {
		if (TargetTypeEnum.instance.equals(type)) {
			if (locationId == null) {
				throw new InvalidSetting("locationId is null.");
			} else if (instanceIds == null) {
				throw new InvalidSetting("instanceIds is null.");
			}
		} else if (TargetTypeEnum.facility.equals(type)) {
			if (facilityId == null) {
				throw new InvalidSetting("facilityId is null.");
			}
		} else {
			throw new InvalidSetting("unknown type : " + type);
		}
	}
}
