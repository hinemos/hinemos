/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
