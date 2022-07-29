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
import org.openapitools.client.model.DeleteStorageSnapshotRequest;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.util.Messages;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.cloud.IStorage;
import com.clustercontrol.xcloud.model.cloud.IStorageBackupEntry;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;

public class DeleteStorageSnapshotHandler extends AbstractCloudOptionHandler implements CloudStringConstants {
	
	private IStorage storage;
	
	@Override
	public Object internalExecute(ExecutionEvent event) throws ExecutionException, InvalidUserPass, InvalidRole, RestConnectFailed, HinemosUnknown, CloudManagerException {
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

			String managerName = entry.getBackup().getStorage().getCloudScope().getCloudScopes().getHinemosManager().getManagerName();
			CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper(managerName);
			
			DeleteStorageSnapshotRequest request = new DeleteStorageSnapshotRequest();
			request.setStorageId(entry.getBackup().getStorage().getId());
			request.setStorageSnapshotIds(entryIds);

			endpoint.deleteStorageSnapshots(
					entry.getBackup().getStorage().getCloudScope().getId(),
					entry.getBackup().getStorage().getLocation().getId(),
					request
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
				storage.getBackup().update();
			}
		});
	}

	@Override
	protected String getErrorMessage() {
		return msgErrorFinishDeleteSnapshot;
	}
}
