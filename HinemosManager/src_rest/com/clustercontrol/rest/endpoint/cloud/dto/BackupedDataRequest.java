/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class BackupedDataRequest implements RequestDto {
	private List<BackupedDataEntryRequest> entries = new ArrayList<>();

	public BackupedDataRequest() {
	}

	public List<BackupedDataEntryRequest> getEntries() {
		return entries;
	}

	public void setEntries(List<BackupedDataEntryRequest> entries) {
		this.entries = entries;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
