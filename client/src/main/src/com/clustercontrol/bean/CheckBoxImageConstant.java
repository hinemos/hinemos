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

/**
 * チェックボックスイメージ定数クラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class CheckBoxImageConstant {
	/** Checked image */
	private static Image checked = null;
	/** Unchecked image */
	private static Image unchecked = null;

	/**
	 * 種別からImageに変換します。<BR>
	 *
	 * @param type
	 * @return image
	 */
	public static Image typeToImage(boolean type) {
		ImageRegistry registry = ClusterControlPlugin.getDefault()
				.getImageRegistry();

		if (type) {
			if (checked == null){
				checked = registry.getDescriptor(
						ClusterControlPlugin.IMG_CHECKED)
						.createImage();
			}
			return checked;
		} else {
			if (unchecked == null){
				unchecked = registry.getDescriptor(
						ClusterControlPlugin.IMG_UNCHECKED)
						.createImage();
			}
			return unchecked;
		}
	}
}
