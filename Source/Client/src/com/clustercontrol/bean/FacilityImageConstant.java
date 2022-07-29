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

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.repository.bean.FacilityConstant;

/**
 * ファシリティのイメージの定数クラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class FacilityImageConstant extends FacilityConstant {
	private static Image composite = null;

	private static Image scope = null;
	
	private static Image scopeInvalid = null; 

	private static Image node = null;

	private static Image nodeInvalid = null;

	/**
	 * 種別から文字列に変換します。<BR>
	 *
	 * @param type
	 * @return
	 */
	public static Image typeToImage(int type, boolean valid) {
		ImageRegistry registry = ClusterControlPlugin.getDefault()
				.getImageRegistry();

		if (type == TYPE_COMPOSITE) {
			if (composite == null)
				composite = registry.getDescriptor(
						ClusterControlPlugin.IMG_CONSOLE).createImage();
			return composite;
		} else if (type == TYPE_SCOPE) {
			if (valid) {
				if (scope == null)
					scope = registry.getDescriptor(ClusterControlPlugin.IMG_SCOPE)
						.createImage();
				return scope;
			} else {
				if (scopeInvalid == null) 
					scopeInvalid = registry.getDescriptor(ClusterControlPlugin.IMG_SCOPE_INVALID)
					.createImage();
				return scopeInvalid;
			}
		} else if (type == TYPE_NODE) {
			if (valid) {
				if (node == null)
					node = registry.getDescriptor(ClusterControlPlugin.IMG_NODE)
					.createImage();
				return node;
			} else {
				if (nodeInvalid == null)
					nodeInvalid = registry.getDescriptor(ClusterControlPlugin.IMG_NODE_INVALID)
					.createImage();
				return nodeInvalid;
			}
		}
		return null;
	}
}
