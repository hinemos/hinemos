/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.filtersetting.dto.EventFilterBaseRequest;

public class DownloadEventFileRequest implements RequestDto {

	private EventFilterBaseRequest filter;

	private List<EventSelectionRequest> selectedEvents;

	@RestValidateString(notNull = true, minLen = 1, maxLen = 200) //TODO 定数
	private String filename;

	public DownloadEventFileRequest() {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		if (filter != null) {
			filter.correlationCheck();
		}
		if (selectedEvents != null) {
			for (EventSelectionRequest it : selectedEvents) {
				it.correlationCheck();
			}
		}
	}

	public EventFilterBaseRequest getFilter() {
		return filter;
	}

	public void setFilter(EventFilterBaseRequest filter) {
		this.filter = filter;
	}

	public String getFilename() {
		return this.filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public List<EventSelectionRequest> getSelectedEvents() {
		return selectedEvents;
	}

	public void setSelectedEvents(List<EventSelectionRequest> selectedEvents) {
		this.selectedEvents = selectedEvents;
	}

}
