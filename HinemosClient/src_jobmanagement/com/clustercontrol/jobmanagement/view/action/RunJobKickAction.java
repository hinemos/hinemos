/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view.action;

import java.util.ArrayList;
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
import org.openapitools.client.model.RunJobRequest;
import org.openapitools.client.model.RunJobRequest.TriggerTypeEnum;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.action.GetJobKickTableDefine;
import com.clustercontrol.jobmanagement.composite.JobKickListComposite;
import com.clustercontrol.jobmanagement.dialog.JobKickRunConfirm;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.view.JobKickListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;

/**
 * ジョブ実行契機[一覧]ビューの「実行」のクライアント側アクションクラス<BR>
 *
 * @version 5.1.0
 */
public class RunJobKickAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( RunJobKickAction.class );

	private RunJobRequest m_trigger = new RunJobRequest();

	/** アクションID */
	public static final String ID = RunJobKickAction.class.getName();
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
	 * ジョブ実行契機[一覧]ビューの「実行」が押された場合に、ジョブを実行します。
	 * <p>
	 * <ol>
	 * <li>ジョブ実行契機[一覧]ビューから選択されたジョブツリーアイテムを取得します。</li>
	 * <li>実行の確認ダイアログを表示します。</li>
	 * <li>ジョブを実行します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.view.JobListView
	 * @see com.clustercontrol.jobmanagement.action.RunJob#runJob(String)
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

		// 選択アイテムの取得
		JobKickListView view = null;
		try {
			view = (JobKickListView) this.viewPart.getAdapter(JobKickListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		JobKickListComposite composite = view.getComposite();
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();
		
		Object [] objs = selection.toArray();
		
		// 1つも選択されていない場合
		if (objs.length == 0) {
			MessageDialog.openConfirm(
					null,
					Messages.getString("confirmed"),
					Messages.getString("message.job.24"));
			return null;
		}

		// ジョブ実行確認ダイアログメッセージの生成 
		StringBuffer jobListMessage = new StringBuffer();
		jobListMessage.append(Messages.getString("message.job.125")); 
		jobListMessage.append("\n");

		// 1つ以上選択されている場合、先頭の１つ実行
		String managerName = (String) ((ArrayList<?>)objs[0]).get(GetJobKickTableDefine.MANAGER_NAME);
		String jobkickId = (String) ((ArrayList<?>)objs[0]).get(GetJobKickTableDefine.JOBKICK_ID);
		String jobunitId = (String) ((ArrayList<?>)objs[0]).get(GetJobKickTableDefine.JOBUNIT_ID); 
		String jobId = (String) ((ArrayList<?>)objs[0]).get(GetJobKickTableDefine.JOB_ID); 
		String jobName = (String) ((ArrayList<?>)objs[0]).get(GetJobKickTableDefine.JOB_NAME);

		// 実行対象のジョブリストを作成(ダイアログ表示用)
		Object[] args1 = { jobName, managerName, jobId, jobunitId };
		jobListMessage.append(Messages.getString(Messages.getString("message.job.32"), args1));
		jobListMessage.append("\n");

		// ジョブ実行確認ダイアログを生成
		JobKickRunConfirm dialog = new JobKickRunConfirm(null, managerName, jobkickId);
		dialog.setMessageText(jobListMessage.toString());

		if (dialog.open() == IDialogConstants.OK_ID) {
			m_trigger = dialog.getInputData();
			try {
				// 実行契機情報を登録
				RunJobRequest triggerInfo = new RunJobRequest();
				triggerInfo.setTriggerType(TriggerTypeEnum.MANUAL);
				triggerInfo.setJobWaitTime(m_trigger.getJobWaitTime());
				triggerInfo.setJobWaitMinute(m_trigger.getJobWaitMinute()); 
				triggerInfo.setJobCommand(m_trigger.getJobCommand()); 
				triggerInfo.setJobCommandText(m_trigger.getJobCommandText());
				triggerInfo.setJobkickId(jobkickId);
				// ランタイムジョブ変数
				if (m_trigger.getJobRuntimeParamList() != null) {
					triggerInfo.getJobRuntimeParamList().addAll(
							m_trigger.getJobRuntimeParamList());
				}
				
				JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);  
				RunJobRequest request = new RunJobRequest();
				RestClientBeanUtil.convertBean(triggerInfo, request);
				wrapper.runJob( jobunitId, jobId, request); 
			} catch (InvalidRole e) { 
				MessageDialog.openInformation(null, Messages.getString("message"), 
						Messages.getString("message.accesscontrol.16")); 
			} catch (Exception e) { 
				m_log.warn("run(), " + e.getMessage(), e); 
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
				if(part instanceof JobKickListView){
					// Run button when 1 item is selected
					JobKickListView view = (JobKickListView)part;
					if (view.getSelectedNum() > 0) {
						editEnable = true;
					}
				}
				this.setBaseEnabled( editEnable );
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
