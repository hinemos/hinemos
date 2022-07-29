/* Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateCollection;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class DeleteStorageSnapshotRequest implements RequestDto {
	@RestValidateString(notNull=true, minLen=1)
	@RestItemName(MessageConstant.XCLOUD_CORE_STORAGE_IDS)
	private String storageId;
	@RestValidateCollection(notNull=true, minSize=1)
	@RestItemName(MessageConstant.XCLOUD_CORE_STORAGESNAPSHOT_IDS)
	private List<String> storageSnapshotIds;


	public String getStorageId() {
		return storageId;
	}

	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}

	public List<String> getStorageSnapshotIds() {
		return storageSnapshotIds;
	}

	public void setStorageSnapshotIds(List<String> storageSnapshotIds) {
		this.storageSnapshotIds = storageSnapshotIds;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
