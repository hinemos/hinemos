/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.jobmanagement.dialog.JobDialog;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmap.view.JobMapHistoryView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブ貼り付けするクライアント側アクションクラス<BR>
 * 
 */
public class ShowJobAction extends BaseAction {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);

		if (!(viewPart instanceof JobMapHistoryView)) {
			return null;
		}
		
		if (m_jobTreeItem == null) {
			return null;
		}
		
		String jobunitId = m_jobTreeItem.getData().getJobunitId();
		String jobId = m_jobTreeItem.getData().getId();
		JobMapHistoryView view = (JobMapHistoryView)viewPart;
		String sessionId = view.getCanvasComposite().getSessionId();
		String managerName = view.getCanvasComposite().getManagerName();
		
		if(sessionId != null && sessionId.length() > 0 &&
				jobunitId != null && jobunitId.length() > 0 &&
				jobId != null && jobId.length() > 0){
			JobTreeItem item = null;
			try {
				item = JobEndpointWrapper.getWrapper(managerName).getSessionJobInfo(sessionId, jobunitId, jobId);
			} catch (InvalidRole_Exception e) {
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
			if(item != null){
				JobDialog dialog = new JobDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						managerName,
						true);

				dialog.setJobTreeItem(item);
				dialog.open();
			}
		}
		
		return null;
	}
}
