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

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.util.Messages;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.cloud.IInstance;
import com.clustercontrol.xcloud.model.cloud.IInstanceBackupEntry;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;

public class DeleteInstanceSnapshotHandler extends AbstractCloudOptionHandler implements CloudStringConstants {

	private IInstance instance;
	
	@Override
	public Object internalExecute(ExecutionEvent event) throws ExecutionException, CloudManagerException, InvalidUserPass, InvalidRole, RestConnectFailed, HinemosUnknown {
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

			String managerName = entry.getBackup().getInstance().getCloudScope().getCloudScopes().getHinemosManager().getManagerName();
			CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper(managerName);
			endpoint.deleteInstanceSnapshots(
					entry.getBackup().getInstance().getCloudScope().getId(),
							entry.getBackup().getInstance().getLocation().getId(),
							entry.getBackup().getInstance().getId(),
							String.join(",", entryIds)
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
