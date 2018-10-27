/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.handlers;

import java.text.MessageFormat;
import java.util.Arrays;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.xcloud.extensions.CloudOptionExtension;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.IInstance;
import com.clustercontrol.xcloud.util.CloudUtil;

public class PowerOnInstanceJobHandler extends AbstaractCloudOptionJobHandler {
	protected IInstance instance;
	
	@Override
	protected void setup(ExecutionEvent event) {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getActiveSite(event).getSelectionProvider().getSelection();
		instance = (IInstance)selection.getFirstElement();
	}

	@Override
	protected ICloudScope getCloudScope() {
		return instance.getCloudScope();
	}

	@Override
	protected String getCommand(CloudEndpoint endpoint) throws Exception {
		return endpoint.makePowerOnInstancesCommand(instance.getCloudScope().getId(), instance.getLocation().getId(), Arrays.asList(instance.getId()));
	}

	@Override
	protected String getJobName() {
		return getJobId();
	}

	@Override
	protected String getJobId() {
		String jobid = String.format("%s_%s_i-poweron", instance.getLocation().getId(), instance.getId());
		if (jobid.length() > CloudUtil.jobIdMaxLength) {
			int diff = jobid.length() - CloudUtil.jobIdMaxLength;
			jobid = String.format("%s_%s_1-poweron", instance.getLocation().getId(), instance.getId().substring(0, instance.getId().length() - diff - 1));
		}
		
		return jobid;
	}
	
	@Override
	protected String getMethodName() {
		return "makePowerOnInstancesCommand";
	}

	@Override
	protected String getWizardTitle() {
		return MessageFormat.format(dlgComputePowerOn, CloudOptionExtension.getOptions().get(instance.getCloudScope().getCloudPlatform().getId()));
	}

	@Override
	protected String getErrorMessage() {
		return msgErrorFinishCreatePowerOnJob;
	}
}
