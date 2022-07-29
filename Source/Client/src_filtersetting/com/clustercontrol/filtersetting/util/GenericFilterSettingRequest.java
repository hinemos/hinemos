/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.filtersetting.util;

import org.openapitools.client.model.AddEventFilterSettingRequest;
import org.openapitools.client.model.AddJobHistoryFilterSettingRequest;
import org.openapitools.client.model.AddStatusFilterSettingRequest;
import org.openapitools.client.model.EventFilterBaseRequest;
import org.openapitools.client.model.JobHistoryFilterBaseRequest;
import org.openapitools.client.model.ModifyEventFilterSettingRequest;
import org.openapitools.client.model.ModifyJobHistoryFilterSettingRequest;
import org.openapitools.client.model.ModifyStatusFilterSettingRequest;
import org.openapitools.client.model.StatusFilterBaseRequest;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.RestClientBeanUtil;

/**
 * クライアント側で処理を共通化するために利用するリクエストオブジェクトの汎用クラス
 */
public class GenericFilterSettingRequest {

	private String filterId;
	private String filterName;
	private String ownerRoleId;
	private EventFilterBaseRequest eventFilter;
	private StatusFilterBaseRequest statusFilter;
	private JobHistoryFilterBaseRequest jobHistoryFilter;

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

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public EventFilterBaseRequest getEventFilter() {
		return eventFilter;
	}

	public void setEventFilter(EventFilterBaseRequest eventFilter) {
		this.eventFilter = eventFilter;
	}

	public StatusFilterBaseRequest getStatusFilter() {
		return statusFilter;
	}

	public void setStatusFilter(StatusFilterBaseRequest statusFilter) {
		this.statusFilter = statusFilter;
	}

	public JobHistoryFilterBaseRequest getJobHistoryFilter() {
		return jobHistoryFilter;
	}

	public void setJobHistoryFilter(JobHistoryFilterBaseRequest jobHistoryFilter) {
		this.jobHistoryFilter = jobHistoryFilter;
	}

	/**
	 * CommonFilterSettingRequest から AddEventFilterSettingRequest へ変換して返します。
	 */
	public static AddEventFilterSettingRequest toAddEventRequest(GenericFilterSettingRequest from) {
		AddEventFilterSettingRequest to = new AddEventFilterSettingRequest();
		try {
			RestClientBeanUtil.convertBean(from, to);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to convert CommonFilterSettingRequest to AddEventFilterSettingRequest.\n"
					+ "res=" + from.toString() + "\nreq=" + to.toString());
		}
		return to;
	}
	
	/**
	 * CommonFilterSettingRequest から AddStatusFilterSettingRequest へ変換して返します。
	 */
	public static AddStatusFilterSettingRequest toAddStatusRequest(GenericFilterSettingRequest from) {
		AddStatusFilterSettingRequest to = new AddStatusFilterSettingRequest();
		try {
			RestClientBeanUtil.convertBean(from, to);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to convert CommonFilterSettingRequest to AddStatusFilterSettingRequest.\n"
					+ "res=" + from.toString() + "\nreq=" + to.toString());
		}
		return to;
	}
	
	/**
	 * CommonFilterSettingRequest から AddJobHistoryFilterSettingRequest へ変換して返します。
	 */
	public static AddJobHistoryFilterSettingRequest toAddJobHistoryRequest(GenericFilterSettingRequest from) {
		AddJobHistoryFilterSettingRequest to = new AddJobHistoryFilterSettingRequest();
		try {
			RestClientBeanUtil.convertBean(from, to);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to convert CommonFilterSettingRequest to AddJobHistoryFilterSettingRequest.\n"
					+ "res=" + from.toString() + "\nreq=" + to.toString());
		}
		return to;
	}
	
	/**
	 * CommonFilterSettingRequest から ModifyEventFilterSettingRequest へ変換して返します。
	 */
	public static ModifyEventFilterSettingRequest toModifyEventRequest(GenericFilterSettingRequest from) {
		ModifyEventFilterSettingRequest to = new ModifyEventFilterSettingRequest();
		try {
			RestClientBeanUtil.convertBean(from, to);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to convert CommonFilterSettingRequest to ModifyEventFilterSettingRequest.\n"
					+ "res=" + from.toString() + "\nreq=" + to.toString());
		}
		return to;
	}
	
	/**
	 * CommonFilterSettingRequest から ModifyStatusFilterSettingRequest へ変換して返します。
	 */
	public static ModifyStatusFilterSettingRequest toModifyStatusRequest(GenericFilterSettingRequest from) {
		ModifyStatusFilterSettingRequest to = new ModifyStatusFilterSettingRequest();
		try {
			RestClientBeanUtil.convertBean(from, to);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to convert CommonFilterSettingRequest to ModifyStatusFilterSettingRequest.\n"
					+ "res=" + from.toString() + "\nreq=" + to.toString());
		}
		return to;
	}
	
	/**
	 * CommonFilterSettingRequest から ModifyJobHistoryFilterSettingRequest へ変換して返します。
	 */
	public static ModifyJobHistoryFilterSettingRequest toModifyJobHistoryRequest(GenericFilterSettingRequest from) {
		ModifyJobHistoryFilterSettingRequest to = new ModifyJobHistoryFilterSettingRequest();
		try {
			RestClientBeanUtil.convertBean(from, to);
		} catch (HinemosUnknown e) {
			// logic error
			throw new RuntimeException("Failed to convert CommonFilterSettingRequest to ModifyJobHistoryFilterSettingRequest.\n"
					+ "res=" + from.toString() + "\nreq=" + to.toString());
		}
		return to;
	}
}
