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
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openapitools.client.model.SnapshotInstanceRequest;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.util.Messages;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.extensions.CloudOptionExtension;
import com.clustercontrol.xcloud.model.cloud.IInstance;
import com.clustercontrol.xcloud.ui.dialogs.SnapshotDialog;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;

public class SnapshotInstanceHandler extends AbstractCloudOptionHandler implements CloudStringConstants {

	@Override
	public Object internalExecute(ExecutionEvent event) throws ExecutionException, CloudManagerException, InvalidUserPass, InvalidRole, RestConnectFailed, HinemosUnknown  {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getActiveSite(event).getSelectionProvider().getSelection();
		final IInstance instance = (IInstance)selection.getFirstElement();

		SnapshotDialog dialog = new SnapshotDialog(HandlerUtil.getActiveShell(event), MessageFormat.format(dlgComputeSnapshot, CloudOptionExtension.getOptions().get(instance.getCloudScope().getCloudPlatform().getId())));
		
		while (true) {
			if (dialog.open() != Window.OK) {
				break;
			}

			if (MessageDialog.openConfirm(
					null,
					Messages.getString("confirmed"),
					MessageFormat.format(msgConfirmSnapshotCreateComputeNode, instance.getName(), instance.getId()))) {

				SnapshotInstanceRequest request = new SnapshotInstanceRequest();
				request.setInstanceId(instance.getId());
				request.setName(dialog.getSnapshotName());
				request.setDescription(dialog.getDescription());
				
				String managerName = instance.getCloudScope().getCloudScopes().getHinemosManager().getManagerName();
				CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper(managerName);
				
				endpoint.snapshotInstance(instance.getCloudScope().getId(), instance.getLocation().getId(), request);

				// 成功報告ダイアログを生成
				MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						MessageFormat.format(msgFinishSnapshotCreateComputeNode, instance.getName(), instance.getId()));

				Display.getCurrent().asyncExec(new Runnable() {
					@Override
					public void run() {
						instance.getBackup().update();
					}
				});
				break;
			}
		}
		
		return null;
	}

	@Override
	protected String getErrorMessage() {
		return msgErrorFinishSnapshotCreateComputeNode;
	}
}
