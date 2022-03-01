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

import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.extensions.CloudOptionExtension;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.IInstance;
import com.clustercontrol.xcloud.model.cloud.IResource;
import com.clustercontrol.xcloud.util.CloudUtil;

public class PowerOffInstanceJobHandler extends AbstaractCloudOptionJobHandler implements CloudStringConstants {
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
	protected String getJobName() {
		return getJobId();
	}

	@Override
	protected String getJobId() {
		String jobid = String.format("%s_%s_i-poweroff", instance.getLocation().getId(), instance.getId());
		if (jobid.length() > CloudUtil.jobIdMaxLength) {
			int diff = jobid.length() - CloudUtil.jobIdMaxLength;
			jobid = String.format("%s_%s_i-poweroff", instance.getLocation().getId(), instance.getId().substring(diff, instance.getId().length()));
		}
		
		return jobid;
	}

	@Override
	protected String cutJobId(int num) {
		String jobid = String.format("%s_%s_i-poweroff", instance.getLocation().getId(), instance.getId());
		int diff = jobid.length() - CloudUtil.jobIdMaxLength;
		jobid = String.format("%s_%s_i-poweroff", instance.getLocation().getId(), instance.getId().substring(diff + num, instance.getId().length()));
		return jobid;
	}

	@Override
	protected String getWizardTitle() {
		return MessageFormat.format(dlgComputePowerOff, CloudOptionExtension.getOptions().get(instance.getCloudScope().getCloudPlatform().getId()));
	}

	@Override
	protected String getErrorMessage() {
		return msgErrorFinishCreatePowerOffJob;
	}

	@Override
	protected IResource getResource() {
		return instance;
	}

	@Override
	protected ResourceActionEnum getAction() {
		return ResourceActionEnum.POWEROFF;
	}
}
