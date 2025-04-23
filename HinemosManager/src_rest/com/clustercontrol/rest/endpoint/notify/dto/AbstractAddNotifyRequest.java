/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.util.MessageConstant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"notifyType"})
public abstract class AbstractAddNotifyRequest extends AbstractNotifyRequest {

	@RestItemName(value = MessageConstant.NOTIFY_ID)
	protected String notifyId;

	@RestItemName(value = MessageConstant.OWNER_ROLE_ID)
	protected String ownerRoleId;

	public AbstractAddNotifyRequest() {
	}

	public String getNotifyId() {
		return notifyId;
	}

	public void setNotifyId(String notifyId) {
		this.notifyId = notifyId;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
}