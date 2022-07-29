/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.dto;

import com.clustercontrol.rest.endpoint.jobmanagement.dto.GetEditLockRequest;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.JobTreeItemRequest;

public class ImportJobMasterRecordRequest extends AbstractImportRecordRequest<JobTreeItemRequest> {
	private GetEditLockRequest editLockData ;

	public ImportJobMasterRecordRequest() {
	}

	public GetEditLockRequest getEditLockData() {
		return editLockData;
	}

	public void setEditLockData(GetEditLockRequest editLockData) {
		this.editLockData = editLockData;
	}
	

}
