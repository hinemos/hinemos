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

import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.xcloud.extensions.CloudOptionExtension;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
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
	protected String getCommand(CloudEndpoint endpoint) throws Exception {
		return endpoint.makeSnapshotStorageCommand(storage.getCloudScope().getId(), storage.getLocation().getId(), storage.getId());
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
			jobid = String.format("%s_%s_s-snapshot", storage.getLocation().getId(), storage.getId().substring(diff - 1, storage.getId().length()-1));
		}
		
		return jobid;
	}

	@Override
	protected String getMethodName() {
		return "makeSnapshotStorageCommand";
	}

	@Override
	protected String getWizardTitle() {
		return MessageFormat.format(dlgStorageSnapshot, CloudOptionExtension.getOptions().get(storage.getCloudScope().getCloudPlatform().getId()));
	}

	@Override
	protected String getErrorMessage() {
		return msgErrorFinishCreateSnapshotStorageJob;
	}
}
