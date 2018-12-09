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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import com.clustercontrol.xcloud.model.cloud.IInstance;

public class ManualNodeAssignHandler extends AbstractCloudOptionHandler implements CloudStringConstants {
	
	private static final Log logger = LogFactory.getLog(ManualNodeAssignHandler.class);
	
	@Override
	public Object internalExecute(ExecutionEvent event) throws CloudManagerException, InvalidRole_Exception, InvalidUserPass_Exception {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getActiveSite(event).getSelectionProvider().getSelection();
		final IInstance selected = (IInstance)selection.getFirstElement();
		
		final List<IInstance> instances = new ArrayList<>();
		for (@SuppressWarnings("rawtypes") Iterator iter = selection.iterator(); iter.hasNext();) {
			instances.add(((IInstance)iter.next()));
		}

		if (MessageDialog.openConfirm(
			null,
			Messages.getString("confirmed"),
			//MessageFormat.format(msgConfirmManualRegistNodeModify, instance.getName(), instance.getId()))) {
			instances.size() > 1 ? MessageFormat.format(msgConfirmManualRegistNodeModifyMulti, instances.size()):
				MessageFormat.format(msgConfirmManualRegistNodeModify, selected.getName(), selected.getId()))) {

			for (IInstance instance: instances) {
				try {
					CloudEndpoint endpoint = instance.getCloudScope().getCloudScopes().getHinemosManager().getEndpoint(CloudEndpoint.class);
					endpoint.assignNodeToInstance(instance.getCloudScope().getId(), instance.getLocation().getId(), instance.getId());
				} catch (Exception e) {
					logger.warn(e);
				}
			}

			// 成功報告ダイアログを生成
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					msgFinishManualRegistNodeModify);
			
			Display.getCurrent().asyncExec(new Runnable() {
				@Override
				public void run() {
					selected.getLocation().updateLocation();
				}
			});
		}
		return null;
	}

	@Override
	protected String getErrorMessage() {
		return msgErrorFinishManualRegistNodeModify;
	}
}
