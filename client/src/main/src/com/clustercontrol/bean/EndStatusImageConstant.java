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
import com.clustercontrol.bean.EndStatusConstant;

/**
 * ジョブ終了状態イメージの定数クラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class EndStatusImageConstant extends EndStatusConstant {
	private static Image normal = null;

	private static Image warning = null;

	private static Image abnormal = null;

	/**
	 * 種別からImageに変換します。<BR>
	 *
	 * @param type
	 * @return
	 */
	public static Image typeToImage(int type) {
		ImageRegistry registry = ClusterControlPlugin.getDefault()
				.getImageRegistry();

		if (type == TYPE_NORMAL) {
			if (normal == null)
				normal = registry.getDescriptor(
						ClusterControlPlugin.IMG_END_STATUS_NORMAL)
						.createImage();
			return normal;
		} else if (type == TYPE_WARNING) {
			if (warning == null)
				warning = registry.getDescriptor(
						ClusterControlPlugin.IMG_END_STATUS_WARNING)
						.createImage();
			return warning;
		} else if (type == TYPE_ABNORMAL) {
			if (abnormal == null)
				abnormal = registry.getDescriptor(
						ClusterControlPlugin.IMG_END_STATUS_ABNORMAL)
						.createImage();
			return abnormal;
		}
		return null;
	}
}
