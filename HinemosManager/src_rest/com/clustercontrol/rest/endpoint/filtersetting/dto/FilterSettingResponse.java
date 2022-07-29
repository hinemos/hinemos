/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.filtersetting.dto;

import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;

public abstract class FilterSettingResponse {

	private String filterId;
	private String filterName;
	private Boolean common;
	private String ownerRoleId;
	private String ownerUserId;
	private String objectId;
	private FilterCategoryEnum filterCategory;

	public String getFilterId() {
		return filterId;
	}

	public void setFilterId(String filterId) {
		this.filterId = filterId;
	}

	public String getFilterName() {
		return filterName;
	}

	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}

	public Boolean getCommon() {
		return common;
	}

	public void setCommon(Boolean common) {
		this.common = common;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public String getOwnerUserId() {
		return ownerUserId;
	}

	public void setOwnerUserId(String ownerUserId) {
		this.ownerUserId = ownerUserId;
	}

	public FilterCategoryEnum getFilterCategory() {
		return filterCategory;
	}

	public void setFilterCategory(FilterCategoryEnum filterCategory) {
		this.filterCategory = filterCategory;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
}
