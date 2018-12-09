/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.views;

import com.clustercontrol.ws.access.RoleInfo;
import com.clustercontrol.xcloud.model.cloud.IHinemosManager;

public class HinemosRole {
	IHinemosManager manager;
	RoleInfo roleInfo;
	
	public IHinemosManager getManager() {
		return manager;
	}
	public void setManager(IHinemosManager manager) {
		this.manager = manager;
	}
	
	public RoleInfo getRoleInfo() {
		return roleInfo;
	}
	public void setRoleInfo(RoleInfo roleInfo) {
		this.roleInfo = roleInfo;
	}
}
