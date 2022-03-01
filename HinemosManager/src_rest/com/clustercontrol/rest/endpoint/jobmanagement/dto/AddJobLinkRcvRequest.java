/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class AddJobLinkRcvRequest extends AbstractJobLinkRcvRequest implements RequestDto{

	/** 実行契機ID */
	@RestItemName(value=MessageConstant.JOBKICK_ID)
	private String id;

	/** オーナーロールID */
	@RestItemName(value=MessageConstant.OWNER_ROLE_ID)
	private String ownerRoleId;

	public AddJobLinkRcvRequest(){
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
}
