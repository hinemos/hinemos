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
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.jobmanagement.dialog.JobDialog;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.jobmap.composite.JobMapTreeComposite;
import com.clustercontrol.jobmap.util.JobMapActionUtil;
import com.clustercontrol.jobmap.view.JobTreeView;

/**
 * ジョブ[一覧]ビューの「変更」のクライアント側アクションクラス<BR>
 * 
 * @version 2.0.0
 * @since 1.0.0
 */
public class ModifyJobAction extends BaseAction {
	public static final String ID = ActionIdBase + ModifyJobAction.class.getSimpleName();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);

		JobTreeItemWrapper item = m_jobTreeItem;
		JobTreeItemWrapper parent = m_jobTreeItem.getParent();
		if (parent == null) {
			return null;
		}
		
		String managerName = null;
		JobTreeItemWrapper mgrTree = JobTreeItemUtil.getManager(parent);
		if(mgrTree == null) {
			managerName = parent.getChildren().get(0).getData().getId();
		} else {
			managerName = mgrTree.getData().getId();
		}
		
		JobEditState editState = JobEditStateUtil.getJobEditState(managerName);
		
		JobTreeView view = JobMapActionUtil.getJobTreeView();
		if (view == null) {
			return null;
		}

		boolean readOnly = !editState.isLockedJobunitId(item.getData().getJobunitId());
		JobDialog dialog = new JobDialog(view.getJobMapTreeComposite(),
				HandlerUtil.getActiveWorkbenchWindow( event ).getShell(), managerName,
				readOnly);
		dialog.setJobTreeItem(item);
		// ダイアログ表示
		if (dialog.open() == IDialogConstants.OK_ID) {
			if (editState.isLockedJobunitId(item.getData().getJobunitId())) {
				// 編集モードのジョブが更新された場合(ダイアログで編集モードになったものを含む）
				editState.addEditedJobunit(item);
				if (item.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT) {
					JobUtil.setJobunitIdAll(item, item.getData().getJobunitId());
				}
			}

			// Refresh after modified
			JobMapTreeComposite tree = view.getJobMapTreeComposite();
			tree.refresh(parent);
			tree.getTreeViewer().setSelection(tree.getTreeViewer().getSelection(), true);
			tree.updateJobMapEditor(null);
		}
		
		return null;
	}
	
	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		super.updateElement(element, parameters);

		if (m_jobTreeItem == null) {
			this.setBaseEnabled(false);
			return;
		}
		
		JobInfoWrapper info = m_jobTreeItem.getData();
		JobInfoWrapper.TypeEnum type = info.getType();
		this.setBaseEnabled(
				(type == JobInfoWrapper.TypeEnum.JOBUNIT || 
				type == JobInfoWrapper.TypeEnum.JOBNET ||
				type == JobInfoWrapper.TypeEnum.JOB ||
				type == JobInfoWrapper.TypeEnum.FILEJOB ||
				type == JobInfoWrapper.TypeEnum.APPROVALJOB ||
				type == JobInfoWrapper.TypeEnum.MONITORJOB ||
				type == JobInfoWrapper.TypeEnum.FILECHECKJOB ||
				type == JobInfoWrapper.TypeEnum.JOBLINKSENDJOB ||
				type == JobInfoWrapper.TypeEnum.JOBLINKRCVJOB ||
				type == JobInfoWrapper.TypeEnum.REFERJOBNET ||
				type == JobInfoWrapper.TypeEnum.REFERJOB ||
				type == JobInfoWrapper.TypeEnum.RESOURCEJOB ||
				type == JobInfoWrapper.TypeEnum.RPAJOB));
	}
}