/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view.action;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;

import com.clustercontrol.jobmanagement.composite.JobListComposite;
import com.clustercontrol.jobmanagement.composite.JobTreeComposite;
import com.clustercontrol.jobmanagement.dialog.JobDialog;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.view.JobListView;

/**
 * ジョブ[一覧]ビューの「ジョブネットの作成」のクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class CreateJobNetAction extends AbstractHandler implements IElementUpdater {
	/** ログ */
	private static Log m_log = LogFactory.getLog(CreateJobNetAction.class);
	/** アクションID */
	public static final String ID = CreateJobNetAction.class.getName();
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
	 * ジョブ[一覧]ビューの「ジョブネットの作成」が押された場合に、ジョブネットを作成します。
	 * <p>
	 * <ol>
	 * <li>ジョブ[一覧]ビューから親となるジョブツリーアイテムを取得します。</li>
	 * <li>ジョブネット用のジョブ情報を作成し、親のジョブツリーアイテムの子として追加します。</li>
	 * <li>ジョブ[ジョブネットの作成・変更]ダイアログを表示します。</li>
	 * <li>ジョブ[一覧]ビューを更新します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.dialog.JobDialog
	 * @see com.clustercontrol.jobmanagement.view.JobListView
	 * @see com.clustercontrol.jobmanagement.composite.JobTreeComposite
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		JobTreeItemWrapper item = null;
		JobTreeItemWrapper parent = null;

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

		JobListView view = null; 
		try {
			view = (JobListView) viewPart.getAdapter(JobListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		JobTreeComposite tree = view.getJobTreeComposite();
		parent = view.getSelectJobTreeItemList().get(0);

		if (parent != null) {
			JobInfoWrapper jobInfo = JobTreeItemUtil.getNewJobInfo(parent.getData().getJobunitId(),
					JobInfoWrapper.TypeEnum.JOBNET);
			item = new JobTreeItemWrapper();
			item.setData(jobInfo);
			String managerName = null;
			JobTreeItemWrapper mgrTree = JobTreeItemUtil.getManager(parent);
			if(mgrTree == null) {
				managerName = parent.getChildren().get(0).getData().getId();
			} else {
				managerName = mgrTree.getData().getId();
			}
			// JobPropertyUtil.setJobFull(item.getData()); // 不要？
			JobTreeItemUtil.addChildren(parent, item);
			JobDialog dialog = new JobDialog(HandlerUtil.getActiveWorkbenchWindow( event ).getShell(),
					managerName, false);
			dialog.setJobTreeItem(item);

			//ダイアログ表示
			if (dialog.open() == IDialogConstants.OK_ID) {
				JobEditStateUtil.getJobEditState(managerName).addEditedJobunit(item);
			} else {
				JobTreeItemUtil.removeChildren(parent, item);
			}
			tree.getTreeViewer().sort(parent);
			tree.refresh(parent);
			tree.getTreeViewer().setSelection( new StructuredSelection(item), true );
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

					Composite comp = view.getLastFocusComposite();
					int size = 0;
					if(comp instanceof JobTreeComposite) {
						size = view.getJobTreeComposite().getSelectItemList().size();
					} else if(comp instanceof JobListComposite) {
						size = view.getJobListComposite().getSelectItemList().size();
					}
					if(size == 1) {
						if(view.getDataType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
								view.getDataType() == JobInfoWrapper.TypeEnum.JOBNET){
							editEnable = view.getEditEnable();
						}
					}
				}
				this.setBaseEnabled(editEnable);
			}
		}
	}
}
