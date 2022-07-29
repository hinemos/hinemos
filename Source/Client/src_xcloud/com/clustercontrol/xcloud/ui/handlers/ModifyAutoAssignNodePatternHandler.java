/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.handlers;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openapitools.client.model.AutoAssignNodePatternEntryInfoResponse;

import com.clustercontrol.util.Messages;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.plugin.CloudOptionSourceProvider;
import com.clustercontrol.xcloud.ui.dialogs.AutoAssignNodeRuleEditDialog;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;
import com.clustercontrol.xcloud.util.ControlUtil;

public class ModifyAutoAssignNodePatternHandler extends AbstractHandler implements CloudStringConstants{
	
	private static final Log logger = LogFactory.getLog(ModifyAutoAssignNodePatternHandler.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ICloudScope cloudScope = (ICloudScope)HandlerUtil.getVariable(event, CloudOptionSourceProvider.ActiveCloudScope);
		
		String managerName = cloudScope.getCloudScopes().getHinemosManager().getManagerName();
		CloudRestClientWrapper endpoint = CloudRestClientWrapper.getWrapper(managerName);
		try {
			try {
				endpoint.checkPublish();
			} catch(CloudManagerException e) {
				if ("COMMUNITY_EDITION_FUNC_NOT_AVAILABLE".equals(e.getFaultInfo().getErrorCode())) {
					throw new CloudModelException(CloudConstants.bundle_messages.getString("message.community_edition.func.not_available"));
				}
				throw e;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			// 失敗報告ダイアログを生成
			ControlUtil.openError(e, msgErrorFinishModifyAutoAssignNodepattern);
			return null;
		}
		
		List<AutoAssignNodePatternEntryInfoResponse> entries;
		try {
			entries = endpoint.getAutoAssigneNodePatterns(cloudScope.getId());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			// 失敗報告ダイアログを生成
			ControlUtil.openError(e, msgErrorFinishModifyAutoAssignNodepattern);
			return null;
		}
		
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		
		AutoAssignNodeRuleEditDialog dialog = new AutoAssignNodeRuleEditDialog(part.getSite().getShell(), cloudScope.getCloudScopes().getHinemosManager().getManagerName());
		dialog.setInput(entries);
		
		loop_end:
		while(true){
			try {
				if (dialog.open() != Window.OK)
					break loop_end;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);

				// 失敗報告ダイアログを生成
				ControlUtil.openError(e, msgErrorFinishModifyAutoAssignNodepattern);
				break;
			}
			
			if (MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				MessageFormat.format(msgConfirmModifyAutoAssignNodepattern, cloudScope.getName(), cloudScope.getId()))) {
				
				try {
					endpoint.registAutoAssigneNodePattern(cloudScope.getId(), dialog.getOutput());
					
					// 成功報告ダイアログを生成
					MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						MessageFormat.format(msgFinishModifyAutoAssignNodepattern, cloudScope.getName(), cloudScope.getId()));
					
					break loop_end;
				} catch (Exception e) {
					logger.error(e.getMessage(), e);

					// 失敗報告ダイアログを生成
					ControlUtil.openError(e, msgErrorFinishModifyAutoAssignNodepattern);
				}
			}
		}
		return null;
	}
}
