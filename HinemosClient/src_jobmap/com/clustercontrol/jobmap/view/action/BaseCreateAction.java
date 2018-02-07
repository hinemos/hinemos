/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.dialog.JobDialog;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmap.composite.JobMapTreeComposite;
import com.clustercontrol.jobmap.util.JobMapActionUtil;
import com.clustercontrol.jobmap.view.JobTreeView;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

abstract public class BaseCreateAction  extends BaseAction {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);
		
		JobTreeItem item = null;
		JobTreeItem parent = m_jobTreeItem;

		JobInfo jobInfo = null;
		if (getJobType() == JobConstant.TYPE_JOBUNIT) {
			jobInfo = new JobInfo();
			jobInfo.setJobunitId(parent.getData().getJobunitId());
			jobInfo.setId("");
			jobInfo.setName("");
			jobInfo.setType(JobConstant.TYPE_JOBUNIT);
		} else {
			jobInfo = JobTreeItemUtil.getNewJobInfo(parent.getData().getJobunitId(),
				getJobType());
		}

		item = new JobTreeItem();
		item.setData(jobInfo);
		
		String managerName = null;
		JobTreeItem mgrTree = JobTreeItemUtil.getManager(parent);
		if(mgrTree == null) {
			managerName = parent.getChildren().get(0).getData().getId();
		} else {
			managerName = mgrTree.getData().getId();
		}
		
		JobTreeView view = JobMapActionUtil.getJobTreeView();
		if (view == null) {
			return null;
		}
		JobMapTreeComposite tree = view.getJobMapTreeComposite();

		JobTreeItemUtil.addChildren(parent, item);
		JobDialog dialog = null;
		if (getJobType() == JobConstant.TYPE_REFERJOB) {
			dialog = new JobDialog(
					tree, 
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					managerName,
					false);
		} else {
			dialog = new JobDialog(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					managerName,
					false);
		}
		dialog.setJobTreeItem(item);
		
		//ダイアログ表示
		if (dialog.open() == IDialogConstants.OK_ID) {
			JobEditState editState = JobEditStateUtil.getJobEditState(JobTreeItemUtil.getManagerName(item));
			editState.addEditedJobunit(item);
		} else {
			JobTreeItemUtil.removeChildren(parent, item);
		}

		tree.refresh(parent);
		tree.getTreeViewer().setSelection(tree.getTreeViewer().getSelection(), true);
		tree.updateJobMapEditor(null);

		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		super.updateElement(element, parameters);

		if (m_jobTreeItem == null) {
			this.setBaseEnabled(false);
			return;
		}
		
		JobInfo info = m_jobTreeItem.getData();
		Integer type = info.getType();
		if (getJobType() == JobConstant.TYPE_JOBUNIT) {
			this.setBaseEnabled(type == JobConstant.TYPE_MANAGER);
		} else {
			boolean enable = false;
			String managerName = JobTreeItemUtil.getManagerName(m_jobTreeItem);
			if (managerName != null) {
				JobEditState editState = JobEditStateUtil.getJobEditState(managerName);
				enable = editState.isLockedJobunitId(info.getJobunitId());
			}
			
			this.setBaseEnabled(enable && 
					(type == JobConstant.TYPE_JOBUNIT || 
					type == JobConstant.TYPE_JOBNET));
		}
	}

	abstract public int getJobType();
}