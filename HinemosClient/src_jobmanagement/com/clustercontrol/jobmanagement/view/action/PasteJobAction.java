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

import com.clustercontrol.fault.OtherUserGetLock;
import com.clustercontrol.jobmanagement.composite.JobTreeComposite;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.jobmanagement.view.JobListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;

/**
 * ジョブ貼り付けするクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 2.0.0
 */
public class PasteJobAction extends AbstractHandler implements IElementUpdater{

	/** アクションID */
	public static final String ID = PasteJobAction.class.getName();
	// ログ
	private static Log m_log = LogFactory.getLog( PasteJobAction.class );

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
	 * ジョブ[一覧]ビューにて選択されたジョブツリーアイテムを取得します。<BR>
	 * ジョブ[一覧]ビューにコピー元のジョブツリーアイテムとして設定します。
	 * <p>
	 * <ol>
	 * <li>ジョブ[一覧]ビューにて選択されたジョブツリーアイテムを取得します。</li>
	 * <li>ジョブ[一覧]ビューからコピー元のジョブツリーアイテムを取得します。</li>
	 * <li>コピー元ジョブツリーアイテムから、ジョブツリーアイテムのコピーを作成します。</li>
	 * <li>ジョブツリーアイテムのコピーを選択されたジョブツリーアイテムの子として追加します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.util.JobUtil#copy(JobTreeItem, JobTreeItem)
	 * @see com.clustercontrol.jobmanagement.bean.JobTreeItem#addChildren(JobTreeItem)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (m_log.isTraceEnabled()) {
			m_log.trace("execute() start");
		}
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		if (viewPart instanceof JobListView == false) {
			return null;
		}

		JobListView jobListView = null;
		try {
			jobListView = (JobListView)viewPart.getAdapter(JobListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (jobListView == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		JobTreeItemWrapper selectItem = jobListView.getSelectJobTreeItemList().get(0);
		JobTreeItemWrapper sourceItem = jobListView.getCopyJobTreeItem();
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
			}//承認ジョブ
			else if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.APPROVALJOB){
				if(selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
						selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET){
					copy = true;
				}
			}//監視ジョブ
			else if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.MONITORJOB){
				if(selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
						selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET){
					copy = true;
				}
			}//ファイルチェックジョブ
			else if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.FILECHECKJOB){
				if(selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
						selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET){
					copy = true;
				}
			}//ジョブ連携送信ジョブ
			else if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBLINKSENDJOB){
				if(selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
						selectItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET){
					copy = true;
				}
			}//ジョブ連携待機ジョブ
			else if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBLINKRCVJOB){
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
				
				JobTreeComposite tree = jobListView.getJobTreeComposite();
				JobTreeItemWrapper top = (JobTreeItemWrapper)tree.getTreeViewer().getInput();
				m_log.trace("run() setJobunitId = " + selectItem.getData().getJobunitId());
				JobTreeItemWrapper copyItem = null;

				// コピー元のジョブツリーのプロパティーがFullでない場合があるので、
				// ここでコピーしておく。
				JobTreeItemWrapper srcManager = JobTreeItemUtil.getManager(sourceItem);
				JobPropertyUtil.setJobFullTree(srcManager.getData().getName(), sourceItem);
				
				JobTreeItemWrapper dstManager = JobTreeItemUtil.getManager(selectItem);
				JobPropertyUtil.setJobFullTree(dstManager.getData().getName(), selectItem);
				JobInfoWrapper dstInfo = dstManager.getData();
				String dstManagerName = dstInfo.getId();

				m_log.debug("dest managerName=" + dstManagerName);
				JobEditState jobEditState = JobEditStateUtil.getJobEditState( dstManagerName );
				if(sourceItem.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT){
					copyItem = JobUtil.copy(sourceItem, top, sourceItem.getData().getId(), sourceItem.getData().getOwnerRoleId());
					Integer result = null;
					try {
						result =JobUtil.getEditLock(dstManagerName, copyItem.getData().getJobunitId(), null, false);
					} catch (OtherUserGetLock e) {
						// 他のユーザがロックを取得している
						String message = e.getMessage() + "\n"
								+ HinemosMessage.replace(MessageConstant.MESSAGE_WANT_TO_GET_LOCK.getMessage());
						if (MessageDialog.openQuestion(
								null,
								Messages.getString("confirmed"),
								message)) {
							try {
								result = JobUtil.getEditLock(dstManagerName, copyItem.getData().getJobunitId(), null, true);
							} catch (Exception e1) {
								// ここには絶対にこないはず
								m_log.error("run() : logical error");
							}
						}
					}
					jobEditState.addLockedJobunit(copyItem.getData(), null, result);
				} else{
					copyItem = JobUtil.copy(sourceItem, top, selectItem.getData().getJobunitId(),selectItem.getData().getOwnerRoleId());
				}
				JobTreeItemUtil.addChildren(selectItem, copyItem);
				jobEditState.addEditedJobunit(copyItem);

				tree.getTreeViewer().sort(selectItem);
				tree.refresh(selectItem);
				tree.getTreeViewer().setSelection(new StructuredSelection(selectItem), true);
			} else {
				MessageDialog.openError(null, Messages.getString("failed"),
						Messages.getString("paste") + Messages.getString("failed"));
			}
		}
		if (m_log.isTraceEnabled()) {
			m_log.trace("execute() end");
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
					int size = view.getJobTreeComposite().getSelectItemList().size();
					if(size == 1 && view.getDataType() == JobInfoWrapper.TypeEnum.MANAGER){
						editEnable = true;
					}else if(size == 1 && view.getEditEnable()){
						if(view.getDataType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
								view.getDataType() == JobInfoWrapper.TypeEnum.JOBNET){
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
