/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.bean;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.JobApprovalResultConstant;

/**
 * 承認結果イメージの定数クラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class JobApprovalResultImageConstant extends JobApprovalResultConstant {
	private static Image approval = null;

	private static Image denial = null;

	/**
	 * 種別からImageに変換します。<BR>
	 *
	 * @param type
	 * @return
	 */
	public static Image typeToImage(int type) {
		ImageRegistry registry = ClusterControlPlugin.getDefault()
				.getImageRegistry();

		if (type == TYPE_APPROVAL) {
			if (approval == null)
				approval = registry.getDescriptor(
						ClusterControlPlugin.IMG_END_STATUS_NORMAL).createImage();
			return approval;
		} else if (type == TYPE_DENIAL) {
			if (denial == null)
				denial = registry.getDescriptor(
						ClusterControlPlugin.IMG_END_STATUS_ABNORMAL).createImage();
			return denial;
		}
		return null;
	}
}
