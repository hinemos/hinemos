/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.jobmap.composite.JobMapTreeComposite;
import com.clustercontrol.jobmap.util.JobMapActionUtil;
import com.clustercontrol.jobmap.view.JobMapEditorView;
import com.clustercontrol.jobmap.view.JobTreeView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.jobmanagement.OtherUserGetLock_Exception;

/**
 * ジョブ[一覧]ビューの「編集モード」のクライアント側アクションクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class EditModeAction extends BaseAction {
	private static Log m_log = LogFactory.getLog( EditModeAction.class );
	public static final String ID = ActionIdBase + EditModeAction.class.getSimpleName();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);
		
		JobTreeItem item = JobUtil.getTopJobUnitTreeItem(m_jobTreeItem);
		JobTreeItem parent = item.getParent();
		
		JobTreeView view = JobMapActionUtil.getJobTreeView();
		if (view == null) {
			this.setBaseEnabled(false);
			return null;
		}
		
		JobMapTreeComposite tree = view.getJobMapTreeComposite();

		m_log.debug("run() : jobId="+item.getData().getId());
		String jobunitId = item.getData().getJobunitId();

		String managerName = "";
		JobTreeItem managerTree = JobTreeItemUtil.getManager(item);
		if (managerTree == null) {
			return null;
		}
		managerName = managerTree.getData().getName();

		ICommandService commandService = (ICommandService)window.getService(ICommandService.class);
		Command command = commandService.getCommand(ID);
		State state = command.getState(RegistryToggleState.STATE_ID);
		
		JobEditState editState = JobEditStateUtil.getJobEditState(managerName);
		boolean isChecked = editState.isLockedJobunitId(item.getData().getJobunitId());
		
		if (!isChecked) {
			if (editState.isLockedJobunitId(jobunitId)) {
				// ここにくることはないはず(編集モードにいるのに編集モードに入るアクション)
				m_log.debug("run() : jobunit(" + jobunitId +") is already locked.");
				return null;
			}
			// 編集モードに入る					
			Long updateTime = editState.getJobunitUpdateTime(jobunitId);
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
				editState.addLockedJobunit(item.getData(), JobTreeItemUtil.clone(item, null), result);
				m_log.debug("run() : get editLock(jobunitId="+jobunitId+")");
				tree.getTreeViewer().sort(parent);
				tree.getTreeViewer().setSelection(
						new StructuredSelection(item), true);
				tree.refresh();
			} else {
				// ロックの取得に失敗した
				m_log.debug("run() : cannot get editLock(jobunitId="+jobunitId+")");
				state.setValue(false);
			}
		} else {
			// 編集モードから抜ける
			if (!editState.isLockedJobunitId(item.getData().getJobunitId())) {
				// ここにくることはないはず(編集モードにいないのに編集モードから抜けるアクション)
				m_log.debug("run() : jobunit("+jobunitId+") is not locked");
				return null;
			}
			try {
				if (MessageDialog.openQuestion(
						null,
						Messages.getString("confirmed"),
						Messages.getString("message.job.103"))) {
					// 編集ロックの開放
					JobEndpointWrapper.getWrapper(managerName).releaseEditLock(editState.getEditSession(item.getData()));
					
					//バックアップに切り戻す
					JobTreeItem backup = editState.getLockedJobunitBackup(item.getData());
					JobTreeItemUtil.removeChildren(parent, item);
					if (backup != null) {
						JobPropertyUtil.setJobFullTree(managerName, backup);
						JobTreeItemUtil.addChildren(parent, backup);
					}
					
					editState.exitEditMode(item);
					m_log.debug("run() : release editLock(jobunitId="+jobunitId+")");
					tree.getTreeViewer().sort(parent);
					tree.refresh();
					
					// ジョブマップ[登録]ビューをクリアする
					// ジョブマップ[登録]ビューをクリアしないと、切り戻し前のジョブツリーがジョブマップに表示されたままになり整合性がとれなくなる
					JobMapEditorView editorView = JobMapActionUtil.getJobMapEditorView();
					if (editorView == null) {
						return null;
					}
					editorView.clear();
				} else {
					//編集ロックを開放しない
					state.setValue(false);
				}
			} catch (InvalidRole_Exception e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				m_log.warn("run() : " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				m_log.warn("run() : " + HinemosMessage.replace(e.getMessage()), e);
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
		super.updateElement(element, parameters);

		if (m_jobTreeItem == null) {
			this.setBaseEnabled(false);
			return;
		}
		
		element.setChecked(JobMapActionUtil.getJobTreeView().getEditEnable());
		
		Integer type = m_jobTreeItem.getData().getType();
		this.setBaseEnabled(
				type == JobConstant.TYPE_JOBUNIT || 
				type == JobConstant.TYPE_JOBNET ||
				type == JobConstant.TYPE_JOB ||
				type == JobConstant.TYPE_FILEJOB ||
				type == JobConstant.TYPE_APPROVALJOB ||
				type == JobConstant.TYPE_MONITORJOB ||
				type == JobConstant.TYPE_REFERJOBNET ||
				type == JobConstant.TYPE_REFERJOB);
	}
}