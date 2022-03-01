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

import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.model.cloud.IInstance;
import com.clustercontrol.xcloud.util.ControlUtil;

public class ManualNodeAssignHandler extends AbstractCloudOptionHandler implements CloudStringConstants {
	
	private static final Log logger = LogFactory.getLog(ManualNodeAssignHandler.class);
	
	@Override
	public Object internalExecute(ExecutionEvent event) throws CloudManagerException {
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

			try {
				for (IInstance instance: instances) {
					String managerName = instance.getCloudScope().getCloudScopes().getHinemosManager().getManagerName();
					RepositoryRestClientWrapper repoWrapper = RepositoryRestClientWrapper.getWrapper(managerName);
					repoWrapper.addNodeAndAssignScopeFromInstance(instance.getCloudScope().getId(), instance.getLocation().getId(), instance.getId());
				}
				// 成功報告ダイアログを生成
				MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						msgFinishManualRegistNodeModify);

			}catch(Exception e) {
				// 失敗報告ダイアログを生成
				ControlUtil.openError(e, getErrorMessage());
				logger.warn(e);
			}finally{
				Display.getCurrent().asyncExec(new Runnable() {
					@Override
					public void run() {
						selected.getLocation().updateLocation();
					}
				});
			}
		}
		return null;
	}

	@Override
	protected String getErrorMessage() {
		return msgErrorFinishManualRegistNodeModify;
	}
}
