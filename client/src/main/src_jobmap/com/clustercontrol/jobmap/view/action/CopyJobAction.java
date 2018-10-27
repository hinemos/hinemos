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

import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmap.util.JobMapActionUtil;
import com.clustercontrol.jobmap.view.JobMapEditorView;
import com.clustercontrol.jobmap.view.JobTreeView;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

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
			JobTreeItem copyItem = view.getSelectJobTreeItem();
			view.setCopyJobTreeItem(copyItem);
		} else if (viewPart instanceof JobMapEditorView) {
			JobMapEditorView view = (JobMapEditorView)viewPart;
			JobTreeItem copyItem = view.getFocusFigure().getJobTreeItem();
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
		
		Integer type = m_jobTreeItem.getData().getType();
		this.setBaseEnabled(
				type == JobConstant.TYPE_JOBUNIT || 
				type == JobConstant.TYPE_JOBNET ||
				type == JobConstant.TYPE_JOB ||
				type == JobConstant.TYPE_FILEJOB ||
				type == JobConstant.TYPE_APPROVALJOB ||
				type == JobConstant.TYPE_MONITORJOB ||
				type == JobConstant.TYPE_REFERJOB ||
				type == JobConstant.TYPE_REFERJOBNET);
	}
}
