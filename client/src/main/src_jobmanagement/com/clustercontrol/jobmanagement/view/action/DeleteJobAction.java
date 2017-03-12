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

package com.clustercontrol.jobmanagement.view.action;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.composite.JobTreeComposite;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.view.JobListView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;

/**
 * ジョブ[一覧]ビューの「削除」のクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class DeleteJobAction extends AbstractHandler implements IElementUpdater {
	/** ログ */
	private static Log m_log = LogFactory.getLog(DeleteJobAction.class);
	/** アクションID */
	public static final String ID = DeleteJobAction.class.getName();
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

	/**
	 * ジョブ[一覧]ビューの「削除」が押された場合に、ジョブを削除します。
	 * <p>
	 * <ol>
	 * <li>ジョブ[一覧]ビューから選択されているジョブツリーアイテムを取得します。</li>
	 * <li>ジョブ[一覧]ビューから選択されているジョブツリーアイテムの親を取得します。</li>
	 * <li>削除の確認ダイアログを表示します。</li>
	 * <li>親のジョブツリーアイテムから、選択さえたジョブツリーアイテムを削除します。</li>
	 * <li>ジョブ[一覧]ビューを更新します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.composite.JobTreeComposite
	 * @see com.clustercontrol.jobmanagement.composite.JobListComposite
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);

		if (!(viewPart instanceof JobListView)) {
			return null;
		}

		JobListView jobListView = null;
		try {
			jobListView = (JobListView) viewPart.getAdapter(JobListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (jobListView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		List<JobTreeItem> itemList = jobListView.getSelectJobTreeItemList();
		if(null == itemList || itemList.isEmpty()){
			return null;
		}

		//親と子のペアを用意する
		int size = itemList.size();
		JobTreeItem[][] itemAry = new JobTreeItem[size][2];
		for(int i=0; i<size; i++) {
			JobTreeItem item = itemList.get(i);
			itemAry[i][0] = item.getParent();
			itemAry[i][1] = item;
		}

		if(null == itemAry || itemAry.length == 0){
			return null;
		}

		String message = null;

		if(size == 1) {
			message = Messages.getString("job") + "["
					+ itemList.get(0).getData().getId() + "]"
					+ Messages.getString("message.job.2");
		} else {
			Object arg[] = {size, Messages.getString("job")};
			message = Messages.getString("message.job.123", arg);
		}

		// 確認ダイアログを生成
		if (MessageDialog.openQuestion(
				null,
				Messages.getString("confirmed"),
				message)) {

			for(JobTreeItem[] ary : itemAry) {
				JobTreeItem parent = ary[0];
				JobTreeItem item = ary[1];

				JobTreeItemUtil.removeChildren(parent, item);

				JobEditState jobEditState = JobEditStateUtil.getJobEditState(JobTreeItemUtil.getManagerName( parent ));
				if (item.getData().getType() == JobConstant.TYPE_JOBUNIT) {
					// ジョブユニットの削除
					jobEditState.removeEditedJobunit(item);
					if (jobEditState.getLockedJobunitBackup(item.getData()) != null) {
						// マネージャから取得してきたジョブユニット
						jobEditState.addDeletedJobunit(item);
					}
				} else {
					// ジョブユニット以外の削除はジョブユニットの編集にあたる
					jobEditState.addEditedJobunit(item);
				}

				JobTreeComposite tree = jobListView.getJobTreeComposite();
				tree.refresh(parent);
				tree.getTreeViewer().setSelection( new StructuredSelection(parent), true);
			}
		}
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				boolean editEnable = false;
				if(part instanceof JobListView){
					// Enable button when 1 item is selected
					JobListView view = (JobListView)part;

					if(view.getDataType() == JobConstant.TYPE_JOBUNIT ||
							view.getDataType() == JobConstant.TYPE_JOBNET ||
							view.getDataType() == JobConstant.TYPE_JOB ||
							view.getDataType() == JobConstant.TYPE_FILEJOB ||
							view.getDataType() == JobConstant.TYPE_APPROVALJOB ||
							view.getDataType() == JobConstant.TYPE_MONITORJOB ||
							view.getDataType() == JobConstant.TYPE_REFERJOBNET ||
							view.getDataType() == JobConstant.TYPE_REFERJOB){
						editEnable = view.getEditEnable();
					}
				}
				this.setBaseEnabled(editEnable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
