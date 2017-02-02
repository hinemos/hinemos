/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.composite.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.jobmanagement.action.GetJobTableDefine;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.composite.JobListComposite;
import com.clustercontrol.jobmanagement.composite.JobTreeComposite;
import com.clustercontrol.jobmanagement.dialog.JobDialog;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.jobmanagement.view.JobListView;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブ[一覧]ビュー用のテーブルビューア用のDoubleClickListenerクラスです。
 *
 * @version 4.1.0
 */
public class JobDoubleClickListener implements IDoubleClickListener {

	private static Log m_log = LogFactory.getLog( JobDoubleClickListener.class );

	/** ジョブ[一覧]ビュー用のコンポジット */
	private JobListComposite m_composite;

	/**
	 * コンストラクタ
	 *
	 * @param composite ジョブ[一覧]ビュー用のコンポジット
	 */
	public JobDoubleClickListener(JobListComposite composite) {
		m_composite = composite;
	}

	/**
	 * ダブルクリック時に呼び出されます。<BR>
	 * ジョブ[一覧]ビューのテーブルビューアをダブルクリックした際に、選択した行の内容をダイアログで表示します。
	 * <P>
	 * <ol>
	 * <li>イベントから選択行を取得し、選択行からジョブIDを取得します。</li>
	 * <li>ジョブ[一覧]ビュー用のコンポジットからジョブツリーアイテムを取得します。</li>
	 * <li>取得したジョブツリーアイテムから、ジョブIDが一致するジョブツリーアイテムを取得します。</li>
	 * <li>ジョブ[一覧]ビュー用のコンポジットに、ジョブIDが一致するジョブツリーアイテムを設定します。</li>
	 * <li>ジョブIDからジョブ情報を取得し、ダイアログで表示します。</li>
	 * </ol>
	 *
	 * @param event イベント
	 *
	 * @see com.clustercontrol.jobmanagement.dialog.JobDialog
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		JobTreeItem selectJobTreeItem = null;

		//ジョブ[登録]ビューのインスタンスを取得
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IViewPart viewPart = page.findView(JobListView.ID);


		//選択アイテムを取得
		StructuredSelection selection = (StructuredSelection) event.getSelection();

		if ( viewPart != null && selection != null ){
			ArrayList<?> item = (ArrayList<?>) selection.getFirstElement();
			JobListView view = (JobListView) viewPart.getAdapter(JobListView.class);
			if (view == null) {
				m_log.info("selection changed: job list view is null");
				return;
			}

			if(item != null){
				String jobId = (String) item.get(GetJobTableDefine.JOB_ID);

				if (m_composite.getJobTreeItem() != null) {
					List<JobTreeItem> items = m_composite.getJobTreeItem().getChildren();

					for (int i = 0; i < items.size(); i++) {
						if (jobId.equals(items.get(i).getData().getId())) {
							selectJobTreeItem = items.get(i);
							break;
						}
					}
				}
			}

			if (selectJobTreeItem != null) {
				//選択ツリーアイテムを設定
				List<JobTreeItem> list = new ArrayList<JobTreeItem>();
				list.add(selectJobTreeItem);
				m_composite.setSelectJobTreeItemList(list);

				String managerName = null;
				JobTreeItem mgrTree = JobTreeItemUtil.getManager(selectJobTreeItem);
				if(mgrTree == null) {
					managerName = selectJobTreeItem.getChildren().get(0).getData().getId();
				} else {
					managerName = mgrTree.getData().getId();
				}
				JobEditState jobEditState = JobEditStateUtil.getJobEditState(managerName);
				boolean readOnly = !jobEditState.isLockedJobunitId(selectJobTreeItem.getData().getJobunitId());

				// ダイアログを生成
				JobDialog dialog = new JobDialog(view.getJobTreeComposite(), m_composite.getShell(), managerName, readOnly);

				// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
				dialog.setJobTreeItem(selectJobTreeItem);
				m_log.info("jobDialog " + selectJobTreeItem.getData().getId());
				//ダイアログ表示
				if (dialog.open() == IDialogConstants.OK_ID) {
					m_composite.update(selectJobTreeItem.getParent());
					if (jobEditState.isLockedJobunitId(selectJobTreeItem.getData().getJobunitId())) {
						// 編集モードのジョブが更新された場合(ダイアログで編集モードになったものを含む）
						jobEditState.addEditedJobunit(selectJobTreeItem);
						if (selectJobTreeItem.getData().getType() == JobConstant.TYPE_JOBUNIT) {
							JobUtil.setJobunitIdAll(selectJobTreeItem, selectJobTreeItem.getData().getJobunitId());
						}
					}

					// Refresh after modified
					// @see ModifyJobAction#run()
					JobTreeItem parent = selectJobTreeItem.getParent();
					JobTreeComposite tree = view.getJobTreeComposite();
					tree.getTreeViewer().sort(parent);
					tree.refresh(item);
					// Also fresh parent in case of selecting from list
					tree.refresh(parent);
				}
			}
		}
	}

}
