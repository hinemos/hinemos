/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.openapitools.client.model.RoleInfoResponse;
import org.openapitools.client.model.UserInfoResponse;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;

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

		if (data instanceof RoleInfoResponse) {
			if (((RoleInfoResponse)data).getRoleId().equals(RoleSettingTreeConstant.ROOT_ID)) {
				if (root == null) {
					root = registry.getDescriptor(
							ClusterControlPlugin.IMG_ROLESETTING_ROOT).createImage();
				}
				return root;
			} else if (((RoleInfoResponse)data).getRoleId().equals(RoleSettingTreeConstant.MANAGER)) {
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
		} else if (data instanceof UserInfoResponse) {
			if (user == null) {
				user = registry.getDescriptor(
						ClusterControlPlugin.IMG_ROLESETTING_USER).createImage();
			}
			return user;
		}

		return null;
	}
}
