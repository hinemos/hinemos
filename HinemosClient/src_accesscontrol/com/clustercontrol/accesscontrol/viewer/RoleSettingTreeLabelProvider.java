/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.viewer;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.openapitools.client.model.RoleInfoResponse;
import org.openapitools.client.model.UserInfoResponse;

import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.accesscontrol.bean.RoleTreeItemWrapper;
import com.clustercontrol.bean.RoleSettingImageConstant;
import com.clustercontrol.util.Messages;

/**
 * ロールツリー用コンポジットのツリービューア用のLabelProviderクラスです。
 *
 * @version 2.0.0
 * @since 1.0.0
 */
public class RoleSettingTreeLabelProvider extends LabelProvider {

	/**
	 * ロールツリーアイテムから表示名を作成し返します。
	 *
	 * @param ロールツリーアイテム
	 * @return 表示名
	 *
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		Object data = ((RoleTreeItemWrapper) element).getData();
		if (data instanceof RoleInfoResponse) {
			String id = ((RoleInfoResponse) data).getRoleId();
			String name = ((RoleInfoResponse) data).getRoleName();
			if (id.equals(RoleSettingTreeConstant.ROOT_ID)) {
				return name;
			} else if (id.equals(RoleSettingTreeConstant.MANAGER)) {
				return Messages.getString("facility.manager") + " (" + name + ")";
			} else {
				return name + " (" + id + ")";
			}
		} else if (data instanceof UserInfoResponse) {
			String id = ((UserInfoResponse) data).getUserId();
			String name = ((UserInfoResponse) data).getUserName();
			return name + " (" + id + ")";
		} else {
			return "";
		}
	}

	/**
	 * ロールツリーアイテムの種別に該当するイメージを返します。
	 *
	 * @param ロールツリーアイテム
	 * @return イメージ
	 *
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {

		Object data = ((RoleTreeItemWrapper) element).getData();

		return RoleSettingImageConstant.typeToImage(data);
	}
}
