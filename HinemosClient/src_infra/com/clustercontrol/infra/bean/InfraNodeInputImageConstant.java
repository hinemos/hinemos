/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.bean;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.clustercontrol.ClusterControlPlugin;

/**
 * クライアントで使用する環境構築のログイン情報設定用アイコンイメージ定数クラス<BR>
 * 
 * @version 6.1.0
 */
public class InfraNodeInputImageConstant {
	/* ノードプロパティの認証情報を利用する */
	private static final ImageDescriptor ICON_NODE_PARAM = AbstractUIPlugin.imageDescriptorFromPlugin(ClusterControlPlugin.getPluginId(), "$nl$/icons/e_add_bkmrk.gif");
	/* 環境構築変数を認証情報として利用する */
	private static final ImageDescriptor ICON_INFRA_PARAM = AbstractUIPlugin.imageDescriptorFromPlugin(ClusterControlPlugin.getPluginId(), "$nl$/icons/pydev_package_explorer.gif");
	/* ログイン情報を入力する */
	private static final ImageDescriptor ICON_DIALOG = AbstractUIPlugin.imageDescriptorFromPlugin(ClusterControlPlugin.getPluginId(), "$nl$/icons/write_obj.gif");

	/**
	 * 種別からImageDescriptorに変換します。<BR>
	 * 
	 * @param type
	 * @return ImageDescriptor
	 */
	public static ImageDescriptor typeToImageDescriptor(int type) {
		if (type == InfraNodeInputConstant.TYPE_NODE_PARAM) {
			return ICON_NODE_PARAM;
		} else if (type == InfraNodeInputConstant.TYPE_INFRA_PARAM) {
			return ICON_INFRA_PARAM;
		} else if (type == InfraNodeInputConstant.TYPE_DIALOG) {
			return ICON_DIALOG;
		} else {
			return null;
		}
	}
}
