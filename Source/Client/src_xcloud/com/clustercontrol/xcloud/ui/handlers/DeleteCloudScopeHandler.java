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
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.util.Messages;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;

public class DeleteCloudScopeHandler extends AbstractCloudOptionHandler implements CloudStringConstants {
	@Override
	public Object internalExecute(ExecutionEvent event) throws ExecutionException, CloudManagerException, InvalidUserPass, InvalidRole, RestConnectFailed, HinemosUnknown {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getActiveSite(event).getSelectionProvider().getSelection();
		final ICloudScope cloudScope = (ICloudScope)selection.getFirstElement();

		if (MessageDialog.openConfirm(
				null,
//			MessageFormat.format(dlgLoginuserDelete, CloudOptionExtension.getOptions().get(cloudScope.getCloudPlatform().getId())),
				Messages.getString("confirmed"),
				MessageFormat.format(msgConfirmDeleteCloudScope, cloudScope.getId()))) {

			String managerName = cloudScope.getCloudScopes().getHinemosManager().getManagerName();
			CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper(managerName);
			endpoint.removeCloudScope(cloudScope.getId());

			// 成功報告ダイアログを生成
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					msgFinishDeleteCloudScope
					);

			Display.getCurrent().asyncExec(new Runnable() {
				@Override
				public void run() {
					cloudScope.getCloudScopes().getHinemosManager().update();
				}
			});
		}
		return null;
	}

	@Override
	protected String getErrorMessage() {
		return msgErrorFinishDeleteCloudScope;
	}
}
