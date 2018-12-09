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
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.cloud.CloudCommonEndpoint;
import com.clustercontrol.ws.cloud.HinemosUnknown_Exception;
import com.clustercontrol.ws.cloud.InvalidRole_Exception;
import com.clustercontrol.ws.cloud.InvalidUserPass_Exception;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.cloud.IInstance;

public class RegistAgentHandler extends AbstractCloudOptionHandler implements CloudStringConstants {
	
	private List<String> facilityIds;
	
	@Override
	public Object internalExecute(ExecutionEvent event) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		IStructuredSelection selection = (IStructuredSelection)HandlerUtil.getActiveSite(event).getSelectionProvider().getSelection();
		IInstance instance = (IInstance)selection.getFirstElement();

		facilityIds = new ArrayList<>();
		for (@SuppressWarnings("rawtypes") Iterator iter = selection.iterator(); iter.hasNext();) {
			facilityIds.add(((IInstance)iter.next()).getFacilityId());
		}

		if (MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				facilityIds.size() > 1 ? MessageFormat.format(msgConfirmAgentRegistMulti, facilityIds.size()):
					MessageFormat.format(msgConfirmAgentRegist, instance.getName(), instance.getId()))) {
			CloudCommonEndpoint endpoint = instance.getCloudScope().getCloudScopes().getHinemosManager().getEndpoint(CloudCommonEndpoint.class);

			List<String> successful = new ArrayList<>();
			List<String> failed = new ArrayList<>();
			for (String facilityId : facilityIds) {
				if(endpoint.sendManagerDiscoveryInfo(facilityId)) {
					successful.add(facilityId);
				} else {
					failed.add(facilityId);
				}
			}

			// 成功報告ダイアログを生成
			if (!successful.isEmpty()) {
				MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						msgFinishSuccessfullAgentRegist);
			}
			// 失敗報告ダイアログを生成
			if(!failed.isEmpty()){
				MessageDialog.openWarning(
						null,
						Messages.getString("failed"),
						MessageFormat.format(msgErrorFinishAgentRegist, failed.size()));
			}
		}
		return null;
	}

	@Override
	protected String getErrorMessage() {
		return MessageFormat.format(msgErrorFinishAgentRegist, facilityIds.size());
	}
}
