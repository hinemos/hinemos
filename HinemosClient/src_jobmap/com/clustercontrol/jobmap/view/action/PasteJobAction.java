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

import com.clustercontrol.fault.OtherUserGetLock;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.jobmap.composite.JobMapTreeComposite;
import com.clustercontrol.jobmap.util.JobMapActionUtil;
import com.clustercontrol.jobmap.view.JobMapEditorView;
import com.clustercontrol.jobmap.view.JobTreeView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;

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
		
		JobTreeItemWrapper selectItem = null;
		if (viewPart instanceof JobTreeView) {
			selectItem = ((JobTreeView)viewPart).getSelectJobTreeItem();
		} else if (viewPart instanceof JobMapEditorView) {
			selectItem = ((JobMapEditorView)viewPart).getFocusFigure().getJobTreeItem();
		}

		JobTreeView view = JobMapActionUtil.getJobTreeView();
		if (view == null) {
			return null;
		}

		JobTreeItemWrapper sourceItem = view.getCopyJobTreeItem();
		if(selectItem != null && sourceItem != null){

			boolean copy = false;
			if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT){
				if(selectItem.getData().getType() == JobInfoWrapper.TypeEnum.MANAGER){
					copy = true;
				}
			}
			else if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET){
				if(selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
						selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET){
					copy = true;
				}
			}
			else if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.JOB){
				if(selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
						selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET){
					copy = true;
				}
			}
			else if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.FILEJOB){
				if(selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
						selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET){
					copy = true;
				}
			}
			else if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.APPROVALJOB){
				if(selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
					selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET){
					copy = true;
				}
			}
			else if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.MONITORJOB){
				if(selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
					selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET){
					copy = true;
				}
			}
			else if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.FILECHECKJOB){
				if(selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
					selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET){
					copy = true;
				}
			}
			else if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBLINKSENDJOB){
				if(selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
					selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET){
					copy = true;
				}
			}
			else if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBLINKRCVJOB){
				if(selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
					selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET){
					copy = true;
				}
			}//参照ジョブ
			else if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.REFERJOB){
				if(selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
						selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET){
					copy = true;
				}
			}//参照ジョブネット
			else if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.REFERJOBNET){
				if(selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
						selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET){
					copy = true;
				}
			}//リソース制御ジョブ
			else if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.RESOURCEJOB){
				if(selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
					selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET){
					copy = true;
				}
			}//RPAシナリオジョブ
			else if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.RPAJOB){
				if(selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
					selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET){
					copy = true;
				}
			}

			if(copy){
				if (!JobTreeItemUtil.getManagerName(sourceItem).equals(JobTreeItemUtil.getManagerName(selectItem))) {
					MessageDialog.openWarning(null, Messages.getString("confirmed"), Messages.getString("message.job.124"));
					return null;
				}
				
				JobMapTreeComposite tree = view.getJobMapTreeComposite();
				JobTreeItemWrapper top = (JobTreeItemWrapper)tree.getTreeViewer().getInput();
				m_log.trace("run() setJobunitId = " + selectItem.getData().getJobunitId());
				JobTreeItemWrapper copyItem = null;

				// コピー元のジョブツリーのプロパティーがFullでない場合があるので、
				// ここでコピーしておく。
				JobTreeItemWrapper manager = JobTreeItemUtil.getManager(sourceItem);
				JobPropertyUtil.setJobFullTree(manager.getData().getName(), sourceItem);
				JobInfoWrapper info = manager.getData();
				String managerName = info.getId();
				JobEditState editState = JobEditStateUtil.getJobEditState(managerName);
				if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT){
					copyItem = JobUtil.copy(sourceItem, top, sourceItem.getData().getId(), sourceItem.getData().getOwnerRoleId());
					Integer result = null;
					try {
						result =JobUtil.getEditLock(managerName, copyItem.getData().getJobunitId(), null, false);
					} catch (OtherUserGetLock e) {
						// 他のユーザがロックを取得している
						String message = e.getMessage() + "\n"
								+ HinemosMessage.replace(MessageConstant.MESSAGE_WANT_TO_GET_LOCK.getMessage());
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
		
		JobInfoWrapper info = m_jobTreeItem.getData();
		JobInfoWrapper.TypeEnum type = info.getType();
		String managerName = JobTreeItemUtil.getManagerName(m_jobTreeItem);
		
		boolean enable = false;
		if (type == JobInfoWrapper.TypeEnum.MANAGER) {
			enable = true;
		} else if (managerName != null) {
			JobEditState editState = JobEditStateUtil.getJobEditState(managerName);
			enable = editState.isLockedJobunitId(info.getJobunitId()) && 
					(type == JobInfoWrapper.TypeEnum.JOBUNIT || 
					type == JobInfoWrapper.TypeEnum.JOBNET);
		}
				
		this.setBaseEnabled(enable);
	}
}
