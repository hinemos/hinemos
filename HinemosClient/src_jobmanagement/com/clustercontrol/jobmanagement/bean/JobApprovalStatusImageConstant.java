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
import com.clustercontrol.bean.JobApprovalStatusConstant;

/**
 * 承認状態イメージの定数クラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class JobApprovalStatusImageConstant extends JobApprovalStatusConstant {
	private static Image pending = null;

	private static Image still = null;

	private static Image suspend = null;

	private static Image stop = null;

	private static Image finished = null;

	/**
	 * 種別からImageに変換します。<BR>
	 *
	 * @param type
	 * @return
	 */
	public static Image typeToImage(int type) {
		ImageRegistry registry = ClusterControlPlugin.getDefault()
				.getImageRegistry();

		if (type == TYPE_FINISHED) {
			if (finished == null)
				finished = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_GREEN).createImage();
			return finished;
		} else if (type == TYPE_PENDING) {
			if (pending == null)
				pending = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_BLUE).createImage();
			return pending;
		} else if (type == TYPE_STILL) {
			if (still == null)
				still = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_WHITE).createImage();
			return still;
		} else if (type == TYPE_SUSPEND) {
			if (suspend == null)
				suspend = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_YELLOW).createImage();
			return suspend;
		} else if (type == TYPE_STOP) {
			if (stop == null)
				stop = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_RED).createImage();
			return stop;
		}
		return null;
	}
}
