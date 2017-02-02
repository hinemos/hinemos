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
