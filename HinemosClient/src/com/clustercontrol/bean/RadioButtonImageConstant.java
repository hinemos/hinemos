/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
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
 * ラジオボタンイメージ定数クラス<BR>
 *
 */
public class RadioButtonImageConstant {
	/** Radio on image */
	private static Image on = null;
	/** Radio off image */
	private static Image off = null;

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
			if (on == null){
				on = registry.getDescriptor(
						ClusterControlPlugin.IMG_RADIO_ON)
						.createImage();
			}
			return on;
		} else {
			if (off == null){
				off = registry.getDescriptor(
						ClusterControlPlugin.IMG_RADIO_OFF)
						.createImage();
			}
			return off;
		}
	}
}
