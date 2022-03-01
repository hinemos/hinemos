/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.jobmanagement.dialog.JobDialog;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.jobmap.composite.JobMapTreeComposite;
import com.clustercontrol.jobmap.util.JobMapActionUtil;
import com.clustercontrol.jobmap.view.JobTreeView;
import com.clustercontrol.util.ICheckPublishRestClientWrapper;
import com.clustercontrol.util.Messages;

abstract public class BaseCreateAction  extends BaseAction {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);
		
		JobTreeItemWrapper item = null;
		JobTreeItemWrapper parent = m_jobTreeItem;

		JobInfoWrapper jobInfo = null;
		if (getJobType() == JobInfoWrapper.TypeEnum.JOBUNIT) {
			JobTreeItemWrapper jobTreeItem = JobEditStateUtil.getJobTreeItem();
			if (jobTreeItem == null) {
				// ジョブのキャッシュ情報が最新でないため処理終了
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.46"));
				return null;
			}

			jobInfo = JobTreeItemUtil.createJobInfoWrapper();
			jobInfo.setJobunitId(parent.getData().getJobunitId());
			jobInfo.setId("");
			jobInfo.setName("");
			jobInfo.setType(JobInfoWrapper.TypeEnum.JOBUNIT);
		} else {
			jobInfo = JobTreeItemUtil.getNewJobInfo(parent.getData().getJobunitId(),
				getJobType());
		}

		item = new JobTreeItemWrapper();
		item.setData(jobInfo);
		
		String managerName = null;
		JobTreeItemWrapper mgrTree = JobTreeItemUtil.getManager(parent);
		if(mgrTree == null) {
			managerName = parent.getChildren().get(0).getData().getId();
		} else {
			managerName = mgrTree.getData().getId();
		}
		
		if (getCheckPublishWrapper(managerName) != null) {
			// 対象マネージャのPublishを確認
			boolean isPublish;
			String message = "";
			if (getJobType() == JobInfoWrapper.TypeEnum.RESOURCEJOB) {
				message = Messages.getString("message.xcloud.required");
			} else {
				message = Messages.getString("message.enterprise.required");
			}
			try {
				isPublish = getCheckPublishWrapper(managerName).checkPublish().getPublish();
				if (!isPublish) {
					// エンドポイントはPublishされているがキーファイルが期限切れの場合
					MessageDialog.openWarning(null, Messages.getString("warning"), message);
				}
			} catch (InvalidRole | InvalidUserPass e) {
				MessageDialog.openInformation(null, Messages.getString("message"), e.getMessage());
				return null;
			} catch (HinemosUnknown e) {
				// 原因例外UrlNotFoundの場合、エンドポイントがPublishされていないマネージャからのレスポンス
				if(UrlNotFound.class.equals(e.getCause().getClass())) {
					MessageDialog.openInformation(null, Messages.getString("message"), message);
					return null;
				} else {
					MessageDialog.openInformation(null, Messages.getString("message"), e.getMessage());
					return null;
				}
			} catch (Exception e) {
				// キーファイルを確認できませんでした。処理を終了します。
				// Key file not found. This process will be terminated.
				MessageDialog.openInformation(null, Messages.getString("message"), message);
				return null;
			}
			
		}
		
		JobTreeView view = JobMapActionUtil.getJobTreeView();
		if (view == null) {
			return null;
		}
		JobMapTreeComposite tree = view.getJobMapTreeComposite();

		JobTreeItemUtil.addChildren(parent, item);
		JobDialog dialog = null;
		if (getJobType() == JobInfoWrapper.TypeEnum.REFERJOB) {
			dialog = new JobDialog(
					tree, 
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					managerName,
					false);
		} else {
			dialog = new JobDialog(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					managerName,
					false);
		}
		dialog.setJobTreeItem(item);
		
		//ダイアログ表示
		if (dialog.open() == IDialogConstants.OK_ID) {
			JobEditState editState = JobEditStateUtil.getJobEditState(JobTreeItemUtil.getManagerName(item));
			editState.addEditedJobunit(item);
		} else {
			JobTreeItemUtil.removeChildren(parent, item);
		}

		tree.refresh(parent);
		tree.getTreeViewer().setSelection(tree.getTreeViewer().getSelection(), true);
		tree.updateJobMapEditor(null);

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
		if (getJobType() == JobInfoWrapper.TypeEnum.JOBUNIT) {
			this.setBaseEnabled(type == JobInfoWrapper.TypeEnum.MANAGER);
		} else {
			boolean enable = false;
			String managerName = JobTreeItemUtil.getManagerName(m_jobTreeItem);
			if (managerName != null) {
				JobEditState editState = JobEditStateUtil.getJobEditState(managerName);
				enable = editState.isLockedJobunitId(info.getJobunitId());
			}
			
			this.setBaseEnabled(enable && 
					(type == JobInfoWrapper.TypeEnum.JOBUNIT || 
					type == JobInfoWrapper.TypeEnum.JOBNET));
		}
	}

	abstract public JobInfoWrapper.TypeEnum getJobType();
	
	/**
	 * エンタープライズ機能 / クラウド・VM管理機能利用有無確認エンドポイントを返します。
	 * @param managerName
	 * @return
	 */
	public ICheckPublishRestClientWrapper getCheckPublishWrapper(String managerName) {
		return null;
	}
}