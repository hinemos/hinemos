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

public class BackupedDataResponse {
	private List<BackupedDataEntryResponse> entries = new ArrayList<>();

	public BackupedDataResponse() {
	}

	public List<BackupedDataEntryResponse> getEntries() {
		return entries;
	}

	public void setEntries(List<BackupedDataEntryResponse> entries) {
		this.entries = entries;
	}

}
