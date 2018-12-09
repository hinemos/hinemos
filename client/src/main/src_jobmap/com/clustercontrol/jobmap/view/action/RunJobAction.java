/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobTriggerTypeConstant;
import com.clustercontrol.jobmanagement.dialog.JobRunConfirm;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.jobmanagement.JobTriggerInfo;

/**
 * ジョブ[一覧]ビューの「実行」のクライアント側アクションクラス<BR>
 * 
 * @version 2.0.0
 * @since 1.0.0
 */
public class RunJobAction extends BaseAction {
	public static final String ID = ActionIdBase + RunJobAction.class.getSimpleName();
	
	private JobTriggerInfo m_trigger = new JobTriggerInfo();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);
		
		List<JobTreeItem> itemList = m_jobTreeItemList;
		
		if (itemList == null || itemList.size() == 0) {
			return null;
		}
		
		StringBuffer jobListMessage = new StringBuffer();
		jobListMessage.append(com.clustercontrol.jobmap.messages.Messages.getString("message.job.125"));
		jobListMessage.append("\n");
		
		for (JobTreeItem item : itemList) {
			String managerName = JobTreeItemUtil.getManagerName(item);
		
			JobEditState editState = JobEditStateUtil.getJobEditState(JobTreeItemUtil.getManagerName(item));
			if(editState.isEditing()){
				// 確認ダイアログを生成
				MessageDialog.openWarning(
						null,
						Messages.getString("confirmed"),
						Messages.getString("message.job.43") + "\n" +
						Messages.getString("message.job.44"));
				return null;
			}

			// 確認ダイアログ用の文字列を生成
			Object[] args1 = { item.getData().getName(), managerName, item.getData().getId(), item.getData().getJobunitId() };
			jobListMessage.append(Messages.getString(com.clustercontrol.jobmap.messages.Messages.getString("message.job.32"), args1));
			jobListMessage.append("\n");
		}
		
		//実行確認ダイアログ表示
		JobRunConfirm dialog = new JobRunConfirm(null);
		dialog.setMessageText(jobListMessage.toString());
		if (dialog.open() == IDialogConstants.OK_ID) {
			m_trigger = dialog.getInputData();
			
			// 選択しているジョブの実行
			for (JobTreeItem item : itemList) {
				try {
					// 実行契機情報を登録
					JobTriggerInfo triggerInfo = new JobTriggerInfo();
					triggerInfo.setTriggerType(JobTriggerTypeConstant.TYPE_MANUAL);
					triggerInfo.setJobWaitTime(m_trigger.isJobWaitTime());
					triggerInfo.setJobWaitMinute(m_trigger.isJobWaitMinute());
					triggerInfo.setJobCommand(m_trigger.isJobCommand());
					triggerInfo.setJobCommandText(m_trigger.getJobCommandText());
	
					JobTreeItem manager = JobTreeItemUtil.getManager(item);
					String managerName = manager.getData().getName();
					JobEndpointWrapper.getWrapper(managerName).runJob(
							item.getData().getJobunitId(),
							item.getData().getId(),
							null,
							triggerInfo);
				} catch (InvalidRole_Exception e) {
					MessageDialog.openInformation(null, Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} catch (Exception e) {
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				}
			}
		}
		
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		super.updateElement(element, parameters);

		boolean editEnable = false;
		
		if (m_jobTreeItemList == null) {
			this.setBaseEnabled(false);
			return;
		}
		
		for (JobTreeItem item : m_jobTreeItemList) {
			if (item.getData().getType() == JobConstant.TYPE_JOBUNIT ||
					item.getData().getType() == JobConstant.TYPE_JOBNET ||
					item.getData().getType() == JobConstant.TYPE_JOB ||
					item.getData().getType() == JobConstant.TYPE_FILEJOB ||
					item.getData().getType() == JobConstant.TYPE_APPROVALJOB ||
					item.getData().getType() == JobConstant.TYPE_MONITORJOB ||
					item.getData().getType() == JobConstant.TYPE_REFERJOBNET ||
					item.getData().getType() == JobConstant.TYPE_REFERJOB) {
				editEnable = true;
			} else {
				editEnable = false;
				break;
			}
		}
		
		this.setBaseEnabled(editEnable);
	}
}