/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.extensions.ICloudOptionHandler;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.ui.dialogs.ModifyBillingSettingDialog;
import com.clustercontrol.xcloud.util.ControlUtil;

public class ModifyBillingSettingHandler implements ICloudOptionHandler, CloudStringConstants {
	
	private static final Log logger = LogFactory.getLog(ModifyBillingSettingHandler.class);

	public ModifyBillingSettingHandler() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getCurrentSelection(event);
		final ICloudScope selectedScope = (ICloudScope)selection.getFirstElement();
		
		ModifyBillingSettingDialog dialog = new ModifyBillingSettingDialog(HandlerUtil.getActiveShell(event), selectedScope);
		
		loop_end:
		while(true){
			try {
				if (dialog.open() != Window.OK)
					break loop_end;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
	
				// 失敗報告ダイアログを生成
				ControlUtil.openError(e, msgErrorFinishModifyBillingSetting);
				break;
			}
			
			if (MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				msgConfirmModifyBillingSetting)) {
				try {
					CloudEndpoint endpoint = selectedScope.getCloudScopes().getHinemosManager().getEndpoint(CloudEndpoint.class);
					endpoint.modifyBillingSetting(dialog.getOutput());
					
					// 成功報告ダイアログを生成
					MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						msgFinishModifyBillingSetting);
					
					Display.getCurrent().asyncExec(new Runnable() {
						@Override
						public void run() {
							selectedScope.getCloudScopes().getHinemosManager().update();
						}
					});
					
					break loop_end;
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
	
					// 失敗報告ダイアログを生成
					ControlUtil.openError(e, msgErrorFinishModifyBillingSetting);
				}
			}
		}
		return null;
	}
}
