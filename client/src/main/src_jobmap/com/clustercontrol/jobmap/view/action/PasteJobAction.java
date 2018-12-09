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
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.jobmap.composite.JobMapTreeComposite;
import com.clustercontrol.jobmap.util.JobMapActionUtil;
import com.clustercontrol.jobmap.view.JobMapEditorView;
import com.clustercontrol.jobmap.view.JobTreeView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.jobmanagement.OtherUserGetLock_Exception;

/**
 * ジョブ貼り付けするクライアント側アクションクラス<BR>
 * 
 */
public class PasteJobAction extends BaseAction {
	private static Log m_log = LogFactory.getLog( PasteJobAction.class );
	public static final String ID = ActionIdBase + PasteJobAction.class.getSimpleName();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);
		
		JobTreeItem selectItem = null;
		if (viewPart instanceof JobTreeView) {
			selectItem = ((JobTreeView)viewPart).getSelectJobTreeItem();
		} else if (viewPart instanceof JobMapEditorView) {
			selectItem = ((JobMapEditorView)viewPart).getFocusFigure().getJobTreeItem();
		}

		JobTreeView view = JobMapActionUtil.getJobTreeView();
		if (view == null) {
			return null;
		}

		JobTreeItem sourceItem = view.getCopyJobTreeItem();
		if(selectItem != null && sourceItem != null){

			boolean copy = false;
			if(sourceItem.getData().getType() == JobConstant.TYPE_JOBUNIT){
				if(selectItem.getData().getType() == JobConstant.TYPE_MANAGER){
					copy = true;
				}
			}
			else if(sourceItem.getData().getType() == JobConstant.TYPE_JOBNET){
				if(selectItem.getData().getType() == JobConstant.TYPE_JOBUNIT ||
						selectItem.getData().getType() == JobConstant.TYPE_JOBNET){
					copy = true;
				}
			}
			else if(sourceItem.getData().getType() == JobConstant.TYPE_JOB){
				if(selectItem.getData().getType() == JobConstant.TYPE_JOBUNIT ||
						selectItem.getData().getType() == JobConstant.TYPE_JOBNET){
					copy = true;
				}
			}
			else if(sourceItem.getData().getType() == JobConstant.TYPE_FILEJOB){
				if(selectItem.getData().getType() == JobConstant.TYPE_JOBUNIT ||
						selectItem.getData().getType() == JobConstant.TYPE_JOBNET){
					copy = true;
				}
			}
			else if(sourceItem.getData().getType() == JobConstant.TYPE_APPROVALJOB){
				if(selectItem.getData().getType() == JobConstant.TYPE_JOBUNIT ||
					selectItem.getData().getType() == JobConstant.TYPE_JOBNET){
					copy = true;
				}
			}
			else if(sourceItem.getData().getType() == JobConstant.TYPE_MONITORJOB){
				if(selectItem.getData().getType() == JobConstant.TYPE_JOBUNIT ||
					selectItem.getData().getType() == JobConstant.TYPE_JOBNET){
					copy = true;
				}
			}//参照ジョブ
			else if(sourceItem.getData().getType() == JobConstant.TYPE_REFERJOB){
				if(selectItem.getData().getType() == JobConstant.TYPE_JOBUNIT ||
						selectItem.getData().getType() == JobConstant.TYPE_JOBNET){
					copy = true;
				}
			}//参照ジョブネット
			else if(sourceItem.getData().getType() == JobConstant.TYPE_REFERJOBNET){
				if(selectItem.getData().getType() == JobConstant.TYPE_JOBUNIT ||
						selectItem.getData().getType() == JobConstant.TYPE_JOBNET){
					copy = true;
				}
			}

			if(copy){
				if (!JobTreeItemUtil.getManagerName(sourceItem).equals(JobTreeItemUtil.getManagerName(selectItem))) {
					MessageDialog.openWarning(null, Messages.getString("confirmed"), Messages.getString("message.job.124"));
					return null;
				}
				
				JobMapTreeComposite tree = view.getJobMapTreeComposite();
				JobTreeItem top = (JobTreeItem)tree.getTreeViewer().getInput();
				m_log.trace("run() setJobunitId = " + selectItem.getData().getJobunitId());
				JobTreeItem copyItem = null;

				// コピー元のジョブツリーのプロパティーがFullでない場合があるので、
				// ここでコピーしておく。
				JobTreeItem manager = JobTreeItemUtil.getManager(sourceItem);
				JobPropertyUtil.setJobFullTree(manager.getData().getName(), sourceItem);
				JobInfo info = manager.getData();
				String managerName = info.getId();
				JobEditState editState = JobEditStateUtil.getJobEditState(managerName);
				if(sourceItem.getData().getType() == JobConstant.TYPE_JOBUNIT){
					copyItem = JobUtil.copy(sourceItem, top, sourceItem.getData().getId(), sourceItem.getData().getOwnerRoleId());
					Integer result = null;
					try {
						result =JobUtil.getEditLock(managerName, copyItem.getData().getJobunitId(), null, false);
					} catch (OtherUserGetLock_Exception e) {
						// 他のユーザがロックを取得している
						String message = HinemosMessage.replace(e.getMessage());
						if (MessageDialog.openQuestion(
								null,
								Messages.getString("confirmed"),
								message)) {
							try {
								result = JobUtil.getEditLock(managerName, copyItem.getData().getJobunitId(), null, true);
							} catch (Exception e1) {
								// ここには絶対にこないはず
								m_log.error("run() : logical error");
							}
						}
					}
					editState.addLockedJobunit(copyItem.getData(), null, result);
				} else{
					copyItem = JobUtil.copy(sourceItem, top, selectItem.getData().getJobunitId(),selectItem.getData().getOwnerRoleId());
				}
				JobTreeItemUtil.addChildren(selectItem, copyItem);
				editState.addEditedJobunit(copyItem);

				tree.getTreeViewer().sort(selectItem);
				tree.refresh(selectItem);
				tree.getTreeViewer().setSelection(
						new StructuredSelection(selectItem), true);
				tree.updateJobMapEditor(null);
			} else {
				MessageDialog.openError(null, Messages.getString("failed"),
						Messages.getString("paste") + Messages.getString("failed"));
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
		String managerName = JobTreeItemUtil.getManagerName(m_jobTreeItem);
		
		boolean enable = false;
		if (type == JobConstant.TYPE_MANAGER) {
			enable = true;
		} else if (managerName != null) {
			JobEditState editState = JobEditStateUtil.getJobEditState(managerName);
			enable = editState.isLockedJobunitId(info.getJobunitId()) && 
					(type == JobConstant.TYPE_JOBUNIT || 
					type == JobConstant.TYPE_JOBNET);
		}
				
		this.setBaseEnabled(enable);
	}
}
