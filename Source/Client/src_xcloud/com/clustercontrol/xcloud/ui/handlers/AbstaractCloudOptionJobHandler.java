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
import com.clustercontrol.xcloud.model.cloud.IResource;
import com.clustercontrol.xcloud.ui.dialogs.job.CreateJobWizard;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;

public abstract class AbstaractCloudOptionJobHandler extends AbstractCloudOptionHandler implements CloudStringConstants {
	@Override
	public Object internalExecute(ExecutionEvent event) throws ExecutionException, CloudManagerException, InvalidRole, InvalidUserPass, RestConnectFailed, HinemosUnknown {
		setup(event);
		registJob(event);
		return null;
	}

	protected abstract void setup(ExecutionEvent event);

	protected abstract ICloudScope getCloudScope();

	protected abstract IResource getResource();

	protected abstract JobResourceInfoResponse.ResourceActionEnum getAction();

	protected abstract String getWizardTitle();

	protected abstract String getJobName();

	protected abstract String getJobId();

	protected abstract String cutJobId(int num);

	protected void registJob(ExecutionEvent event) throws ExecutionException, CloudManagerException, InvalidRole, InvalidUserPass, RestConnectFailed, HinemosUnknown {
		String managerName =  getCloudScope().getCloudScopes().getHinemosManager().getManagerName();
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
			CreateJobWizard wizard = new CreateJobWizard(endpoint, getWizardTitle(), new CreateJobWizard.IJobDetailProvider() {
				@Override
				public String getJobId() {
					return AbstaractCloudOptionJobHandler.this.getJobId();
				}
				@Override
				public String cutJobId(int num) {
					return AbstaractCloudOptionJobHandler.this.cutJobId(num);
				}
				@Override
				public ICloudScope getCloudScope() {
					return AbstaractCloudOptionJobHandler.this.getCloudScope();
				}
				@Override
				public IResource getResource() {
					return AbstaractCloudOptionJobHandler.this.getResource();
				}
				@Override
				public JobResourceInfoResponse.ResourceActionEnum getAction() {
					return AbstaractCloudOptionJobHandler.this.getAction();
				}
			});

			WizardDialog wizardDialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
			wizardDialog.open();
		} finally {
			Font changed = JFaceResources.getBannerFont();
			JFaceResources.getFontRegistry().put(JFaceResources.BANNER_FONT, bannerFont.getFontData());
			changed.dispose();
		}
	}
}
