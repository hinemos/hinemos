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
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.xcloud.extensions.CloudOptionExtension;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.plugin.CloudOptionSourceProvider;
import com.clustercontrol.xcloud.util.CloudUtil;

public class PowerOffInstanceGroupJobHandler extends AbstaractCloudOptionGroupJobHandler {
	protected ICloudScope cloudScope;

	@Override
	protected void setup(ExecutionEvent event) {
		cloudScope = (ICloudScope)HandlerUtil.getVariable(event, CloudOptionSourceProvider.ActiveCloudScope);
	}
	@Override
	protected ICloudScope getCloudScope() {
		return cloudScope;
	}
	@Override
	protected String getCommand(CloudEndpoint endpoint, String facilityId) throws Exception {
		return endpoint.makePowerOffInstancesCommandUsingFacility(cloudScope.getId(), facilityId);
	}
	@Override
	protected String getJobName(String facilityId) {
		return getJobId(facilityId);
	}
	@Override
	protected String getJobId(String facilityId) {
		String jobid = String.format("%s_i-poweroff", facilityId);
		if (jobid.length() > CloudUtil.jobIdMaxLength) {
			int diff = jobid.length() - CloudUtil.jobIdMaxLength;
			jobid = String.format("%s_i-poweroff", facilityId.substring(0, facilityId.length() - diff - 1));
		}
		
		return jobid;
	}
	@Override
	protected String getMethodName() {
		return "makePowerOffInstancesCommandUsingFacility";
	}
	
	@Override
	protected String getWizardTitle() {
		return MessageFormat.format(dlgComputePowerOff, CloudOptionExtension.getOptions().get(cloudScope.getCloudPlatform().getId()));
	}
	
	@Override
	protected String getErrorMessage() {
		return msgErrorFinishCreatePowerOffJob;
	}
}
