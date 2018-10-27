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
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.cloud.IInstance;
import com.clustercontrol.xcloud.model.cloud.ILocation;
import com.clustercontrol.xcloud.ui.views.InstanceMonitorService;

public class DeleteInstanceHandler extends AbstractCloudOptionHandler implements CloudStringConstants {
	@Override
	public Object internalExecute(ExecutionEvent event) throws CloudManagerException, InvalidRole_Exception, InvalidUserPass_Exception {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getActiveSite(event).getSelectionProvider().getSelection();
		final IInstance instance = (IInstance)selection.getFirstElement();

		final List<String> instanceIds = new ArrayList<>();
		for (@SuppressWarnings("rawtypes") Iterator iter = selection.iterator(); iter.hasNext();) {
			instanceIds.add(((IInstance)iter.next()).getId());
		}
		
		if (MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				//MessageFormat.format(msgConfirmDeleteComputeNode, instance.getName(), instance.getId()))) {
				instanceIds.size() > 1 ? MessageFormat.format(msgConfirmDeleteComputeNodeMulti, instanceIds.size()):
					MessageFormat.format(msgConfirmDeleteComputeNode, instance.getName(), instance.getId()))) {

			final ILocation location = instance.getLocation();
			CloudEndpoint endpoint = location.getCloudScope().getCloudScopes().getHinemosManager().getEndpoint(CloudEndpoint.class);
			endpoint.removeInstances(instance.getCloudScope().getId(), instance.getLocation().getId(), instanceIds);

			// 成功報告ダイアログを生成
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					msgFinishDeleteComputeNode);

			Display.getCurrent().asyncExec(new Runnable() {
				@Override
				public void run() {
					instance.getLocation().updateLocation();
					for (String instanceId: instanceIds) {
						IInstance instance;
						try {
							instance = location.getComputeResources().getInstance(instanceId);
						} catch(CloudModelException e) {
							return;
						}
						InstanceMonitorService.getInstanceMonitorService().startMonitor(
								instance.getHinemosManager().getManagerName(),
								instance.getCloudScope().getId(),
								instance.getLocation().getId(),
								instanceId,
								"terminated"
								);
					}
				}
			});
		}
		return null;
	}

	@Override
	protected String getErrorMessage() {
		return msgErrorFinishDeleteComputeNode;
	}
}
