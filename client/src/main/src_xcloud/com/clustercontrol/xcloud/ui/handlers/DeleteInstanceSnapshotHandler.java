/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.handlers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.ws.xcloud.CloudManagerException;
import com.clustercontrol.ws.xcloud.InvalidRole_Exception;
import com.clustercontrol.ws.xcloud.InvalidUserPass_Exception;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.cloud.IInstance;
import com.clustercontrol.xcloud.model.cloud.IInstanceBackupEntry;

public class DeleteInstanceSnapshotHandler extends AbstractCloudOptionHandler implements CloudStringConstants {

	private IInstance instance;
	
	@Override
	public Object internalExecute(ExecutionEvent event) throws ExecutionException, CloudManagerException, InvalidRole_Exception, InvalidUserPass_Exception {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getActiveSite(event).getSelectionProvider().getSelection();
		IInstanceBackupEntry entry = (IInstanceBackupEntry)selection.getFirstElement();
		instance = entry.getBackup().getInstance();

		if (MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				MessageFormat.format(msgConfirmDeleteSnapshot, entry.getName(), entry.getId()))) {

			List<String> entryIds = new ArrayList<>();
			for (@SuppressWarnings("rawtypes") Iterator iter = selection.iterator(); iter.hasNext();) {
				entryIds.add(((IInstanceBackupEntry)iter.next()).getId());
			}

			CloudEndpoint endpoint = entry.getBackup().getInstance().getCloudScope().getCloudScopes().getHinemosManager().getEndpoint(CloudEndpoint.class);
			endpoint.deleteInstanceSnapshots(
					entry.getBackup().getInstance().getCloudScope().getId(),
					entry.getBackup().getInstance().getLocation().getId(),
					entry.getBackup().getInstance().getId(),
					entryIds
					);

			// 成功報告ダイアログを生成
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					MessageFormat.format(msgFinishDeleteSnapshot, entry.getName(), entry.getId()));
		}
		return null;
	}

	@Override
	protected void afterCall() {
		Display.getCurrent().asyncExec(new Runnable() {
			@Override
			public void run() {
				instance.getBackup().update();
			}
		});
	}

	@Override
	protected String getErrorMessage() {
		return msgErrorFinishDeleteSnapshot;
	}
}
