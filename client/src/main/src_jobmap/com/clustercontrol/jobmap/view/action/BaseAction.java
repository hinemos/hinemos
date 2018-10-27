/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.viewer.JobTreeViewer;
import com.clustercontrol.jobmap.figure.JobFigure;
import com.clustercontrol.jobmap.util.JobMapActionUtil;
import com.clustercontrol.jobmap.util.JobMapTreeUtil;
import com.clustercontrol.jobmap.view.JobMapEditorView;
import com.clustercontrol.jobmap.view.JobMapHistoryView;
import com.clustercontrol.jobmap.view.JobModuleView;
import com.clustercontrol.jobmap.view.JobTreeView;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

public class BaseAction  extends AbstractHandler implements IElementUpdater {
	protected IWorkbenchWindow window;
	protected IWorkbenchPart viewPart;
	protected JobTreeItem m_jobTreeItem = null;
	protected List<JobTreeItem> m_jobTreeItemList = null;
	protected static final String ActionIdBase = "com.clustercontrol.enterprise.jobmap.view.action.";
	
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		if( this.window == null || !isEnabled() ){
			return null;
		}

		this.viewPart = HandlerUtil.getActivePart(event);
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}
		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			return;
		}
		viewPart = page.getActivePart();
		
		if (viewPart instanceof JobMapEditorView) {
			JobMapEditorView view = (JobMapEditorView) viewPart;
			JobFigure figure = (JobFigure) view.getCanvasComposite()
					.getSelection();
			if (figure == null) {
				return;
			}
			
			m_jobTreeItem =  figure.getJobTreeItem();
			m_jobTreeItemList = new ArrayList<JobTreeItem>();
			m_jobTreeItemList.add(m_jobTreeItem);
		} else if (viewPart instanceof JobModuleView) {
			JobModuleView view = (JobModuleView) viewPart;
			// JobTreeModuleRegistViewのitemは不完全コピーのため、JobTreeからコピーする
			JobTreeItem jobTreeItem = view.getSelectJobTreeItem();
			JobTreeViewer treeViewer = JobMapActionUtil.getJobTreeView().getJobMapTreeComposite().getTreeViewer();
			if (jobTreeItem != null) {
				String jobunitId = jobTreeItem.getData().getJobunitId();
				String jobId = jobTreeItem.getData().getId();
				String managerName = JobTreeItemUtil.getManagerName(jobTreeItem);
				m_jobTreeItem = JobMapTreeUtil.getTargetJobTreeItem(treeViewer, managerName, jobunitId, jobId);
			} else {
				m_jobTreeItem = null;
			}
			List<JobTreeItem> jobTreeItemList = view.getSelectJobTreeItemList();
			List<JobTreeItem> selectItemList = new ArrayList<>();
			for (JobTreeItem item : jobTreeItemList) {
				String jobunitId = item.getData().getJobunitId();
				String jobId = item.getData().getId();
				String managerName = JobTreeItemUtil.getManagerName(item);
				selectItemList.add(JobMapTreeUtil.getTargetJobTreeItem(treeViewer, managerName, jobunitId, jobId));
			}
			m_jobTreeItemList = selectItemList;
		} else if (viewPart instanceof JobTreeView) {
			JobTreeView view = (JobTreeView) viewPart;
			m_jobTreeItem = view.getSelectJobTreeItem();
			m_jobTreeItemList = view.getSelectJobTreeItemList();
		} else if (viewPart instanceof JobMapHistoryView) {
			JobMapHistoryView view = (JobMapHistoryView) viewPart;
			JobFigure figure = (JobFigure) view.getCanvasComposite().getSelection();
			if (figure == null) {
				return;
			}
			
			m_jobTreeItem = figure.getJobTreeItem();
		}
	}
}