/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
				management = new SelectInfraManagement().get(monitorId, null, ObjectPrivilegeMode.READ);
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
			}
			return management != null ? management.getOwnerRoleId(): null;
		}

		@Override
		public String getStatusOwnerRoleId(String monitorId, String monitorDetailId, String pluginId, String facilityId) {
			InfraManagementInfo management = null;
			try {
				management = new SelectInfraManagement().get(monitorId, null, ObjectPrivilegeMode.READ);
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
	public Set<String> getRequiredKeys() {
		return null;
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
