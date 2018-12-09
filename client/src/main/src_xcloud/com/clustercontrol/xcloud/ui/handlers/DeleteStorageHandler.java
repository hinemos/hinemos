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
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
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

public class DeleteStorageHandler extends AbstractCloudOptionHandler implements CloudStringConstants {
	@Override
	public Object internalExecute(ExecutionEvent event) throws CloudManagerException, InvalidRole_Exception, InvalidUserPass_Exception {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getCurrentSelection(event);
		final IStorage storage = (IStorage)selection.getFirstElement();
		List<String> storageIds = new ArrayList<>();
		for (Object item: selection.toList()) {
			storageIds.add(((IStorage)item).getId());
		}

		if (MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				storageIds.size() > 1 ?
						MessageFormat.format(msgConfirmDeleteStorageMulti, storageIds.size()):
							MessageFormat.format(msgConfirmDeleteStorage, storage.getName(), storage.getId()))) {

			CloudEndpoint endpoint = storage.getCloudScope().getCloudScopes().getHinemosManager().getEndpoint(CloudEndpoint.class);
			endpoint.removeStorages(storage.getCloudScope().getId(), storage.getLocation().getId(), storageIds);

			// 成功報告ダイアログを生成
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					msgFinishDeleteStorage);
			
			Display.getCurrent().asyncExec(new Runnable() {
				@Override
				public void run() {
					storage.getLocation().getComputeResources().updateStorages();
				}
			});
		}
		return null;
	}

	@Override
	protected String getErrorMessage() {
		return msgErrorFinishDeleteStorage;
	}
}
