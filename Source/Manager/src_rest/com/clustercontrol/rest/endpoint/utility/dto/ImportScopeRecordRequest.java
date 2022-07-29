/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.rest.endpoint.repository.dto.AddNodeRequest;
import com.clustercontrol.rest.endpoint.repository.dto.AddScopeRequest;

/**
 * スコープのインポート向けDto
 * 
 * スコープ情報（AddScopeRequest継承項目）と割付ノード情報（facilityIdList）をまとめて保持
 * 
 * @see com.clustercontrol.rest.endpoint.repository.dto.AddScopeRequest 
 * @see com.clustercontrol.rest.endpoint.repository.dto.AssignNodeScopeRequest 
 *
 */

public class ImportScopeRecordRequest extends AbstractImportRecordRequest<AddScopeRequest> {
	List<String> assignFacilityIdList = new ArrayList<>();
	public ImportScopeRecordRequest() {
	}
	public List<String> getAssignFacilityIdList() {
		return assignFacilityIdList;
	}

	public void setAssignFacilityIdList(List<String> assignFacilityIdList) {
		this.assignFacilityIdList = assignFacilityIdList;
	}
}
