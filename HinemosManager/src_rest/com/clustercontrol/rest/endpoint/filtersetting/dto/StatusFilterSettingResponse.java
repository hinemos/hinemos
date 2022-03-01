/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.filtersetting.dto;

import com.clustercontrol.filtersetting.bean.FilterSettingInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = FilterSettingInfo.class)
public class StatusFilterSettingResponse extends FilterSettingResponse {

	private StatusFilterBaseResponse statusFilter;

	public StatusFilterBaseResponse getStatusFilter() {
		return statusFilter;
	}

	public void setStatusFilter(StatusFilterBaseResponse statusFilter) {
		this.statusFilter = statusFilter;
	}

}
