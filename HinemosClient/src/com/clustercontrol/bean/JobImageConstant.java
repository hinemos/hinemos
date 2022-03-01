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
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.jobmanagement.bean.JobConstant;

/**
 * ジョブイメージ定数のクラス<BR>
 *
 * @version 4.1.0
 * @since 1.0.0
 */
public class JobImageConstant extends JobConstant {
	private static Image jobUnit = null;

	private static Image jobUnitUnref = null;

	private static Image jobNet = null;

	private static Image job = null;

	private static Image fileJob = null;

	private static Image referJob = null;

	private static Image manager = null;

	private static Image referJobNet = null;

	private static Image approvalJob = null;

	private static Image monitorJob = null;

	private static Image filecheckJob = null;

	private static Image joblinksendJob = null;

	private static Image joblinkrcvJob = null;

	private static Image resourceJob = null;

	private static Image rpaJob = null;

	/**
	 * 種別からImageに変換します。<BR>
	 *
	 * @param type
	 * @return
	 */
	public static Image typeToImage(int type) {
		ImageRegistry registry = ClusterControlPlugin.getDefault()
				.getImageRegistry();

		if (type == TYPE_JOBUNIT || type == TYPE_COMPOSITE) {
			if (jobUnit == null)
				jobUnit = registry.getDescriptor(
						ClusterControlPlugin.IMG_JOBUNIT).createImage();
			return jobUnit;
		} else if (type == TYPE_JOBUNIT_UNREFERABLE) {
			if (jobUnitUnref == null)
				jobUnitUnref = registry.getDescriptor(
						ClusterControlPlugin.IMG_JOBUNIT_UNREFERABLE).createImage();
			return jobUnitUnref;
		} else if (type == TYPE_JOBNET) {
			if (jobNet == null)
				jobNet = registry.getDescriptor(
						ClusterControlPlugin.IMG_JOBNET).createImage();
			return jobNet;
		} else if (type == TYPE_JOB) {
			if (job == null)
				job = registry.getDescriptor(
						ClusterControlPlugin.IMG_JOB).createImage();
			return job;
		} else if (type == TYPE_FILEJOB) {
			if (fileJob == null)
				fileJob = registry.getDescriptor(
						ClusterControlPlugin.IMG_FILEJOB).createImage();
			return fileJob;
		} else if (type == TYPE_REFERJOB) {
			if (referJob == null)
				referJob = registry.getDescriptor(
						ClusterControlPlugin.IMG_REFERJOB).createImage();
			return referJob;
		} else if (type == TYPE_MANAGER) {
			if (manager == null)
				manager = registry.getDescriptor(
						ClusterControlPlugin.IMG_CONSOLE).createImage();
			return manager;
		} else if (type == TYPE_REFERJOBNET) {
			if (referJobNet == null)
				referJobNet = registry.getDescriptor(
						ClusterControlPlugin.IMG_REFERJOBNET).createImage();
			return referJobNet;
		} else if (type == TYPE_APPROVALJOB) {
			if (approvalJob == null)
				approvalJob = registry.getDescriptor(
						ClusterControlPlugin.IMG_APPROVALJOB).createImage();
			return approvalJob;
		} else if (type == TYPE_MONITORJOB) {
			if (monitorJob == null)
				monitorJob = registry.getDescriptor(
						ClusterControlPlugin.IMG_MONITORJOB).createImage();
			return monitorJob;
		} else if (type == TYPE_FILECHECKJOB) {
			if (filecheckJob == null)
				filecheckJob = registry.getDescriptor(
						ClusterControlPlugin.IMG_FILECHECKJOB).createImage();
			return filecheckJob;
		} else if (type == TYPE_JOBLINKSENDJOB) {
			if (joblinksendJob == null)
				joblinksendJob = registry.getDescriptor(
						ClusterControlPlugin.IMG_JOBLINKSENDJOB).createImage();
			return joblinksendJob;
		} else if (type == TYPE_JOBLINKRCVJOB) {
			if (joblinkrcvJob == null)
				joblinkrcvJob = registry.getDescriptor(
						ClusterControlPlugin.IMG_JOBLINKRCVJOB).createImage();
			return joblinkrcvJob;
		} else if (type == TYPE_RESOURCEJOB) {
			if (resourceJob == null)
				resourceJob = registry.getDescriptor(
						ClusterControlPlugin.IMG_RESOURCEJOB).createImage();
			return resourceJob;
		}

		return null;
	}
	public static Image typeEnumValueToImage(String type) {
		if (type == null) {
			return null;
		}
		ImageRegistry registry = ClusterControlPlugin.getDefault()
				.getImageRegistry();
		//findbugs対応 String比較方法をequelsに統一
		if (type.equals(JobInfoWrapper.TypeEnum.JOBUNIT.getValue()) || type.equals(JobInfoWrapper.TypeEnum.COMPOSITE.getValue())) {
			if (jobUnit == null)
				jobUnit = registry.getDescriptor(
						ClusterControlPlugin.IMG_JOBUNIT).createImage();
			return jobUnit;
		} else if (type.equals(JobInfoWrapper.TypeEnum.JOBUNIT_UNREFERABLE.getValue())) {
			if (jobUnitUnref == null)
				jobUnitUnref = registry.getDescriptor(
						ClusterControlPlugin.IMG_JOBUNIT_UNREFERABLE).createImage();
			return jobUnitUnref;
		} else if (type.equals(JobInfoWrapper.TypeEnum.JOBNET.getValue())) {
			if (jobNet == null)
				jobNet = registry.getDescriptor(
						ClusterControlPlugin.IMG_JOBNET).createImage();
			return jobNet;
		} else if (type.equals(JobInfoWrapper.TypeEnum.JOB.getValue())) {
			if (job == null)
				job = registry.getDescriptor(
						ClusterControlPlugin.IMG_JOB).createImage();
			return job;
		} else if (type.equals(JobInfoWrapper.TypeEnum.FILEJOB.getValue())) {
			if (fileJob == null)
				fileJob = registry.getDescriptor(
						ClusterControlPlugin.IMG_FILEJOB).createImage();
			return fileJob;
		} else if (type.equals(JobInfoWrapper.TypeEnum.REFERJOB.getValue())) {
			if (referJob == null)
				referJob = registry.getDescriptor(
						ClusterControlPlugin.IMG_REFERJOB).createImage();
			return referJob;
		} else if (type.equals(JobInfoWrapper.TypeEnum.MANAGER.getValue())) {
			if (manager == null)
				manager = registry.getDescriptor(
						ClusterControlPlugin.IMG_CONSOLE).createImage();
			return manager;
		} else if (type.equals(JobInfoWrapper.TypeEnum.REFERJOBNET.getValue())) {
			if (referJobNet == null)
				referJobNet = registry.getDescriptor(
						ClusterControlPlugin.IMG_REFERJOBNET).createImage();
			return referJobNet;
		} else if (type.equals(JobInfoWrapper.TypeEnum.APPROVALJOB.getValue())) {
			if (approvalJob == null)
				approvalJob = registry.getDescriptor(
						ClusterControlPlugin.IMG_APPROVALJOB).createImage();
			return approvalJob;
		} else if (type.equals(JobInfoWrapper.TypeEnum.MONITORJOB.getValue())) {
			if (monitorJob == null)
				monitorJob = registry.getDescriptor(
						ClusterControlPlugin.IMG_MONITORJOB).createImage();
			return monitorJob;
		} else if (type.equals(JobInfoWrapper.TypeEnum.FILECHECKJOB.getValue())) {
			if (filecheckJob == null)
				filecheckJob = registry.getDescriptor(
						ClusterControlPlugin.IMG_FILECHECKJOB).createImage();
			return filecheckJob;
		} else if (type.equals(JobInfoWrapper.TypeEnum.JOBLINKSENDJOB.getValue())) {
			if (joblinksendJob == null)
				joblinksendJob = registry.getDescriptor(
						ClusterControlPlugin.IMG_JOBLINKSENDJOB).createImage();
			return joblinksendJob;
		} else if (type.equals(JobInfoWrapper.TypeEnum.JOBLINKRCVJOB.getValue())) {
			if (joblinkrcvJob == null)
				joblinkrcvJob = registry.getDescriptor(
						ClusterControlPlugin.IMG_JOBLINKRCVJOB).createImage();
			return joblinkrcvJob;
		} else if (type.equals(JobInfoWrapper.TypeEnum.RESOURCEJOB.getValue())) {
			if (resourceJob == null)
				resourceJob = registry.getDescriptor(
						ClusterControlPlugin.IMG_RESOURCEJOB).createImage();
			return resourceJob;
		} else if (type.equals(JobInfoWrapper.TypeEnum.RPAJOB.getValue())) {
			if (rpaJob == null)
				rpaJob = registry.getDescriptor(
						ClusterControlPlugin.IMG_RPAJOB).createImage();
			return rpaJob;
		}

		return null;
	}
}
