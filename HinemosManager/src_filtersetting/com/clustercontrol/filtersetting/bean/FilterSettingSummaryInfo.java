/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.bean;

import com.clustercontrol.filtersetting.entity.FilterEntity;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;

/**
 * フィルタ設定の概要情報(一覧表示のための要約情報)です。
 */
public class FilterSettingSummaryInfo {

	private FilterCategoryEnum filterCategory;
	private String filterId;
	private String filterName;
	private Boolean common;
	private String ownerRoleId;
	private String ownerUserId;
	private String objectId;

	public FilterSettingSummaryInfo() {
		// NOP
	}

	public FilterSettingSummaryInfo(FilterEntity entity) {
		this.filterCategory = FilterCategoryEnum.fromCode(entity.getId().getFilterCategory());
		this.filterId = entity.getId().getFilterId();
		this.filterName = entity.getFilterName();
		this.common = FilterOwner.isCommon(entity.getId().getFilterOwner());
		this.ownerRoleId = entity.getOwnerRoleId();
		this.ownerUserId = FilterOwner.toOwnerUserId(entity.getId().getFilterOwner());
		this.objectId = entity.getObjectId();
	}

	public FilterCategoryEnum getFilterCategory() {
		return filterCategory;
	}

	public void setFilterCategory(FilterCategoryEnum filterCategory) {
		this.filterCategory = filterCategory;
	}

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

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

}
