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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

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
 * ジョブ[一覧]ビューの「変更」のクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class ModifyJobAction extends AbstractHandler implements IElementUpdater {
	/** ログ */
	private static Log m_log = LogFactory.getLog(ModifyJobAction.class);
	/** アクションID */
	public static final String ID = ModifyJobAction.class.getName();
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
	 * ジョブ[一覧]ビューの「変更」が押された場合に、ジョブを変更します。
	 * <p>
	 * <ol>
	 * <li>ジョブ[一覧]ビューから選択されたジョブツリーアイテムを取得します。</li>
	 * <li>ジョブ[一覧]ビューから選択されたジョブツリーアイテムの親を取得します。</li>
	 * <li>選択したジョブツリーアイテムのジョブ種別に合わせて、ジョブ[ジョブの作成・変更]ダイアログを表示します。</li>
	 * <li>ジョブ[一覧]ビューを更新します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.dialog.JobDialog
	 * @see com.clustercontrol.jobmanagement.view.JobListView
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

		JobTreeItem item = jobListView.getSelectJobTreeItemList().get(0);
		if (null == item) {
			return null;
		}

		JobTreeItem parent = item.getParent();
		if (null != parent) {
			String managerName = null;
			JobTreeItem mgrTree = JobTreeItemUtil.getManager(parent);
			if(mgrTree == null) {
				managerName = parent.getChildren().get(0).getData().getId();
			} else {
				managerName = mgrTree.getData().getId();
			}

			JobEditState jobEditState = JobEditStateUtil.getJobEditState( managerName );
			boolean readOnly = !jobEditState.isLockedJobunitId(item.getData().getJobunitId());
			JobDialog dialog = new JobDialog(jobListView.getJobTreeComposite(), HandlerUtil.getActiveWorkbenchWindow( event ).getShell(), managerName,
					readOnly);
			dialog.setJobTreeItem(item);

			// ダイアログ表示
			if (dialog.open() == IDialogConstants.OK_ID) {
				if (jobEditState.isLockedJobunitId(item.getData().getJobunitId())) {
					// 編集モードのジョブが更新された場合(ダイアログで編集モードになったものを含む）
					jobEditState.addEditedJobunit(item);
					if (item.getData().getType() == JobConstant.TYPE_JOBUNIT) {
						JobUtil.setJobunitIdAll(item, item.getData().getJobunitId());
					}
				}

				// Refresh after modified
				JobTreeComposite tree = jobListView.getJobTreeComposite();
				tree.getTreeViewer().sort(parent);
				tree.refresh(item);
				// Also fresh parent in case of selecting from list
				tree.refresh(parent);
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
					Composite comp = view.getLastFocusComposite();
					int size = 0;
					if(comp instanceof JobTreeComposite) {
						size = view.getJobTreeComposite().getSelectItemList().size();
					} else if(comp instanceof JobListComposite) {
						size = view.getJobListComposite().getSelectItemList().size();
					}
					if(size == 1) {
						if(view.getDataType() == JobConstant.TYPE_JOBUNIT ||
								view.getDataType() == JobConstant.TYPE_JOBNET ||
								view.getDataType() == JobConstant.TYPE_JOB ||
								view.getDataType() == JobConstant.TYPE_FILEJOB ||
								view.getDataType() == JobConstant.TYPE_APPROVALJOB ||
								view.getDataType() == JobConstant.TYPE_MONITORJOB ||
								view.getDataType() == JobConstant.TYPE_REFERJOBNET ||
								view.getDataType() == JobConstant.TYPE_REFERJOB){
							editEnable = true;
						}
					}
				}
				this.setBaseEnabled(editEnable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
