/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.plugin.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.infra.factory.SelectInfraManagement;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.notify.util.INotifyOwnerDeterminer;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.jcraft.jsch.JSch;

/**
 * 環境構築機能の初期化を行う。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraPlugin implements HinemosPlugin {
//	private static final Logger logger = Logger.getLogger(InfraPlugin.class);
	
	public static class NotifyOwnerDeterminerImpl implements INotifyOwnerDeterminer {
		@Override
		public String getEventOwnerRoleId(String monitorId, String monitorDetailId, String pluginId, String facilityId) {
			InfraManagementInfo management = null;
			try {
				management = new SelectInfraManagement().get(monitorId, ObjectPrivilegeMode.READ);
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
			}
			return management != null ? management.getOwnerRoleId(): null;
		}

		@Override
		public String getStatusOwnerRoleId(String monitorId, String monitorDetailId, String pluginId, String facilityId) {
			InfraManagementInfo management = null;
			try {
				management = new SelectInfraManagement().get(monitorId, ObjectPrivilegeMode.READ);
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
			}
			return management != null ? management.getOwnerRoleId(): null;
		}
	}
	
	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(AsyncWorkerPlugin.class.getName());
		return dependency;
	}

	@Override
	public void create() {
		// サーバー証明書の認証をキャンセルする
		JSch.setConfig("StrictHostKeyChecking", "no");
	}

	@Override
	public void activate() {
	}

	@Override
	public void deactivate() {
	}

	@Override
	public void destroy() {

	}
}
