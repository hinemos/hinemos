/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openapitools.client.model.JobResourceInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.ui.dialogs.job.CreateGroupJobWizard;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;

public abstract class AbstaractCloudOptionGroupJobHandler extends AbstractCloudOptionHandler implements CloudStringConstants {
	@Override
	public Object internalExecute(ExecutionEvent event) throws ExecutionException, CloudManagerException, InvalidRole, InvalidUserPass, RestConnectFailed, HinemosUnknown {
		setup(event);
		registJob(event);
		return null;
	}

	protected abstract void setup(ExecutionEvent event);

	protected abstract String getWizardTitle();

	protected abstract ICloudScope getCloudScope();

	protected abstract String getJobName(String facilityId);

	protected abstract String getJobId(String facilityId);

	protected abstract JobResourceInfoResponse.ResourceActionEnum getAction();

	protected void registJob(ExecutionEvent event) throws ExecutionException, CloudManagerException, InvalidRole, InvalidUserPass, RestConnectFailed, HinemosUnknown {
		String managerName = getCloudScope().getCloudScopes().getHinemosManager().getManagerName();
		CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper(managerName);
		endpoint.checkPublish();
		
		Font bannerFont = JFaceResources.getBannerFont();
		Font dialogFont = JFaceResources.getDialogFont();
		FontData[] fontDatas = dialogFont.getFontData();
		for (FontData fontData: fontDatas) {
			fontData.setHeight(fontData.getHeight() + 2);
			fontData.setStyle(SWT.BOLD);
		}
		JFaceResources.getFontRegistry().put(JFaceResources.BANNER_FONT, fontDatas);

		try {
			CreateGroupJobWizard wizard = new CreateGroupJobWizard(getWizardTitle(), new CreateGroupJobWizard.IJobDetailProvider() {
				@Override
				public String getJobName(String facilityId) {
					return AbstaractCloudOptionGroupJobHandler.this.getJobName(facilityId);
				}
				@Override
				public String getJobId(String facilityId) {
					return AbstaractCloudOptionGroupJobHandler.this.getJobId(facilityId);
				}
				@Override
				public ICloudScope getCloudScope() {
					return AbstaractCloudOptionGroupJobHandler.this.getCloudScope();
				}
				@Override
				public JobResourceInfoResponse.ResourceActionEnum getAction() {
					return AbstaractCloudOptionGroupJobHandler.this.getAction();
				}
			});

			WizardDialog wizardDialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
			wizardDialog.open();
		} finally {
			Font changed = JFaceResources.getBannerFont();
			JFaceResources.getFontRegistry().put(JFaceResources.BANNER_FONT, bannerFont.getFontData());
			changed.dispose();
		}
		return;
	}
}
