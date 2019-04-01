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
import com.clustercontrol.bean.StatusConstant;

/**
 * 状態イメージの定数クラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class StatusImageConstant extends StatusConstant {
	private static Image running = null;

	private static Image end = null;

	private static Image waiting = null;

	private static Image stopping = null;

	private static Image stop = null;

	private static Image wait = null;

	private static Image skip = null;

	private static Image suspend = null;

	private static Image runError = null;

	/**
	 * 種別からImageに変換します。<BR>
	 *
	 * @param type
	 * @return
	 */
	public static Image typeToImage(int type) {
		ImageRegistry registry = ClusterControlPlugin.getDefault()
				.getImageRegistry();

		if (type == TYPE_RUNNING || type == TYPE_RUNNING_QUEUE) {
			if (running == null)
				running = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_BLUE).createImage();
			return running;
		} else if (StatusConstant.isEndGroup(type)) {
			if (end == null)
				end = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_GREEN).createImage();
			return end;
		} else if (type == TYPE_WAIT) {
			if (waiting == null)
				waiting = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_WHITE).createImage();
			return waiting;
		} else if (type == TYPE_STOPPING) {
			if (stopping == null)
				stopping = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_BLUE).createImage();
			return stopping;
		} else if (type == TYPE_STOP) {
			if (stop == null)
				stop = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_RED).createImage();
			return stop;
		} else if (type == TYPE_RESERVING) {
			if (wait == null)
				wait = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_YELLOW).createImage();
			return wait;
		} else if (type == TYPE_SKIP) {
			if (skip == null)
				skip = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_YELLOW).createImage();
			return skip;
		} else if (type == TYPE_SUSPEND || type == TYPE_SUSPEND_QUEUE) {
			if (suspend == null)
				suspend = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_YELLOW).createImage();
			return suspend;
		} else if (type == TYPE_ERROR) {
			if (runError == null)
				runError = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_RED).createImage();
			return runError;
		}

		return null;
	}
}
