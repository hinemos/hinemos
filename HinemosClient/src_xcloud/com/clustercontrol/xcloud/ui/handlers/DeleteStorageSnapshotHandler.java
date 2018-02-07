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
import com.clustercontrol.xcloud.model.cloud.IStorage;
import com.clustercontrol.xcloud.model.cloud.IStorageBackupEntry;

public class DeleteStorageSnapshotHandler extends AbstractCloudOptionHandler implements CloudStringConstants {
	
	private IStorage storage;
	
	@Override
	public Object internalExecute(ExecutionEvent event) throws ExecutionException, CloudManagerException, InvalidRole_Exception, InvalidUserPass_Exception {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getActiveSite(event).getSelectionProvider().getSelection();
		IStorageBackupEntry entry = (IStorageBackupEntry)selection.getFirstElement();
		storage = entry.getBackup().getStorage();

		if (MessageDialog.openConfirm(
			null,
			Messages.getString("confirmed"),
			MessageFormat.format(msgConfirmDeleteSnapshot, entry.getName(), entry.getId()))) {
			
			List<String> entryIds = new ArrayList<>();
			for (@SuppressWarnings("rawtypes") Iterator iter = selection.iterator(); iter.hasNext();) {
				entryIds.add(((IStorageBackupEntry)iter.next()).getId());
			}

				CloudEndpoint endpoint = entry.getBackup().getStorage().getCloudScope().getCloudScopes().getHinemosManager().getEndpoint(CloudEndpoint.class);
				endpoint.deleteStorageSnapshots(
						entry.getBackup().getStorage().getCloudScope().getId(),
						entry.getBackup().getStorage().getLocation().getId(),
						entry.getBackup().getStorage().getId(),
						entryIds
						);;
				
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
				storage.getBackup().update();
			}
		});
	}

	@Override
	protected String getErrorMessage() {
		return msgErrorFinishDeleteSnapshot;
	}
}
