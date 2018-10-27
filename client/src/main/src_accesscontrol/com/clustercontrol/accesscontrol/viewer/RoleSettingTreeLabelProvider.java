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

import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.bean.RoleSettingImageConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.access.RoleInfo;
import com.clustercontrol.ws.access.UserInfo;
import com.clustercontrol.ws.accesscontrol.RoleTreeItem;

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
		Object data = ((RoleTreeItem) element).getData();
		if (data instanceof RoleInfo) {
			String id = ((RoleInfo) data).getRoleId();
			String name = ((RoleInfo) data).getRoleName();
			if (id.equals(RoleSettingTreeConstant.ROOT_ID)) {
				return name;
			} else if (id.equals(RoleSettingTreeConstant.MANAGER)) {
				return Messages.getString("facility.manager") + " (" + name + ")";
			} else {
				return name + " (" + id + ")";
			}
		} else if (data instanceof UserInfo) {
			String id = ((UserInfo) data).getUserId();
			String name = ((UserInfo) data).getUserName();
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

		Object data = ((RoleTreeItem) element).getData();

		return RoleSettingImageConstant.typeToImage(data);
	}
}
