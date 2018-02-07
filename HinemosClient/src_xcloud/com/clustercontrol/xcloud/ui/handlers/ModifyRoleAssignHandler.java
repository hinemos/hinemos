/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.ui.handlers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.util.Messages;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.ILoginUser;
import com.clustercontrol.xcloud.model.cloud.RoleRelation;
import com.clustercontrol.xcloud.ui.dialogs.EditAssignRoleDialog;
import com.clustercontrol.xcloud.ui.views.HinemosRole;
import com.clustercontrol.xcloud.util.ControlUtil;

public class ModifyRoleAssignHandler extends AbstractHandler implements CloudStringConstants {
	
	private static final Log logger = LogFactory.getLog(ModifyRoleAssignHandler.class);
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getCurrentSelection(event);
		HinemosRole role = (HinemosRole)selection.getFirstElement();
		
		EditAssignRoleDialog dialog = new EditAssignRoleDialog(HandlerUtil.getActiveShell(event), role);

		if (dialog.open() != Window.OK)
			return null;
			
		if (MessageDialog.openConfirm(
			null,
			Messages.getString("confirmed"),
			msgConfirmModifyRoleRelations)) {
			
			List<EditAssignRoleDialog.DialogOutput> output = new ArrayList<>(dialog.getOutput());
			try {
				for(ICloudScope scope: role.getManager().getCloudScopes().getCloudScopes()){
					for(ILoginUser user: scope.getLoginUsers().getLoginUsers()){
						for(RoleRelation relation: user.getRoleRelations()){
							if(relation.getId().equals(role.getRoleInfo().getRoleId())){
								int index = indexOf(output, scope.getId(), user.getId());
								if(index != -1){
									output.remove(index);
								} else {
									user.removeRoleRelation(role.getRoleInfo().getRoleId());
								}
							}
						}
					}
				}

				for(EditAssignRoleDialog.DialogOutput item: output){
					role.getManager().getCloudScopes().getCloudScope(item.getCloudScopeId()).getLoginUsers().getLoginUser(item.getCloudUserId()).addRoleRelation(role.getRoleInfo().getRoleId());
				}
			} catch (CloudModelException e) {
				logger.error(e.getCause().getMessage(), e.getCause());

				// 失敗報告ダイアログを生成
				ControlUtil.openError(e.getCause(), msgErrorFinishModifyRoleRelations);
				return null;
			}

			// 成功報告ダイアログを生成
			MessageDialog.openInformation(
				null,
				Messages.getString("successful"),
				msgFinishModifyRoleRelations);
		}
		else {
			return null;
		}
		return null;
	}
	
	private int indexOf(List<EditAssignRoleDialog.DialogOutput> list, String scopeId, String userId){
		for(EditAssignRoleDialog.DialogOutput item: list){
			if(item.getCloudScopeId().equals(scopeId) && item.getCloudUserId().equals(userId)){
				return list.indexOf(item);
			}
		}
		return -1;
	}
}
