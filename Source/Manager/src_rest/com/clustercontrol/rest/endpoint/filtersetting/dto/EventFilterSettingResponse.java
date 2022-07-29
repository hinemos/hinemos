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
public class EventFilterSettingResponse extends FilterSettingResponse {

	private EventFilterBaseResponse eventFilter;

	public EventFilterBaseResponse getEventFilter() {
		return eventFilter;
	}

	public void setEventFilter(EventFilterBaseResponse eventFilter) {
		this.eventFilter = eventFilter;
	}
}
