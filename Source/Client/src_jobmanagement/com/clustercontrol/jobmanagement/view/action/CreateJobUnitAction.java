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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;

import com.clustercontrol.jobmanagement.composite.JobTreeComposite;
import com.clustercontrol.jobmanagement.dialog.JobDialog;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.view.JobListView;
import com.clustercontrol.util.Messages;

/**
 * ジョブ[一覧]ビューの「ジョブユニットの作成」のクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class CreateJobUnitAction extends AbstractHandler implements IElementUpdater {
	/** ログ */
	private static Log m_log = LogFactory.getLog(CreateJobUnitAction.class);
	/** アクションID */
	public static final String ID = CreateJobUnitAction.class.getName();
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
	 * ジョブ[一覧]ビューの「ジョブユニットの作成」が押された場合に、ジョブユニットを作成します。
	 * <p>
	 * <ol>
	 * <li>ジョブ[一覧]ビューから親となるジョブツリーアイテムを取得します。</li>
	 * <li>ジョブユニット用のジョブ情報を作成し、親のジョブツリーアイテムの子として追加します。</li>
	 * <li>ジョブ[ジョブユニットの作成・変更]ダイアログを表示します。</li>
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

		JobTreeItemWrapper jobTreeItem = JobEditStateUtil.getJobTreeItem();
		if (jobTreeItem == null) {
			// ジョブのキャッシュ情報が最新でないため処理終了
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.46"));
			return null;
		}

		JobTreeComposite tree = view.getJobTreeComposite();
		parent = view.getSelectJobTreeItemList().get(0);

		if (parent != null) {

			JobInfoWrapper jobInfo = JobTreeItemUtil.createJobInfoWrapper();
			jobInfo.setJobunitId(parent.getData().getJobunitId());
			jobInfo.setId("");
			jobInfo.setName("");
			jobInfo.setType(JobInfoWrapper.TypeEnum.JOBUNIT);
			jobInfo.setPropertyFull(true);

			item = new JobTreeItemWrapper();
			item.setData(jobInfo);
			JobTreeItemUtil.addChildren(parent, item);

			String managerName = null;
			JobTreeItemWrapper mgrTree = JobTreeItemUtil.getManager(parent);
			if(mgrTree == null) {
				managerName = parent.getChildren().get(0).getData().getId();
			} else {
				managerName = mgrTree.getData().getId();
			}

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
					element.setChecked(view.getEditEnable());

					if(view.getDataType() == JobInfoWrapper.TypeEnum.MANAGER){
						editEnable = true;
					}
				}
				this.setBaseEnabled(editEnable);
			}
		}
	}
}
