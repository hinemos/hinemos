/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.dto;

import java.util.List;

import com.clustercontrol.rest.endpoint.cloud.dto.AddCloudLoginUserRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.AddCloudScopeRequest;
import com.clustercontrol.rest.endpoint.cloud.dto.ModifyBillingSettingRequest;

public class ImportCloudScopeRecordRequest extends AbstractImportRecordRequest<AddCloudScopeRequest> {

	// サブユーザー情報(メインは AddCloudScopeRequest 内に保持)
	private List<AddCloudLoginUserRequest> subUserList;
	
	// ユーザーの優先順情報
	private List<String> priorityArrayList;

	// 課金詳細情報
	private ModifyBillingSettingRequest billingSetting;
	
	
	public ImportCloudScopeRecordRequest(){
		
	}

	public List<AddCloudLoginUserRequest> getSubUserList() {
		return subUserList;
	}

	public void setSubUserList(List<AddCloudLoginUserRequest> subUserList) {
		this.subUserList = subUserList;
	}

	public ModifyBillingSettingRequest getBillingSetting() {
		return billingSetting;
	}

	public void setBillingSetting(ModifyBillingSettingRequest billingSetting) {
		this.billingSetting = billingSetting;
	}

	public List<String> getPriorityArrayList() {
		return priorityArrayList;
	}

	public void setPriorityArrayList(List<String> priorityArrayList) {
		this.priorityArrayList = priorityArrayList;
	}
	

}

