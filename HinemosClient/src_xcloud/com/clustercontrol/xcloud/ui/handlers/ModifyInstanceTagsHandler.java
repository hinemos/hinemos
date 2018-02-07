/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.handlers;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.ws.xcloud.Instance;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.cloud.IInstance;
import com.clustercontrol.xcloud.ui.dialogs.ModifyInstanceTagDialog;
import com.clustercontrol.xcloud.util.ControlUtil;

public class ModifyInstanceTagsHandler extends AbstractHandler implements CloudStringConstants {
	
	private static final Log logger = LogFactory.getLog(ModifyInstanceTagsHandler.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getCurrentSelection(event);
		IInstance instance = (IInstance)selection.getFirstElement();
		
		CloudEndpoint endpoint = instance.getCloudScope().getCloudScopes().getHinemosManager().getEndpoint(CloudEndpoint.class);

		List<Instance> webInstances;
		try {
			webInstances = endpoint.getInstances(instance.getCloudScope().getId(), instance.getLocation().getId(), Arrays.asList(instance.getId()));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			// 失敗報告ダイアログを生成
			ControlUtil.openError(e, msgErrorFinishModifyComputeNode);
			return null;
		}
		
		if (webInstances.isEmpty()) {
			MessageDialog.openError(null, Messages.getString("failed"), String.format("not found instance of %s", instance.getId()));
			return null;
		}
		
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		ModifyInstanceTagDialog dialog = new ModifyInstanceTagDialog(part.getSite().getShell(), webInstances.get(0), instance.getCloudScope().getPlatformId());
		
		loop_end:
		while(true){
			try {
				if (dialog.open() != Window.OK)
					break loop_end;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				
				// 失敗報告ダイアログを生成
				ControlUtil.openError(e, msgErrorFinishModifyComputeNode);
				break;
			}
				
			if (MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				MessageFormat.format(msgConfirmModifyComputeNode, instance.getName(), instance.getId()))) {
				
				try {
					endpoint.modifyInstance(instance.getCloudScope().getId(), instance.getLocation().getId(), dialog.getOutput());
					
					// 成功報告ダイアログを生成
					MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						msgFinishModifyComputeNode);
					
					break loop_end;
				} catch (Exception e) {
					logger.error(e.getMessage(), e);

					// 失敗報告ダイアログを生成
					ControlUtil.openError(e, msgErrorFinishModifyComputeNode);
				}
			}
		}
		return null;
	}
}
