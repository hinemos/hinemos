/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmap.composite.JobMapTreeComposite;
import com.clustercontrol.jobmap.view.JobTreeView;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブツリービューの「表示」のクライアント側アクションクラス<BR>
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class OpenJobMapAction extends BaseAction {
	public static final String ID = ActionIdBase + OpenJobMapAction.class.getSimpleName();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);
		
		List<JobTreeItem> itemList = m_jobTreeItemList;
		
		if (itemList == null || itemList.size() != 1) {
			return null;
		}
		JobTreeItem item = itemList.get(0);
		
		//ジョブ[登録]ビューのインスタンスを取得
		IWorkbenchPage page = PlatformUI.getWorkbench()
		.getActiveWorkbenchWindow().getActivePage();
		IViewPart viewPart = page.findView(JobTreeView.ID);

		if (viewPart != null) {

			JobTreeView view = (JobTreeView) viewPart.getAdapter(JobTreeView.class);
			JobMapTreeComposite tree = view.getJobMapTreeComposite();
			
			if (item != null) {
				tree.updateJobMapEditor(item);
				//選択ツリーアイテムを設定
				tree.setSelectItem(itemList);
				
				// ログインユーザで参照可能なジョブユニットかどうかチェックする
				if (item.getData().getType() == JobConstant.TYPE_JOBUNIT) {
					view.setEnabledActionAll(true);
					view.setEnabledAction(item.getData().getType(), item.getData().getJobunitId(), new StructuredSelection(item));
					tree.updateJobMapEditor(item);

				} else {
					//ビューのアクションの有効/無効を設定
					view.setEnabledAction(item.getData().getType(), item.getData().getJobunitId(), new StructuredSelection(item));
					tree.updateJobMapEditor(item);
				}
			} else {
				return null;
			}
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
		
		JobInfo info = m_jobTreeItem.getData();
		Integer type = info.getType();

		this.setBaseEnabled(
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