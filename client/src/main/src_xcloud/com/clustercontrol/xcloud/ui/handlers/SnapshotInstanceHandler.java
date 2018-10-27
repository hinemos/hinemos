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

import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.ws.xcloud.CloudManagerException;
import com.clustercontrol.ws.xcloud.CreateInstanceSnapshotRequest;
import com.clustercontrol.ws.xcloud.InvalidRole_Exception;
import com.clustercontrol.ws.xcloud.InvalidUserPass_Exception;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.extensions.CloudOptionExtension;
import com.clustercontrol.xcloud.model.cloud.IInstance;
import com.clustercontrol.xcloud.ui.dialogs.SnapshotDialog;

public class SnapshotInstanceHandler extends AbstractCloudOptionHandler implements CloudStringConstants {

	@Override
	public Object internalExecute(ExecutionEvent event) throws ExecutionException, CloudManagerException, InvalidRole_Exception, InvalidUserPass_Exception {
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

				CreateInstanceSnapshotRequest request = new CreateInstanceSnapshotRequest();
				request.setInstanceId(instance.getId());
				request.setName(dialog.getSnapshotName());
				request.setDescription(dialog.getDescription());

				CloudEndpoint endpoint = instance.getCloudScope().getCloudScopes().getHinemosManager().getEndpoint(CloudEndpoint.class);
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
