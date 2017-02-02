/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.bean;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.ws.access.RoleInfo;
import com.clustercontrol.ws.access.UserInfo;

/**
 * ロールイメージ定数のクラス<BR>
 *
 * @version 2.0.0
 * @since 1.0.0
 */
public class RoleSettingImageConstant {
	private static Image root = null;

	private static Image role = null;

	private static Image user = null;

	private static Image manager = null;

	/**
	 * 種別からImageに変換します。<BR>
	 *
	 * @param data
	 * @return
	 */
	public static Image typeToImage(Object data) {
		ImageRegistry registry = ClusterControlPlugin.getDefault()
				.getImageRegistry();

		if (data instanceof RoleInfo) {
			if (((RoleInfo)data).getRoleId().equals(RoleSettingTreeConstant.ROOT_ID)) {
				if (root == null) {
					root = registry.getDescriptor(
							ClusterControlPlugin.IMG_ROLESETTING_ROOT).createImage();
				}
				return root;
			} else if (((RoleInfo)data).getRoleId().equals(RoleSettingTreeConstant.MANAGER)) {
					if (manager == null) {
						manager = registry.getDescriptor(
								ClusterControlPlugin.IMG_CONSOLE).createImage();
					}
					return manager;
			} else {
				if (role == null) {
					role = registry.getDescriptor(
							ClusterControlPlugin.IMG_ROLESETTING_ROLE).createImage();
				}
				return role;
			}
		} else if (data instanceof UserInfo) {
			if (user == null) {
				user = registry.getDescriptor(
						ClusterControlPlugin.IMG_ROLESETTING_USER).createImage();
			}
			return user;
		}

		return null;
	}
}
