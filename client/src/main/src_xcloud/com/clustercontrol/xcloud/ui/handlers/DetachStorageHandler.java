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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.extensions.ICloudOptionHandler;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.IInstance;
import com.clustercontrol.xcloud.model.cloud.IStorage;
import com.clustercontrol.xcloud.plugin.CloudOptionSourceProvider;
import com.clustercontrol.xcloud.util.ControlUtil;

public class DetachStorageHandler implements ICloudOptionHandler, CloudStringConstants {
	
	private static final Log logger = LogFactory.getLog(DetachStorageHandler.class);
	
	@Override
	public Object execute(ExecutionEvent event) {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getCurrentSelection(event);
		final IStorage storage = (IStorage)selection.getFirstElement();
		IInstance instance = null;
		for (IInstance i: storage.getCloudComputeManager().getInstances()) {
			if (i.getId().equals(storage.getTargetInstanceId())) {
				instance = i;
				break;
			}
		}
		
		List<String> storageIds = new ArrayList<>();
		for (Object item: selection.toList()) {
			storageIds.add(((IStorage)item).getId());
		}
		loop_end:
		while(true){
			if (MessageDialog.openQuestion(
				null,
				Messages.getString("confirmed"),
				storageIds.size() > 1 ?
				MessageFormat.format(msgConfirmDetachStorageMulti, storageIds.size()):
				MessageFormat.format(msgConfirmDetachStorage, instance.getName(), instance.getId(), storage.getName(), storage.getId()))) {
				
				try {
					ICloudScope cloudScope = (ICloudScope)HandlerUtil.getVariable(event, CloudOptionSourceProvider.ActiveCloudScope);
					CloudEndpoint endpoint = storage.getCloudScope().getCloudScopes().getHinemosManager().getEndpoint(CloudEndpoint.class);
					endpoint.detachStorage(cloudScope.getId(),storage.getLocationId(), storageIds);
					
					// 成功報告ダイアログを生成
					MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						msgFinishDetachStorage);
					
					Display.getCurrent().asyncExec(new Runnable() {
						@Override
						public void run() {
							storage.getLocation().getComputeResources().updateStorages();
						}
					});
					
					break loop_end;
				} catch (Exception e) {
					logger.error(e.getMessage(), e);

					ControlUtil.openError(e, msgErrorFinishDetachStorage);
					break loop_end;
				}
			} else {
				break loop_end;
			}
			
		}
		return null;
	}
}
