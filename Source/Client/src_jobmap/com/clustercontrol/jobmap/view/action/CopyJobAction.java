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
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmap.util.JobMapActionUtil;
import com.clustercontrol.jobmap.view.JobMapEditorView;
import com.clustercontrol.jobmap.view.JobTreeView;

/**
 * ジョブコピーするクライアント側アクションクラス<BR>
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
public class CopyJobAction extends BaseAction {
	public static final String ID = ActionIdBase  + CopyJobAction.class.getSimpleName();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);

		if (viewPart instanceof JobTreeView) {
			JobTreeView view = (JobTreeView)viewPart;
			JobTreeItemWrapper copyItem = view.getSelectJobTreeItem();
			view.setCopyJobTreeItem(copyItem);
		} else if (viewPart instanceof JobMapEditorView) {
			JobMapEditorView view = (JobMapEditorView)viewPart;
			JobTreeItemWrapper copyItem = view.getFocusFigure().getJobTreeItem();
			JobMapActionUtil.getJobTreeView().setCopyJobTreeItem(copyItem);
		}
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		super.updateElement(element, parameters);

		if (m_jobTreeItem == null) {
			return;
		}
		
		JobInfoWrapper.TypeEnum type = m_jobTreeItem.getData().getType();
		this.setBaseEnabled(
				type == JobInfoWrapper.TypeEnum.JOBUNIT || 
				type == JobInfoWrapper.TypeEnum.JOBNET ||
				type == JobInfoWrapper.TypeEnum.JOB ||
				type == JobInfoWrapper.TypeEnum.FILEJOB ||
				type == JobInfoWrapper.TypeEnum.APPROVALJOB ||
				type == JobInfoWrapper.TypeEnum.MONITORJOB ||
				type == JobInfoWrapper.TypeEnum.FILECHECKJOB ||
				type == JobInfoWrapper.TypeEnum.JOBLINKSENDJOB ||
				type == JobInfoWrapper.TypeEnum.JOBLINKRCVJOB ||
				type == JobInfoWrapper.TypeEnum.REFERJOB ||
				type == JobInfoWrapper.TypeEnum.REFERJOBNET ||
				type == JobInfoWrapper.TypeEnum.RESOURCEJOB ||
				type == JobInfoWrapper.TypeEnum.RPAJOB);
	}
}
