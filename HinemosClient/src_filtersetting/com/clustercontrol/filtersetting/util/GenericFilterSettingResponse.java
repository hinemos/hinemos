/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.filtersetting.util;

import org.openapitools.client.model.EventFilterBaseResponse;
import org.openapitools.client.model.EventFilterSettingResponse;
import org.openapitools.client.model.JobHistoryFilterBaseResponse;
import org.openapitools.client.model.JobHistoryFilterSettingResponse;
import org.openapitools.client.model.StatusFilterBaseResponse;
import org.openapitools.client.model.StatusFilterSettingResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.rest.endpoint.filtersetting.dto.enumtype.FilterCategoryEnum;
import com.clustercontrol.util.RestClientBeanUtil;

/**
 * クライアント側で処理を共通化するために利用するレスポンスオブジェクトの汎用クラス
 *
 */
public class GenericFilterSettingResponse {
	private String filterId;
	private String filterName;
	private Boolean common;
	private String ownerRoleId;
	private String ownerUserId;
	private String objectId;
	private FilterCategoryEnum filterCategory;
	private EventFilterBaseResponse eventFilter;
	private StatusFilterBaseResponse statusFilter;
	private JobHistoryFilterBaseResponse jobHistoryFilter;

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

	public EventFilterBaseResponse getEventFilter() {
		return eventFilter;
	}

	public void setEventFilter(EventFilterBaseResponse eventFilter) {
		this.eventFilter = eventFilter;
	}

	public StatusFilterBaseResponse getStatusFilter() {
		return statusFilter;
	}

	public void setStatusFilter(StatusFilterBaseResponse statusFilter) {
		this.statusFilter = statusFilter;
	}

	public JobHistoryFilterBaseResponse getJobHistoryFilter() {
		return jobHistoryFilter;
	}

	public void setJobHistoryFilter(JobHistoryFilterBaseResponse jobHistoryFilter) {
		this.jobHistoryFilter = jobHistoryFilter;
	}

	/**
	 * EventFilterSettingResponse から CommonFilterSettingResponse へ変換して返します。
	 */
	public static GenericFilterSettingResponse fromEventResponse(EventFilterSettingResponse from) {
		GenericFilterSettingResponse to = new GenericFilterSettingResponse();
		try {
			RestClientBeanUtil.convertBean(from, to);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to convert EventFilterSettingResponse to CommonFilterSettingResponse.\n"
					+ "res=" + from.toString() + "\nreq=" + to.toString());
		}
		return to;
	}
	
	/**
	 * StatusFilterSettingResponse から CommonFilterSettingResponse へ変換して返します。
	 */
	public static GenericFilterSettingResponse fromStatusResponse(StatusFilterSettingResponse from) {
		GenericFilterSettingResponse to = new GenericFilterSettingResponse();
		try {
			RestClientBeanUtil.convertBean(from, to);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to convert StatusFilterSettingResponse to CommonFilterSettingResponse.\n"
					+ "res=" + from.toString() + "\nreq=" + to.toString());
		}
		return to;
	}
	
	/**
	 * JobHistoryFilterSettingResponse から CommonFilterSettingResponse へ変換して返します。
	 */
	public static GenericFilterSettingResponse fromJobHistoryResponse(JobHistoryFilterSettingResponse from) {
		GenericFilterSettingResponse to = new GenericFilterSettingResponse();
		try {
			RestClientBeanUtil.convertBean(from, to);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to convert JobHistoryFilterSettingResponse to CommonFilterSettingResponse.\n"
					+ "res=" + from.toString() + "\nreq=" + to.toString());
		}
		return to;
	}
}
