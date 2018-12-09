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

import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmap.composite.JobMapTreeComposite;
import com.clustercontrol.jobmap.util.JobMapActionUtil;
import com.clustercontrol.jobmap.view.JobTreeView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

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
		
		JobTreeItem item = m_jobTreeItem;
		JobTreeItem parent = m_jobTreeItem.getParent();
		
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

		if (item.getData().getType() == JobConstant.TYPE_JOBUNIT) {
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
		
		JobInfo info = m_jobTreeItem.getData();
		boolean enable = false;
		String managerName = JobTreeItemUtil.getManagerName(m_jobTreeItem);
		if (managerName != null) {
			JobEditState editState = JobEditStateUtil.getJobEditState(managerName);
			enable = editState.isLockedJobunitId(info.getJobunitId());
		}
		Integer type = info.getType();
		this.setBaseEnabled(enable && 
				(type == JobConstant.TYPE_JOBUNIT || 
				type == JobConstant.TYPE_JOBNET ||
				type == JobConstant.TYPE_JOB ||
				type == JobConstant.TYPE_FILEJOB ||
				type == JobConstant.TYPE_APPROVALJOB ||
				type == JobConstant.TYPE_MONITORJOB ||
				type == JobConstant.TYPE_REFERJOBNET ||
				type == JobConstant.TYPE_REFERJOB
				));
	}
}