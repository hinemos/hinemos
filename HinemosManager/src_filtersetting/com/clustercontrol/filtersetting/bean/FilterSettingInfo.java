/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.bean;

import com.clustercontrol.filtersetting.entity.FilterEntity;
import com.clustercontrol.filtersetting.entity.FilterEntityPK;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;

/**
 * フィルタ設定情報。
 */
public class FilterSettingInfo {

	private String filterId;
	private String filterName;
	private Boolean common;
	private String ownerRoleId;
	private String ownerUserId;
	private String objectId;
	private FilterCategoryEnum filterCategory;
	private EventFilterBaseInfo eventFilter;
	private StatusFilterBaseInfo statusFilter;
	private JobHistoryFilterBaseInfo jobHistoryFilter;

	public FilterSettingInfo() {
	}

	public FilterSettingInfo(FilterEntity entity) {
		this.filterId = entity.getId().getFilterId();
		this.filterName = entity.getFilterName();

		if (FilterOwner.isCommon(entity)) {
			this.common = Boolean.TRUE;
			this.ownerRoleId = entity.getOwnerRoleId();
			this.ownerUserId = null;
			this.objectId = entity.getObjectId();
		} else {
			this.common = Boolean.FALSE;
			this.ownerRoleId = null;
			this.ownerUserId = entity.getId().getFilterOwner();
			this.objectId = null;
		}

		this.filterCategory = FilterCategoryEnum.fromCode(entity.getId().getFilterCategory());

		switch (this.filterCategory) {
		case EVENT:
			eventFilter = new EventFilterBaseInfo(entity);
			break;
		case JOB_HISTORY:
			jobHistoryFilter = new JobHistoryFilterBaseInfo(entity);
			break;
		case STATUS:
			statusFilter = new StatusFilterBaseInfo(entity);
			break;
		}
	}

	public FilterEntityPK getEntityPK() {
		return new FilterEntityPK(
				filterCategory.getCode(),
				FilterOwner.resolve(common.booleanValue(), ownerUserId),
				filterId);
	}

	/**
	 * このオブジェクトの情報を {@link FilterEntity} へ変換して返します。
	 */
	public FilterEntity toEntity() {

		FilterEntity entity = new FilterEntity(
				getEntityPK(),
				filterName,
				new FilterSettingObjectId(filterCategory, common, filterId).toDbValue(),
				(common.booleanValue() ? ownerRoleId : null),
				null,  // facilityId
				null,  // facilityTarget
				null,  // filterRange
				null, null, null, null,  // regUser, regDate, updateUser, updateDate
				null);  // conditions

		switch (this.filterCategory) {
		case EVENT:
			eventFilter.writeTo(entity);
			break;
		case JOB_HISTORY:
			jobHistoryFilter.writeTo(entity);
			break;
		case STATUS:
			statusFilter.writeTo(entity);
			break;
		}

		return entity;
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

	public EventFilterBaseInfo getEventFilter() {
		return eventFilter;
	}

	public void setEventFilter(EventFilterBaseInfo eventFilter) {
		this.eventFilter = eventFilter;
	}

	public StatusFilterBaseInfo getStatusFilter() {
		return statusFilter;
	}

	public void setStatusFilter(StatusFilterBaseInfo statusFilter) {
		this.statusFilter = statusFilter;
	}

	public JobHistoryFilterBaseInfo getJobHistoryFilter() {
		return jobHistoryFilter;
	}

	public void setJobHistoryFilter(JobHistoryFilterBaseInfo jobHistoryFilter) {
		this.jobHistoryFilter = jobHistoryFilter;
	}

}
