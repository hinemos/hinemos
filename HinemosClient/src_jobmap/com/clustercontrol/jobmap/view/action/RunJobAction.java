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
import org.openapitools.client.model.RunJobRequest;
import org.openapitools.client.model.RunJobRequest.TriggerTypeEnum;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.dialog.JobRunConfirm;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobInfoWrapper;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

/**
 * ジョブ[一覧]ビューの「実行」のクライアント側アクションクラス<BR>
 * 
 * @version 2.0.0
 * @since 1.0.0
 */
public class RunJobAction extends BaseAction {
	public static final String ID = ActionIdBase + RunJobAction.class.getSimpleName();
	
	private RunJobRequest m_trigger = new RunJobRequest();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);
		
		List<JobTreeItemWrapper> itemList = m_jobTreeItemList;
		
		if (itemList == null || itemList.size() == 0) {
			return null;
		}
		
		StringBuffer jobListMessage = new StringBuffer();
		jobListMessage.append(com.clustercontrol.jobmap.messages.Messages.getString("message.job.125"));
		jobListMessage.append("\n");
		
		for (JobTreeItemWrapper item : itemList) {
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
			for (JobTreeItemWrapper item : itemList) {
				JobTreeItemWrapper manager = JobTreeItemUtil.getManager(item);
				String managerName = manager.getData().getName();
				try {
					// 実行契機情報を登録
					RunJobRequest triggerInfo = new RunJobRequest(); 
					triggerInfo.setTriggerType(TriggerTypeEnum.MANUAL); 
					triggerInfo.setJobWaitTime(m_trigger.getJobWaitTime()); 
					triggerInfo.setJobWaitMinute(m_trigger.getJobWaitMinute()); 
					triggerInfo.setJobCommand(m_trigger.getJobCommand()); 
					triggerInfo.setJobCommandText(m_trigger.getJobCommandText()); 

					JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName); 
					RunJobRequest request = new RunJobRequest();
					RestClientBeanUtil.convertBean(triggerInfo, request);
					
					wrapper.runJob(item.getData().getJobunitId(), item.getData().getId(), triggerInfo);
				} catch (InvalidRole e) {
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
		
		for (JobTreeItemWrapper item : m_jobTreeItemList) {
			if (item.getData().getType() == JobInfoWrapper.TypeEnum.JOBUNIT ||
					item.getData().getType() == JobInfoWrapper.TypeEnum.JOBNET ||
					item.getData().getType() == JobInfoWrapper.TypeEnum.JOB ||
					item.getData().getType() == JobInfoWrapper.TypeEnum.FILEJOB ||
					item.getData().getType() == JobInfoWrapper.TypeEnum.APPROVALJOB ||
					item.getData().getType() == JobInfoWrapper.TypeEnum.MONITORJOB ||
					item.getData().getType() == JobInfoWrapper.TypeEnum.FILECHECKJOB ||
					item.getData().getType() == JobInfoWrapper.TypeEnum.JOBLINKSENDJOB ||
					item.getData().getType() == JobInfoWrapper.TypeEnum.JOBLINKRCVJOB ||
					item.getData().getType() == JobInfoWrapper.TypeEnum.REFERJOBNET ||
					item.getData().getType() == JobInfoWrapper.TypeEnum.REFERJOB ||
					item.getData().getType() == JobInfoWrapper.TypeEnum.RESOURCEJOB ||
					item.getData().getType() == JobInfoWrapper.TypeEnum.RPAJOB) {
				editEnable = true;
			} else {
				editEnable = false;
				break;
			}
		}
		
		this.setBaseEnabled(editEnable);
	}
}