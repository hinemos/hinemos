/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.FacilityTypeEnum;

public class FacilityInfoResponse {

	private String facilityId;
	@RestPartiallyTransrateTarget
	private String facilityName;
	@RestBeanConvertEnum
	private FacilityTypeEnum facilityType;
	private String description;
	private Integer displaySortOrder;
	private String iconImage;
	private Boolean valid;
	private String createUserId;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String createDatetime;
	private String modifyUserId;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String modifyDatetime;
	private Boolean builtInFlg;
	private Boolean notReferFlg;

	private String ownerRoleId;

	public FacilityInfoResponse() {
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getFacilityName() {
		return facilityName;
	}

	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}

	public FacilityTypeEnum getFacilityType() {
		return facilityType;
	}

	public void setFacilityType(FacilityTypeEnum facilityType) {
		this.facilityType = facilityType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getDisplaySortOrder() {
		return displaySortOrder;
	}

	public void setDisplaySortOrder(Integer displaySortOrder) {
		this.displaySortOrder = displaySortOrder;
	}

	public String getIconImage() {
		return iconImage;
	}

	public void setIconImage(String iconImage) {
		this.iconImage = iconImage;
	}

	public Boolean getValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public String getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	public String getCreateDatetime() {
		return createDatetime;
	}

	public void setCreateDatetime(String createDatetime) {
		this.createDatetime = createDatetime;
	}

	public String getModifyUserId() {
		return modifyUserId;
	}

	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}

	public String getModifyDatetime() {
		return modifyDatetime;
	}

	public void setModifyDatetime(String modifyDatetime) {
		this.modifyDatetime = modifyDatetime;
	}

	public Boolean getBuiltInFlg() {
		return builtInFlg;
	}

	public void setBuiltInFlg(Boolean builtInFlg) {
		this.builtInFlg = builtInFlg;
	}

	public Boolean getNotReferFlg() {
		return notReferFlg;
	}

	public void setNotReferFlg(Boolean notReferFlg) {
		this.notReferFlg = notReferFlg;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
}
