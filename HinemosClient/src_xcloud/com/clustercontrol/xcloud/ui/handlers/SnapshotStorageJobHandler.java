/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.handlers;

import java.text.MessageFormat;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openapitools.client.model.JobResourceInfoResponse.ResourceActionEnum;

import com.clustercontrol.xcloud.extensions.CloudOptionExtension;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.IResource;
import com.clustercontrol.xcloud.model.cloud.IStorage;
import com.clustercontrol.xcloud.util.CloudUtil;

public class SnapshotStorageJobHandler extends AbstaractCloudOptionJobHandler {
	protected IStorage storage;

	@Override
	protected void setup(ExecutionEvent event) {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getActiveSite(event).getSelectionProvider().getSelection();
		storage = (IStorage)selection.getFirstElement();
	}

	@Override
	protected ICloudScope getCloudScope() {
		return storage.getCloudScope();
	}

	@Override
	protected String getJobName() {
		return getJobId();
	}

	@Override
	protected String getJobId() {
		String jobid = String.format("%s_%s_s-snapshot", storage.getLocation().getId(), storage.getId());
		if (jobid.length() > CloudUtil.jobIdMaxLength) {
			int diff = jobid.length() - CloudUtil.jobIdMaxLength;
			jobid = String.format("%s_%s_s-snapshot", storage.getLocation().getId(), storage.getId().substring(diff, storage.getId().length()));
		}
		
		return jobid;
	}

	@Override
	protected String cutJobId(int num) {
		String jobid = String.format("%s_%s_s-snapshot", storage.getLocation().getId(), storage.getId());
		int diff = jobid.length() - CloudUtil.jobIdMaxLength;
		jobid = String.format("%s_%s_s-snapshot", storage.getLocation().getId(), storage.getId().substring(diff + num, storage.getId().length()-1));
		return jobid;
	}

	@Override
	protected String getWizardTitle() {
		return MessageFormat.format(dlgStorageSnapshot, CloudOptionExtension.getOptions().get(storage.getCloudScope().getCloudPlatform().getId()));
	}

	@Override
	protected String getErrorMessage() {
		return msgErrorFinishCreateSnapshotStorageJob;
	}

	@Override
	protected IResource getResource() {
		return storage;
	}

	@Override
	protected ResourceActionEnum getAction() {
		return ResourceActionEnum.SNAPSHOT;
	}
}
