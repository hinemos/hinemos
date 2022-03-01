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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmap.composite.JobMapTreeComposite;
import com.clustercontrol.jobmap.util.JobMapActionUtil;
import com.clustercontrol.jobmap.view.JobTreeView;
import com.clustercontrol.util.Messages;

/**
 * ジョブ[一覧]ビューの「削除」のクライアント側アクションクラス<BR>
 * 
 * @version 2.0.0
 * @since 1.0.0
 */
public class DeleteJobAction extends BaseAction {
	public static final String ID = ActionIdBase + DeleteJobAction.class.getSimpleName();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);
		
		JobTreeItemWrapper item = m_jobTreeItem;
		JobTreeItemWrapper parent = m_jobTreeItem.getParent();
		
		String message = Messages.getString("job") + "["
				+ item.getData().getId() + "]"
				+ Messages.getString("message.job.2");

		// 確認ダイアログを生成
		if (!MessageDialog.openQuestion(
				null,
				Messages.getString("confirmed"),
				message)) {
			return null;
		}
		
		JobTreeView view = JobMapActionUtil.getJobTreeView();
		if (view == null) {
			return null;
		}
		JobTreeItemUtil.removeChildren(parent, item);
		JobEditState editState = JobEditStateUtil.getJobEditState(JobTreeItemUtil.getManagerName(item));

		if (item.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT) {
			// ジョブユニットの削除
			editState.removeEditedJobunit(item);
			if (editState.getLockedJobunitBackup(item.getData()) != null) {
				// マネージャから取得してきたジョブユニット
				editState.addDeletedJobunit(item);
			}
		} else {
			// ジョブユニット以外の削除はジョブユニットの編集にあたる
			editState.addEditedJobunit(item);
		}

		JobMapTreeComposite tree = view.getJobMapTreeComposite();
		tree.refresh(parent);
		tree.getTreeViewer().setSelection( new StructuredSelection(parent), true);
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
		
		JobInfoWrapper info = m_jobTreeItem.getData();
		boolean enable = false;
		String managerName = JobTreeItemUtil.getManagerName(m_jobTreeItem);
		if (managerName != null) {
			JobEditState editState = JobEditStateUtil.getJobEditState(managerName);
			enable = editState.isLockedJobunitId(info.getJobunitId());
		}
		JobInfoWrapper.TypeEnum type = info.getType();
		this.setBaseEnabled(enable && 
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
				type == JobInfoWrapper.TypeEnum.RPAJOB
				));
	}
}