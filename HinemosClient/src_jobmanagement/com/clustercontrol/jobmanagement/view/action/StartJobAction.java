/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.openapitools.client.model.JobOperationPropResponse;
import org.openapitools.client.model.JobOperationPropResponse.AvailableOperationListEnum;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.jobmanagement.OperationMessage;
import com.clustercontrol.jobmanagement.action.OperationJob;
import com.clustercontrol.jobmanagement.bean.JobOperationConstant;
import com.clustercontrol.jobmanagement.composite.DetailComposite;
import com.clustercontrol.jobmanagement.composite.HistoryComposite;
import com.clustercontrol.jobmanagement.composite.NodeDetailComposite;
import com.clustercontrol.jobmanagement.dialog.JobOperationDialog;
import com.clustercontrol.jobmanagement.preference.JobManagementPreferencePage;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.view.JobDetailView;
import com.clustercontrol.jobmanagement.view.JobHistoryView;
import com.clustercontrol.jobmanagement.view.JobNodeDetailView;
import com.clustercontrol.jobmanagement.view.JobQueueContentsView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;

/**
 * ジョブ[履歴]・ジョブ[ジョブ詳細]・ジョブ[ノード詳細]・ジョブ[同時実行制御状況]ビューの
 * 「開始」のクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public abstract class StartJobAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( StartJobAction.class );

	/** アクションID */
	public static final String ID = StartJobAction.class.getName();

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	/**
	 * 開始用プロパティ取得
	 *
	 * @param managerName マネージャ名
	 * @param sessionId セッションID
	 * @param jobunitId 所属ジョブユニットのジョブID
	 * @param jobId ジョブID
	 * @param facilityId ファシリティID
	 * @return ジョブ開始操作用プロパティ
	 *
	 */
	private Property getStartProperty(String managerName, String sessionId, String jobunitId, String jobId, String jobName, String facilityId) {
		Locale locale = Locale.getDefault();
		//セッションID
		Property session =
				new Property(JobOperationConstant.SESSION, Messages.getString("session.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		//ジョブユニットID
		Property jobUnit =
				new Property(JobOperationConstant.JOB_UNIT, Messages.getString("jobunit.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		//ジョブID
		Property job =
				new Property(JobOperationConstant.JOB, Messages.getString("job.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		//ジョブ名
		Property name =
				new Property(JobOperationConstant.JOB_NAME, Messages.getString("job.name", locale), PropertyDefineConstant.EDITOR_TEXT);
		//ファシリティID
		Property facility =
				new Property(JobOperationConstant.FACILITY, Messages.getString("facility.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		//制御
		Property control =
				new Property(JobOperationConstant.CONTROL, Messages.getString("control", locale), PropertyDefineConstant.EDITOR_SELECT);

		List<AvailableOperationListEnum> values = null;
		try {
			JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
			JobOperationPropResponse response = new JobOperationPropResponse();
			if(facilityId == null){
				response = wrapper.getAvailableStartOperationSessionJob(sessionId, jobunitId, jobId);
				values = response.getAvailableOperationList();
			} else {
				response = wrapper.getAvailableStartOperationSessionNode(sessionId, jobunitId, jobId, facilityId);
				values = response.getAvailableOperationList();
			}
		} catch (InvalidRole e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
			throw new InternalError("values is null.");
		} catch (Exception e) {
			m_log.warn("getStartProperty(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			throw new InternalError("values is null.");
		}

		// 値をtypeからStringに変換
		List<String> valuesStr = new ArrayList<String>();
		for (AvailableOperationListEnum controlType : values) {
			valuesStr.add(OperationMessage.enumToString(controlType));
		}
		
		//値を初期化
		if(values.size() >= 1){
			Object controlValues[][] = {valuesStr.toArray(), valuesStr.toArray()};
			control.setSelectValues(controlValues);
			control.setValue(valuesStr.get(0));
		}
		else{
			Object controlValues[][] = {{""}, {""}};
			control.setSelectValues(controlValues);
			control.setValue("");
		}

		session.setValue(sessionId);
		jobUnit.setValue(jobunitId);
		job.setValue(jobId);
		name.setValue(jobName);
		if(facilityId != null && facilityId.length() > 0){
			facility.setValue(facilityId);
		}
		else{
			facility.setValue("");
		}

		//変更の可/不可を設定
		session.setModify(PropertyDefineConstant.MODIFY_NG);
		jobUnit.setModify(PropertyDefineConstant.MODIFY_NG);
		job.setModify(PropertyDefineConstant.MODIFY_NG);
		name.setModify(PropertyDefineConstant.MODIFY_NG);
		facility.setModify(PropertyDefineConstant.MODIFY_NG);
		control.setModify(PropertyDefineConstant.MODIFY_OK);

		Property property = new Property(null, null, "");

		// 初期表示ツリーを構成。
		property.removeChildren();
		property.addChildren(session);
		property.addChildren(jobUnit);
		property.addChildren(job);
		property.addChildren(name);
		if(facilityId != null && facilityId.length() > 0){
			property.addChildren(facility);
		}
		property.addChildren(control);

		return property;
	}

	/**
	 * ジョブ[履歴]・ジョブ[ジョブ詳細]・ジョブ[ノード詳細]・ジョブ[同時実行制御状況]ビューの
	 * 「開始」が押された場合に、ジョブの開始操作を行います。
	 * <p>
	 * <ol>
	 * <li>ジョブ[履歴]・ジョブ[ジョブ詳細]・ジョブ[同時実行制御状況]ビューの場合、ビューからセッションID・ジョブIDを取得します。</li>
	 * <li>ジョブ[ノード詳細]ビューの場合、ビューから、セッションID・ジョブID・ファシリティIDを取得します。</li>
	 * <li>ジョブ開始操作用プロパティを取得します。</li>
	 * <li>ジョブ[開始]ダイアログを表示します。</li>
	 * <li>ジョブ[開始]ダイアログからジョブ開始操作用プロパティを取得します。</li>
	 * <li>ジョブ開始操作用プロパティを元にジョブ開始操作を行います。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.view.JobHistoryView
	 * @see com.clustercontrol.jobmanagement.view.JobDetailView
	 * @see com.clustercontrol.jobmanagement.view.JobNodeDetailView
	 * @see com.clustercontrol.jobmanagement.dialog.JobOperationDialog
	 * @see com.clustercontrol.jobmanagement.action.OperationJob#operationJob(Property)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String managerName = null;
		String sessionId = null;
		String jobunitId = null;
		String jobId = null;
		String jobName = null;
		String facilityId = null;

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow( event );

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);

		if (viewPart instanceof JobHistoryView) { // ボタンが押された場合
			JobHistoryView jobHistoryView = null;
			try {
				jobHistoryView = (JobHistoryView) viewPart
						.getAdapter(JobHistoryView.class);
			} catch (Exception e) { 
				m_log.info("execute " + e.getMessage()); 
				return null; 
			}

			if (jobHistoryView == null) {
				m_log.info("execute: job history view is null"); 
			} else {
				HistoryComposite historyComposite = jobHistoryView.getComposite();
				//マネージャ名取得
				managerName = historyComposite.getManagerName();
				//セッションID取得
				sessionId = historyComposite.getSessionId();
				//ジョブユニットID取得
				jobunitId = historyComposite.getJobunitId();
				//ジョブID取得
				jobId = historyComposite.getJobId();
				//ジョブID取得
				jobName = historyComposite.getJobName();
			}
		} else if (viewPart instanceof JobDetailView) { // ボタンが押された場合
			JobDetailView jobDetailView = null;
			try { 
				jobDetailView = (JobDetailView) viewPart
						.getAdapter(JobDetailView.class);
			} catch (Exception e) { 
				m_log.info("execute " + e.getMessage()); 
				return null; 
			}

			if (jobDetailView == null) {
				m_log.info("execute: job detail view is null"); 
			} else {
				DetailComposite detailComposite = jobDetailView.getComposite();
				//マネージャ名取得
				managerName = detailComposite.getManagerName();
				//セッションID取得
				sessionId = detailComposite.getSessionId();
				//ジョブユニットID取得
				jobunitId = detailComposite.getJobunitId();
				//ジョブID取得
				jobId = detailComposite.getJobId();
				//ジョブID取得
				jobName = detailComposite.getJobName();
			}
		} else if (viewPart instanceof JobNodeDetailView) { // ボタンが押された場合
			JobNodeDetailView jobNodeDetailView = null;
			try {
				jobNodeDetailView = (JobNodeDetailView) viewPart
						.getAdapter(JobNodeDetailView.class);
			} catch (Exception e) { 
				m_log.info("execute " + e.getMessage()); 
				return null; 
			}

			if (jobNodeDetailView == null) {
				m_log.info("execute: job node detail view is null"); 
			} else {
				NodeDetailComposite nodeDetailComposite = jobNodeDetailView.getComposite();
				//マネージャ名取得
				managerName = nodeDetailComposite.getManagerName();
				//セッションID取得
				sessionId = nodeDetailComposite.getSessionId();
				//ジョブユニットID取得
				jobunitId = nodeDetailComposite.getJobunitId();
				//ジョブID取得
				jobId = nodeDetailComposite.getJobId();
				//ジョブ名取得
				jobName = nodeDetailComposite.getJobName();
				//ファシリティID取得
				facilityId = nodeDetailComposite.getFacilityId();
				if (facilityId == null) {
					sessionId = null;
					jobunitId = null;
					jobId = null;
					jobName = null;
				}
			}
		} else if (viewPart instanceof JobQueueContentsView) {
			JobQueueContentsView view = null;
			try {
				view = (JobQueueContentsView) viewPart
						.getAdapter(JobQueueContentsView.class);
			} catch (Exception e) { 
				m_log.info("execute " + e.getMessage()); 
				return null; 
			}

			if (view == null) {
				m_log.info("execute: JobQueueContentsView is null");
			} else {
				managerName = view.getManagerName();
				sessionId = view.getSelectedSessionId();
				jobunitId = view.getSelectedJobunitId();
				jobId = view.getSelectedJobId();
				jobName = view.getSelectedJobName();
			}
		}

		if (sessionId != null && sessionId.length() > 0 && jobunitId != null
				&& jobunitId.length() > 0 && jobId != null
				&& jobId.length() > 0) {

			JobOperationDialog dialog = new JobOperationDialog(window.getShell());

			//プロパティ設定
			dialog.setProperty(getStartProperty(managerName, sessionId, jobunitId, jobId, jobName, facilityId));
			dialog.setTitleText(Messages.getString("job") + "["
					+ Messages.getString("start") + "]");

			//ダイアログ表示
			if (dialog.open() == IDialogConstants.OK_ID) {
				// 確認ダイアログを表示するかどうかのフラグをPreferenceから取得
				if (ClusterControlPlugin.getDefault().getPreferenceStore().getBoolean(JobManagementPreferencePage.P_HISTORY_CONFIRM_DIALOG_FLG)) {
					StringBuffer jobListMessage = new StringBuffer(); 
					jobListMessage.append(Messages.getString("dialog.job.start.confirm")); 
					jobListMessage.append("\n");
					Object[] args1 = { managerName, jobName, jobId, jobunitId, sessionId };
					jobListMessage.append(Messages.getString(Messages.getString("dialog.job.confirm.name"), args1));
					if(!MessageDialog.openConfirm(
						null,
						Messages.getString("confirmed"),
						jobListMessage.toString())) {
						// OKが押されない場合は処理しない
						return null;
					}
				}
				//ジョブ開始
				OperationJob operation = new OperationJob();
				operation.operationJob(managerName, dialog.getProperty());
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
				if(part instanceof JobHistoryView){
					// Enable button when 1 item is selected
					JobHistoryView view = (JobHistoryView)part;
					if(view.getSelectedNum() > 0) {
						editEnable = true;
					}
				} else if(part instanceof JobDetailView) {
					JobDetailView view = (JobDetailView)part;
					if(view.getSelectedNum() > 0) {
						editEnable = true;
					}
				} else if(part instanceof JobNodeDetailView) {
					JobNodeDetailView view = (JobNodeDetailView)part;
					if(view.getSelectedNum() > 0) {
						editEnable = true;
					}
				} else if (part instanceof JobQueueContentsView) {
					JobQueueContentsView view = (JobQueueContentsView) part;
					if (view.getSelectedCount() > 0) {
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
