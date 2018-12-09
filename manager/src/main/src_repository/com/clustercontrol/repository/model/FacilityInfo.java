/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.util.HinemosTime;


/**
 * The persistent class for the cc_cfg_facility database table.
 *
 */

@Entity
@Table(name="cc_cfg_facility", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.PLATFORM_REPOSITORY,
		isModifyCheck=true)
@DiscriminatorColumn(name="facility_type", discriminatorType=DiscriminatorType.INTEGER)
@DiscriminatorValue(value="2")
@Inheritance(strategy=InheritanceType.JOINED)
@AttributeOverride(name="objectId",
		column=@Column(name="facility_id", insertable=false, updatable=false))
public class FacilityInfo extends ObjectPrivilegeTargetInfo implements Cloneable {
	private static final long serialVersionUID = 1L;
	private String facilityId = "";
	private String facilityName = "";
	private Integer facilityType = FacilityConstant.TYPE_NODE;
	private String description = "";
	private Integer displaySortOrder = 100;
	private String iconImage = "";
	private Boolean valid = Boolean.TRUE;
	private String createUserId = "";
	private Long createDatetime = HinemosTime.currentTimeMillis();
	private String modifyUserId = "";
	private Long modifyDatetime = HinemosTime.currentTimeMillis();
	/**スコープがビルトインかのフラグ*/
	private Boolean builtInFlg = false;

	/** 参照フラグ（true：参照権限のないスコープ） */
	private Boolean notReferFlg = true;
	
	public FacilityInfo() {
	}

	public FacilityInfo(String facilityId) {
		this.setFacilityId(facilityId);
	}
	
	@Id
	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
		setObjectId(facilityId);
	}


	@Column(name="create_datetime")
	public Long getCreateDatetime() {
		return this.createDatetime;
	}

	public void setCreateDatetime(Long createDatetime) {
		this.createDatetime = createDatetime;
	}


	@Column(name="create_user_id")
	public String getCreateUserId() {
		return this.createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	@Column(name="description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Column(name="display_sort_order")
	public Integer getDisplaySortOrder() {
		return this.displaySortOrder;
	}

	public void setDisplaySortOrder(Integer displaySortOrder) {
		this.displaySortOrder = displaySortOrder;
	}


	@Column(name="facility_name")
	public String getFacilityName() {
		return this.facilityName;
	}

	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}


	@Column(name="facility_type")
	public Integer getFacilityType() {
		return this.facilityType;
	}

	public void setFacilityType(Integer facilityType) {
		this.facilityType = facilityType;
	}

	@Column(name="icon_image")
	public String getIconImage() {
		return this.iconImage;
	}

	public void setIconImage(String iconImage) {
		this.iconImage = iconImage;
	}

	@Column(name="modify_datetime")
	public Long getModifyDatetime() {
		return this.modifyDatetime;
	}

	public void setModifyDatetime(Long modifyDatetime) {
		this.modifyDatetime = modifyDatetime;
	}


	@Column(name="modify_user_id")
	public String getModifyUserId() {
		return this.modifyUserId;
	}

	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}

	@Column(name="valid")
	public Boolean getValid() {
		return this.valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}
	
	@Transient
	public Boolean getBuiltInFlg() {
		if (builtInFlg == null)
			builtInFlg = FacilitySelector.isBuildinScope(this);
		return builtInFlg;
	}

	public void setBuiltInFlg(Boolean builtInFlg) {
		this.builtInFlg = builtInFlg;
	}
	
	@Transient
	public Boolean isNotReferFlg() {
		return this.notReferFlg;
	}

	public void setNotReferFlg(Boolean notReferFlg) {
		this.notReferFlg = notReferFlg;
	}
	
	@Override
	public FacilityInfo clone(){
		FacilityInfo cloneInfo = null;
		try {
			cloneInfo = (FacilityInfo) super.clone();
			cloneInfo.setFacilityId(getFacilityId());
			cloneInfo.facilityName = this.facilityName;
			cloneInfo.facilityType = this.facilityType;
			cloneInfo.description = this.description;
			cloneInfo.displaySortOrder = this.displaySortOrder;
			cloneInfo.iconImage = this.iconImage;
			cloneInfo.valid = this.valid;
			cloneInfo.createUserId = this.createUserId;
			cloneInfo.createDatetime = this.createDatetime;
			cloneInfo.modifyUserId = this.modifyUserId;
			cloneInfo.modifyDatetime = this.modifyDatetime;
			cloneInfo.builtInFlg = getBuiltInFlg();
			cloneInfo.setOwnerRoleId(getOwnerRoleId());
			cloneInfo.notReferFlg = this.notReferFlg;
			cloneInfo.tranSetUncheckFlg(this.tranGetUncheckFlg());
		} catch (CloneNotSupportedException e) {
			// do nothing
		}
		return cloneInfo;
	}
	
	protected void preparePersisting() {
	}
	
	public void persistSelf() {
		preparePersisting();
	}
}