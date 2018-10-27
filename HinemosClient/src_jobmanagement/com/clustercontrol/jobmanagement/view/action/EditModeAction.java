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
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.composite.JobTreeComposite;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.jobmanagement.view.JobListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.jobmanagement.OtherUserGetLock_Exception;

/**
 * ジョブ[一覧]ビューの「編集モード」のクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class EditModeAction extends AbstractHandler implements IElementUpdater {
	// ログ
	private static Log m_log = LogFactory.getLog( EditModeAction.class );

	/** アクションID */
	public static final String ID = EditModeAction.class.getName();
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
	 * ジョブ[一覧]ビューの「編集モード」が押された場合に、編集ロックを取得します。
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.view.JobPlanListView
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
		// ジョブが正しく選択されていないときに警告を出すためのフラグ

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

		// 選択されたジョブツリーアイテム
		JobTreeItem selectedItem = null;
		// 編集モードにするジョブユニット
		JobTreeItem item = null;
		JobTreeItem parent = null;

		selectedItem = jobListView.getSelectJobTreeItemList().get(0);
		ICommandService commandService = (ICommandService)window.getService(ICommandService.class);
		Command command = commandService.getCommand(ID);
		State state = command.getState(RegistryToggleState.STATE_ID);
		boolean isChecked = jobListView.getEditEnable();

		if( selectedItem != null ){
			item = JobUtil.getTopJobUnitTreeItem(selectedItem);
		}else{
			// 編集モードボタンをOFFにする
			state.setValue(false);
		}
		if( item != null ){
			parent = item.getParent();
		}
		if( parent == null ){
			// ジョブツリーアイテムが選択されていないときには、メッセージを表示する
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.job.110"));
			return null;
		}

		String jobunitId = item.getData().getJobunitId();
		String managerName = "";
		JobTreeItem managerTree = JobTreeItemUtil.getManager(item);
		if ( managerTree == null)
			throw new InternalError("JobTreeItem is null");

		managerName = managerTree.getData().getName();
		JobEditState jobEditState = JobEditStateUtil.getJobEditState( managerName );
		if (!isChecked) {
			if (jobEditState.getLockedJobunitList().contains(item.getData())) {
				// ここにくることはないはず(編集モードにいるのに編集モードに入るアクション)
				return null;
			}
			// 編集モードに入る
			Long updateTime = jobEditState.getJobunitUpdateTime(jobunitId);
			Integer result = null;
			try {
				result =JobUtil.getEditLock(managerName, jobunitId, updateTime, false);
			} catch (OtherUserGetLock_Exception e) {
				// 他のユーザがロックを取得している
				String message = HinemosMessage.replace(e.getMessage());
				if (MessageDialog.openQuestion(
						null,
						Messages.getString("confirmed"),
						message)) {
					try {
						result = JobUtil.getEditLock(managerName, jobunitId, updateTime, true);
					} catch (Exception e1) {
						// ここには絶対にこないはず
						m_log.error("run() : logical error");
					}
				}
			}

			if (result != null) {
				// ロックを取得した
				m_log.debug("run() : get editLock(jobunitId="+jobunitId+")");
				jobEditState.addLockedJobunit(item.getData(), JobTreeItemUtil.clone(item, null), result);
				jobListView.getJobTreeComposite().refresh(parent);
				JobTreeComposite tree = jobListView.getJobTreeComposite();
				tree.getTreeViewer().setSelection(
						new StructuredSelection(selectedItem), true);
			} else {
				// ロックの取得に失敗した
				m_log.debug("run() : cannot get editLock(jobunitId="+jobunitId+")");
				state.setValue(false);
			}
		} else {
			// 編集モードから抜ける
			if (!jobEditState.getLockedJobunitList().contains(item.getData())) {
				// ここにくることはないはず(編集モードにいないのに編集モードから抜けるアクション)
				return null;
			}
			try {
				if (MessageDialog.openQuestion(
						null,
						Messages.getString("confirmed"),
						Messages.getString("message.job.103"))) {
					// 編集ロックの開放
					JobTreeItem manager = JobTreeItemUtil.getManager(item);
					JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
					wrapper.releaseEditLock(jobEditState.getEditSession(item.getData()));

					//バックアップに切り戻す
					JobTreeItem backup = jobEditState.getLockedJobunitBackup(item.getData());
					JobTreeItemUtil.removeChildren(parent, item);
					if (backup != null) {
						JobPropertyUtil.setJobFullTree(manager.getData().getName(), backup);
						JobTreeItemUtil.addChildren(parent, backup);
					}

					jobEditState.exitEditMode(item);
					jobListView.getJobTreeComposite().getTreeViewer().sort(parent);
					jobListView.getJobTreeComposite().refresh();
				} else {
					//編集ロックを開放しない
					state.setValue(false);
				}
			} catch (InvalidRole_Exception e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				m_log.warn("updateJobunitUpdateTime() : " + e.getMessage(), e);
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
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
				if( part instanceof JobListView  ){
					// Enable button when 1 item is selected
					JobListView view = (JobListView)part;
					element.setChecked(view.getEditEnable());

					int size = view.getJobTreeComposite().getSelectItemList().size();
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
				this.setBaseEnabled( editEnable );
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
