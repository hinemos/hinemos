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
import org.openapitools.client.model.JobDetailInfoResponse;

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

	private static Image scheduled = null;

	private static Image waiting = null;

	private static Image stopping = null;

	private static Image stop = null;

	private static Image wait = null;

	private static Image skip = null;

	private static Image suspend = null;

	private static Image runError = null;

	private static Image notManaged = null;

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
		} else if (type == TYPE_SCHEDULED) {
			if (scheduled == null)
				scheduled = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_WHITE).createImage();
			return scheduled;
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
		} else if (type == TYPE_NOT_MANAGED) {
			if (notManaged == null)
				notManaged = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_WHITE).createImage();
			return notManaged;
		}

		return null;
	}
	public static Image typeEnumValueToImage(String type) {
		ImageRegistry registry = ClusterControlPlugin.getDefault()
				.getImageRegistry();

		if (type.equals(JobDetailInfoResponse.StatusEnum.RUNNING.getValue()) || type.equals(JobDetailInfoResponse.StatusEnum.RUNNING_QUEUE.getValue())) {
			if (running == null)
				running = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_BLUE).createImage();
			return running;
		} else if (isEndGroupEnum(type)) {
			if (end == null)
				end = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_GREEN).createImage();
			return end;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.SCHEDULED.getValue())) {
			if (scheduled == null)
				scheduled = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_WHITE).createImage();
			return scheduled;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.WAIT.getValue())) {
			if (waiting == null)
				waiting = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_WHITE).createImage();
			return waiting;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.STOPPING.getValue())) {
			if (stopping == null)
				stopping = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_BLUE).createImage();
			return stopping;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.STOP.getValue())) {
			if (stop == null)
				stop = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_RED).createImage();
			return stop;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.RESERVING.getValue())) {
			if (wait == null)
				wait = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_YELLOW).createImage();
			return wait;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.SKIP.getValue())) {
			if (skip == null)
				skip = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_YELLOW).createImage();
			return skip;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.SUSPEND.getValue()) || type.equals(JobDetailInfoResponse.StatusEnum.SUSPEND_QUEUE.getValue())) {
			if (suspend == null)
				suspend = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_YELLOW).createImage();
			return suspend;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.ERROR.getValue())) {
			if (runError == null)
				runError = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_RED).createImage();
			return runError;
		} else if (type.equals(JobDetailInfoResponse.StatusEnum.NOT_MANAGED.getValue())) {
			if (notManaged == null)
				notManaged = registry.getDescriptor(
						ClusterControlPlugin.IMG_STATUS_WHITE).createImage();
			return notManaged;
		}

		return null;
	}
	private static boolean isEndGroupEnum(String type) {
		if (type.equals(JobDetailInfoResponse.StatusEnum.END.getValue())) {
			return true;
		}
		if (type.equals(JobDetailInfoResponse.StatusEnum.MODIFIED.getValue())) {
			return true;
		}
		if (type.equals(JobDetailInfoResponse.StatusEnum.END_UNMATCH.getValue())) {
			return true;
		}
		if (type.equals(JobDetailInfoResponse.StatusEnum.END_CALENDAR.getValue())) {
			return true;
		}
		if (type.equals(JobDetailInfoResponse.StatusEnum.END_SKIP.getValue())) {
			return true;
		}
		if (type.equals(JobDetailInfoResponse.StatusEnum.END_START_DELAY.getValue())) {
			return true;
		}
		if (type.equals(JobDetailInfoResponse.StatusEnum.END_END_DELAY.getValue())) {
			return true;
		}
		if (type.equals(JobDetailInfoResponse.StatusEnum.END_EXCLUSIVE_BRANCH.getValue())) {
			return true;
		}
		if (type.equals(JobDetailInfoResponse.StatusEnum.END_FAILED_OUTPUT.getValue())) {
			return true;
		}
		return false;
	}

}
